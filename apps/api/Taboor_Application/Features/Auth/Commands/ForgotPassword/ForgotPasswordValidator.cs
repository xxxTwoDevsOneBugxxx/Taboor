using FluentValidation;

namespace Taboor_Application.Features.Auth.Commands.ForgotPassword;

/// <summary>
/// Validates the forgot password request.
/// </summary>
public class ForgotPasswordValidator : AbstractValidator<ForgotPasswordCommand>
{
    public ForgotPasswordValidator()
    {
        RuleFor(x => x.Email)
            .NotEmpty().WithMessage("Email is required.")
            .EmailAddress().WithMessage("A valid email address is required.");
    }
}
