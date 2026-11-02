using MediatR;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;

namespace Taboor_Application.Features.Plans.Commands.DeletePlan;

/// <summary>
/// Handler for DeletePlanCommand.
/// Enforces business rule that only inactive plans can be deleted.
/// </summary>
public class DeletePlanCommandHandler(IApplicationDbContext context) 
    : IRequestHandler<DeletePlanCommand>
{
    public async Task Handle(DeletePlanCommand request, CancellationToken cancellationToken)
    {
        var plan = await context.Plans
            .FirstOrDefaultAsync(p => p.Id == request.Id, cancellationToken);

        if (plan == null)
        {
            throw AppException.NotFound($"Plan with ID {request.Id} was not found.");
        }

        if (plan.IsActive)
        {
            throw AppException.Conflict("Only inactive plans can be deleted. Please deactivate the plan first.");
        }

        context.Plans.Remove(plan);
        await context.SaveChangesAsync(cancellationToken);
    }
}
