import SwiftUI

// MARK: - Native Apple-Grade Ticket History & Saved Time View

public struct TicketHistoryView: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var dataService = QueueDataService.shared
    @ObservedObject private var localization = LocalizationManager.shared
    @Environment(\.colorScheme) private var colorScheme

    @State private var selectedFilter: HistoryFilter = .all
    @State private var selectedPlaceForBooking: ServicePlace? = nil
    @State private var showClearConfirmation = false

    public enum HistoryFilter: String, CaseIterable, Identifiable {
        case all
        case completed
        case cancelled

        public var id: String { rawValue }

        public func title(isArabic: Bool) -> String {
            switch self {
            case .all: return isArabic ? "الكل" : "All"
            case .completed: return isArabic ? "مكتملة" : "Completed"
            case .cancelled: return isArabic ? "ملغية" : "Cancelled"
            }
        }
    }

    public let onDismiss: (() -> Void)?

    public init(onDismiss: (() -> Void)? = nil) {
        self.onDismiss = onDismiss
    }

    private var filteredItems: [TicketHistoryItem] {
        switch selectedFilter {
        case .all:
            return dataService.history
        case .completed:
            return dataService.history.filter { $0.status == .completed }
        case .cancelled:
            return dataService.history.filter { $0.status == .cancelled }
        }
    }

    private var completedCount: Int {
        dataService.history.filter { $0.status == .completed }.count
    }

    private var totalSavedMinutes: Int {
        dataService.history.filter { $0.status == .completed }.reduce(0) { $0 + $1.durationMinutes }
    }

    private var formattedSavedTime: String {
        let hours = totalSavedMinutes / 60
        let mins = totalSavedMinutes % 60
        if hours > 0 {
            return localization.isArabic
                ? "\(hours) س و \(mins) د"
                : "\(hours)h \(mins)m"
        } else {
            return localization.isArabic
                ? "\(mins) دقيقة"
                : "\(mins) mins"
        }
    }

    public var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 18) {
                // Subtitle Header
                Text(localization.isArabic ? "تاريخ طوابيرك والوقت الذي وفرته" : "Past queues & saved waiting time")
                    .font(.app(12, .medium, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 20)
                    .padding(.top, 4)

                // 1. Hero Stat Card (Time Saved)
                heroStatCard

                // 2. Filter Pills
                filterSegmentedBar

                // 3. History List / Empty State
                if filteredItems.isEmpty {
                    emptyFilterView
                } else {
                    LazyVStack(spacing: 12) {
                        ForEach(filteredItems) { item in
                            historyCard(item: item)
                        }
                    }
                    .padding(.horizontal, 20)
                }
            }
            .padding(.top, 6)
            .padding(.bottom, 36)
        }
        .background(AppColors.screenBackground.ignoresSafeArea())
        .navigationBarBackButtonHidden(true)
        .toolbar(.visible, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    dataService.isTabBarHidden = false
                    dismiss()
                    onDismiss?()
                }) {
                    Image(systemName: localization.isArabic ? "chevron.right" : "chevron.left")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(AppColors.textPrimary)
                }
            }

            ToolbarItem(placement: .principal) {
                Text(localization.isArabic ? "سجل الزيارات والتذاكر" : "Ticket History & Visits")
                    .font(.app(17, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)
            }

            ToolbarItem(placement: .topBarTrailing) {
                Menu {
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                        Task { await dataService.refreshAll() }
                    }) {
                        Label(localization.isArabic ? "تحديث السجل" : "Refresh", systemImage: "arrow.clockwise")
                    }

                    Button(action: {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        shareHistoryStats()
                    }) {
                        Label(localization.isArabic ? "مشاركة إنجاز التوفير" : "Share Stats", systemImage: "square.and.arrow.up")
                    }

                    if !dataService.history.isEmpty {
                        Divider()
                        Button(role: .destructive, action: {
                            UIImpactFeedbackGenerator(style: .heavy).impactOccurred()
                            showClearConfirmation = true
                        }) {
                            Label(localization.isArabic ? "مسح السجل" : "Clear History", systemImage: "trash")
                        }
                    }
                } label: {
                    ZStack(alignment: .topTrailing) {
                        Image(systemName: "ticket.fill")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(AppColors.teal)

                        if !dataService.history.isEmpty {
                            Text("\(dataService.history.count)")
                                .font(.system(size: 9, weight: .heavy, design: .rounded))
                                .foregroundColor(.white)
                                .frame(minWidth: 14, minHeight: 14)
                                .background(AppColors.teal)
                                .clipShape(Circle())
                                .offset(x: 8, y: -7)
                        }
                    }
                }
            }
        }
        .confirmationDialog(
            localization.isArabic ? "هل أنت متأكد من رغبتك في مسح سجل الزيارات؟" : "Clear ticket history?",
            isPresented: $showClearConfirmation,
            titleVisibility: .visible
        ) {
            Button(localization.isArabic ? "مسح السجل" : "Clear History", role: .destructive) {
                withAnimation {
                    dataService.history.removeAll()
                }
            }
            Button(localization.isArabic ? "إلغاء" : "Cancel", role: .cancel) { }
        }
        .sheet(item: $selectedPlaceForBooking) { place in
            NavigationStack {
                ServiceDetailsView(place: place) {
                    selectedPlaceForBooking = nil
                    dataService.isTabBarHidden = false
                }
            }
        }
        .environment(\.layoutDirection, localization.layoutDirection)
    }

    // MARK: - 1. Hero Stat Card
    private var heroStatCard: some View {
        VStack(spacing: 14) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 6) {
                    HStack(spacing: 6) {
                        Image(systemName: "sparkles")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(AppColors.amber)

                        Text(localization.isArabic ? "إنجازك مع طابور" : "Your Taboor Impact")
                            .font(.app(12, .bold, isArabic: localization.isArabic))
                            .foregroundColor(Color.white.opacity(0.85))
                    }

                    Text(localization.isArabic ? "وفرت \(formattedSavedTime) انتظار!" : "You saved \(formattedSavedTime) waiting!")
                        .font(.app(21, .extraBold, isArabic: localization.isArabic))
                        .foregroundColor(.white)
                }

                Spacer()

                ZStack {
                    Circle()
                        .fill(Color.white.opacity(0.15))
                        .frame(width: 48, height: 48)

                    Image(systemName: "clock.badge.checkmark.fill")
                        .font(.system(size: 24, weight: .semibold))
                        .foregroundColor(.white)
                }
            }

            Divider()
                .overlay(Color.white.opacity(0.2))

            // Two Metrics
            HStack(spacing: 16) {
                HStack(spacing: 8) {
                    ZStack {
                        Circle()
                            .fill(Color.white.opacity(0.18))
                            .frame(width: 28, height: 28)
                        Image(systemName: "checkmark")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.white)
                    }

                    VStack(alignment: .leading, spacing: 1) {
                        Text("\(completedCount) " + (localization.isArabic ? "طوابير مكتملة" : "Queues completed"))
                            .font(.app(12.5, .bold, isArabic: localization.isArabic))
                            .foregroundColor(.white)
                        Text(localization.isArabic ? "دون وقوف في الزحام" : "Zero standing in lines")
                            .font(.app(10.5, .regular, isArabic: localization.isArabic))
                            .foregroundColor(Color.white.opacity(0.75))
                    }
                }

                Spacer()

                HStack(spacing: 8) {
                    ZStack {
                        Circle()
                            .fill(Color.white.opacity(0.18))
                            .frame(width: 28, height: 28)
                        Image(systemName: "bolt.fill")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(AppColors.amber)
                    }

                    VStack(alignment: .leading, spacing: 1) {
                        Text(localization.isArabic ? "حجز ذكي فوري" : "Instant Smart Queue")
                            .font(.app(12.5, .bold, isArabic: localization.isArabic))
                            .foregroundColor(.white)
                        Text(localization.isArabic ? "تنبيه قبل دورك" : "Timely turn alerts")
                            .font(.app(10.5, .regular, isArabic: localization.isArabic))
                            .foregroundColor(Color.white.opacity(0.75))
                    }
                }
            }
        }
        .padding(18)
        .background(
            LinearGradient(
                colors: [AppColors.ink, AppColors.deepTeal],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
        .shadow(color: AppColors.ink.opacity(0.20), radius: 10, y: 4)
        .padding(.horizontal, 20)
    }

    // MARK: - 2. Filter Segmented Bar
    private var filterSegmentedBar: some View {
        HStack(spacing: 8) {
            ForEach(HistoryFilter.allCases) { filter in
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    withAnimation(.spring(response: 0.3, dampingFraction: 0.75)) {
                        selectedFilter = filter
                    }
                }) {
                    Text(filter.title(isArabic: localization.isArabic))
                        .font(.app(13, selectedFilter == filter ? .bold : .medium, isArabic: localization.isArabic))
                        .foregroundColor(selectedFilter == filter ? .white : AppColors.textSecondary)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .frame(maxWidth: .infinity)
                        .background(
                            selectedFilter == filter
                                ? AnyView(AppColors.teal)
                                : AnyView(Color.white)
                        )
                        .clipShape(Capsule())
                        .overlay(
                            Capsule().strokeBorder(
                                selectedFilter == filter ? Color.clear : AppColors.cardBorder,
                                lineWidth: 1
                            )
                        )
                }
                .buttonStyle(ScaleButtonStyle(scale: 0.96, pressedOpacity: 0.85))
            }
        }
        .padding(.horizontal, 20)
    }

    // MARK: - 3. History Card
    private var isCompleted: (TicketHistoryItem) -> Bool {
        return { $0.status == .completed }
    }

    private func historyCard(item: TicketHistoryItem) -> some View {
        VStack(spacing: 12) {
            // Header Row: Ticket # + Status
            HStack(alignment: .center) {
                // Ticket Number Pill
                HStack(spacing: 5) {
                    Image(systemName: "number")
                        .font(.system(size: 11, weight: .bold))
                    Text(item.ticketNumber)
                        .font(.system(size: 14, weight: .black, design: .rounded))
                }
                .foregroundColor(AppColors.teal)
                .padding(.horizontal, 10)
                .padding(.vertical, 4.5)
                .background(AppColors.softTeal)
                .clipShape(Capsule())

                Spacer()

                // Status Badge
                if item.status == .completed {
                    HStack(spacing: 4) {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 11, weight: .bold))
                        Text(localization.isArabic ? "مكتملة بنجاح" : "Completed")
                            .font(.app(11.5, .bold, isArabic: localization.isArabic))
                    }
                    .foregroundColor(AppColors.accentGreen)
                    .padding(.horizontal, 9)
                    .padding(.vertical, 4)
                    .background(AppColors.accentGreen.opacity(0.12))
                    .clipShape(Capsule())
                } else {
                    HStack(spacing: 4) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 11, weight: .bold))
                        Text(localization.isArabic ? "ملغية" : "Cancelled")
                            .font(.app(11.5, .bold, isArabic: localization.isArabic))
                    }
                    .foregroundColor(AppColors.accentRed)
                    .padding(.horizontal, 9)
                    .padding(.vertical, 4)
                    .background(AppColors.accentRed.opacity(0.12))
                    .clipShape(Capsule())
                }
            }

            // Body: Service Name & Branch
            VStack(alignment: .leading, spacing: 4) {
                Text(item.localizedServiceName(isArabic: localization.isArabic))
                    .font(.app(15, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)
                    .lineLimit(1)

                Text(item.localizedBranch(isArabic: localization.isArabic))
                    .font(.app(12, .regular, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
                    .lineLimit(1)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            Divider()
                .overlay(AppColors.cardBorder.opacity(0.6))

            // Footer Row: Date, Duration, and Re-book button
            HStack(alignment: .center) {
                HStack(spacing: 5) {
                    Image(systemName: "calendar")
                        .font(.system(size: 11, weight: .medium))
                        .foregroundColor(AppColors.textTertiary)

                    Text(item.dateString)
                        .font(.app(11.5, .medium, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)
                }

                Spacer()

                // Re-book Button
                Button(action: {
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    // Find place matching service
                    if let match = dataService.places.first(where: {
                        $0.name.lowercased() == item.serviceName.lowercased() ||
                        $0.nameAr == item.serviceNameAr
                    }) {
                        selectedPlaceForBooking = match
                    } else if let fallback = dataService.places.first {
                        selectedPlaceForBooking = fallback
                    }
                }) {
                    HStack(spacing: 4) {
                        Image(systemName: "arrow.counterclockwise")
                            .font(.system(size: 11, weight: .bold))
                        Text(localization.isArabic ? "حجز دور جديد" : "Book Again")
                            .font(.app(11.5, .bold, isArabic: localization.isArabic))
                    }
                    .foregroundColor(AppColors.teal)
                    .padding(.horizontal, 11)
                    .padding(.vertical, 5.5)
                    .background(AppColors.softTeal)
                    .clipShape(Capsule())
                }
                .buttonStyle(ScaleButtonStyle(scale: 0.94, pressedOpacity: 0.85))
            }
        }
        .padding(16)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.03), radius: 6, y: 2)
    }

    // MARK: - Empty State for Filter
    private var emptyFilterView: some View {
        VStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(AppColors.softTeal)
                    .frame(width: 64, height: 64)

                Image(systemName: "ticket")
                    .font(.system(size: 26, weight: .medium))
                    .foregroundColor(AppColors.teal)
            }
            .padding(.top, 24)

            Text(localization.isArabic ? "لا توجد تذاكر في هذا التصنيف" : "No tickets in this category")
                .font(.app(15, .bold, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textPrimary)

            Text(localization.isArabic ? "جرب تصفية الكل لعرض كامل تاريخك" : "Switch filter to 'All' to view your full history")
                .font(.app(12, .regular, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textSecondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 30)
    }

    private func shareHistoryStats() {
        let text = localization.isArabic
            ? "لقد وفرت \(formattedSavedTime) من وقت الانتظار في طوابير الخدمات عبر تطبيق طابور الذكي! ⏱️✨"
            : "I saved \(formattedSavedTime) of waiting time using Taboor Smart Queue! ⏱️✨"
        let av = UIActivityViewController(activityItems: [text], applicationActivities: nil)
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootVC = windowScene.windows.first?.rootViewController {
            rootVC.present(av, animated: true, completion: nil)
        }
    }
}
