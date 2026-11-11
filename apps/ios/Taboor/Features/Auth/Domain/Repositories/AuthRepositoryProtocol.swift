import Foundation

// MARK: - Auth Repository Protocol (Clean Architecture Port)

public protocol AuthRepositoryProtocol {
    func login(email: String, password: String) async throws -> User
    func register(fullName: String, email: String, phoneNumber: String?, password: String, confirmPassword: String) async throws -> User
    func sendOtp(email: String) async throws
    func verifyEmail(email: String, code: String) async throws
    func forgotPassword(email: String) async throws
    func verifyResetCode(email: String, code: String) async throws
    func resetPassword(email: String, code: String, newPassword: String, confirmPassword: String) async throws
    func refreshToken() async throws
    func googleLogin(idToken: String) async throws -> User
    func facebookLogin(accessToken: String) async throws -> User
    func logout() async throws
    func getSavedUser() -> User?
    var isAuthenticated: Bool { get }
}
