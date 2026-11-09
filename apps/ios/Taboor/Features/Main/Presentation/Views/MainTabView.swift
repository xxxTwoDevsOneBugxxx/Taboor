import SwiftUI

public enum TaboorTab: Hashable, CaseIterable {
    case home
    case queue
    case alerts
    case profile

    public var rawIndex: Int {
        switch self {
        case .home: return 0
        case .queue: return 1
        case .alerts: return 2
        case .profile: return 3
        }
    }

    public var iconName: String {
        switch self {
        case .home: return "house.fill"
        case .queue: return "ticket"
        case .alerts: return "bell"
        case .profile: return "person"
        }
    }

    public func title(isArabic: Bool) -> String {
        switch self {
        case .home: return isArabic ? "الرئيسية" : "Home"
        case .queue: return isArabic ? "طوابيري" : "My Queues"
        case .alerts: return isArabic ? "التنبيهات" : "Alerts"
        case .profile: return isArabic ? "حسابي" : "Profile"
        }
    }
}

// MARK: - Native Floating Bottom Dock (Matches Approved Redesign Exactly)

public struct FloatingBottomDockView: View {
    @Binding var selectedTab: TaboorTab
    var animation: Namespace.ID? = nil
    @ObservedObject private var localization = LocalizationManager.shared
    @StateObject private var dataService = QueueDataService.shared

    public init(selectedTab: Binding<TaboorTab>, animation: Namespace.ID? = nil) {
        self._selectedTab = selectedTab
        self.animation = animation
    }

    public var body: some View {
        HStack(spacing: 0) {
            ForEach(TaboorTab.allCases, id: \.self) { tab in
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    withAnimation(.spring(response: 0.32, dampingFraction: 0.8)) {
                        selectedTab = tab
                    }
                }) {
                    VStack(spacing: 4) {
                        ZStack(alignment: .topTrailing) {
                            Image(systemName: tab.iconName)
                                .font(.system(size: 20, weight: selectedTab == tab ? .bold : .medium))
                                .foregroundColor(selectedTab == tab ? AppColors.ink : AppColors.textTertiary)

                            if tab == .alerts && dataService.unreadAlertsCount > 0 {
                                Circle()
                                    .fill(AppColors.amber)
                                    .frame(width: 7, height: 7)
                                    .offset(x: 4, y: -2)
                            }
                        }

                        Text(tab.title(isArabic: localization.isArabic))
                            .font(.app(11, selectedTab == tab ? .bold : .medium, isArabic: localization.isArabic))
                            .foregroundColor(selectedTab == tab ? AppColors.ink : AppColors.textTertiary)

                        // Amber Active Indicator Bar with smooth sliding
                        ZStack {
                            if selectedTab == tab {
                                if let anim = animation {
                                    RoundedRectangle(cornerRadius: 1.5)
                                        .fill(AppColors.amber)
                                        .frame(width: 16, height: 3)
                                        .matchedGeometryEffect(id: "activeTabIndicator", in: anim)
                                } else {
                                    RoundedRectangle(cornerRadius: 1.5)
                                        .fill(AppColors.amber)
                                        .frame(width: 16, height: 3)
                                }
                            } else {
                                Color.clear
                                    .frame(width: 16, height: 3)
                            }
                        }
                        .frame(height: 3)
                    }
                    .frame(maxWidth: .infinity)
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.vertical, 10)
        .background(
            RoundedRectangle(cornerRadius: 32, style: .continuous)
                .fill(Color.white.opacity(0.96))
                .shadow(color: AppColors.ink.opacity(0.12), radius: 24, x: 0, y: 8)
                .overlay(
                    RoundedRectangle(cornerRadius: 32, style: .continuous)
                        .strokeBorder(AppColors.cardBorder, lineWidth: 1)
                )
        )
        .padding(.horizontal, 20)
        .padding(.bottom, 2)
    }
}

struct MainTabView: View {
    let onLogout: () -> Void

