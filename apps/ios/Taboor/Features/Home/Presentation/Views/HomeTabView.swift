import SwiftUI
import MapKit

// MARK: - Redesigned Native iOS 26 Home / Dashboard View

public struct HomeTabView: View {
    @Binding var selectedTab: TaboorTab
    public let onNavigateToQueue: () -> Void
    public let onNavigateToProfile: () -> Void

    @StateObject private var dataService = QueueDataService.shared
    @ObservedObject private var localization = LocalizationManager.shared
    @ObservedObject private var locationService = UserLocationService.shared
    @Environment(\.colorScheme) private var colorScheme

    @State private var user: User? = AuthRepository.shared.getSavedUser()
    @State private var searchQuery = ""
    @State private var showQRScanner = false
    @State private var qrBounce = false
    @State private var selectedPlaceForDetails: ServicePlace? = nil
    @State private var navigateToAllServicesWithCategory: ServiceCategory? = nil
    @State private var isNavigatingToAllServices = false
    @State private var selectedLocation: String = "المعادي، شارع النصر"
    @State private var showLocationPickerSheet = false
    @State private var showLiveTracking = false

    // Home Mini-Map Coordinates & Camera
    private var homeUserCoord: CLLocationCoordinate2D {
        locationService.effectiveCoordinate
    }
    private let homeClinicCoord = CLLocationCoordinate2D(latitude: 30.0585, longitude: 31.3465)
    private var homeRouteCoords: [CLLocationCoordinate2D] {
        let start = homeUserCoord
        let end = homeClinicCoord
        let dLat = end.latitude - start.latitude
        let dLng = end.longitude - start.longitude
        return [
            start,
            CLLocationCoordinate2D(latitude: start.latitude + dLat * 0.25 + 0.0008, longitude: start.longitude + dLng * 0.20 - 0.0005),
            CLLocationCoordinate2D(latitude: start.latitude + dLat * 0.50 - 0.0006, longitude: start.longitude + dLng * 0.55 + 0.0007),
            CLLocationCoordinate2D(latitude: start.latitude + dLat * 0.75 + 0.0004, longitude: start.longitude + dLng * 0.80 + 0.0002),
            end
        ]
    }
    @State private var homeMapCamera: MapCameraPosition = .region(
        MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: 30.0520, longitude: 31.3400),
            span: MKCoordinateSpan(latitudeDelta: 0.024, longitudeDelta: 0.024)
        )
    )

    public init(
        selectedTab: Binding<TaboorTab> = .constant(.home),
        onNavigateToQueue: @escaping () -> Void,
        onNavigateToProfile: @escaping () -> Void = {}
    ) {
        self._selectedTab = selectedTab
        self.onNavigateToQueue = onNavigateToQueue
        self.onNavigateToProfile = onNavigateToProfile
    }

    private var fastQueues: [ServicePlace] {
        dataService.places
            .sorted { $0.totalWaiting < $1.totalWaiting }
            .prefix(4)
            .map { $0 }
    }

    private var topRated: [ServicePlace] {
        dataService.places
            .sorted { $0.starRating > $1.starRating }
            .prefix(3)
            .map { $0 }
    }

    public var body: some View {
        NavigationStack {
            ZStack(alignment: .bottom) {
                ScrollView(showsIndicators: false) {
                    VStack(spacing: 18) {
                        // 1. Unified Premium Header Bar (Avatar + Greeting + Location + Action Buttons)
                        topHeaderBar

                        // 2. Interactive Search Bar Card
                        searchBarCard

                        // 3. Dynamic Live Ticket Hero or Booking Invitation
                        ticketHeroCard

                        // 4. Categories Grid
                        categoriesSection

                        // 5. "أسرع الطوابير بالقرب منك" (Fast Queues Near You)
                        fastQueuesSection

                        // 6. "الأعلى تقييماً" (Top Rated Providers)
                        topRatedSection

                        Spacer().frame(height: 130)
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
            .sheet(isPresented: $showQRScanner) {
                QRScannerView(onQueueJoined: {
                    onNavigateToQueue()
                })
            }
            .sheet(item: $selectedPlaceForDetails) { place in
                NavigationStack {
                    ServiceDetailsView(place: place, onQueueJoined: {
                        selectedPlaceForDetails = nil
                        isNavigatingToAllServices = false
                        dataService.isTabBarHidden = false
                        onNavigateToQueue()
                    })
                }
            }
            .sheet(isPresented: $showLocationPickerSheet) {
                LocationPickerSheetView(selectedLocation: $selectedLocation)
            }
            .fullScreenCover(isPresented: $showLiveTracking) {
                LiveQueueTrackingView()
            }
            .navigationDestination(isPresented: $isNavigatingToAllServices) {
                AllServicesView(initialCategory: navigateToAllServicesWithCategory) { place in
                    selectedPlaceForDetails = place
                }
                .onDisappear {
                    dataService.isTabBarHidden = false
                }
            }
            .onChange(of: dataService.shouldNavigateToQueueTab) { _, shouldNavigate in
                if shouldNavigate {
                    selectedPlaceForDetails = nil
                    isNavigatingToAllServices = false
                    showQRScanner = false
                    showLocationPickerSheet = false
                    dataService.isTabBarHidden = false
                    showLiveTracking = true
                    dataService.shouldNavigateToQueueTab = false
                }
            }
            .onChange(of: dataService.showAllServicesScreen) { _, show in
                if show {
                    dataService.isTabBarHidden = true
                    navigateToAllServicesWithCategory = nil
                    isNavigatingToAllServices = true
                    dataService.showAllServicesScreen = false
                }
            }
            .onAppear {
                user = AuthRepository.shared.getSavedUser()
                locationService.requestPermission()
                if locationService.isAuthorized && locationService.userCoordinate != nil {
                    selectedLocation = locationService.currentAddress
                }
            }
            .onChange(of: locationService.currentAddress) { _, newAddr in
                if !newAddr.isEmpty {
                    selectedLocation = newAddr
                }
            }
        }
        .background(AppColors.screenBackground.ignoresSafeArea())
    }

    // MARK: - 1. Brand & Location Header (Matching HTML Native Redesign)
    private var topHeaderBar: some View {
        HStack(alignment: .center, spacing: 10) {
            // Brand Side (Logo + taboor. + Location Picker)
            HStack(spacing: 10) {
                // Taboor Squircles Logo with official "T & ط" SVG vector mark
                TaboorLogoIcon(size: 38)

                VStack(alignment: .leading, spacing: 2) {
                    // Brand Wordmark strictly LTR: "taboor." with Inter Bold font and amber dot
                    HStack(spacing: 0) {
                        Text("taboor")
                            .font(.inter(19, .bold))
                            .tracking(-0.8)
                            .foregroundColor(AppColors.ink)
                        Text(".")
                            .font(.inter(19, .bold))
                            .tracking(-0.8)
                            .foregroundColor(AppColors.amber)
                    }
                    .environment(\.layoutDirection, .leftToRight)

                    Button(action: {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        showLocationPickerSheet = true
                    }) {
                        HStack(spacing: 3) {
                            Image(systemName: "mappin.and.ellipse")
                                .font(.system(size: 10, weight: .bold))
                                .foregroundColor(AppColors.teal)

                            Text(selectedLocation)
                                .font(.app(11.5, .medium, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textSecondary)
                                .lineLimit(1)

                            Image(systemName: "chevron.down")
                                .font(.system(size: 8, weight: .bold))
                                .foregroundColor(AppColors.teal)
                        }
                    }
                    .buttonStyle(.plain)
                }
            }

            Spacer()

            // Actions: QR Scanner & Profile / Notifications
            HStack(spacing: 8) {
                // QR Scanner
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    showQRScanner = true
                }) {
                    Image(systemName: "qrcode.viewfinder")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(AppColors.ink)
                        .frame(width: 38, height: 38)
                        .background(AppColors.cardBackground)
                        .clipShape(RoundedRectangle(cornerRadius: 11, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 11, style: .continuous)
                                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
                        )
                        .shadow(color: Color.black.opacity(0.04), radius: 4, y: 1.5)
                }
                .buttonStyle(ScaleButtonStyle(scale: 0.94, pressedOpacity: 0.85))

                // Profile / Notifications
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    onNavigateToProfile()
                }) {
                    ZStack(alignment: .topTrailing) {
                        Image(systemName: "bell.fill")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(AppColors.ink)
                            .frame(width: 38, height: 38)
                            .background(AppColors.cardBackground)
                            .clipShape(RoundedRectangle(cornerRadius: 11, style: .continuous))
                            .overlay(
                                RoundedRectangle(cornerRadius: 11, style: .continuous)
                                    .strokeBorder(AppColors.cardBorder, lineWidth: 1)
                            )
                            .shadow(color: Color.black.opacity(0.04), radius: 4, y: 1.5)

                        Circle()
                            .fill(AppColors.amber)
                            .frame(width: 8, height: 8)
                            .offset(x: -6, y: 6)
                    }
                }
                .buttonStyle(ScaleButtonStyle(scale: 0.94, pressedOpacity: 0.85))
            }
        }
        .padding(.vertical, 4)
    }

    private func navigateToServices(category: ServiceCategory? = nil) {
        dataService.isTabBarHidden = true
        navigateToAllServicesWithCategory = category
        isNavigatingToAllServices = true
    }

    // MARK: - 2. Quick Search Card (Option 2: Native White with Filter Pill)
    private var searchBarCard: some View {
        Button(action: {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            navigateToServices()
        }) {
            HStack(spacing: 10) {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(AppColors.teal)

                Text(localization.isArabic ? "ابحث عن دكتور، عيادة، صالون، خدمة..." : "Search clinic, doctor, salon, service...")
                    .font(.app(13, .regular, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)

                Spacer()

                Image(systemName: "slider.horizontal.3")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(AppColors.teal)
                    .frame(width: 28, height: 28)
                    .background(AppColors.softTeal)
                    .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
            }
            .padding(.horizontal, 14)
            .frame(height: 46)
            .background(AppColors.cardBackground)
            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .strokeBorder(AppColors.cardBorder, lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.03), radius: 6, y: 2)
        }
        .buttonStyle(ScaleButtonStyle(scale: 0.99, pressedOpacity: 0.95))
    }

    // MARK: - 3. Dynamic Live Ticket Hero Card
    @ViewBuilder
    private var ticketHeroCard: some View {
        if let ticket = dataService.activeTicket {
            activeTicketCard(ticket)
        } else {
            emptyQueueHeroCard
        }
    }

    // Active Ticket Card (Option 2: Uber Trip Tray with Live Route Map)
    private func activeTicketCard(_ ticket: UserTicket) -> some View {
        VStack(spacing: 0) {
            // 1. Top Wide Map View (Tapping opens the full-screen live tracking view!)
            Button(action: {
                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                showLiveTracking = true
            }) {
                uberMapHeaderView(ticket: ticket)
            }
            .buttonStyle(ScaleButtonStyle(scale: 0.99, pressedOpacity: 0.95))

            // Subtle separator line
            Divider()
                .opacity(colorScheme == .dark ? 0.3 : 0.15)

            // 2. Bottom Ticket Tray
            VStack(spacing: 12) {
                // Info row: Clinic info full-width & Ticket pill
                Button(action: {
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    showLiveTracking = true
                }) {
                    VStack(alignment: .leading, spacing: 6) {
                        // Title Full Width (Takes 100% of Card Width)
                        Text(ticket.localizedServiceName(isArabic: localization.isArabic))
                            .font(.app(16.5, .extraBold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .lineLimit(1)
                            .minimumScaleFactor(0.85)

                        // Subtitle & Ticket Pill Row
                        HStack(alignment: .center, spacing: 10) {
                            Text("\(ticket.localizedBranch(isArabic: localization.isArabic)) • \(ticket.localizedCounter(isArabic: localization.isArabic))")
                                .font(.app(12, .medium, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textSecondary)
                                .lineLimit(1)

                            Spacer(minLength: 4)

                            // Compact Ticket Pill
                            HStack(spacing: 4) {
                                Text(localization.isArabic ? "تذكرتك" : "Ticket")
                                    .font(.app(10, .medium, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textSecondary)

                                Text(ticket.ticketNumber)
                                    .font(.system(size: 13.5, weight: .black, design: .rounded))
                                    .foregroundColor(AppColors.teal)
                            }
                            .padding(.horizontal, 10)
                            .padding(.vertical, 4)
                            .background(AppColors.teal.opacity(colorScheme == .dark ? 0.22 : 0.10))
                            .clipShape(Capsule())
                        }
                    }
                }
                .buttonStyle(.plain)

                // Progress Bar
                ProgressView(value: ticket.progress)
                    .tint(AppColors.teal)

                // Stats & CTA Row
                HStack {
                    HStack(spacing: 5) {
                        Circle()
                            .fill(AppColors.amber)
                            .frame(width: 6, height: 6)
                        Text(localization.isArabic
                             ? "أمامك \(ticket.waitingAhead) في الانتظار"
                             : "\(ticket.waitingAhead) ahead of you")
                            .font(.app(12, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.amber)
                    }

                    Spacer()

                    // Primary Live Tracking CTA
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                        showLiveTracking = true
                    }) {
                        HStack(spacing: 5) {
                            Image(systemName: "location.fill")
                                .font(.system(size: 11, weight: .bold))
                            Text(localization.isArabic ? "تتبع مباشر" : "Live Track")
                                .font(.app(12.5, .bold, isArabic: localization.isArabic))
                            Image(systemName: localization.isArabic ? "chevron.left" : "chevron.right")
                                .font(.system(size: 9.5, weight: .bold))
                        }
                        .foregroundColor(.white)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6.5)
                        .background(
                            LinearGradient(
                                colors: [AppColors.teal, AppColors.deepTeal],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .clipShape(Capsule())
                        .shadow(color: AppColors.teal.opacity(0.3), radius: 4, y: 2)
                    }
                    .buttonStyle(ScaleButtonStyle(scale: 0.96, pressedOpacity: 0.85))
                }
            }
            .padding(14)
            .background(AppColors.cardBackground)
        }
        .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 22, style: .continuous)
                .strokeBorder(AppColors.teal.opacity(colorScheme == .dark ? 0.35 : 0.25), lineWidth: 1.2)
        )
        .shadow(color: Color.black.opacity(colorScheme == .dark ? 0.25 : 0.06), radius: 10, y: 4)
    }

    // Top Wide Map View for Active Ticket (Real Apple Maps MapKit)
    private func uberMapHeaderView(ticket: UserTicket) -> some View {
        ZStack {
            // 1. Real Native Apple Map
            Map(position: $homeMapCamera) {
                // Real Route Polyline
                MapPolyline(coordinates: homeRouteCoords)
                    .stroke(
                        AppColors.teal,
                        style: StrokeStyle(lineWidth: 5, lineCap: .round, lineJoin: .round)
                    )

                // User Location Pin
                Annotation(localization.isArabic ? "موقعك" : "You", coordinate: homeUserCoord) {
                    ZStack {
                        Circle()
                            .fill(AppColors.teal.opacity(0.30))
                            .frame(width: 24, height: 24)
                        Circle()
                            .fill(AppColors.teal)
                            .frame(width: 12, height: 12)
                        Circle()
                            .fill(Color.white)
                            .frame(width: 4, height: 4)
                    }
                }

                // Clinic Destination Pin
                Annotation(ticket.localizedServiceName(isArabic: localization.isArabic), coordinate: homeClinicCoord) {
                    VStack(spacing: 0) {
                        ZStack {
                            Circle()
                                .fill(AppColors.deepTeal)
                                .frame(width: 28, height: 28)
                                .overlay(Circle().stroke(Color.white, lineWidth: 2))
                                .shadow(color: Color.black.opacity(0.25), radius: 4, y: 2)

                            Image(systemName: "cross.fill")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(.white)
                        }

                        Image(systemName: "arrowtriangle.down.fill")
                            .font(.system(size: 8))
                            .foregroundColor(AppColors.deepTeal)
                            .offset(y: -2)
                    }
                }
            }
            .mapStyle(.standard(elevation: .realistic))
            .disabled(true) // Allows smooth scrolling of the parent ScrollView without intercepting touch
            .overlay(
                // Subtle gradient overlay for depth
                LinearGradient(
                    colors: [Color.clear, Color.black.opacity(0.08)],
                    startPoint: .top,
                    endPoint: .bottom
                )
            )

            // 2. Floating Live ETA Badge & Expand CTA
            VStack {
                HStack {
                    // Live Status Floating Badge
                    HStack(spacing: 6) {
                        Circle()
                            .fill(AppColors.accentGreen)
                            .frame(width: 7, height: 7)
                            .symbolEffect(.pulse)

                        Text(localization.isArabic ? "دورك خلال \(ticket.estMinutes) دقائق" : "Your turn in ~\(ticket.estMinutes)m")
                            .font(.app(11.5, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                    .background(.ultraThinMaterial)
                    .clipShape(Capsule())
                    .overlay(
                        Capsule().strokeBorder(Color.white.opacity(colorScheme == .dark ? 0.2 : 0.8), lineWidth: 0.8)
                    )
                    .shadow(color: Color.black.opacity(0.08), radius: 4, y: 2)

                    Spacer()

                    // Expand / Live Tracking Hint Button
                    HStack(spacing: 4) {
                        Image(systemName: "map.fill")
                            .font(.system(size: 9.5, weight: .bold))
                        Text(localization.isArabic ? "تتبع مباشر" : "Live Map")
                            .font(.app(11, .bold, isArabic: localization.isArabic))
                    }
                    .foregroundColor(AppColors.teal)
                    .padding(.horizontal, 9)
                    .padding(.vertical, 5)
                    .background(AppColors.cardBackground.opacity(0.92))
                    .clipShape(Capsule())
                    .overlay(
                        Capsule().strokeBorder(AppColors.teal.opacity(0.25), lineWidth: 0.8)
                    )
                    .shadow(color: Color.black.opacity(0.06), radius: 3, y: 1.5)
                }
                .padding(.horizontal, 12)
                .padding(.top, 10)

                Spacer()
            }
        }
        .frame(height: 120)
    }

    // MARK: - Refined Apple HIG Promo & Hero Card (When No Ticket is Active)
    private var emptyQueueHeroCard: some View {
        Button(action: {
            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
            navigateToServices()
        }) {
            VStack(alignment: .leading, spacing: 10) {
                // 1. Radar Live Tag
                HStack(spacing: 6) {
                    Circle()
                        .fill(AppColors.teal)
                        .frame(width: 6, height: 6)
                        .overlay(
                            Circle()
                                .stroke(AppColors.teal.opacity(0.4), lineWidth: 1.5)
                                .scaleEffect(1.6)
                        )
                    
                    Text(localization.isArabic ? "طوابير سريعة بالقرب منك • \(selectedLocation)" : "Fast queues near you • \(selectedLocation)")
                        .font(.app(11, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.teal)
                }
                .padding(.horizontal, 9)
                .padding(.vertical, 4)
                .background(AppColors.teal.opacity(0.10))
                .clipShape(Capsule())

                // 2. Main Row: Title/Description & Radar Vector
                HStack(alignment: .center, spacing: 12) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(localization.isArabic ? "احجز دورك قبل ما توصل" : "Reserve your spot ahead")
                            .font(.app(16, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)
                            .lineLimit(1)

                        Text(localization.isArabic ? "انضم لطابورك عن بُعد، وتابع دورك في الطريق بدون انتظار على الكراسي" : "Join remotely & track your turn on the go without waiting in lines")
                            .font(.app(11.5, .regular, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textSecondary)
                            .lineLimit(2)
                            .lineSpacing(2)

                        // Clean CTA Button
                        HStack(spacing: 5) {
                            Text(localization.isArabic ? "استكشف الطوابير" : "Explore Queues")
                                .font(.app(12, .bold, isArabic: localization.isArabic))
                            Image(systemName: localization.isArabic ? "chevron.left" : "chevron.right")
                                .font(.system(size: 9.5, weight: .bold))
                        }
                        .foregroundColor(.white)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 7)
                        .background(
                            LinearGradient(
                                colors: [AppColors.teal, AppColors.deepTeal],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )
                        .clipShape(Capsule())
                        .shadow(color: AppColors.teal.opacity(0.3), radius: 5, y: 2)
                        .padding(.top, 4)
                    }

                    Spacer(minLength: 4)

                    // 3. Crisp Vector Radar Visual
                    radarVectorIllustration
                        .frame(width: 78, height: 78)
                }

                // 4. Integrated Quick Utility Shortcuts Strip
                Divider().opacity(0.15)
                    .padding(.top, 2)

                HStack(spacing: 8) {
                    // Shortcut 1: Scan QR
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        showQRScanner = true
                    }) {
                        HStack(spacing: 4) {
                            Image(systemName: "qrcode.viewfinder")
                                .font(.system(size: 11, weight: .semibold))
                            Text(localization.isArabic ? "مسح QR الشباك" : "Scan QR")
                                .font(.app(10.5, .bold, isArabic: localization.isArabic))
                        }
                        .foregroundColor(AppColors.textPrimary)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 6)
                        .frame(maxWidth: .infinity)
                        .background(AppColors.screenBackground)
                        .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 8, style: .continuous)
                                .strokeBorder(AppColors.cardBorder, lineWidth: 0.8)
                        )
                    }
                    .buttonStyle(.plain)

                    // Shortcut 2: Fast wait time shortcut
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        if let fastest = fastQueues.first {
                            selectedPlaceForDetails = fastest
                        }
                    }) {
                        HStack(spacing: 4) {
                            Image(systemName: "bolt.fill")
                                .font(.system(size: 10, weight: .bold))
                                .foregroundColor(AppColors.amber)
                            Text(localization.isArabic ? "أقل انتظار: 3 د" : "Min wait: 3m")
                                .font(.app(10.5, .bold, isArabic: localization.isArabic))
                        }
                        .foregroundColor(AppColors.textPrimary)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 6)
                        .frame(maxWidth: .infinity)
                        .background(AppColors.screenBackground)
                        .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 8, style: .continuous)
                                .strokeBorder(AppColors.cardBorder, lineWidth: 0.8)
                        )
                    }
                    .buttonStyle(.plain)

                    // Shortcut 3: Favorites / Map
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        showLocationPickerSheet = true
                    }) {
                        HStack(spacing: 4) {
                            Image(systemName: "map.fill")
                                .font(.system(size: 10, weight: .semibold))
                                .foregroundColor(AppColors.teal)
                            Text(localization.isArabic ? "خريطة الطوابير" : "Map Radar")
                                .font(.app(10.5, .bold, isArabic: localization.isArabic))
                        }
                        .foregroundColor(AppColors.textPrimary)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 6)
                        .frame(maxWidth: .infinity)
                        .background(AppColors.screenBackground)
                        .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 8, style: .continuous)
                                .strokeBorder(AppColors.cardBorder, lineWidth: 0.8)
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(14)
            .background(AppColors.cardBackground)
            .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 20, style: .continuous)
                    .strokeBorder(AppColors.teal.opacity(0.20), lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.04), radius: 8, y: 3)
        }
        .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.96))
    }

    // Vector Radar Graphic (Clean Apple SVG Style)
    private var radarVectorIllustration: some View {
        ZStack {
            // Outer dashed orbit
            Circle()
                .strokeBorder(AppColors.teal.opacity(0.22), style: StrokeStyle(lineWidth: 1.2, dash: [4, 4]))
                .frame(width: 74, height: 74)

            // Mid circle
            Circle()
                .fill(AppColors.teal.opacity(0.05))
                .frame(width: 52, height: 52)
                .overlay(Circle().stroke(AppColors.teal.opacity(0.25), lineWidth: 1))

            // Inner core
            Circle()
                .fill(AppColors.teal.opacity(0.12))
                .frame(width: 28, height: 28)
                .overlay(Circle().stroke(AppColors.teal, lineWidth: 1.5))

            // Center User Pin
            Circle()
                .fill(AppColors.ink)
                .frame(width: 10, height: 10)
                .overlay(Circle().fill(Color.white).frame(width: 4, height: 4))

            // Nearby Place 1 (Teal Clinic Pin)
            ZStack {
                Circle()
                    .fill(AppColors.teal)
                    .frame(width: 16, height: 16)
                    .shadow(color: AppColors.teal.opacity(0.3), radius: 2)
                Image(systemName: "cross.fill")
                    .font(.system(size: 7, weight: .bold))
                    .foregroundColor(.white)
            }
            .offset(x: -22, y: -16)

            // Nearby Place 2 (Amber Salon Pin)
            ZStack {
                Circle()
                    .fill(AppColors.amber)
                    .frame(width: 14, height: 14)
                    .shadow(color: AppColors.amber.opacity(0.3), radius: 2)
                Circle()
                    .fill(Color.white)
                    .frame(width: 4, height: 4)
            }
            .offset(x: 22, y: 18)
        }
    }

    // MARK: - 3. Categories Section
    private var categoriesSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text(localization.isArabic ? "التصنيفات الرئيسية" : "Main Categories")
                    .font(.app(16, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)

                Spacer()

                Button(action: {
                    navigateToServices()
                }) {
                    Text(localization.isArabic ? "عرض الكل" : "See all")
                        .font(.app(13, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.teal)
                }
            }

            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                ForEach(ServiceCategory.allCases) { category in
                    Button(action: {
                        navigateToServices(category: category)
                    }) {
                        HStack(spacing: 10) {
                            ZStack {
                                RoundedRectangle(cornerRadius: 12, style: .continuous)
                                    .fill(AppColors.softTeal)
                                    .frame(width: 38, height: 38)

                                Image(systemName: category.iconName)
                                    .font(.system(size: 17, weight: .semibold))
                                    .foregroundColor(AppColors.ink)
                            }

                            VStack(alignment: .leading, spacing: 2) {
                                Text(category.title(isArabic: localization.isArabic))
                                    .font(.app(13, .bold, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textPrimary)
                                    .lineLimit(2)
                                    .fixedSize(horizontal: false, vertical: true)
                                    .minimumScaleFactor(0.85)

                                let count = dataService.places.filter { $0.category == category }.count
                                Text(localization.isArabic ? "\(count) أماكن" : "\(count) places")
                                    .font(.app(11, .regular, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textSecondary)
                            }

                            Spacer(minLength: 0)
                        }
                        .padding(.horizontal, 10)
                        .padding(.vertical, 10)
                        .frame(maxWidth: .infinity, minHeight: 62, alignment: .leading)
                        .background(AppColors.cardBackground)
                        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 16, style: .continuous)
                                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
                        )
                    }
                    .buttonStyle(ScaleButtonStyle(scale: 0.97, pressedOpacity: 0.9))
                }
            }
        }
    }

    // MARK: - 4. Nearby Live Queues (Vertical List matching HTML Redesign)
    private var fastQueuesSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text(localization.isArabic ? "أسرع الطوابير بالقرب منك" : "Fastest Queues Near You")
                    .font(.app(16, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)

                Spacer()

                Button(action: {
                    navigateToServices()
                }) {
                    Text(localization.isArabic ? "عرض الكل" : "See all")
                        .font(.app(13, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.teal)
                }
            }

            VStack(spacing: 12) {
                ForEach(fastQueues) { place in
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        selectedPlaceForDetails = place
                    }) {
                        HStack(alignment: .top, spacing: 12) {
                            // Vector Thumbnail (Aligned to Top)
                            ZStack {
                                RoundedRectangle(cornerRadius: 14, style: .continuous)
                                    .fill(AppColors.softTeal)
                                    .frame(width: 50, height: 50)
                                Image(systemName: place.iconName)
                                    .font(.system(size: 20, weight: .semibold))
                                    .foregroundColor(AppColors.ink)
                            }

                            // Details Body (Full sentences visible without truncation)
                            VStack(alignment: .leading, spacing: 5) {
                                // Title & Rating Row
                                HStack(alignment: .top, spacing: 8) {
                                    Text(place.localizedName(isArabic: localization.isArabic))
                                        .font(.app(14.5, .bold, isArabic: localization.isArabic))
                                        .foregroundColor(AppColors.textPrimary)
                                        .lineLimit(2)
                                        .lineSpacing(2)
                                        .fixedSize(horizontal: false, vertical: true)
                                        .multilineTextAlignment(.leading)

                                    Spacer(minLength: 4)

                                    // Rating Pill
                                    HStack(spacing: 2) {
                                        Image(systemName: "star.fill")
                                            .font(.system(size: 9))
                                            .foregroundColor(AppColors.amber)
                                        Text(String(format: "%.1f", place.starRating))
                                            .font(.system(size: 11, weight: .bold))
                                            .foregroundColor(AppColors.textPrimary)
                                    }
                                    .padding(.horizontal, 7)
                                    .padding(.vertical, 3)
                                    .background(AppColors.amber.opacity(0.12))
                                    .clipShape(Capsule())
                                }

                                // Branch & Distance (Wraps cleanly onto next line if long)
                                if let branch = place.primaryBranch {
                                    Text("\(branch.localizedLabel(isArabic: localization.isArabic)) • \(branch.distanceKm) كم")
                                        .font(.app(12, .medium, isArabic: localization.isArabic))
                                        .foregroundColor(AppColors.textSecondary)
                                        .lineLimit(2)
                                        .lineSpacing(1.5)
                                        .fixedSize(horizontal: false, vertical: true)
                                        .multilineTextAlignment(.leading)
                                }

                                // Tags Row (Queue count & ETA)
                                HStack(spacing: 6) {
                                    Text(localization.isArabic ? "\(place.totalWaiting) انتظار" : "\(place.totalWaiting) in line")
                                        .font(.app(11, .bold, isArabic: localization.isArabic))
                                        .foregroundColor(AppColors.ink)
                                        .padding(.horizontal, 8)
                                        .padding(.vertical, 3)
                                        .background(AppColors.softTeal)
                                        .clipShape(RoundedRectangle(cornerRadius: 6, style: .continuous))

                                    Text(localization.isArabic ? "~\(place.primaryBranch?.estWaitMinutes ?? 5) دقائق" : "~\(place.primaryBranch?.estWaitMinutes ?? 5)m")
                                        .font(.app(11, .bold, isArabic: localization.isArabic))
                                        .foregroundColor(AppColors.amber)
                                        .padding(.horizontal, 8)
                                        .padding(.vertical, 3)
                                        .background(AppColors.amber.opacity(0.12))
                                        .clipShape(RoundedRectangle(cornerRadius: 6, style: .continuous))
                                }
                                .padding(.top, 2)
                            }
                        }
                        .padding(14)
                        .background(AppColors.cardBackground)
                        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 18, style: .continuous)
                                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
                        )
                        .shadow(color: Color.black.opacity(0.03), radius: 6, y: 2)
                    }
                    .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.94))
                }
            }
        }
    }

    // MARK: - 5. Top Rated Section (Refined Apple HIG Standard)
    private var topRatedSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                HStack(spacing: 6) {
                    Image(systemName: "star.fill")
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(AppColors.amber)

                    Text(localization.isArabic ? "الأعلى تقييماً" : "Top Rated")
                        .font(.app(16, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textPrimary)
                }

                Spacer()

                Button(action: {
                    navigateToServices()
                }) {
                    Text(localization.isArabic ? "عرض الكل" : "See all")
                        .font(.app(13, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.teal)
                }
            }

            VStack(spacing: 12) {
                ForEach(topRated) { place in
                    topRatedCard(place: place)
                }
            }
        }
    }

    private func topRatedCard(place: ServicePlace) -> some View {
        Button(action: {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            selectedPlaceForDetails = place
        }) {
            VStack(alignment: .leading, spacing: 10) {
                // Top Row: Place Icon + Full Title & Verified Seal
                HStack(alignment: .top, spacing: 12) {
                    // Vector Thumbnail (Soft Teal Background)
                    ZStack {
                        RoundedRectangle(cornerRadius: 14, style: .continuous)
                            .fill(AppColors.softTeal)
                            .frame(width: 50, height: 50)
                        Image(systemName: place.iconName)
                            .font(.system(size: 20, weight: .semibold))
                            .foregroundColor(AppColors.ink)
                    }

                    // Place Details (No Truncation)
                    VStack(alignment: .leading, spacing: 4) {
                        HStack(alignment: .top, spacing: 6) {
                            Text(place.localizedName(isArabic: localization.isArabic))
                                .font(.app(14.5, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textPrimary)
                                .lineLimit(2)
                                .lineSpacing(2)
                                .fixedSize(horizontal: false, vertical: true)
                                .multilineTextAlignment(.leading)

                            Spacer(minLength: 4)

                            // Verified Seal
                            Image(systemName: "checkmark.seal.fill")
                                .font(.system(size: 13, weight: .semibold))
                                .foregroundColor(AppColors.teal)
                        }

                        if let branch = place.primaryBranch {
                            Text("\(branch.localizedLabel(isArabic: localization.isArabic)) • \(branch.distanceKm) كم")
                                .font(.app(12, .medium, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textSecondary)
                                .lineLimit(1)
                        }
                    }
                }

                Divider().opacity(0.15)

                // Bottom Row: Rating Badge + Live Wait Pill + Quick Book Button
                HStack(alignment: .center) {
                    // Golden Star Rating
                    HStack(spacing: 3) {
                        Image(systemName: "star.fill")
                            .font(.system(size: 10))
                            .foregroundColor(AppColors.amber)

                        Text(String(format: "%.1f", place.starRating))
                            .font(.system(size: 11.5, weight: .bold))
                            .foregroundColor(AppColors.textPrimary)

                        Text(localization.isArabic ? "(ممتاز)" : "(Top)")
                            .font(.app(10.5, .medium, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.amber)
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(AppColors.amber.opacity(0.12))
                    .clipShape(Capsule())

                    // Live Wait Status
                    HStack(spacing: 4) {
                        Circle()
                            .fill(place.totalWaiting <= 2 ? AppColors.accentGreen : AppColors.amber)
                            .frame(width: 6, height: 6)

                        Text(localization.isArabic ? "\(place.totalWaiting) انتظار" : "\(place.totalWaiting) in line")
                            .font(.app(11, .bold, isArabic: localization.isArabic))
                            .foregroundColor(place.totalWaiting <= 2 ? AppColors.accentGreen : AppColors.amber)
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(AppColors.softTeal)
                    .clipShape(Capsule())

                    Spacer()

                    // Quick CTA Pill
                    HStack(spacing: 4) {
                        Text(localization.isArabic ? "حجز دور" : "Join")
                            .font(.app(11.5, .bold, isArabic: localization.isArabic))
                        Image(systemName: localization.isArabic ? "chevron.left" : "chevron.right")
                            .font(.system(size: 9, weight: .bold))
                    }
                    .foregroundColor(AppColors.teal)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                    .background(AppColors.teal.opacity(0.10))
                    .clipShape(Capsule())
                }
            }
            .padding(14)
            .background(AppColors.cardBackground)
            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 18, style: .continuous)
                    .strokeBorder(AppColors.cardBorder, lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.03), radius: 6, y: 2)
        }
        .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.94))
    }
}

