using FluentValidation;

namespace Taboor_Application.Features.Tenants.Commands.ActivateTenant;

/// <summary>
/// Validator for ActivateTenantCommand.
/// </summary>
public class ActivateTenantValidator : AbstractValidator<ActivateTenantCommand>
{
    public ActivateTenantValidator()
    {
        RuleFor(x => x.Id)
            .NotEmpty().WithMessage("Tenant ID is required.");
    }
}
