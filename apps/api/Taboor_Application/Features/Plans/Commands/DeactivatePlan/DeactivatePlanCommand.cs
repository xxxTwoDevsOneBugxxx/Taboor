using MediatR;

namespace Taboor_Application.Features.Plans.Commands.DeactivatePlan;

/// <summary>
/// Command to deactivate an active subscription plan.
/// </summary>
public record DeactivatePlanCommand(int Id) : IRequest;