    @State private var selectedTab: TaboorTab = .home
    @StateObject private var dataService = QueueDataService.shared
    @ObservedObject private var localization = LocalizationManager.shared
    @Namespace private var tabAnimation

    var body: some View {
        ZStack(alignment: .bottom) {
            // Horizontal Sliding PageView (Matching Mahfazti Exactly: 320ms easeInOutCubic)
            GeometryReader { proxy in
                let width = proxy.size.width

                ZStack {
                    tabContent(tab: .home, width: width) {
                        HomeTabView(
                            selectedTab: $selectedTab,
                            onNavigateToQueue: {
                                selectedTab = .queue
                            },
                            onNavigateToProfile: {
                                selectedTab = .profile
                            }
                        )
                    }

                    tabContent(tab: .queue, width: width) {
                        QueueTabView(selectedTab: $selectedTab)
                    }

                    tabContent(tab: .alerts, width: width) {
                        AlertsTabView(selectedTab: $selectedTab)
                    }

                    tabContent(tab: .profile, width: width) {
                        ProfileTabView(selectedTab: $selectedTab, onLogout: onLogout)
                    }
                }
                .clipped()
                .animation(.timingCurve(0.65, 0, 0.35, 1, duration: 0.32), value: selectedTab)
            }
            .ignoresSafeArea()

            // Single Persistent Floating Bottom Dock
            if !dataService.isTabBarHidden {
                FloatingBottomDockView(selectedTab: $selectedTab, animation: tabAnimation)
                    .padding(.bottom, 20)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .zIndex(100)
            }
        }
        .ignoresSafeArea()
        .environment(\.layoutDirection, localization.layoutDirection)
        .id(localization.currentLanguage)
        .fullScreenCover(isPresented: $dataService.showLiveTracking) {
            LiveQueueTrackingView()
        }
        .sheet(item: $dataService.selectedPlaceFromSpotlight) { place in
            NavigationStack {
                ServiceDetailsView(place: place) {
                    dataService.selectedPlaceFromSpotlight = nil
                    dataService.isTabBarHidden = false
                }
            }
        }
        .onChange(of: dataService.shouldNavigateToQueueTab) { _, shouldNavigate in
            if shouldNavigate {
                selectedTab = .queue
                dataService.isTabBarHidden = false
                dataService.shouldNavigateToQueueTab = false
            }
        }
        .onAppear {
            if let tab = dataService.requestedTab {
                selectedTab = tab
                dataService.requestedTab = nil
            }
        }
        .onChange(of: dataService.requestedTab) { _, tab in
            if let tab = tab {
                selectedTab = tab
                dataService.isTabBarHidden = false
                dataService.requestedTab = nil
            }
        }
    }

    // MARK: - Mahfazti-style Horizontal Page Item
    @ViewBuilder
    private func tabContent<Content: View>(
        tab: TaboorTab,
        width: CGFloat,
        @ViewBuilder content: () -> Content
    ) -> some View {
        let isSelected = selectedTab == tab
        // Clean Mahfazti PageView offset:
        // Moving forward (0 -> 1 -> 2 -> 3) slides in from Right (+width) to Left.
        // Moving backward (3 -> 2 -> 1 -> 0) slides in from Left (-width) to Right.
        let offset = CGFloat(tab.rawIndex - selectedTab.rawIndex) * width

        content()
            .frame(width: width)
            .frame(maxHeight: .infinity)
            .offset(x: offset)
            .allowsHitTesting(isSelected)
    }
}

// MARK: - Native Swipe-to-Back Support with Hidden Navigation Back Button
extension UINavigationController: @retroactive UIGestureRecognizerDelegate {
    override open func viewDidLoad() {
        super.viewDidLoad()
        interactivePopGestureRecognizer?.delegate = self
    }

    public func gestureRecognizerShouldBegin(_ gestureRecognizer: UIGestureRecognizer) -> Bool {
        return viewControllers.count > 1
    }
}
