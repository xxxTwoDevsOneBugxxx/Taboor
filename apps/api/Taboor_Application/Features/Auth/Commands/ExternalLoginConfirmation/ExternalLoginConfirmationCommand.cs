using MediatR;
using Taboor_Application.Features.Auth.DTOs;

namespace Taboor_Application.Features.Auth.Commands.ExternalLoginConfirmation;

/// <summary>
/// Command to confirm external user registration details (e.g. name, email).
/// </summary>
public record ExternalLoginConfirmationCommand(
    string Name,
    string Email
) : IRequest<ExternalAuthResponseDto>;
