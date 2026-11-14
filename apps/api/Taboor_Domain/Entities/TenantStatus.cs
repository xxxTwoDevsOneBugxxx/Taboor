namespace Taboor_Domain.Entities;

/// <summary>
/// Represents the administrative lifecycle status of a business tenant on the platform.
/// </summary>
public enum TenantStatus
{
    /// <summary>
    /// Tenant is in good standing and can access services according to its subscription.
    /// </summary>
    Active = 1,

    /// <summary>
    /// Tenant has been administratively suspended (e.g. policy violations or prolonged non-payment).
    /// Access to management operations is blocked.
    /// </summary>
    Suspended = 2
}
