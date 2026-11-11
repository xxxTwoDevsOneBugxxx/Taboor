import Foundation

// MARK: - Auth Repository Implementation (Connected to Taboor Live Backend API)

public final class AuthRepository: AuthRepositoryProtocol {
    public static let shared = AuthRepository()

    private let apiClient: ApiClient
    private let keychain: KeychainManager
    private let userDefaults: UserDefaultsManager

    public init(
        apiClient: ApiClient = .shared,
        keychain: KeychainManager = .shared,
        userDefaults: UserDefaultsManager = .shared
    ) {
        self.apiClient = apiClient
        self.keychain = keychain
        self.userDefaults = userDefaults
    }

    public func login(email: String, password: String) async throws -> User {
        let payload = LoginRequestDTO(email: email, password: password)

        let response: LoginResponseDTO = try await apiClient.request(
            endpoint: AppConstants.API.Endpoints.login,
            method: .post,
            body: payload,
            requiresAuth: false
        )

        // Check if locked out or blocked
        if response.isLockedOut == true {
            throw NetworkError.custom(LocalizationManager.shared.isArabic ? "تم قفل الحساب مؤقتاً" : "Account is locked out")
        }
        if response.isBlocked == true {
            throw NetworkError.custom(LocalizationManager.shared.isArabic ? "تم حظر هذا الحساب" : "Account is blocked")
        }

        // Persist tokens securely in Keychain
        if let token = response.token {
            keychain.save(key: AppConstants.Storage.accessToken, value: token)
        }
        if let refresh = response.refreshToken {
            keychain.save(key: AppConstants.Storage.refreshToken, value: refresh)
        }

        let userDto = response.user
        let domainUser = User(
            id: userDto?.id ?? "",
            email: userDto?.email ?? email,
            fullName: userDto?.fullName ?? "",
            phoneNumber: userDto?.phoneNumber,
            role: userDto?.role,
            token: response.token
        )

        // Cache user metadata locally
        userDefaults.save(domainUser, forKey: AppConstants.Storage.savedUser)

        return domainUser
    }

    public func register(
        fullName: String,
        email: String,
        phoneNumber: String?,
        password: String,
        confirmPassword: String
    ) async throws -> User {
        let payload = RegisterRequestDTO(
            fullName: fullName,
            email: email,
            phoneNumber: phoneNumber,
            password: password,
            confirmPassword: confirmPassword
        )

        let userDto: UserDTO = try await apiClient.request(
            endpoint: AppConstants.API.Endpoints.register,
            method: .post,
            body: payload,
            requiresAuth: false
        )

        let domainUser = User(
            id: userDto.id,
            email: userDto.email,
            fullName: userDto.fullName,
            phoneNumber: userDto.phoneNumber,
            role: userDto.role
        )

        userDefaults.save(domainUser, forKey: AppConstants.Storage.savedUser)
        return domainUser
    }

    public func sendOtp(email: String) async throws {
        let payload = SendCodeRequestDTO(email: email)
        let _: EmptyPayload = try await apiClient.request(
            endpoint: AppConstants.API.Endpoints.sendCode,
            method: .post,
            body: payload,
            requiresAuth: false
        )
    }

    public func verifyEmail(email: String, code: String) async throws {
        let payload = VerifyCodeRequestDTO(email: email, code: code)
        let _: EmptyPayload = try await apiClient.request(
            endpoint: AppConstants.API.Endpoints.verifyEmail,
            method: .post,
            body: payload,
            requiresAuth: false
        )
    }

    public func forgotPassword(email: String) async throws {
        let payload = SendCodeRequestDTO(email: email)
        let _: EmptyPayload = try await apiClient.request(
            endpoint: AppConstants.API.Endpoints.forgotPassword,
            method: .post,
            body: payload,
            requiresAuth: false
        )
    }

    public func verifyResetCode(email: String, code: String) async throws {
        let payload = VerifyCodeRequestDTO(email: email, code: code)
        let _: EmptyPayload = try await apiClient.request(
            endpoint: AppConstants.API.Endpoints.verifyResetCode,
            method: .post,
            body: payload,
            requiresAuth: false
        )
    }

