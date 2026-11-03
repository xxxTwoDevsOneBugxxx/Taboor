import SwiftUI

// MARK: - Register Screen (3-Step Animated Wizard mirroring Flutter RegisterScreen)

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

    var body: some View {
        ZStack {
            // 1. Background Gradient
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
            // Back Button
            Button(action: goBack) {
                Image(systemName: "arrow.right")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(AppColors.deepTeal)
                    .padding(8)
            }

            Spacer()

            // Step Indicator (3 Capsules)
            StepIndicatorView(currentStep: currentStep)

            Spacer()

            // Invisible Spacer for balance
            Color.clear
                .frame(width: 36, height: 36)
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

            Text("أنشئ حسابك")
                .font(.tajawal(25, .extraBold))
                .foregroundColor(AppColors.deepTeal)
                .multilineTextAlignment(.center)

            Spacer().frame(height: 8)

            Text("ابدأ بالبريد الإلكتروني\nوهنبعث لك كود التحقق")
                .font(.tajawal(14, .regular))
                .foregroundColor(AppColors.gray600)
                .multilineTextAlignment(.center)
                .lineSpacing(4)

            Spacer().frame(height: 32)

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

            Spacer().frame(height: 24)

            AuthPrimaryButton(
                title: "إرسال الكود",
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
                Text("عندك حساب بالفعل؟")
                    .font(.tajawal(14, .regular))
                    .foregroundColor(AppColors.gray600)

                Button(action: onNavigateToLogin) {
                    Text("تسجيل الدخول")
                        .font(.tajawal(14, .bold))
                        .foregroundColor(AppColors.deepTeal)
                }
            }
        }
    }

    private func sendCode() {
        let trimmed = email.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.isEmpty {
            emailError = "اكتب إيميلك."
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

            // Large Circular Mail Icon (88x88 softTeal)
            ZStack {
                Circle()
                    .fill(AppColors.softTeal)
                    .frame(width: 88, height: 88)

                Image(systemName: "envelope.badge")
                    .font(.system(size: 38, weight: .medium))
                    .foregroundColor(AppColors.teal)
            }

            Spacer().frame(height: 26)

            Text("كود التحقق")
                .font(.tajawal(26, .extraBold))
                .foregroundColor(AppColors.deepTeal)
                .multilineTextAlignment(.center)

            Spacer().frame(height: 10)

            Text("أدخل الكود المكون من 6 أرقام\nأُرسل إلى \(email.isEmpty ? "بريدك الإلكتروني" : email)")
                .font(.tajawal(15, .regular))
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
                Text("لم يصلك الكود؟")
                    .font(.tajawal(15, .regular))
                    .foregroundColor(AppColors.gray500)

                Button(action: {
                    // Resend code logic
                }) {
                    Text("إعادة الإرسال")
                        .font(.tajawal(15, .bold))
                        .foregroundColor(AppColors.teal)
                }
            }

            Spacer().frame(height: 36)

            let isOtpComplete = otp.allSatisfy { !$0.isEmpty }
            AuthPrimaryButton(
                title: "تحقق",
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

            Text("كمل بياناتك")
                .font(.tajawal(25, .extraBold))
                .foregroundColor(AppColors.deepTeal)
                .multilineTextAlignment(.center)

            Spacer().frame(height: 8)

            Text("الاسم وهاتفك وكلمة المرور عشان تكمّل")
                .font(.tajawal(14, .regular))
                .foregroundColor(AppColors.gray600)
                .multilineTextAlignment(.center)

            Spacer().frame(height: 28)

            // Full Name Field
            AuthTextField(
                label: "الاسم الكامل",
                hint: "مثال: محمد أحمد",
                text: $fullName,
                leadingIcon: "person.text.rectangle",
                errorMessage: fullNameError,
                onTextChange: { _ in
                    if fullNameError != nil { fullNameError = nil }
                }
            )

            Spacer().frame(height: 18)

            // Egyptian Phone Field (+20)
            PhoneInputField(
                label: "رقم الموبايل",
                hint: "01xxxxxxxxx",
                phoneNumber: $phone,
                errorMessage: phoneError
            )

            Spacer().frame(height: 18)

            // Password Field
            AuthTextField(
                label: "كلمة المرور",
                hint: "••••••••••",
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
                label: "تأكيد كلمة المرور",
                hint: "••••••••••",
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
                title: "إنشاء الحساب",
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
