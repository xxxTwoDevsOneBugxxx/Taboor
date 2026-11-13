import SwiftUI
import MapKit

// MARK: - Service Details Screen with Real Interactive Apple Map, Branch Selection, and Queue Joining

public struct ServiceDetailsView: View {
    public let place: ServicePlace
    public let onQueueJoined: () -> Void

    @StateObject private var dataService = QueueDataService.shared
    @ObservedObject private var localization = LocalizationManager.shared
    @ObservedObject private var userLocationService = UserLocationService.shared
    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var colorScheme

    @State private var selectedBranchIndex: Int = 0
    @State private var isJoining: Bool = false
    @State private var showSuccessModal: Bool = false
    @State private var issuedTicketNumber: String = ""
    @State private var showNavigationAppDialog: Bool = false
    @State private var cameraPosition: MapCameraPosition
    @State private var routeCoordinates: [CLLocationCoordinate2D]
    @State private var calculatedRouteInfo: CalculatedRouteInfo? = nil
    @State private var isCalculatingRoute: Bool = false

    // Booking Mode & Slot Selection
    @State private var bookingMode: TicketBookingType = .liveQueue
    @State private var selectedDayIndex: Int = 0
    @State private var selectedSlot: AppointmentSlot? = nil
    @State private var showAlreadyBookedAlert: Bool = false
    @State private var showMaxTicketsAlert: Bool = false

    private func slots(for day: Int) -> [AppointmentSlot] {
        switch day {
        case 0: // اليوم (Today)
            return [
                AppointmentSlot(time: "03:30 م", timeEn: "03:30 PM", isBooked: true),
                AppointmentSlot(time: "04:00 م", timeEn: "04:00 PM", isBooked: true),
                AppointmentSlot(time: "04:30 م", timeEn: "04:30 PM", isBooked: true),
                AppointmentSlot(time: "05:00 م", timeEn: "05:00 PM", isBooked: false, isRecommended: true),
                AppointmentSlot(time: "05:30 م", timeEn: "05:30 PM", isBooked: false),
                AppointmentSlot(time: "06:00 م", timeEn: "06:00 PM", isBooked: false),
                AppointmentSlot(time: "06:30 م", timeEn: "06:30 PM", isBooked: false),
                AppointmentSlot(time: "07:00 م", timeEn: "07:00 PM", isBooked: false)
            ]
        case 1: // غداً (Tomorrow)
            return [
                AppointmentSlot(time: "11:00 ص", timeEn: "11:00 AM", isBooked: false, isRecommended: true),
                AppointmentSlot(time: "12:00 م", timeEn: "12:00 PM", isBooked: true),
                AppointmentSlot(time: "01:30 م", timeEn: "01:30 PM", isBooked: false),
                AppointmentSlot(time: "03:00 م", timeEn: "03:00 PM", isBooked: true),
                AppointmentSlot(time: "04:00 م", timeEn: "04:00 PM", isBooked: false),
                AppointmentSlot(time: "05:00 م", timeEn: "05:00 PM", isBooked: false),
                AppointmentSlot(time: "06:30 م", timeEn: "06:30 PM", isBooked: false),
                AppointmentSlot(time: "08:00 م", timeEn: "08:00 PM", isBooked: false)
            ]
        default: // بعد غد (Day after tomorrow)
            return [
                AppointmentSlot(time: "10:30 ص", timeEn: "10:30 AM", isBooked: false, isRecommended: true),
                AppointmentSlot(time: "11:30 ص", timeEn: "11:30 AM", isBooked: false),
                AppointmentSlot(time: "01:00 م", timeEn: "01:00 PM", isBooked: false),
                AppointmentSlot(time: "02:30 م", timeEn: "02:30 PM", isBooked: false),
                AppointmentSlot(time: "04:00 م", timeEn: "04:00 PM", isBooked: false),
                AppointmentSlot(time: "05:30 م", timeEn: "05:30 PM", isBooked: false),
                AppointmentSlot(time: "07:00 م", timeEn: "07:00 PM", isBooked: false),
                AppointmentSlot(time: "08:30 م", timeEn: "08:30 PM", isBooked: false)
            ]
        }
    }

    private var availableSlots: [AppointmentSlot] {
        slots(for: selectedDayIndex)
    }

    // User Location (Dynamic from CoreLocation GPS)
    private var userLocation: CLLocationCoordinate2D {
        userLocationService.effectiveCoordinate
    }

    private var estimatedDriveMinutes: Int {
        if let calculatedRouteInfo {
            return max(1, Int(ceil(calculatedRouteInfo.travelTimeSeconds / 60.0)))
        }
        return currentBranch.realEstimatedDriveMinutes
    }

    private var displayDistanceString: String {
        if let calculatedRouteInfo {
            return calculatedRouteInfo.formattedDistance(isArabic: localization.isArabic)
        }
        return currentBranch.realDistanceFormatted(isArabic: localization.isArabic)
    }

    public init(place: ServicePlace, onQueueJoined: @escaping () -> Void) {
        self.place = place
        self.onQueueJoined = onQueueJoined

        let userCoord = UserLocationService.shared.effectiveCoordinate
        let initialBranch = place.nearestBranch(to: userCoord) ?? place.branches.first
        let initialIndex = place.branches.firstIndex(where: { $0.id == initialBranch?.id }) ?? 0

        _selectedBranchIndex = State(initialValue: initialIndex)
        if let branch = initialBranch {
            _routeCoordinates = State(initialValue: [userCoord, branch.coordinate])
            let midLat = (userCoord.latitude + branch.lat) / 2
            let midLng = (userCoord.longitude + branch.lng) / 2
            let span = max(abs(userCoord.latitude - branch.lat), abs(userCoord.longitude - branch.lng)) * 1.5 + 0.04
            _cameraPosition = State(initialValue: .region(MKCoordinateRegion(
                center: CLLocationCoordinate2D(latitude: midLat, longitude: midLng),
                span: MKCoordinateSpan(latitudeDelta: max(0.04, span), longitudeDelta: max(0.04, span))
            )))
        } else {
            _routeCoordinates = State(initialValue: [])
            _cameraPosition = State(initialValue: .automatic)
        }
    }

