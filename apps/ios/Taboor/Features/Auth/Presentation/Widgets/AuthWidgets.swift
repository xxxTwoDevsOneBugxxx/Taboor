import SwiftUI

// MARK: - Auth Text Field (Exact Flutter _LoginField & _StepField)

struct AuthTextField: View {
    let label: String
    let hint: String
    @Binding var text: String
    var isSecure: Bool = false
    var keyboardType: UIKeyboardType = .default
    var leadingIcon: String? = nil
    var trailingIcon: String? = nil
    var trailingAction: (() -> Void)? = nil
    var errorMessage: String? = nil
    var onTextChange: ((String) -> Void)? = nil

    @FocusState private var isFocused: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Field Label
            Text(label)
                .font(.tajawal(14, .bold))
                .foregroundColor(AppColors.deepTeal)

            // Input Container
            HStack(spacing: 12) {
                // Leading Icon
                if let leadingIcon = leadingIcon {
                    Image(systemName: leadingIcon)
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(AppColors.teal)
                        .frame(width: 24)
                }

                // Input Field
                Group {
                    if isSecure {
                        SecureField("", text: $text)
                    } else {
                        TextField("", text: $text)
                            .keyboardType(keyboardType)
                            .autocapitalization(.none)
                            .disableAutocorrection(true)
                    }
                }
                .font(.tajawal(15, .medium))
                .foregroundColor(AppColors.deepTeal)
                .focused($isFocused)
                .overlay(
                    // Custom Placeholder (so it stays RTL-aligned and uses Tajawal)
                    Group {
                        if text.isEmpty {
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
                .onChange(of: text) { newValue in
                    onTextChange?(newValue)
                }

                // Trailing Icon / Action (e.g., eye toggle)
                if let trailingIcon = trailingIcon {
                    Button(action: {
                        trailingAction?()
                    }) {
                        Image(systemName: trailingIcon)
                            .font(.system(size: 18, weight: .medium))
                            .foregroundColor(AppColors.gray500)
                            .frame(width: 28, height: 28)
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
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

            // Error Text
            if let errorMessage = errorMessage, !errorMessage.isEmpty {
                Text(errorMessage)
                    .font(.tajawal(12, .medium))
                    .foregroundColor(AppColors.accentRed)
                    .padding(.horizontal, 4)
                    .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
    }
}

// MARK: - Auth Primary Button (Exact Flutter _LoginButton & _StepButton)

struct AuthPrimaryButton: View {
    let title: String
    var isLoading: Bool = false
    var isEnabled: Bool = true
    let action: () -> Void

    var body: some View {
        Button(action: {
            if isEnabled && !isLoading {
                action()
            }
        }) {
            ZStack {
                if isLoading {
                    HStack(spacing: 10) {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: AppColors.paper))
                        Text("جارٍ التحميل...")
                            .font(.tajawal(16, .bold))
                            .foregroundColor(AppColors.paper)
                    }
                } else {
                    Text(title)
                        .font(.tajawal(17, .bold))
                        .foregroundColor(isEnabled ? AppColors.paper : AppColors.teal)
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 56)
            .background(
                Group {
                    if isEnabled {
                        LinearGradient(
                            colors: [AppColors.deepTeal, AppColors.teal],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    } else {
                        AppColors.softTeal
                    }
                }
            )
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .shadow(
                color: AppColors.teal.opacity(isEnabled ? 0.35 : 0.08),
                radius: 20,
                x: 0,
                y: 10
            )
        }
        .disabled(!isEnabled || isLoading)
    }
}

// MARK: - Social Login Button (Google & Facebook)

struct SocialLoginButton: View {
    let title: String
    let iconName: String
    let iconColor: Color
    var isSystemIcon: Bool = true
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                if isSystemIcon {
                    Image(systemName: iconName)
                        .font(.system(size: 20, weight: .bold))
                        .foregroundColor(iconColor)
                } else {
                    Text(iconName)
                        .font(.system(size: 20, weight: .black))
                        .foregroundColor(iconColor)
                }

                Text(title)
                    .font(.tajawal(15, .bold))
                    .foregroundColor(AppColors.deepTeal)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 56)
            .background(AppColors.paper)
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .stroke(AppColors.gray200, lineWidth: 1.5)
            )
        }
    }
}

// MARK: - Or Divider

struct AuthOrDivider: View {
    var body: some View {
        HStack(spacing: 14) {
            Rectangle()
                .fill(AppColors.gray200)
                .frame(height: 1)

            Text("أو")
                .font(.tajawal(13, .medium))
                .foregroundColor(AppColors.gray500)

            Rectangle()
                .fill(AppColors.gray200)
                .frame(height: 1)
        }
    }
}

// MARK: - Language Toggle Button

struct LanguageToggleButton: View {
    @State private var isArabic = true

    var body: some View {
        Button(action: {
            withAnimation(.easeInOut(duration: 0.2)) {
                isArabic.toggle()
            }
        }) {
            HStack(spacing: 6) {
                Image(systemName: "globe")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundColor(AppColors.teal)

                Text(isArabic ? "EN" : "عربي")
                    .font(.tajawal(13, .bold))
                    .foregroundColor(AppColors.deepTeal)
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 8)
            .background(AppColors.paper)
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .stroke(AppColors.gray200, lineWidth: 1.2)
            )
        }
    }
}
