using MediatR;
using Microsoft.AspNetCore.Identity;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;
using Taboor_Application.Common.Security;
using Taboor_Application.Features.Auth.DTOs;
using Taboor_Application.ServiceInterfaces;
using Taboor_Domain.Entities;
using RefreshTokenEntity = Taboor_Domain.Entities.RefreshToken;

namespace Taboor_Application.Features.Auth.Commands.Login;

/// <summary>
/// Handler for authenticating a user and issuing JWT access & refresh tokens.
/// </summary>
public class LoginCommandHandler(
    UserManager<ApplicationUser> userManager,
    ITokenService tokenService,
    IApplicationDbContext context) 
    : IRequestHandler<LoginCommand, LoginResponseDto>
{
    public async Task<LoginResponseDto> Handle(LoginCommand request, CancellationToken cancellationToken)
    {
        var normalizedEmail = request.Email.Trim().ToLower();
        var user = await userManager.FindByEmailAsync(normalizedEmail);

        if (user == null)
        {
            throw AppException.Unauthorized("Invalid email or password.");
        }

        // 1. Account status checks
        if (user.IsBlocked)
        {
            throw AppException.Forbidden("This account has been blocked. Please contact support.");
        }

        if (await userManager.IsLockedOutAsync(user))
        {
            var lockoutEnd = await userManager.GetLockoutEndDateAsync(user);
            throw AppException.Forbidden($"Account is locked out until {lockoutEnd?.LocalDateTime:yyyy-MM-dd HH:mm:ss}.");
        }

        // 2. Validate password with ASP.NET Identity
        var passwordValid = await userManager.CheckPasswordAsync(user, request.Password);
        if (!passwordValid)
        {
            await userManager.AccessFailedAsync(user);

            if (await userManager.IsLockedOutAsync(user))
            {
                throw AppException.Forbidden("Account has been locked due to multiple failed login attempts.");
            }

            throw AppException.Unauthorized("Invalid email or password.");
        }

        // Reset failed attempt counter on successful login
        await userManager.ResetAccessFailedCountAsync(user);

        // 3. Retrieve user roles
        var roles = await userManager.GetRolesAsync(user);
        var primaryRole = roles.FirstOrDefault() ?? "Customer";

        // 4. Generate JWT and cryptographic refresh token
        var accessToken = await tokenService.GenerateAccessToken(user);
        var rawRefreshToken = tokenService.GenerateRefreshToken();
        var expiryDays = request.RememberMe ? 30 : 7;
        var refreshTokenExpiry = DateTime.UtcNow.AddDays(expiryDays);

        // 5. Persist hashed refresh token directly via IApplicationDbContext
        var refreshTokenEntity = RefreshTokenEntity.Create(user.Id, TokenHasher.Hash(rawRefreshToken), refreshTokenExpiry);

        context.RefreshTokens.Add(refreshTokenEntity);
        await context.SaveChangesAsync(cancellationToken);

        // 6. Project to UserDto and return response
        var userDto = new UserDto(
            user.Id,
            user.FullName,
            user.Email ?? normalizedEmail,
            user.PhoneNumber,
            user.NationalId,
            user.PreferredLanguage,
            user.NoShowCount,
            user.IsBlocked,
            primaryRole,
            user.IsLocked,
            user.CreatedAt);

        return new LoginResponseDto(
            accessToken,
            rawRefreshToken,
            refreshTokenExpiry,
            userDto);
    }
}
