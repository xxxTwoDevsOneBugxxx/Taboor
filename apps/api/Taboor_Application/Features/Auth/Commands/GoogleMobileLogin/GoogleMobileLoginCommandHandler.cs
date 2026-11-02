using System.Globalization;
using Google.Apis.Auth;
using MediatR;
using Microsoft.AspNetCore.Identity;
using Microsoft.Extensions.Configuration;
using Taboor_Application.Common.Constants;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;
using Taboor_Application.Common.Security;
using Taboor_Application.Features.Auth.DTOs;
using Taboor_Application.ServiceInterfaces;
using Taboor_Domain.Entities;
using RefreshTokenEntity = Taboor_Domain.Entities.RefreshToken;

namespace Taboor_Application.Features.Auth.Commands.GoogleMobileLogin;

/// <summary>
/// Handler for GoogleMobileLoginCommand.
/// Validates Google ID token cryptographically and logs in or creates user.
/// </summary>
public class GoogleMobileLoginCommandHandler(
    UserManager<ApplicationUser> userManager,
    RoleManager<ApplicationRole> roleManager,
    ITokenService tokenService,
    IApplicationDbContext context,
    IConfiguration configuration) 
    : IRequestHandler<GoogleMobileLoginCommand, ExternalAuthResponseDto>
{
    public async Task<ExternalAuthResponseDto> Handle(GoogleMobileLoginCommand request, CancellationToken cancellationToken)
    {
        GoogleJsonWebSignature.Payload payload;

        try
        {
            var googleClientId = configuration["Authentication:Google:ClientId"];
            var settings = new GoogleJsonWebSignature.ValidationSettings();

            if (!string.IsNullOrEmpty(googleClientId) && googleClientId != "YOUR_GOOGLE_CLIENT_ID")
            {
                settings.Audience = [googleClientId];
            }

            payload = await GoogleJsonWebSignature.ValidateAsync(request.IdToken, settings);
        }
        catch (InvalidJwtException ex)
        {
            throw AppException.Unauthorized($"Invalid Google token: {ex.Message}");
        }
        catch (Exception ex)
        {
            throw AppException.Unauthorized($"Google authentication failed: {ex.Message}");
        }

        var email = payload.Email;
        var name = payload.Name ?? email;
        var providerKey = payload.Subject;

        // 1. Check if user exists by Google provider login
        var user = await userManager.FindByLoginAsync("Google", providerKey);
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
                await userManager.AddLoginAsync(existingUser, new UserLoginInfo("Google", providerKey, "Google"));
                return await GenerateAuthResponseAsync(existingUser, isNewUser: false, cancellationToken);
            }
        }

        // 3. Create new user
        var newUser = new ApplicationUser
        {
            FullName = name ?? string.Empty,
            Email = email,
            UserName = email,
            EmailConfirmed = !string.IsNullOrEmpty(email),
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
        await userManager.AddLoginAsync(newUser, new UserLoginInfo("Google", providerKey, "Google"));

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
            Message: "Google login successful");
    }
}
