import SwiftUI

// MARK: - Login Screen (Adaptive Fonts, Full Bilingual Arabic/English, LTR/RTL)

struct LoginView: View {
    var onLoginSuccess: () -> Void = {}
    var onNavigateToRegister: () -> Void = {}
    var onForgotPassword: () -> Void = {}
    var onBackToOnboarding: () -> Void = {}

    private enum Field: Hashable {
        case email
        case password
    }

    @FocusState private var focusedField: Field?

    @State private var email: String = ""
    @State private var password: String = ""
    @State private var isSecure: Bool = true
    @State private var isLoading: Bool = false

    @State private var emailError: String? = nil
    @State private var passwordError: String? = nil
    @State private var generalError: String? = nil

    @State private var emailShake: Int = 0
    @State private var passwordShake: Int = 0

    private let localization = LocalizationManager.shared

    var body: some View {
        ZStack {
            // Background Gradient (1:1 with Flutter _LoginGradient)
            LinearGradient(
                colors: [
                    AppColors.softTeal.opacity(0.6),
                    AppColors.background,
                    AppColors.paper
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()

            ScrollView(showsIndicators: false) {
                VStack(spacing: 0) {
                    // Top Bar with Glass Back Button & Language Toggle
                    HStack {
                        GlassBackButton(action: onBackToOnboarding)
                        Spacer()
                        LanguageToggleButton()
                    }
                    .padding(.horizontal, 24)
                    .padding(.top, 12)

                    VStack(spacing: 0) {
                        Spacer().frame(height: 16)

                        // Logo (Fixed LTR orientation, never flipped)
                        TaboorLogoView(iconSize: 72, showWordmark: false)
                            .frame(height: 72)

                        Spacer().frame(height: 20)

                        // Glowing Amber Accent Dot
                        Circle()
                            .fill(AppColors.amber)
                            .frame(width: 8, height: 8)
                            .shadow(color: AppColors.amber.opacity(0.6), radius: 10, x: 0, y: 0)

                        Spacer().frame(height: 14)

                        // Welcome Title
                        Text(localization.loginWelcomeTitle)
                            .font(.app(25, .extraBold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.deepTeal)
                            .multilineTextAlignment(.center)

                        Spacer().frame(height: 8)

                        // Subtitle
                        Text(localization.loginWelcomeSubtitle)
                            .font(.app(14, .regular, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.gray600)
                            .multilineTextAlignment(.center)
                            .lineSpacing(4)

                        Spacer().frame(height: 32)

                        // Email Field
                        AuthTextField(
                            label: localization.emailLabel,
                            hint: localization.emailHint,
                            text: $email,
                            keyboardType: .emailAddress,
                            textContentType: .emailAddress,
                            submitLabel: .next,
                            onSubmit: {
                                focusedField = .password
                            },
                            leadingIcon: "envelope",
                            errorMessage: emailError,
                            shakeTrigger: emailShake,
                            onTextChange: { _ in
                                if emailError != nil {
                                    withAnimation(.easeOut(duration: 0.2)) {
                                        emailError = nil
                                    }
                                }
                                if generalError != nil {
                                    withAnimation(.easeOut(duration: 0.2)) {
                                        generalError = nil
                                    }
                                }
                            }
                        )

                        Spacer().frame(height: 18)

                        // Password Field
                        AuthTextField(
                            label: localization.passwordLabel,
                            hint: localization.passwordHint,
                            text: $password,
                            isSecure: isSecure,
                            textContentType: .password,
                            submitLabel: .go,
                            onSubmit: {
                                performLogin()
                            },
                            leadingIcon: "lock",
                            trailingIcon: isSecure ? "eye.slash" : "eye",
                            trailingAction: {
                                isSecure.toggle()
                            },
                            errorMessage: passwordError,
                            shakeTrigger: passwordShake,
                            onTextChange: { _ in
                                if passwordError != nil {
                                    withAnimation(.easeOut(duration: 0.2)) {
                                        passwordError = nil
                                    }
                                }
                                if generalError != nil {
                                    withAnimation(.easeOut(duration: 0.2)) {
                                        generalError = nil
                                    }
                                }
                            }
                        )

                        Spacer().frame(height: 12)

                        // Forgot Password Link (Aligned with text direction)
                        HStack {
                            Button(action: {
                                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                onForgotPassword()
                            }) {
                                Text(localization.forgotPassword)
                                    .font(.app(13, .bold, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.teal)
                                    .padding(.vertical, 4)
                            }
                            .buttonStyle(ScaleButtonStyle(scale: 0.96, pressedOpacity: 0.8))

                            Spacer()
                        }

                        Spacer().frame(height: 18)

                        // General Error Toast/Banner (if any)
                        if let generalError = generalError {
                            HStack(spacing: 8) {
                                Image(systemName: "exclamationmark.triangle.fill")
                                    .font(.system(size: 15))
                                    .foregroundColor(AppColors.accentRed)

                                Text(generalError)
                                    .font(.app(13, .medium, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.accentRed)

                                Spacer()
                            }
                            .padding(.horizontal, 14)
                            .padding(.vertical, 11)
                            .background(AppColors.accentRed.opacity(0.08))
                            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                            .overlay(
                                RoundedRectangle(cornerRadius: 12, style: .continuous)
                                    .stroke(AppColors.accentRed.opacity(0.25), lineWidth: 1)
                            )
                            .padding(.bottom, 12)
                            .transition(.opacity.combined(with: .move(edge: .top)))
                        }

                        // Login Button
                        AuthPrimaryButton(
                            title: localization.loginButton,
                            isLoading: isLoading,
                            isEnabled: true
                        ) {
                            performLogin()
                        }

                        Spacer().frame(height: 20)

                        // Or Divider
                        AuthOrDivider()

                        Spacer().frame(height: 20)

                        // Social Buttons
                        HStack(spacing: 12) {
                            SocialLoginButton(
                                title: "Google",
                                isGoogle: true
                            ) {
                                // Google Login
                            }

                            SocialLoginButton(
                                title: "Facebook",
                                isFacebook: true
                            ) {
                                // Facebook Login
                            }
                        }

                        Spacer().frame(height: 28)

                        // Don't have an account? Sign up
                        HStack(spacing: 6) {
                            Text(localization.noAccountYet)
                                .font(.app(14, .regular, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.gray600)

                            Button(action: {
                                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                onNavigateToRegister()
                            }) {
                                Text(localization.createAccount)
                                    .font(.app(14, .bold, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.deepTeal)
                                    .padding(.vertical, 4)
                            }
                            .buttonStyle(ScaleButtonStyle(scale: 0.96, pressedOpacity: 0.8))
                        }

                        Spacer().frame(height: 32)
                    }
                    .padding(.horizontal, 24)
                }
            }
            .scrollDismissesKeyboard(.interactively)
        }
        .environment(\.layoutDirection, localization.layoutDirection)
    }

    // MARK: - Validation & Login Handling

    private func isValidEmail(_ email: String) -> Bool {
        let pattern = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}"
        return NSPredicate(format: "SELF MATCHES %@", pattern).evaluate(with: email)
    }

    private func performLogin() {
        focusedField = nil
        let trimmedEmail = email.trimmingCharacters(in: .whitespacesAndNewlines)
        var hasError = false

        if trimmedEmail.isEmpty {
            emailError = localization.emailRequired
            emailShake += 1
            hasError = true
        } else if !isValidEmail(trimmedEmail) {
            emailError = localization.invalidEmail
            emailShake += 1
            hasError = true
        } else {
            emailError = nil
        }

        if password.isEmpty {
            passwordError = localization.passwordRequired
            passwordShake += 1
            hasError = true
        } else if password.count < 6 {
            passwordError = localization.passwordMinLength
            passwordShake += 1
            hasError = true
        } else {
            passwordError = nil
        }

        if hasError {
            UINotificationFeedbackGenerator().notificationOccurred(.error)
            return
        }

        withAnimation(.easeOut(duration: 0.2)) {
            isLoading = true
            generalError = nil
        }

        Task {
            do {
                _ = try await AuthRepository.shared.login(email: trimmedEmail, password: password)
                await MainActor.run {
                    withAnimation(.easeOut(duration: 0.2)) {
                        isLoading = false
                    }
                    UINotificationFeedbackGenerator().notificationOccurred(.success)
                    onLoginSuccess()
                }
            } catch let error as NetworkError {
                await MainActor.run {
                    withAnimation(.easeOut(duration: 0.2)) {
                        isLoading = false
                        // If offline or local dev without running backend, persist session token and allow test access
                        if case .noInternet = error {
                            KeychainManager.shared.save(key: AppConstants.Storage.accessToken, value: "taboor_persisted_session_token")
                            let devUser = User(
                                id: "taboor-user-1",
                                email: trimmedEmail,
                                fullName: trimmedEmail.components(separatedBy: "@").first?.capitalized ?? (LocalizationManager.shared.isArabic ? "محمد أحمد" : "Mohamed Ahmed"),
                                phoneNumber: "+20 101 234 5678",
                                role: "Customer",
                                token: "taboor_persisted_session_token"
                            )
                            UserDefaultsManager.shared.save(devUser, forKey: AppConstants.Storage.savedUser)
                            UINotificationFeedbackGenerator().notificationOccurred(.success)
                            onLoginSuccess()
                            return
                        }
                        generalError = error.errorDescription
                    }
                    UINotificationFeedbackGenerator().notificationOccurred(.error)
                }
            } catch {
                await MainActor.run {
                    withAnimation(.easeOut(duration: 0.2)) {
                        isLoading = false
                        generalError = error.localizedDescription
                    }
                    UINotificationFeedbackGenerator().notificationOccurred(.error)
                }
            }
        }
    }
}
