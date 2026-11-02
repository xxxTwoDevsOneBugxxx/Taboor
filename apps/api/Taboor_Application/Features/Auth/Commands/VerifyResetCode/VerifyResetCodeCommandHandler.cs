using MediatR;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;
using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Auth.Commands.VerifyResetCode;

/// <summary>
/// Handler for VerifyResetCodeCommand.
/// Validates the password reset OTP code and extends validity window for password completion.
/// </summary>
public class VerifyResetCodeCommandHandler(IApplicationDbContext context) 
    : IRequestHandler<VerifyResetCodeCommand>
{
    public async Task Handle(VerifyResetCodeCommand request, CancellationToken cancellationToken)
    {
        var normalizedEmail = request.Email.Trim().ToLower();

        var otp = await context.OtpCodes
            .FirstOrDefaultAsync(o => o.Email.ToLower() == normalizedEmail
                && o.Code == request.Code
                && o.Purpose == OtpPurpose.PasswordReset
                && !o.IsUsed
                && o.ExpiresAt > DateTime.UtcNow, cancellationToken);

        if (otp == null)
        {
            throw AppException.Validation("The reset code is invalid or has expired.");
        }

        otp.MarkAsVerified(extendedMinutes: 30);
        await context.SaveChangesAsync(cancellationToken);
    }
}
