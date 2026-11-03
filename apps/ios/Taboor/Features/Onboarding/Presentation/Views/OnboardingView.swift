import SwiftUI
import Combine

// MARK: - Onboarding Screen (Exact Pixel & Animation Math from Flutter)

struct OnboardingView: View {
    var onFinish: () -> Void = {}

    @State private var currentPage = 0
    @State private var animT: Double = 0.0

    // Auto-advance timer (moves to next tab automatically like in Flutter)
    @State private var autoTimer = Timer.publish(every: 3.5, on: .main, in: .common).autoconnect()
    @State private var isUserInteracting = false

    private let pageCount = 3

    var body: some View {
        ZStack {
            // 1. Background with 3 Ambient Circles (Exact Flutter _OnboardingBackground)
            OnboardingBackground()
                .ignoresSafeArea()

            VStack(spacing: 0) {
                // Top Bar Spacer (height: 44 in Flutter)
                Color.clear
                    .frame(height: 44)

                // Page Carousel (Auto-advances & Swipeable)
                TabView(selection: $currentPage) {
                    // Page 1: Ticket
                    OnboardingPageContent(
                        isActive: currentPage == 0,
                        title: "استنى وأنت في بيتك",
                        subtitle: "سجّل مكانك من أي مكان\nواحصل على رقمك الفوري مجاناً"
                    ) {
                        TicketIllustration(t: animT)
                    }
                    .tag(0)

                    // Page 2: Live Tracking
                    OnboardingPageContent(
                        isActive: currentPage == 1,
                        title: "دورك.. لحظة بلحظة",
                        subtitle: "تابع ترتيبك في الطابور مباشر\nمع عداد واضح ومؤشر تقدم"
                    ) {
                        LiveTrackingIllustration(t: animT)
                    }
                    .tag(1)

                    // Page 3: Smart Alert
                    OnboardingPageContent(
                        isActive: currentPage == 2,
                        title: "هننبهك قبل ما يجي دورك",
                        subtitle: "اشعارات ذكية تقولك امتى تخرج\n\"وقت الخروج - قدامك اتنين بس!\""
                    ) {
                        AlertIllustration(t: animT)
                    }
                    .tag(2)
                }
                .tabViewStyle(.page(indexDisplayMode: .never))

                // Dots Indicator
                dotsIndicator
                    .padding(.bottom, 20)

                // Bottom Action Button
                bottomActionButton
                    .padding(.horizontal, 24)
                    .padding(.bottom, 24)
            }
        }
        .onAppear {
            // Continuous 2-second repeat reverse animation loop
            withAnimation(.easeInOut(duration: 2.0).repeatForever(autoreverses: true)) {
                animT = 1.0
            }
        }
        // Auto-scroll between tabs
        .onReceive(autoTimer) { _ in
            if !isUserInteracting {
                withAnimation(.easeInOut(duration: 0.45)) {
                    currentPage = (currentPage + 1) % pageCount
                }
            }
        }
    }

    // MARK: - Dots Indicator (Exact Flutter: 28 x 8 active, 8 x 8 inactive)
    private var dotsIndicator: some View {
        HStack(spacing: 8) {
            ForEach(0..<pageCount, id: \.self) { index in
                let isActive = index == currentPage
                let accentColor: Color = {
                    switch index {
                    case 0: return AppColors.teal
                    case 1: return AppColors.amber
                    default: return AppColors.indigo
                    }
                }()

                Capsule()
                    .fill(isActive ? accentColor : AppColors.gray300)
                    .frame(width: isActive ? 28 : 8, height: 8)
                    .animation(.easeOut(duration: 0.3), value: currentPage)
                    .onTapGesture {
                        withAnimation(.easeInOut(duration: 0.35)) {
                            currentPage = index
                        }
                    }
            }
        }
    }

