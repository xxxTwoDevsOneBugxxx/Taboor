using MediatR;

namespace Taboor_Application.Features.Auth.Commands.VerifyResetCode;

/// <summary>
/// Command to verify the password reset OTP code.
/// </summary>
public record VerifyResetCodeCommand(
    string Email,
    string Code
) : IRequest;
