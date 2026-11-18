namespace Taboor_Application.Features.Tenants.Requests;

/// <summary>
/// HTTP request body payload for suspending a tenant organization.
/// </summary>
public record SuspendTenantRequest(string? Reason = null);
