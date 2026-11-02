using FluentValidation;

namespace Taboor_Application.Features.Auth.Commands.GoogleMobileLogin;

/// <summary>
/// Validates the Google mobile login request.
/// </summary>
public class GoogleMobileLoginValidator : AbstractValidator<GoogleMobileLoginCommand>
{
    public GoogleMobileLoginValidator()
    {
        RuleFor(x => x.IdToken)
            .NotEmpty().WithMessage("Google ID token is required.");
    }
}
