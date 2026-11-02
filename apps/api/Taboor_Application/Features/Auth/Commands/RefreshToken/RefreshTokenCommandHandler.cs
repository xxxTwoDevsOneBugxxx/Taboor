using AutoMapper;
using MediatR;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Constants;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;
using Taboor_Application.Common.Security;
using Taboor_Application.Features.Auth.DTOs;
using Taboor_Application.ServiceInterfaces;
using Taboor_Domain.Entities;
using RefreshTokenEntity = Taboor_Domain.Entities.RefreshToken;

namespace Taboor_Application.Features.Auth.Commands.RefreshToken;

/// <summary>
/// Handler for RefreshTokenCommand.
/// Enforces reuse detection, token rotation, revocation check, and issues a new token pair.
/// </summary>
public class RefreshTokenCommandHandler(
    UserManager<ApplicationUser> userManager,
    ITokenService tokenService,
    IApplicationDbContext context,
    IMapper mapper) 
    : IRequestHandler<RefreshTokenCommand, TokenResponseDto>
{
    public async Task<TokenResponseDto> Handle(RefreshTokenCommand request, CancellationToken cancellationToken)
    {
        var hashedToken = TokenHasher.Hash(request.RefreshToken);

        // 1. Locate the stored refresh token by hash
        var storedToken = await context.RefreshTokens
            .FirstOrDefaultAsync(rt => rt.Token == hashedToken, cancellationToken);

        if (storedToken == null)
        {
            throw AppException.Unauthorized("Invalid refresh token.");
        }

        // 2. Reuse (theft) detection: If already used, revoke all tokens for this user family
        if (storedToken.IsUsed)
        {
            var activeUserTokens = await context.RefreshTokens
                .Where(rt => rt.UserId == storedToken.UserId && !rt.IsRevoked)
                .ToListAsync(cancellationToken);

            foreach (var token in activeUserTokens)
            {
                token.Revoke();
            }

            await context.SaveChangesAsync(cancellationToken);

            throw AppException.Unauthorized("Refresh token reuse detected. All active sessions have been revoked for security.");
        }

        // 3. Revocation check
        if (storedToken.IsRevoked)
        {
            throw AppException.Unauthorized("Refresh token has been revoked.");
        }

        // 4. Expiration check
        if (storedToken.Expiry <= DateTime.UtcNow)
        {
            throw AppException.Unauthorized("Refresh token has expired.");
        }

        // 5. User lookup & status checks
        var user = await userManager.FindByIdAsync(storedToken.UserId);
        if (user == null || user.IsBlocked)
        {
            throw AppException.Unauthorized("User not found or account has been blocked.");
        }

        // 6. Single-use rotation: Mark current token as used
        storedToken.MarkAsUsed();

        // 7. Generate new token pair
        var newAccessToken = await tokenService.GenerateAccessToken(user);
        var newRefreshToken = tokenService.GenerateRefreshToken();
        var newExpiry = DateTime.UtcNow.AddDays(7);

        var newRefreshTokenEntity = RefreshTokenEntity.Create(user.Id, TokenHasher.Hash(newRefreshToken), newExpiry);
        context.RefreshTokens.Add(newRefreshTokenEntity);

        await context.SaveChangesAsync(cancellationToken);

        // 8. Map user details to UserDto
        var roles = await userManager.GetRolesAsync(user);
        var primaryRole = roles.FirstOrDefault() ?? SD.Customer;
        var userDto = mapper.Map<UserDto>(user) with { Role = primaryRole };

        return new TokenResponseDto(
            newAccessToken,
            newRefreshToken,
            newExpiry,
            userDto);
    }
}
