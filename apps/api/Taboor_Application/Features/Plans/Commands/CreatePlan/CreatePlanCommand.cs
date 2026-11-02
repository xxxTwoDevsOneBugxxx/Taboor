using MediatR;
using Taboor_Application.Features.Plans.DTOs;
using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Plans.Commands.CreatePlan;

/// <summary>
/// Command to create a new platform subscription plan.
/// Returns the created PlanDto matching dashboard and mobile requirements.
/// </summary>
public record CreatePlanCommand(
    string Name,
    string? Description,
    decimal Price,
    BillingCycle BillingCycle,
    int MaxBranches,
    int MaxServices,
    int MaxServicesPerBranch
) : IRequest<PlanDto>;
