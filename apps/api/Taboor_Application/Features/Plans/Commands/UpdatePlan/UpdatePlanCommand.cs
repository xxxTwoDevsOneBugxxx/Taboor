using MediatR;
using Taboor_Application.Features.Plans.DTOs;
using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Plans.Commands.UpdatePlan;

/// <summary>
/// Command to update an existing platform subscription plan.
/// </summary>
public record UpdatePlanCommand(
    int Id,
    string Name,
    string? Description,
    decimal Price,
    BillingCycle BillingCycle,
    int MaxBranches,
    int MaxServices,
    int MaxServicesPerBranch
) : IRequest<PlanDto>;
