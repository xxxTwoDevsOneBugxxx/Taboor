import SwiftUI

// MARK: - iOS Native Interactive Button Style

struct ScaleButtonStyle: ButtonStyle {
    var scale: CGFloat = 0.98
    var pressedOpacity: CGFloat = 0.92

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .scaleEffect(configuration.isPressed ? scale : 1.0)
            .opacity(configuration.isPressed ? pressedOpacity : 1.0)
            .animation(.easeOut(duration: 0.15), value: configuration.isPressed)
    }
}

// MARK: - Shake Animation Effect for Validation Errors

struct ShakeEffect: GeometryEffect {
    var amount: CGFloat = 8
    var shakesPerUnit: CGFloat = 3
    var animatableData: CGFloat

    func effectValue(size: CGSize) -> ProjectionTransform {
        ProjectionTransform(CGAffineTransform(translationX:
            amount * sin(animatableData * .pi * shakesPerUnit),
            y: 0))
    }
}

// MARK: - Google & Facebook Official Brand Icons

struct GoogleLogoIcon: View {
    var size: CGFloat = 20

    var body: some View {
        Canvas { context, canvasSize in
            let w = canvasSize.width
            let h = canvasSize.height
            let sx = w / 24.0
            let sy = h / 24.0

            func p(_ x: Double, _ y: Double) -> CGPoint {
                CGPoint(x: x * sx, y: y * sy)
            }

            // 1. Blue (#4285F4): Crossbar and right cap
            var bluePath = Path()
            bluePath.move(to: p(22.56, 12.25))
            bluePath.addCurve(to: p(22.36, 10.0), control1: p(22.56, 11.47), control2: p(22.49, 10.72))
            bluePath.addLine(to: p(12.0, 10.0))
            bluePath.addLine(to: p(12.0, 14.26))
            bluePath.addLine(to: p(17.92, 14.26))
            bluePath.addCurve(to: p(15.71, 17.57), control1: p(17.66, 15.63), control2: p(16.88, 16.79))
            bluePath.addLine(to: p(15.71, 20.34))
            bluePath.addLine(to: p(19.28, 20.34))
            bluePath.addCurve(to: p(22.56, 12.25), control1: p(21.36, 18.42), control2: p(22.56, 15.6))
            bluePath.closeSubpath()
            context.fill(bluePath, with: .color(Color(hex: 0x4285F4)))

            // 2. Green (#34A853): Bottom curve
            var greenPath = Path()
            greenPath.move(to: p(12.0, 23.0))
            greenPath.addCurve(to: p(19.28, 20.34), control1: p(14.97, 23.0), control2: p(17.46, 22.02))
            greenPath.addLine(to: p(15.71, 17.57))
            greenPath.addCurve(to: p(12.0, 18.63), control1: p(14.73, 18.23), control2: p(13.48, 18.63))
            greenPath.addCurve(to: p(5.84, 14.09), control1: p(9.14, 18.63), control2: p(6.71, 16.7))
            greenPath.addLine(to: p(2.18, 14.09))
            greenPath.addLine(to: p(2.18, 16.94))
            greenPath.addCurve(to: p(12.0, 23.0), control1: p(3.99, 20.53), control2: p(7.7, 23.0))
            greenPath.closeSubpath()
            context.fill(greenPath, with: .color(Color(hex: 0x34A853)))

            // 3. Yellow (#FBBC05): Left curve
            var yellowPath = Path()
            yellowPath.move(to: p(5.84, 14.09))
            yellowPath.addCurve(to: p(5.49, 12.0), control1: p(5.62, 13.43), control2: p(5.49, 12.73))
            yellowPath.addCurve(to: p(5.84, 9.91), control1: p(5.49, 11.27), control2: p(5.62, 10.57))
            yellowPath.addLine(to: p(5.84, 7.06))
            yellowPath.addLine(to: p(2.18, 7.06))
            yellowPath.addCurve(to: p(1.0, 12.0), control1: p(1.43, 8.55), control2: p(1.0, 10.22))
            yellowPath.addCurve(to: p(2.18, 16.94), control1: p(1.0, 13.78), control2: p(1.43, 15.45))
            yellowPath.addLine(to: p(5.84, 14.09))
            yellowPath.closeSubpath()
            context.fill(yellowPath, with: .color(Color(hex: 0xFBBC05)))

            // 4. Red (#EA4335): Top curve
            var redPath = Path()
            redPath.move(to: p(12.0, 5.38))
            redPath.addCurve(to: p(16.21, 7.02), control1: p(13.62, 5.38), control2: p(15.06, 5.94))
            redPath.addLine(to: p(19.36, 3.87))
            redPath.addCurve(to: p(12.0, 1.0), control1: p(17.45, 2.09), control2: p(14.97, 1.0))
            redPath.addCurve(to: p(2.18, 7.06), control1: p(7.7, 1.0), control2: p(3.99, 3.47))
            redPath.addLine(to: p(5.84, 9.91))
            redPath.addCurve(to: p(12.0, 5.38), control1: p(6.71, 7.3), control2: p(9.14, 5.38))
            redPath.closeSubpath()
            context.fill(redPath, with: .color(Color(hex: 0xEA4335)))
        }
        .frame(width: size, height: size)
    }
}

