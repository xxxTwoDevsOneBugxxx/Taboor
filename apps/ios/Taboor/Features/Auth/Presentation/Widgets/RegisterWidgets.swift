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

    func label(isArabic: Bool) -> String {
        switch self {
        case .empty: return ""
        case .weak: return isArabic ? "ضعيفة" : "Weak"
        case .medium: return isArabic ? "متوسطة" : "Medium"
        case .strong: return isArabic ? "قوية" : "Strong"
        case .veryStrong: return isArabic ? "قوية جداً" : "Very strong"
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
    private let localization = LocalizationManager.shared

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
                    Text(strength.label(isArabic: localization.isArabic))
                        .font(.app(12, .bold, isArabic: localization.isArabic))
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
    private let localization = LocalizationManager.shared

    var body: some View {
        if !confirm.isEmpty {
            let isMatch = password == confirm
            HStack(spacing: 6) {
                Image(systemName: isMatch ? "checkmark.circle.fill" : "exclamationmark.circle.fill")
                    .font(.system(size: 14))
                    .foregroundColor(isMatch ? AppColors.success : AppColors.accentRed)

                Text(isMatch ? localization.passwordMatchOk : localization.passwordMismatch)
                    .font(.app(12, .bold, isArabic: localization.isArabic))
                    .foregroundColor(isMatch ? AppColors.success : AppColors.accentRed)
            }
            .transition(.opacity)
        }
    }
}

// MARK: - Step Indicator (3 Capsules)

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

// MARK: - OTP 6-Digit Box Input (High-Performance Single First-Responder with iOS AutoFill)

struct OtpInputView: View {
    @Binding var otp: [String]
    var onCompleted: (() -> Void)? = nil

    @State private var code: String = ""
    @FocusState private var isFocused: Bool

    var body: some View {
        ZStack {
            // 1. Single hidden TextField managing keyboard input & SMS/Mail one-time code
            TextField("", text: $code)
                .keyboardType(.numberPad)
                .textContentType(.oneTimeCode)
                .focused($isFocused)
                .opacity(0.01)
                .frame(width: 1, height: 1)
                .accentColor(.clear)
                .onChange(of: code) { _, newValue in
                    let digitsOnly = String(newValue.filter { $0.isNumber }.prefix(6))
                    if digitsOnly != newValue {
                        code = digitsOnly
                    }

                    // Sync to otp binding array
                    var newOtp = Array(repeating: "", count: 6)
                    for (i, char) in digitsOnly.enumerated() {
                        if i < 6 {
                            newOtp[i] = String(char)
                        }
                    }
                    otp = newOtp

                    if digitsOnly.count == 6 {
                        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                        onCompleted?()
                    }
                }

            // 2. Six visual display boxes with smooth focus and filling
            HStack(spacing: 8) {
                ForEach(0..<6, id: \.self) { index in
                    let digit = index < code.count ? String(Array(code)[index]) : ""
                    let isCurrent = isFocused && (index == code.count || (index == 5 && code.count == 6))

                    ZStack {
                        RoundedRectangle(cornerRadius: 14, style: .continuous)
                            .fill(AppColors.paper)

                        RoundedRectangle(cornerRadius: 14, style: .continuous)
                            .stroke(
                                isCurrent ? AppColors.teal : (digit.isEmpty ? AppColors.gray200 : AppColors.teal.opacity(0.7)),
                                lineWidth: isCurrent ? 2.0 : 1.2
                            )

                        if !digit.isEmpty {
                            Text(digit)
                                .font(.inter(24, .bold))
                                .foregroundColor(AppColors.deepTeal)
                        } else if isCurrent {
                            // Subtle pulse indicator for active box
                            RoundedRectangle(cornerRadius: 1)
                                .fill(AppColors.teal)
                                .frame(width: 2, height: 22)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 58)
                    .animation(.easeOut(duration: 0.15), value: isCurrent)
                }
            }
            .contentShape(Rectangle())
            .onTapGesture {
                isFocused = true
            }
        }
        .environment(\.layoutDirection, .leftToRight)
        .onAppear {
            code = otp.joined()
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                isFocused = true
            }
        }
        .onChange(of: otp) { _, newOtp in
            let joined = newOtp.joined()
            if joined != code {
                code = joined
            }
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
    @ObservedObject private var localization = LocalizationManager.shared

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(label)
                .font(.app(14, .bold, isArabic: localization.isArabic))
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

                // Phone Digits Input (Native 0ms Touch Responder & Custom Aligned Placeholder)
                ZStack(alignment: .leading) {
                    if phoneNumber.isEmpty {
                        Text(hint)
                            .font(.inter(14, .regular))
                            .foregroundColor(AppColors.gray400)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .allowsHitTesting(false)
                    }

                    TextField("", text: $phoneNumber)
                        .keyboardType(.phonePad)
                        .font(.inter(15, .medium))
                        .foregroundColor(AppColors.deepTeal)
                        .multilineTextAlignment(.leading)
                        .focused($isFocused)
                        .frame(maxWidth: .infinity, minHeight: 48, alignment: .leading)
                        .contentShape(Rectangle())
                        .onTapGesture {
                            isFocused = true
                        }
                }
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 4)
            .frame(minHeight: 52)
            .background(
                ZStack {
                    RoundedRectangle(cornerRadius: 16, style: .continuous)
                        .fill(errorMessage != nil ? AppColors.accentRed.opacity(0.05) : Color.white.opacity(0.92))

                    RoundedRectangle(cornerRadius: 16, style: .continuous)
                        .strokeBorder(
                            LinearGradient(
                                colors: [Color.white.opacity(0.9), Color.white.opacity(0.3)],
                                startPoint: .top,
                                endPoint: .bottom
                            ),
                            lineWidth: 1
                        )
                }
                .contentShape(Rectangle())
                .onTapGesture {
                    isFocused = true
                }
            )
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 16, style: .continuous)
                    .stroke(
                        errorMessage != nil ? AppColors.accentRed :
                        isFocused ? AppColors.teal : AppColors.gray200,
                        lineWidth: errorMessage != nil ? 1.8 : (isFocused ? 2.0 : 1.2)
                    )
            )
            .shadow(
                color: isFocused ? AppColors.teal.opacity(0.12) : Color.black.opacity(0.02),
                radius: isFocused ? 10 : 4,
                x: 0,
                y: isFocused ? 3 : 1
            )
            .animation(.spring(response: 0.22, dampingFraction: 0.78), value: isFocused)
            .animation(.spring(response: 0.22, dampingFraction: 0.78), value: errorMessage != nil)

            if let errorMessage = errorMessage, !errorMessage.isEmpty {
                Text(errorMessage)
                    .font(.app(12, .medium, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.accentRed)
                    .padding(.horizontal, 4)
            }
        }
        .environment(\.layoutDirection, localization.layoutDirection)
    }
}
