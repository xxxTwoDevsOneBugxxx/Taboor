import SwiftUI

// MARK: - Redesigned Native iOS 26 Alerts / Notifications View

public struct AlertsTabView: View {
    @StateObject private var dataService = QueueDataService.shared
    @ObservedObject private var localization = LocalizationManager.shared
    @Environment(\.colorScheme) private var colorScheme

    @State private var filterIndex: Int = 0 // 0 = All, 1 = Unread Only
    @State private var showClearConfirm = false

    public init() {}

    private var displayedAlerts: [AppAlertItem] {
        if filterIndex == 1 {
            return dataService.alerts.filter { !$0.isRead }
        }
        return dataService.alerts
    }

    private var unreadAlerts: [AppAlertItem] {
        dataService.alerts.filter { !$0.isRead }
    }

    private var readAlerts: [AppAlertItem] {
        dataService.alerts.filter { $0.isRead }
    }

    public var body: some View {
        NavigationStack {
            ScrollView(showsIndicators: false) {
                VStack(spacing: 16) {
                    // 1. Top Custom Header Bar (Cohesive with Home & Queue Tabs)
                    topHeaderBar

                    // 2. Custom Filter Segmented Pills
                    filterPillsBar

                    // 3. Alerts Content
                    if displayedAlerts.isEmpty {
                        emptyAlertsCard
                    } else {
                        if filterIndex == 0 {
                            // Section: Unread (if any)
                            if !unreadAlerts.isEmpty {
                                sectionHeader(localization.isArabic ? "تنبيهات جديدة" : "NEW NOTIFICATIONS")
                                ForEach(unreadAlerts) { alert in
                                    alertCard(alert: alert)
                                }
                            }

                            // Section: Earlier
                            if !readAlerts.isEmpty {
                                sectionHeader(localization.isArabic ? "السابقة" : "EARLIER")
                                    .padding(.top, unreadAlerts.isEmpty ? 0 : 8)
                                ForEach(readAlerts) { alert in
                                    alertCard(alert: alert)
                                }
                            }
                        } else {
                            // Filtered Unread Only
                            ForEach(displayedAlerts) { alert in
                                alertCard(alert: alert)
                            }
                        }
                    }

                    Spacer().frame(height: 60)
                }
                .padding(.horizontal, 20)
                .padding(.top, 12)
            }
            .background(AppColors.screenBackground.ignoresSafeArea())
            .toolbar(.hidden, for: .navigationBar)
            .refreshable {
                try? await Task.sleep(nanoseconds: 500_000_000)
            }
            .alert(localization.isArabic ? "مسح جميع التنبيهات" : "Clear All Alerts", isPresented: $showClearConfirm) {
                Button(localization.isArabic ? "إلغاء" : "Cancel", role: .cancel) {}
                Button(localization.isArabic ? "مسح الكل" : "Clear All", role: .destructive) {
                    withAnimation(.spring) {
                        dataService.clearAllAlerts()
                    }
                }
            } message: {
                Text(localization.isArabic
                     ? "هل أنت متأكد من مسح جميع التنبيهات؟ لن تتمكن من استعادتها."
                     : "Are you sure you want to clear all alerts? This cannot be undone.")
            }
        }
    }

    // MARK: - 1. Top Header Bar (Cohesive with Home & Queue Tabs)
    private var topHeaderBar: some View {
        HStack(alignment: .center, spacing: 12) {
            // Icon Squircle Badge (38x38 with gradient)
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

                Image(systemName: "bell.badge.fill")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(.white)
            }

            // Title & Subtitle with Unread Counter Badge
            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 6) {
                    Text(localization.isArabic ? "التنبيهات" : "Alerts")
                        .font(.app(20, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textPrimary)

                    if dataService.unreadAlertsCount > 0 {
                        Text("\(dataService.unreadAlertsCount)")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(.white)
                            .padding(.horizontal, 7)
                            .padding(.vertical, 2)
                            .background(AppColors.accentRed)
                            .clipShape(Capsule())
                    }
                }

                Text(localization.isArabic ? "إشعارات دورك وتحديثات الطابور" : "Turn updates & queue notifications")
                    .font(.app(11.5, .medium, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
                    .lineLimit(1)
            }

            Spacer()

            // Trailing Action Cluster
            HStack(spacing: 8) {
                // "قراءة الكل" Mark All Read Button
                if dataService.unreadAlertsCount > 0 {
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        withAnimation(.spring(response: 0.35, dampingFraction: 0.75)) {
                            dataService.markAllAlertsRead()
                        }
                    }) {
                        HStack(spacing: 4) {
                            Image(systemName: "checkmark.circle.fill")
                                .font(.system(size: 13, weight: .semibold))
                            Text(localization.isArabic ? "قراءة الكل" : "Read All")
                                .font(.app(12, .bold, isArabic: localization.isArabic))
                        }
                        .foregroundColor(AppColors.teal)
                        .padding(.horizontal, 10)
                        .frame(height: 38)
                        .background(AppColors.cardBackground)
                        .clipShape(Capsule())
                        .overlay(
                            Capsule().strokeBorder(AppColors.teal.opacity(0.25), lineWidth: 0.8)
                        )
                        .shadow(color: Color.black.opacity(colorScheme == .dark ? 0 : 0.04), radius: 4, x: 0, y: 1.5)
                    }
                    .buttonStyle(ScaleButtonStyle(scale: 0.94, pressedOpacity: 0.85))
                }

                // Clear / Trash Button
                if !dataService.alerts.isEmpty {
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        showClearConfirm = true
                    }) {
                        Image(systemName: "trash")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(AppColors.textSecondary)
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
    }

