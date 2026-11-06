import Foundation

// MARK: - API Response Envelope (Matching Taboor Backend & Flutter auth_models.dart)

public struct ApiResponse<T: Decodable>: Decodable {
    public let success: Bool
    public let data: T?
    public let message: String?
    public let error: String?
    public let errors: [String]
    public let statusCode: Int?

    enum CodingKeys: String, CodingKey {
        case success
        case isSuccess
        case data
        case message
        case error
        case errors
        case statusCode
    }

    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)

        if let isSuccess = try? container.decode(Bool.self, forKey: .isSuccess) {
            self.success = isSuccess
        } else if let s = try? container.decode(Bool.self, forKey: .success) {
            self.success = s
        } else {
            self.success = false
        }

        self.data = try? container.decodeIfPresent(T.self, forKey: .data)
        self.message = try? container.decodeIfPresent(String.self, forKey: .message)
        self.error = try? container.decodeIfPresent(String.self, forKey: .error)
        self.errors = (try? container.decodeIfPresent([String].self, forKey: .errors)) ?? []
        self.statusCode = try? container.decodeIfPresent(Int.self, forKey: .statusCode)
    }

    public var effectiveError: String? {
        if let error = error, !error.isEmpty { return error }
        if let first = errors.first, !first.isEmpty { return first }
        if !success, let message = message, !message.isEmpty { return message }
        return nil
    }
}

// Marker type for empty/void API payloads (e.g. status 200 with null data)
public struct EmptyPayload: Decodable {}
