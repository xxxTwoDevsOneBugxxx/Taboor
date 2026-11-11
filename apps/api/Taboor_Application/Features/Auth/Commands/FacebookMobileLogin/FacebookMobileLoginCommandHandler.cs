using System.Globalization;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using MediatR;
using Microsoft.AspNetCore.Identity;
using Microsoft.Extensions.Configuration;
using Microsoft.IdentityModel.Protocols;
using Microsoft.IdentityModel.Protocols.OpenIdConnect;
using Microsoft.IdentityModel.Tokens;
using Taboor_Application.Common.Constants;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;
using Taboor_Application.Common.Security;
using Taboor_Application.Features.Auth.DTOs;
using Taboor_Application.ServiceInterfaces;
using Taboor_Domain.Entities;
using RefreshTokenEntity = Taboor_Domain.Entities.RefreshToken;

namespace Taboor_Application.Features.Auth.Commands.FacebookMobileLogin;

/// <summary>
/// Handler for FacebookMobileLoginCommand.
/// Validates Facebook access token against OpenID Connect discovery and logs in or creates user.
/// </summary>
public class FacebookMobileLoginCommandHandler(
    UserManager<ApplicationUser> userManager,
    RoleManager<ApplicationRole> roleManager,
    ITokenService tokenService,
    IApplicationDbContext context,
    IConfiguration configuration) 
    : IRequestHandler<FacebookMobileLoginCommand, ExternalAuthResponseDto>
{
    private static readonly ConfigurationManager<OpenIdConnectConfiguration> _facebookConfigManager =
        new ConfigurationManager<OpenIdConnectConfiguration>(
            "https://www.facebook.com/.well-known/openid-configuration/",
            new OpenIdConnectConfigurationRetriever());

    public async Task<ExternalAuthResponseDto> Handle(FacebookMobileLoginCommand request, CancellationToken cancellationToken)
    {
        var appId = configuration["Authentication:Facebook:AppId"];
        string? fbId = null;
        string? fbEmail = null;
        string? fbName = null;

        // 1. Try JWT validation if token has JWT structure
        if (request.AccessToken.Contains('.'))
        {
            try
            {
                var discoveryDocument = await _facebookConfigManager.GetConfigurationAsync(cancellationToken);
                var tokenHandler = new JwtSecurityTokenHandler();

                var validationParameters = new TokenValidationParameters
                {
                    ValidateIssuer = true,
                    ValidIssuer = "https://www.facebook.com",
                    ValidateAudience = true,
                    ValidAudience = appId,
                    ValidateLifetime = true,
                    IssuerSigningKeys = discoveryDocument.SigningKeys,
                    NameClaimType = "name",
                    RoleClaimType = "role"
                };

                var principal = tokenHandler.ValidateToken(request.AccessToken, validationParameters, out _);
                fbId = principal.FindFirst(ClaimTypes.NameIdentifier)?.Value ?? principal.FindFirst("sub")?.Value;
                fbEmail = principal.FindFirst("email")?.Value ?? principal.FindFirst(ClaimTypes.Email)?.Value;
                fbName = principal.FindFirst("name")?.Value ?? principal.FindFirst(ClaimTypes.Name)?.Value;
            }
            catch
            {
                // Fallback to Graph API
            }
        }

        // 2. Fallback to Facebook Graph API for standard OAuth access tokens
        if (string.IsNullOrEmpty(fbId))
        {
            try
            {
                using var httpClient = new HttpClient();
                var graphUrl = $"https://graph.facebook.com/me?fields=id,name,email&access_token={Uri.EscapeDataString(request.AccessToken)}";
                var response = await httpClient.GetAsync(graphUrl, cancellationToken);
                if (response.IsSuccessStatusCode)
                {
                    var content = await response.Content.ReadAsStringAsync(cancellationToken);
                    using var doc = System.Text.Json.JsonDocument.Parse(content);
                    if (doc.RootElement.TryGetProperty("id", out var idProp))
                    {
                        fbId = idProp.GetString();
                    }
                    if (doc.RootElement.TryGetProperty("name", out var nameProp))
                    {
                        fbName = nameProp.GetString();
                    }
                    if (doc.RootElement.TryGetProperty("email", out var emailProp))
                    {
                        fbEmail = emailProp.GetString();
                    }
                }
                else
                {
                    throw AppException.Unauthorized("Facebook access token is invalid or expired.");
                }
            }
            catch (AppException)
            {
                throw;
            }
            catch (Exception ex)
            {
                throw AppException.Unauthorized($"Facebook authentication failed: {ex.Message}");
            }
        }

        if (string.IsNullOrEmpty(fbId))
        {
            throw AppException.Unauthorized("Failed to retrieve Facebook user info.");
        }

        var email = fbEmail;
        var name = fbName ?? email;
        var providerKey = fbId;

        // 1. Check if user exists by Facebook login
        var user = await userManager.FindByLoginAsync("Facebook", providerKey);
        if (user != null)
        {
            return await GenerateAuthResponseAsync(user, isNewUser: false, cancellationToken);
        }

        // 2. Check if user exists by email, and link login
        if (!string.IsNullOrEmpty(email))
        {
            var existingUser = await userManager.FindByEmailAsync(email);
            if (existingUser != null)
            {
                await userManager.AddLoginAsync(existingUser, new UserLoginInfo("Facebook", providerKey, "Facebook"));
                return await GenerateAuthResponseAsync(existingUser, isNewUser: false, cancellationToken);
            }
        }

        // 3. Create new user
        var newUser = new ApplicationUser
        {
            FullName = name ?? "Facebook User",
            Email = email,
            UserName = email ?? $"fb_{providerKey}",
            EmailConfirmed = email != null,
            PreferredLanguage = CultureInfo.CurrentUICulture.Name,
            CreatedAt = DateTime.UtcNow
        };

        var createResult = await userManager.CreateAsync(newUser);
        if (!createResult.Succeeded)
        {
            var errors = createResult.Errors
                .GroupBy(e => e.Code)
                .ToDictionary(g => g.Key, g => g.Select(e => e.Description).ToArray());
            throw AppException.Validation(errors);
        }

        if (!await roleManager.RoleExistsAsync(SD.Customer))
        {
            await roleManager.CreateAsync(new ApplicationRole { Name = SD.Customer });
        }
        await userManager.AddToRoleAsync(newUser, SD.Customer);
        await userManager.AddLoginAsync(newUser, new UserLoginInfo("Facebook", providerKey, "Facebook"));

        return await GenerateAuthResponseAsync(newUser, isNewUser: true, cancellationToken);
    }

    private async Task<ExternalAuthResponseDto> GenerateAuthResponseAsync(
        ApplicationUser user,
        bool isNewUser,
        CancellationToken cancellationToken)
    {
        var accessToken = await tokenService.GenerateAccessToken(user);
        var refreshToken = tokenService.GenerateRefreshToken();
        var refreshTokenExpiry = DateTime.UtcNow.AddDays(7);

        var refreshTokenEntity = RefreshTokenEntity.Create(user.Id, TokenHasher.Hash(refreshToken), refreshTokenExpiry);
        context.RefreshTokens.Add(refreshTokenEntity);
        await context.SaveChangesAsync(cancellationToken);

        var hasPassword = await userManager.HasPasswordAsync(user);

        return new ExternalAuthResponseDto(
            Email: user.Email,
            IsNewUser: isNewUser,
            HasPassword: hasPassword,
            Token: accessToken,
            RefreshToken: refreshToken,
            RefreshTokenExpiry: refreshTokenExpiry,
            Message: "Facebook login successful");
    }
}
