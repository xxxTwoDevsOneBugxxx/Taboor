using FluentValidation;

namespace Taboor_Application.Features.Auth.Commands.SendVerificationCode;

/// <summary>
/// Validates the send verification code request.
/// </summary>
public class SendVerificationCodeValidator : AbstractValidator<SendVerificationCodeCommand>
{
    public SendVerificationCodeValidator()
    {
        RuleFor(x => x.Email)
            .NotEmpty().WithMessage("Email is required.")
            .EmailAddress().WithMessage("A valid email address is required.");
    }
}
