using MediatR;

namespace Taboor_Application.Features.Auth.Commands.LogoutWeb;

/// <summary>
/// Command to perform web logout by revoking the refresh token lookup by hash alone.
/// </summary>
public record LogoutWebCommand(
    string? RefreshToken
) : IRequest;