    // MARK: - Next / Start Button (Exact Flutter _NextButton)
    private var bottomActionButton: some View {
        let isLast = currentPage == pageCount - 1
        return Button {
            if isLast {
                onFinish()
            } else {
                withAnimation(.easeInOut(duration: 0.4)) {
                    currentPage += 1
                }
            }
        } label: {
            HStack(spacing: 8) {
                Text(isLast ? "ابدأ الآن" : "التالي")
                    .font(.tajawal(17, .bold))
                    .foregroundColor(AppColors.paper)

                if !isLast {
                    Image(systemName: "arrow.left")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(AppColors.paper)
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 56)
            .background(
                LinearGradient(
                    colors: isLast ? [AppColors.deepTeal, AppColors.teal] : [AppColors.teal, AppColors.teal],
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .shadow(color: AppColors.teal.opacity(0.35), radius: 20, x: 0, y: 8)
        }
    }
}

// MARK: - Page Content
struct OnboardingPageContent<Illustration: View>: View {
    let isActive: Bool
    let title: String
    let subtitle: String
    @ViewBuilder let illustration: () -> Illustration

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            // Illustration
            illustration()
                .frame(width: 250, height: 235)
                .opacity(isActive ? 1.0 : 0.0)
                .offset(y: isActive ? 0 : 25)
                .animation(.easeOut(duration: 0.5), value: isActive)
                .padding(.bottom, 16)

            // Texts Container
            VStack(spacing: 0) {
                // Amber Top Dash (Exact Flutter: 40 x 5)
                RoundedRectangle(cornerRadius: 4)
                    .fill(AppColors.amber)
                    .frame(width: 40, height: 5)
                    .padding(.bottom, 24)

                // Title (Tajawal 28 ExtraBold)
                Text(title)
                    .font(.tajawal(28, .extraBold))
                    .foregroundColor(AppColors.deepTeal)
                    .multilineTextAlignment(.center)
                    .lineSpacing(4)
                    .padding(.bottom, 16)

                // Subtitle (Tajawal 15.5 Regular)
                Text(subtitle)
                    .font(.tajawal(15.5, .regular))
                    .foregroundColor(AppColors.gray600)
                    .lineSpacing(8)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 24)
            }
            .opacity(isActive ? 1.0 : 0.0)
            .offset(y: isActive ? 0 : 35)
            .animation(.easeOut(duration: 0.6), value: isActive)

            Spacer()
        }
    }
}

// MARK: - Background Ambient Blobs
struct OnboardingBackground: View {
    var body: some View {
        ZStack {
            AppColors.cream

            GeometryReader { geo in
                // Top-Left SoftTeal Circle
                Circle()
                    .fill(AppColors.softTeal.opacity(0.5))
                    .frame(width: 220, height: 220)
                    .position(x: 40, y: 50)

                // Bottom-Right Indigo Circle
                Circle()
                    .fill(AppColors.indigo.opacity(0.08))
                    .frame(width: 200, height: 200)
                    .position(x: geo.size.width + 30, y: geo.size.height - 100)

                // Bottom-Left Amber Circle
                Circle()
                    .fill(AppColors.amber.opacity(0.1))
                    .frame(width: 160, height: 160)
                    .position(x: 60, y: geo.size.height - 30)
            }
        }
        .allowsHitTesting(false)
    }
}

// MARK: - Illustration 1: _TicketIllustration
struct TicketIllustration: View {
    let t: Double // 0.0 to 1.0

