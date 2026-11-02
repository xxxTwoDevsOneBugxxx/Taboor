using MediatR;

namespace Taboor_Application.Features.Auth.Commands.ForgotPassword;

/// <summary>
/// Command to initiate the password reset process by generating and sending an OTP code.
/// </summary>
public record ForgotPasswordCommand(
    string Email
) : IRequest;
