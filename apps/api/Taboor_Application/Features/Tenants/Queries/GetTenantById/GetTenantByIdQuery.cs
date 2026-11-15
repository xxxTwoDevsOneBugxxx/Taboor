using MediatR;
using Taboor_Application.Features.Tenants.DTOs;

namespace Taboor_Application.Features.Tenants.Queries.GetTenantById;

/// <summary>
/// Query to retrieve complete organizational profile, subscriptions, and members for a specific tenant by ID.
/// </summary>
/// <param name="Id">The unique identifier of the tenant.</param>
public record GetTenantByIdQuery(Guid Id) : IRequest<TenantDetailDto>;
