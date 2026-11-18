using MediatR;

namespace Taboor_Application.Features.Tenants.Commands.CancelTenantSubscription;

/// <summary>
/// Command to cancel a tenant's active subscription contract.
/// </summary>
public record CancelTenantSubscriptionCommand(
    Guid TenantId,
    string? Reason = null
) : IRequest;
