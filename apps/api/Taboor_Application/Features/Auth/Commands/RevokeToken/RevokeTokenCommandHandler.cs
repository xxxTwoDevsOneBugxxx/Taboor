using MediatR;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Interfaces;
using Taboor_Application.Common.Security;

namespace Taboor_Application.Features.Auth.Commands.RevokeToken;

/// <summary>
/// Handler for revoking a user refresh token.
/// </summary>
public class RevokeTokenCommandHandler(IApplicationDbContext context) 
    : IRequestHandler<RevokeTokenCommand>
{
    public async Task Handle(RevokeTokenCommand request, CancellationToken cancellationToken)
    {
        if (string.IsNullOrWhiteSpace(request.RefreshToken) || string.IsNullOrWhiteSpace(request.UserId))
            return;

        var hashedToken = TokenHasher.Hash(request.RefreshToken);

        var token = await context.RefreshTokens
            .FirstOrDefaultAsync(rt => rt.UserId == request.UserId && rt.Token == hashedToken, cancellationToken);

        if (token != null && !token.IsRevoked)
        {
            token.Revoke();
            await context.SaveChangesAsync(cancellationToken);
        }
    }
}
