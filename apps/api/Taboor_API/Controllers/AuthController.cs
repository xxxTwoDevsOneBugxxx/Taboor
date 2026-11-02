using MediatR;
using Taboor_Application.Common;
using Taboor_Application.Features.Auth.Commands.Login;
using Taboor_Application.Features.Auth.Commands.Register;
using Taboor_Application.Features.Auth.Commands.RefreshToken;
using Taboor_Application.Features.Auth.Commands.RevokeToken;
using Taboor_Application.Features.Auth.Commands.LogoutWeb;
using Taboor_Application.Features.Auth.Commands.SendVerificationCode;
using Taboor_Application.Features.Auth.Commands.VerifyEmail;
using Taboor_Application.Features.Auth.Commands.ForgotPassword;
using Taboor_Application.Features.Auth.Commands.VerifyResetCode;
using Taboor_Application.Features.Auth.Commands.ResetPassword;
using Taboor_Application.Features.Auth.DTOs;
using Taboor_Application.DTOs.Auth;
using Taboor_Application.DTOs.Token;
using Taboor_Application.ServiceInterfaces;
using Microsoft.AspNetCore.Antiforgery;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using System.Globalization;
using System.Security.Claims;

namespace Taboor_API.Controllers
{
    /// <summary>
    /// Controller for handling authentication operations including login, registration,
    /// OTP email verification, token refresh, and external login (Google/Facebook).
    /// </summary>
    [Route("api/[Controller]")]
    [ApiController]
    public class AuthController : ControllerBase
    {
        private const string RefreshTokenCookieName = "refreshToken";
        private const string XsrfCookieName = "XSRF-TOKEN";
        private const string XsrfHeaderName = "X-XSRF-TOKEN";
        private const string ClientTypeHeader = "X-Client-Type";
        private const string WebClientType = "web";

        private readonly IExternalLoginService _externalLoginService;
        private readonly IAntiforgery _antiforgery;
        private readonly ISender _sender;
        private readonly ILogger<AuthController> _logger;

        public AuthController(
            IExternalLoginService externalLoginService,
            IAntiforgery antiforgery,
            ISender sender,
            ILogger<AuthController> logger)
        {
            _externalLoginService = externalLoginService ?? throw new ArgumentNullException(nameof(externalLoginService));
            _antiforgery = antiforgery ?? throw new ArgumentNullException(nameof(antiforgery));
            _sender = sender ?? throw new ArgumentNullException(nameof(sender));
            _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        }

        /// <summary>
        /// Determines whether the current request comes from the web dashboard client.
        /// Absence of the header (or any other value) is treated as the mobile flow.
        /// </summary>
        private bool IsWebClient() => Request.Headers[ClientTypeHeader] == WebClientType;

        /// <summary>
        /// Sets the HttpOnly refresh-token cookie for web clients.
        /// </summary>
        private void SetRefreshTokenCookie(string refreshToken)
        {
            // Remove any legacy cookie at a broader path first so it can't
            // accumulate alongside the canonical one and resurrect a session.
            Response.Cookies.Delete(RefreshTokenCookieName, new CookieOptions
            {
                HttpOnly = true,
                Secure = true,
                SameSite = SameSiteMode.None,
                Path = "/api"
            });

            Response.Cookies.Append(RefreshTokenCookieName, refreshToken, new CookieOptions
            {
                HttpOnly = true,
                Secure = true,
                SameSite = SameSiteMode.None,
                Path = "/api/Auth",
                Expires = DateTimeOffset.UtcNow.AddDays(7)
            });
        }

        /// <summary>
        /// Clears the HttpOnly refresh-token cookie for web clients.
        /// </summary>
        private void ClearRefreshTokenCookie()
        {
            Response.Cookies.Delete(RefreshTokenCookieName, new CookieOptions
            {
                HttpOnly = true,
                Secure = true,
                SameSite = SameSiteMode.None,
                Path = "/api/Auth"
            });

            // Also clear any legacy cookie set at the broader path so logout
            // can't be undone by a surviving cookie.
            Response.Cookies.Delete(RefreshTokenCookieName, new CookieOptions
            {
                HttpOnly = true,
                Secure = true,
                SameSite = SameSiteMode.None,
                Path = "/api"
            });
        }

        /// <summary>
        /// Validates the anti-forgery token for state-changing web requests (refresh/revoke).
        /// </summary>
        private async Task<bool> ValidateCsrfTokenAsync()
        {
            try
            {
                await _antiforgery.ValidateRequestAsync(HttpContext);
                return true;
            }
            catch (AntiforgeryValidationException)
            {
                _logger.LogWarning("Anti-forgery token validation failed");
                return false;
            }
        }

        #region Authentication Endpoints

