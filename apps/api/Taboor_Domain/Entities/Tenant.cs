namespace Taboor_Domain.Entities;

/// <summary>
/// Represents a business organization/tenant (e.g., clinic, salon, auto workshop, service center)
/// subscribing to the Taboor SaaS platform.
/// Fully encapsulated Rich Domain Entity: state changes are strictly governed by domain methods.
/// </summary>
public class Tenant
{
    public Guid Id { get; private set; }

    /// <summary>
    /// The public commercial display name of the tenant (e.g., "Al-Amal Medical Center").
    /// </summary>
    public string Name { get; private set; } = null!;

    /// <summary>
    /// Unique URL-friendly slug used for subdomain or routing (e.g., "al-amal-medical").
    /// </summary>
    public string Slug { get; private set; } = null!;

    /// <summary>
    /// URL to the business logo image.
    /// </summary>
    public string? LogoUrl { get; private set; }

    /// <summary>
    /// Primary business contact and billing email address.
    /// </summary>
    public string ContactEmail { get; private set; } = null!;

    /// <summary>
    /// Primary business contact phone number.
    /// </summary>
    public string ContactPhone { get; private set; } = null!;

    /// <summary>
    /// Administrative lifecycle status of the tenant.
    /// </summary>
    public TenantStatus Status { get; private set; } = TenantStatus.Active;

    public DateTime CreatedAt { get; private set; }

    public DateTime? UpdatedAt { get; private set; }

    /// <summary>
    /// Private constructor for EF Core reflection materialization.
    /// </summary>
    private Tenant() { }

    /// <summary>
    /// Named Static Factory Method to create a newly registered valid tenant.
    /// </summary>
    public static Tenant Create(
        string name,
        string slug,
        string contactEmail,
        string contactPhone,
        string? logoUrl = null)
    {
        return new Tenant
        {
            Id = Guid.NewGuid(),
            Name = name.Trim(),
            Slug = slug.Trim().ToLowerInvariant(),
            ContactEmail = contactEmail.Trim().ToLowerInvariant(),
            ContactPhone = contactPhone.Trim(),
            LogoUrl = logoUrl?.Trim(),
            Status = TenantStatus.Active,
            CreatedAt = DateTime.UtcNow
        };
    }

    #region Domain Behaviors

    /// <summary>
    /// Updates the commercial profile details of the tenant.
    /// </summary>
    public void UpdateProfile(
        string name,
        string contactEmail,
        string contactPhone,
        string? logoUrl)
    {
        Name = name.Trim();
        ContactEmail = contactEmail.Trim().ToLowerInvariant();
        ContactPhone = contactPhone.Trim();
        LogoUrl = logoUrl?.Trim();
        UpdatedAt = DateTime.UtcNow;
    }

    /// <summary>
    /// Administratively suspends the tenant, blocking management operations.
    /// </summary>
    public void Suspend()
    {
        Status = TenantStatus.Suspended;
        UpdatedAt = DateTime.UtcNow;
    }

    /// <summary>
    /// Re-activates a previously suspended tenant.
    /// </summary>
    public void Activate()
    {
        Status = TenantStatus.Active;
        UpdatedAt = DateTime.UtcNow;
    }

    #endregion
}
