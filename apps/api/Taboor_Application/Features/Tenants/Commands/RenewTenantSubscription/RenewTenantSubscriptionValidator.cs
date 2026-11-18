using FluentValidation;

namespace Taboor_Application.Features.Tenants.Commands.RenewTenantSubscription;

/// <summary>
/// Validator enforcing valid constraints on RenewTenantSubscriptionCommand.
/// </summary>
public class RenewTenantSubscriptionValidator : AbstractValidator<RenewTenantSubscriptionCommand>
{
    public RenewTenantSubscriptionValidator()
    {
        RuleFor(x => x.TenantId)
            .NotEmpty().WithMessage("Tenant ID is required.");

        RuleFor(x => x.CustomDays)
            .GreaterThan(0).WithMessage("Custom days must be greater than 0.")
            .When(x => x.CustomDays.HasValue);

        RuleFor(x => x.BillingCycle)
            .IsInEnum().WithMessage("Invalid billing cycle.")
            .When(x => x.BillingCycle.HasValue);
    }
}
