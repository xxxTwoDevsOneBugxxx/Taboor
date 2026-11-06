import SwiftUI

enum AppScreen {
    case splash
    case onboarding
    case login
    case register
    case home
}

enum AuthDestination: Hashable {
    case register
    case forgotPassword
}

@main
struct TaboorApp: App {
    @State private var currentScreen: AppScreen = .splash
    @State private var authPath = NavigationPath()
    @AppStorage("app_appearance_mode") private var appearanceMode: String = "system"
    @ObservedObject private var localization = LocalizationManager.shared

    private var colorSchemeOverride: ColorScheme? {
        switch appearanceMode {
        case "light": return .light
        case "dark": return .dark
        default: return nil
        }
    }

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
                            // 1. Auto-login if valid session/token exists
                            if AuthRepository.shared.isAuthenticated {
                                currentScreen = .home
                            } else if UserDefaultsManager.shared.hasCompletedOnboarding {
                                currentScreen = .login
                            } else {
                                currentScreen = .onboarding
                            }
                        }
                    }
                    .transition(.opacity)

                case .onboarding:
                    OnboardingView {
                        UserDefaultsManager.shared.hasCompletedOnboarding = true
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
                                withAnimation(.spring(response: 0.45, dampingFraction: 0.8)) {
                                    currentScreen = .home
                                }
                            },
                            onNavigateToRegister: {
                                authPath.append(AuthDestination.register)
                            },
                            onForgotPassword: {
                                authPath.append(AuthDestination.forgotPassword)
                            },
                            onBackToOnboarding: {
                                withAnimation(.easeInOut(duration: 0.35)) {
                                    currentScreen = .onboarding
                                }
                            }
                        )
                        .navigationDestination(for: AuthDestination.self) { destination in
                            switch destination {
                            case .register:
                                RegisterView(
                                    onRegisterSuccess: {
                                        withAnimation(.spring(response: 0.45, dampingFraction: 0.8)) {
                                            currentScreen = .home
                                        }
                                    },
                                    onNavigateToLogin: {
                                        if !authPath.isEmpty {
                                            authPath.removeLast()
                                        }
                                    }
                                )
                                .navigationBarBackButtonHidden(true)

                            case .forgotPassword:
                                ForgotPasswordView(
                                    onResetSuccess: {
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

                case .home:
                    MainTabView(
                        onLogout: {
                            Task {
                                try? await AuthRepository.shared.logout()
                            }
                            UserDefaultsManager.shared.hasCompletedOnboarding = false
                            withAnimation(.spring(response: 0.45, dampingFraction: 0.8)) {
                                currentScreen = .onboarding
                                authPath = NavigationPath()
                            }
                        }
                    )
                    .transition(.asymmetric(
                        insertion: .opacity.combined(with: .scale(scale: 0.98)),
                        removal: .opacity.combined(with: .scale(scale: 0.98))
                    ))
                }
            }
            .animation(.easeInOut(duration: 0.35), value: currentScreen)
            .environment(\.layoutDirection, localization.layoutDirection)
            .preferredColorScheme(colorSchemeOverride)
            .id(localization.currentLanguage)
        }
    }
}
