using MediatR;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Interfaces;
using Taboor_Application.Common.Models;
using Taboor_Application.Features.Tenants.DTOs;
using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Tenants.Queries.GetAllTenants;

/// <summary>
/// Query handler executing server-side paginated, filtered, and sorted tenant retrieval
/// with direct EF Core projections for maximum throughput.
/// </summary>
public class GetAllTenantsQueryHandler(IApplicationDbContext context)
    : IRequestHandler<GetAllTenantsQuery, PagedResult<TenantListItemDto>>
{
    public async Task<PagedResult<TenantListItemDto>> Handle(GetAllTenantsQuery request, CancellationToken ct)
    {
        var query = context.Tenants.AsNoTracking();

        // 1. Text Search (Name, Slug, Email, Phone)
        if (!string.IsNullOrWhiteSpace(request.Search))
        {
            var term = request.Search.Trim().ToLower();
            query = query.Where(t =>
                t.Name.ToLower().Contains(term) ||
                t.Slug.ToLower().Contains(term) ||
                t.ContactEmail.ToLower().Contains(term) ||
                t.ContactPhone.Contains(term));
        }

        // 2. Tenant Lifecycle Status Filter
        if (request.Status.HasValue)
        {
            query = query.Where(t => t.Status == request.Status.Value);
        }

        // 3. Subscription Status Filter
        if (request.SubStatus.HasValue)
        {
            query = query.Where(t => context.TenantSubscriptions
                .Where(s => s.TenantId == t.Id)
                .OrderByDescending(s => s.StartDate)
                .Select(s => s.Status)
                .FirstOrDefault() == request.SubStatus.Value);
        }

        // 4. Plan Filter
        if (request.PlanId.HasValue)
        {
            query = query.Where(t => context.TenantSubscriptions
                .Where(s => s.TenantId == t.Id)
                .OrderByDescending(s => s.StartDate)
                .Select(s => s.PlanId)
                .FirstOrDefault() == request.PlanId.Value);
        }

        // 5. Total Count for Pagination Boundaries
        var totalCount = await query.CountAsync(ct);

        // 6. Dynamic Sorting
        if (string.IsNullOrWhiteSpace(request.SortBy))
        {
            query = query.OrderByDescending(t => t.CreatedAt);
        }
        else
        {
            query = request.SortBy.Trim().ToLowerInvariant() switch
            {
                "name" => request.SortDescending ? query.OrderByDescending(t => t.Name) : query.OrderBy(t => t.Name),
                "status" => request.SortDescending ? query.OrderByDescending(t => t.Status) : query.OrderBy(t => t.Status),
                "planname" or "plan" => request.SortDescending
                    ? query.OrderByDescending(t => context.TenantSubscriptions
                        .Where(s => s.TenantId == t.Id)
                        .OrderByDescending(s => s.StartDate)
                        .Select(s => s.Plan.Name)
                        .FirstOrDefault())
                    : query.OrderBy(t => context.TenantSubscriptions
                        .Where(s => s.TenantId == t.Id)
                        .OrderByDescending(s => s.StartDate)
                        .Select(s => s.Plan.Name)
                        .FirstOrDefault()),
                "createdat" or "created_at" => request.SortDescending ? query.OrderByDescending(t => t.CreatedAt) : query.OrderBy(t => t.CreatedAt),
                _ => query.OrderByDescending(t => t.CreatedAt)
            };
        }

        // 7. Pagination & Projection to TenantListItemDto
        var skip = (request.PageNumber - 1) * request.PageSize;
        var items = await query
            .Skip(skip)
            .Take(request.PageSize)
            .Select(t => new TenantListItemDto(
                t.Id,
                t.Name,
                t.Slug,
                t.LogoUrl,
                t.ContactEmail,
                t.ContactPhone,
                t.Status,
                t.CreatedAt,
                context.TenantMembers
                    .Where(m => m.TenantId == t.Id && m.Role == TenantRole.Owner)
                    .Select(m => m.User.FullName)
                    .FirstOrDefault(),
                context.TenantMembers
                    .Where(m => m.TenantId == t.Id && m.Role == TenantRole.Owner)
                    .Select(m => m.User.Email)
                    .FirstOrDefault(),
                context.TenantMembers
                    .Where(m => m.TenantId == t.Id && m.Role == TenantRole.Owner)
                    .Select(m => m.User.PhoneNumber)
                    .FirstOrDefault(),
                context.TenantSubscriptions
                    .Where(s => s.TenantId == t.Id)
                    .OrderByDescending(s => s.StartDate)
                    .Select(s => (int?)s.PlanId)
                    .FirstOrDefault(),
                context.TenantSubscriptions
                    .Where(s => s.TenantId == t.Id)
                    .OrderByDescending(s => s.StartDate)
                    .Select(s => s.Plan.Name)
                    .FirstOrDefault(),
                context.TenantSubscriptions
                    .Where(s => s.TenantId == t.Id)
                    .OrderByDescending(s => s.StartDate)
                    .Select(s => (decimal?)s.Plan.Price)
                    .FirstOrDefault(),
                context.TenantSubscriptions
                    .Where(s => s.TenantId == t.Id)
                    .OrderByDescending(s => s.StartDate)
                    .Select(s => (BillingCycle?)s.Plan.BillingCycle)
                    .FirstOrDefault(),
                context.TenantSubscriptions
                    .Where(s => s.TenantId == t.Id)
                    .OrderByDescending(s => s.StartDate)
                    .Select(s => (SubscriptionStatus?)s.Status)
                    .FirstOrDefault(),
                context.TenantSubscriptions
                    .Where(s => s.TenantId == t.Id)
                    .OrderByDescending(s => s.StartDate)
                    .Select(s => (DateTime?)s.StartDate)
                    .FirstOrDefault(),
                context.TenantSubscriptions
                    .Where(s => s.TenantId == t.Id)
                    .OrderByDescending(s => s.StartDate)
                    .Select(s => (DateTime?)s.EndDate)
                    .FirstOrDefault(),
                context.TenantSubscriptions
                    .Where(s => s.TenantId == t.Id)
                    .OrderByDescending(s => s.StartDate)
                    .Select(s => (bool?)s.AutoRenew)
                    .FirstOrDefault()
            ))
            .ToListAsync(ct);

        return new PagedResult<TenantListItemDto>(items, request.PageNumber, request.PageSize, totalCount);
    }
}
