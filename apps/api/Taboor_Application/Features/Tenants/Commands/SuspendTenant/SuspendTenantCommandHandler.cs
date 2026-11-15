using MediatR;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;
using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Tenants.Commands.SuspendTenant;

/// <summary>
/// Handler for SuspendTenantCommand.
/// </summary>
public class SuspendTenantCommandHandler(IApplicationDbContext context) 
    : IRequestHandler<SuspendTenantCommand>
{
    public async Task Handle(SuspendTenantCommand request, CancellationToken cancellationToken)
    {
        var tenant = await context.Tenants
            .FirstOrDefaultAsync(t => t.Id == request.Id, cancellationToken);

        if (tenant == null)
        {
            throw AppException.NotFound($"Tenant with ID {request.Id} was not found.");
        }

        if (tenant.Status == TenantStatus.Suspended)
        {
            return;
        }

        tenant.Suspend();
        await context.SaveChangesAsync(cancellationToken);
    }
}