    public func resetPassword(email: String, code: String, newPassword: String, confirmPassword: String) async throws {
        let payload = ResetPasswordRequestDTO(
            email: email,
            code: code,
            newPassword: newPassword,
            confirmPassword: confirmPassword
        )
        let _: EmptyPayload = try await apiClient.request(
            endpoint: AppConstants.API.Endpoints.resetPassword,
            method: .post,
            body: payload,
            requiresAuth: false
        )
    }

    public func refreshToken() async throws {
        guard let access = keychain.get(key: AppConstants.Storage.accessToken),
              let refresh = keychain.get(key: AppConstants.Storage.refreshToken) else {
            throw NetworkError.unauthorized
        }

        let payload = RefreshTokenRequestDTO(accessToken: access, refreshToken: refresh)
        let tokenResponse: TokenResponseDTO = try await apiClient.request(
            endpoint: AppConstants.API.Endpoints.refreshToken,
            method: .post,
            body: payload,
            requiresAuth: false
        )

        keychain.save(key: AppConstants.Storage.accessToken, value: tokenResponse.accessToken)
        keychain.save(key: AppConstants.Storage.refreshToken, value: tokenResponse.refreshToken)
    }

    public func googleLogin(idToken: String) async throws -> User {
        let payload = GoogleMobileLoginRequestDTO(idToken: idToken)
        let response: ExternalAuthResponseDTO = try await apiClient.request(
            endpoint: AppConstants.API.Endpoints.googleMobile,
            method: .post,
            body: payload,
            requiresAuth: false
        )

        if let token = response.token {
            keychain.save(key: AppConstants.Storage.accessToken, value: token)
        }
        if let refresh = response.refreshToken {
            keychain.save(key: AppConstants.Storage.refreshToken, value: refresh)
        }

        let email = response.email ?? ""
        let domainUser = User(
            id: "",
            email: email,
            fullName: email.components(separatedBy: "@").first ?? "Google User",
            token: response.token
        )

        userDefaults.save(domainUser, forKey: AppConstants.Storage.savedUser)
        return domainUser
    }

    public func facebookLogin(accessToken: String) async throws -> User {
        let payload = FacebookMobileLoginRequestDTO(accessToken: accessToken)
        let response: ExternalAuthResponseDTO = try await apiClient.request(
            endpoint: AppConstants.API.Endpoints.facebookMobile,
            method: .post,
            body: payload,
            requiresAuth: false
        )

        if let token = response.token {
            keychain.save(key: AppConstants.Storage.accessToken, value: token)
        }
        if let refresh = response.refreshToken {
            keychain.save(key: AppConstants.Storage.refreshToken, value: refresh)
        }

        let email = response.email ?? ""
        let domainUser = User(
            id: "",
            email: email,
            fullName: email.components(separatedBy: "@").first ?? "Facebook User",
            token: response.token
        )

        userDefaults.save(domainUser, forKey: AppConstants.Storage.savedUser)
        return domainUser
    }

    public func logout() async throws {
        if let refresh = keychain.get(key: AppConstants.Storage.refreshToken) {
            let _: EmptyPayload? = try? await apiClient.request(
                endpoint: AppConstants.API.Endpoints.revokeToken,
                method: .post,
                body: ["refreshToken": refresh],
                requiresAuth: true
            )
        }
        keychain.clearAllAuthTokens()
        userDefaults.clearSession()
        UserDefaultsManager.shared.hasCompletedOnboarding = false

        await MainActor.run {
            QueueLiveActivityManager.shared.endLiveActivity()
            QueueDataService.shared.resetOnLogout()
        }
    }

    public func getSavedUser() -> User? {
        return userDefaults.get(User.self, forKey: AppConstants.Storage.savedUser)
    }

    public var isAuthenticated: Bool {
        return keychain.hasAccessToken
    }
}