// MARK: - Interactive Apple Map Location Picker Sheet (Uber / Careem / Apple HIG)
public struct LocationPickerSheetView: View {
    @Binding var selectedLocation: String
    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var colorScheme
    @ObservedObject private var localization = LocalizationManager.shared

    struct DistrictLocation: Identifiable {
        let id = UUID()
        let nameAr: String
        let nameEn: String
        let cityAr: String
        let cityEn: String
        let latitude: Double
        let longitude: Double

        var coordinate: CLLocationCoordinate2D {
            CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
        }
    }

    private let districts: [DistrictLocation] = [
        DistrictLocation(nameAr: "المعادي، شارع النصر", nameEn: "Maadi, El-Nasr St", cityAr: "القاهرة", cityEn: "Cairo", latitude: 29.9602, longitude: 31.2569),
        DistrictLocation(nameAr: "مدينة نصر، عباس العقاد", nameEn: "Nasr City, Abbas El-Akkad", cityAr: "القاهرة", cityEn: "Cairo", latitude: 30.0561, longitude: 31.3414),
        DistrictLocation(nameAr: "التجمع الخامس، شارع التسعين", nameEn: "New Cairo, 90th St", cityAr: "القاهرة الجديدة", cityEn: "New Cairo", latitude: 30.0197, longitude: 31.4289),
        DistrictLocation(nameAr: "مصر الجديدة، الكوربة", nameEn: "Heliopolis, Korba", cityAr: "القاهرة", cityEn: "Cairo", latitude: 30.0898, longitude: 31.3285),
        DistrictLocation(nameAr: "الشيخ زايد، وصلة دهشور", nameEn: "Sheikh Zayed, Dahshur Link", cityAr: "الجيزة", cityEn: "Giza", latitude: 30.0468, longitude: 30.9850),
        DistrictLocation(nameAr: "الدقي، شارع مصدق", nameEn: "Dokki, Mossadak St", cityAr: "الجيزة", cityEn: "Giza", latitude: 30.0384, longitude: 31.2114),
        DistrictLocation(nameAr: "سموحة، فيكتور عمانويل", nameEn: "Smouha, Victor Emmanuel", cityAr: "الإسكندرية", cityEn: "Alexandria", latitude: 31.2156, longitude: 29.9553)
    ]

