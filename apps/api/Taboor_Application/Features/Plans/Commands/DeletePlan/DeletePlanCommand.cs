using MediatR;

namespace Taboor_Application.Features.Plans.Commands.DeletePlan;

/// <summary>
/// Command to permanently delete an inactive subscription plan.
/// </summary>
public record DeletePlanCommand(int Id) : IRequest;
