using MediatR;
using Taboor_Application.Features.Auth.DTOs;

namespace Taboor_Application.Features.Auth.Commands.GoogleMobileLogin;

/// <summary>
/// Command to authenticate mobile clients using a Google OpenID Connect ID token.
/// </summary>
public record GoogleMobileLoginCommand(
    string IdToken
) : IRequest<ExternalAuthResponseDto>;
