using FluentValidation;

namespace Taboor_Application.Features.Auth.Commands.VerifyResetCode;

/// <summary>
/// Validates the verify reset code payload.
/// </summary>
public class VerifyResetCodeValidator : AbstractValidator<VerifyResetCodeCommand>
{
    public VerifyResetCodeValidator()
    {
        RuleFor(x => x.Email)
            .NotEmpty().WithMessage("Email is required.")
            .EmailAddress().WithMessage("A valid email address is required.");

        RuleFor(x => x.Code)
            .NotEmpty().WithMessage("Reset code is required.")
            .Length(6).WithMessage("Reset code must be 6 digits.")
            .Matches(@"^[0-9]{6}$").WithMessage("Reset code must contain digits only.");
    }
}
