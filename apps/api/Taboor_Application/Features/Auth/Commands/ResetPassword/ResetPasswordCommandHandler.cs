using System.Globalization;
using MediatR;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;
using Taboor_Application.ServiceInterfaces;
using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Auth.Commands.ResetPassword;

/// <summary>
/// Handler for ResetPasswordCommand.
/// Verifies the OTP, resets the user password, revokes all active sessions, and emails confirmation.
/// </summary>
public class ResetPasswordCommandHandler(
    UserManager<ApplicationUser> userManager,
    IApplicationDbContext context,
    IEmailSender emailSender,
    IEmailTemplateService emailTemplateService) 
    : IRequestHandler<ResetPasswordCommand>
{
    public async Task Handle(ResetPasswordCommand request, CancellationToken cancellationToken)
    {
        var normalizedEmail = request.Email.Trim().ToLower();

        // 1. Enforce verified OTP check
        var hasVerifiedOtp = await context.OtpCodes
            .AnyAsync(o => o.Email == normalizedEmail
                && o.Code == request.Code
                && o.Purpose == OtpPurpose.PasswordReset
                && o.IsVerified
                && !o.IsUsed
                && o.ExpiresAt > DateTime.UtcNow, cancellationToken);

        if (!hasVerifiedOtp)
        {
            throw AppException.Forbidden("Please verify the reset code first.");
        }

        // 2. Locate user
        var user = await userManager.FindByEmailAsync(normalizedEmail);
        if (user == null)
        {
            throw AppException.NotFound("User not found.");
        }

        // 3. Reset password via ASP.NET Identity
        var resetToken = await userManager.GeneratePasswordResetTokenAsync(user);
        var result = await userManager.ResetPasswordAsync(user, resetToken, request.NewPassword);

        if (!result.Succeeded)
        {
            var errors = result.Errors
                .GroupBy(e => e.Code)
                .ToDictionary(g => g.Key, g => g.Select(e => e.Description).ToArray());

            throw AppException.Validation(errors);
        }

        // 4. Invalidate all active OTP codes for password reset
        var activeOtps = await context.OtpCodes
            .Where(o => o.Email == normalizedEmail && o.Purpose == OtpPurpose.PasswordReset && !o.IsUsed)
            .ToListAsync(cancellationToken);

        foreach (var otp in activeOtps)
        {
            otp.Invalidate();
        }

        // 5. Revoke all refresh tokens so the user is logged out everywhere
        var activeTokens = await context.RefreshTokens
            .Where(rt => rt.UserId == user.Id && !rt.IsRevoked)
            .ToListAsync(cancellationToken);

        foreach (var token in activeTokens)
        {
            token.Revoke();
        }

        await context.SaveChangesAsync(cancellationToken);

        // 6. Send confirmation email
        var language = user.PreferredLanguage ?? CultureInfo.CurrentUICulture.Name;
        var emailBody = emailTemplateService.GeneratePasswordResetConfirmationEmail(language);
        await emailSender.SendEmailAsync(normalizedEmail, "Taboor - Password Changed", emailBody);
    }
}
