using FluentValidation;

namespace Taboor_Application.Features.Plans.Commands.UpdatePlan;

/// <summary>
/// Validates input data for UpdatePlanCommand before the handler executes.
/// </summary>
public class UpdatePlanValidator : AbstractValidator<UpdatePlanCommand>
{
    public UpdatePlanValidator()
    {
        RuleFor(x => x.Id)
            .GreaterThan(0).WithMessage("Invalid plan ID.");

        RuleFor(x => x.Name)
            .NotEmpty().WithMessage("Plan name is required.")
            .MaximumLength(100).WithMessage("Plan name cannot exceed 100 characters.");

        RuleFor(x => x.Description)
            .MaximumLength(500).WithMessage("Description cannot exceed 500 characters.");

        RuleFor(x => x.Price)
            .GreaterThanOrEqualTo(0).WithMessage("Price must be greater than or equal to 0.");

        RuleFor(x => x.BillingCycle)
            .IsInEnum().WithMessage("Invalid billing cycle.");

        RuleFor(x => x.MaxBranches)
            .GreaterThan(0).WithMessage("MaxBranches must be at least 1.");

        RuleFor(x => x.MaxServices)
            .GreaterThan(0).WithMessage("MaxServices must be at least 1.");

        RuleFor(x => x.MaxServicesPerBranch)
            .GreaterThan(0).WithMessage("MaxServicesPerBranch must be at least 1.");
    }
}
