using FluentValidation;

namespace Taboor_Application.Features.Auth.Commands.FacebookMobileLogin;

/// <summary>
/// Validates the Facebook mobile login request.
/// </summary>
public class FacebookMobileLoginValidator : AbstractValidator<FacebookMobileLoginCommand>
{
    public FacebookMobileLoginValidator()
    {
        RuleFor(x => x.AccessToken)
            .NotEmpty().WithMessage("Facebook access token is required.");
    }
}
