import SwiftUI

// MARK: - Splash Screen (Exact Mirror from Flutter SplashScreen)

struct SplashScreenView: View {
    var onFinish: () -> Void

    @State private var logoScale: CGFloat = 0.0
    @State private var logoOpacity: Double = 0.0
    @State private var floatT: Double = 0.0

    var body: some View {
        ZStack {
            // 1. _SplashGradient: [softTeal.opacity(0.7), background(cream), paper]
            LinearGradient(
                stops: [
                    .init(color: AppColors.softTeal.opacity(0.7), location: 0.0),
                    .init(color: AppColors.background, location: 0.55),
                    .init(color: AppColors.paper, location: 1.0)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()

            // 2. _FloatingBackground: 8-second breathing ambient circles
            GeometryReader { geo in
                // Circle 1: softTeal (0.45), size 260x260, offset (-60 + t*80, -40 + t*30)
                Circle()
                    .fill(AppColors.softTeal.opacity(0.45))
                    .frame(width: 260, height: 260)
                    .position(x: 70 + floatT * 80, y: 90 + floatT * 30)

                // Circle 2: teal (0.1), size 190x190, offset (70 - t*60, 60 - t*40)
                Circle()
                    .fill(AppColors.teal.opacity(0.1))
                    .frame(width: 190, height: 190)
                    .position(x: geo.size.width - 20 - floatT * 60, y: 200 - floatT * 40)

                // Circle 3: amber (0.12), size 150x150, at bottom right (-40, 90)
                Circle()
                    .fill(AppColors.amber.opacity(0.12))
                    .frame(width: 150, height: 150)
                    .position(x: geo.size.width - 40, y: geo.size.height - 40)
            }
            .allowsHitTesting(false)

            // 3. Center Logo with easeOutBack Spring Scale & Fade
            TaboorLogoView(iconSize: 85, showWordmark: true)
                .scaleEffect(logoScale)
                .opacity(logoOpacity)
        }
        .onAppear {
            // 8s loop for floating background
            withAnimation(.easeInOut(duration: 8.0).repeatForever(autoreverses: true)) {
                floatT = 1.0
            }

            // Logo Spring Pop-in (easeOutBack equivalent in SwiftUI spring)
            withAnimation(.spring(response: 0.8, dampingFraction: 0.65)) {
                logoScale = 1.0
            }
            withAnimation(.easeIn(duration: 0.5)) {
                logoOpacity = 1.0
            }

            // 2.2-second timer to navigate to Onboarding
            DispatchQueue.main.asyncAfter(deadline: .now() + 2.2) {
                withAnimation(.easeInOut(duration: 0.4)) {
                    onFinish()
                }
            }
        }
    }
}

#Preview {
    SplashScreenView(onFinish: {})
}