        /// <summary>
        /// Authenticates a user and returns access and refresh tokens.
        /// </summary>
        [HttpPost("Login")]
        public async Task<IActionResult> Login([FromBody] LoginCommand command, CancellationToken ct)
        {
            _logger.LogInformation("Login attempt for email: {Email}", command.Email);

            var response = await _sender.Send(command, ct);

            if (IsWebClient())
            {
                SetRefreshTokenCookie(response.RefreshToken!);
                var webResponse = response with { RefreshToken = null };
                return Ok(ApiResponse<LoginResponseDto>.SuccessResponse(webResponse, "Login successful"));
            }

            return Ok(ApiResponse<LoginResponseDto>.SuccessResponse(response, "Login successful"));
        }

        /// <summary>
        /// Refreshes an access token using a valid refresh token.
        /// </summary>
        [HttpPost("refresh")]
        public async Task<IActionResult> RefreshToken([FromBody] RefreshTokenCommand? request, CancellationToken ct)
        {
            _logger.LogInformation("Refresh token request received");

            var isWebClient = IsWebClient();

            // Web clients authenticate via the HttpOnly cookie and must pass CSRF validation.
            if (isWebClient && !await ValidateCsrfTokenAsync())
            {
                return BadRequest(ApiResponse<object>.FailResponse("Invalid CSRF token"));
            }

            var incomingRefreshToken = isWebClient
                ? Request.Cookies[RefreshTokenCookieName]
                : request?.RefreshToken;

            if (string.IsNullOrEmpty(incomingRefreshToken))
            {
                return Unauthorized(ApiResponse<object>.FailResponse("Refresh token missing"));
            }

            var command = new RefreshTokenCommand(incomingRefreshToken, request?.AccessToken);
            var result = await _sender.Send(command, ct);

            _logger.LogInformation("Token refresh successful");

            if (isWebClient)
            {
                SetRefreshTokenCookie(result.RefreshToken!);
                result = result with { RefreshToken = null };
            }

            return Ok(ApiResponse<TokenResponseDto>.SuccessResponse(result, "Token refreshed successfully"));
        }

        /// <summary>
        /// Revokes a refresh token for the authenticated user.
        /// </summary>
        [HttpPost("revoke")]
        [Authorize]
        public async Task<IActionResult> RevokeToken([FromBody] string? refreshToken, CancellationToken ct)
        {
            _logger.LogInformation("Revoke token request received");

            var isWebClient = IsWebClient();

            // Web clients authenticate via the HttpOnly cookie and must pass CSRF validation.
            if (isWebClient && !await ValidateCsrfTokenAsync())
            {
                return BadRequest(ApiResponse<object>.FailResponse("Invalid CSRF token"));
            }

            var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);
            if (string.IsNullOrEmpty(userId))
            {
                return Unauthorized(ApiResponse<object>.FailResponse("User not authenticated"));
            }

            var incomingRefreshToken = isWebClient
                ? Request.Cookies[RefreshTokenCookieName]
                : refreshToken;

            if (string.IsNullOrEmpty(incomingRefreshToken))
            {
                return BadRequest(ApiResponse<object>.FailResponse("Refresh token is required"));
            }

            await _sender.Send(new RevokeTokenCommand(userId, incomingRefreshToken), ct);

            if (isWebClient)
            {
                ClearRefreshTokenCookie();
            }

            _logger.LogInformation("Refresh token revoked successfully for user {UserId}", userId);
            return Ok(ApiResponse<object>.SuccessResponse(new { message = "Token revoked successfully" }, "Token revoked"));
        }

        /// <summary>
        /// Web logout: clears the HttpOnly refresh-token cookie and revokes the token by
        /// looking it up from the cookie alone.
        /// </summary>
        [HttpPost("logout")]
        public async Task<IActionResult> LogoutWeb(CancellationToken ct)
        {
            var incomingRefreshToken = Request.Cookies[RefreshTokenCookieName];

            if (!string.IsNullOrEmpty(incomingRefreshToken))
            {
                await _sender.Send(new LogoutWebCommand(incomingRefreshToken), ct);
            }

            ClearRefreshTokenCookie();

            _logger.LogInformation("Web logout completed");
            return Ok(ApiResponse<object>.SuccessResponse(new { message = "Logged out successfully" }, "Logged out"));
        }

        /// <summary>
        /// Returns an anti-forgery request token for web clients (double-submit cookie pattern).
        /// The response also sets a non-HttpOnly XSRF-TOKEN cookie that the frontend must echo back
        /// in the X-XSRF-TOKEN header on state-changing requests (refresh/revoke).
        /// </summary>
        [HttpGet("csrf-token")]
        public IActionResult GetCsrfToken()
        {
            var tokens = _antiforgery.GetAndStoreTokens(HttpContext);
            return Ok(ApiResponse<object>.SuccessResponse(
                new { csrfToken = tokens.RequestToken },
                "CSRF token generated"));
        }