    // MARK: - 2. Custom Filter Segmented Pills
    private var filterPillsBar: some View {
        HStack(spacing: 8) {
            ForEach([0, 1], id: \.self) { idx in
                let isSelected = filterIndex == idx
                let title = idx == 0
                    ? (localization.isArabic ? "الكل (\(dataService.alerts.count))" : "All (\(dataService.alerts.count))")
                    : (localization.isArabic ? "غير مقروءة (\(dataService.unreadAlertsCount))" : "Unread (\(dataService.unreadAlertsCount))")

                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
                        filterIndex = idx
                    }
                }) {
                    Text(title)
                        .font(.app(13, isSelected ? .bold : .medium, isArabic: localization.isArabic))
                        .foregroundColor(isSelected ? .white : AppColors.textSecondary)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .background(isSelected ? AppColors.teal : AppColors.cardBackground)
                        .clipShape(Capsule())
                        .overlay(
                            Capsule()
                                .strokeBorder(
                                    isSelected ? Color.clear : Color(UIColor.separator).opacity(colorScheme == .dark ? 0.35 : 0.18),
                                    lineWidth: 0.8
                                )
                        )
                        .shadow(color: isSelected ? AppColors.teal.opacity(0.3) : Color.black.opacity(colorScheme == .dark ? 0 : 0.03), radius: isSelected ? 4 : 2, x: 0, y: 1)
                }
                .buttonStyle(ScaleButtonStyle(scale: 0.96, pressedOpacity: 0.85))
            }

            Spacer()
        }
        .padding(.top, 2)
    }

    // MARK: - Section Header Label
    private func sectionHeader(_ title: String) -> some View {
        HStack {
            Text(title)
                .font(.app(12, .bold, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textSecondary)
            Spacer()
        }
        .padding(.horizontal, 4)
        .padding(.top, 4)
    }

    // MARK: - 3. Alert Card
    private func alertCard(alert: AppAlertItem) -> some View {
        Button(action: {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            withAnimation(.spring) {
                dataService.markAlertRead(id: alert.id)
            }
        }) {
            HStack(alignment: .top, spacing: 14) {
                // SF Symbol Icon with Type Accent
                ZStack {
                    Circle()
                        .fill(alert.type.color.opacity(colorScheme == .dark ? 0.22 : 0.12))
                        .frame(width: 42, height: 42)

                    Image(systemName: alert.type.iconName)
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundColor(alert.type.color)
                }

                VStack(alignment: .leading, spacing: 4) {
                    HStack(alignment: .firstTextBaseline) {
                        Text(alert.localizedTitle(isArabic: localization.isArabic))
                            .font(.app(15, alert.isRead ? .medium : .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)
                            .fixedSize(horizontal: false, vertical: true)

                        Spacer(minLength: 8)

                        Text(alert.localizedTimeAgo(isArabic: localization.isArabic))
                            .font(.app(11, .regular, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textTertiary)
                            .fixedSize()
                    }

                    Text(alert.localizedDesc(isArabic: localization.isArabic))
                        .font(.app(13, .regular, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)
                        .lineSpacing(2)
                        .multilineTextAlignment(.leading)
                }

                if !alert.isRead {
                    Circle()
                        .fill(AppColors.teal)
                        .frame(width: 8, height: 8)
                        .padding(.top, 4)
                }
            }
            .padding(14)
            .background(AppColors.cardBackground)
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 16, style: .continuous)
                    .strokeBorder(
                        alert.isRead
                            ? Color(UIColor.separator).opacity(colorScheme == .dark ? 0.35 : 0.15)
                            : AppColors.teal.opacity(0.35),
                        lineWidth: alert.isRead ? 0.8 : 1.2
                    )
            )
            .shadow(color: Color.black.opacity(colorScheme == .dark ? 0 : 0.03), radius: 5, x: 0, y: 2)
        }
        .buttonStyle(ScaleButtonStyle(scale: 0.99, pressedOpacity: 0.95))
        .contextMenu {
            if !alert.isRead {
                Button {
                    dataService.markAlertRead(id: alert.id)
                } label: {
                    Label(localization.isArabic ? "تحديد كمقروء" : "Mark as Read", systemImage: "envelope.open")
                }
            }
            Button(role: .destructive) {
                dataService.deleteAlert(id: alert.id)
            } label: {
                Label(localization.isArabic ? "حذف التنبيه" : "Delete Alert", systemImage: "trash")
            }
        }
    }

    // MARK: - Empty State Card
    private var emptyAlertsCard: some View {
        VStack(spacing: 14) {
            ZStack {
                Circle()
                    .fill(AppColors.teal.opacity(colorScheme == .dark ? 0.20 : 0.10))
                    .frame(width: 64, height: 64)

                Image(systemName: "bell.slash")
                    .font(.system(size: 28, weight: .medium))
                    .foregroundColor(AppColors.teal)
            }

            VStack(spacing: 4) {
                Text(localization.isArabic ? "لا توجد تنبيهات" : "No Alerts")
                    .font(.app(17, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)

                Text(localization.isArabic
                     ? "ستظهر هنا إشعارات اقتراب دورك وتحديثات حالة الطابور لحظة بلحظة."
                     : "You'll be notified here when your turn is approaching and queue status updates.")
                    .font(.app(13, .regular, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
                    .multilineTextAlignment(.center)
                    .lineSpacing(2)
            }
            .padding(.horizontal, 24)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 40)
        .background(AppColors.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .strokeBorder(Color(UIColor.separator).opacity(colorScheme == .dark ? 0.35 : 0.18), lineWidth: 0.8)
        )
        .shadow(color: Color.black.opacity(colorScheme == .dark ? 0 : 0.03), radius: 6, x: 0, y: 2)
        .padding(.top, 12)
    }
}
