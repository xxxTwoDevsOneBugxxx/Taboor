namespace Taboor_Application.Features.Auth.DTOs;

/// <summary>
/// Response payload returned upon successful authentication.
/// </summary>
public record LoginResponseDto(
    string Token,
    string? RefreshToken,
    DateTime RefreshTokenExpiry,
    UserDto User
);
