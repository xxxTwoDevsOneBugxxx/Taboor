namespace Taboor_Application.Common.Exceptions;

/// <summary>
/// Domain-level classification of application errors, completely decoupled from HTTP protocols.
/// </summary>
public enum ErrorType
{
    Validation,
    NotFound,
    Conflict,
    Forbidden,
    Unauthorized
}
