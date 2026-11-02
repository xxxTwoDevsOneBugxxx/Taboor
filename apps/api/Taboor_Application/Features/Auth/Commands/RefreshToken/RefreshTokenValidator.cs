using FluentValidation;

namespace Taboor_Application.Features.Auth.Commands.RefreshToken;

/// <summary>
/// Validates the refresh token request payload.
/// </summary>
public class RefreshTokenValidator : AbstractValidator<RefreshTokenCommand>
{
    public RefreshTokenValidator()
    {
        RuleFor(x => x.RefreshToken)
            .NotEmpty().WithMessage("Refresh token is required.");
    }
}
