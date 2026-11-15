using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Taboor_Infrastructure.Config;
using Taboor_Infrastructure.DB;
using Taboor_Seeder;

var environmentName = Environment.GetEnvironmentVariable("DOTNET_ENVIRONMENT") 
                      ?? Environment.GetEnvironmentVariable("ASPNETCORE_ENVIRONMENT") 
                      ?? "Development";

var configuration = new ConfigurationBuilder()
    .SetBasePath(AppContext.BaseDirectory)
    .AddJsonFile("appsettings.json", optional: true, reloadOnChange: false)
    .AddJsonFile($"appsettings.{environmentName}.json", optional: true, reloadOnChange: false)
    .AddJsonFile(Path.Combine(AppContext.BaseDirectory, "../../../Taboor_API/appsettings.json"), optional: true, reloadOnChange: false)
    .AddJsonFile(Path.Combine(AppContext.BaseDirectory, $"../../../Taboor_API/appsettings.{environmentName}.json"), optional: true, reloadOnChange: false)
    .AddEnvironmentVariables()
    .Build();

var connectionString = configuration.GetConnectionString("DefaultConnectionString");
if (string.IsNullOrWhiteSpace(connectionString))
{
    Console.ForegroundColor = ConsoleColor.Red;
    Console.WriteLine($"[ERROR] Connection string 'DefaultConnectionString' is missing or empty in appsettings.{environmentName}.json!");
    Console.ResetColor();
    return;
}

var host = Host.CreateDefaultBuilder(args)
    .ConfigureServices((_, services) =>
    {
        services.AddInfrastructureServices(configuration);
        services.AddScoped<DataSeeder>();
    })
    .Build();

using (var scope = host.Services.CreateScope())
{
    var context = scope.ServiceProvider.GetRequiredService<ApplicationDbContext>();
    
    // Check & apply pending migrations defensively before seeding
    if (context.Database.GetPendingMigrations().Any())
    {
        Console.ForegroundColor = ConsoleColor.Yellow;
        Console.WriteLine("[INFO] Applying pending migrations before seeding...");
        Console.ResetColor();
        await context.Database.MigrateAsync();
    }

    var seeder = scope.ServiceProvider.GetRequiredService<DataSeeder>();
    await seeder.SeedAllAsync();
}
