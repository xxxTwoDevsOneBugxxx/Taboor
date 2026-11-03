import SwiftUI

// MARK: - Register Screen (Adaptive Fonts, Full Bilingual Arabic/English, LTR/RTL)

struct RegisterView: View {
    var onRegisterSuccess: () -> Void = {}
    var onNavigateToLogin: () -> Void = {}

    @State private var currentStep: Int = 0

    // Step 0: Email
    @State private var email: String = ""
    @State private var emailError: String? = nil

    // Step 1: OTP
    @State private var otp: [String] = Array(repeating: "", count: 6)

    // Step 2: Details
    @State private var fullName: String = ""
    @State private var fullNameError: String? = nil
    @State private var phone: String = ""
    @State private var phoneError: String? = nil
    @State private var password: String = ""
    @State private var passwordError: String? = nil
    @State private var confirmPassword: String = ""
    @State private var confirmPasswordError: String? = nil
    @State private var isPasswordSecure: Bool = true
    @State private var isConfirmSecure: Bool = true

    @State private var isLoading: Bool = false

    private let localization = LocalizationManager.shared

    var body: some View {
        ZStack {
            // Background Gradient
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

            VStack(spacing: 0) {
                // Top Navigation Bar
                topBar

                // Content for Current Step (Animated)
                ScrollView(showsIndicators: false) {
                    VStack(spacing: 0) {
                        switch currentStep {
                        case 0:
                            emailStepView
                                .transition(.asymmetric(
                                    insertion: .opacity.combined(with: .move(edge: .trailing)),
                                    removal: .opacity.combined(with: .move(edge: .leading))
                                ))
                        case 1:
                            otpStepView
                                .transition(.asymmetric(
                                    insertion: .opacity.combined(with: .move(edge: .trailing)),
                                    removal: .opacity.combined(with: .move(edge: .leading))
                                ))
                        default:
                            detailsStepView
                                .transition(.asymmetric(
                                    insertion: .opacity.combined(with: .move(edge: .trailing)),
                                    removal: .opacity.combined(with: .move(edge: .leading))
                                ))
                        }
                    }
                    .padding(.horizontal, 24)
                    .padding(.top, 16)
                    .padding(.bottom, 32)
                }
            }
        }
    }

    // MARK: - Top Navigation Bar
    private var topBar: some View {
        HStack {
            // Glass Back Button
            GlassBackButton(action: goBack)

            Spacer()

            // Step Indicator
            StepIndicatorView(currentStep: currentStep)

            Spacer()

            // Language Toggle in Register Top Bar
            LanguageToggleButton()
        }
        .padding(.horizontal, 20)
        .padding(.top, 8)
        .padding(.bottom, 8)
    }

    private func goBack() {
        if currentStep > 0 {
            withAnimation(.easeInOut(duration: 0.35)) {
                currentStep -= 1
            }
        } else {
            onNavigateToLogin()
        }
    }

