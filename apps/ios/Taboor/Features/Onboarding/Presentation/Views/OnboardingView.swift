import SwiftUI
import Combine

// MARK: - Arc Shape for Live Tracking Counter (Counter-Clockwise Sweep from 3 o'clock)

struct CounterArcShape: Shape {
    var progress: Double

    var animatableData: Double {
        get { progress }
        set { progress = newValue }
    }

    func path(in rect: CGRect) -> Path {
        var path = Path()
        let center = CGPoint(x: rect.midX, y: rect.midY)
        let radius = min(rect.width, rect.height) / 2 - 8

        // Flutter & Android: drawArc(rect, 0, -360 * progress)
        // Starts at 0 radians (3 o'clock) and sweeps counter-clockwise
        path.addArc(
            center: center,
            radius: radius,
            startAngle: .radians(0),
            endAngle: .radians(-2.0 * .pi * max(0.001, progress)),
            clockwise: true
        )
        return path
    }
}

// MARK: - Onboarding Screen

struct OnboardingView: View {
    var onFinish: () -> Void = {}

    @State private var currentPage = 0
    @State private var scrollPosition: Int? = 0
    @State private var isTouching = false
    @State private var isAutoAdvanceEnabled = true
    private let autoTimer = Timer.publish(every: 3.5, on: .main, in: .common).autoconnect()

    private let localization = LocalizationManager.shared
    private let pageCount = 3

    var body: some View {
        ZStack {
            // Ambient Background
            OnboardingBackground()
                .ignoresSafeArea()

            VStack(spacing: 0) {
                // Top Bar with Language Toggle
                HStack {
                    LanguageToggleButton()
                    Spacer()
                }
                .padding(.horizontal, 24)
                .padding(.top, 12)
                .frame(height: 52)

                // Page Carousel via Paged ScrollView (Zero TabView bugs, 100% reliable swipe & active dots)
                ScrollView(.horizontal, showsIndicators: false) {
                    LazyHStack(spacing: 0) {
                        // Page 0: Ticket
                        OnboardingPageContent(
                            title: localization.onboarding1Title,
                            subtitle: localization.onboarding1Subtitle,
                            isActive: currentPage == 0
                        ) {
                            TicketAnimationHost()
                        }
                        .containerRelativeFrame(.horizontal)
                        .id(0)

                        // Page 1: Live Tracking
                        OnboardingPageContent(
                            title: localization.onboarding2Title,
                            subtitle: localization.onboarding2Subtitle,
                            isActive: currentPage == 1
                        ) {
                            LiveTrackingAnimationHost()
                        }
                        .containerRelativeFrame(.horizontal)
                        .id(1)

                        // Page 2: Alert
                        OnboardingPageContent(
                            title: localization.onboarding3Title,
                            subtitle: localization.onboarding3Subtitle,
                            isActive: currentPage == 2
                        ) {
                            AlertAnimationHost()
                        }
                        .containerRelativeFrame(.horizontal)
                        .id(2)
                    }
                    .scrollTargetLayout()
                }
                .scrollTargetBehavior(.paging)
                .scrollPosition(id: $scrollPosition)
                .detectScrollInteraction(isTouching: $isTouching)
                .onChange(of: isTouching) { _, touching in
                    if touching {
                        isAutoAdvanceEnabled = false
                    }
                }
                .onChange(of: scrollPosition) { _, newPos in
                    if let newPos = newPos, newPos != currentPage {
                        currentPage = newPos
                        isAutoAdvanceEnabled = false
                    }
                }
                .onChange(of: currentPage) { _, newPage in
                    if scrollPosition != newPage {
                        withAnimation(.easeInOut(duration: 0.35)) {
                            scrollPosition = newPage
                        }
                    }
                }

                // Dots Indicator (28x8 active, 8x8 inactive with individual tab accents)
                dotsIndicator
                    .padding(.bottom, 20)

                // Bottom Action Button
                bottomActionButton
                    .padding(.horizontal, 24)
                    .padding(.bottom, 24)
            }
        }
        // Auto-advance timer (only runs until user interacts manually)
        .onReceive(autoTimer) { _ in
            guard isAutoAdvanceEnabled, !isTouching else { return }
            withAnimation(.easeInOut(duration: 0.45)) {
                currentPage = (currentPage + 1) % pageCount
            }
        }
    }

