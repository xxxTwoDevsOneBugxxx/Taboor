using MediatR;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;
using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Tenants.Commands.ActivateTenant;

/// <summary>
/// Handler for ActivateTenantCommand.
/// </summary>
public class ActivateTenantCommandHandler(IApplicationDbContext context) 
    : IRequestHandler<ActivateTenantCommand>
{
    public async Task Handle(ActivateTenantCommand request, CancellationToken cancellationToken)
    {
        var tenant = await context.Tenants
            .FirstOrDefaultAsync(t => t.Id == request.Id, cancellationToken);

        if (tenant == null)
        {
            throw AppException.NotFound($"Tenant with ID {request.Id} was not found.");
        }

        if (tenant.Status == TenantStatus.Active)
        {
            return;
        }

        tenant.Activate();
        await context.SaveChangesAsync(cancellationToken);
    }
}
