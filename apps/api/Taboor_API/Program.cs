using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.DataProtection;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Localization;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi;
using Scalar.AspNetCore;
using System.Globalization;
using System.Security.Claims;
using System.Text;
using Taboor_API.MappingConfig;
using Taboor_Application.Config;
using Taboor_Domain.Entities;
using Taboor_Infrastructure.Config;
using Taboor_Infrastructure.DB;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddInfrastructureServices(builder.Configuration);
builder.Services.AddApplicationServices();
builder.Services.AddExceptionHandler<Taboor_API.Middlewares.GlobalExceptionHandler>();
builder.Services.AddProblemDetails();

// Persist Data Protection keys so antiforgery/CSRF tokens survive restarts and deploys.
// Defensive: on hosts where the key path isn't writable (e.g. IIS app pools), fall back
// to default key storage instead of crashing at startup (HTTP 500.30).
var dataProtectionKeyPath = builder.Configuration["DataProtection:KeyPath"];
try
{
    if (string.IsNullOrEmpty(dataProtectionKeyPath))
    {
        dataProtectionKeyPath = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "Taboor",
            "DataProtection-Keys");
    }
    Directory.CreateDirectory(dataProtectionKeyPath);
    builder.Services.AddDataProtection()
        .PersistKeysToFileSystem(new DirectoryInfo(dataProtectionKeyPath));
}
catch (Exception ex)
{
    // Unwritable key path on the host -> use default key storage so startup never fails.
    builder.Services.AddDataProtection();
    Console.Error.WriteLine($"DataProtection key path unavailable, using default key storage: {ex.Message}");
}

builder.Services.AddControllers();
builder.Services.AddAutoMapper(cfg => { }, typeof(MappingConfig).Assembly);

// CORS for the web dashboard (cross-origin cookies require explicit origins + credentials)
builder.Services.AddCors(options =>
{
  options.AddPolicy("Dashboard", policy =>
  {
    var origins = builder.Configuration.GetSection("Dashboard:AllowedOrigins").Get<string[]>()
        ?? new[] { builder.Configuration["Dashboard:Origin"]! };

    policy.WithOrigins(origins)
          .AllowAnyHeader()
          .AllowAnyMethod()
          .AllowCredentials();
  });
});

// CSRF protection (double-submit cookie) for cookie-authenticated web requests
builder.Services.AddAntiforgery(options =>
{
  options.HeaderName = "X-XSRF-TOKEN";
  options.Cookie.Name = "XSRF-TOKEN";
  options.Cookie.HttpOnly = false; // JS must read it to echo back in the header
  options.Cookie.SecurePolicy = CookieSecurePolicy.Always;
  options.Cookie.SameSite = SameSiteMode.None;
});

// Localization: register IStringLocalizer for SharedResources (resx files inside Taboor_Application)
builder.Services.AddLocalization();

// Localization: detect user language from Accept-Language header (Arabic/English)
var supportedCultures = new[] { "ar", "en" };
var requestLocalizationOptions = new RequestLocalizationOptions
{
  DefaultRequestCulture = new RequestCulture("en"),
  SupportedCultures = supportedCultures.Select(c => new CultureInfo(c)).ToList(),
  SupportedUICultures = supportedCultures.Select(c => new CultureInfo(c)).ToList(),
  RequestCultureProviders = new List<IRequestCultureProvider>
    {
        new AcceptLanguageHeaderRequestCultureProvider()
    }
};

