using FluentValidation;

namespace Taboor_Application.Features.Auth.Commands.VerifyEmail;

/// <summary>
/// Validates the email verification request payload.
/// </summary>
public class VerifyEmailValidator : AbstractValidator<VerifyEmailCommand>
{
    public VerifyEmailValidator()
    {
        RuleFor(x => x.Email)
            .NotEmpty().WithMessage("Email is required.")
            .EmailAddress().WithMessage("A valid email address is required.");

        RuleFor(x => x.Code)
            .NotEmpty().WithMessage("Verification code is required.")
            .Length(6).WithMessage("Verification code must be 6 digits.")
            .Matches(@"^[0-9]{6}$").WithMessage("Verification code must contain digits only.");
    }
}
