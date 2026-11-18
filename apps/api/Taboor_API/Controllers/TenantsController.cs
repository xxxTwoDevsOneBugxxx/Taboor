using MediatR;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Taboor_Application.Common;
using Taboor_Application.Common.Constants;
using Taboor_Application.Features.Tenants.Commands.ActivateTenant;
using Taboor_Application.Features.Tenants.Commands.ChangeTenantPlan;
using Taboor_Application.Features.Tenants.Commands.RenewTenantSubscription;
using Taboor_Application.Features.Tenants.Commands.SuspendTenant;
using Taboor_Application.Features.Tenants.DTOs;
using Taboor_Application.Features.Tenants.Queries.GetAllTenants;
using Taboor_Application.Features.Tenants.Queries.GetTenantById;
using Taboor_Application.Features.Tenants.Requests;
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

    /// <summary>
    /// Activates a tenant organization, restoring active operational access.
    /// </summary>
    [HttpPatch("{id:guid}/activate")]
    public async Task<IActionResult> Activate([FromRoute] Guid id, CancellationToken ct = default)
    {
        await sender.Send(new ActivateTenantCommand(id), ct);
        return Ok(ApiResponse<object>.SuccessResponse(null, "Tenant activated successfully."));
    }

    /// <summary>
    /// Administratively suspends a tenant organization, blocking operations.
    /// </summary>
    [HttpPatch("{id:guid}/suspend")]
    public async Task<IActionResult> Suspend(
        [FromRoute] Guid id,
        [FromBody] SuspendTenantRequest? request = null,
        CancellationToken ct = default)
    {
        await sender.Send(new SuspendTenantCommand(id, request?.Reason), ct);
        return Ok(ApiResponse<object>.SuccessResponse(null, "Tenant suspended successfully."));
    }

    /// <summary>
    /// Changes, upgrades, or downgrades a tenant's subscription plan.
    /// </summary>
    [HttpPut("{id:guid}/subscription/plan")]
    public async Task<IActionResult> ChangePlan(
        [FromRoute] Guid id,
        [FromBody] ChangeTenantPlanRequest request,
        CancellationToken ct = default)
    {
        var command = new ChangeTenantPlanCommand(
            id,
            request.NewPlanId,
            request.BillingCycle,
            request.CustomDays);

        await sender.Send(command, ct);

        return Ok(ApiResponse<object>.SuccessResponse(
            null,
            "Subscription plan changed successfully."));
    }

    /// <summary>
    /// Renews an existing tenant subscription contract, extending the active period.
    /// </summary>
    [HttpPost("{id:guid}/subscription/renew")]
    public async Task<IActionResult> RenewSubscription(
        [FromRoute] Guid id,
        [FromBody] RenewTenantSubscriptionRequest? request = null,
        CancellationToken ct = default)
    {
        var command = new RenewTenantSubscriptionCommand(
            id,
            request?.BillingCycle,
            request?.CustomDays,
            request?.ExternalPaymentId);

        await sender.Send(command, ct);

        return Ok(ApiResponse<object>.SuccessResponse(
            null,
            "Subscription renewed successfully."));
    }
}
