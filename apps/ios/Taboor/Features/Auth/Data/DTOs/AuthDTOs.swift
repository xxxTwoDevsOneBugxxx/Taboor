import Foundation

// MARK: - Auth Data Transfer Objects (Mirroring Flutter auth_models.dart & ASP.NET Core)

// MARK: - Requests

public struct LoginRequestDTO: Encodable {
    public let email: String
    public let password: String

    public init(email: String, password: String) {
        self.email = email
        self.password = password
    }
}

public struct RegisterRequestDTO: Encodable {
    public let fullName: String
    public let email: String
    public let phoneNumber: String?
    public let password: String
    public let confirmPassword: String

    public init(
        fullName: String,
        email: String,
        phoneNumber: String?,
        password: String,
        confirmPassword: String
    ) {
        self.fullName = fullName
        self.email = email
        self.phoneNumber = phoneNumber
        self.password = password
        self.confirmPassword = confirmPassword
    }
}

public struct SendCodeRequestDTO: Encodable {
    public let email: String

    public init(email: String) {
        self.email = email
    }
}

public struct VerifyCodeRequestDTO: Encodable {
    public let email: String
    public let code: String

    public init(email: String, code: String) {
        self.email = email
        self.code = code
    }
}

public struct ResetPasswordRequestDTO: Encodable {
    public let email: String
    public let code: String
    public let newPassword: String
    public let confirmPassword: String

    public init(email: String, code: String, newPassword: String, confirmPassword: String) {
        self.email = email
        self.code = code
        self.newPassword = newPassword
        self.confirmPassword = confirmPassword
    }
}

public struct RefreshTokenRequestDTO: Encodable {
    public let accessToken: String
    public let refreshToken: String

    public init(accessToken: String, refreshToken: String) {
        self.accessToken = accessToken
        self.refreshToken = refreshToken
    }
}

// MARK: - Responses

public struct UserDTO: Codable {
    public let id: String
    public let fullName: String
    public let email: String
    public let phoneNumber: String?
    public let preferredLanguage: String?
    public let role: String?
}

public struct LoginResponseDTO: Decodable {
    public let user: UserDTO?
    public let token: String?
    public let refreshToken: String?
    public let isLockedOut: Bool?
    public let isBlocked: Bool?
}

public struct TokenResponseDTO: Decodable {
    public let accessToken: String
    public let refreshToken: String
}
