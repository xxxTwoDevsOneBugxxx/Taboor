namespace Taboor_Domain.Entities;

/// <summary>
/// Represents the organizational membership linking an Identity User to a specific Tenant with an assigned role.
/// Fully encapsulated Rich Domain Entity: changes in role or status are strictly governed by domain methods.
/// </summary>
public class TenantMember
{
    public Guid Id { get; private set; }

    public Guid TenantId { get; private set; }

    public Tenant Tenant { get; private set; } = null!;

    public string UserId { get; private set; } = null!;

    public ApplicationUser User { get; private set; } = null!;

    /// <summary>
    /// The operational role of the member within this specific tenant.
    /// </summary>
    public TenantRole Role { get; private set; }

    /// <summary>
    /// Internal business job title or display title (e.g. "أخصائي عيون", "مسؤول استقبال").
    /// </summary>
    public string? JobTitle { get; private set; }

    /// <summary>
    /// Indicates whether the member is currently active in this tenant.
    /// Deactivated members are preserved for historical audit trails without granting access.
    /// </summary>
    public bool IsActive { get; private set; } = true;

    public DateTime JoinedAt { get; private set; }

    public DateTime? UpdatedAt { get; private set; }

    /// <summary>
    /// Private constructor for EF Core reflection materialization.
    /// </summary>
    private TenantMember() { }

    /// <summary>
    /// Static Factory Method to create the primary Owner member during tenant onboarding.
    /// </summary>
    public static TenantMember CreateOwner(Guid tenantId, string userId, string? jobTitle = "المالك")
    {
        return new TenantMember
        {
            Id = Guid.NewGuid(),
            TenantId = tenantId,
            UserId = userId,
            Role = TenantRole.Owner,
            JobTitle = jobTitle?.Trim(),
            IsActive = true,
            JoinedAt = DateTime.UtcNow
        };
    }

    /// <summary>
    /// Static Factory Method to add a new staff or admin member to an existing tenant.
    /// </summary>
    public static TenantMember Create(Guid tenantId, string userId, TenantRole role, string? jobTitle = null)
    {
        return new TenantMember
        {
            Id = Guid.NewGuid(),
            TenantId = tenantId,
            UserId = userId,
            Role = role,
            JobTitle = jobTitle?.Trim(),
            IsActive = true,
            JoinedAt = DateTime.UtcNow
        };
    }

    #region Domain Behaviors

    /// <summary>
    /// Updates the member's operational role within the tenant.
    /// </summary>
    public void ChangeRole(TenantRole newRole)
    {
        Role = newRole;
        UpdatedAt = DateTime.UtcNow;
    }

    /// <summary>
    /// Updates the display job title.
    /// </summary>
    public void UpdateJobTitle(string? jobTitle)
    {
        JobTitle = jobTitle?.Trim();
        UpdatedAt = DateTime.UtcNow;
    }

    /// <summary>
    /// Deactivates the member (disables operational access to this tenant).
    /// </summary>
    public void Deactivate()
    {
        IsActive = false;
        UpdatedAt = DateTime.UtcNow;
    }

    /// <summary>
    /// Re-activates a previously deactivated member.
    /// </summary>
    public void Activate()
    {
        IsActive = true;
        UpdatedAt = DateTime.UtcNow;
    }

    #endregion
}
