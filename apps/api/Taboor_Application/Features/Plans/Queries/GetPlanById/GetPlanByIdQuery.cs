using MediatR;
using Taboor_Application.Features.Plans.DTOs;

namespace Taboor_Application.Features.Plans.Queries.GetPlanById;

/// <summary>
/// Query to retrieve a single subscription plan by its identifier.
/// </summary>
public record GetPlanByIdQuery(int Id) : IRequest<PlanDto>;
