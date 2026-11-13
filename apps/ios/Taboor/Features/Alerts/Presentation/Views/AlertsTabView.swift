import SwiftUI

// MARK: - Redesigned Native iOS Alerts / Notifications Tab View (Taboor Palette & Apple HIG)

public struct AlertsTabView: View {
    @Binding var selectedTab: TaboorTab
    @StateObject private var dataService = QueueDataService.shared
    @ObservedObject private var localization = LocalizationManager.shared
    @Environment(\.colorScheme) private var colorScheme

    @State private var filterIndex: Int = 0 // 0: All, 1: Unread, 2: Queue Status, 3: Updates
    @State private var showClearConfirm = false

    public init(selectedTab: Binding<TaboorTab> = .constant(.alerts)) {
        self._selectedTab = selectedTab
    }

    private var filteredAlerts: [AppAlertItem] {
        switch filterIndex {
        case 1:
            return dataService.alerts.filter { !$0.isRead }
        case 2:
            return dataService.alerts.filter { $0.type == .turn || $0.type == .join || $0.type == .go }
        case 3:
            return dataService.alerts.filter { $0.type == .open || $0.type == .thanks }
        default:
            return dataService.alerts
        }
    }

    public var body: some View {
        NavigationStack {
            ZStack(alignment: .bottom) {
                ScrollView(showsIndicators: false) {
                    VStack(spacing: 18) {
                        // 1. Top Custom Header Bar
                        topHeaderBar

                        // 2. Filter Segmented Chips Bar
                        filterChipsBar

                        // 3. Alerts Content List
                        if filteredAlerts.isEmpty {
                            emptyAlertsCard
                        } else {
                            VStack(spacing: 12) {
                                ForEach(filteredAlerts) { alert in
                                    alertCard(alert: alert)
                                }
                            }
                        }

                        Spacer().frame(height: 105)
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 12)
                }
                .background(AppColors.screenBackground.ignoresSafeArea())
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(AppColors.screenBackground.ignoresSafeArea())
            .toolbar(.hidden, for: .navigationBar)
            .refreshable {
                await dataService.refreshAll()
            }
            .alert(localization.isArabic ? "مسح جميع التنبيهات" : "Clear All Alerts", isPresented: $showClearConfirm) {
                Button(localization.isArabic ? "إلغاء" : "Cancel", role: .cancel) {}
                Button(localization.isArabic ? "مسح الكل" : "Clear All", role: .destructive) {
                    withAnimation(.spring(response: 0.35, dampingFraction: 0.75)) {
                        dataService.clearAllAlerts()
                    }
                }
            } message: {
                Text(localization.isArabic
                     ? "هل أنت متأكد من مسح جميع التنبيهات؟ لن تتمكن من استعادتها."
                     : "Are you sure you want to clear all alerts? This cannot be undone.")
            }
        }
        .background(AppColors.screenBackground.ignoresSafeArea())
    }

    // MARK: - 1. Top Header Bar
    private var topHeaderBar: some View {
        HStack(alignment: .center, spacing: 12) {
            // Squircle Brand Icon
            ZStack {
                RoundedRectangle(cornerRadius: 13, style: .continuous)
                    .fill(
                        LinearGradient(
                            colors: [AppColors.ink, AppColors.deepTeal],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 42, height: 42)
                    .overlay(
                        RoundedRectangle(cornerRadius: 13, style: .continuous)
                            .strokeBorder(Color.white.opacity(0.18), lineWidth: 1)
                    )
                    .shadow(color: AppColors.ink.opacity(0.2), radius: 6, x: 0, y: 2)

                Image(systemName: "bell.badge.fill")
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(.white)
            }

            // Title & Subtitle with Unread Counter Badge
            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 8) {
                    Text(localization.isArabic ? "التنبيهات" : "Alerts")
                        .font(.app(22, .extraBold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textPrimary)

                    if dataService.unreadAlertsCount > 0 {
                        Text("\(dataService.unreadAlertsCount)")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(.white)
                            .padding(.horizontal, 7)
                            .padding(.vertical, 2)
                            .background(AppColors.amber)
                            .clipShape(Capsule())
                    }
                }

                Text(localization.isArabic ? "إشعارات دورك وتحديثات الأماكن" : "Turn updates & place notifications")
                    .font(.app(12, .medium, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
                    .lineLimit(1)
            }

            Spacer()

            HStack(spacing: 8) {
                // Header Action Pill: Mark All Read
                if dataService.unreadAlertsCount > 0 {
                    Button(action: {
                        withAnimation(.spring(response: 0.35, dampingFraction: 0.75)) {
                            dataService.markAllAlertsRead()
                        }
                    }) {
                        HStack(spacing: 5) {
                            Image(systemName: "checkmark.circle")
                                .font(.system(size: 12, weight: .bold))

                            Text(localization.isArabic ? "تحديد كمقروء" : "Mark Read")
                                .font(.app(11.5, .bold, isArabic: localization.isArabic))
                        }
                        .foregroundColor(AppColors.teal)
                        .padding(.horizontal, 11)
                        .padding(.vertical, 6)
                        .background(Color.white)
                        .clipShape(Capsule())
                        .overlay(
                            Capsule().strokeBorder(AppColors.teal.opacity(0.25), lineWidth: 1)
                        )
                        .shadow(color: Color.black.opacity(0.03), radius: 4, y: 2)
                    }
                    .buttonStyle(ScaleButtonStyle(scale: 0.94, pressedOpacity: 0.85))
                }

                // Native Glass Delete / Clear All Button
                if !dataService.alerts.isEmpty {
                    Button(action: {
                        showClearConfirm = true
                    }) {
                        Image(systemName: "trash")
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(AppColors.accentRed)
                            .frame(width: 24, height: 24)
                    }
                    .buttonStyle(.glass)
                    .buttonBorderShape(.circle)
                }
            }
        }
    }

    // MARK: - 2. Filter Segmented Chips Bar
    private var filterChipsBar: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                filterChip(index: 0, labelAr: "الكل (\(dataService.alerts.count))", labelEn: "All (\(dataService.alerts.count))")
                filterChip(index: 1, labelAr: "غير مقروءة (\(dataService.unreadAlertsCount))", labelEn: "Unread (\(dataService.unreadAlertsCount))")
                filterChip(index: 2, labelAr: "حالة الدور", labelEn: "Queue Status")
                filterChip(index: 3, labelAr: "تحديثات", labelEn: "Updates")
            }
            .padding(.vertical, 2)
        }
    }

