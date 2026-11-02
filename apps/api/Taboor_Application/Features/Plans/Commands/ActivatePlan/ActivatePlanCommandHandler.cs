using MediatR;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;

namespace Taboor_Application.Features.Plans.Commands.ActivatePlan;

/// <summary>
/// Handler for ActivatePlanCommand.
/// </summary>
public class ActivatePlanCommandHandler(IApplicationDbContext context) 
    : IRequestHandler<ActivatePlanCommand>
{
    public async Task Handle(ActivatePlanCommand request, CancellationToken cancellationToken)
    {
        var plan = await context.Plans
            .FirstOrDefaultAsync(p => p.Id == request.Id, cancellationToken);

        if (plan == null)
        {
            throw AppException.NotFound($"Plan with ID {request.Id} was not found.");
        }

        plan.Activate();
        await context.SaveChangesAsync(cancellationToken);
    }
}
