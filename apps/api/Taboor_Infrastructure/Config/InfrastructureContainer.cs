using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Taboor_Application.Common.Interfaces;
using Taboor_Domain.Entities;
using Taboor_Infrastructure.DB;

namespace Taboor_Infrastructure.Config
{
  /// <summary>
  /// Registers infrastructure services (database, identity, DbContext port) with the DI container.
  /// </summary>
  public static class InfrastructureContainer
  {
    /// <summary>
    /// Adds the application database context and ASP.NET Core Identity to the service collection.
    /// </summary>
    /// <param name="services">The service collection to register services into.</param>
    /// <param name="configuration">The application configuration.</param>
    /// <returns>The same service collection so that registrations can be chained.</returns>
    public static IServiceCollection AddInfrastructureServices(
        this IServiceCollection services,
        IConfiguration configuration)
    {
      services.AddDbContext<ApplicationDbContext>(options =>
          options.UseSqlServer(configuration.GetConnectionString("DefaultConnectionString")));

      services.AddScoped<IApplicationDbContext>(sp => 
          sp.GetRequiredService<ApplicationDbContext>());

      services.AddIdentity<ApplicationUser, ApplicationRole>(options =>
      {
        options.Tokens.EmailConfirmationTokenProvider = TokenOptions.DefaultEmailProvider;
        options.Password.RequireDigit = true;
        options.Password.RequiredLength = 8;
        options.Password.RequireNonAlphanumeric = false;
        options.Password.RequireUppercase = true;
        options.Password.RequireLowercase = false;
        options.User.RequireUniqueEmail = true;

        // Lockout settings
        options.Lockout.DefaultLockoutTimeSpan = TimeSpan.FromHours(2);
        options.Lockout.MaxFailedAccessAttempts = 10;
        options.Lockout.AllowedForNewUsers = true;
      })
      .AddPasswordValidator<PasswordValidator<ApplicationUser>>()
      .AddEntityFrameworkStores<ApplicationDbContext>()
      .AddDefaultTokenProviders();

      services.AddHttpContextAccessor();

      return services;
    }
  }
}
