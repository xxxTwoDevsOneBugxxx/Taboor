using MediatR;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Taboor_Application.Common;
using Taboor_Application.Common.Constants;
using Taboor_Application.Features.Tenants.DTOs;
using Taboor_Application.Features.Tenants.Queries.GetAllTenants;
using Taboor_Application.Features.Tenants.Queries.GetTenantById;
using Taboor_Domain.Entities;

namespace Taboor_API.Controllers;

/// <summary>
/// Controller for Platform Admin to query and oversee all subscribed tenants and their accounts.
/// Thin driving adapter that delegates query processing directly to MediatR slices.
/// </summary>
[Route("api/[controller]")]
[ApiController]
[Authorize(Roles = $"{SD.PlatformAdmin},Admin,PlatformAdmin")]
public class TenantsController(ISender sender) : ControllerBase
{
    /// <summary>
    /// Retrieves a paginated, filterable, and sortable list of business tenants.
    /// </summary>
    [HttpGet]
    public async Task<IActionResult> GetAll(
        [FromQuery] int pageNumber = 1,
        [FromQuery] int pageSize = 10,
        [FromQuery] string? search = null,
        [FromQuery] TenantStatus? status = null,
        [FromQuery] SubscriptionStatus? subStatus = null,
        [FromQuery] int? planId = null,
        [FromQuery] string? sortBy = null,
        [FromQuery] bool sortDescending = true,
        CancellationToken ct = default)
    {
        var query = new GetAllTenantsQuery(
            pageNumber, pageSize, search, status, subStatus, planId, sortBy, sortDescending);

        var result = await sender.Send(query, ct);

        return Ok(ApiResponse<IReadOnlyList<TenantListItemDto>>.PagedSuccess(
            result.Items,
            result.PageNumber,
            result.PageSize,
            result.TotalCount));
    }

    /// <summary>
    /// Retrieves full profile, subscriptions history, and members for a specific tenant.
    /// </summary>
    [HttpGet("{id:guid}")]
    public async Task<IActionResult> GetById([FromRoute] Guid id, CancellationToken ct = default)
    {
        var tenant = await sender.Send(new GetTenantByIdQuery(id), ct);
        return Ok(ApiResponse<TenantDetailDto>.SuccessResponse(tenant));
    }
}
