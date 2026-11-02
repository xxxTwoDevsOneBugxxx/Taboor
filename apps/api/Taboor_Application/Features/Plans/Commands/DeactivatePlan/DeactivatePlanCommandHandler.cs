using MediatR;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;

namespace Taboor_Application.Features.Plans.Commands.DeactivatePlan;

/// <summary>
/// Handler for DeactivatePlanCommand.
/// </summary>
public class DeactivatePlanCommandHandler(IApplicationDbContext context) 
    : IRequestHandler<DeactivatePlanCommand>
{
    public async Task Handle(DeactivatePlanCommand request, CancellationToken cancellationToken)
    {
        var plan = await context.Plans
            .FirstOrDefaultAsync(p => p.Id == request.Id, cancellationToken);

        if (plan == null)
        {
            throw AppException.NotFound($"Plan with ID {request.Id} was not found.");
        }

        plan.Deactivate();
        await context.SaveChangesAsync(cancellationToken);
    }
}
