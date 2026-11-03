import SwiftUI

enum AppScreen {
    case splash
    case onboarding
    case login
    case register
}

enum AuthDestination: Hashable {
    case register
}

@main
struct TaboorApp: App {
    @State private var currentScreen: AppScreen = .splash
    @State private var authPath = NavigationPath()
    private let localization = LocalizationManager.shared

    init() {
        // Register Tajawal and Inter TTF fonts into CoreText
        FontRegistration.registerCustomFonts()
        LocalizationManager.shared.currentLanguage = .arabic
    }

    var body: some Scene {
        WindowGroup {
            ZStack {
                switch currentScreen {
                case .splash:
                    SplashScreenView {
                        withAnimation(.easeInOut(duration: 0.45)) {
                            currentScreen = .onboarding
                        }
                    }
                    .transition(.opacity)

                case .onboarding:
                    OnboardingView {
                        withAnimation(.easeInOut(duration: 0.4)) {
                            currentScreen = .login
                        }
                    }
                    .transition(.asymmetric(
                        insertion: .opacity,
                        removal: .opacity.combined(with: .move(edge: .leading))
                    ))

                case .login, .register:
                    NavigationStack(path: $authPath) {
                        LoginView(
                            onLoginSuccess: {
                                // Can route to Home
                            },
                            onNavigateToRegister: {
                                authPath.append(AuthDestination.register)
                            },
                            onForgotPassword: {
                                // Forgot password flow
                            }
                        )
                        .navigationDestination(for: AuthDestination.self) { destination in
                            switch destination {
                            case .register:
                                RegisterView(
                                    onRegisterSuccess: {
                                        if !authPath.isEmpty {
                                            authPath.removeLast()
                                        }
                                    },
                                    onNavigateToLogin: {
                                        if !authPath.isEmpty {
                                            authPath.removeLast()
                                        }
                                    }
                                )
                                .navigationBarBackButtonHidden(true)
                            }
                        }
                    }
                    .transition(.asymmetric(
                        insertion: .opacity.combined(with: .move(edge: .trailing)),
                        removal: .opacity.combined(with: .move(edge: .leading))
                    ))
                    .onAppear {
                        if currentScreen == .register && authPath.isEmpty {
                            authPath.append(AuthDestination.register)
                        }
                    }
                }
            }
            .animation(.easeInOut(duration: 0.35), value: currentScreen)
            .environment(\.layoutDirection, localization.layoutDirection)
        }
    }
}
