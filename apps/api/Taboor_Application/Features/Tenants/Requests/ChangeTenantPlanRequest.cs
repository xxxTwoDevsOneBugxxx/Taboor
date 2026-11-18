using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Tenants.Requests;

/// <summary>
/// HTTP request body payload for changing, upgrading, or downgrading a tenant's subscription plan.
/// </summary>
public record ChangeTenantPlanRequest(
    int NewPlanId,
    BillingCycle? BillingCycle = null,
    int? CustomDays = null
);
