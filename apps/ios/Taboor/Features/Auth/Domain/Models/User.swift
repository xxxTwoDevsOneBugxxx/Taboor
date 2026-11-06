import Foundation

// MARK: - Domain User Model (Clean Architecture Entity)

public struct User: Identifiable, Codable, Equatable {
    public let id: String
    public let email: String
    public let fullName: String
    public let phoneNumber: String?
    public let role: String?
    public let token: String?

    public init(
        id: String,
        email: String,
        fullName: String,
        phoneNumber: String? = nil,
        role: String? = nil,
        token: String? = nil
    ) {
        self.id = id
        self.email = email
        self.fullName = fullName
        self.phoneNumber = phoneNumber
        self.role = role
        self.token = token
    }
}
