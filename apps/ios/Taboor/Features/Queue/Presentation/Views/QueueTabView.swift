import SwiftUI

// MARK: - Redesigned Native iOS 26 Queue Tab View with Live Tracking & History

public struct QueueTabView: View {
    @StateObject private var dataService = QueueDataService.shared
    @ObservedObject private var localization = LocalizationManager.shared
    @Environment(\.colorScheme) private var colorScheme

    @State private var showCancelAlert = false
    @State private var postponeTrigger = false
    @State private var showAllServices = false
    @State private var selectedPlaceForDetails: ServicePlace? = nil
    @State private var showLiveTracking = false

    public init() {}

    public var body: some View {
        NavigationStack {
            ScrollView(showsIndicators: false) {
                VStack(spacing: 18) {
                    // Top Custom Header Bar (Cohesive with Home Tab)
                    topHeaderBar

                    if let ticket = dataService.activeTicket {
                        // 1. Virtual Ticket Big Interactive Card (Apple Wallet Style)
                        activeTicketCard(ticket: ticket)

                        // 2. Ticket Quick Actions
                        quickActions(ticket: ticket)
                    } else {
                        // Empty State when no queue is active
                        emptyQueueCard
                    }

                    // 3. Past Visits / Queue History Section
                    if !dataService.history.isEmpty {
                        historySection
                    }

                    Spacer().frame(height: 50)
                }
                .padding(.horizontal, 20)
                .padding(.top, 12)
            }
            .background(AppColors.screenBackground.ignoresSafeArea())
            .toolbar(.hidden, for: .navigationBar)
            .refreshable {
                try? await Task.sleep(nanoseconds: 500_000_000)
                withAnimation(.spring) {
                    if let ticket = dataService.activeTicket, ticket.waitingAhead > 1 {
                        dataService.activeTicket?.waitingAhead -= 1
                        dataService.activeTicket?.estMinutes = max(2, (dataService.activeTicket?.estMinutes ?? 4) - 2)
                        dataService.activeTicket?.progress = min(0.95, (dataService.activeTicket?.progress ?? 0.5) + 0.15)
                    }
                }
            }
            .alert(localization.isArabic ? "إلغاء التذكرة" : "Cancel Ticket", isPresented: $showCancelAlert) {
                Button(localization.isArabic ? "تراجع" : "Keep", role: .cancel) {}
                Button(localization.isArabic ? "تأكيد الإلغاء" : "Cancel Ticket", role: .destructive) {
                    withAnimation(.spring(response: 0.4, dampingFraction: 0.8)) {
                        dataService.cancelActiveTicket()
                        UINotificationFeedbackGenerator().notificationOccurred(.warning)
                    }
                }
            } message: {
                Text(localization.isArabic
                     ? "هل أنت متأكد من رغبتك في إلغاء دورك الحالي؟ ستفقد مكانك في الطابور."
                     : "Are you sure you want to cancel your ticket? You will lose your spot in line.")
            }
            .sheet(isPresented: $showAllServices) {
                NavigationStack {
                    AllServicesView { place in
                        showAllServices = false
                        selectedPlaceForDetails = place
                    }
                }
            }
            .sheet(item: $selectedPlaceForDetails) { place in
                NavigationStack {
                    ServiceDetailsView(place: place) {
                        selectedPlaceForDetails = nil
                    }
                }
            }
            .fullScreenCover(isPresented: $showLiveTracking) {
                LiveQueueTrackingView()
            }
        }
    }

