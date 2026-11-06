import SwiftUI

enum TaboorTab: Hashable {
    case home
    case queue
    case alerts
    case profile
}

// MARK: - Native iOS Edge-to-Edge Bottom Tab Bar Navigation

struct MainTabView: View {
    let onLogout: () -> Void

    @StateObject private var dataService = QueueDataService.shared
    @State private var selectedTab: TaboorTab = .home
    @ObservedObject private var localization = LocalizationManager.shared

    init(onLogout: @escaping () -> Void) {
        self.onLogout = onLogout

        // Standard Apple HIG UITabBar Appearance
        let appearance = UITabBarAppearance()
        appearance.configureWithDefaultBackground()
        appearance.backgroundEffect = UIBlurEffect(style: .systemUltraThinMaterial)
        appearance.shadowColor = UIColor.separator.withAlphaComponent(0.25)

        UITabBar.appearance().standardAppearance = appearance
        UITabBar.appearance().scrollEdgeAppearance = appearance
    }

    private var tabBinding: Binding<TaboorTab> {
        Binding(
            get: { selectedTab },
            set: { newTab in
                if newTab != selectedTab {
                    UISelectionFeedbackGenerator().selectionChanged()
                    selectedTab = newTab
                }
            }
        )
    }

    var body: some View {
        TabView(selection: tabBinding) {
            // 1. Home Tab
            HomeTabView(
                onNavigateToQueue: {
                    selectedTab = .queue
                },
                onNavigateToProfile: {
                    selectedTab = .profile
                }
            )
            .tabItem {
                Image(systemName: selectedTab == .home ? "house.fill" : "house")
                Text(localization.isArabic ? "الرئيسية" : "Home")
            }
            .tag(TaboorTab.home)

            // 2. Queue Tab
            QueueTabView()
            .tabItem {
                Image(systemName: selectedTab == .queue ? "ticket.fill" : "ticket")
                Text(localization.isArabic ? "طابوري" : "My Queue")
            }
            .tag(TaboorTab.queue)

            // 3. Alerts Tab
            AlertsTabView()
            .tabItem {
                Image(systemName: selectedTab == .alerts ? "bell.fill" : "bell")
                Text(localization.isArabic ? "التنبيهات" : "Alerts")
            }
            .tag(TaboorTab.alerts)
            .badge(dataService.unreadAlertsCount)

            // 4. Profile Tab
            ProfileTabView(onLogout: onLogout)
            .tabItem {
                Image(systemName: selectedTab == .profile ? "person.crop.circle.fill" : "person.crop.circle")
                Text(localization.isArabic ? "حسابي" : "Profile")
            }
            .tag(TaboorTab.profile)
        }
        .tint(AppColors.teal)
        .environment(\.layoutDirection, localization.layoutDirection)
        .id(localization.currentLanguage)
    }
}
