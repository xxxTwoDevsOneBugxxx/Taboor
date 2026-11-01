using System.Net;
using System.Text.Json.Serialization;

namespace Taboor_Application.Common
{
    /// <summary>
    /// Unified API Response wrapper across the platform.
    /// </summary>
    public class ApiResponse<T>
    {
        public bool Success { get; set; }

        [JsonIgnore]
        public bool IsSuccess
        {
            get => Success;
            set => Success = value;
        }

        /// <summary>
        /// General human-readable status message for the client.
        /// </summary>
        public string? Message { get; set; }

        /// <summary>
        /// Single error message (primary error representation).
        /// </summary>
        public string? Error { get; set; }

        /// <summary>
        /// Flat list of error messages (for simple toast/alert display in Mobile/Web).
        /// </summary>
        public List<string> Errors { get; set; } = new();

        [JsonIgnore]
        public List<string> ErrorMessages
        {
            get => Errors;
            set => Errors = value;
        }

        /// <summary>
        /// Field-specific validation errors dictionary (e.g. {"Email": ["Invalid format"]}).
        /// Enables forms to bind error messages directly underneath input components.
        /// </summary>
        public IDictionary<string, string[]>? ValidationErrors { get; set; }

        /// <summary>
        /// Strongly-typed response payload.
        /// </summary>
        public T? Data { get; set; }

        [JsonIgnore]
        public object? Result
        {
            get => Data;
            set => Data = (T)value!;
        }

        /// <summary>
        /// HTTP Status Code.
        /// </summary>
        public HttpStatusCode StatusCode { get; set; }

        #region Factory Methods

        public static ApiResponse<T> SuccessResponse(T? data, string? message = null, HttpStatusCode statusCode = HttpStatusCode.OK)
        {
            return new ApiResponse<T>
            {
                Success = true,
                Data = data,
                Message = message,
                StatusCode = statusCode
            };
        }

        public static ApiResponse<T> FailResponse(
            string message,
            List<string>? errors = null,
            IDictionary<string, string[]>? validationErrors = null,
            HttpStatusCode statusCode = HttpStatusCode.BadRequest)
        {
            return new ApiResponse<T>
            {
                Success = false,
                Message = message,
                Error = message,
                Errors = errors ?? new List<string>(),
                ValidationErrors = validationErrors,
                StatusCode = statusCode
            };
        }

        #endregion
    }
}