struct FacebookLogoIcon: View {
    var size: CGFloat = 20

    var body: some View {
        Canvas { context, canvasSize in
            let w = canvasSize.width
            let h = canvasSize.height
            let sx = w / 24.0
            let sy = h / 24.0

            func p(_ x: Double, _ y: Double) -> CGPoint {
                CGPoint(x: x * sx, y: y * sy)
            }

            // 1. Blue Circle Background (#1877F2)
            let circleRect = CGRect(x: 0, y: 0, width: w, height: h)
            let circlePath = Path(ellipseIn: circleRect)
            context.fill(circlePath, with: .color(Color(hex: 0x1877F2)))

            // 2. Official White "f" Glyph
            var fPath = Path()
            fPath.move(to: p(16.671, 15.469))
            fPath.addLine(to: p(17.203, 12.0))
            fPath.addLine(to: p(13.875, 12.0))
            fPath.addLine(to: p(13.875, 9.75))
            fPath.addCurve(to: p(15.831, 7.876), control1: p(13.875, 8.801), control2: p(14.34, 7.876))
            fPath.addLine(to: p(17.344, 7.876))
            fPath.addLine(to: p(17.344, 4.922))
            fPath.addCurve(to: p(14.658, 4.687), control1: p(17.344, 4.922), control2: p(15.97, 4.687))
            fPath.addCurve(to: p(10.125, 9.356), control1: p(11.917, 4.687), control2: p(10.125, 6.349))
            fPath.addLine(to: p(10.125, 12.0))
            fPath.addLine(to: p(7.078, 12.0))
            fPath.addLine(to: p(7.078, 15.469))
            fPath.addLine(to: p(10.125, 15.469))
            fPath.addLine(to: p(10.125, 23.854))
            fPath.addLine(to: p(13.875, 23.854))
            fPath.addLine(to: p(13.875, 15.469))
            fPath.addLine(to: p(16.671, 15.469))
            fPath.closeSubpath()

            context.clip(to: circlePath)
            context.fill(fPath, with: .color(.white))
        }
        .frame(width: size, height: size)
    }
}

// MARK: - iOS Native Glass Back Button (Official Apple GlassButtonStyle)

struct GlassBackButton: View {
    let action: () -> Void
    private let localization = LocalizationManager.shared

    var body: some View {
        Button {
            UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            action()
        } label: {
            Image(systemName: localization.isArabic ? "chevron.right" : "chevron.left")
                .font(.system(size: 18, weight: .semibold))
                .foregroundColor(AppColors.deepTeal)
                .frame(width: 24, height: 24)
        }
        .buttonStyle(.glass)
        .buttonBorderShape(.circle)
    }
}

// MARK: - Auth Text Field (Supporting Adaptive Font, Error States & Native iOS Feedback)

struct AuthTextField: View {
    let label: String
    let hint: String
    @Binding var text: String
    var isSecure: Bool = false
    var keyboardType: UIKeyboardType = .default
    var textContentType: UITextContentType? = nil
    var submitLabel: SubmitLabel = .done
    var onSubmit: (() -> Void)? = nil
    var leadingIcon: String? = nil
    var trailingIcon: String? = nil
    var trailingAction: (() -> Void)? = nil
    var errorMessage: String? = nil
    var shakeTrigger: Int = 0
    var onTextChange: ((String) -> Void)? = nil

    @FocusState private var isFocused: Bool
    private let localization = LocalizationManager.shared

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Field Label
            Text(label)
                .font(.app(14, .bold, isArabic: localization.isArabic))
                .foregroundColor(errorMessage != nil ? AppColors.accentRed : AppColors.deepTeal)