    var body: some View {
        let rotation = Angle(radians: -0.05 + t * 0.1)

        ZStack {
            // Back Gradient Card with QR
            RoundedRectangle(cornerRadius: 190 * 0.18, style: .continuous)
                .fill(
                    LinearGradient(
                        colors: [AppColors.indigo, AppColors.teal],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )
                .frame(width: 150, height: 190)
                .overlay {
                    Image(systemName: "qrcode")
                        .font(.system(size: 92))
                        .foregroundColor(Color(hex: 0xFFFDF8, alpha: 0.6))
                }
                .shadow(color: AppColors.teal.opacity(0.35), radius: 34, x: 0, y: 14)
                .offset(x: 16 - t * 8, y: 10)

            // Front Paper Ticket
            VStack(spacing: 0) {
                // Header (App Icon + "طابور")
                HStack(spacing: 8) {
                    RoundedRectangle(cornerRadius: 7)
                        .fill(
                            LinearGradient(
                                colors: [AppColors.deepTeal, AppColors.teal],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 20, height: 20)

                    Text("طابور")
                        .font(.tajawal(16, .bold))
                        .foregroundColor(AppColors.deepTeal)
                }
                .padding(.top, 18)

                Spacer().frame(height: 16)

                // Perforated line with Scissors
                ZStack {
                    Rectangle()
                        .fill(AppColors.gray300)
                        .frame(height: 1)
                        .padding(.horizontal, 16)

                    Circle()
                        .fill(AppColors.paper)
                        .frame(width: 26, height: 26)

                    Image(systemName: "scissors")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(AppColors.gray400)
                        .offset(y: 1)
                }

                Spacer()

                // Ticket Number Label
                Text("رقمك")
                    .font(.tajawal(12, .medium))
                    .foregroundColor(AppColors.gray500)

                // Big Number "07"
                Text("07")
                    .font(.tajawal(44, .extraBold))
                    .foregroundColor(AppColors.deepTeal)
                    .lineLimit(1)

                Spacer().frame(height: 8)

                // "مجلسك وريح" Pill
                Text("مجلسك وريح")
                    .font(.tajawal(13, .bold))
                    .foregroundColor(AppColors.paper)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 5)
                    .background(
                        LinearGradient(
                            colors: [AppColors.deepTeal, AppColors.teal],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .clipShape(Capsule())

                Spacer().frame(height: 14)

                // Barcode Lines
                HStack(spacing: 3) {
                    ForEach(0..<14, id: \.self) { i in
                        RoundedRectangle(cornerRadius: 1)
                            .fill(AppColors.gray600)
                            .frame(width: (i % 3 == 0) ? 3 : 1, height: 12)
                    }
                }
                .padding(.bottom, 14)
            }
            .frame(width: 168, height: 208)
            .background(AppColors.paper)
            .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
            .shadow(color: AppColors.deepTeal.opacity(0.22), radius: 28, x: 0, y: 14)
            .offset(x: -10 + t * 6, y: -14)
        }
        .frame(width: 240, height: 230)
        .rotationEffect(rotation)
        .environment(\.layoutDirection, .leftToRight)
    }
}

// MARK: - Illustration 2: _LiveTrackingIllustration (Exact Circular Counter Arc)
struct LiveTrackingIllustration: View {
    let t: Double // 0.0 to 1.0

    var body: some View {
        let counterNum = max(1, min(6, Int(round(6.0 - (t * 5.0)))))
        let formattedCounter = String(format: "%02d", counterNum)

        ZStack {
            // Indigo Radial Glow behind
            Circle()
                .fill(
                    RadialGradient(
                        colors: [AppColors.indigo.opacity(0.4), AppColors.indigo.opacity(0.0)],
                        center: .center,
                        startRadius: 0,
                        endRadius: 105
                    )
                )
                .frame(width: 210, height: 210)
                .opacity(0.5 + t * 0.3)

            // Scaled White Circle Card (176 x 176)
            ZStack {
                // Background Track Circle (SoftTeal stroke: 7, radius: 80)
                Circle()
                    .stroke(AppColors.softTeal, lineWidth: 7)
                    .frame(width: 160, height: 160)

                // Animated Amber Arc: sweeps counter-clockwise starting from 0 (3 o'clock)
                Circle()
                    .trim(from: 0, to: CGFloat(max(0.01, 1.0 - t)))
                    .stroke(AppColors.amber, style: StrokeStyle(lineWidth: 7, lineCap: .round))
                    .frame(width: 160, height: 160)
                    .scaleEffect(x: 1, y: -1) // Inverts vertical so it sweeps counter-clockwise from 3 o'clock!

                // Inner Content
                VStack(spacing: 2) {
                    // Teal Dot + "أمامك"
                    HStack(spacing: 6) {
                        Circle()
                            .fill(AppColors.teal)
                            .frame(width: 8, height: 8)

                        Text("أمامك")
                            .font(.tajawal(15, .bold))
                            .foregroundColor(AppColors.deepTeal)
                    }

                    // Live Counter Number (Tajawal 58 ExtraBold)
                    Text(formattedCounter)
                        .font(.tajawal(58, .extraBold))
                        .foregroundColor(AppColors.deepTeal)
                        .lineLimit(1)

                    // Pill: icon + "دورك قرب"
                    HStack(spacing: 6) {
                        Image(systemName: "chart.line.downtrend.xyaxis")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(AppColors.teal)

                        Text("دورك قرب")
                            .font(.tajawal(12, .bold))
                            .foregroundColor(AppColors.teal)
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 4)
                    .background(AppColors.softTeal)
                    .clipShape(Capsule())
                }
            }
            .frame(width: 176, height: 176)
            .background(AppColors.paper)
            .clipShape(Circle())
            .shadow(color: AppColors.indigo.opacity(0.2), radius: 28, x: 0, y: 12)
            .scaleEffect(0.97 + t * 0.04)
        }
        .frame(width: 250, height: 235)
        .environment(\.layoutDirection, .leftToRight)
    }
}

// MARK: - Illustration 3: _AlertIllustration
// The Notification Paper Card is UNDER (behind) the Bell Circle!
struct AlertIllustration: View {
    let t: Double // 0.0 to 1.0

    var body: some View {
        ZStack {
            // 1. Ring Ripple Painter (at very back)
            Circle()
                .stroke(AppColors.indigo.opacity((1.0 - t) * 0.18 * 0.9), lineWidth: 3)
                .frame(
                    width: (250 - 16) * (0.5 + t * 0.5),
                    height: (250 - 16) * (0.5 + t * 0.5)
                )

            // 2. Notification Paper Card is UNDER the bell (drawn before the bell)
            ZStack(alignment: .leading) {
                // Amber Bar on Left
                RoundedRectangle(cornerRadius: 2)
                    .fill(AppColors.amber)
                    .frame(width: 4, height: 84)

                VStack(alignment: .leading, spacing: 0) {
                    // Header (Icon + "طابور" + "الآن")
                    HStack {
                        RoundedRectangle(cornerRadius: 7)
                            .fill(
                                LinearGradient(
                                    colors: [AppColors.deepTeal, AppColors.teal],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                            .frame(width: 22, height: 22)
                            .overlay {
                                Image(systemName: "bell")
                                    .font(.system(size: 13))
                                    .foregroundColor(AppColors.paper)
                            }

                        Text("طابور")
                            .font(.tajawal(13, .bold))
                            .foregroundColor(AppColors.deepTeal)

                        Spacer()

                        Text("الآن")
                            .font(.tajawal(10, .medium))
                            .foregroundColor(AppColors.gray500)
                    }

                    Spacer()

                    Text("وقت الخروج!")
                        .font(.tajawal(14, .extraBold))
                        .foregroundColor(AppColors.deepTeal)

                    Spacer().frame(height: 2)

                    Text("قدامك 2 فقط")
                        .font(.tajawal(12, .regular))
                        .foregroundColor(AppColors.gray600)
                }
                .padding(.leading, 14)
                .padding(.trailing, 12)
                .padding(.vertical, 12)
            }
            .frame(width: 176, height: 124)
            .background(AppColors.paper)
            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
            .shadow(color: AppColors.deepTeal.opacity(0.18), radius: 24, x: 0, y: 10)
            .offset(x: -24 + t * 14, y: 0)

            // 3. Bell Circle (drawn on top of the paper card)
            ZStack(alignment: .topTrailing) {
                Circle()
                    .fill(
                        LinearGradient(
                            colors: [AppColors.deepTeal, AppColors.teal],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 128, height: 128)
                    .overlay {
                        Image(systemName: "bell.badge.fill")
                            .font(.system(size: 54))
                            .foregroundColor(AppColors.paper)
                    }
                    .shadow(color: AppColors.teal.opacity(0.35), radius: 26, x: 0, y: 12)

                // Amber Dot on Bell
                Circle()
                    .fill(AppColors.amber)
                    .frame(width: 13, height: 13)
                    .overlay(Circle().stroke(AppColors.deepTeal, lineWidth: 2.5))
                    .scaleEffect(1.0 + t * 0.25)
                    .offset(x: -24, y: 14)
            }
            .offset(x: 24 - t * 6, y: 18 - t * 4)
        }
        .frame(width: 250, height: 235)
        .environment(\.layoutDirection, .leftToRight)
    }
}

#Preview {
    OnboardingView()
}
