using MediatR;

namespace Taboor_Application.Features.Auth.Commands.RevokeToken;

/// <summary>
/// Command to revoke an active refresh token for a specific user.
/// </summary>
public record RevokeTokenCommand(
    string UserId,
    string RefreshToken
) : IRequest;
