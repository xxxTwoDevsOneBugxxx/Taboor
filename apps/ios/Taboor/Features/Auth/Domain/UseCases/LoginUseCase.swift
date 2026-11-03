import Foundation

// Domain UseCase: Single Responsibility (like LoginUseCase in Flutter Clean Arch)
struct LoginUseCase {
    private let repository: AuthRepositoryProtocol

    init(repository: AuthRepositoryProtocol) {
        self.repository = repository
    }

    func execute(email: String, password: String) async throws -> User {
        guard !email.isEmpty, !password.isEmpty else {
            throw NSError(domain: "AuthError", code: 400, userInfo: [NSLocalizedDescriptionKey: "Email and password cannot be empty"])
        }
        return try await repository.login(email: email, password: password)
    }
}
