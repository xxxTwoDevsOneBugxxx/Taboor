using System.Globalization;
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

namespace Taboor_Application.Features.Auth.Commands.ExternalLoginConfirmation;

/// <summary>
/// Handler for ExternalLoginConfirmationCommand.
/// </summary>
public class ExternalLoginConfirmationCommandHandler(
    SignInManager<ApplicationUser> signInManager,
    UserManager<ApplicationUser> userManager,
    RoleManager<ApplicationRole> roleManager,
    ITokenService tokenService,
    IApplicationDbContext context) 
    : IRequestHandler<ExternalLoginConfirmationCommand, ExternalAuthResponseDto>
{
    public async Task<ExternalAuthResponseDto> Handle(ExternalLoginConfirmationCommand request, CancellationToken cancellationToken)
    {
        var normalizedEmail = request.Email.Trim().ToLower();

        var info = await signInManager.GetExternalLoginInfoAsync();
        if (info == null)
        {
            throw AppException.Forbidden("Invalid or expired external login session.");
        }

        var existingUser = await userManager.FindByEmailAsync(normalizedEmail);
        if (existingUser != null)
        {
            throw AppException.Conflict("This email is already registered.");
        }

        var user = new ApplicationUser
        {
            FullName = request.Name.Trim(),
            Email = normalizedEmail,
            UserName = normalizedEmail,
            CreatedAt = DateTime.UtcNow,
            EmailConfirmed = true,
            PreferredLanguage = CultureInfo.CurrentUICulture.Name
        };

        var result = await userManager.CreateAsync(user);
        if (!result.Succeeded)
        {
            var errors = result.Errors
                .GroupBy(e => e.Code)
                .ToDictionary(g => g.Key, g => g.Select(e => e.Description).ToArray());
            throw AppException.Validation(errors);
        }

        if (!await roleManager.RoleExistsAsync(SD.Customer))
        {
            await roleManager.CreateAsync(new ApplicationRole { Name = SD.Customer });
        }
        await userManager.AddToRoleAsync(user, SD.Customer);

        var loginResult = await userManager.AddLoginAsync(user, info);
        if (!loginResult.Succeeded)
        {
            var errors = loginResult.Errors
                .GroupBy(e => e.Code)
                .ToDictionary(g => g.Key, g => g.Select(e => e.Description).ToArray());
            throw AppException.Validation(errors);
        }

        var accessToken = await tokenService.GenerateAccessToken(user);
        var refreshToken = tokenService.GenerateRefreshToken();
        var refreshTokenExpiry = DateTime.UtcNow.AddDays(7);

        var refreshTokenEntity = RefreshTokenEntity.Create(user.Id, TokenHasher.Hash(refreshToken), refreshTokenExpiry);
        context.RefreshTokens.Add(refreshTokenEntity);
        await context.SaveChangesAsync(cancellationToken);

        return new ExternalAuthResponseDto(
            Email: user.Email,
            IsNewUser: true,
            HasPassword: await userManager.HasPasswordAsync(user),
            Token: accessToken,
            RefreshToken: refreshToken,
            RefreshTokenExpiry: refreshTokenExpiry,
            Message: "User registered successfully via external provider.");
    }
}
