using Microsoft.EntityFrameworkCore;
using Taboor_Domain.Entities;

namespace Taboor_Application.Common.Interfaces;

/// <summary>
/// Abstraction for the application's primary database context.
/// Exposes entity DbSets and transaction commit capabilities directly to use case handlers.
/// </summary>
public interface IApplicationDbContext
{
    DbSet<ApplicationUser> ApplicationUsers { get; set; }
    DbSet<RefreshToken> RefreshTokens { get; set; }
    DbSet<OtpCode> OtpCodes { get; set; }
    DbSet<Plan> Plans { get; set; }
    DbSet<Tenant> Tenants { get; set; }
    DbSet<TenantSubscription> TenantSubscriptions { get; set; }
    DbSet<TenantMember> TenantMembers { get; set; }

    Task<int> SaveChangesAsync(CancellationToken cancellationToken = default);
}