    // MARK: - Dots Indicator (Exact match from Android/Flutter with frosted pill)
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
                    .animation(.easeInOut(duration: 0.3), value: currentPage)
                    .contentShape(Rectangle())
                    .onTapGesture {
                        UISelectionFeedbackGenerator().selectionChanged()
                        isAutoAdvanceEnabled = false
                        withAnimation(.easeInOut(duration: 0.35)) {
                            currentPage = index
                        }
                    }
            }
        }
    }

    // MARK: - Next / Start Button (56pt height, 16pt radius)
    private var bottomActionButton: some View {
        let isLast = currentPage == pageCount - 1
        return Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            isAutoAdvanceEnabled = false
            if isLast {
                onFinish()
            } else {
                withAnimation(.easeInOut(duration: 0.35)) {
                    currentPage += 1
                }
            }
        } label: {
            ZStack {
                if isLast {
                    Text(localization.startNow)
                        .font(.app(17, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.paper)
                        .transition(.opacity)
                } else {
                    HStack(spacing: 8) {
                        Text(localization.next)
                            .font(.app(17, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.paper)

                        Image(systemName: localization.isArabic ? "arrow.left" : "arrow.right")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(AppColors.paper)
                    }
                    .transition(.opacity)
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
            .shadow(color: AppColors.teal.opacity(0.35), radius: 16, x: 0, y: 8)
        }
        .buttonStyle(ScaleButtonStyle())
    }
}

// MARK: - Page Content Wrapper

struct OnboardingPageContent<Illustration: View>: View {
    let title: String
    let subtitle: String
    var isActive: Bool = true
    @ViewBuilder let illustration: () -> Illustration

    private let localization = LocalizationManager.shared

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            illustration()
                .frame(height: 250)
                .padding(.bottom, 20)
                .offset(y: isActive ? 0 : 25)
                .opacity(isActive ? 1 : 0)
                .animation(.easeOut(duration: 0.45).delay(0.04), value: isActive)

            // Accent Pill
            Capsule()
                .fill(AppColors.amber)
                .frame(width: 40, height: 5)
                .padding(.bottom, 18)
                .offset(y: isActive ? 0 : 30)
                .opacity(isActive ? 1 : 0)
                .animation(.easeOut(duration: 0.5).delay(0.08), value: isActive)

            // Title
            Text(title)
                .font(.app(28, .extraBold, isArabic: localization.isArabic))
                .foregroundColor(AppColors.deepTeal)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 24)
                .padding(.bottom, 12)
                .offset(y: isActive ? 0 : 35)
                .opacity(isActive ? 1 : 0)
                .animation(.easeOut(duration: 0.55).delay(0.12), value: isActive)

            // Subtitle
            Text(subtitle)
                .font(.app(15.5, .regular, isArabic: localization.isArabic))
                .foregroundColor(AppColors.gray600)
                .multilineTextAlignment(.center)
                .lineSpacing(4)
                .padding(.horizontal, 32)
                .offset(y: isActive ? 0 : 40)
                .opacity(isActive ? 1 : 0)
                .animation(.easeOut(duration: 0.6).delay(0.15), value: isActive)

            Spacer()
        }
    }
}

// MARK: - Animation Hosts (Using TimelineView for 60/120fps Silky-Smooth Interpolation)

struct TicketAnimationHost: View {
    var body: some View {
        TimelineView(.animation) { timeline in
            let cycle: Double = 4.0
            let elapsed = timeline.date.timeIntervalSinceReferenceDate
            let phase = (elapsed.truncatingRemainder(dividingBy: cycle)) / cycle
            let t = phase < 0.5 ? (phase * 2.0) : (2.0 - phase * 2.0)
            TicketIllustration(t: t)
        }
    }
}

struct LiveTrackingAnimationHost: View {
    var body: some View {
        TimelineView(.animation) { timeline in
            let cycle: Double = 4.0
            let elapsed = timeline.date.timeIntervalSinceReferenceDate
            let phase = (elapsed.truncatingRemainder(dividingBy: cycle)) / cycle
            let t = phase < 0.5 ? (phase * 2.0) : (2.0 - phase * 2.0)
            LiveTrackingIllustration(t: t)
        }
    }
}

