using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Tenants.DTOs;

/// <summary>
/// Detailed DTO representing a specific tenant, including active & historical subscriptions and members.
/// </summary>
public record TenantDetailDto(
    Guid Id,
    string Name,
    string Slug,
    string? LogoUrl,
    string ContactEmail,
    string ContactPhone,
    TenantStatus Status,
    DateTime CreatedAt,
    DateTime? UpdatedAt,
    TenantSubscriptionDto? CurrentSubscription,
    IReadOnlyList<TenantSubscriptionDto> Subscriptions,
    IReadOnlyList<TenantMemberDto> Members
);

/// <summary>
/// DTO representing a tenant subscription contract.
/// </summary>
public record TenantSubscriptionDto(
    Guid Id,
    int PlanId,
    string PlanName,
    decimal Price,
    BillingCycle BillingCycle,
    SubscriptionStatus Status,
    DateTime StartDate,
    DateTime EndDate,
    DateTime? TrialEndsAt,
    bool AutoRenew,
    string? ExternalSubscriptionId,
    DateTime? CanceledAt,
    string? CancellationReason
) ;

/// <summary>
/// DTO representing an organizational member attached to a tenant.
/// </summary>
public record TenantMemberDto(
    Guid Id,
    string UserId,
    string FullName,
    string Email,
    string PhoneNumber,
    TenantRole Role,
    string? JobTitle,
    bool IsActive,
    DateTime JoinedAt
);