import SwiftUI

// MARK: - Password Strength Helper & Widget

enum PasswordStrengthLevel {
    case empty
    case weak
    case medium
    case strong
    case veryStrong

    var level: Int {
        switch self {
        case .empty: return 0
        case .weak: return 1
        case .medium: return 2
        case .strong: return 3
        case .veryStrong: return 4
        }
    }

    var color: Color {
        switch self {
        case .empty: return AppColors.gray200
        case .weak: return AppColors.accentRed
        case .medium: return AppColors.amber
        case .strong: return AppColors.teal
        case .veryStrong: return AppColors.indigo
        }
    }

    var label: String {
        switch self {
        case .empty: return ""
        case .weak: return "ضعيفة"
        case .medium: return "متوسطة"
        case .strong: return "قوية"
        case .veryStrong: return "قوية جداً"
        }
    }

    static func calculate(for password: String) -> PasswordStrengthLevel {
        if password.isEmpty { return .empty }
        var score = 0
        if password.count >= 8 { score += 1 }
        if password.range(of: "[A-Z]", options: .regularExpression) != nil { score += 1 }
        if password.range(of: "[0-9]", options: .regularExpression) != nil { score += 1 }
        if password.range(of: "[^A-Za-z0-9]", options: .regularExpression) != nil { score += 1 }

        if score <= 1 { return .weak }
        if score == 2 { return .medium }
        if score == 3 { return .strong }
        return .veryStrong
    }
}

struct PasswordStrengthMeterView: View {
    let password: String

    var body: some View {
        let strength = PasswordStrengthLevel.calculate(for: password)

        VStack(alignment: .leading, spacing: 6) {
            HStack(spacing: 6) {
                ForEach(0..<4, id: \.self) { index in
                    let isFilled = password.count > 0 && index < strength.level
                    Capsule()
                        .fill(isFilled ? strength.color : AppColors.gray200)
                        .frame(height: 6)
                        .animation(.easeOut(duration: 0.2), value: strength.level)
                }

                if !password.isEmpty {
                    Text(strength.label)
                        .font(.tajawal(12, .bold))
                        .foregroundColor(strength.color)
                        .padding(.horizontal, 4)
                        .transition(.opacity)
                }
            }
        }
    }
}

// MARK: - Password Match Indicator

struct PasswordMatchIndicatorView: View {
    let password: String
    let confirm: String

    var body: some View {
        if !confirm.isEmpty {
            let isMatch = password == confirm
            HStack(spacing: 6) {
                Image(systemName: isMatch ? "checkmark.circle.fill" : "exclamationmark.circle.fill")
                    .font(.system(size: 14))
                    .foregroundColor(isMatch ? AppColors.success : AppColors.accentRed)

                Text(isMatch ? "كلمتا المرور متطابقتين" : "كلمتا المرور غير متطابقتين")
                    .font(.tajawal(12, .bold))
                    .foregroundColor(isMatch ? AppColors.success : AppColors.accentRed)
            }
            .transition(.opacity)
        }
    }
}

// MARK: - Step Indicator (3 Capsules: 24x8 active, 8x8 inactive)

struct StepIndicatorView: View {
    let currentStep: Int // 0, 1, 2

    var body: some View {
        HStack(spacing: 6) {
            ForEach(0..<3, id: \.self) { index in
                let isActive = index == currentStep
                let isDone = index < currentStep

                Capsule()
                    .fill(isDone ? AppColors.teal : (isActive ? AppColors.amber : AppColors.gray300))
                    .frame(width: isActive ? 24 : 8, height: 8)
                    .animation(.easeInOut(duration: 0.25), value: currentStep)
            }
        }
    }
}

// MARK: - OTP 6-Digit Box Input (LTR direction)

struct OtpInputView: View {
    @Binding var otp: [String] // Array of 6 single-character strings
    var onCompleted: (() -> Void)? = nil

    @FocusState private var focusedField: Int?

    var body: some View {
        HStack(spacing: 8) {
            ForEach(0..<6, id: \.self) { index in
                TextField("", text: Binding(
                    get: { otp[index] },
                    set: { newVal in
                        if newVal.count > 1 {
                            otp[index] = String(newVal.suffix(1))
                        } else {
                            otp[index] = newVal
                        }

                        // Auto advance
                        if !newVal.isEmpty && index < 5 {
                            focusedField = index + 1
                        } else if newVal.isEmpty && index > 0 {
                            focusedField = index - 1
                        }

                        // Check completion
                        if otp.allSatisfy({ !$0.isEmpty }) {
                            onCompleted?()
                        }
                    }
                ))
                .keyboardType(.numberPad)
                .multilineTextAlignment(.center)
                .font(.inter(24, .bold))
                .foregroundColor(AppColors.deepTeal)
                .frame(maxWidth: .infinity)
                .frame(height: 60)
                .background(AppColors.paper)
                .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 14, style: .continuous)
                        .stroke(
                            focusedField == index ? AppColors.teal : AppColors.gray300,
                            lineWidth: focusedField == index ? 2 : 1.2
                        )
                )
                .focused($focusedField, equals: index)
            }
        }
        .environment(\.layoutDirection, .leftToRight) // OTP digits are always LTR
        .onAppear {
            focusedField = 0
        }
    }
}

// MARK: - Phone Number Input Field

struct PhoneInputField: View {
    let label: String
    let hint: String
    @Binding var phoneNumber: String
    var errorMessage: String? = nil

    @FocusState private var isFocused: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(label)
                .font(.tajawal(14, .bold))
                .foregroundColor(AppColors.deepTeal)

            HStack(spacing: 10) {
                // Country Code Pill (Egypt +20)
                HStack(spacing: 6) {
                    Text("🇪🇬")
                        .font(.system(size: 20))
                    Text("+20")
                        .font(.inter(14, .semiBold))
                        .foregroundColor(AppColors.deepTeal)
                }
                .padding(.horizontal, 10)
                .padding(.vertical, 8)
                .background(AppColors.gray100)
                .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))

                Rectangle()
                    .fill(AppColors.gray200)
                    .frame(width: 1, height: 26)

                // Phone Digits Input
                TextField("", text: $phoneNumber)
                    .keyboardType(.phonePad)
                    .font(.tajawal(15, .medium))
                    .foregroundColor(AppColors.deepTeal)
                    .focused($isFocused)
                    .overlay(
                        Group {
                            if phoneNumber.isEmpty {
                                HStack {
                                    Text(hint)
                                        .font(.tajawal(14, .regular))
                                        .foregroundColor(AppColors.gray400)
                                    Spacer()
                                }
                                .allowsHitTesting(false)
                            }
                        }
                    )
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 12)
            .background(AppColors.paper)
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .stroke(
                        errorMessage != nil ? AppColors.accentRed :
                        isFocused ? AppColors.teal : AppColors.gray200,
                        lineWidth: errorMessage != nil ? 1.8 : (isFocused ? 1.8 : 1.5)
                    )
            )

            if let errorMessage = errorMessage, !errorMessage.isEmpty {
                Text(errorMessage)
                    .font(.tajawal(12, .medium))
                    .foregroundColor(AppColors.accentRed)
                    .padding(.horizontal, 4)
            }
        }
    }
}
