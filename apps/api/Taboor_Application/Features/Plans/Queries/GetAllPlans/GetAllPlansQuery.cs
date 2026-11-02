using MediatR;
using Taboor_Application.Features.Plans.DTOs;

namespace Taboor_Application.Features.Plans.Queries.GetAllPlans;

/// <summary>
/// Query to retrieve all platform subscription plans.
/// </summary>
/// <param name="OnlyActive">When true, returns only active/available plans.</param>
public record GetAllPlansQuery(bool OnlyActive = false) : IRequest<IReadOnlyList<PlanDto>>;
