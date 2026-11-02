namespace Taboor_Application.Features.Auth.DTOs;

/// <summary>
/// Immutable user data transfer object representing customer or admin details.
/// </summary>
public record UserDto(
    string Id,
    string FullName,
    string Email,
    string? PhoneNumber,
    string? NationalId,
    string? PreferredLanguage,
    int NoShowCount,
    bool IsBlocked,
    string Role,
    bool IsLocked,
    DateTime CreatedAt
);
