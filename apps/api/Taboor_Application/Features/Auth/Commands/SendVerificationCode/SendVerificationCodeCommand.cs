using MediatR;

namespace Taboor_Application.Features.Auth.Commands.SendVerificationCode;

/// <summary>
/// Command to request and send an email verification OTP code.
/// </summary>
public record SendVerificationCodeCommand(
    string Email
) : IRequest;
