using System.Globalization;
using System.Security.Cryptography;
using MediatR;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;
using Taboor_Application.ServiceInterfaces;
using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Auth.Commands.SendVerificationCode;

/// <summary>
/// Handler for SendVerificationCodeCommand.
/// Enforces email uniqueness, invalidates stale codes, creates a new OTP code, and emails it.
/// </summary>
public class SendVerificationCodeCommandHandler(
    UserManager<ApplicationUser> userManager,
    IApplicationDbContext context,
    IEmailSender emailSender,
    IEmailTemplateService emailTemplateService) 
    : IRequestHandler<SendVerificationCodeCommand>
{
    public async Task Handle(SendVerificationCodeCommand request, CancellationToken cancellationToken)
    {
        var normalizedEmail = request.Email.Trim().ToLower();

        // 1. Check if email is already registered
        var existingUser = await userManager.FindByEmailAsync(normalizedEmail);
        if (existingUser != null)
        {
            throw AppException.Conflict("This email is already registered.");
        }

        // 2. Invalidate any existing unused verification codes for this email
        var activeOtps = await context.OtpCodes
            .Where(o => o.Email == normalizedEmail && o.Purpose == OtpPurpose.EmailVerification && !o.IsUsed)
            .ToListAsync(cancellationToken);

        foreach (var otp in activeOtps)
        {
            otp.Invalidate();
        }

        // 3. Generate a secure 6-digit OTP code
        var code = RandomNumberGenerator.GetInt32(100000, 1000000).ToString();

        // 4. Create and persist the OTP code
        var newOtp = OtpCode.Create(normalizedEmail, code, OtpPurpose.EmailVerification, expiryMinutes: 10);
        context.OtpCodes.Add(newOtp);
        await context.SaveChangesAsync(cancellationToken);

        // 5. Generate template and send verification email
        var language = CultureInfo.CurrentUICulture.Name;
        var emailBody = emailTemplateService.GenerateVerificationEmail(code, language);
        await emailSender.SendEmailAsync(normalizedEmail, "Taboor - Verification Code", emailBody);
    }
}
