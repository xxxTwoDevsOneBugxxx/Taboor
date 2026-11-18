using MediatR;
using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Tenants.Commands.ChangeTenantPlan;

/// <summary>
/// Command to change, upgrade, or downgrade a tenant's subscription plan.
/// Follows CQRS: mutates state and completes successfully without query DTO projections.
/// </summary>
public record ChangeTenantPlanCommand(
    Guid TenantId,
    int NewPlanId,
    BillingCycle? BillingCycle = null,
    int? CustomDays = null
) : IRequest;
