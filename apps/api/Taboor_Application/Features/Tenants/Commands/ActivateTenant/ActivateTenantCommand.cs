using MediatR;

namespace Taboor_Application.Features.Tenants.Commands.ActivateTenant;

/// <summary>
/// Command to administratively activate an existing tenant organization.
/// </summary>
public record ActivateTenantCommand(Guid Id) : IRequest;
