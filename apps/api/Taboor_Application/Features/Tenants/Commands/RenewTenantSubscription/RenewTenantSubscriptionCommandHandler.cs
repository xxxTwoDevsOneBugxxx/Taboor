using MediatR;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;
using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Tenants.Commands.RenewTenantSubscription;

/// <summary>
/// Handler for RenewTenantSubscriptionCommand.
/// Extends the active subscription period based on plan billing cycle or custom extension days.
/// </summary>
public class RenewTenantSubscriptionCommandHandler(IApplicationDbContext context)
    : IRequestHandler<RenewTenantSubscriptionCommand>
{
    public async Task Handle(RenewTenantSubscriptionCommand request, CancellationToken ct)
    {
        // 1. Ensure Tenant exists
        var tenantExists = await context.Tenants
            .AnyAsync(t => t.Id == request.TenantId, ct);

        if (!tenantExists)
        {
            throw AppException.NotFound($"Tenant with ID ({request.TenantId}) was not found.");
        }

        // 2. Retrieve latest subscription
        var currentSub = await context.TenantSubscriptions
            .Include(s => s.Plan)
            .Where(s => s.TenantId == request.TenantId)
            .OrderByDescending(s => s.StartDate)
            .FirstOrDefaultAsync(ct);

        if (currentSub is null)
        {
            throw AppException.Conflict("Tenant does not have an existing subscription to renew. Please assign a plan first.");
        }

        // 3. Calculate new EndDate extending from existing EndDate if still in future, or from now if expired
        var now = DateTime.UtcNow;
        var baseDate = currentSub.EndDate > now ? currentSub.EndDate : now;

        var cycle = request.BillingCycle ?? currentSub.Plan.BillingCycle;
        DateTime newEndDate;

        if (request.CustomDays.HasValue && request.CustomDays.Value > 0)
        {
            newEndDate = baseDate.AddDays(request.CustomDays.Value);
        }
        else if (cycle == BillingCycle.Yearly)
        {
            newEndDate = baseDate.AddYears(1);
        }
        else
        {
            newEndDate = baseDate.AddMonths(1);
        }

        // 4. Execute domain behavior
        currentSub.Renew(newEndDate, request.ExternalPaymentId);

        await context.SaveChangesAsync(ct);
    }
}
