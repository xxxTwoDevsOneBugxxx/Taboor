using FluentValidation;

namespace Taboor_Application.Features.Auth.Commands.ExternalLoginConfirmation;

/// <summary>
/// Validates external login confirmation payload.
/// </summary>
public class ExternalLoginConfirmationValidator : AbstractValidator<ExternalLoginConfirmationCommand>
{
    public ExternalLoginConfirmationValidator()
    {
        RuleFor(x => x.Name)
            .NotEmpty().WithMessage("Name is required.")
            .MaximumLength(150).WithMessage("Name cannot exceed 150 characters.");

        RuleFor(x => x.Email)
            .NotEmpty().WithMessage("Email is required.")
            .EmailAddress().WithMessage("A valid email address is required.");
    }
}