struct AlertAnimationHost: View {
    var body: some View {
        TimelineView(.animation) { timeline in
            let cycle: Double = 4.0
            let elapsed = timeline.date.timeIntervalSinceReferenceDate
            let phase = (elapsed.truncatingRemainder(dividingBy: cycle)) / cycle
            let t = phase < 0.5 ? (phase * 2.0) : (2.0 - phase * 2.0)
            AlertIllustration(t: t)
        }
    }
}

// MARK: - Illustration 1: TicketIllustration (1:1 Android/Flutter)

struct TicketIllustration: View {
    let t: Double
    private let localization = LocalizationManager.shared

    var body: some View {
        let rotationAngle = Angle.degrees(-3.0 + t * 6.0)

        ZStack {
            // Backdrop QR Card (offset x: 16 - t * 8, y: 10, size 150x190)
            RoundedRectangle(cornerRadius: 28, style: .continuous)
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
                        .font(.system(size: 80, weight: .light))
                        .foregroundColor(AppColors.paper.opacity(0.6))
                }
                .shadow(color: AppColors.teal.opacity(0.4), radius: 20, x: 0, y: 10)
                .offset(x: 16 - CGFloat(t * 8), y: 10)

            // Foreground White Ticket Card (offset x: -10 + t * 6, y: -14, size 168x208)
            VStack(spacing: 0) {
                Spacer().frame(height: 16)

                // Header: brand mark + appTitle
                HStack(spacing: 8) {
                    RoundedRectangle(cornerRadius: 7, style: .continuous)
                        .fill(
                            LinearGradient(
                                colors: [AppColors.deepTeal, AppColors.teal],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 20, height: 20)

                    Text(localization.appTitle)
                        .font(.app(16, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.deepTeal)
                }

                Spacer().frame(height: 14)

                // Cut line with scissors
                ZStack {
                    Rectangle()
                        .fill(AppColors.gray300)
                        .frame(height: 1)
                        .padding(.horizontal, 16)

                    Circle()
                        .fill(AppColors.paper)
                        .frame(width: 24, height: 24)

                    Image(systemName: "scissors")
                        .font(.system(size: 13))
                        .foregroundColor(AppColors.gray400)
                }

                Spacer()

                Text(localization.ticketNumberLabel)
                    .font(.app(12, .medium, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.gray500)

                Text("07")
                    .font(.app(44, .extraBold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.deepTeal)
                    .lineLimit(1)

                Spacer().frame(height: 6)

                Text(localization.ticketSitRelax)
                    .font(.app(12, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.paper)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 4)
                    .background(
                        LinearGradient(
                            colors: [AppColors.deepTeal, AppColors.teal],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .clipShape(Capsule())

                Spacer().frame(height: 12)

                // Barcode simulation dashes
                HStack(spacing: 3) {
                    ForEach(0..<14, id: \.self) { i in
                        RoundedRectangle(cornerRadius: 1)
                            .fill(AppColors.gray600)
                            .frame(width: (i % 3 == 0) ? 3 : 1.5, height: 12)
                    }
                }
                .padding(.bottom, 14)
            }
            .frame(width: 168, height: 208)
            .background(AppColors.paper)
            .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
            .shadow(color: AppColors.deepTeal.opacity(0.35), radius: 24, x: 0, y: 12)
            .offset(x: -10 + CGFloat(t * 6), y: -14)
        }
        .frame(width: 240, height: 230)
        .rotationEffect(rotationAngle)
        .environment(\.layoutDirection, .leftToRight)
    }
}

// MARK: - Illustration 2: LiveTrackingIllustration (1:1 Android/Flutter)

struct LiveTrackingIllustration: View {
    let t: Double
    private let localization = LocalizationManager.shared

    var body: some View {
        let countValue = max(1, min(6, Int(round(6.0 - (t * 5.0)))))
        let formattedCounter = String(format: "%02d", countValue)
        let scale = 0.97 + t * 0.04
        let arcProgress = 1.0 - t

        ZStack {
            // Pulsating radar radial background (size 210)
            Circle()
                .fill(
                    RadialGradient(
                        colors: [AppColors.indigo.opacity(0.35), Color.clear],
                        center: .center,
                        startRadius: 0,
                        endRadius: 105
                    )
                )
                .frame(width: 210, height: 210)
                .opacity(0.5 + t * 0.3)

            // Center circular counter (size 176)
            ZStack {
                // 1. SoftTeal Track Circle (diameter 160)
                Circle()
                    .stroke(AppColors.softTeal, lineWidth: 7)
                    .frame(width: 160, height: 160)

                // 2. Amber Arc indicator - drawn DIRECTLY ON the SoftTeal track
                Circle()
                    .trim(from: 0, to: CGFloat(max(0.001, min(1.0, arcProgress))))
                    .stroke(AppColors.amber, style: StrokeStyle(lineWidth: 7, lineCap: .round))
                    .frame(width: 160, height: 160)
                    .scaleEffect(y: -1)

                // Counter Content Inside
                VStack(spacing: 2) {
                    HStack(spacing: 5) {
                        Circle()
                            .fill(AppColors.teal)
                            .frame(width: 6, height: 6)

                        Text(localization.liveAhead)
                            .font(.app(localization.isArabic ? 13.5 : 11, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.deepTeal)
                            .lineLimit(1)
                            .minimumScaleFactor(0.8)
                    }

                    // Dynamic Countdown 05..01
                    Text(formattedCounter)
                        .font(.app(localization.isArabic ? 50 : 46, .extraBold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.deepTeal)
                        .lineLimit(1)

                    // Badge: "دورك قرب" / "Almost your turn" with trend icon
                    HStack(spacing: 3) {
                        Image(systemName: "chart.line.downtrend.xyaxis")
                            .font(.system(size: localization.isArabic ? 9.5 : 8.5, weight: .bold))
                            .foregroundColor(AppColors.teal)

                        Text(localization.liveTurnNear)
                            .font(.app(localization.isArabic ? 11.5 : 9.5, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.teal)
                            .lineLimit(1)
                            .minimumScaleFactor(0.7)
                    }
                    .padding(.horizontal, localization.isArabic ? 9 : 8)
                    .padding(.vertical, 3)
                    .frame(maxWidth: 114)
                    .background(AppColors.softTeal)
                    .clipShape(Capsule())
                }
            }
            .frame(width: 176, height: 176)
            .background(AppColors.paper)
            .clipShape(Circle())
            .shadow(color: AppColors.indigo.opacity(0.2), radius: 20, x: 0, y: 12)
            .scaleEffect(scale)
        }
        .frame(width: 250, height: 235)
        .environment(\.layoutDirection, .leftToRight)
    }
}

// MARK: - Illustration 3: AlertIllustration (Exact match from Android/Flutter)

struct AlertIllustration: View {
    let t: Double
    private let localization = LocalizationManager.shared

    var body: some View {
        let bellOffsetX = 24.0 - t * 6.0
        let bellOffsetY = 18.0 - t * 4.0
        let cardOffsetX = -24.0 + t * 14.0
        let cardOffsetY = 0.0

        let rippleRadius = 110.0 * (0.5 + t * 0.5)
        let rippleAlpha = max(0.0, min(1.0, (1.0 - t) * 0.9)) * 0.35

        ZStack {
            // 1. Expanding Sound Ripple Ring (Centered around the bell)
            Circle()
                .stroke(AppColors.indigo.opacity(rippleAlpha), lineWidth: 3)
                .frame(width: rippleRadius * 2, height: rippleRadius * 2)
                .offset(x: bellOffsetX, y: bellOffsetY)

            // 2. Active Bell Circle (Rendered FIRST / UNDER the notification card)
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
                            .font(.system(size: 52, weight: .bold))
                            .foregroundColor(AppColors.paper)
                    }
                    .shadow(color: AppColors.teal.opacity(0.4), radius: 24, x: 0, y: 12)

                // Pulsing Notification Dot at top-trailing
                Circle()
                    .fill(AppColors.amber)
                    .frame(width: 13, height: 13)
                    .overlay(
                        Circle().stroke(AppColors.deepTeal, lineWidth: 2.5)
                    )
                    .scaleEffect(1.0 + t * 0.25)
                    .padding(.top, 14)
                    .padding(.trailing, 24)
            }
            .offset(x: bellOffsetX, y: bellOffsetY)

            // 3. Notification Card Popup (Rendered ON TOP of the bell)
            HStack(spacing: 0) {
                // Amber Accent Strip on leading edge
                RoundedRectangle(cornerRadius: 2)
                    .fill(AppColors.amber)
                    .frame(width: 4, height: 76)

                VStack(alignment: .leading, spacing: 0) {
                    // Header Row
                    HStack(spacing: 8) {
                        RoundedRectangle(cornerRadius: 7, style: .continuous)
                            .fill(
                                LinearGradient(
                                    colors: [AppColors.deepTeal, AppColors.teal],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                            .frame(width: 22, height: 22)
                            .overlay {
                                Image(systemName: "bell.fill")
                                    .font(.system(size: 11, weight: .bold))
                                    .foregroundColor(AppColors.paper)
                            }

                        Text(localization.appTitle)
                            .font(.app(13, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.deepTeal)

                        Spacer()

                        Text(localization.alertNow)
                            .font(.app(10, .medium, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.gray500)
                    }

                    Spacer()

                    // Title: "وقت الخروج!" / "Time to go!"
                    Text(localization.alertLeaveTitle)
                        .font(.app(14, .extraBold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.deepTeal)
                        .lineLimit(1)
                        .minimumScaleFactor(0.85)

                    Spacer().frame(height: 2)

                    // Subtitle: "قدامك 2 فقط" / "Only 2 ahead"
                    Text(localization.alertOnlyTwo)
                        .font(.app(12, .regular, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.gray600)
                        .lineLimit(1)
                        .minimumScaleFactor(0.85)
                }
                .padding(.leading, 12)
                .padding(.trailing, 12)
                .padding(.vertical, 12)
            }
            .frame(width: 176, height: 124)
            .background(AppColors.paper)
            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
            .shadow(color: AppColors.deepTeal.opacity(0.18), radius: 24, x: 0, y: 10)
            .offset(x: cardOffsetX, y: cardOffsetY)
        }
        .frame(width: 250, height: 235)
        .environment(\.layoutDirection, .leftToRight)
    }
}

// MARK: - Ambient Circles Background

struct OnboardingBackground: View {
    var body: some View {
        GeometryReader { proxy in
            let w = proxy.size.width
            let h = proxy.size.height

            ZStack {
                AppColors.background
                    .ignoresSafeArea()

                // Top-Left SoftTeal Circle (Flutter: 220x220, top: -60, left: -70)
                Circle()
                    .fill(AppColors.softTeal.opacity(0.5))
                    .frame(width: 220, height: 220)
                    .position(x: 40, y: 50)

                // Bottom-Right faint Indigo Circle (Flutter: 200x200, bottom: 60, right: -70)
                Circle()
                    .fill(AppColors.indigo.opacity(0.08))
                    .frame(width: 200, height: 200)
                    .position(x: w + 20, y: h - 140)

                // Bottom-Left faint Amber Circle (Flutter: 160x160, bottom: -50, left: 20)
                Circle()
                    .fill(AppColors.amber.opacity(0.1))
                    .frame(width: 160, height: 160)
                    .position(x: 80, y: h + 20)
            }
            .allowsHitTesting(false)
        }
    }
}

// MARK: - Scroll Interaction Detection Helper

private extension View {
    @ViewBuilder
    func detectScrollInteraction(isTouching: Binding<Bool>) -> some View {
        if #available(iOS 18.0, *) {
            self.onScrollPhaseChange { _, newPhase in
                isTouching.wrappedValue = (newPhase == .interacting || newPhase == .tracking)
            }
        } else {
            self.simultaneousGesture(
                DragGesture(minimumDistance: 15)
                    .onChanged { _ in isTouching.wrappedValue = true }
                    .onEnded { _ in isTouching.wrappedValue = false }
            )
        }
    }
}
