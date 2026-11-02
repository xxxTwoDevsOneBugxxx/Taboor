using MediatR;

namespace Taboor_Application.Features.Plans.Commands.ActivatePlan;

/// <summary>
/// Command to activate an existing subscription plan.
/// </summary>
public record ActivatePlanCommand(int Id) : IRequest;
