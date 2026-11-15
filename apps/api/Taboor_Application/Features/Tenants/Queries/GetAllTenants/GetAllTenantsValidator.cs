using FluentValidation;

namespace Taboor_Application.Features.Tenants.Queries.GetAllTenants;

/// <summary>
/// Validator for pagination and boundary parameters of GetAllTenantsQuery.
/// </summary>
public class GetAllTenantsValidator : AbstractValidator<GetAllTenantsQuery>
{
    public GetAllTenantsValidator()
    {
        RuleFor(x => x.PageNumber)
            .GreaterThanOrEqualTo(1)
            .WithMessage("Page number must be at least 1.");

        RuleFor(x => x.PageSize)
            .InclusiveBetween(1, 100)
            .WithMessage("Page size must be between 1 and 100.");

        RuleFor(x => x.Search)
            .MaximumLength(100)
            .WithMessage("Search term cannot exceed 100 characters.");
    }
}
