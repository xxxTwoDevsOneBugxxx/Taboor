using MediatR;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Interfaces;
using Taboor_Application.Common.Security;

namespace Taboor_Application.Features.Auth.Commands.LogoutWeb;

/// <summary>
/// Handler for web logout, revoking refresh token by hash without requiring access token or claims.
/// </summary>
public class LogoutWebCommandHandler(IApplicationDbContext context) 
    : IRequestHandler<LogoutWebCommand>
{
    public async Task Handle(LogoutWebCommand request, CancellationToken cancellationToken)
    {
        if (string.IsNullOrWhiteSpace(request.RefreshToken))
            return;

        var hashedToken = TokenHasher.Hash(request.RefreshToken);

        var token = await context.RefreshTokens
            .FirstOrDefaultAsync(rt => rt.Token == hashedToken, cancellationToken);

        if (token != null && !token.IsRevoked)
        {
            token.Revoke();
            await context.SaveChangesAsync(cancellationToken);
        }
    }
}
