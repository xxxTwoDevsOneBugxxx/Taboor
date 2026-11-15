using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using Taboor_Domain.Entities;

namespace Taboor_Infrastructure.Configurations;

public class TenantMemberConfiguration : IEntityTypeConfiguration<TenantMember>
{
    public void Configure(EntityTypeBuilder<TenantMember> builder)
    {
        builder.ToTable("TenantMembers");

        builder.HasKey(m => m.Id);

        builder.Property(m => m.TenantId)
            .IsRequired();

        builder.Property(m => m.UserId)
            .IsRequired()
            .HasMaxLength(450);

        builder.Property(m => m.Role)
            .HasConversion<string>()
            .HasMaxLength(50)
            .IsRequired();

        builder.Property(m => m.JobTitle)
            .HasMaxLength(100);

        builder.Property(m => m.IsActive)
            .HasDefaultValue(true);

        builder.Property(m => m.JoinedAt)
            .IsRequired();

        // Foreign Key Relationships
        builder.HasOne(m => m.Tenant)
            .WithMany()
            .HasForeignKey(m => m.TenantId)
            .OnDelete(DeleteBehavior.Restrict);

        builder.HasOne(m => m.User)
            .WithMany()
            .HasForeignKey(m => m.UserId)
            .OnDelete(DeleteBehavior.Restrict);

        // Indexes
        // 1. Composite Unique Index: A user can only have one membership record per tenant
        builder.HasIndex(m => new { m.TenantId, m.UserId })
            .IsUnique();

        // 2. Fast lookup: "Which tenants does this user belong to?" (e.g. during login / organization switcher)
        builder.HasIndex(m => m.UserId);

        // 3. Fast lookup: "List all staff/members of this tenant"
        builder.HasIndex(m => m.TenantId);
    }
}
