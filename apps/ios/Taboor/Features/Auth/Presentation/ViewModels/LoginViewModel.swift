import Foundation
import Observation

// In modern Swift (iOS 17+), @Observable is used (like Cubit/ChangeNotifier in Flutter)
@Observable
final class LoginViewModel {
    var email: String = ""
    var password: String = ""
    var isLoading: Bool = false
    var errorMessage: String? = nil
    var currentUser: User? = nil

    private let loginUseCase: LoginUseCase

    init(loginUseCase: LoginUseCase) {
        self.loginUseCase = loginUseCase
    }

    @MainActor
    func login() async {
        isLoading = true
        errorMessage = nil

        do {
            currentUser = try await loginUseCase.execute(email: email, password: password)
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }
}
