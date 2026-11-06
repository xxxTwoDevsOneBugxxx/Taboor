import SwiftUI

// MARK: - Minimal Native iOS Splash Screen

struct SplashScreenView: View {
    var onFinish: () -> Void

    @State private var logoScale: CGFloat = 0.92
    @State private var logoOpacity: Double = 0.0
    @State private var hasFinished = false

    var body: some View {
        ZStack {
            // Clean, solid native brand background supporting light & dark appearance
            AppColors.splashBackground
                .ignoresSafeArea()

            // Perfectly centered Taboor logo
            TaboorLogoView(iconSize: 84, showWordmark: true)
                .scaleEffect(logoScale)
                .opacity(logoOpacity)
                .accessibilityElement(children: .combine)
                .accessibilityLabel("Taboor")
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .ignoresSafeArea()
        .task {
            // Subtle, refined native SwiftUI entrance: gentle spring scale and smooth fade
            withAnimation(.spring(response: 0.55, dampingFraction: 0.88)) {
                logoScale = 1.0
                logoOpacity = 1.0
            }

            // Clean, minimal display duration (1.1s total) before smooth transition
            try? await Task.sleep(nanoseconds: 1_100_000_000)
            guard !Task.isCancelled else { return }
            finishSplash()
        }
    }

    private func finishSplash() {
        guard !hasFinished else { return }
        hasFinished = true
        onFinish()
    }
}

#Preview("Light Appearance") {
    SplashScreenView(onFinish: {})
        .preferredColorScheme(.light)
}

#Preview("Dark Appearance") {
    SplashScreenView(onFinish: {})
        .preferredColorScheme(.dark)
}
