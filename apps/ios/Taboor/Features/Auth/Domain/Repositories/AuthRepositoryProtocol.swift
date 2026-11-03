import Foundation

// In Swift, interface is called "protocol" (like abstract class in Dart)
protocol AuthRepositoryProtocol {
    func login(email: String, password: String) async throws -> User
}
