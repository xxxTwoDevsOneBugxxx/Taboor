using MediatR;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;
using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Auth.Commands.VerifyEmail;

/// <summary>
/// Handler for VerifyEmailCommand.
/// Verifies the OTP code against database and extends its validity for registration completion.
/// </summary>
public class VerifyEmailCommandHandler(IApplicationDbContext context) 
    : IRequestHandler<VerifyEmailCommand>
{
    public async Task Handle(VerifyEmailCommand request, CancellationToken cancellationToken)
    {
        var normalizedEmail = request.Email.Trim().ToLower();

        // 1. Locate valid unused OTP code for email verification
        var otp = await context.OtpCodes
            .FirstOrDefaultAsync(o => o.Email.ToLower() == normalizedEmail
                && o.Code == request.Code
                && o.Purpose == OtpPurpose.EmailVerification
                && !o.IsUsed
                && o.ExpiresAt > DateTime.UtcNow, cancellationToken);

        if (otp == null)
        {
            throw AppException.Validation("The verification code is invalid or has expired.");
        }

        // 2. Mark code as verified and extend validity window for 30 minutes to complete registration
        otp.MarkAsVerified(extendedMinutes: 30);
        await context.SaveChangesAsync(cancellationToken);
    }
}