    private func filterChip(index: Int, labelAr: String, labelEn: String) -> some View {
        let isSelected = filterIndex == index
        return Button(action: {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            withAnimation(.spring(response: 0.3, dampingFraction: 0.75)) {
                filterIndex = index
            }
        }) {
            Text(localization.isArabic ? labelAr : labelEn)
                .font(.app(12.5, isSelected ? .bold : .medium, isArabic: localization.isArabic))
                .foregroundColor(isSelected ? .white : AppColors.textSecondary)
                .padding(.horizontal, 14)
                .padding(.vertical, 7.5)
                .background(isSelected ? AppColors.ink : Color.white)
                .clipShape(Capsule())
                .overlay(
                    Capsule()
                        .strokeBorder(isSelected ? Color.clear : AppColors.cardBorder, lineWidth: 1)
                )
                .shadow(color: isSelected ? AppColors.ink.opacity(0.18) : Color.black.opacity(0.02), radius: 4, y: 2)
        }
        .buttonStyle(ScaleButtonStyle(scale: 0.95, pressedOpacity: 0.85))
    }

    // MARK: - 3. Alert Card
    private func alertCard(alert: AppAlertItem) -> some View {
        Button(action: {
            if !alert.isRead {
                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                withAnimation(.spring(response: 0.3, dampingFraction: 0.75)) {
                    dataService.markAlertRead(id: alert.id)
                }
            }
        }) {
            HStack(alignment: .top, spacing: 14) {
                // Leading Icon in Squircle Badge
                ZStack {
                    RoundedRectangle(cornerRadius: 13, style: .continuous)
                        .fill(alertIconBackground(for: alert.type))
                        .frame(width: 44, height: 44)

                    Image(systemName: alertIcon(for: alert.type))
                        .font(.system(size: 19, weight: .bold))
                        .foregroundColor(alertIconColor(for: alert.type))
                }

                // Alert Details
                VStack(alignment: .leading, spacing: 4) {
                    HStack(alignment: .firstTextBaseline) {
                        Text(localization.isArabic ? alert.titleAr : alert.titleEn)
                            .font(.app(14.5, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)
                            .lineLimit(2)
                            .fixedSize(horizontal: false, vertical: true)

                        Spacer()

                        // Unread Pulse Amber Dot
                        if !alert.isRead {
                            Circle()
                                .fill(AppColors.amber)
                                .frame(width: 8, height: 8)
                        }
                    }

                    Text(localization.isArabic ? alert.descAr : alert.descEn)
                        .font(.app(12.5, .regular, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)
                        .lineSpacing(3)
                        .lineLimit(3)
                        .fixedSize(horizontal: false, vertical: true)

                    // Footer: Relative Time & Category Tag
                    HStack(spacing: 6) {
                        HStack(spacing: 4) {
                            Image(systemName: "clock")
                                .font(.system(size: 10))
                            Text(localization.isArabic ? alert.timeAgoAr : alert.timeAgoEn)
                                .font(.app(11, .regular, isArabic: localization.isArabic))
                        }
                        .foregroundColor(AppColors.textTertiary)

                        Text("•")
                            .foregroundColor(AppColors.textTertiary)

                        Text(alertCategoryTag(for: alert.type))
                            .font(.app(10.5, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.teal)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(AppColors.softTeal)
                            .clipShape(Capsule())
                    }
                    .padding(.top, 4)
                }
            }
            .padding(14)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 18, style: .continuous)
                    .strokeBorder(!alert.isRead ? AppColors.amber.opacity(0.35) : AppColors.cardBorder, lineWidth: 1.2)
            )
            .shadow(color: Color.black.opacity(!alert.isRead ? 0.04 : 0.02), radius: 6, y: 2)
        }
        .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.92))
        .contextMenu {
            Button(role: .destructive) {
                withAnimation(.spring) {
                    dataService.deleteAlert(id: alert.id)
                }
            } label: {
                Label(localization.isArabic ? "حذف التنبيه" : "Delete", systemImage: "trash")
            }

            if !alert.isRead {
                Button {
                    withAnimation(.spring) {
                        dataService.markAlertRead(id: alert.id)
                    }
                } label: {
                    Label(localization.isArabic ? "تحديد كمقروء" : "Mark as Read", systemImage: "checkmark.circle")
                }
            }
        }
    }

    // Helper functions for Alert Type Icon & Styling
    private func alertIcon(for type: AlertType) -> String {
        switch type {
        case .turn: return "clock.badge.fill"
        case .join: return "ticket.fill"
        case .go: return "checkmark.seal.fill"
        case .open: return "door.left.hand.open"
        case .thanks: return "star.circle.fill"
        }
    }

    private func alertIconColor(for type: AlertType) -> Color {
        switch type {
        case .turn: return AppColors.amber
        case .join: return AppColors.teal
        case .go: return AppColors.accentGreen
        case .open: return AppColors.indigo
        case .thanks: return AppColors.teal
        }
    }

    private func alertIconBackground(for type: AlertType) -> Color {
        switch type {
        case .turn: return AppColors.amber.opacity(0.12)
        case .join: return AppColors.softTeal
        case .go: return AppColors.accentGreen.opacity(0.12)
        case .open: return AppColors.indigo.opacity(0.12)
        case .thanks: return AppColors.softTeal
        }
    }

    private func alertCategoryTag(for type: AlertType) -> String {
        if localization.isArabic {
            switch type {
            case .turn: return "اقتراب الدور"
            case .join: return "تأكيد الحجز"
            case .go: return "انتهاء الزيارة"
            case .open: return "فتح الطابور"
            case .thanks: return "تقييم وتجربة"
            }
        } else {
            switch type {
            case .turn: return "Turn Alert"
            case .join: return "Reservation"
            case .go: return "Completed"
            case .open: return "Queue Open"
            case .thanks: return "Feedback"
            }
        }
    }

    // MARK: - Empty Alerts State
    private var emptyAlertsCard: some View {
        VStack(spacing: 16) {
            ZStack {
                Circle()
                    .fill(AppColors.softTeal)
                    .frame(width: 70, height: 70)

                Image(systemName: "bell.slash")
                    .font(.system(size: 26, weight: .semibold))
                    .foregroundColor(AppColors.teal)
            }
            .padding(.top, 14)

            VStack(spacing: 4) {
                Text(localization.isArabic ? "لا توجد تنبيهات جديدة" : "No Alerts Found")
                    .font(.app(16, .extraBold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)

                Text(localization.isArabic
                     ? "ستظهر هنا إشعارات دورك عند اقترابه وتحديثات الأماكن المحجوزة"
                     : "Turn alerts and updates from places you joined will appear here")
                    .font(.app(12.5, .regular, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
                    .multilineTextAlignment(.center)
                    .lineSpacing(2)
                    .padding(.horizontal, 24)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 32)
        .padding(.horizontal, 16)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 20, style: .continuous)
                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.02), radius: 6, y: 2)
    }
}
