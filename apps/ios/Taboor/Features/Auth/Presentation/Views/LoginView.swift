import SwiftUI

// MARK: - Login Screen (Exact Flutter UI Mirror)

struct LoginView: View {
    var onLoginSuccess: () -> Void = {}
    var onNavigateToRegister: () -> Void = {}
    var onForgotPassword: () -> Void = {}

    @State private var email: String = ""
    @State private var password: String = ""
    @State private var isSecure: Bool = true
    @State private var isLoading: Bool = false
    @State private var emailError: String? = nil
    @State private var passwordError: String? = nil
    @State private var generalError: String? = nil

    var body: some View {
        ZStack {
            // 1. Background Gradient (Matches _LoginGradient in Flutter)
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

            // 2. Scrollable Content
            ScrollView(showsIndicators: false) {
                VStack(spacing: 0) {
                    // Top Bar Spacer & Language Toggle
                    HStack {
                        Spacer()
                        LanguageToggleButton()
                    }
                    .padding(.horizontal, 24)
                    .padding(.top, 12)

                    VStack(spacing: 0) {
                        Spacer().frame(height: 16)

                        // Logo (Squircle Vector Path)
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
                        Text("أهلًا بيك من جديد")
                            .font(.tajawal(25, .extraBold))
                            .foregroundColor(AppColors.deepTeal)
                            .multilineTextAlignment(.center)

                        Spacer().frame(height: 8)

                        // Subtitle
                        Text("سجّل دخولك واستكمل طلبك في الطابور")
                            .font(.tajawal(14, .regular))
                            .foregroundColor(AppColors.gray600)
                            .multilineTextAlignment(.center)

                        Spacer().frame(height: 32)

                        // Email Field
                        AuthTextField(
                            label: "البريد الإلكتروني",
                            hint: "you@example.com",
                            text: $email,
                            keyboardType: .emailAddress,
                            leadingIcon: "envelope",
                            errorMessage: emailError,
                            onTextChange: { _ in
                                if emailError != nil { emailError = nil }
                            }
                        )

                        Spacer().frame(height: 18)

                        // Password Field
                        AuthTextField(
                            label: "كلمة المرور",
                            hint: "••••••••••",
                            text: $password,
                            isSecure: isSecure,
                            leadingIcon: "lock",
                            trailingIcon: isSecure ? "eye.slash" : "eye",
                            trailingAction: {
                                isSecure.toggle()
                            },
                            errorMessage: passwordError,
                            onTextChange: { _ in
                                if passwordError != nil { passwordError = nil }
                            }
                        )

                        Spacer().frame(height: 12)

                        // Forgot Password Link
                        HStack {
                            Button(action: onForgotPassword) {
                                Text("نسيت كلمة المرور؟")
                                    .font(.tajawal(13, .bold))
                                    .foregroundColor(AppColors.teal)
                            }
                            Spacer()
                        }

                        Spacer().frame(height: 20)

                        // General Error Banner (if any)
                        if let generalError = generalError {
                            Text(generalError)
                                .font(.tajawal(13, .medium))
                                .foregroundColor(AppColors.accentRed)
                                .padding(.bottom, 8)
                        }

                        // Login Action Button
                        AuthPrimaryButton(
                            title: "تسجيل الدخول",
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
                                iconName: "G",
                                iconColor: Color(hex: 0xEA4335),
                                isSystemIcon: false
                            ) {
                                // Google Sign-in trigger
                            }

                            SocialLoginButton(
                                title: "Facebook",
                                iconName: "f",
                                iconColor: Color(hex: 0x1877F2),
                                isSystemIcon: false
                            ) {
                                // Facebook Sign-in trigger
                            }
                        }

                        Spacer().frame(height: 28)

                        // Don't have an account? Sign up
                        HStack(spacing: 6) {
                            Text("مش عندك حساب؟")
                                .font(.tajawal(14, .regular))
                                .foregroundColor(AppColors.gray600)

                            Button(action: onNavigateToRegister) {
                                Text("أنشئ حسابًا")
                                    .font(.tajawal(14, .bold))
                                    .foregroundColor(AppColors.deepTeal)
                            }
                        }

                        Spacer().frame(height: 32)
                    }
                    .padding(.horizontal, 24)
                }
            }
        }
    }

    private func performLogin() {
        let trimmedEmail = email.trimmingCharacters(in: .whitespacesAndNewlines)
        var hasError = false

        if trimmedEmail.isEmpty {
            emailError = "اكتب إيميلك."
            hasError = true
        } else {
            emailError = nil
        }

        if password.isEmpty {
            passwordError = "اكتب كلمة المرور."
            hasError = true
        } else {
            passwordError = nil
        }

        if hasError { return }

        // Start loading
        isLoading = true
        generalError = nil

        // Simulate login success or hook to API
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            isLoading = false
            onLoginSuccess()
        }
    }
}
