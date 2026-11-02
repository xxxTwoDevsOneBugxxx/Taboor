using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Taboor_Domain.Entities;
using Taboor_Domain.Repositories;
using Taboor_Domain.Repositories.IRepository;
using Taboor_Infrastructure.DB;
using Taboor_Infrastructure.Persistence.Repositories;

namespace Taboor_Infrastructure.Config
{
  /// <summary>
  /// Registers infrastructure services (database, identity, repositories) with the DI container.
  /// </summary>
  public static class InfrastructureContainer
  {
    /// <summary>
    /// Adds the application database context, ASP.NET Core Identity, and all repository
    /// implementations to the service collection.
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

      services.AddScoped<Taboor_Application.Common.Interfaces.IApplicationDbContext>(sp => 
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

      services.AddScoped<IUnitOfWork, UnitOfWork>();

      // Auto-register all specialized repositories from the Infrastructure assembly.
      // Matches concrete classes that implement a closed IRepository<T> (directly or
      // via a specialized interface such as IRefreshTokenRepository), excluding the
      // generic Repository<T> base class.
      services.Scan(scan => scan
          .FromAssembliesOf(typeof(RefreshTokenRepository))
          .AddClasses(classes => classes
              .Where(type =>
                  !type.IsGenericTypeDefinition &&
                  type.GetInterfaces().Any(i =>
                      i.IsGenericType && i.GetGenericTypeDefinition() == typeof(IRepository<>))))
          .AsImplementedInterfaces()
          .WithScopedLifetime());
      return services;
    }
  }
}