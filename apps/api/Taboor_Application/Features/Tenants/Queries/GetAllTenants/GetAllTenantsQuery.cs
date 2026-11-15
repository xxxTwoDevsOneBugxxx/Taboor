using MediatR;
using Taboor_Application.Common.Models;
using Taboor_Application.Features.Tenants.DTOs;
using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Tenants.Queries.GetAllTenants;

/// <summary>
/// Query to retrieve a paginated, filterable, and sortable list of business tenants for the platform administration table.
/// </summary>
public record GetAllTenantsQuery(
    int PageNumber = 1,
    int PageSize = 10,
    string? Search = null,
    TenantStatus? Status = null,
    SubscriptionStatus? SubStatus = null,
    int? PlanId = null,
    string? SortBy = null,
    bool SortDescending = true
) : IRequest<PagedResult<TenantListItemDto>>;