    @State private var cameraPosition: MapCameraPosition = .region(
        MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: 29.9602, longitude: 31.2569),
            span: MKCoordinateSpan(latitudeDelta: 0.018, longitudeDelta: 0.018)
        )
    )

    @State private var activeDistrictName: String = "المعادي، شارع النصر"
    @State private var activeCityName: String = "القاهرة"
    @State private var activeLatitude: Double = 29.9602
    @State private var activeLongitude: Double = 31.2569
    @State private var searchQuery: String = ""
    @State private var isGpsPulsing: Bool = false

    private var filteredDistricts: [DistrictLocation] {
        if searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
            return districts
        }
        return districts.filter {
            $0.nameAr.localizedCaseInsensitiveContains(searchQuery) ||
            $0.nameEn.localizedCaseInsensitiveContains(searchQuery) ||
            $0.cityAr.localizedCaseInsensitiveContains(searchQuery) ||
            $0.cityEn.localizedCaseInsensitiveContains(searchQuery)
        }
    }

    public var body: some View {
        NavigationStack {
            ZStack {
                // 1. Interactive Apple Map
                Map(position: $cameraPosition) {
                    Marker(
                        activeDistrictName,
                        systemImage: "figure.walk",
                        coordinate: CLLocationCoordinate2D(latitude: activeLatitude, longitude: activeLongitude)
                    )
                    .tint(AppColors.teal)
                }
                .mapStyle(.standard(elevation: .realistic))
                .ignoresSafeArea(edges: .bottom)

                // 2. Central Floating Overlay & Controls
                VStack(spacing: 12) {
                    // Search Bar over Map
                    HStack(spacing: 10) {
                        Image(systemName: "magnifyingglass")
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(Color(UIColor.secondaryLabel))

                        TextField(localization.isArabic ? "ابحث عن منطقة، شارع، علامة مميزة..." : "Search area, street, landmark...", text: $searchQuery)
                            .font(.app(14, .regular, isArabic: localization.isArabic))

                        if !searchQuery.isEmpty {
                            Button(action: { searchQuery = "" }) {
                                Image(systemName: "xmark.circle.fill")
                                    .foregroundColor(Color(UIColor.tertiaryLabel))
                            }
                        }
                    }
                    .padding(.horizontal, 14)
                    .padding(.vertical, 11)
                    .background(.ultraThinMaterial)
                    .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                    .overlay(
                        RoundedRectangle(cornerRadius: 14, style: .continuous)
                            .strokeBorder(Color.white.opacity(0.35), lineWidth: 0.8)
                    )
                    .shadow(color: .black.opacity(0.08), radius: 8, x: 0, y: 3)
                    .padding(.horizontal, 16)
                    .padding(.top, 8)

                    // Quick Area Chips Horizontal Scroll
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(filteredDistricts) { district in
                                let isSelected = activeDistrictName == (localization.isArabic ? district.nameAr : district.nameEn)
                                Button(action: {
                                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                    withAnimation(.spring(response: 0.45, dampingFraction: 0.8)) {
                                        activeDistrictName = localization.isArabic ? district.nameAr : district.nameEn
                                        activeCityName = localization.isArabic ? district.cityAr : district.cityEn
                                        activeLatitude = district.latitude
                                        activeLongitude = district.longitude
                                        cameraPosition = .region(
                                            MKCoordinateRegion(
                                                center: district.coordinate,
                                                span: MKCoordinateSpan(latitudeDelta: 0.018, longitudeDelta: 0.018)
                                            )
                                        )
                                    }
                                }) {
                                    HStack(spacing: 6) {
                                        if isSelected {
                                            Circle()
                                                .fill(Color.white)
                                                .frame(width: 6, height: 6)
                                        }
                                        Text(localization.isArabic ? district.nameAr : district.nameEn)
                                            .font(.app(12.5, isSelected ? .bold : .medium, isArabic: localization.isArabic))
                                    }
                                    .foregroundColor(isSelected ? .white : AppColors.textPrimary)
                                    .padding(.horizontal, 14)
                                    .padding(.vertical, 8)
                                    .background(isSelected ? AppColors.teal : AppColors.cardBackground)
                                    .clipShape(Capsule())
                                    .overlay(
                                        Capsule()
                                            .strokeBorder(isSelected ? Color.clear : Color(UIColor.separator).opacity(0.2), lineWidth: 0.8)
                                    )
                                    .shadow(color: .black.opacity(isSelected ? 0.15 : 0.04), radius: 4, x: 0, y: 2)
                                }
                                .buttonStyle(ScaleButtonStyle(scale: 0.95, pressedOpacity: 0.85))
                            }
                        }
                        .padding(.horizontal, 16)
                    }

                    Spacer()

                    // Floating GPS Re-Center Button
                    HStack {
                        Spacer()
                        Button(action: {
                            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                            isGpsPulsing = true
                            UserLocationService.shared.requestPermission()
                            UserLocationService.shared.startTracking()
                            let coord = UserLocationService.shared.effectiveCoordinate
                            withAnimation(.spring(response: 0.45, dampingFraction: 0.8)) {
                                activeLatitude = coord.latitude
                                activeLongitude = coord.longitude
                                activeDistrictName = UserLocationService.shared.currentAddress
                                activeCityName = UserLocationService.shared.currentArea
                                cameraPosition = .region(
                                    MKCoordinateRegion(
                                        center: coord,
                                        span: MKCoordinateSpan(latitudeDelta: 0.018, longitudeDelta: 0.018)
                                    )
                                )
                            }
                        }) {
                            ZStack {
                                Circle()
                                    .fill(AppColors.cardBackground)
                                    .frame(width: 46, height: 46)
                                    .shadow(color: .black.opacity(0.12), radius: 6, x: 0, y: 3)
                                    .overlay(
                                        Circle()
                                            .strokeBorder(Color(UIColor.separator).opacity(0.2), lineWidth: 0.8)
                                    )

                                Image(systemName: "location.fill")
                                    .font(.system(size: 19, weight: .bold))
                                    .foregroundColor(AppColors.teal)
                            }
                        }
                        .buttonStyle(ScaleButtonStyle(scale: 0.92, pressedOpacity: 0.85))
                        .padding(.trailing, 16)
                    }

                    // Bottom Confirmation Card (Uber / Careem Style)
                    VStack(spacing: 14) {
                        HStack(spacing: 12) {
                            ZStack {
                                RoundedRectangle(cornerRadius: 12, style: .continuous)
                                    .fill(AppColors.teal.opacity(0.12))
                                    .frame(width: 44, height: 44)

                                Image(systemName: "mappin.and.ellipse")
                                    .font(.system(size: 20, weight: .bold))
                                    .foregroundColor(AppColors.teal)
                            }

                            VStack(alignment: .leading, spacing: 3) {
                                Text(activeDistrictName)
                                    .font(.app(16, .bold, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textPrimary)

                                Text("\(activeCityName) • " + (localization.isArabic ? "موقعك المحدد لأقرب الطوابير" : "Selected for nearest queues"))
                                    .font(.app(12, .medium, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textSecondary)
                            }

                            Spacer()
                        }

                        // Primary Confirmation CTA
                        Button(action: {
                            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                            selectedLocation = activeDistrictName
                            dismiss()
                        }) {
                            HStack(spacing: 8) {
                                Image(systemName: "checkmark")
                                    .font(.system(size: 14, weight: .bold))
                                Text(localization.isArabic ? "تأكيد هذا الموقع" : "Confirm Location")
                                    .font(.app(15, .bold, isArabic: localization.isArabic))
                            }
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .frame(height: 50)
                            .background(
                                LinearGradient(
                                    colors: [AppColors.teal, AppColors.deepTeal],
                                    startPoint: .leading,
                                    endPoint: .trailing
                                )
                            )
                            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                            .shadow(color: AppColors.teal.opacity(0.35), radius: 8, x: 0, y: 4)
                        }
                        .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.9))
                    }
                    .padding(18)
                    .background(AppColors.cardBackground)
                    .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
                    .overlay(
                        RoundedRectangle(cornerRadius: 22, style: .continuous)
                            .strokeBorder(Color(UIColor.separator).opacity(colorScheme == .dark ? 0.35 : 0.15), lineWidth: 0.8)
                    )
                    .shadow(color: .black.opacity(colorScheme == .dark ? 0.3 : 0.08), radius: 12, x: 0, y: 4)
                    .padding(.horizontal, 16)
                    .padding(.bottom, 12)
                }
            }
            .navigationTitle(localization.isArabic ? "تحديد الموقع على الخريطة" : "Pin Location on Map")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(localization.isArabic ? "إلغاء" : "Cancel") {
                        dismiss()
                    }
                }
            }
            .onAppear {
                isGpsPulsing = true
                activeDistrictName = selectedLocation
            }
        }
        .presentationDetents([.large])
        .presentationDragIndicator(.visible)
    }
}
