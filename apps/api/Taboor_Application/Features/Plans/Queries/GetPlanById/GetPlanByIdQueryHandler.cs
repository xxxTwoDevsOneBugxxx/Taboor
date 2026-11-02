using MediatR;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;
using Taboor_Application.Features.Plans.DTOs;

namespace Taboor_Application.Features.Plans.Queries.GetPlanById;

/// <summary>
/// Query handler for retrieving a single plan. Throws an AppException.NotFound if not found,
/// which is automatically translated to HTTP 404 by the GlobalExceptionHandler.
/// </summary>
public class GetPlanByIdQueryHandler(IApplicationDbContext context)
    : IRequestHandler<GetPlanByIdQuery, PlanDto>
{
    public async Task<PlanDto> Handle(GetPlanByIdQuery request, CancellationToken ct)
    {
        var plan = await context.Plans
            .AsNoTracking()
            .Where(p => p.Id == request.Id)
            .Select(p => new PlanDto(
                p.Id,
                p.Name,
                p.Description,
                p.Price,
                p.BillingCycle,
                p.MaxBranches,
                p.MaxServices,
                p.MaxServicesPerBranch,
                p.IsActive,
                p.CreatedAt,
                p.UpdatedAt))
            .FirstOrDefaultAsync(ct);

        if (plan is null)
        {
            throw AppException.NotFound($"Plan with ID ({request.Id}) was not found.");
        }

        return plan;
    }
}
