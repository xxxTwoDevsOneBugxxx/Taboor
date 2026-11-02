using System.Globalization;
using System.Security.Cryptography;
using MediatR;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Interfaces;
using Taboor_Application.ServiceInterfaces;
using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Auth.Commands.ForgotPassword;

/// <summary>
/// Handler for ForgotPasswordCommand.
/// Generates a password reset OTP and emails it without leaking account existence.
/// </summary>
public class ForgotPasswordCommandHandler(
    UserManager<ApplicationUser> userManager,
    IApplicationDbContext context,
    IEmailSender emailSender,
    IEmailTemplateService emailTemplateService) 
    : IRequestHandler<ForgotPasswordCommand>
{
    public async Task Handle(ForgotPasswordCommand request, CancellationToken cancellationToken)
    {
        var normalizedEmail = request.Email.Trim().ToLower();

        var user = await userManager.FindByEmailAsync(normalizedEmail);
        if (user == null)
        {
            // Security: Prevent account enumeration, do not reveal whether the email exists
            return;
        }

        // 1. Invalidate previous active password-reset codes for this email
        var activeOtps = await context.OtpCodes
            .Where(o => o.Email == normalizedEmail && o.Purpose == OtpPurpose.PasswordReset && !o.IsUsed)
            .ToListAsync(cancellationToken);

        foreach (var otp in activeOtps)
        {
            otp.Invalidate();
        }

        // 2. Generate a secure 6-digit OTP code
        var resetCode = RandomNumberGenerator.GetInt32(100000, 1000000).ToString();

        // 3. Persist the OTP code in database
        var newOtp = OtpCode.Create(normalizedEmail, resetCode, OtpPurpose.PasswordReset, expiryMinutes: 10);
        context.OtpCodes.Add(newOtp);
        await context.SaveChangesAsync(cancellationToken);

        // 4. Send email template
        var language = user.PreferredLanguage ?? CultureInfo.CurrentUICulture.Name;
        var emailBody = emailTemplateService.GeneratePasswordResetEmail(resetCode, language);
        await emailSender.SendEmailAsync(normalizedEmail, "Taboor - Password Reset", emailBody);
    }
}
