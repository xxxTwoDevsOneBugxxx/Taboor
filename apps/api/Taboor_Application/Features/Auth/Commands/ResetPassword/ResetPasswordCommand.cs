using MediatR;

namespace Taboor_Application.Features.Auth.Commands.ResetPassword;

/// <summary>
/// Command to complete password reset using a verified reset code.
/// </summary>
public record ResetPasswordCommand(
    string Email,
    string Code,
    string NewPassword,
    string ConfirmPassword
) : IRequest;
