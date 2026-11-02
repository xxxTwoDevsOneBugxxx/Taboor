using MediatR;

namespace Taboor_Application.Features.Auth.Commands.VerifyEmail;

/// <summary>
/// Command to verify an email address using the received OTP code.
/// </summary>
public record VerifyEmailCommand(
    string Email,
    string Code
) : IRequest;
