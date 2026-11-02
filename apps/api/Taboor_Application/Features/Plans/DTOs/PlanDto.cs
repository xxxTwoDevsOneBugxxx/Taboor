using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Plans.DTOs;

/// <summary>
/// Immutable DTO representation of a subscription plan for API consumers.
/// </summary>
public record PlanDto(
    int Id,
    string Name,
    string? Description,
    decimal Price,
    BillingCycle BillingCycle,
    int MaxBranches,
    int MaxServices,
    int MaxServicesPerBranch,
    bool IsActive,
    DateTime CreatedAt,
    DateTime? UpdatedAt
);
