using MediatR;

namespace Taboor_Application.Features.Tenants.Commands.SuspendTenant;

/// <summary>
/// Command to administratively suspend an existing tenant organization.
/// </summary>
public record SuspendTenantCommand(Guid Id, string? Reason = null) : IRequest;
