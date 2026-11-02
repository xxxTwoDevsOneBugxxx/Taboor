namespace Taboor_Application.Common.Exceptions;

/// <summary>
/// Unified application exception for all domain and business-level failures.
/// </summary>
public class AppException : Exception
{
    public ErrorType ErrorType { get; }
    public IDictionary<string, string[]>? ValidationErrors { get; }

    public AppException(string message, ErrorType errorType) : base(message)
    {
        ErrorType = errorType;
    }

    public AppException(IDictionary<string, string[]> validationErrors)
        : base("One or more validation failures have occurred.")
    {
        ErrorType = ErrorType.Validation;
        ValidationErrors = validationErrors;
    }

    #region Static Factory Helpers

    public static AppException NotFound(string message)
        => new(message, ErrorType.NotFound);

    public static AppException Conflict(string message)
        => new(message, ErrorType.Conflict);

    public static AppException Forbidden(string message = "You do not have permission to access this resource.")
        => new(message, ErrorType.Forbidden);

    public static AppException Unauthorized(string message = "Unauthorized access.")
        => new(message, ErrorType.Unauthorized);

    public static AppException Validation(string message)
        => new(message, ErrorType.Validation);

    public static AppException Validation(string propertyName, string errorMessage)
        => new(new Dictionary<string, string[]> { [propertyName] = [errorMessage] });

    public static AppException Validation(IDictionary<string, string[]> errors)
        => new(errors);

    #endregion
}
