using FluentValidation;

namespace Taboor_Application.Features.Tenants.Commands.SuspendTenant;

/// <summary>
/// Validator for SuspendTenantCommand.
/// </summary>
public class SuspendTenantValidator : AbstractValidator<SuspendTenantCommand>
{
    public SuspendTenantValidator()
    {
        RuleFor(x => x.Id)
            .NotEmpty().WithMessage("Tenant ID is required.");

        RuleFor(x => x.Reason)
            .MaximumLength(500).WithMessage("Suspension reason must not exceed 500 characters.");
    }
}
