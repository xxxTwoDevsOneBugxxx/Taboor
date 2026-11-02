using AutoMapper;
using MediatR;
using Microsoft.AspNetCore.Identity;
using Microsoft.EntityFrameworkCore;
using Taboor_Application.Common.Constants;
using Taboor_Application.Common.Exceptions;
using Taboor_Application.Common.Interfaces;
using Taboor_Application.Features.Auth.DTOs;
using Taboor_Domain.Entities;

namespace Taboor_Application.Features.Auth.Commands.Register;

/// <summary>
/// Handler for RegisterCommand.
/// Enforces email verification via OTP, validates uniqueness, creates user and role, and maps to UserDto via AutoMapper.
/// </summary>
public class RegisterCommandHandler(
    UserManager<ApplicationUser> userManager,
    IApplicationDbContext context,
    IMapper mapper) 
    : IRequestHandler<RegisterCommand, UserDto>
{
    public async Task<UserDto> Handle(RegisterCommand request, CancellationToken cancellationToken)
    {
        var normalizedEmail = request.Email.Trim().ToLower();

        // 1. Enforce OTP email verification condition
        var isOtpVerified = await context.OtpCodes
            .AnyAsync(o => o.Email.ToLower() == normalizedEmail
                && o.Purpose == OtpPurpose.EmailVerification
                && o.IsVerified
                && !o.IsUsed
                && o.ExpiresAt > DateTime.UtcNow, cancellationToken);

        if (!isOtpVerified)
        {
            throw AppException.Forbidden("Please verify your email with the OTP code before registering.");
        }

        // 2. Uniqueness check for Email
        var existingUserByEmail = await userManager.FindByEmailAsync(normalizedEmail);
        if (existingUserByEmail != null)
        {
            throw AppException.Conflict("This email is already registered.");
        }

        // 3. Uniqueness check for Phone Number
        var phoneExists = await context.ApplicationUsers
            .AnyAsync(u => u.PhoneNumber == request.PhoneNumber, cancellationToken);

        if (phoneExists)
        {
            throw AppException.Conflict("This phone number is already registered.");
        }

        // 4. Create the ApplicationUser entity
        var user = new ApplicationUser
        {
            UserName = normalizedEmail,
            Email = normalizedEmail,
            FullName = request.FullName.Trim(),
            PhoneNumber = request.PhoneNumber.Trim(),
            EmailConfirmed = true,
            PreferredLanguage = !string.IsNullOrWhiteSpace(request.PreferredLanguage) 
                ? request.PreferredLanguage 
                : System.Globalization.CultureInfo.CurrentUICulture.Name,
            CreatedAt = DateTime.UtcNow
        };

        var identityResult = await userManager.CreateAsync(user, request.Password);
        if (!identityResult.Succeeded)
        {
            var errors = identityResult.Errors
                .GroupBy(e => e.Code)
                .ToDictionary(g => g.Key, g => g.Select(e => e.Description).ToArray());

            throw AppException.Validation(errors);
        }

        // 5. Assign default Customer role
        await userManager.AddToRoleAsync(user, SD.Customer);

        // 6. Invalidate active OTP codes for this email so they cannot be reused
        var activeOtps = await context.OtpCodes
            .Where(o => o.Email.ToLower() == normalizedEmail && o.Purpose == OtpPurpose.EmailVerification && !o.IsUsed)
            .ToListAsync(cancellationToken);

        foreach (var otp in activeOtps)
        {
            otp.IsUsed = true;
        }

        await context.SaveChangesAsync(cancellationToken);

        // 7. Map to UserDto using AutoMapper
        var userDto = mapper.Map<UserDto>(user) with { Role = SD.Customer };
        return userDto;
    }
}
