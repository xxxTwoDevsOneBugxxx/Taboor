namespace Taboor_Domain.Entities;

/// <summary>
/// Represents the baseline operational roles an individual member may hold within a tenant organization.
/// In future iterations, this will seamlessly map to dynamic custom RBAC policies.
/// </summary>
public enum TenantRole
{
    /// <summary>
    /// The primary account owner who registered the tenant.
    /// Has absolute control over subscriptions, billing, and tenant configuration.
    /// </summary>
    Owner = 1,

    /// <summary>
    /// Operational administrator (e.g. branch manager or clinic supervisor).
    /// Can manage branches, services, staff, and view analytics, but cannot modify billing or ownership.
    /// </summary>
    Admin = 2,

    /// <summary>
    /// Service provider / desk operator (e.g. doctor, barber, counter agent).
    /// Focuses on day-to-day queue ticketing and customer attendance operations.
    /// </summary>
    Staff = 3
}