// =======================
// JWT + External Login Authentication
// =======================
var authenticationBuilder = builder.Services
    .AddAuthentication(options =>
    {
      options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
      options.DefaultScheme = JwtBearerDefaults.AuthenticationScheme;
      options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
    })
    .AddJwtBearer(options =>
    {
      options.SaveToken = true;
      options.TokenValidationParameters = new TokenValidationParameters
      {
        ValidateAudience = true,
        ValidateIssuer = true,
        ValidateIssuerSigningKey = true,
        ValidateLifetime = true,
        RequireExpirationTime = true,
        ValidIssuer = builder.Configuration["JWT:Issuer"],
        ValidAudience = builder.Configuration["JWT:Audience"],
        IssuerSigningKey = new SymmetricSecurityKey(
              Encoding.UTF8.GetBytes(builder.Configuration["JWT:Key"]!)
          ),
        NameClaimType = ClaimTypes.NameIdentifier,
        RoleClaimType = ClaimTypes.Role
      };
    });

// External login providers (only registered when credentials are configured)
var fbAppId = builder.Configuration["Authentication:Facebook:AppId"];
var fbAppSecret = builder.Configuration["Authentication:Facebook:AppSecret"];
if (!string.IsNullOrEmpty(fbAppId) && !string.IsNullOrEmpty(fbAppSecret))
{
  authenticationBuilder.AddFacebook(facebookOptions =>
  {
    facebookOptions.AppId = fbAppId;
    facebookOptions.AppSecret = fbAppSecret;
    facebookOptions.Scope.Add("email");
  });
}

var googleClientId = builder.Configuration["Authentication:Google:ClientId"];
var googleClientSecret = builder.Configuration["Authentication:Google:ClientSecret"];
if (!string.IsNullOrEmpty(googleClientId) && !string.IsNullOrEmpty(googleClientSecret))
{
  authenticationBuilder.AddGoogle(googleOptions =>
  {
    googleOptions.ClientId = googleClientId;
    googleOptions.ClientSecret = googleClientSecret;
  });
}

// OpenAPI + Scalar
builder.Services.AddOpenApi(options =>
{
  options.AddDocumentTransformer((document, context, cancellationToken) =>
  {
    document.Info = new OpenApiInfo
    {
      Title = "Taboor API",
      Version = "v1",
      Description = "Taboor API Documentation"
    };

    document.Components ??= new OpenApiComponents();
    document.Components.SecuritySchemes = new Dictionary<string, IOpenApiSecurityScheme>
    {
      ["Bearer"] = new OpenApiSecurityScheme
      {
        Type = SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT",
        In = ParameterLocation.Header,
        Description = "Bearer {your JWT token}"
      }
    };

    if (document.Paths != null)
    {
      foreach (var path in document.Paths.Values)
      {
        if (path.Operations == null) continue;
        foreach (var operation in path.Operations)
        {
          operation.Value.Security = new List<OpenApiSecurityRequirement>
                  {
                        new OpenApiSecurityRequirement
                        {
                            [new OpenApiSecuritySchemeReference("Bearer")] = new List<string>()
                        }
                  };
        }
      }
    }

    return Task.CompletedTask;
  });
});

var app = builder.Build();

// Seed database (apply migrations + roles)
using (var scope = app.Services.CreateScope())
{
  var db = scope.ServiceProvider.GetRequiredService<ApplicationDbContext>();
  var roleManager = scope.ServiceProvider.GetRequiredService<RoleManager<ApplicationRole>>();
  var userManager = scope.ServiceProvider.GetRequiredService<UserManager<ApplicationUser>>();
  await DbInitializer.InitializeAsync(db, roleManager, userManager);
}

// Configure the HTTP request pipeline.
app.UseExceptionHandler();

app.UseRequestLocalization(requestLocalizationOptions);

app.UseHttpsRedirection();

app.UseCors("Dashboard");

app.UseAuthentication();
app.UseAuthorization();

app.MapControllers();

// Scalar endpoints
app.MapOpenApi();

app.MapScalarApiReference(options =>
{
  options.Title = "Taboor API Docs";
  options.Theme = ScalarTheme.BluePlanet;
});

app.MapGet("/env", (IWebHostEnvironment env) => new
{
  Environment = env.EnvironmentName
});

app.MapGet("/", context =>
{
  context.Response.Redirect("/scalar");
  return Task.CompletedTask;
});

app.Run();