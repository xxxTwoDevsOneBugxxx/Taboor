namespace Taboor_Application.Features.Auth.DTOs;

/// <summary>
/// Immutable response data transfer object containing external / social authentication results.
/// </summary>
public record ExternalAuthResponseDto(
    string? Email,
    bool IsNewUser,
    bool HasPassword,
    string? Token,
    string? RefreshToken,
    DateTime? RefreshTokenExpiry = null,
    string? Message = null,
    string? ReturnUrl = null
);