        #endregion

        #region Forgot Password Endpoints

        /// <summary>
        /// Initiates the forgot password process by sending a reset code to the user email.
        /// </summary>
        [HttpPost("forgot-password")]
        public async Task<IActionResult> ForgotPassword([FromBody] ForgotPasswordCommand command, CancellationToken ct)
        {
            _logger.LogInformation("Forgot password request for email: {Email}", command.Email);

            await _sender.Send(command, ct);

            return Ok(ApiResponse<object>.SuccessResponse(
                "If this email is registered, a reset code has been sent to it.",
                "Reset code sent"));
        }

        /// <summary>
        /// Verifies the password reset code.
        /// </summary>
        [HttpPost("verify-reset-code")]
        public async Task<IActionResult> VerifyResetCode([FromBody] VerifyResetCodeCommand command, CancellationToken ct)
        {
            _logger.LogInformation("Reset code verification for email: {Email}", command.Email);

            await _sender.Send(command, ct);

            return Ok(ApiResponse<object>.SuccessResponse(null, "Code verified. You can now reset your password."));
        }

        /// <summary>
        /// Resets the user's password using a verified reset code.
        /// </summary>
        [HttpPost("reset-password")]
        public async Task<IActionResult> ResetPassword([FromBody] ResetPasswordCommand command, CancellationToken ct)
        {
            _logger.LogInformation("Password reset for email: {Email}", command.Email);

            await _sender.Send(command, ct);

            return Ok(ApiResponse<object>.SuccessResponse(null, "Password reset successfully. You can now log in."));
        }

        #endregion

        #region Registration Endpoints

        /// <summary>
        /// Registers a new user (requires OTP email verification first).
        /// </summary>
        [HttpPost("Register")]
        public async Task<IActionResult> Register([FromBody] RegisterCommand command, CancellationToken ct)
        {
            _logger.LogInformation("Registration attempt for email: {Email}", command.Email);

            var user = await _sender.Send(command, ct);

            return Ok(ApiResponse<UserDto>.SuccessResponse(user, "Registration successful"));
        }

        #endregion

        #region Email Verification (OTP) Endpoints

        /// <summary>
        /// Verifies a user's email using an OTP code.
        /// </summary>
        [HttpPost("verify-email")]
        public async Task<IActionResult> VerifyEmail([FromBody] VerifyEmailCommand command, CancellationToken ct)
        {
            _logger.LogInformation("Email verification attempt for email: {Email}", command.Email);

            await _sender.Send(command, ct);

            return Ok(ApiResponse<object>.SuccessResponse(null, "Email confirmed successfully."));
        }

        /// <summary>
        /// Sends an OTP verification code to the specified email.
        /// </summary>
        [HttpPost("send-code")]
        public async Task<IActionResult> SendVerificationCodeAsync([FromBody] SendVerificationCodeCommand command, CancellationToken ct)
        {
            _logger.LogInformation("Send verification code request for email: {Email}", command.Email);

            await _sender.Send(command, ct);

            return Ok(ApiResponse<object>.SuccessResponse(null, "Verification code sent to your email."));
        }

        #endregion

        #region External Authentication Endpoints

        /// <summary>
        /// Initiates external authentication with the specified provider (Google/Facebook).
        /// </summary>
        [HttpGet("ExternalLogin")]
        public IActionResult ExternalLogin([FromQuery] string provider, [FromQuery] string? returnUrl = null)
        {
            try
            {
                _logger.LogInformation("External login initiated with provider: {Provider}", provider);

                if (string.IsNullOrEmpty(provider))
                {
                    return BadRequest(ApiResponse<object>.FailResponse("Provider is required"));
                }

                var redirectUrl = Url.Action(nameof(ExternalLoginCallback), "Auth", new { returnUrl }, Request.Scheme);
                var properties = _externalLoginService.ConfigureExternalAuthProperties(provider, redirectUrl!);
                return Challenge(properties, provider);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error initiating external login");
                return BadRequest(ApiResponse<object>.FailResponse("External login failed", new List<string> { ex.Message }));
            }
        }

