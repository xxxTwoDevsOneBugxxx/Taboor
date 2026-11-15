using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Constants;
using Taboor_Domain.Entities;
using Taboor_Infrastructure.DB;
using Taboor_Seeder.SeedData;

namespace Taboor_Seeder;

public class DataSeeder(
    ApplicationDbContext context,
    RoleManager<ApplicationRole> roleManager,
    UserManager<ApplicationUser> userManager)
{
    public async Task SeedAllAsync()
    {
        Console.ForegroundColor = ConsoleColor.Cyan;
        Console.WriteLine("\n=======================================================");
        Console.WriteLine("        TABOOR SAAS DATABASE SEEDER (PROD READY)");
        Console.WriteLine("=======================================================\n");
        Console.ResetColor();

        // 1. Roles
        await SeedRolesAsync();

        // 2. Platform Admins
        await SeedPlatformAdminAsync();

        // 3. Subscription Plans
        await SeedPlansAsync();

        // 4. Multi-Tenant Organizations with Subscriptions and Staff
        await SeedTenantsAsync();

        Console.ForegroundColor = ConsoleColor.Green;
        Console.WriteLine("\n[SUCCESS] All database seed operations completed successfully!\n");
        Console.ResetColor();
    }

    private async Task SeedRolesAsync()
    {
        Console.WriteLine("[STEP 1/4] Seeding Platform Identity Roles...");
        string[] roles = [SD.PlatformAdmin, SD.Business, SD.Customer];

        foreach (var roleName in roles)
        {
            if (!await roleManager.RoleExistsAsync(roleName))
            {
                await roleManager.CreateAsync(new ApplicationRole { Name = roleName });
                Console.WriteLine($"  [+] Created Role: {roleName}");
            }
            else
            {
                Console.WriteLine($"  [.] Role already exists: {roleName}");
            }
        }
    }

    private async Task SeedPlatformAdminAsync()
    {
        Console.WriteLine("\n[STEP 2/4] Seeding Platform Super Admin...");
        const string adminEmail = "admin@taboor.com";

        var adminUser = await userManager.FindByEmailAsync(adminEmail);
        if (adminUser == null)
        {
            adminUser = new ApplicationUser
            {
                UserName = adminEmail,
                Email = adminEmail,
                FullName = "مدير منصة طابور (Super Admin)",
                PhoneNumber = "01000000001",
                EmailConfirmed = true,
                PreferredLanguage = "ar",
                CreatedAt = DateTime.UtcNow
            };

            var result = await userManager.CreateAsync(adminUser, "Admin@123");
            if (result.Succeeded)
            {
                await userManager.AddToRoleAsync(adminUser, SD.PlatformAdmin);
                Console.ForegroundColor = ConsoleColor.Green;
                Console.WriteLine($"  [+] Created Super Admin: {adminEmail} (Pass: Admin@123)");
                Console.ResetColor();
            }
        }
        else
        {
            Console.WriteLine($"  [.] Super Admin already exists: {adminEmail}");
        }
    }

    private async Task SeedPlansAsync()
    {
        Console.WriteLine("\n[STEP 3/4] Seeding Paid Subscription Plans...");

        foreach (var planDef in PlanSeedData.Plans)
        {
            var existingPlan = await context.Plans.FirstOrDefaultAsync(p => p.Name == planDef.Name);
            if (existingPlan == null)
            {
                var plan = Plan.Create(
                    planDef.Name,
                    planDef.Description,
                    planDef.Price,
                    planDef.BillingCycle,
                    planDef.MaxBranches,
                    planDef.MaxServices,
                    planDef.MaxServicesPerBranch);

                context.Plans.Add(plan);
                await context.SaveChangesAsync();

                Console.ForegroundColor = ConsoleColor.Green;
                Console.WriteLine($"  [+] Created Plan: {plan.Name} - {plan.Price:C} / Month (Max Branches: {plan.MaxBranches}, Max Services: {plan.MaxServices})");
                Console.ResetColor();
            }
            else
            {
                Console.WriteLine($"  [.] Plan already exists: {existingPlan.Name} ({existingPlan.Price:C})");
            }
        }
    }

    private async Task SeedTenantsAsync()
    {
        Console.WriteLine("\n[STEP 4/4] Seeding 25 Multi-Tenant Organizations with Members & Subscriptions...");

        var plansMap = await context.Plans.ToDictionaryAsync(p => p.Name, p => p);
        int createdCount = 0;
        int skippedCount = 0;
        int tenantIndex = 0;

        foreach (var item in TenantSeedData.Tenants)
        {
            tenantIndex++;
            if (await context.Tenants.AnyAsync(t => t.Slug == item.Slug))
            {
                skippedCount++;
                continue;
            }

            if (!plansMap.TryGetValue(item.TargetPlanName, out var plan))
            {
                Console.ForegroundColor = ConsoleColor.Yellow;
                Console.WriteLine($"  [!] Warning: Plan '{item.TargetPlanName}' not found for tenant '{item.Name}'. Skipping.");
                Console.ResetColor();
                continue;
            }

            // Generate guaranteed unique phone numbers to avoid IX_Users_PhoneNumber collisions
            var ownerPhone = $"010{10000000 + tenantIndex}";
            var staffPhone = $"011{20000000 + tenantIndex}";

            // 1. Create Owner ApplicationUser
            var ownerUser = await userManager.FindByEmailAsync(item.OwnerEmail);
            if (ownerUser == null)
            {
                ownerUser = new ApplicationUser
                {
                    UserName = item.OwnerEmail,
                    Email = item.OwnerEmail,
                    FullName = item.OwnerName,
                    PhoneNumber = ownerPhone,
                    EmailConfirmed = true,
                    PreferredLanguage = "ar",
                    CreatedAt = DateTime.UtcNow.AddMonths(-Math.Max(1, item.DurationMonths))
                };
                var userResult = await userManager.CreateAsync(ownerUser, "Pass@1234");
                if (userResult.Succeeded)
                {
                    await userManager.AddToRoleAsync(ownerUser, SD.Business);
                }
            }

            // 2. Create Tenant
            var tenant = Tenant.Create(
                item.Name,
                item.Slug,
                item.ContactEmail,
                item.ContactPhone);

            if (item.DesiredTenantStatus == TenantStatus.Suspended)
            {
                tenant.Suspend();
            }

            context.Tenants.Add(tenant);
            await context.SaveChangesAsync();

            // 3. Create TenantMember (Owner)
            var ownerMember = TenantMember.CreateOwner(tenant.Id, ownerUser.Id, item.OwnerJobTitle);
            context.TenantMembers.Add(ownerMember);

            // 4. Create Staff Member (if defined)
            if (!string.IsNullOrWhiteSpace(item.StaffEmail) && !string.IsNullOrWhiteSpace(item.StaffName))
            {
                var staffUser = await userManager.FindByEmailAsync(item.StaffEmail);
                if (staffUser == null)
                {
                    staffUser = new ApplicationUser
                    {
                        UserName = item.StaffEmail,
                        Email = item.StaffEmail,
                        FullName = item.StaffName,
                        PhoneNumber = staffPhone,
                        EmailConfirmed = true,
                        PreferredLanguage = "ar",
                        CreatedAt = DateTime.UtcNow
                    };
                    var staffResult = await userManager.CreateAsync(staffUser, "Pass@1234");
                    if (staffResult.Succeeded)
                    {
                        await userManager.AddToRoleAsync(staffUser, SD.Business);
                    }
                }

                var staffMember = TenantMember.Create(tenant.Id, staffUser.Id, TenantRole.Staff, item.StaffJobTitle);
                context.TenantMembers.Add(staffMember);
            }

            // 5. Create TenantSubscription with desired status
            var now = DateTime.UtcNow;
            TenantSubscription subscription;

            switch (item.DesiredSubscriptionStatus)
            {
                case SubscriptionStatus.Trialing:
                    subscription = TenantSubscription.CreateTrial(
                        tenant.Id,
                        plan.Id,
                        trialDays: 14,
                        externalSubscriptionId: null);
                    break;

                case SubscriptionStatus.Active:
                    subscription = TenantSubscription.CreateActive(
                        tenant.Id,
                        plan.Id,
                        startDate: now.AddMonths(-item.DurationMonths),
                        endDate: now.AddMonths(1),
                        externalSubscriptionId: $"sub_paymob_{item.Slug}");
                    break;

                case SubscriptionStatus.PastDue:
                    subscription = TenantSubscription.CreateActive(
                        tenant.Id,
                        plan.Id,
                        startDate: now.AddMonths(-item.DurationMonths),
                        endDate: now.AddDays(-2),
                        externalSubscriptionId: $"sub_paymob_{item.Slug}");
                    subscription.MarkPastDue();
                    break;

                case SubscriptionStatus.Canceled:
                    subscription = TenantSubscription.CreateActive(
                        tenant.Id,
                        plan.Id,
                        startDate: now.AddMonths(-item.DurationMonths),
                        endDate: now.AddDays(-10),
                        externalSubscriptionId: $"sub_paymob_{item.Slug}");
                    subscription.Cancel("Requested cancellation via billing portal");
                    break;

                case SubscriptionStatus.Expired:
                default:
                    subscription = TenantSubscription.CreateActive(
                        tenant.Id,
                        plan.Id,
                        startDate: now.AddMonths(-item.DurationMonths),
                        endDate: now.AddDays(-30),
                        externalSubscriptionId: $"sub_paymob_{item.Slug}");
                    subscription.Expire();
                    break;
            }

            context.TenantSubscriptions.Add(subscription);
            await context.SaveChangesAsync();

            createdCount++;
            var statusBadge = item.DesiredSubscriptionStatus.ToString().ToUpper();
            Console.WriteLine($"  [{createdCount:D2}] Seeded Tenant: '{item.Name}' ({item.Slug}) | Plan: {item.TargetPlanName} | Status: [{statusBadge}]");
        }

        Console.WriteLine($"\n[Summary] {createdCount} new Tenants seeded, {skippedCount} Tenants already existed (skipped).");
    }
}
