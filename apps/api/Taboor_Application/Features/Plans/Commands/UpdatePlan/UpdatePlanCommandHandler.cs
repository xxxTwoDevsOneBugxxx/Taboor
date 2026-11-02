using MediatR;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;
using Taboor_Application.Features.Plans.DTOs;

namespace Taboor_Application.Features.Plans.Commands.UpdatePlan;

/// <summary>
/// Handler for UpdatePlanCommand.
/// Loads the entity, validates business rules, calls domain method, and commits changes.
/// </summary>
public class UpdatePlanCommandHandler(IApplicationDbContext context) 
    : IRequestHandler<UpdatePlanCommand, PlanDto>
{
    public async Task<PlanDto> Handle(UpdatePlanCommand request, CancellationToken cancellationToken)
    {
        var plan = await context.Plans
            .FirstOrDefaultAsync(p => p.Id == request.Id, cancellationToken);

        if (plan == null)
        {
            throw AppException.NotFound($"Plan with ID {request.Id} was not found.");
        }

        var normalizedName = request.Name.Trim().ToLower();
        var nameExists = await context.Plans
            .AnyAsync(p => p.Id != request.Id && p.Name.ToLower() == normalizedName, cancellationToken);

        if (nameExists)
        {
            throw AppException.Conflict("A plan with this name already exists.");
        }

        plan.Update(
            request.Name,
            request.Description,
            request.Price,
            request.BillingCycle,
            request.MaxBranches,
            request.MaxServices,
            request.MaxServicesPerBranch);

        await context.SaveChangesAsync(cancellationToken);

        return new PlanDto(
            plan.Id,
            plan.Name,
            plan.Description,
            plan.Price,
            plan.BillingCycle,
            plan.MaxBranches,
            plan.MaxServices,
            plan.MaxServicesPerBranch,
            plan.IsActive,
            plan.CreatedAt,
            plan.UpdatedAt);
    }
}