    // MARK: - Top Header Bar (Cohesive with Home Tab Style)
    private var topHeaderBar: some View {
        HStack(alignment: .center, spacing: 12) {
            // Section Icon Badge (38x38 Squircle with Gradient)
            ZStack {
                RoundedRectangle(cornerRadius: 12, style: .continuous)
                    .fill(
                        LinearGradient(
                            colors: [AppColors.teal, AppColors.deepTeal],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 38, height: 38)
                    .overlay(
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .strokeBorder(Color.white.opacity(0.25), lineWidth: 1)
                    )
                    .shadow(color: AppColors.teal.opacity(0.25), radius: 4, x: 0, y: 2)

                Image(systemName: "ticket.fill")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.white)
            }

            // Title & Subtitle with Live Badge
            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 6) {
                    Text(localization.isArabic ? "طابوري" : "My Queue")
                        .font(.app(20, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textPrimary)

                    if dataService.activeTicket != nil {
                        HStack(spacing: 4) {
                            Circle()
                                .fill(AppColors.accentGreen)
                                .frame(width: 6, height: 6)
                                .symbolEffect(.pulse)

                            Text(localization.isArabic ? "مباشر" : "Live")
                                .font(.app(11, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.accentGreen)
                        }
                        .padding(.horizontal, 7)
                        .padding(.vertical, 3)
                        .background(AppColors.accentGreen.opacity(colorScheme == .dark ? 0.22 : 0.12))
                        .clipShape(Capsule())
                    }
                }

                Text(dataService.activeTicket != nil
                     ? (localization.isArabic ? "متابعة حية لتذكرتك وموقعك في الطابور" : "Live queue & ticket tracker")
                     : (localization.isArabic ? "لا توجد تذكرة نشطة حالياً" : "No active ticket currently"))
                    .font(.app(11.5, .medium, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
                    .fixedSize(horizontal: false, vertical: true)
            }

            Spacer()

            // Trailing Actions (Circular Button matching Home Tab)
            if dataService.activeTicket != nil {
                // Open Live Tracking Map
                Button(action: {
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    showLiveTracking = true
                }) {
                    Image(systemName: "map.fill")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(AppColors.teal)
                        .frame(width: 38, height: 38)
                        .background(AppColors.cardBackground)
                        .clipShape(Circle())
                        .overlay(
                            Circle()
                                .strokeBorder(Color(UIColor.separator).opacity(colorScheme == .dark ? 0.35 : 0.18), lineWidth: 0.8)
                        )
                        .shadow(color: Color.black.opacity(colorScheme == .dark ? 0 : 0.04), radius: 4, x: 0, y: 1.5)
                }
                .buttonStyle(ScaleButtonStyle(scale: 0.94, pressedOpacity: 0.85))
            }
        }
    }

    // MARK: - 1. Active Ticket Big Card
    private func activeTicketCard(ticket: UserTicket) -> some View {
        VStack(spacing: 18) {
            // Header: Service Name + Branch
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text(ticket.localizedServiceName(isArabic: localization.isArabic))
                        .font(.app(17, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textPrimary)

                    Text(ticket.localizedBranch(isArabic: localization.isArabic))
                        .font(.app(13, .medium, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)
                }

                Spacer()

                ZStack {
                    Circle()
                        .fill(AppColors.teal.opacity(colorScheme == .dark ? 0.22 : 0.12))
                        .frame(width: 48, height: 48)

                    Image(systemName: "ticket.fill")
                        .font(.system(size: 24))
                        .foregroundColor(AppColors.teal)
                }
            }

            // Ticket Tear Line Divider
            ticketDivider

            // Big Ticket Number Display
            VStack(spacing: 6) {
                Text(localization.isArabic ? "رقم تذكرتك" : "Your Ticket Number")
                    .font(.app(12, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textTertiary)

                Text("#\(ticket.ticketNumber)")
                    .font(.system(size: 54, weight: .black, design: .rounded))
                    .foregroundColor(AppColors.teal)
                    .contentTransition(.numericText())

                HStack(spacing: 4) {
                    Image(systemName: "clock.badge.checkmark")
                    Text(localization.isArabic
                         ? "أمامك \(ticket.waitingAhead) فقط في الانتظار"
                         : "Only \(ticket.waitingAhead) ahead in line")
                }
                .font(.app(13, .bold, isArabic: localization.isArabic))
                .foregroundColor(AppColors.amber)
                .contentTransition(.numericText())
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 4)

            // Progress Bar
            VStack(spacing: 6) {
                ProgressView(value: ticket.progress)
                    .tint(AppColors.teal)

                HStack {
                    Text(localization.isArabic ? "يجري تقديم الخدمة: \(ticket.currentServing)" : "Now Serving: \(ticket.currentServing)")
                        .font(.app(11, .medium, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)

                    Spacer()

                    Text("\(Int(ticket.progress * 100))%")
                        .font(.system(size: 11, weight: .bold, design: .rounded))
                        .foregroundColor(AppColors.teal)
                }
            }

            // Real-Time Queue Line Stepper
            HStack(spacing: 10) {
                queueNode(number: ticket.currentServing, labelAr: "في الخدمة", labelEn: "Serving", isCurrent: false, isUser: false)
                arrowSeparator
                queueNode(number: "#A-11", labelAr: "التالي", labelEn: "Next", isCurrent: false, isUser: false)
                arrowSeparator
                queueNode(number: "#\(ticket.ticketNumber)", labelAr: "أنت هنا", labelEn: "You", isCurrent: true, isUser: true)
            }
            .padding(.vertical, 4)

            // Live Tracking CTA Button
            Button(action: {
                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                showLiveTracking = true
            }) {
                HStack(spacing: 8) {
                    Image(systemName: "map.fill")
                        .font(.system(size: 13, weight: .bold))
                    Text(localization.isArabic ? "فتح شاشة التتبع والخريطة المباشرة" : "Open Live Map Tracking")
                        .font(.app(13, .bold, isArabic: localization.isArabic))
                    Spacer()
                    Image(systemName: localization.isArabic ? "arrow.left" : "arrow.right")
                        .font(.system(size: 11, weight: .bold))
                }
                .foregroundColor(.white)
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .background(
                    LinearGradient(
                        colors: [AppColors.teal, AppColors.deepTeal],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                .shadow(color: AppColors.teal.opacity(0.3), radius: 6, y: 3)
            }
            .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.90))

            // Bottom Divider
            ticketDivider

            // Time & Station Info
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(localization.isArabic ? "الوقت التقديري" : "Estimated wait")
                        .font(.app(11, .regular, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)

                    Text(localization.isArabic ? "حوالي \(ticket.estMinutes) دقائق" : "Approx. \(ticket.estMinutes) mins")
                        .font(.app(14, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textPrimary)
                        .contentTransition(.numericText())
                }

                Spacer()

                VStack(alignment: .trailing, spacing: 2) {
                    Text(localization.isArabic ? "شباك الخدمة" : "Counter")
                        .font(.app(11, .regular, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)

                    Text(ticket.localizedCounter(isArabic: localization.isArabic))
                        .font(.app(14, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textPrimary)
                }
            }
        }
        .padding(22)
        .background(AppColors.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 24, style: .continuous)
                .strokeBorder(AppColors.teal.opacity(0.3), lineWidth: 1.2)
        )
        .shadow(color: AppColors.teal.opacity(colorScheme == .dark ? 0.20 : 0.08), radius: 16, y: 6)
    }

    private var ticketDivider: some View {
        HStack(spacing: 6) {
            Circle()
                .fill(AppColors.screenBackground)
                .frame(width: 16, height: 16)
                .offset(x: -28)

            LineDashed()
                .stroke(style: StrokeStyle(lineWidth: 1, dash: [5]))
                .frame(height: 1)
                .foregroundColor(AppColors.cardBorder)

            Circle()
                .fill(AppColors.screenBackground)
                .frame(width: 16, height: 16)
                .offset(x: 28)
        }
    }

    private func queueNode(number: String, labelAr: String, labelEn: String, isCurrent: Bool, isUser: Bool) -> some View {
        VStack(spacing: 6) {
            ZStack {
                Circle()
                    .fill(isUser ? AppColors.teal : (isCurrent ? AppColors.amber : AppColors.subtleFill))
                    .frame(width: 44, height: 44)

                Text(number)
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(isUser ? .white : (isCurrent ? .white : AppColors.textSecondary))
            }

            Text(localization.isArabic ? labelAr : labelEn)
                .font(.app(11, isUser ? .bold : .medium, isArabic: localization.isArabic))
                .foregroundColor(isUser ? AppColors.teal : AppColors.textSecondary)
        }
        .frame(maxWidth: .infinity)
    }

    private var arrowSeparator: some View {
        Image(systemName: localization.isArabic ? "arrow.left" : "arrow.right")
            .font(.system(size: 13, weight: .bold))
            .foregroundColor(AppColors.textTertiary)
            .padding(.bottom, 18)
    }

    // MARK: - 2. Quick Actions
    private func quickActions(ticket: UserTicket) -> some View {
        HStack(spacing: 10) {
            // Postpone Button
            Button(action: {
                withAnimation(.spring(response: 0.35, dampingFraction: 0.7)) {
                    dataService.delayActiveTicket(minutes: 10)
                    postponeTrigger.toggle()
                }
            }) {
                HStack(spacing: 6) {
                    Image(systemName: "clock.arrow.circlepath")
                    Text(localization.isArabic ? "تأجيل الدور (+10د)" : "Postpone (+10m)")
                }
                .font(.app(13, .bold, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textPrimary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
            }
            .buttonStyle(.glass)
            .buttonBorderShape(.capsule)
            .sensoryFeedback(.impact(weight: .medium), trigger: postponeTrigger)

            // Cancel Ticket Button
            Button(action: {
                showCancelAlert = true
            }) {
                HStack(spacing: 6) {
                    Image(systemName: "xmark.circle")
                    Text(localization.isArabic ? "إلغاء الدور" : "Cancel")
                }
                .font(.app(13, .bold, isArabic: localization.isArabic))
                .foregroundColor(AppColors.accentRed)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
            }
            .buttonStyle(.glass)
            .buttonBorderShape(.capsule)
        }
    }

    // MARK: - Empty State Card (Uber Promo Style)
    private var emptyQueueCard: some View {
        Button(action: {
            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
            showAllServices = true
        }) {
            ZStack {
                RoundedRectangle(cornerRadius: 20, style: .continuous)
                    .fill(AppColors.cardBackground)
                    .overlay(
                        LinearGradient(
                            colors: [
                                AppColors.cardBackground,
                                AppColors.teal.opacity(colorScheme == .dark ? 0.09 : 0.04)
                            ],
                            startPoint: .topTrailing,
                            endPoint: .bottomLeading
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 20, style: .continuous)
                            .strokeBorder(Color(UIColor.separator).opacity(colorScheme == .dark ? 0.35 : 0.15), lineWidth: 0.8)
                    )
                    .shadow(color: Color.black.opacity(colorScheme == .dark ? 0.25 : 0.05), radius: 10, y: 3)

                HStack(spacing: 14) {
                    VStack(alignment: .leading, spacing: 5) {
                        HStack(spacing: 4) {
                            Image(systemName: "bolt.fill")
                                .font(.system(size: 9.5, weight: .bold))
                            Text(localization.isArabic ? "وفر وقتك" : "Save Time")
                                .font(.app(11, .bold, isArabic: localization.isArabic))
                        }
                        .foregroundColor(AppColors.teal)
                        .padding(.horizontal, 9)
                        .padding(.vertical, 3.5)
                        .background(AppColors.teal.opacity(colorScheme == .dark ? 0.22 : 0.10))
                        .clipShape(Capsule())

                        Text(localization.isArabic ? "احجز دورك قبل ما توصل" : "Reserve your spot ahead")
                            .font(.app(16, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)
                            .lineLimit(1)

                        Text(localization.isArabic ? "ادخل طابورك وتابع مكانك في الطريق" : "Join remotely & track your turn on the go")
                            .font(.app(11.5, .regular, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textSecondary)
                            .lineLimit(1)

                        HStack(spacing: 6) {
                            Text(localization.isArabic ? "ابدأ الآن" : "Get Started")
                                .font(.app(12.5, .bold, isArabic: localization.isArabic))
                            Image(systemName: localization.isArabic ? "arrow.left" : "arrow.right")
                                .font(.system(size: 10, weight: .bold))
                        }
                        .foregroundColor(.white)
                        .padding(.horizontal, 15)
                        .padding(.vertical, 7.5)
                        .background(
                            LinearGradient(
                                colors: [AppColors.teal, AppColors.deepTeal],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .clipShape(Capsule())
                        .shadow(color: AppColors.teal.opacity(colorScheme == .dark ? 0.40 : 0.28), radius: 6, x: 0, y: 2.5)
                        .padding(.top, 2)
                    }

                    Spacer(minLength: 4)

                    uberTicketIllustration
                        .frame(width: 82, height: 82)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 14)
            }
        }
        .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.95))
    }

    private var uberTicketIllustration: some View {
        ZStack {
            Circle()
                .fill(
                    RadialGradient(
                        colors: [
                            AppColors.teal.opacity(colorScheme == .dark ? 0.35 : 0.22),
                            AppColors.teal.opacity(0)
                        ],
                        center: .center,
                        startRadius: 8,
                        endRadius: 42
                    )
                )
                .frame(width: 84, height: 84)

            ZStack {
                RoundedRectangle(cornerRadius: 11, style: .continuous)
                    .fill(
                        LinearGradient(
                            colors: [AppColors.teal, AppColors.deepTeal],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 52, height: 42)
                    .shadow(color: AppColors.teal.opacity(0.40), radius: 6, x: 0, y: 3)

                VStack(spacing: 3.5) {
                    RoundedRectangle(cornerRadius: 2)
                        .fill(Color.white.opacity(0.85))
                        .frame(width: 26, height: 3.5)
                    RoundedRectangle(cornerRadius: 2)
                        .fill(Color.white.opacity(0.50))
                        .frame(width: 16, height: 2.5)
                }

                HStack {
                    Circle()
                        .fill(AppColors.cardBackground)
                        .frame(width: 7, height: 7)
                        .offset(x: -3.5)
                    Spacer()
                    Circle()
                        .fill(AppColors.cardBackground)
                        .frame(width: 7, height: 7)
                        .offset(x: 3.5)
                }
                .frame(width: 52)
            }
            .rotationEffect(.degrees(-10))

            ZStack {
                Circle()
                    .fill(AppColors.deepTeal)
                    .frame(width: 22, height: 22)
                    .overlay(Circle().stroke(Color.white, lineWidth: 1.8))
                    .shadow(color: Color.black.opacity(0.18), radius: 3, y: 1.5)

                Image(systemName: "clock.fill")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(.white)
            }
            .offset(x: 18, y: -15)

            Image(systemName: "sparkle")
                .font(.system(size: 12, weight: .bold))
                .foregroundColor(AppColors.amber)
                .offset(x: -22, y: 14)
        }
    }

    // MARK: - History Section
    private var historySection: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                Label {
                    Text(localization.isArabic ? "زياراتك السابقة" : "Past Visits")
                        .font(.app(16, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textPrimary)
                } icon: {
                    Image(systemName: "clock.arrow.circlepath")
                        .foregroundColor(AppColors.teal)
                }

                Spacer()
            }

            VStack(spacing: 10) {
                ForEach(dataService.history) { item in
                    HStack(spacing: 12) {
                        ZStack {
                            RoundedRectangle(cornerRadius: 12, style: .continuous)
                                .fill(item.status == .completed ? AppColors.accentGreen.opacity(0.12) : AppColors.accentRed.opacity(0.12))
                                .frame(width: 44, height: 44)

                            Image(systemName: item.status == .completed ? "checkmark.seal.fill" : "xmark.circle.fill")
                                .font(.system(size: 20))
                                .foregroundColor(item.status == .completed ? AppColors.accentGreen : AppColors.accentRed)
                        }

                        VStack(alignment: .leading, spacing: 3) {
                            Text(localization.isArabic ? item.serviceNameAr : item.serviceName)
                                .font(.app(14, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textPrimary)
                                .lineLimit(2)
                                .lineSpacing(2)

                            HStack(spacing: 4) {
                                Text(localization.isArabic ? item.branchLabelAr : item.branchLabel)
                                    .font(.app(11, .regular, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textSecondary)
                                    .lineLimit(1)

                                Text("•")
                                    .foregroundColor(AppColors.textTertiary)

                                Text(item.dateString)
                                    .font(.app(11, .regular, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textTertiary)
                            }
                        }

                        Spacer()

                        VStack(alignment: .trailing, spacing: 2) {
                            Text("#\(item.ticketNumber)")
                                .font(.system(size: 13, weight: .bold, design: .rounded))
                                .foregroundColor(AppColors.textPrimary)

                            Text(item.status == .completed
                                 ? (localization.isArabic ? "مكتملة (\(item.durationMinutes)د)" : "Completed (\(item.durationMinutes)m)")
                                 : (localization.isArabic ? "ملغاة" : "Cancelled"))
                                .font(.app(11, .medium, isArabic: localization.isArabic))
                                .foregroundColor(item.status == .completed ? AppColors.accentGreen : AppColors.accentRed)
                        }
                    }
                    .padding(14)
                    .background(AppColors.cardBackground)
                    .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                    .overlay(
                        RoundedRectangle(cornerRadius: 18, style: .continuous)
                            .strokeBorder(AppColors.cardBorder, lineWidth: 1)
                    )
                    .contextMenu {
                        Button {
                            if let place = dataService.places.first(where: { $0.name == item.serviceName || $0.nameAr == item.serviceNameAr }) {
                                selectedPlaceForDetails = place
                            } else {
                                showAllServices = true
                            }
                        } label: {
                            Label(localization.isArabic ? "حجز دور مجدداً" : "Book Again", systemImage: "arrow.clockwise")
                        }

                        ShareLink(item: "\(item.serviceName) - #\(item.ticketNumber)") {
                            Label(localization.isArabic ? "مشاركة بيانات الزيارة" : "Share Visit Info", systemImage: "square.and.arrow.up")
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Dashed Line Shape
private struct LineDashed: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: 0, y: rect.midY))
        path.addLine(to: CGPoint(x: rect.width, y: rect.midY))
        return path
    }
}
