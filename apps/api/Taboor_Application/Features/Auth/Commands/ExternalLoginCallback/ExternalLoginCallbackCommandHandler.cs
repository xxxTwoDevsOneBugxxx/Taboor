using System.Globalization;
using System.Security.Claims;
using MediatR;
using Microsoft.AspNetCore.Identity;
using Taboor_Application.Common.Constants;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;
using Taboor_Application.Common.Security;
using Taboor_Application.Features.Auth.DTOs;
using Taboor_Application.ServiceInterfaces;
using Taboor_Domain.Entities;
using RefreshTokenEntity = Taboor_Domain.Entities.RefreshToken;

namespace Taboor_Application.Features.Auth.Commands.ExternalLoginCallback;

/// <summary>
/// Handler for processing OAuth external login callback and returning JWT tokens.
/// </summary>
public class ExternalLoginCallbackCommandHandler(
    SignInManager<ApplicationUser> signInManager,
    UserManager<ApplicationUser> userManager,
    RoleManager<ApplicationRole> roleManager,
    ITokenService tokenService,
    IApplicationDbContext context) 
    : IRequestHandler<ExternalLoginCallbackCommand, ExternalAuthResponseDto>
{
    public async Task<ExternalAuthResponseDto> Handle(ExternalLoginCallbackCommand request, CancellationToken cancellationToken)
    {
        if (!string.IsNullOrEmpty(request.RemoteError))
        {
            return new ExternalAuthResponseDto(
                Email: null,
                IsNewUser: false,
                HasPassword: false,
                Token: null,
                RefreshToken: null,
                Message: $"Error from provider: {request.RemoteError}",
                ReturnUrl: request.ReturnUrl);
        }

        var info = await signInManager.GetExternalLoginInfoAsync();
        if (info == null)
        {
            return new ExternalAuthResponseDto(
                Email: null,
                IsNewUser: false,
                HasPassword: false,
                Token: null,
                RefreshToken: null,
                Message: "Unable to retrieve external login info.",
                ReturnUrl: request.ReturnUrl);
        }

        // 1. Check if user has already linked this external login
        var user = await userManager.FindByLoginAsync(info.LoginProvider, info.ProviderKey);
        if (user != null)
        {
            var signInResult = await signInManager.ExternalLoginSignInAsync(info.LoginProvider, info.ProviderKey, isPersistent: false);
            if (signInResult.Succeeded)
            {
                await signInManager.UpdateExternalAuthenticationTokensAsync(info);
                return await GenerateAuthResponseAsync(user, isNewUser: false, request.ReturnUrl, cancellationToken);
            }

            return new ExternalAuthResponseDto(
                Email: user.Email,
                IsNewUser: false,
                HasPassword: await userManager.HasPasswordAsync(user),
                Token: null,
                RefreshToken: null,
                Message: "External login sign-in failed",
                ReturnUrl: request.ReturnUrl);
        }

        // 2. Extract user claims from external provider
        var email = info.Principal.FindFirstValue(ClaimTypes.Email);
        var name = info.Principal.FindFirstValue(ClaimTypes.Name) 
                   ?? info.Principal.FindFirstValue(ClaimTypes.GivenName) 
                   ?? email;

        if (string.IsNullOrEmpty(email))
        {
            return new ExternalAuthResponseDto(
                Email: null,
                IsNewUser: false,
                HasPassword: false,
                Token: null,
                RefreshToken: null,
                Message: "Email not provided by external provider.",
                ReturnUrl: request.ReturnUrl);
        }

        // 3. Check if user already exists by email
        var existingUser = await userManager.FindByEmailAsync(email);
        if (existingUser != null)
        {
            await userManager.AddLoginAsync(existingUser, info);
            return await GenerateAuthResponseAsync(existingUser, isNewUser: false, request.ReturnUrl, cancellationToken);
        }

        // 4. Create new user automatically
        var newUser = new ApplicationUser
        {
            FullName = name ?? string.Empty,
            Email = email,
            UserName = email,
            EmailConfirmed = true,
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
        await userManager.AddLoginAsync(newUser, info);

        return await GenerateAuthResponseAsync(newUser, isNewUser: true, request.ReturnUrl, cancellationToken);
    }

    private async Task<ExternalAuthResponseDto> GenerateAuthResponseAsync(
        ApplicationUser user,
        bool isNewUser,
        string? returnUrl,
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
            Message: "Logged in successfully via external provider",
            ReturnUrl: returnUrl);
    }
}
