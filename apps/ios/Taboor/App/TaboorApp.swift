import SwiftUI
import AppIntents
import CoreSpotlight

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
    @Environment(\.scenePhase) private var scenePhase

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

        // Configure native iOS notifications & actions
        NotificationService.shared.configure()
        NotificationService.shared.requestAuthorization()

        // Register AppShortcuts with Siri
        TaboorShortcuts.updateAppShortcutParameters()

        // Configure transparent navigation bar & warm cream background to eliminate any safe area flashes
        let navBarAppearance = UINavigationBarAppearance()
        navBarAppearance.configureWithTransparentBackground()
        UINavigationBar.appearance().standardAppearance = navBarAppearance
        UINavigationBar.appearance().scrollEdgeAppearance = navBarAppearance
        UINavigationBar.appearance().compactAppearance = navBarAppearance
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
                    .transition(.opacity)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(AppColors.screenBackground.ignoresSafeArea())
            .ignoresSafeArea()
            .environment(\.layoutDirection, localization.layoutDirection)
            .preferredColorScheme(.light)
            .id(localization.currentLanguage)
            .onChange(of: scenePhase) { newPhase in
                QueueLiveActivityManager.shared.handleScenePhaseChange(
                    newPhase,
                    activeTicket: QueueDataService.shared.activeTicket
                )
            }
            .onOpenURL { url in
                if url.scheme == "taboor" {
                    if url.host == "ticket" || url.path.contains("ticket") {
                        QueueDataService.shared.openLiveTracking()
                    } else if url.host == "profile" || url.host == "settings" || url.path.contains("profile") {
                        QueueDataService.shared.requestedTab = .profile
                    } else if url.host == "history" || url.path.contains("history") {
                        QueueDataService.shared.requestedTab = .profile
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.35) {
                            QueueDataService.shared.showHistoryScreen = true
                        }
                    } else if url.host == "favorites" || url.path.contains("favorites") {
                        QueueDataService.shared.requestedTab = .profile
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.35) {
                            QueueDataService.shared.showFavoritesScreen = true
                        }
                    } else if url.host == "alerts-settings" || url.path.contains("alerts-settings") {
                        QueueDataService.shared.requestedTab = .profile
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.35) {
                            QueueDataService.shared.showAlertPreferencesScreen = true
                        }
                    } else if url.host == "place" {
                        let placeId = url.path.replacingOccurrences(of: "/", with: "").isEmpty ? (URLComponents(url: url, resolvingAgainstBaseURL: false)?.queryItems?.first(where: { $0.name == "id" })?.value ?? "place_2") : url.path.replacingOccurrences(of: "/", with: "")
                        QueueDataService.shared.selectPlaceFromSpotlight(id: placeId)
                    } else if url.host == "add-calendar" {
                        QueueDataService.shared.requestedTab = .queue
                        Task {
                            if let ticket = QueueDataService.shared.activeTicket {
                                _ = await AppleCalendarManager.shared.addAppointmentEvent(ticket: ticket, isArabic: LocalizationManager.shared.isArabic)
                            }
                        }
                    } else if url.host == "queue" || url.path.contains("queue") {
                        QueueDataService.shared.requestedTab = nil
                        DispatchQueue.main.async {
                            QueueDataService.shared.requestedTab = .queue
                            if let idxStr = URLComponents(url: url, resolvingAgainstBaseURL: false)?.queryItems?.first(where: { $0.name == "ticket" })?.value, let idx = Int(idxStr) {
                                QueueDataService.shared.selectedTicketIndex = idx
                            }
                        }
                    } else if url.host == "home" || url.path.contains("home") {
                        QueueDataService.shared.requestedTab = .home
                    } else if url.host == "services" || url.path.contains("services") {
                        QueueDataService.shared.requestedTab = .home
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.35) {
                            QueueDataService.shared.showAllServicesScreen = true
                        }
                    }
                }
            }
            .onContinueUserActivity(CSSearchableItemActionType) { userActivity in
                _ = SpotlightService.shared.handleSpotlightActivity(userActivity)
            }
        }
    }
}
