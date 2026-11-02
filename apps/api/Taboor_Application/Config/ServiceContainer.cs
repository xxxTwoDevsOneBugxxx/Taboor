using FluentValidation;
using MediatR;
using Microsoft.Extensions.DependencyInjection;
using Taboor_Application.Common.Behaviors;
using Taboor_Application.Services;

namespace Taboor_Application.Config
{
    public static class ServiceContainer
    {
        public static IServiceCollection AddApplicationServices(
            this IServiceCollection services)
        {
            var applicationAssembly = typeof(ServiceContainer).Assembly;

            // Register MediatR and Pipeline Behaviors
            services.AddMediatR(cfg =>
            {
                cfg.RegisterServicesFromAssembly(applicationAssembly);
                cfg.AddBehavior(typeof(IPipelineBehavior<,>), typeof(ValidationBehavior<,>));
            });

            // Register all FluentValidation validators found in Taboor_Application
            services.AddValidatorsFromAssembly(applicationAssembly);

            // Auto-register all application services matching their interfaces (Scoped)
            services.Scan(scan => scan
                .FromAssembliesOf(typeof(TokenService))
                .AddClasses(classes => classes.InNamespaces("Taboor_Application.Services"))
                .AsImplementedInterfaces()
                .WithScopedLifetime());

            return services;
        }
    }
}
