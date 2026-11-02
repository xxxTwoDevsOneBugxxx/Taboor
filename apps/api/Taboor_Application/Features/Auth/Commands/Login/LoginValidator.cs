using FluentValidation;

namespace Taboor_Application.Features.Auth.Commands.Login;

/// <summary>
/// Validates login input credentials prior to command handler execution.
/// </summary>
public class LoginValidator : AbstractValidator<LoginCommand>
{
    public LoginValidator()
    {
        RuleFor(x => x.Email)
            .NotEmpty().WithMessage("Email is required.")
            .EmailAddress().WithMessage("A valid email address is required.");

        RuleFor(x => x.Password)
            .NotEmpty().WithMessage("Password is required.");
    }
}
