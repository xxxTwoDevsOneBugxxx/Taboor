using MediatR;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;
using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Tenants.Commands.CancelTenantSubscription;

/// <summary>
/// Handler for CancelTenantSubscriptionCommand.
/// Enforces business rules for cancellation, transitions subscription to Canceled status,
/// and persists cancellation metadata.
/// </summary>
public class CancelTenantSubscriptionCommandHandler(IApplicationDbContext context)
    : IRequestHandler<CancelTenantSubscriptionCommand>
{
    public async Task Handle(CancelTenantSubscriptionCommand request, CancellationToken ct)
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
            .Where(s => s.TenantId == request.TenantId)
            .OrderByDescending(s => s.StartDate)
            .FirstOrDefaultAsync(ct);

        if (currentSub is null)
        {
            throw AppException.Conflict("Tenant does not have an active subscription to cancel.");
        }

        if (currentSub.Status == SubscriptionStatus.Canceled)
        {
            throw AppException.Conflict("Tenant's subscription is already canceled.");
        }

        if (currentSub.Status == SubscriptionStatus.Expired)
        {
            throw AppException.Conflict("Subscription has already expired and cannot be canceled.");
        }

        // 3. Execute domain behavior
        currentSub.Cancel(request.Reason);

        await context.SaveChangesAsync(ct);
    }
}
