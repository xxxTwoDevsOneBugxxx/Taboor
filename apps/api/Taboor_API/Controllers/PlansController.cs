using System.Net;
using MediatR;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Taboor_Application.Common;
using Taboor_Application.Common.Constants;
using Taboor_Application.Features.Plans.Commands.ActivatePlan;
using Taboor_Application.Features.Plans.Commands.CreatePlan;
using Taboor_Application.Features.Plans.Commands.DeactivatePlan;
using Taboor_Application.Features.Plans.Commands.DeletePlan;
using Taboor_Application.Features.Plans.Commands.UpdatePlan;
using Taboor_Application.Features.Plans.DTOs;
using Taboor_Application.Features.Plans.Queries.GetAllPlans;
using Taboor_Application.Features.Plans.Queries.GetPlanById;
using Taboor_Domain.Entities;

namespace Taboor_API.Controllers;

/// <summary>
/// Controller for Platform Admin to define and manage subscription plans.
/// Thin driving adapter that delegates all operations directly to MediatR slices.
/// </summary>
[Route("api/[controller]")]
[ApiController]
[Authorize(Roles = $"{SD.PlatformAdmin},Admin,PlatformAdmin")]
public class PlansController(ISender sender) : ControllerBase
{
    /// <summary>
    /// Retrieves subscription plans.
    /// </summary>
    [HttpGet]
    public async Task<IActionResult> GetAll([FromQuery] bool onlyActivated = false, CancellationToken ct = default)
    {
        var plans = await sender.Send(new GetAllPlansQuery(onlyActivated), ct);
        return Ok(ApiResponse<IReadOnlyList<PlanDto>>.SuccessResponse(plans));
    }

    /// <summary>
    /// Retrieves a single plan by its ID.
    /// </summary>
    [HttpGet("{id:int}")]
    public async Task<IActionResult> GetById([FromRoute] int id, CancellationToken ct = default)
    {
        var plan = await sender.Send(new GetPlanByIdQuery(id), ct);
        return Ok(ApiResponse<PlanDto>.SuccessResponse(plan));
    }

    /// <summary>
    /// Creates a new subscription plan.
    /// </summary>
    [HttpPost]
    public async Task<IActionResult> Create([FromBody] CreatePlanCommand command, CancellationToken ct = default)
    {
        var plan = await sender.Send(command, ct);
        return CreatedAtAction(nameof(GetById), new { id = plan.Id }, 
            ApiResponse<PlanDto>.SuccessResponse(plan, "Plan created successfully.", HttpStatusCode.Created));
    }

    /// <summary>
    /// Updates an existing subscription plan.
    /// </summary>
    [HttpPut("{id:int}")]
    public async Task<IActionResult> Update([FromRoute] int id, [FromBody] UpdatePlanRequest request, CancellationToken ct = default)
    {
        var command = new UpdatePlanCommand(
            id,
            request.Name,
            request.Description,
            request.Price,
            request.BillingCycle,
            request.MaxBranches,
            request.MaxServices,
            request.MaxServicesPerBranch);

        var plan = await sender.Send(command, ct);
        return Ok(ApiResponse<PlanDto>.SuccessResponse(plan, "Plan updated successfully."));
    }

    /// <summary>
    /// Activates a subscription plan.
    /// </summary>
    [HttpPatch("{id:int}/activate")]
    public async Task<IActionResult> Activate([FromRoute] int id, CancellationToken ct = default)
    {
        await sender.Send(new ActivatePlanCommand(id), ct);
        return Ok(ApiResponse<object>.SuccessResponse(null, "Plan activated successfully."));
    }

    /// <summary>
    /// Deactivates a subscription plan.
    /// </summary>
    [HttpPatch("{id:int}/deactivate")]
    public async Task<IActionResult> Deactivate([FromRoute] int id, CancellationToken ct = default)
    {
        await sender.Send(new DeactivatePlanCommand(id), ct);
        return Ok(ApiResponse<object>.SuccessResponse(null, "Plan deactivated successfully."));
    }

    /// <summary>
    /// Deletes an inactive subscription plan.
    /// </summary>
    [HttpDelete("{id:int}")]
    public async Task<IActionResult> Delete([FromRoute] int id, CancellationToken ct = default)
    {
        await sender.Send(new DeletePlanCommand(id), ct);
        return Ok(ApiResponse<object>.SuccessResponse(null, "Plan deleted successfully."));
    }
}

/// <summary>
/// HTTP Request body payload for updating a plan (Id comes from the route).
/// </summary>
public record UpdatePlanRequest(
    string Name,
    string? Description,
    decimal Price,
    BillingCycle BillingCycle,
    int MaxBranches,
    int MaxServices,
    int MaxServicesPerBranch
);
