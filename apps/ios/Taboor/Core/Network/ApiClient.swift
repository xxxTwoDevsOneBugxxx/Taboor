import Foundation

// MARK: - HTTP Methods
public enum HTTPMethod: String {
    case get = "GET"
    case post = "POST"
    case put = "PUT"
    case patch = "PATCH"
    case delete = "DELETE"
}

// MARK: - Async/Await Native API Client (Connected to Taboor Live Backend)
public final class ApiClient {
    public static let shared = ApiClient()
    private let session: URLSession

    private init() {
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = AppConstants.API.timeoutInterval
        configuration.timeoutIntervalForResource = AppConstants.API.timeoutInterval * 2
        self.session = URLSession(configuration: configuration)
    }

    /// Performs a network request and automatically unwraps Taboor's `ApiResponse<T>` envelope.
    public func request<T: Decodable>(
        endpoint: String,
        method: HTTPMethod = .get,
        body: (any Encodable)? = nil,
        requiresAuth: Bool = true
    ) async throws -> T {
        let base = AppConstants.API.baseUrl.hasSuffix("/") ? AppConstants.API.baseUrl : "\(AppConstants.API.baseUrl)/"
        let cleanEndpoint = endpoint.hasPrefix("/") ? String(endpoint.dropFirst()) : endpoint

        guard let url = URL(string: "\(base)\(cleanEndpoint)") else {
            throw NetworkError.invalidURL
        }

        var request = URLRequest(url: url)
        request.httpMethod = method.rawValue
        request.setValue("application/json", forHTTPHeaderField: AppConstants.API.Headers.contentType)
        request.setValue("application/json", forHTTPHeaderField: AppConstants.API.Headers.accept)
        request.setValue(AppConstants.API.Headers.clientTypeValue, forHTTPHeaderField: AppConstants.API.Headers.clientType)

        // Inject current language header
        let lang = LocalizationManager.shared.currentLanguage.rawValue
        request.setValue(lang, forHTTPHeaderField: AppConstants.API.Headers.acceptLanguage)

        // Inject Bearer token if required
        if requiresAuth, let token = KeychainManager.shared.get(key: AppConstants.Storage.accessToken) {
            request.setValue("Bearer \(token)", forHTTPHeaderField: AppConstants.API.Headers.authorization)
        }

        // Encode JSON body if provided
        if let body = body {
            do {
                request.httpBody = try JSONEncoder().encode(body)
            } catch {
                throw NetworkError.custom("Failed to encode request body")
            }
        }

        let data: Data
        let response: URLResponse

        do {
            (data, response) = try await session.data(for: request)
        } catch let urlError as URLError {
            if urlError.code == .notConnectedToInternet || urlError.code == .networkConnectionLost {
                throw NetworkError.noInternet
            }
            throw NetworkError.custom(urlError.localizedDescription)
        } catch {
            throw NetworkError.unknown
        }

        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.unknown
        }

        // Try decoding standard Taboor ApiResponse<T>
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601

        if let apiResponse = try? decoder.decode(ApiResponse<T>.self, from: data) {
            if apiResponse.success {
                if let responseData = apiResponse.data {
                    return responseData
                } else if T.self == EmptyPayload.self {
                    return EmptyPayload() as! T
                }
            }

            if let errorMsg = apiResponse.effectiveError {
                throw NetworkError.custom(errorMsg)
            }
        }

        // If not decoded as standard ApiResponse, extract error details from JSON (ProblemDetails / Error dictionaries)
        if let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
            if let message = json["message"] as? String, !message.isEmpty {
                throw NetworkError.custom(message)
            }
            if let detail = json["detail"] as? String, !detail.isEmpty {
                throw NetworkError.custom(detail)
            }
            if let error = json["error"] as? String, !error.isEmpty {
                throw NetworkError.custom(error)
            }
            if let errors = json["errors"] as? [String], let first = errors.first, !first.isEmpty {
                throw NetworkError.custom(first)
            }
            if let errorsDict = json["errors"] as? [String: [String]], let first = errorsDict.values.first?.first, !first.isEmpty {
                throw NetworkError.custom(first)
            }
            if let title = json["title"] as? String, !title.isEmpty, httpResponse.statusCode >= 400 {
                throw NetworkError.custom(title)
            }
        }

        // Check HTTP status code
        switch httpResponse.statusCode {
        case 200...299:
            if T.self == EmptyPayload.self {
                return EmptyPayload() as! T
            }
            throw NetworkError.decodingError("Empty or unexpected response payload")
        case 401:
            KeychainManager.shared.clearAllAuthTokens()
            throw NetworkError.unauthorized
        case 403:
            throw NetworkError.forbidden
        case 404:
            throw NetworkError.notFound
        case 409:
            throw NetworkError.conflict(LocalizationManager.shared.isArabic ? "هذا البريد الإلكتروني مسجل بالفعل" : "This email is already registered")
        default:
            throw NetworkError.serverError(statusCode: httpResponse.statusCode, message: nil)
        }

        throw NetworkError.unknown
    }
}
