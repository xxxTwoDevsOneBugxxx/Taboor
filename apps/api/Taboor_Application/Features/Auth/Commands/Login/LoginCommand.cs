using MediatR;
using Taboor_Application.Features.Auth.DTOs;

namespace Taboor_Application.Features.Auth.Commands.Login;

/// <summary>
/// Command to authenticate an existing user via email and password credentials.
/// </summary>
public record LoginCommand(
    string Email,
    string Password,
    bool RememberMe = false
) : IRequest<LoginResponseDto>;
