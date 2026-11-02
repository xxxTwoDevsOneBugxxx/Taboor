using MediatR;
using Taboor_Application.Features.Auth.DTOs;

namespace Taboor_Application.Features.Auth.Commands.RefreshToken;

/// <summary>
/// Command to rotate and refresh an access token using a valid refresh token.
/// </summary>
public record RefreshTokenCommand(
    string RefreshToken,
    string? AccessToken = null
) : IRequest<TokenResponseDto>;