        /// <summary>
        /// Handles the callback from external authentication providers.
        /// </summary>
        [HttpGet("ExternalLoginCallback")]
        public async Task<IActionResult> ExternalLoginCallback([FromQuery] string? returnUrl = null, [FromQuery] string? remoteError = null)
        {
            try
            {
                _logger.LogInformation("External login callback received");

                var result = await _externalLoginService.HandleExternalLoginCallbackAsync(remoteError, returnUrl);
                if (!string.IsNullOrEmpty(remoteError) || result == null || string.IsNullOrEmpty(result.Email))
                {
                    return Redirect($"{returnUrl}?error=external_login_failed");
                }

                var separator = returnUrl!.Contains("?") ? "&" : "?";
                var url = $"{returnUrl}{separator}email={Uri.EscapeDataString(result.Email)}&isNewUser={result.IsNewUser.ToString().ToLower()}";
                if (!string.IsNullOrEmpty(result.Token))
                {
                    url += $"&token={Uri.EscapeDataString(result.Token)}";
                }
                if (!string.IsNullOrEmpty(result.RefreshToken))
                {
                    url += $"&refreshToken={Uri.EscapeDataString(result.RefreshToken)}";
                }
                return Redirect(url);
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error in external login callback");
                return Redirect($"{returnUrl}?error=external_login_failed");
            }
        }

        /// <summary>
        /// Confirms and completes external user registration.
        /// </summary>
        [HttpPost("ExternalLoginConfirmation")]
        public async Task<IActionResult> ExternalLoginConfirmation([FromBody] ExternalLoginConfirmationDto model)
        {
            try
            {
                _logger.LogInformation("External login confirmation for email: {Email}", model?.Email);

                if (!ModelState.IsValid)
                {
                    var errors = ModelState.Values.SelectMany(v => v.Errors).Select(e => e.ErrorMessage).ToList();
                    return BadRequest(ApiResponse<object>.FailResponse("Validation failed", errors));
                }

                var result = await _externalLoginService.ConfirmExternalUserAsync(model!);
                if (string.IsNullOrEmpty(result.Token))
                {
                    return BadRequest(ApiResponse<object>.FailResponse(
                        "External login confirmation failed",
                        new List<string> { result.Message ?? "Unknown error" }
                    ));
                }

                return Ok(ApiResponse<object>.SuccessResponse(new
                {
                    message = "User registered via external provider",
                    token = result.Token,
                    refreshToken = result.RefreshToken
                }, "External user confirmed"));
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error in external login confirmation");
                return BadRequest(ApiResponse<object>.FailResponse("External login confirmation error", new List<string> { ex.Message }));
            }
        }

        /// <summary>
        /// Handles Google login from a mobile app by validating the Google ID token.
        /// </summary>
        [HttpPost("GoogleMobile")]
        public async Task<IActionResult> GoogleMobileLogin([FromBody] GoogleMobileLoginDto dto)
        {
            try
            {
                _logger.LogInformation("Google mobile login attempt");

                if (dto == null || string.IsNullOrEmpty(dto.IdToken))
                {
                    return BadRequest(ApiResponse<object>.FailResponse("Id token is required"));
                }

                var result = await _externalLoginService.HandleGoogleMobileLoginAsync(dto.IdToken);
                if (string.IsNullOrEmpty(result.Token))
                {
                    return BadRequest(ApiResponse<object>.FailResponse(
                        "Google login failed",
                        new List<string> { result.Message ?? "Unknown error" }
                    ));
                }

                return Ok(ApiResponse<object>.SuccessResponse(new
                {
                    email = result.Email,
                    isNewUser = result.IsNewUser,
                    hasPassword = result.HasPassword,
                    token = result.Token,
                    refreshToken = result.RefreshToken
                }, result.Message ?? "Google login successful"));
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error in Google mobile login");
                return StatusCode(500, new ProblemDetails
                {
                    Title = "Internal server error",
                    Detail = "An error occurred while processing Google login."
                });
            }
        }

        /// <summary>
        /// Handles Facebook login from a mobile app by validating the Facebook access token.
        /// </summary>
        [HttpPost("FacebookMobile")]
        public async Task<IActionResult> FacebookMobileLogin([FromBody] FacebookMobileLoginDto dto)
        {
            try
            {
                _logger.LogInformation("Facebook mobile login attempt");

                if (dto == null || string.IsNullOrEmpty(dto.AccessToken))
                {
                    return BadRequest(ApiResponse<object>.FailResponse("Access token is required"));
                }

                var result = await _externalLoginService.HandleFacebookMobileLoginAsync(dto.AccessToken);
                if (string.IsNullOrEmpty(result.Token))
                {
                    return BadRequest(ApiResponse<object>.FailResponse(
                        "Facebook login failed",
                        new List<string> { result.Message ?? "Unknown error" }
                    ));
                }

                return Ok(ApiResponse<object>.SuccessResponse(new
                {
                    email = result.Email,
                    isNewUser = result.IsNewUser,
                    hasPassword = result.HasPassword,
                    token = result.Token,
                    refreshToken = result.RefreshToken
                }, result.Message ?? "Facebook login successful"));
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Error in Facebook mobile login");
                return StatusCode(500, new ProblemDetails
                {
                    Title = "Internal server error",
                    Detail = "An error occurred while processing Facebook login."
                });
            }
        }

        #endregion
    }
}