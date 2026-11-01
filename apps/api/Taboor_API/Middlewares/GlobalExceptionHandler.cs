using System.Net;
using Microsoft.AspNetCore.Diagnostics;
using Taboor_Application.Common;
using Taboor_Application.Common.Exceptions;

namespace Taboor_API.Middlewares;

/// <summary>
/// Centralized Global Exception Handler for ASP.NET Core (.NET 10).
/// Catches all unhandled exceptions and formats responses consistently using the unified ApiResponse structure.
/// </summary>
public class GlobalExceptionHandler(ILogger<GlobalExceptionHandler> logger) : IExceptionHandler
{
    public async ValueTask<bool> TryHandleAsync(
        HttpContext httpContext,
        Exception exception,
        CancellationToken cancellationToken)
    {
        int statusCode;
        ApiResponse<object> apiResponse;

        if (exception is AppException appException)
        {
            statusCode = appException.ErrorType switch
            {
                ErrorType.Validation => StatusCodes.Status400BadRequest,
                ErrorType.NotFound => StatusCodes.Status404NotFound,
                ErrorType.Conflict => StatusCodes.Status409Conflict,
                ErrorType.Forbidden => StatusCodes.Status403Forbidden,
                ErrorType.Unauthorized => StatusCodes.Status401Unauthorized,
                _ => StatusCodes.Status400BadRequest
            };

            logger.LogWarning(
                "Handled Application Exception ({ErrorType}): {Message} on Path: {Path}",
                appException.ErrorType,
                appException.Message,
                httpContext.Request.Path);

            // Populate both a flattened list of errors and the fine-grained dictionary
            List<string> flatErrors = appException.ValidationErrors != null
                ? appException.ValidationErrors
                    .SelectMany(kvp => kvp.Value.Select(msg => $"{kvp.Key}: {msg}"))
                    .ToList()
                : [appException.Message];

            apiResponse = ApiResponse<object>.FailResponse(
                message: appException.Message,
                errors: flatErrors,
                validationErrors: appException.ValidationErrors,
                statusCode: (HttpStatusCode)statusCode);
        }
        else
        {
            statusCode = StatusCodes.Status500InternalServerError;

            logger.LogError(
                exception,
                "Unhandled Exception occurred on Path: {Path}. Message: {Message}",
                httpContext.Request.Path,
                exception.Message);

            apiResponse = ApiResponse<object>.FailResponse(
                message: "An unexpected internal server error occurred.",
                errors: ["An unexpected server error occurred. Please try again later."],
                validationErrors: null,
                statusCode: HttpStatusCode.InternalServerError);
        }

        httpContext.Response.StatusCode = statusCode;
        httpContext.Response.ContentType = "application/json";

        await httpContext.Response.WriteAsJsonAsync(apiResponse, cancellationToken);

        return true;
    }
}
