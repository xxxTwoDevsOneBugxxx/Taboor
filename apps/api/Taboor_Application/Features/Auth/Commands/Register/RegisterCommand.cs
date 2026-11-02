using MediatR;
using Taboor_Application.Features.Auth.DTOs;

namespace Taboor_Application.Features.Auth.Commands.Register;

/// <summary>
/// Command to register a new user in the system after OTP email verification.
/// </summary>
public record RegisterCommand(
    string FullName,
    string Email,
    string PhoneNumber,
    string Password,
    string ConfirmPassword,
    string? PreferredLanguage = null
) : IRequest<UserDto>;
