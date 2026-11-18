using FluentValidation;

namespace Taboor_Application.Features.Tenants.Commands.CancelTenantSubscription;

/// <summary>
/// Validator enforcing constraints on CancelTenantSubscriptionCommand.
/// </summary>
public class CancelTenantSubscriptionValidator : AbstractValidator<CancelTenantSubscriptionCommand>
{
    public CancelTenantSubscriptionValidator()
    {
        RuleFor(x => x.TenantId)
            .NotEmpty().WithMessage("Tenant ID is required.");

        RuleFor(x => x.Reason)
            .MaximumLength(500).WithMessage("Cancellation reason cannot exceed 500 characters.")
            .When(x => !string.IsNullOrEmpty(x.Reason));
    }
}
