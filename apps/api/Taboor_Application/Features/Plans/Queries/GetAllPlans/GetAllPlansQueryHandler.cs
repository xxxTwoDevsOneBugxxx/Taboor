using MediatR;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Interfaces;
using Taboor_Application.Features.Plans.DTOs;

namespace Taboor_Application.Features.Plans.Queries.GetAllPlans;

/// <summary>
/// High-performance query handler for retrieving plans directly via EF Core projections.
/// </summary>
public class GetAllPlansQueryHandler(IApplicationDbContext context)
    : IRequestHandler<GetAllPlansQuery, IReadOnlyList<PlanDto>>
{
    public async Task<IReadOnlyList<PlanDto>> Handle(GetAllPlansQuery request, CancellationToken ct)
    {
        var query = context.Plans.AsNoTracking();

        if (request.OnlyActive)
        {
            query = query.Where(p => p.IsActive);
        }

        return await query
            .OrderBy(p => p.Price)
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
            .ToListAsync(ct);
    }
}
