using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Tenants.DTOs;

/// <summary>
/// Immutable DTO projection representing a single tenant in tabular administrative overviews.
/// Combines core tenant info, primary owner identity, and current active/latest subscription snapshot.
/// </summary>
public record TenantListItemDto(
    Guid Id,
    string Name,
    string Slug,
    string? LogoUrl,
    string ContactEmail,
    string ContactPhone,
    TenantStatus Status,
    DateTime CreatedAt,
    string? OwnerName,
    string? OwnerEmail,
    string? OwnerPhone,
    int? PlanId,
    string? PlanName,
    decimal? PlanPrice,
    BillingCycle? BillingCycle,
    SubscriptionStatus? SubscriptionStatus,
    DateTime? SubscriptionStartDate,
    DateTime? SubscriptionEndDate,
    bool? AutoRenew
);
