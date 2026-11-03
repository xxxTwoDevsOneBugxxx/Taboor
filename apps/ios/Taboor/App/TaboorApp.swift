import SwiftUI

enum AppScreen {
    case splash
    case onboarding
    case login
    case register
}

@main
struct TaboorApp: App {
    @State private var currentScreen: AppScreen = .splash

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

                case .login:
                    LoginView(
                        onLoginSuccess: {
                            // Can route to Home
                        },
                        onNavigateToRegister: {
                            withAnimation(.easeInOut(duration: 0.35)) {
                                currentScreen = .register
                            }
                        },
                        onForgotPassword: {
                            // Forgot password flow
                        }
                    )
                    .transition(.asymmetric(
                        insertion: .opacity.combined(with: .move(edge: .trailing)),
                        removal: .opacity.combined(with: .move(edge: .leading))
                    ))

                case .register:
                    RegisterView(
                        onRegisterSuccess: {
                            withAnimation(.easeInOut(duration: 0.35)) {
                                currentScreen = .login
                            }
                        },
                        onNavigateToLogin: {
                            withAnimation(.easeInOut(duration: 0.35)) {
                                currentScreen = .login
                            }
                        }
                    )
                    .transition(.asymmetric(
                        insertion: .opacity.combined(with: .move(edge: .leading)),
                        removal: .opacity.combined(with: .move(edge: .trailing))
                    ))
                }
            }
            .environment(\.layoutDirection, .rightToLeft)
        }
    }
}
