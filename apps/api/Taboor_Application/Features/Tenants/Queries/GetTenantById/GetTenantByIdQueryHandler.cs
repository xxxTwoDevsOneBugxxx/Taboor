using MediatR;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;
using Taboor_Application.Features.Tenants.DTOs;

namespace Taboor_Application.Features.Tenants.Queries.GetTenantById;

/// <summary>
/// Query handler retrieving complete tenant profile, subscriptions, and team roster.
/// </summary>
public class GetTenantByIdQueryHandler(IApplicationDbContext context)
    : IRequestHandler<GetTenantByIdQuery, TenantDetailDto>
{
    public async Task<TenantDetailDto> Handle(GetTenantByIdQuery request, CancellationToken ct)
    {
        var tenant = await context.Tenants
            .AsNoTracking()
            .Where(t => t.Id == request.Id)
            .Select(t => new
            {
                t.Id,
                t.Name,
                t.Slug,
                t.LogoUrl,
                t.ContactEmail,
                t.ContactPhone,
                t.Status,
                t.CreatedAt,
                t.UpdatedAt
            })
            .FirstOrDefaultAsync(ct);

        if (tenant is null)
        {
            throw AppException.NotFound($"Tenant with ID ({request.Id}) was not found.");
        }

        var subscriptions = await context.TenantSubscriptions
            .AsNoTracking()
            .Where(s => s.TenantId == request.Id)
            .OrderByDescending(s => s.StartDate)
            .Select(s => new TenantSubscriptionDto(
                s.Id,
                s.PlanId,
                s.Plan.Name,
                s.Plan.Price,
                s.Plan.BillingCycle,
                s.Status,
                s.StartDate,
                s.EndDate,
                s.TrialEndsAt,
                s.AutoRenew,
                s.ExternalSubscriptionId,
                s.CanceledAt,
                s.CancellationReason))
            .ToListAsync(ct);

        var members = await context.TenantMembers
            .AsNoTracking()
            .Where(m => m.TenantId == request.Id)
            .OrderBy(m => m.Role)
            .ThenBy(m => m.JoinedAt)
            .Select(m => new TenantMemberDto(
                m.Id,
                m.UserId,
                m.User.FullName,
                m.User.Email!,
                m.User.PhoneNumber!,
                m.Role,
                m.JobTitle,
                m.IsActive,
                m.JoinedAt))
            .ToListAsync(ct);

        var currentSub = subscriptions.FirstOrDefault();

        return new TenantDetailDto(
            tenant.Id,
            tenant.Name,
            tenant.Slug,
            tenant.LogoUrl,
            tenant.ContactEmail,
            tenant.ContactPhone,
            tenant.Status,
            tenant.CreatedAt,
            tenant.UpdatedAt,
            currentSub,
            subscriptions,
            members);
    }
}