    // MARK: - Step 0: Email View
    private var emailStepView: some View {
        VStack(spacing: 0) {
            Spacer().frame(height: 10)

            TaboorLogoView(iconSize: 76, showWordmark: false)
                .frame(height: 76)

            Spacer().frame(height: 20)

            Circle()
                .fill(AppColors.amber)
                .frame(width: 8, height: 8)
                .shadow(color: AppColors.amber.opacity(0.6), radius: 10, x: 0, y: 0)

            Spacer().frame(height: 14)

            Text(localization.createYourAccount)
                .font(.app(25, .extraBold, isArabic: localization.isArabic))
                .foregroundColor(AppColors.deepTeal)
                .multilineTextAlignment(.center)

            Spacer().frame(height: 8)

            Text(localization.registerSubtitle)
                .font(.app(14, .regular, isArabic: localization.isArabic))
                .foregroundColor(AppColors.gray600)
                .multilineTextAlignment(.center)
                .lineSpacing(4)

            Spacer().frame(height: 32)

            AuthTextField(
                label: localization.emailLabel,
                hint: localization.emailHint,
                text: $email,
                keyboardType: .emailAddress,
                leadingIcon: "envelope",
                errorMessage: emailError,
                onTextChange: { _ in
                    if emailError != nil { emailError = nil }
                }
            )

            Spacer().frame(height: 24)

            AuthPrimaryButton(
                title: localization.sendCode,
                isLoading: isLoading,
                isEnabled: true
            ) {
                sendCode()
            }

            Spacer().frame(height: 20)

            AuthOrDivider()

            Spacer().frame(height: 20)

            HStack(spacing: 12) {
                SocialLoginButton(
                    title: "Google",
                    iconName: "G",
                    iconColor: Color(hex: 0xEA4335),
                    isSystemIcon: false
                ) {}

                SocialLoginButton(
                    title: "Facebook",
                    iconName: "f",
                    iconColor: Color(hex: 0x1877F2),
                    isSystemIcon: false
                ) {}
            }

            Spacer().frame(height: 24)

            HStack(spacing: 6) {
                Text(localization.alreadyHaveAccount)
                    .font(.app(14, .regular, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.gray600)

                Button(action: onNavigateToLogin) {
                    Text(localization.loginButton)
                        .font(.app(14, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.deepTeal)
                }
            }
        }
    }

    private func sendCode() {
        let trimmed = email.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.isEmpty {
            emailError = localization.emailRequired
            return
        }
        emailError = nil
        isLoading = true

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.8) {
            isLoading = false
            withAnimation(.easeInOut(duration: 0.35)) {
                currentStep = 1
            }
        }
    }

    // MARK: - Step 1: OTP View
    private var otpStepView: some View {
        VStack(spacing: 0) {
            Spacer().frame(height: 16)

            ZStack {
                Circle()
                    .fill(AppColors.softTeal)
                    .frame(width: 88, height: 88)

                Image(systemName: "envelope.badge")
                    .font(.system(size: 38, weight: .medium))
                    .foregroundColor(AppColors.teal)
            }

            Spacer().frame(height: 26)

            Text(localization.otpTitle)
                .font(.app(26, .extraBold, isArabic: localization.isArabic))
                .foregroundColor(AppColors.deepTeal)
                .multilineTextAlignment(.center)

            Spacer().frame(height: 10)

            Text(localization.otpDescription(email.isEmpty ? (localization.isArabic ? "بريدك الإلكتروني" : "your email") : email))
                .font(.app(15, .regular, isArabic: localization.isArabic))
                .foregroundColor(AppColors.gray600)
                .multilineTextAlignment(.center)
                .lineSpacing(4)

            Spacer().frame(height: 36)

            // 6-Digit OTP Boxes
            OtpInputView(otp: $otp) {
                verifyCode()
            }

            Spacer().frame(height: 32)

            // Resend action
            HStack(spacing: 6) {
                Text(localization.resendPrompt)
                    .font(.app(15, .regular, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.gray500)

                Button(action: {
                    // Resend trigger
                }) {
                    Text(localization.resend)
                        .font(.app(15, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.teal)
                }
            }

            Spacer().frame(height: 36)

            let isOtpComplete = otp.allSatisfy { !$0.isEmpty }
            AuthPrimaryButton(
                title: localization.verify,
                isLoading: isLoading,
                isEnabled: isOtpComplete
            ) {
                verifyCode()
            }
        }
    }

    private func verifyCode() {
        isLoading = true
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.8) {
            isLoading = false
            withAnimation(.easeInOut(duration: 0.35)) {
                currentStep = 2
            }
        }
    }

    // MARK: - Step 2: Details & Password View
    private var detailsStepView: some View {
        VStack(spacing: 0) {
            Spacer().frame(height: 10)

            Text(localization.completeDetails)
                .font(.app(25, .extraBold, isArabic: localization.isArabic))
                .foregroundColor(AppColors.deepTeal)
                .multilineTextAlignment(.center)

            Spacer().frame(height: 8)

            Text(localization.detailsSubtitle)
                .font(.app(14, .regular, isArabic: localization.isArabic))
                .foregroundColor(AppColors.gray600)
                .multilineTextAlignment(.center)

            Spacer().frame(height: 28)

            // Full Name Field
            AuthTextField(
                label: localization.fullNameLabel,
                hint: localization.fullNameHint,
                text: $fullName,
                leadingIcon: "person.text.rectangle",
                errorMessage: fullNameError,
                onTextChange: { _ in
                    if fullNameError != nil { fullNameError = nil }
                }
            )

            Spacer().frame(height: 18)

            // Phone Field
            PhoneInputField(
                label: localization.phoneLabel,
                hint: localization.phoneHint,
                phoneNumber: $phone,
                errorMessage: phoneError
            )

            Spacer().frame(height: 18)

            // Password Field
            AuthTextField(
                label: localization.passwordLabel,
                hint: localization.passwordHint,
                text: $password,
                isSecure: isPasswordSecure,
                leadingIcon: "lock",
                trailingIcon: isPasswordSecure ? "eye.slash" : "eye",
                trailingAction: {
                    isPasswordSecure.toggle()
                },
                errorMessage: passwordError,
                onTextChange: { _ in
                    if passwordError != nil { passwordError = nil }
                }
            )

            Spacer().frame(height: 8)

            // Password Strength Indicator
            PasswordStrengthMeterView(password: password)

            Spacer().frame(height: 18)

            // Confirm Password Field
            AuthTextField(
                label: localization.confirmPasswordLabel,
                hint: localization.passwordHint,
                text: $confirmPassword,
                isSecure: isConfirmSecure,
                leadingIcon: "lock",
                trailingIcon: isConfirmSecure ? "eye.slash" : "eye",
                trailingAction: {
                    isConfirmSecure.toggle()
                },
                errorMessage: confirmPasswordError,
                onTextChange: { _ in
                    if confirmPasswordError != nil { confirmPasswordError = nil }
                }
            )

            Spacer().frame(height: 8)

            // Match Indicator
            PasswordMatchIndicatorView(password: password, confirm: confirmPassword)

            Spacer().frame(height: 28)

            // Submit Button
            let canSubmit = isDetailsValid
            AuthPrimaryButton(
                title: localization.createAccountSubmit,
                isLoading: isLoading,
                isEnabled: canSubmit
            ) {
                createAccount()
            }
        }
    }

    private var isDetailsValid: Bool {
        return !fullName.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty &&
            !phone.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty &&
            password.count >= 8 &&
            password == confirmPassword
    }

    private func createAccount() {
        isLoading = true
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.2) {
            isLoading = false
            onRegisterSuccess()
        }
    }
}
