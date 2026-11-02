namespace Taboor_Application.Features.Auth.DTOs;

/// <summary>
/// Immutable response data transfer object containing refreshed access and refresh tokens.
/// </summary>
public record TokenResponseDto(
    string AccessToken,
    string? RefreshToken,
    DateTime RefreshTokenExpiry,
    UserDto? User
);
