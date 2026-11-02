using MediatR;
using Taboor_Application.Features.Auth.DTOs;

namespace Taboor_Application.Features.Auth.Commands.FacebookMobileLogin;

/// <summary>
/// Command to authenticate mobile clients using a Facebook access token.
/// </summary>
public record FacebookMobileLoginCommand(
    string AccessToken
) : IRequest<ExternalAuthResponseDto>;
