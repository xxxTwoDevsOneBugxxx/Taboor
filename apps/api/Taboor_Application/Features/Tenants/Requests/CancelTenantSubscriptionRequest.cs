namespace Taboor_Application.Features.Tenants.Requests;

/// <summary>
/// HTTP request body payload for canceling a tenant's subscription.
/// </summary>
public record CancelTenantSubscriptionRequest(
    string? Reason = null
);