            // Input Container
            HStack(spacing: 12) {
                // Leading Icon
                if let leadingIcon = leadingIcon {
                    Image(systemName: leadingIcon)
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(
                            errorMessage != nil ? AppColors.accentRed :
                            (isFocused ? AppColors.teal : AppColors.gray500)
                        )
                        .frame(width: 24)
                }

                // Input Field (Native 0ms Touch Responder & Native Placeholder)
                Group {
                    if isSecure {
                        SecureField(
                            "",
                            text: $text,
                            prompt: Text(hint)
                                .font(.app(14, .regular, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.gray400)
                        )
                        .textContentType(textContentType)
                        .submitLabel(submitLabel)
                        .onSubmit { onSubmit?() }
                    } else {
                        TextField(
                            "",
                            text: $text,
                            prompt: Text(hint)
                                .font(.app(14, .regular, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.gray400)
                        )
                        .keyboardType(keyboardType)
                        .textContentType(textContentType)
                        .submitLabel(submitLabel)
                        .onSubmit { onSubmit?() }
                        .autocapitalization(.none)
                        .disableAutocorrection(true)
                    }
                }
                .font(.app(15, .medium, isArabic: localization.isArabic))
                .foregroundColor(AppColors.deepTeal)
                .focused($isFocused)
                .frame(maxWidth: .infinity, minHeight: 24, alignment: .leading)
                .onChange(of: text) { _, newValue in
                    onTextChange?(newValue)
                }

                // Clear Button (iOS standard quick clear)
                if !isSecure && !text.isEmpty && isFocused {
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        text = ""
                        onTextChange?("")
                    }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 16, weight: .medium))
                            .foregroundColor(AppColors.gray400)
                            .frame(width: 24, height: 24)
                    }
                    .buttonStyle(.plain)
                    .transition(.scale.combined(with: .opacity))
                }

                // Trailing Icon (e.g. eye)
                if let trailingIcon = trailingIcon {
                    Button(action: { trailingAction?() }) {
                        Image(systemName: trailingIcon)
                            .font(.system(size: 18, weight: .medium))
                            .foregroundColor(isFocused ? AppColors.teal : AppColors.gray500)
                            .frame(width: 28, height: 28)
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(
                ZStack {
                    RoundedRectangle(cornerRadius: 16, style: .continuous)
                        .fill(errorMessage != nil ? AppColors.accentRed.opacity(0.05) : Color.white.opacity(0.92))

                    // Apple Glass subtle specular gradient stroke
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
            .modifier(ShakeEffect(animatableData: CGFloat(shakeTrigger)))

            // Error Text with Animated Warning Icon
            if let errorMessage = errorMessage, !errorMessage.isEmpty {
                HStack(spacing: 5) {
                    Image(systemName: "exclamationmark.circle.fill")
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(AppColors.accentRed)

                    Text(errorMessage)
                        .font(.app(12, .medium, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.accentRed)

                    Spacer()
                }
                .padding(.horizontal, 4)
                .transition(.opacity.combined(with: .move(edge: .top)))
            }
        }
    }
}

// MARK: - Auth Primary Button (Touch Scale & Haptics)

struct AuthPrimaryButton: View {
    let title: String
    var isLoading: Bool = false
    var isEnabled: Bool = true
    let action: () -> Void

    private let localization = LocalizationManager.shared

    var body: some View {
        Button(action: {
            if isEnabled && !isLoading {
                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                action()
            }
        }) {
            ZStack {
                if isLoading {
                    HStack(spacing: 10) {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: AppColors.paper))
                        Text(localization.loggingInText)
                            .font(.app(16, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.paper)
                    }
                } else {
                    Text(title)
                        .font(.app(17, .bold, isArabic: localization.isArabic))
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
                radius: 16,
                x: 0,
                y: 8
            )
        }
        .buttonStyle(ScaleButtonStyle())
        .disabled(!isEnabled || isLoading)
    }
}

// MARK: - Social Login Button (Google / Facebook / Custom)

struct SocialLoginButton: View {
    let title: String
    var isGoogle: Bool = false
    var isFacebook: Bool = false
    var iconName: String? = nil
    var iconColor: Color = AppColors.teal
    var isSystemIcon: Bool = true
    let action: () -> Void

    private let localization = LocalizationManager.shared

    var body: some View {
        Button(action: {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            action()
        }) {
            HStack(spacing: 8) {
                if isGoogle || iconName == "G" {
                    GoogleLogoIcon(size: 20)
                } else if isFacebook || iconName == "f" {
                    FacebookLogoIcon(size: 20)
                } else if let iconName = iconName {
                    Image(systemName: iconName)
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(iconColor)
                }

                Text(title)
                    .font(.app(15, .bold, isArabic: localization.isArabic))
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
        .buttonStyle(ScaleButtonStyle())
    }
}

// MARK: - Or Divider

struct AuthOrDivider: View {
    private let localization = LocalizationManager.shared

    var body: some View {
        HStack(spacing: 14) {
            Rectangle()
                .fill(AppColors.gray200)
                .frame(height: 1)

            Text(localization.orText)
                .font(.app(13, .medium, isArabic: localization.isArabic))
                .foregroundColor(AppColors.gray500)

            Rectangle()
                .fill(AppColors.gray200)
                .frame(height: 1)
        }
    }
}

// MARK: - Language Toggle Button (Switches App Locale Live)

struct LanguageToggleButton: View {
    private let localization = LocalizationManager.shared

    var body: some View {
        Button(action: {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            withAnimation(.easeInOut(duration: 0.25)) {
                localization.toggleLanguage()
            }
        }) {
            HStack(spacing: 6) {
                Image(systemName: "globe")
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(AppColors.teal)

                Text(localization.isArabic ? "EN" : "عربي")
                    .font(.inter(13, .bold))
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
            .shadow(color: AppColors.deepTeal.opacity(0.04), radius: 6, x: 0, y: 3)
        }
        .buttonStyle(ScaleButtonStyle())
    }
}
