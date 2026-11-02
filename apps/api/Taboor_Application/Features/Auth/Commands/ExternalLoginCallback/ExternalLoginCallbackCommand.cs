using MediatR;
using Taboor_Application.Features.Auth.DTOs;

namespace Taboor_Application.Features.Auth.Commands.ExternalLoginCallback;

/// <summary>
/// Command to handle the OAuth authentication callback from external providers (Google, Facebook).
/// </summary>
public record ExternalLoginCallbackCommand(
    string? RemoteError,
    string? ReturnUrl
) : IRequest<ExternalAuthResponseDto>;