    private var currentBranch: ServiceBranch {
        if selectedBranchIndex < place.branches.count {
            return place.branches[selectedBranchIndex]
        }
        return place.branches.first ?? ServiceBranch(
            label: "Main",
            labelAr: "الرئيسي",
            distanceKm: "1.0",
            waiting: 0,
            address: "",
            addressAr: "",
            lat: 30.0525,
            lng: 31.2408
        )
    }

    public var body: some View {
        ZStack(alignment: .bottom) {
            ScrollView(showsIndicators: false) {
                VStack(spacing: 18) {
                    // 1. Real Interactive Apple Map at the Top
                    heroMapSection

                    // 2. Service Header & Rating Identity Card
                    serviceHeaderCard

                    // 3. Quick Real-Time Metrics Strip
                    quickMetricsStrip

                    // 4. Branch Selector Section with Live Queue Counts
                    branchSelectorSection

                    // 4.5. Booking Mode (Live Queue vs Scheduled Appointment Slots)
                    bookingModeSection

                    // 5. About Service Card
                    aboutSection

                    // 6. Working Hours & Open Status Banner
                    workingHoursSection

                    // Bottom Spacing for Floating Glass CTA Bar
                    Spacer().frame(height: 100)
                }
                .padding(.horizontal, 16)
                .padding(.top, 10)
            }

            // Floating Bottom Glass Action Bar
            bottomActionBar
        }
        .background(AppColors.screenBackground.ignoresSafeArea())
        .navigationTitle(place.localizedName(isArabic: localization.isArabic))
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(.ultraThinMaterial, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button(action: {
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    withAnimation(.spring(response: 0.35, dampingFraction: 0.75)) {
                        dataService.toggleFavorite(placeId: place.id)
                    }
                }) {
                    Image(systemName: dataService.isFavorite(placeId: place.id) ? "heart.fill" : "heart")
                        .foregroundColor(dataService.isFavorite(placeId: place.id) ? .pink : AppColors.ink)
                        .font(.system(size: 16, weight: .semibold))
                }
            }

            ToolbarItem(placement: .topBarTrailing) {
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    dismiss()
                }) {
                    Image(systemName: "xmark")
                }
            }
        }
        .id(localization.currentLanguage)
        .onAppear {
            Task {
                await fetchRouteForCurrentBranch()
            }
        }
        .onChange(of: place.id) { _, _ in
            let userCoord = userLocationService.effectiveCoordinate
            let nearest = place.nearestBranch(to: userCoord) ?? place.branches.first
            selectedBranchIndex = place.branches.firstIndex(where: { $0.id == nearest?.id }) ?? 0
            Task {
                await fetchRouteForCurrentBranch()
            }
        }
        .alert(
            localization.isArabic ? "اختر تطبيق الملاحة والخرائط" : "Choose Navigation App",
            isPresented: $showNavigationAppDialog
        ) {
            Button(localization.isArabic ? "خرائط آبل (Apple Maps)" : "Apple Maps") {
                let name = place.localizedName(isArabic: localization.isArabic)
                NavigationLauncher.shared.open(app: .appleMaps, latitude: currentBranch.lat, longitude: currentBranch.lng, destinationName: name)
            }

            Button(localization.isArabic ? "خرائط جوجل (Google Maps)" : "Google Maps") {
                let name = place.localizedName(isArabic: localization.isArabic)
                NavigationLauncher.shared.open(app: .googleMaps, latitude: currentBranch.lat, longitude: currentBranch.lng, destinationName: name)
            }

            Button(localization.isArabic ? "إلغاء" : "Cancel", role: .cancel) {}
        } message: {
            let msg = localization.isArabic
                 ? "اختر التطبيق لبدء التوجيه الصوتي المباشر بالـ GPS."
                 : "Select an app for turn-by-turn voice GPS navigation."
            Text(msg)
        }
        .alert(
            localization.isArabic ? "لديك تذكرة نشطة بالفعل" : "Active Ticket Exists",
            isPresented: $showAlreadyBookedAlert
        ) {
            Button(localization.isArabic ? "عرض تذكرتي" : "View My Ticket") {
                dismiss()
                onQueueJoined()
                dataService.navigateToQueueTab()
            }
            Button(localization.isArabic ? "حسناً" : "OK", role: .cancel) {}
        } message: {
            Text(localization.isArabic
                 ? "أنت مسجل حالياً في طابور \(place.nameAr). لا يمكن حجز أكثر من دور لنفس المكان في وقت واحد."
                 : "You already have an active ticket at \(place.name). You cannot book multiple tickets for the same place.")
        }
        .alert(
            localization.isArabic ? "وصلت للحد الأقصى من الطوابير" : "Maximum Active Queues",
            isPresented: $showMaxTicketsAlert
        ) {
            Button(localization.isArabic ? "إدارة طوابيري" : "Manage Queues") {
                dismiss()
                onQueueJoined()
                dataService.navigateToQueueTab()
            }
            Button(localization.isArabic ? "حسناً" : "OK", role: .cancel) {}
        } message: {
            Text(localization.isArabic
                 ? "لديك 3 طوابير/مواعيد نشطة حالياً. يرجى إتمام أو إلغاء أحدها قبل إضافة حجز جديد."
                 : "You have reached the maximum limit of 3 active queues/appointments. Please complete or cancel one before booking.")
        }
        .sheet(isPresented: $showSuccessModal) {
            ticketSuccessSheet
        }
        .task(id: "\(currentBranch.id),\(userLocation.latitude),\(userLocation.longitude)") {
            await fetchRouteForCurrentBranch()
        }
    }

    // MARK: - 1. Real Interactive Apple Map Section
    private var heroMapSection: some View {
        ZStack(alignment: .topTrailing) {
            Map(position: $cameraPosition) {
                // Real Turn-by-Turn Driving Route along Bridges & Streets
                if !routeCoordinates.isEmpty {
                    MapPolyline(coordinates: routeCoordinates)
                        .stroke(
                            AppColors.teal,
                            style: StrokeStyle(lineWidth: 5.5, lineCap: .round, lineJoin: .round)
                        )
                }

                // User Location Annotation (Fixed Center Anchor)
                Annotation(localization.isArabic ? "موقعك" : "You", coordinate: userLocation, anchor: .center) {
                    ZStack {
                        Circle()
                            .fill(AppColors.teal.opacity(0.20))
                            .frame(width: 26, height: 26)
                        Circle()
                            .fill(Color.white)
                            .frame(width: 14, height: 14)
                            .shadow(color: Color.black.opacity(0.15), radius: 3, y: 1)
                        Circle()
                            .fill(AppColors.teal)
                            .frame(width: 10, height: 10)
                    }
                }

                // Place Branches Annotations (Fixed Bottom Anchor to Pin Pointer Tip)
                ForEach(Array(place.branches.enumerated()), id: \.element.id) { index, branch in
                    Annotation(
                        branch.localizedLabel(isArabic: localization.isArabic),
                        coordinate: CLLocationCoordinate2D(latitude: branch.lat, longitude: branch.lng),
                        anchor: .bottom
                    ) {
                        BranchMapAnnotationPin(
                            place: place,
                            branch: branch,
                            isSelected: selectedBranchIndex == index,
                            isArabic: localization.isArabic,
                            onTap: {
                                selectBranch(at: index)
                            }
                        )
                    }
                }
            }
            .mapStyle(.standard(elevation: .realistic))
            .frame(height: 265)
            .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
            .overlay(
                // Subtle bottom vignette for depth
                LinearGradient(
                    colors: [Color.clear, Color.black.opacity(0.10)],
                    startPoint: .top,
                    endPoint: .bottom
                )
                .allowsHitTesting(false)
            )

            // Floating Glass Controls (Apple Maps directions, ETA, & Recenter)
            VStack(spacing: 8) {
                HStack(spacing: 8) {
                    // Open in Navigation Apps
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                        showNavigationAppDialog = true
                    }) {
                        HStack(spacing: 5) {
                            Image(systemName: "arrow.triangle.turn.up.right.circle.fill")
                                .font(.system(size: 13, weight: .bold))
                            Text(localization.isArabic ? "الاتجاهات والملاحة" : "Directions")
                                .font(.app(11, .bold, isArabic: localization.isArabic))
                        }
                        .foregroundColor(AppColors.teal)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(.ultraThinMaterial)
                        .clipShape(Capsule())
                        .shadow(color: Color.black.opacity(0.12), radius: 6, y: 2)
                    }

                    // ETA Driving Badge
                    HStack(spacing: 5) {
                        Circle()
                            .fill(AppColors.accentGreen)
                            .frame(width: 6, height: 6)

                        Image(systemName: "car.fill")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(AppColors.teal)

                        Text(localization.isArabic ? "~\(estimatedDriveMinutes) د بالسيارة" : "~\(estimatedDriveMinutes)m drive")
                            .font(.app(11, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)
                    }
                    .padding(.horizontal, 9)
                    .padding(.vertical, 6)
                    .background(.ultraThinMaterial)
                    .clipShape(Capsule())
                    .shadow(color: Color.black.opacity(0.12), radius: 6, y: 2)

                    Spacer()

                    // Recenter Button
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        recenterCamera()
                    }) {
                        Image(systemName: "location.fill")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(AppColors.teal)
                            .frame(width: 32, height: 32)
                            .background(.ultraThinMaterial)
                            .clipShape(Circle())
                            .shadow(color: Color.black.opacity(0.12), radius: 6, y: 2)
                    }
                }
                .padding(10)

                Spacer()

                // Address & Distance Glass Pill at the bottom of the map
                HStack(spacing: 8) {
                    Image(systemName: "mappin.circle.fill")
                        .font(.system(size: 14))
                        .foregroundColor(AppColors.teal)

                    Text(currentBranch.localizedAddress(isArabic: localization.isArabic))
                        .font(.app(11.5, .medium, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textPrimary)
                        .lineLimit(1)

                    Spacer()

                    Text(displayDistanceString)
                        .font(.system(size: 11.5, weight: .bold, design: .rounded))
                        .foregroundColor(AppColors.textSecondary)
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 7)
                .background(.ultraThinMaterial)
                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                .padding(10)
            }
        }
        .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 22, style: .continuous)
                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(colorScheme == .dark ? 0.3 : 0.05), radius: 10, y: 3)
    }

    // MARK: - Real Driving Route & Camera Positioning via RouteDirectionsService
    @MainActor
    private func fetchRouteForCurrentBranch() async {
        isCalculatingRoute = true
        let start = userLocation
        let dest = currentBranch.coordinate

        if let route = await RouteDirectionsService.shared.fetchRoute(from: start, to: dest) {
            self.routeCoordinates = route.coordinates
            self.calculatedRouteInfo = route
            self.isCalculatingRoute = false

            if !route.coordinates.isEmpty {
                var mapRect = MKMapRect.null
                for coord in route.coordinates {
                    let point = MKMapPoint(coord)
                    mapRect = mapRect.union(MKMapRect(x: point.x, y: point.y, width: 0, height: 0))
                }
                let padded = mapRect.insetBy(dx: -max(mapRect.size.width * 0.35, 1500), dy: -max(mapRect.size.height * 0.55, 2200))
                withAnimation(.easeInOut(duration: 0.6)) {
                    self.cameraPosition = .rect(padded)
                }
            }
        } else {
            self.routeCoordinates = [start, dest]
            self.calculatedRouteInfo = nil
            self.isCalculatingRoute = false
            recenterCamera()
        }
    }

    @MainActor
    private func recenterCamera() {
        let start = userLocation
        let dest = currentBranch.coordinate
        var mapRect = MKMapRect.null
        let p1 = MKMapPoint(start)
        let p2 = MKMapPoint(dest)
        mapRect = mapRect.union(MKMapRect(x: p1.x, y: p1.y, width: 0, height: 0))
        mapRect = mapRect.union(MKMapRect(x: p2.x, y: p2.y, width: 0, height: 0))
        for coord in routeCoordinates {
            let p = MKMapPoint(coord)
            mapRect = mapRect.union(MKMapRect(x: p.x, y: p.y, width: 0, height: 0))
        }
        let padded = mapRect.insetBy(dx: -max(mapRect.size.width * 0.35, 1500), dy: -max(mapRect.size.height * 0.55, 2200))
        withAnimation(.easeInOut(duration: 0.5)) {
            cameraPosition = .rect(padded)
        }
    }

    // MARK: - 2. Service Header & Rating Identity Card
    private var serviceHeaderCard: some View {
        VStack(spacing: 14) {
            HStack(spacing: 14) {
                ZStack {
                    RoundedRectangle(cornerRadius: 18, style: .continuous)
                        .fill(place.color.opacity(colorScheme == .dark ? 0.25 : 0.12))
                        .frame(width: 58, height: 58)

                    Image(systemName: place.iconName)
                        .font(.system(size: 26, weight: .semibold))
                        .foregroundColor(place.color)
                }

                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 6) {
                        Text(place.localizedName(isArabic: localization.isArabic))
                            .font(.app(17, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)
                            .lineLimit(2)

                        Image(systemName: "checkmark.seal.fill")
                            .font(.system(size: 14))
                            .foregroundColor(AppColors.teal)
                    }

                    HStack(spacing: 8) {
                        // Category Pill
                        Text(place.category.title(isArabic: localization.isArabic))
                            .font(.app(11, .medium, isArabic: localization.isArabic))
                            .foregroundColor(place.color)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(place.color.opacity(0.12))
                            .clipShape(Capsule())

                        // Star Rating
                        HStack(spacing: 3) {
                            Image(systemName: "star.fill")
                                .font(.system(size: 11))
                                .foregroundColor(AppColors.amber)
                            Text(String(format: "%.1f", place.starRating))
                                .font(.system(size: 12, weight: .bold, design: .rounded))
                                .foregroundColor(AppColors.textPrimary)
                            Text("(\(place.reviewCount))")
                                .font(.app(11, .regular, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textTertiary)
                        }
                    }
                }

                Spacer()
            }
        }
        .padding(14)
        .background(AppColors.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
        )
    }

    // MARK: - 3. Quick Real-Time Metrics Strip
    private var quickMetricsStrip: some View {
        HStack(spacing: 0) {
            metricItem(
                titleAr: "الانتظار المتوقع",
                titleEn: "Est. Wait",
                value: "~\(currentBranch.estWaitMinutes) \(localization.isArabic ? "د" : "min")",
                icon: "timer",
                color: AppColors.teal
            )

            Divider()
                .frame(height: 28)

            metricItem(
                titleAr: "أمامك بالدور",
                titleEn: "Ahead of You",
                value: "\(currentBranch.waiting) \(localization.isArabic ? "عملاء" : "ahead")",
                icon: "person.2.fill",
                color: currentBranch.waiting <= 2 ? AppColors.accentGreen : AppColors.amber
            )

            Divider()
                .frame(height: 28)

            metricItem(
                titleAr: "الحالة الآن",
                titleEn: "Status",
                value: currentBranch.isOpen ? (localization.isArabic ? "مفتوح" : "Open") : (localization.isArabic ? "مغلق" : "Closed"),
                icon: currentBranch.isOpen ? "checkmark.circle.fill" : "moon.fill",
                color: currentBranch.isOpen ? AppColors.accentGreen : AppColors.accentRed
            )
        }
        .padding(.vertical, 10)
        .background(AppColors.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
        )
    }

    private func metricItem(titleAr: String, titleEn: String, value: String, icon: String, color: Color) -> some View {
        VStack(spacing: 3) {
            HStack(spacing: 4) {
                Image(systemName: icon)
                    .font(.system(size: 10))
                    .foregroundColor(color)

                Text(localization.isArabic ? titleAr : titleEn)
                    .font(.app(10, .regular, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
            }

            Text(value)
                .font(.app(13, .bold, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textPrimary)
        }
        .frame(maxWidth: .infinity)
    }

    private func selectBranch(at index: Int) {
        guard index < place.branches.count else { return }
        UIImpactFeedbackGenerator(style: .light).impactOccurred()
        withAnimation(.spring(response: 0.35, dampingFraction: 0.75)) {
            selectedBranchIndex = index
        }
        Task {
            await fetchRouteForCurrentBranch()
        }
    }

    // MARK: - 4. Branch Selector Section with Live Queue Counts
    private var branchSelectorSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Label {
                    Text(localization.isArabic ? "اختر الفرع المطلوب" : "Select Branch")
                        .font(.app(14, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textPrimary)
                } icon: {
                    Image(systemName: "building.2.crop.circle.fill")
                        .foregroundColor(AppColors.indigo)
                }

                Spacer()

                Text("\(selectedBranchIndex + 1) / \(place.branches.count)")
                    .font(.app(11, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background(AppColors.subtleFill)
                    .clipShape(Capsule())
            }

            VStack(spacing: 10) {
                ForEach(Array(place.branches.enumerated()), id: \.element.id) { index, branch in
                    let isSelected = selectedBranchIndex == index
                    Button(action: {
                        selectBranch(at: index)
                    }) {
                        HStack(spacing: 12) {
                            // Radio Indicator
                            ZStack {
                                Circle()
                                    .strokeBorder(isSelected ? AppColors.teal : AppColors.cardBorder, lineWidth: 2)
                                    .frame(width: 22, height: 22)

                                if isSelected {
                                    Circle()
                                        .fill(AppColors.teal)
                                        .frame(width: 12, height: 12)
                                }
                            }

                            VStack(alignment: .leading, spacing: 3) {
                                Text(branch.localizedLabel(isArabic: localization.isArabic))
                                    .font(.app(14, isSelected ? .bold : .medium, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textPrimary)

                                Text(branch.localizedAddress(isArabic: localization.isArabic))
                                    .font(.app(12, .regular, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textSecondary)
                                    .lineLimit(1)
                            }

                            Spacer()

                            VStack(alignment: .trailing, spacing: 2) {
                                HStack(spacing: 4) {
                                    Circle()
                                        .fill(branch.waiting <= 2 ? AppColors.accentGreen : AppColors.amber)
                                        .frame(width: 7, height: 7)
                                    Text(localization.isArabic ? "\(branch.waiting) انتظار" : "\(branch.waiting) in line")
                                        .font(.app(11, .bold, isArabic: localization.isArabic))
                                        .foregroundColor(branch.waiting <= 2 ? AppColors.accentGreen : AppColors.amber)
                                }

                                Text(branch.realDistanceFormatted(isArabic: localization.isArabic))
                                    .font(.app(11, .regular, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textTertiary)
                            }
                        }
                        .padding(13)
                        .background(
                            isSelected
                                ? AppColors.teal.opacity(colorScheme == .dark ? 0.15 : 0.06)
                                : AppColors.cardBackground
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 16, style: .continuous)
                                .strokeBorder(
                                    isSelected ? AppColors.teal : AppColors.cardBorder,
                                    lineWidth: isSelected ? 1.5 : 1
                                )
                        )
                    }
                    .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.9))
                }
            }
        }
        .padding(15)
        .background(AppColors.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
        )
    }

    // MARK: - 5. About Section
    private var aboutSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            Label {
                Text(localization.isArabic ? "عن الخدمة" : "About Service")
                    .font(.app(14, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)
            } icon: {
                Image(systemName: "info.circle.fill")
                    .foregroundColor(AppColors.teal)
            }

            Text(place.localizedAbout(isArabic: localization.isArabic))
                .font(.app(13, .regular, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textSecondary)
                .lineSpacing(4)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(15)
        .background(AppColors.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
        )
    }

    // MARK: - 6. Working Hours Section
    private var workingHoursSection: some View {
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(AppColors.amber.opacity(colorScheme == .dark ? 0.20 : 0.12))
                    .frame(width: 40, height: 40)
                Image(systemName: "clock.badge.checkmark.fill")
                    .foregroundColor(AppColors.amber)
                    .font(.system(size: 18))
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(localization.isArabic ? "مواعيد العمل الرسمية" : "Official Working Hours")
                    .font(.app(11, .medium, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)

                Text(place.localizedWorkingHours(isArabic: localization.isArabic))
                    .font(.app(13, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)
            }

            Spacer()

            Text(currentBranch.isOpen
                 ? (localization.isArabic ? "مفتوح الآن" : "Open Now")
                 : (localization.isArabic ? "مغلق" : "Closed"))
                .font(.app(11, .bold, isArabic: localization.isArabic))
                .foregroundColor(currentBranch.isOpen ? AppColors.accentGreen : AppColors.accentRed)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(
                    (currentBranch.isOpen ? AppColors.accentGreen : AppColors.accentRed)
                        .opacity(colorScheme == .dark ? 0.2 : 0.1)
                )
                .clipShape(Capsule())
        }
        .padding(14)
        .background(AppColors.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
        )
    }

    // MARK: - 4.5 Booking Mode Section (Live Queue vs Scheduled Appointment Slots)
    private var bookingModeSection: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                Text(localization.isArabic ? "طريقة الحجز" : "Booking Method")
                    .font(.app(14, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)

                Spacer()

                Text(bookingMode == .liveQueue
                     ? (localization.isArabic ? "طابور فوري لحظي" : "Instant Live Queue")
                     : (localization.isArabic ? "موعد محدد بالساعة" : "Scheduled Slot"))
                    .font(.app(11.5, .medium, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.teal)
            }

            // Segmented Picker
            HStack(spacing: 8) {
                // Option 1: Live Queue
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    withAnimation(.spring(response: 0.35, dampingFraction: 0.75)) {
                        bookingMode = .liveQueue
                    }
                }) {
                    HStack(spacing: 6) {
                        Image(systemName: "bolt.fill")
                            .font(.system(size: 13, weight: .bold))
                        Text(localization.isArabic ? "طابور فوري (الآن)" : "Live Queue (Now)")
                            .font(.app(12.5, .bold, isArabic: localization.isArabic))
                    }
                    .foregroundColor(bookingMode == .liveQueue ? .white : AppColors.textPrimary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .background(
                        bookingMode == .liveQueue
                            ? AppColors.teal
                            : Color.black.opacity(colorScheme == .dark ? 0.2 : 0.05)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                }

                // Option 2: Scheduled Slot
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    withAnimation(.spring(response: 0.35, dampingFraction: 0.75)) {
                        bookingMode = .scheduled
                        if selectedSlot == nil {
                            selectedSlot = availableSlots.first(where: { !$0.isBooked })
                        }
                    }
                }) {
                    HStack(spacing: 6) {
                        Image(systemName: "calendar.badge.clock")
                            .font(.system(size: 13, weight: .bold))
                        Text(localization.isArabic ? "موعد مسبق مجدول" : "Schedule Ahead")
                            .font(.app(12.5, .bold, isArabic: localization.isArabic))
                    }
                    .foregroundColor(bookingMode == .scheduled ? .white : AppColors.textPrimary)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .background(
                        bookingMode == .scheduled
                            ? AppColors.teal
                            : Color.black.opacity(colorScheme == .dark ? 0.2 : 0.05)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                }
            }

            if bookingMode == .scheduled {
                scheduledSlotPickerView
                    .transition(.opacity.combined(with: .move(edge: .top)))
            } else {
                HStack(spacing: 8) {
                    Image(systemName: "sparkles")
                        .font(.system(size: 13))
                        .foregroundColor(AppColors.amber)

                    Text(localization.isArabic
                         ? "يتم إصدار رقم دور فوري ويتم حساب وقت وصولك بالـ GPS تلقائياً"
                         : "Instant queue ticket issued with live GPS arrival estimate")
                        .font(.app(11.5, .medium, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)
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
    }

    private var scheduledSlotPickerView: some View {
        VStack(alignment: .leading, spacing: 12) {
            Divider()

            // 1. Day Selector Pills (اليوم / غداً / بعد غد)
            HStack(spacing: 8) {
                dayPill(index: 0, titleAr: "اليوم", titleEn: "Today")
                dayPill(index: 1, titleAr: "غداً", titleEn: "Tomorrow")
                dayPill(index: 2, titleAr: "بعد غد", titleEn: "In 2 Days")
            }

            // Notice about capacity
            HStack(spacing: 6) {
                Image(systemName: "info.circle.fill")
                    .font(.system(size: 11))
                    .foregroundColor(AppColors.textSecondary)
                Text(localization.isArabic
                     ? "المواعيد المكتملة غير متاحة • تم تمييز أقرب ميعاد متاح ⭐"
                     : "Booked slots disabled • Closest open slot highlighted ⭐")
                    .font(.app(11, .regular, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
            }

            // 2. Time Slots Grid
            LazyVGrid(columns: [
                GridItem(.flexible(), spacing: 8),
                GridItem(.flexible(), spacing: 8),
                GridItem(.flexible(), spacing: 8),
                GridItem(.flexible(), spacing: 8)
            ], spacing: 8) {
                ForEach(availableSlots) { slot in
                    slotButton(slot: slot)
                }
            }
        }
    }

    private func dayPill(index: Int, titleAr: String, titleEn: String) -> some View {
        Button(action: {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            withAnimation(.spring(response: 0.3, dampingFraction: 0.75)) {
                selectedDayIndex = index
                let daySlots = slots(for: index)
                selectedSlot = daySlots.first(where: { !$0.isBooked && $0.isRecommended }) ?? daySlots.first(where: { !$0.isBooked })
            }
        }) {
            Text(localization.isArabic ? titleAr : titleEn)
                .font(.app(11.5, .bold, isArabic: localization.isArabic))
                .foregroundColor(selectedDayIndex == index ? AppColors.teal : AppColors.textSecondary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 7)
                .background(
                    selectedDayIndex == index
                        ? AppColors.softTeal
                        : Color.black.opacity(colorScheme == .dark ? 0.2 : 0.04)
                )
                .clipShape(Capsule())
                .overlay(
                    Capsule()
                        .strokeBorder(selectedDayIndex == index ? AppColors.teal.opacity(0.3) : AppColors.cardBorder, lineWidth: 1)
                )
        }
    }

    private func slotButton(slot: AppointmentSlot) -> some View {
        Button(action: {
            if slot.isBooked {
                UINotificationFeedbackGenerator().notificationOccurred(.warning)
                return
            }
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            withAnimation(.spring(response: 0.3, dampingFraction: 0.75)) {
                selectedSlot = slot
            }
        }) {
            VStack(spacing: 2) {
                Text(localization.isArabic ? slot.time : slot.timeEn)
                    .font(.app(11, .bold, isArabic: localization.isArabic))
                    .strikethrough(slot.isBooked, color: Color.gray.opacity(0.6))

                if slot.isBooked {
                    Text(localization.isArabic ? "مكتمل" : "Full")
                        .font(.app(8.5, .bold, isArabic: localization.isArabic))
                        .foregroundColor(Color.gray)
                } else if slot.isRecommended {
                    Text(localization.isArabic ? "أقرب متاح ⭐" : "Nearest ⭐")
                        .font(.app(8.5, .bold, isArabic: localization.isArabic))
                        .foregroundColor(selectedSlot?.id == slot.id ? .white : AppColors.teal)
                } else {
                    Text(localization.isArabic ? "متاح" : "Open")
                        .font(.app(8.5, .regular, isArabic: localization.isArabic))
                        .foregroundColor(selectedSlot?.id == slot.id ? .white.opacity(0.85) : AppColors.accentGreen)
                }
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 7)
            .background(
                slot.isBooked
                    ? Color.gray.opacity(colorScheme == .dark ? 0.15 : 0.08)
                    : (selectedSlot?.id == slot.id
                        ? AppColors.teal
                        : (slot.isRecommended
                            ? AppColors.teal.opacity(colorScheme == .dark ? 0.2 : 0.1)
                            : Color.white))
            )
            .foregroundColor(
                slot.isBooked
                    ? Color.gray.opacity(0.7)
                    : (selectedSlot?.id == slot.id ? .white : AppColors.textPrimary)
            )
            .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 10, style: .continuous)
                    .strokeBorder(
                        slot.isBooked
                            ? Color.clear
                            : (selectedSlot?.id == slot.id
                                ? AppColors.teal
                                : (slot.isRecommended ? AppColors.teal.opacity(0.4) : AppColors.cardBorder)),
                        lineWidth: 1
                    )
            )
        }
    }

    // MARK: - Floating Bottom Action Bar
    private var hasExistingTicket: Bool {
        dataService.hasActiveTicket(for: place.id)
    }

    private var bottomActionBar: some View {
        VStack(spacing: 0) {
            HStack(spacing: 16) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(bookingMode == .scheduled
                         ? (localization.isArabic ? "الموعد المختار" : "Selected Slot")
                         : (localization.isArabic ? "الانتظار المتوقع" : "Expected Wait"))
                        .font(.app(11, .regular, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)

                    if bookingMode == .scheduled {
                        let activeTime = selectedSlot?.time ?? (availableSlots.first(where: { !$0.isBooked })?.time ?? "")
                        let dayName = selectedDayIndex == 0 ? (localization.isArabic ? "اليوم" : "Today") : (selectedDayIndex == 1 ? (localization.isArabic ? "غداً" : "Tomorrow") : (localization.isArabic ? "بعد غد" : "In 2 days"))
                        HStack(spacing: 5) {
                            Text(dayName)
                                .font(.app(13, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.amber)
                            Text("•")
                                .foregroundColor(AppColors.textSecondary)
                            Text(activeTime)
                                .font(.system(size: 16, weight: .black, design: .rounded))
                                .foregroundColor(AppColors.teal)
                        }
                    } else {
                        HStack(spacing: 4) {
                            Text("~\(currentBranch.estWaitMinutes)")
                                .font(.system(size: 20, weight: .black, design: .rounded))
                                .foregroundColor(AppColors.teal)
                            Text(localization.isArabic ? "دقيقة" : "mins")
                                .font(.app(13, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textPrimary)
                        }
                    }
                }

                Spacer()

                Button(action: handleJoinQueue) {
                    HStack(spacing: 8) {
                        if isJoining {
                            ProgressView()
                                .tint(.white)
                        } else if hasExistingTicket {
                            Image(systemName: "checkmark.circle.fill")
                                .font(.system(size: 14, weight: .bold))
                            Text(localization.isArabic ? "عرض تذكرتك الحالية" : "View Active Ticket")
                                .font(.app(13.5, .bold, isArabic: localization.isArabic))
                        } else {
                            Image(systemName: bookingMode == .scheduled ? "calendar.badge.plus" : "ticket.fill")
                                .font(.system(size: 14, weight: .bold))

                            Text(bookingMode == .scheduled
                                 ? (localization.isArabic ? "تأكيد حجز الموعد" : "Confirm Appointment")
                                 : (localization.isArabic ? "انضم للطابور الآن" : "Join Queue Now"))
                                .font(.app(14, .bold, isArabic: localization.isArabic))
                        }
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(hasExistingTicket ? AppColors.ink : AppColors.teal)
                    .clipShape(Capsule())
                    .shadow(color: (hasExistingTicket ? AppColors.ink : AppColors.teal).opacity(0.35), radius: 10, y: 4)
                }
                .disabled(isJoining)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 14)
            .background(.ultraThinMaterial)
            .overlay(
                Rectangle()
                    .fill(AppColors.cardBorder)
                    .frame(height: 1),
                alignment: .top
            )
        }
    }

    // MARK: - Ticket Success Sheet
    private var ticketSuccessSheet: some View {
        VStack(spacing: 20) {
            HStack {
                Spacer()
                SheetCloseButton {
                    showSuccessModal = false
                    dismiss()
                }
            }
            .padding(.top, 16)
            .padding(.horizontal, 4)

            ZStack {
                Circle()
                    .fill(AppColors.teal.opacity(0.14))
                    .frame(width: 76, height: 76)

                Image(systemName: "checkmark.circle.fill")
                    .font(.system(size: 48))
                    .foregroundColor(AppColors.teal)
                    .symbolEffect(.bounce)
            }

            VStack(spacing: 6) {
                Text(bookingMode == .scheduled
                     ? (localization.isArabic ? "تم حجز موعدك بنجاح! 📅" : "Appointment Booked! 📅")
                     : (localization.isArabic ? "تم حجز دورك بنجاح! 🎉" : "Ticket Issued Successfully! 🎉"))
                    .font(.app(20, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)

                Text(localization.isArabic
                     ? "رقم الحجز في \(place.nameAr)"
                     : "Your booking at \(place.name)")
                    .font(.app(14, .regular, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
            }

            // Ticket Number Capsule
            VStack(spacing: 6) {
                Text(bookingMode == .scheduled
                     ? (localization.isArabic ? "رقم الحجز المؤكد" : "Booking Number")
                     : (localization.isArabic ? "رقم التذكرة" : "Ticket Number"))
                    .font(.app(12, .medium, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)

                Text("#\(issuedTicketNumber)")
                    .font(.system(size: 46, weight: .black, design: .rounded))
                    .foregroundColor(AppColors.teal)

                if bookingMode == .scheduled {
                    let dayName = selectedDayIndex == 0 ? (localization.isArabic ? "اليوم" : "Today") : (selectedDayIndex == 1 ? (localization.isArabic ? "غداً" : "Tomorrow") : (localization.isArabic ? "بعد غد" : "In 2 days"))
                    let slotTime = selectedSlot?.time ?? (availableSlots.first(where: { !$0.isBooked })?.time ?? "04:00 م")
                    Text(localization.isArabic
                         ? "الموعد: \(dayName) الساعة \(slotTime) • تم التثبيت"
                         : "Date: \(dayName) at \(slotTime) • Confirmed")
                        .font(.app(13.5, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.teal)
                } else {
                    Text(localization.isArabic
                         ? "أمامك \(currentBranch.waiting) في الانتظار — جهز نفسك قبل الموعد"
                         : "\(currentBranch.waiting) people ahead — please arrive on time")
                        .font(.app(12, .medium, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.amber)
                }
            }
            .padding(.horizontal, 32)
            .padding(.vertical, 18)
            .frame(maxWidth: .infinity)
            .background(AppColors.cardBackground)
            .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 20, style: .continuous)
                    .strokeBorder(AppColors.teal.opacity(0.3), lineWidth: 1.5)
            )

            // Primary Button to View Ticket in Queue Tab
            Button(action: {
                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                showSuccessModal = false
                onQueueJoined()
                QueueDataService.shared.navigateToQueueTab()
                dismiss()
            }) {
                Text(localization.isArabic ? "عرض تذكرتي في طابوري" : "View My Ticket in Queue")
                    .font(.app(15, .bold, isArabic: localization.isArabic))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(AppColors.teal)
                    .clipShape(Capsule())
            }

            // Secondary: Share Ticket with Family / Friends Button
            Button(action: {
                let shareTicketObj = dataService.activeTicket ?? UserTicket(
                    ticketNumber: issuedTicketNumber,
                    serviceName: place.name,
                    serviceNameAr: place.nameAr,
                    branchLabel: currentBranch.label,
                    branchLabelAr: currentBranch.labelAr,
                    counterLabel: "Counter 2",
                    counterLabelAr: "شباك رقم 2",
                    waitingAhead: currentBranch.waiting,
                    estMinutes: currentBranch.estWaitMinutes,
                    currentServing: "A-10",
                    progress: 0.15,
                    isActive: true
                )
                ShareTicketHelper.shareTicket(shareTicketObj, isArabic: localization.isArabic)
            }) {
                HStack(spacing: 6) {
                    Image(systemName: "square.and.arrow.up")
                        .font(.system(size: 13, weight: .bold))

                    Text(localization.isArabic ? "مشاركة التذكرة مع العائلة أو الأصدقاء" : "Share Ticket with Family / Friends")
                        .font(.app(13.5, .bold, isArabic: localization.isArabic))
                }
                .foregroundColor(AppColors.teal)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .background(AppColors.softTeal)
                .clipShape(Capsule())
            }
            .padding(.bottom, 16)
        }
        .padding(.horizontal, 24)
        .presentationDetents([.fraction(0.65)])
        .presentationDragIndicator(.visible)
    }

    private func handleJoinQueue() {
        if hasExistingTicket {
            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
            showAlreadyBookedAlert = true
            return
        }

        if dataService.activeTickets.count >= 3 {
            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
            showMaxTicketsAlert = true
            return
        }

        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
        isJoining = true

        Task {
            try? await Task.sleep(nanoseconds: 500_000_000)
            let dateStr = selectedDayIndex == 0 ? (localization.isArabic ? "اليوم" : "Today") : (selectedDayIndex == 1 ? (localization.isArabic ? "غداً" : "Tomorrow") : (localization.isArabic ? "بعد غد" : "In 2 days"))
            let timeStr = selectedSlot?.time ?? (availableSlots.first(where: { !$0.isBooked })?.time ?? "04:00 م")

            let ticket = dataService.joinQueue(
                place: place,
                branch: currentBranch,
                bookingType: bookingMode,
                scheduledDate: bookingMode == .scheduled ? dateStr : nil,
                scheduledTime: bookingMode == .scheduled ? timeStr : nil
            )

            await MainActor.run {
                isJoining = false
                issuedTicketNumber = ticket.ticketNumber
                UINotificationFeedbackGenerator().notificationOccurred(.success)
                showSuccessModal = true
            }
        }
    }

    private func openInAppleMaps() {
        let lat = currentBranch.lat
        let lng = currentBranch.lng
        if let url = URL(string: "http://maps.apple.com/?daddr=\(lat),\(lng)") {
            UIApplication.shared.open(url)
        }
    }
}

// MARK: - Custom Map Pin Annotation View
public struct BranchMapAnnotationPin: View {
    public let place: ServicePlace
    public let branch: ServiceBranch
    public let isSelected: Bool
    public let isArabic: Bool
    public let onTap: () -> Void

    public var body: some View {
        Button(action: {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            onTap()
        }) {
            VStack(spacing: 2) {
                // Floating branch title bubble if selected
                if isSelected {
                    HStack(spacing: 5) {
                        Text(branch.localizedLabel(isArabic: isArabic))
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(AppColors.textPrimary)

                        Text("•")
                            .foregroundColor(AppColors.textTertiary)

                        Text(isArabic ? "\(branch.waiting) بالانتظار" : "\(branch.waiting) wait")
                            .font(.system(size: 10, weight: .semibold))
                            .foregroundColor(branch.waiting <= 2 ? AppColors.accentGreen : AppColors.amber)
                    }
                    .padding(.horizontal, 9)
                    .padding(.vertical, 4.5)
                    .background(Color.white)
                    .clipShape(Capsule())
                    .overlay(
                        Capsule().strokeBorder(AppColors.cardBorder, lineWidth: 0.8)
                    )
                    .shadow(color: Color.black.opacity(0.12), radius: 4, y: 2)
                    .fixedSize()
                }

                // Pin Body (Rock-solid, stationary, no repeating layout jitter)
                ZStack {
                    if isSelected {
                        Circle()
                            .fill(place.color.opacity(0.20))
                            .frame(width: 44, height: 44)
                    }

                    Circle()
                        .fill(isSelected ? place.color : Color.white)
                        .frame(width: isSelected ? 34 : 26, height: isSelected ? 34 : 26)
                        .overlay(
                            Circle().strokeBorder(Color.white, lineWidth: isSelected ? 2 : 1.5)
                        )
                        .shadow(color: Color.black.opacity(0.18), radius: isSelected ? 5 : 2.5, y: 2)

                    Image(systemName: place.iconName)
                        .font(.system(size: isSelected ? 15 : 12, weight: .bold))
                        .foregroundColor(isSelected ? .white : place.color)
                }
                .frame(width: 44, height: 44)

                // Pin Point Triangle
                Triangle()
                    .fill(isSelected ? place.color : Color.white)
                    .frame(width: 10, height: 6)
                    .offset(y: -2)
            }
            .fixedSize()
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Helper Shape for Pin Pointer
struct Triangle: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.minX, y: rect.minY))
        path.addLine(to: CGPoint(x: rect.maxX, y: rect.minY))
        path.addLine(to: CGPoint(x: rect.midX, y: rect.maxY))
        path.closeSubpath()
        return path
    }
}
