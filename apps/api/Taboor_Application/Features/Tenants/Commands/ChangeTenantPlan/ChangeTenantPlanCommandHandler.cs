using MediatR;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;
using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Tenants.Commands.ChangeTenantPlan;

/// <summary>
/// Handler for ChangeTenantPlanCommand.
/// Enforces business rules for plan changes, calculates end date, and persists updated contract state.
/// </summary>
public class ChangeTenantPlanCommandHandler(IApplicationDbContext context)
    : IRequestHandler<ChangeTenantPlanCommand>
{
    public async Task Handle(ChangeTenantPlanCommand request, CancellationToken ct)
    {
        // 1. Ensure Tenant exists
        var tenant = await context.Tenants
            .FirstOrDefaultAsync(t => t.Id == request.TenantId, ct);

        if (tenant is null)
        {
            throw AppException.NotFound($"Tenant with ID ({request.TenantId}) was not found.");
        }

        // 2. Ensure Target Plan exists and is active
        var newPlan = await context.Plans
            .FirstOrDefaultAsync(p => p.Id == request.NewPlanId, ct);

        if (newPlan is null)
        {
            throw AppException.NotFound($"Plan with ID ({request.NewPlanId}) was not found.");
        }

        if (!newPlan.IsActive)
        {
            throw AppException.Conflict($"Plan '{newPlan.Name}' is currently inactive and cannot be assigned.");
        }

        // 3. Find latest/current subscription
        var currentSub = await context.TenantSubscriptions
            .Include(s => s.Plan)
            .Where(s => s.TenantId == request.TenantId)
            .OrderByDescending(s => s.StartDate)
            .FirstOrDefaultAsync(ct);

        var now = DateTime.UtcNow;

        // Check if tenant is already on this plan actively
        if (currentSub is not null &&
            currentSub.PlanId == newPlan.Id &&
            currentSub.Status == SubscriptionStatus.Active &&
            currentSub.EndDate > now)
        {
            throw AppException.Conflict($"Tenant is already actively subscribed to '{newPlan.Name}'. Use renewal to extend the period.");
        }

        // 4. Calculate new billing period end date
        var cycle = request.BillingCycle ?? newPlan.BillingCycle;
        DateTime newEndDate;

        if (request.CustomDays.HasValue && request.CustomDays.Value > 0)
        {
            newEndDate = now.AddDays(request.CustomDays.Value);
        }
        else if (cycle == BillingCycle.Yearly)
        {
            newEndDate = now.AddYears(1);
        }
        else
        {
            newEndDate = now.AddMonths(1);
        }

        // 5. Update existing subscription or create new contract if previous was terminated/null
        TenantSubscription activeSubscription;

        if (currentSub is not null && 
            currentSub.Status != SubscriptionStatus.Canceled && 
            currentSub.Status != SubscriptionStatus.Expired)
        {
            currentSub.ChangePlan(newPlan.Id, newEndDate);
            activeSubscription = currentSub;
        }
        else
        {
            activeSubscription = TenantSubscription.CreateActive(
                tenant.Id,
                newPlan.Id,
                startDate: now,
                endDate: newEndDate,
                externalSubscriptionId: null,
                autoRenew: true);

            context.TenantSubscriptions.Add(activeSubscription);
        }

        await context.SaveChangesAsync(ct);
    }
}
