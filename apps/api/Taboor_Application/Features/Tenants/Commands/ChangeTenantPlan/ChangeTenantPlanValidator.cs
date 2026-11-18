using FluentValidation;

namespace Taboor_Application.Features.Tenants.Commands.ChangeTenantPlan;

/// <summary>
/// Validator for ChangeTenantPlanCommand.
/// </summary>
public class ChangeTenantPlanValidator : AbstractValidator<ChangeTenantPlanCommand>
{
    public ChangeTenantPlanValidator()
    {
        RuleFor(x => x.TenantId)
            .NotEmpty().WithMessage("Tenant ID is required.");

        RuleFor(x => x.NewPlanId)
            .GreaterThan(0).WithMessage("Valid Plan ID is required.");

        RuleFor(x => x.CustomDays)
            .GreaterThan(0).When(x => x.CustomDays.HasValue)
            .WithMessage("Custom duration must be greater than 0 days.");

        RuleFor(x => x.BillingCycle)
            .IsInEnum().When(x => x.BillingCycle.HasValue)
            .WithMessage("Invalid billing cycle specified.");
    }
}
