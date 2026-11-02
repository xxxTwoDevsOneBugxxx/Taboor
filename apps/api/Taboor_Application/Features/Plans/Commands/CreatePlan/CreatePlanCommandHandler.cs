using MediatR;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;
using Taboor_Application.Features.Plans.DTOs;
using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Plans.Commands.CreatePlan;

/// <summary>
/// Handler for CreatePlanCommand.
/// Directly executes against IApplicationDbContext using domain factory method.
/// </summary>
public class CreatePlanCommandHandler(IApplicationDbContext context) 
    : IRequestHandler<CreatePlanCommand, PlanDto>
{
    public async Task<PlanDto> Handle(CreatePlanCommand request, CancellationToken cancellationToken)
    {
        var normalizedName = request.Name.Trim().ToLower();
        var nameExists = await context.Plans
            .AnyAsync(p => p.Name.ToLower() == normalizedName, cancellationToken);

        if (nameExists)
        {
            throw AppException.Conflict("A plan with this name already exists.");
        }

        var plan = Plan.Create(
            request.Name,
            request.Description,
            request.Price,
            request.BillingCycle,
            request.MaxBranches,
            request.MaxServices,
            request.MaxServicesPerBranch);

        context.Plans.Add(plan);
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
