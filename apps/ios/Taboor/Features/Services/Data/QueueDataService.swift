import SwiftUI
import Combine

// MARK: - Queue Data Service (Central Observable State for Authenticated Flows)

@MainActor
public final class QueueDataService: ObservableObject {
    public static let shared = QueueDataService()

    @Published public var activeTickets: [UserTicket] = []
    @Published public var selectedTicketIndex: Int = 0

    public var activeTicket: UserTicket? {
        get {
            guard !activeTickets.isEmpty else { return nil }
            let idx = min(selectedTicketIndex, activeTickets.count - 1)
            return activeTickets[max(0, idx)]
        }
        set {
            if let val = newValue {
                if activeTickets.isEmpty {
                    activeTickets = [val]
                } else {
                    let idx = min(selectedTicketIndex, activeTickets.count - 1)
                    activeTickets[max(0, idx)] = val
                }
            } else {
                if !activeTickets.isEmpty {
                    let idx = min(selectedTicketIndex, activeTickets.count - 1)
                    activeTickets.remove(at: max(0, idx))
                    if selectedTicketIndex >= activeTickets.count {
                        selectedTicketIndex = max(0, activeTickets.count - 1)
                    }
                }
            }
        }
    }
    @Published public var places: [ServicePlace] = []
    @Published public var alerts: [AppAlertItem] = []
    @Published public var history: [TicketHistoryItem] = []
    @Published public var isTabBarHidden: Bool = false
    @Published public var shouldNavigateToQueueTab: Bool = false
    @Published public var requestedTab: TaboorTab? = nil
    @Published public var showLiveTracking: Bool = false
    @Published public var showHistoryScreen: Bool = false
    @Published public var showFavoritesScreen: Bool = false
    @Published public var showAlertPreferencesScreen: Bool = false
    @Published public var showAllServicesScreen: Bool = false
    @Published public var selectedPlaceFromSpotlight: ServicePlace? = nil
    @Published public var favoritePlaceIds: Set<String> = []

    // Smart Alert Preferences
    @Published public var customersAheadNotice: Int = 2
    @Published public var minutesBeforeNotice: Int = 10
    @Published public var smartDepartureNotice: Bool = true
    @Published public var timeSensitiveHaptics: Bool = true
    @Published public var liveActivityLockScreen: Bool = true

    public var favoritePlaces: [ServicePlace] {
        places.filter { favoritePlaceIds.contains($0.id) }
    }

    // Repositories (Decoupled Mock-First & Real API Access)
    private let placesRepository: PlacesRepositoryProtocol
    private let queueRepository: QueueRepositoryProtocol
    private let alertsRepository: AlertsRepositoryProtocol

    public func isFavorite(placeId: String) -> Bool {
        favoritePlaceIds.contains(placeId)
    }

    public func toggleFavorite(placeId: String) {
        if favoritePlaceIds.contains(placeId) {
            favoritePlaceIds.remove(placeId)
        } else {
            favoritePlaceIds.insert(placeId)
        }
        UserDefaults.standard.set(Array(favoritePlaceIds), forKey: "taboor_favorite_places")

        Task { [weak self] in
            try? await self?.placesRepository.toggleFavorite(placeId: placeId)
        }
    }

    /// Asynchronously refreshes all data from API (or seed JSON when useMockData == true)
    public func refreshAll() async {
        do {
            async let fetchedPlaces = placesRepository.getPlaces()
            async let fetchedTickets = queueRepository.getActiveTickets()
            async let fetchedAlerts = alertsRepository.getAlerts()
            async let fetchedHistory = queueRepository.getHistory()

            let (p, t, a, h) = try await (fetchedPlaces, fetchedTickets, fetchedAlerts, fetchedHistory)

            if !p.isEmpty { self.places = p }
            if !t.isEmpty { self.activeTickets = t }
            if !a.isEmpty { self.alerts = a }
            if !h.isEmpty { self.history = h }
        } catch {
            print("[QueueDataService] Failed to load data from repository: \(error)")
        }
    }

    /// Resets all user-specific active tickets and queue state on logout
    public func resetOnLogout() {
        self.activeTickets = []
        self.selectedTicketIndex = 0
        self.alerts = []
        self.history = []
        self.showLiveTracking = false
        self.requestedTab = nil
    }

    public func updateAlertPreferences(
        customersAhead: Int,
        minutesBefore: Int,
        smartDeparture: Bool,
        timeSensitive: Bool,
        liveActivity: Bool
    ) {
        self.customersAheadNotice = customersAhead
        self.minutesBeforeNotice = minutesBefore
        self.smartDepartureNotice = smartDeparture
        self.timeSensitiveHaptics = timeSensitive
        self.liveActivityLockScreen = liveActivity

        let def = UserDefaults.standard
        def.set(customersAhead, forKey: "taboor_pref_customers_ahead")
        def.set(minutesBefore, forKey: "taboor_pref_minutes_before")
        def.set(smartDeparture, forKey: "taboor_pref_smart_departure")
        def.set(timeSensitive, forKey: "taboor_pref_time_sensitive")
        def.set(liveActivity, forKey: "taboor_pref_live_activity")
    }

    public func navigateToQueueTab() {
        self.isTabBarHidden = false
        self.shouldNavigateToQueueTab = true
    }

    public func openLiveTracking() {
        self.isTabBarHidden = false
        self.showLiveTracking = true
    }

    public func selectPlaceFromSpotlight(id: String) {
        if let place = places.first(where: { $0.id == id }) {
            self.selectedPlaceFromSpotlight = place
        }
    }

    public var unreadAlertsCount: Int {
        alerts.filter { !$0.isRead }.count
    }

    private init(
        placesRepository: PlacesRepositoryProtocol = PlacesRepository.shared,
        queueRepository: QueueRepositoryProtocol = QueueRepository.shared,
        alertsRepository: AlertsRepositoryProtocol = AlertsRepository.shared
    ) {
        self.placesRepository = placesRepository
        self.queueRepository = queueRepository
        self.alertsRepository = alertsRepository

        self.places = Self.seedPlaces()
        self.alerts = Self.seedAlerts()
        self.history = Self.seedHistory()

        // Load favorites
        if let saved = UserDefaults.standard.stringArray(forKey: "taboor_favorite_places") {
            self.favoritePlaceIds = Set(saved)
        } else {
            self.favoritePlaceIds = ["place_1", "place_2"]
        }

        // Load alert preferences
        let def = UserDefaults.standard
        if def.object(forKey: "taboor_pref_customers_ahead") != nil {
            self.customersAheadNotice = def.integer(forKey: "taboor_pref_customers_ahead")
        }
        if def.object(forKey: "taboor_pref_minutes_before") != nil {
            self.minutesBeforeNotice = def.integer(forKey: "taboor_pref_minutes_before")
        }
        if def.object(forKey: "taboor_pref_smart_departure") != nil {
            self.smartDepartureNotice = def.bool(forKey: "taboor_pref_smart_departure")
        }
        if def.object(forKey: "taboor_pref_time_sensitive") != nil {
            self.timeSensitiveHaptics = def.bool(forKey: "taboor_pref_time_sensitive")
        }
        if def.object(forKey: "taboor_pref_live_activity") != nil {
            self.liveActivityLockScreen = def.bool(forKey: "taboor_pref_live_activity")
        }

        let userCoord = UserLocationService.shared.effectiveCoordinate
        let place1 = self.places.first(where: { $0.id == "place_1" })
        let branch1 = place1?.nearestBranch(to: userCoord) ?? place1?.branches.first
        let coord1 = branch1?.coordinate ?? CLLocationCoordinate2D(latitude: 31.2635, longitude: 32.3060)

        let initialTicket = UserTicket(
            id: "ticket_1",
            ticketNumber: "A-12",
            serviceName: "Dar Al Fouad Hospital",
            serviceNameAr: "مستشفى دار الفؤاد - العيادات الخارجية",
            branchLabel: branch1?.label ?? "Port Said Waterfront Hospital & Clinics",
            branchLabelAr: branch1?.labelAr ?? "فرع بورسعيد - شارع طرح البحر (حي الشرق)",
            counterLabel: "Counter 3",
            counterLabelAr: "شباك رقم 3",
            waitingAhead: 2,
            estMinutes: 8,
            currentServing: "A-10",
            progress: 0.75,
            isActive: true,
            bookingType: .liveQueue,
            placeId: "place_1",
            latitude: coord1.latitude,
            longitude: coord1.longitude
        )

        let place5 = self.places.first(where: { $0.id == "place_7" || $0.id == "place_5" })
        let branch5 = place5?.nearestBranch(to: userCoord) ?? place5?.branches.first
        let coord5 = branch5?.coordinate ?? CLLocationCoordinate2D(latitude: 31.2610, longitude: 32.3080)

        let scheduledTicket = UserTicket(
            id: "ticket_2",
            ticketNumber: "M-24",
            serviceName: "Mirror Barber & Grooming",
            serviceNameAr: "صالون ميرور للحلاقة والتجميل",
            branchLabel: branch5?.label ?? "Port Said Marina Branch",
            branchLabelAr: branch5?.labelAr ?? "فرع بورسعيد - الممشى السياحي",
            counterLabel: "Chair 2",
            counterLabelAr: "كرسي رقم 2",
            waitingAhead: 0,
            estMinutes: 0,
            currentServing: "M-20",
            progress: 0.0,
            isActive: true,
            bookingType: .scheduled,
            scheduledDate: "غداً",
            scheduledTime: "04:30 م",
            placeId: place5?.id ?? "place_7",
            latitude: coord5.latitude,
            longitude: coord5.longitude
        )

        self.activeTickets = [initialTicket, scheduledTicket]
        self.selectedTicketIndex = 0

        // Index places & active ticket into iOS Spotlight
        SpotlightService.shared.indexPlaces(self.places)
        if let ticket = self.activeTicket {
            SpotlightService.shared.indexActiveTicket(ticket)
        }

        // Asynchronously load data from Repositories (JSON seeds or API)
        Task { [weak self] in
            await self?.refreshAll()
            self?.syncActiveTicketsWithUserLocation()
        }
    }

    // MARK: - Queue Operations

    public func hasActiveTicket(for placeId: String) -> Bool {
        activeTickets.contains { $0.placeId == placeId }
    }

    @discardableResult
    public func joinQueue(
        place: ServicePlace,
        branch: ServiceBranch,
        bookingType: TicketBookingType = .liveQueue,
        scheduledDate: String? = nil,
        scheduledTime: String? = nil
    ) -> UserTicket {
        let randomNum = Int.random(in: 14...68)
        let prefix = place.category == .clinics ? "A" : (place.category == .salons ? "M" : (place.category == .garages ? "C" : "L"))
        let ticketNum = "\(prefix)-\(randomNum)"

        let newTicket = UserTicket(
            id: UUID().uuidString,
            ticketNumber: ticketNum,
            serviceName: place.name,
            serviceNameAr: place.nameAr,
            branchLabel: branch.label,
            branchLabelAr: branch.labelAr,
            counterLabel: "Counter 2",
            counterLabelAr: "شباك رقم 2",
            waitingAhead: bookingType == .scheduled ? 0 : max(1, branch.waiting),
            estMinutes: bookingType == .scheduled ? 0 : branch.estWaitMinutes,
            currentServing: "\(prefix)-0\(max(1, randomNum - branch.waiting))",
            progress: bookingType == .scheduled ? 0.0 : 0.15,
            joinedAt: Date(),
            isActive: true,
            bookingType: bookingType,
            scheduledDate: scheduledDate,
            scheduledTime: scheduledTime,
            placeId: place.id,
            latitude: branch.lat,
            longitude: branch.lng
        )

        self.activeTickets.insert(newTicket, at: 0)
        self.selectedTicketIndex = 0

        // Add corresponding confirmation notification
        let newAlert = AppAlertItem(
            type: .join,
            titleAr: bookingType == .scheduled ? "تم تأكيد موعدك بنجاح" : "تم إصدار تذكرتك بنجاح",
            titleEn: bookingType == .scheduled ? "Appointment Confirmed" : "Ticket Issued Successfully",
            descAr: bookingType == .scheduled
                ? "موعدك رقم #\(ticketNum) في \(place.nameAr) يوم \(scheduledDate ?? "اليوم") الساعة \(scheduledTime ?? ""). سننبهك قبل الموعد."
                : "تذكرتك رقم #\(ticketNum) في \(place.nameAr) جاهزة. أمامك \(branch.waiting) فقط.",
            descEn: bookingType == .scheduled
                ? "Appointment #\(ticketNum) at \(place.name) on \(scheduledDate ?? "Today") at \(scheduledTime ?? ""). We'll notify you."
                : "Ticket #\(ticketNum) at \(place.name) is active. \(branch.waiting) ahead in line.",
            timeAgoAr: "الآن",
            timeAgoEn: "Just now",
            isRead: false
        )
        self.alerts.insert(newAlert, at: 0)

        // Index new ticket into Spotlight
        SpotlightService.shared.indexActiveTicket(newTicket)

        // Play uplifting success confirmation chime & haptics
        TaboorSensoryManager.shared.playSuccessAlert()

        return newTicket
    }

    public func delayActiveTicket(minutes: Int = 10) {
        guard var ticket = activeTicket else { return }
        ticket.estMinutes += minutes
        ticket.waitingAhead += 2
        ticket.progress = max(0.1, ticket.progress - 0.2)
        self.activeTicket = ticket
        QueueLiveActivityManager.shared.updateLiveActivity(with: ticket)
        SpotlightService.shared.indexActiveTicket(ticket)

        let ticketId = ticket.id
        Task { [weak self] in
            try? await self?.queueRepository.delayTicket(id: ticketId, minutes: minutes)
        }
    }

    public func cancelActiveTicket(id: String? = nil) {
        let ticketToCancel: UserTicket?
        let targetIndex: Int?

        if let id = id, let idx = activeTickets.firstIndex(where: { $0.id == id }) {
            ticketToCancel = activeTickets[idx]
            targetIndex = idx
        } else if let current = activeTicket, let idx = activeTickets.firstIndex(where: { $0.id == current.id }) {
            ticketToCancel = current
            targetIndex = idx
        } else {
            ticketToCancel = nil
            targetIndex = nil
        }

        guard let ticket = ticketToCancel, let idx = targetIndex else { return }

        // De-index ticket from Spotlight
        SpotlightService.shared.deindexTicket(ticketNumber: ticket.ticketNumber)

        // Archive to history as cancelled
        let historyItem = TicketHistoryItem(
            ticketNumber: ticket.ticketNumber,
            serviceName: ticket.serviceName,
            serviceNameAr: ticket.serviceNameAr,
            branchLabel: ticket.branchLabel,
            branchLabelAr: ticket.branchLabelAr,
            dateString: LocalizationManager.shared.isArabic ? "اليوم" : "Today",
            status: .cancelled,
            durationMinutes: 4
        )
        self.history.insert(historyItem, at: 0)

        self.activeTickets.remove(at: idx)
        if selectedTicketIndex >= activeTickets.count {
            selectedTicketIndex = max(0, activeTickets.count - 1)
        }

        if activeTickets.isEmpty {
            QueueLiveActivityManager.shared.endLiveActivity()
        }

        let ticketId = ticket.id
        Task { [weak self] in
            try? await self?.queueRepository.cancelTicket(id: ticketId)
        }
    }

    /// Automatically syncs active tickets with the nearest branch to the user's updated location
    public func syncActiveTicketsWithUserLocation(_ userCoord: CLLocationCoordinate2D = UserLocationService.shared.effectiveCoordinate) {
        var didChange = false
        for idx in activeTickets.indices {
            let ticket = activeTickets[idx]
            if let placeId = ticket.placeId,
               let place = places.first(where: { $0.id == placeId }),
               let nearest = place.nearestBranch(to: userCoord) {
                if ticket.latitude != nearest.lat || ticket.longitude != nearest.lng {
                    activeTickets[idx].latitude = nearest.lat
                    activeTickets[idx].longitude = nearest.lng
                    activeTickets[idx].branchLabel = nearest.label
                    activeTickets[idx].branchLabelAr = nearest.labelAr
                    didChange = true
                }
            }
        }
        if didChange, let active = self.activeTicket {
            SpotlightService.shared.indexActiveTicket(active)
            QueueLiveActivityManager.shared.updateLiveActivity(with: active)
        }
    }

    // MARK: - Alerts Operations

    public func markAlertRead(id: String) {
        if let idx = alerts.firstIndex(where: { $0.id == id }) {
            alerts[idx].isRead = true
        }
    }

    public func markAllAlertsRead() {
        for idx in alerts.indices {
            alerts[idx].isRead = true
        }
    }

    public func clearAllAlerts() {
        alerts.removeAll()
    }

    public func deleteAlert(id: String) {
        alerts.removeAll(where: { $0.id == id })
    }

    // MARK: - Search & Filtering

    public func filterPlaces(
        query: String,
        category: ServiceCategory? = nil,
        filter: ServiceSortFilter = .all,
        scopeToCurrentCity: Bool = true
    ) -> [ServicePlace] {
        let userCoord = UserLocationService.shared.effectiveCoordinate
        var results = places.filter { place in
            let matchesCat = (category == nil || place.category == category)
            let matchesQ = place.matches(query: query)
            let matchesArea = !scopeToCurrentCity || place.isAvailableInLocalArea(around: userCoord)
            return matchesCat && matchesQ && matchesArea
        }

        // If user typed a search query specifically and got 0 results locally,
        // search nationwide so users can discover services across Egypt if explicitly searched
        if results.isEmpty && !query.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && scopeToCurrentCity {
            results = places.filter { place in
                let matchesCat = (category == nil || place.category == category)
                let matchesQ = place.matches(query: query)
                return matchesCat && matchesQ
            }
        }

        switch filter {
        case .all:
            break
        case .nearest:
            results.sort { p1, p2 in
                let d1 = p1.primaryBranch.map { UserLocationService.shared.distance(to: $0.coordinate) } ?? 999_999
                let d2 = p2.primaryBranch.map { UserLocationService.shared.distance(to: $0.coordinate) } ?? 999_999
                return d1 < d2
            }
        case .fastest:
            results.sort { $0.totalWaiting < $1.totalWaiting }
        case .topRated:
            results.sort { $0.starRating > $1.starRating }
        case .openNow:
            results = results.filter { $0.primaryBranch?.isOpen == true }
        }

        return results
    }

    public func scanQRCode(code: String) -> (ServicePlace, ServiceBranch)? {
        let clean = code.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        if clean.isEmpty { return nil }

        for place in places {
            if clean.contains(place.id.lowercased()) || clean.contains(place.name.lowercased()) {
                if let branch = place.primaryBranch {
                    return (place, branch)
                }
            }
        }
        // Fallback to first place and primary branch
        if let place = places.first, let branch = place.primaryBranch {
            return (place, branch)
        }
        return nil
    }

    // MARK: - Seeded Data

    private static func seedPlaces() -> [ServicePlace] {
        if let url = Bundle.main.url(forResource: "places_seed", withExtension: "json") ??
                     Bundle.main.url(forResource: "places_seed", withExtension: "json", subdirectory: "Seeds"),
           let data = try? Data(contentsOf: url),
           let response = try? JSONDecoder().decode(ApiResponse<[PlaceDTO]>.self, from: data),
           let dtos = response.data, !dtos.isEmpty {
            return dtos.map { $0.toDomain() }
        }

        return [
            ServicePlace(
                id: "place_1",
                name: "Dar Al Fouad Hospital",
                nameAr: "مستشفى دار الفؤاد",
                iconName: "cross.case.fill",
                color: AppColors.teal,
                category: .clinics,
                starRating: 4.9,
                reviewCount: 1420,
                keywords: ["hospital", "clinic", "doctor", "emergency", "مستشفى", "طوارئ", "باطنة", "فؤاد"],
                about: "A premier international hospital offering comprehensive outpatient clinics, cardiac center, and smart queue admission.",
                aboutAr: "مستشفى دولي رائد يقدم عيادات خارجية تخصصية ومركزاً متكاملاً للقلب مع نظام دخول وتنظيم طوابير ذكي.",
                workingHours: "24/7 Open",
                workingHoursAr: "مفتوح ٢٤ ساعة طوال الأسبوع",
                branches: [
                    ServiceBranch(
                        id: "branch_1_1",
                        label: "Nasr City Main Branch",
                        labelAr: "الفرع الرئيسي - مدينة نصر",
                        distanceKm: "8.4",
                        waiting: 4,
                        isOpen: true,
                        address: "Ahmed Fakhry St, Nasr City, Cairo",
                        addressAr: "شارع أحمد فخري، مدينة نصر، القاهرة",
                        lat: 30.0585,
                        lng: 31.3465
                    ),
                    ServiceBranch(
                        id: "branch_1_2",
                        label: "6th of October Branch",
                        labelAr: "فرع 6 أكتوبر - المحور المركزي",
                        distanceKm: "24.2",
                        waiting: 2,
                        isOpen: true,
                        address: "Central Axis, 6th of October City",
                        addressAr: "المحور المركزي، مدينة 6 أكتوبر",
                        lat: 29.9725,
                        lng: 30.9542
                    ),
                    ServiceBranch(
                        id: "branch_1_3",
                        label: "Maadi Outpatient Center",
                        labelAr: "فرع المعادي - طريق النصر",
                        distanceKm: "12.8",
                        waiting: 3,
                        isOpen: true,
                        address: "El Nasr St, New Maadi, Cairo",
                        addressAr: "شارع النصر، المعادي الجديدة، القاهرة",
                        lat: 29.9760,
                        lng: 31.2810
                    ),
                    ServiceBranch(
                        id: "branch_1_4",
                        label: "Port Said Waterfront Hospital & Clinics",
                        labelAr: "فرع بورسعيد - شارع طرح البحر (حي الشرق)",
                        distanceKm: "1.2",
                        waiting: 2,
                        isOpen: true,
                        address: "Tarh El Bahr St, Al Sharq, Port Said",
                        addressAr: "شارع طرح البحر، حي الشرق، بورسعيد",
                        lat: 31.2635,
                        lng: 32.3060
                    )
                ]
            ),
            ServicePlace(
                id: "place_2",
                name: "Dr. Sara Dental Clinic",
                nameAr: "عيادة د. سارة لطب وجراحة الأسنان",
                iconName: "cross.case.fill",
                color: AppColors.teal,
                category: .clinics,
                starRating: 4.9,
                reviewCount: 420,
                keywords: ["dental", "teeth", "doctor", "clinic", "dentist", "اسنان", "أسنان", "طبيب", "دكتور", "سارة", "عيادة", "تقويم"],
                about: "A premier dental clinic offering digital smile design, painless checkups, pediatric dentistry, and teeth whitening with real-time queue notifications.",
                aboutAr: "عيادة أسنان تخصصية حديثة تقدم تصميم الابتسامة الرقمي وتقويم الأسنان وطب أسنان الأطفال بأحدث التقنيات مع نظام حجز طابور ذكي لتوفير وقتك.",
                workingHours: "10:00 AM – 10:00 PM",
                workingHoursAr: "١٠:٠٠ ص – ١٠:٠٠ م",
                branches: [
                    ServiceBranch(
                        id: "branch_2_1",
                        label: "New Cairo - Festival City",
                        labelAr: "فرع التجمع الخامس - كايرو فيستيفال سيتي",
                        distanceKm: "18.6",
                        waiting: 2,
                        isOpen: true,
                        address: "Business District, CFC, New Cairo",
                        addressAr: "مجمع الأعمال، كايرو فيستيفال، التجمع الخامس",
                        lat: 30.0305,
                        lng: 31.4080
                    ),
                    ServiceBranch(
                        id: "branch_2_2",
                        label: "Heliopolis - Merghany",
                        labelAr: "فرع مصر الجديدة - شارع الميرغني",
                        distanceKm: "11.2",
                        waiting: 3,
                        isOpen: true,
                        address: "68 El Merghany St, Heliopolis",
                        addressAr: "٦٨ شارع الميرغني، مصر الجديدة",
                        lat: 30.0890,
                        lng: 31.3285
                    ),
                    ServiceBranch(
                        id: "branch_2_3",
                        label: "Sheikh Zayed - Capital Park",
                        labelAr: "فرع الشيخ زايد - كابيتال بارك",
                        distanceKm: "28.5",
                        waiting: 1,
                        isOpen: true,
                        address: "Building B2, Capital Business Park, Zayed",
                        addressAr: "مبنى B2، كابيتال بيزنس بارك، الشيخ زايد",
                        lat: 30.0163,
                        lng: 30.9837
                    ),
                    ServiceBranch(
                        id: "branch_2_4",
                        label: "Port Said - El Gomhouria St",
                        labelAr: "فرع بورسعيد - شارع الجمهورية",
                        distanceKm: "1.8",
                        waiting: 1,
                        isOpen: true,
                        address: "El Gomhouria St, Port Said",
                        addressAr: "شارع الجمهورية، أمام حديقة المنتزه، بورسعيد",
                        lat: 31.2585,
                        lng: 32.2905
                    )
                ]
            ),
            ServicePlace(
                id: "place_3",
                name: "Al Borg Diagnostics & Labs",
                nameAr: "معامل ومختبرات البرج التخصصية",
                iconName: "cross.case.fill",
                color: AppColors.teal,
                category: .clinics,
                starRating: 4.8,
                reviewCount: 980,
                keywords: ["lab", "blood", "test", "analysis", "تحاليل", "معمل", "دم", "فحص", "البرج"],
                about: "Accredited medical diagnostic lab network with accurate clinical testing and instant queue slot dispatch.",
                aboutAr: "شبكة معامل ومختبرات طبية معتمدة تقدم أدق التحاليل والمسحات المخبرية مع حجز أسرع دور سحب عينات.",
                workingHours: "07:00 AM – 11:00 PM",
                workingHoursAr: "٠٧:٠٠ ص – ١١:٠٠ م",
                branches: [
                    ServiceBranch(
                        id: "branch_3_1",
                        label: "Dokki - Mesaha Square",
                        labelAr: "فرع الدقي - ميدان المساحة",
                        distanceKm: "1.8",
                        waiting: 5,
                        isOpen: true,
                        address: "12 Mesaha Sq, Dokki, Giza",
                        addressAr: "١٢ ميدان المساحة، الدقي، الجيزة",
                        lat: 30.0380,
                        lng: 31.2120
                    ),
                    ServiceBranch(
                        id: "branch_3_2",
                        label: "New Maadi - Laselki",
                        labelAr: "فرع المعادي - شارع اللاسلكي",
                        distanceKm: "14.2",
                        waiting: 2,
                        isOpen: true,
                        address: "24 Laselki St, New Maadi",
                        addressAr: "٢٤ شارع اللاسلكي، المعادي الجديدة",
                        lat: 29.9755,
                        lng: 31.2790
                    ),
                    ServiceBranch(
                        id: "branch_3_3",
                        label: "Mohandessin - Gameat Dowal",
                        labelAr: "فرع المهندسين - جامعة الدول",
                        distanceKm: "3.2",
                        waiting: 4,
                        isOpen: true,
                        address: "45 Gameat El Dowal St, Mohandessin",
                        addressAr: "٤٥ شارع جامعة الدول العربية، المهندسين",
                        lat: 30.0540,
                        lng: 31.2015
                    ),
                    ServiceBranch(
                        id: "branch_3_4",
                        label: "Port Said - 23rd of July St",
                        labelAr: "فرع بورسعيد - شارع ٢٣ يوليو",
                        distanceKm: "2.1",
                        waiting: 3,
                        isOpen: true,
                        address: "23rd of July St, Port Said",
                        addressAr: "شارع ٢٣ يوليو، حي المناخ، بورسعيد",
                        lat: 31.2590,
                        lng: 32.2940
                    )
                ]
            ),
            ServicePlace(
                id: "place_4",
                name: "Cairo Eye & Vision Center",
                nameAr: "مركز القاهرة للعيون والليزك",
                iconName: "eye.fill",
                color: AppColors.teal,
                category: .clinics,
                starRating: 4.7,
                reviewCount: 350,
                keywords: ["eye", "lasik", "vision", "doctor", "عيون", "ليزك", "نظارات", "طبيب", "فحص"],
                about: "Advanced ophthalmic care, ultra-lasik surgery, and cornea diagnostics under supervision of senior consultants.",
                aboutAr: "مركز متخصص في جراحات الفيمتو ليزك وتصحيح الإبصار وفحوصات القرنية وعلاج المياه البيضاء بإشراف استشاريين.",
                workingHours: "10:00 AM – 09:00 PM",
                workingHoursAr: "١٠:٠٠ ص – ٠٩:٠٠ م",
                branches: [
                    ServiceBranch(
                        id: "branch_4_1",
                        label: "New Cairo - North 90th",
                        labelAr: "فرع التجمع الخامس - التسعين الشمالي",
                        distanceKm: "33.3",
                        waiting: 1,
                        isOpen: true,
                        address: "Medical Park 1, North 90th St",
                        addressAr: "ميديكال بارك ١، شارع التسعين الشمالي",
                        lat: 30.0270,
                        lng: 31.4766
                    ),
                    ServiceBranch(
                        id: "branch_4_2",
                        label: "Nasr City - Abbas El Akkad",
                        labelAr: "فرع مدينة نصر - عباس العقاد",
                        distanceKm: "9.5",
                        waiting: 3,
                        isOpen: true,
                        address: "58 Abbas El Akkad St, Nasr City",
                        addressAr: "٥٨ شارع عباس العقاد، مدينة نصر",
                        lat: 30.0610,
                        lng: 31.3390
                    )
                ]
            ),
            ServicePlace(
                id: "place_5",
                name: "Mirror Beauty & Barber Salon",
                nameAr: "صالون ميرور للحلاقة والتجميل",
                iconName: "scissors",
                color: AppColors.indigo,
                category: .salons,
                starRating: 4.8,
                reviewCount: 780,
                keywords: ["salon", "barber", "hair", "beauty", "حلاقة", "صالون", "شعر", "كوافير", "تجميل", "ميرور"],
                about: "Luxury grooming and styling experience with master stylists, beard care, and facial treatments in a peaceful atmosphere.",
                aboutAr: "تجربة عناية وتصفيف فاخرة مع أمهر مصففي الشعر وخبراء العناية بالبشرة واللحية في أجواء هادئة ومريحة.",
                workingHours: "10:00 AM – 12:00 AM",
                workingHoursAr: "١٠:٠٠ ص – ١٢:٠٠ منتصف الليل",
                branches: [
                    ServiceBranch(
                        id: "branch_5_1",
                        label: "Zamalek Main Branch",
                        labelAr: "فرع الزمالك - شارع البرازيل",
                        distanceKm: "5.8",
                        waiting: 3,
                        isOpen: true,
                        address: "18 Brazil St, Zamalek, Cairo",
                        addressAr: "١٨ شارع البرازيل، الزمالك، القاهرة",
                        lat: 30.0612,
                        lng: 31.2185
                    ),
                    ServiceBranch(
                        id: "branch_5_2",
                        label: "Sheikh Zayed - Capital Park",
                        labelAr: "فرع الشيخ زايد - كابيتال بيزنس بارك",
                        distanceKm: "28.6",
                        waiting: 1,
                        isOpen: true,
                        address: "Building B4, Capital Business Park",
                        addressAr: "مبنى B4، كابيتال بيزنس بارك، الشيخ زايد",
                        lat: 30.0163,
                        lng: 30.9837
                    ),
                    ServiceBranch(
                        id: "branch_5_3",
                        label: "New Cairo - Downtown Mall",
                        labelAr: "فرع التجمع الخامس - الداون تاون",
                        distanceKm: "22.0",
                        waiting: 2,
                        isOpen: true,
                        address: "Downtown Mall, Sector A, New Cairo",
                        addressAr: "داون تاون مول، القطاع أ، التجمع الخامس",
                        lat: 30.0120,
                        lng: 31.4350
                    ),
                    ServiceBranch(
                        id: "branch_5_4",
                        label: "Port Said Marina Branch",
                        labelAr: "فرع بورسعيد - الممشى السياحي",
                        distanceKm: "1.5",
                        waiting: 2,
                        isOpen: true,
                        address: "Tourist Walkway, Al Sharq, Port Said",
                        addressAr: "الممشى السياحي، حي الشرق، بورسعيد",
                        lat: 31.2610,
                        lng: 32.3080
                    )
                ]
            ),
            ServicePlace(
                id: "place_6",
                name: "Tarek El Sagheer Luxury Salon",
                nameAr: "صالون طارق الصغير للتصفيف الفاخر",
                iconName: "scissors",
                color: AppColors.indigo,
                category: .salons,
                starRating: 4.9,
                reviewCount: 610,
                keywords: ["salon", "barber", "grooming", "تصفيف", "شعر", "كوافير", "طارق الصغير"],
                about: "High-end hair styling, VIP grooming sessions, and signature care rituals for gentlemen and youth.",
                aboutAr: "صالون تصفيف راقٍ يقدم أحدث قصات الشعر وجلسات العناية الملكية في خصوصية تامة.",
                workingHours: "11:00 AM – 11:00 PM",
                workingHoursAr: "١١:٠٠ ص – ١١:٠٠ م",
                branches: [
                    ServiceBranch(
                        id: "branch_6_1",
                        label: "Heliopolis - Thawra St",
                        labelAr: "فرع مصر الجديدة - شارع الثورة",
                        distanceKm: "12.4",
                        waiting: 4,
                        isOpen: true,
                        address: "94 El Thawra St, Heliopolis",
                        addressAr: "٩٤ شارع الثورة، مصر الجديدة",
                        lat: 30.0910,
                        lng: 31.3395
                    ),
                    ServiceBranch(
                        id: "branch_6_2",
                        label: "Sheikh Zayed - Arkan Plaza",
                        labelAr: "فرع الشيخ زايد - أركان بلازا",
                        distanceKm: "29.1",
                        waiting: 2,
                        isOpen: true,
                        address: "Arkan Plaza, Phase 2, Zayed",
                        addressAr: "أركان بلازا، المرحلة الثانية، الشيخ زايد",
                        lat: 30.0125,
                        lng: 30.9850
                    )
                ]
            ),
            ServicePlace(
                id: "place_7",
                name: "Glossy Beauty & Spa Lounge",
                nameAr: "بيوتي لاونج جلوسي للسيدات",
                iconName: "sparkles",
                color: AppColors.indigo,
                category: .salons,
                starRating: 4.9,
                reviewCount: 540,
                keywords: ["spa", "glow", "massage", "nails", "skincare", "سبا", "تجميل", "مساج", "بشرة", "أظافر", "سيدات"],
                about: "Full-body relaxation, organic facials, hammam rituals, and premium nail artistry in private luxury suites.",
                aboutAr: "عناية متكاملة بالبشرة والمساج والحمام المغربي الفاخر والعناية بالأظافر في أجنحة خاصة للسيدات.",
                workingHours: "10:00 AM – 09:30 PM",
                workingHoursAr: "١٠:٠٠ ص – ٠٩:٣٠ م",
                branches: [
                    ServiceBranch(
                        id: "branch_7_1",
                        label: "Maadi - Degla St 216",
                        labelAr: "فرع المعادي - دجلة شارع 216",
                        distanceKm: "15.0",
                        waiting: 3,
                        isOpen: true,
                        address: "Villa 14, St 216, Degla, Maadi",
                        addressAr: "فيلا ١٤، شارع ٢١٦، دجلة، المعادي",
                        lat: 29.9620,
                        lng: 31.2840
                    ),
                    ServiceBranch(
                        id: "branch_7_2",
                        label: "New Cairo - North 70th",
                        labelAr: "فرع التجمع الخامس - السبعين الشمالي",
                        distanceKm: "24.5",
                        waiting: 2,
                        isOpen: true,
                        address: "North 70th Axis, Plaza Mall",
                        addressAr: "محور السبعين الشمالي، بلازا مول",
                        lat: 30.0380,
                        lng: 31.4650
                    )
                ]
            ),
            ServicePlace(
                id: "place_8",
                name: "Cairo Fix Auto Repair",
                nameAr: "مركز كايرو فيكس لصيانة السيارات",
                iconName: "wrench.and.screwdriver.fill",
                color: AppColors.amber,
                category: .garages,
                starRating: 4.6,
                reviewCount: 890,
                keywords: ["car", "auto", "repair", "mechanic", "oil", "brakes", "صيانة", "سيارات", "ميكانيكي", "فرامل", "زيوت"],
                about: "Comprehensive computerized diagnostics, periodic maintenance, brake servicing, and quick oil changes with live bay status.",
                aboutAr: "فحص شامل بالكمبيوتر وصيانة دورية وتغيير زيوت وفرامل بأحدث الأجهزة مع متابعة حية لمرحلة صيانة سيارتك.",
                workingHours: "08:00 AM – 09:00 PM",
                workingHoursAr: "٠٨:٠٠ ص – ٠٩:٠٠ م",
                branches: [
                    ServiceBranch(
                        id: "branch_8_1",
                        label: "Mohandessin Main Branch",
                        labelAr: "الفرع الرئيسي - المهندسين",
                        distanceKm: "3.5",
                        waiting: 5,
                        isOpen: true,
                        address: "29 Shehab St, Mohandessin, Giza",
                        addressAr: "٢٩ شارع شهاب، المهندسين، الجيزة",
                        lat: 30.0520,
                        lng: 31.2010
                    ),
                    ServiceBranch(
                        id: "branch_8_2",
                        label: "New Maadi - Laselki",
                        labelAr: "فرع المعادي - شارع اللاسلكي",
                        distanceKm: "14.6",
                        waiting: 2,
                        isOpen: true,
                        address: "7 Laselki St, New Maadi",
                        addressAr: "٧ شارع اللاسلكي، المعادي الجديدة",
                        lat: 29.9752,
                        lng: 31.2785
                    ),
                    ServiceBranch(
                        id: "branch_8_3",
                        label: "Nasr City - Mostafa El Nahas",
                        labelAr: "فرع مدينة نصر - مصطفى النحاس",
                        distanceKm: "10.5",
                        waiting: 4,
                        isOpen: true,
                        address: "112 Mostafa El Nahas St, Nasr City",
                        addressAr: "١١٢ شارع مصطفى النحاس، مدينة نصر",
                        lat: 30.0530,
                        lng: 31.3650
                    )
                ]
            ),
            ServicePlace(
                id: "place_9",
                name: "Fit & Fix Tyres & Service",
                nameAr: "مركز فيت أند فيكس السريع للسيارات",
                iconName: "wrench.and.screwdriver.fill",
                color: AppColors.amber,
                category: .garages,
                starRating: 4.7,
                reviewCount: 1150,
                keywords: ["tyres", "wheels", "fit", "fix", "إطارات", "كاوتش", "بطاريات", "زوايا"],
                about: "Fast tyre replacements, computer wheel balancing, battery checks, and express oil change.",
                aboutAr: "تغيير إطارات وبطاريات وضبط زوايا وترصيص بالليزر مع صيانة دورية سريعة.",
                workingHours: "08:00 AM – 10:00 PM",
                workingHoursAr: "٠٨:٠٠ ص – ١٠:٠٠ م",
                branches: [
                    ServiceBranch(
                        id: "branch_9_1",
                        label: "Dokki - Mossadak",
                        labelAr: "فرع الدقي - شارع مصدق",
                        distanceKm: "2.1",
                        waiting: 3,
                        isOpen: true,
                        address: "33 Mossadak St, Dokki, Giza",
                        addressAr: "٣٣ شارع مصدق، الدقي، الجيزة",
                        lat: 30.0405,
                        lng: 31.2095
                    ),
                    ServiceBranch(
                        id: "branch_9_2",
                        label: "Nozha - Joseph Tito",
                        labelAr: "فرع النزهة - طريق جوزيف تيتو",
                        distanceKm: "16.8",
                        waiting: 4,
                        isOpen: true,
                        address: "Joseph Tito Axis, Nozha El Gedida",
                        addressAr: "محور جوزيف تيتو، النزهة الجديدة",
                        lat: 30.1250,
                        lng: 31.3780
                    ),
                    ServiceBranch(
                        id: "branch_9_3",
                        label: "Maadi - Autostrad",
                        labelAr: "فرع المعادي - طريق الأوتوستراد",
                        distanceKm: "13.9",
                        waiting: 1,
                        isOpen: true,
                        address: "Autostrad Rd, near Total Station, Maadi",
                        addressAr: "طريق الأوتوستراد، بجوار توتال، المعادي",
                        lat: 29.9700,
                        lng: 31.2950
                    ),
                    ServiceBranch(
                        id: "branch_9_4",
                        label: "Port Said Ring Road Branch",
                        labelAr: "فرع بورسعيد - طريق الميناء الدائري",
                        distanceKm: "3.4",
                        waiting: 2,
                        isOpen: true,
                        address: "Port Said Ring Road, Industrial Zone",
                        addressAr: "طريق بورسعيد الدائري، مدخل المدينة الصناعي",
                        lat: 31.2380,
                        lng: 32.2650
                    )
                ]
            ),
            ServicePlace(
                id: "place_10",
                name: "AutoStar German Car Care",
                nameAr: "أوتو ستار الألمانية لصيانة السيارات",
                iconName: "car.fill",
                color: AppColors.amber,
                category: .garages,
                starRating: 4.8,
                reviewCount: 430,
                keywords: ["german", "bmw", "mercedes", "audi", "صيانة", "ألماني", "مرسيدس", "بي إم"],
                about: "Specialized service center for German vehicles with genuine diagnostic software and certified engineers.",
                aboutAr: "مركز معتمد لصيانة وبرمجة السيارات الألمانية وفحص الأعطال المعقدة وتغيير قطع الغيار الأصلية.",
                workingHours: "09:00 AM – 07:30 PM",
                workingHoursAr: "٠٩:٠٠ ص – ٠٧:٣٠ م",
                branches: [
                    ServiceBranch(
                        id: "branch_10_1",
                        label: "6th of October Industrial Area",
                        labelAr: "فرع 6 أكتوبر - المنطقة الصناعية",
                        distanceKm: "26.0",
                        waiting: 3,
                        isOpen: true,
                        address: "3rd Industrial Zone, 6th of October",
                        addressAr: "المنطقة الصناعية الثالثة، مدينة 6 أكتوبر",
                        lat: 29.9320,
                        lng: 30.9150
                    ),
                    ServiceBranch(
                        id: "branch_10_2",
                        label: "1st Settlement Industrial Zone",
                        labelAr: "فرع التجمع الأول - المنطقة الصناعية",
                        distanceKm: "21.3",
                        waiting: 2,
                        isOpen: true,
                        address: "Workshops Zone, 1st Settlement",
                        addressAr: "منطقة الورش، التجمع الأول، القاهرة الجديدة",
                        lat: 30.0650,
                        lng: 31.4550
                    )
                ]
            ),
            ServicePlace(
                id: "place_11",
                name: "Downtown Documentation & Legal Office",
                nameAr: "مكتب وسط البلد للاستشارات والتوثيق",
                iconName: "building.columns.fill",
                color: AppColors.deepTeal,
                category: .offices,
                starRating: 4.5,
                reviewCount: 670,
                keywords: ["law", "notary", "office", "contracts", "توثيق", "محاماة", "قانون", "عقود", "مكتب", "شهر عقاري"],
                about: "Certified legal consultation, contract drafting, trademark registration, and notary queue scheduling.",
                aboutAr: "صياغة وتوثيق العقود الرسمية وتسجيل العلامات التجارية والاستشارات القانونية المتخصصة عبر طابور رقمي فائق التنظيم.",
                workingHours: "09:00 AM – 05:00 PM",
                workingHoursAr: "٠٩:٠٠ ص – ٠٥:٠٠ م",
                branches: [
                    ServiceBranch(
                        id: "branch_11_1",
                        label: "Downtown - Qasr El Nil",
                        labelAr: "فرع وسط البلد - شارع قصر النيل",
                        distanceKm: "2.7",
                        waiting: 3,
                        isOpen: true,
                        address: "22 Qasr El Nil St., Downtown Cairo",
                        addressAr: "٢٢ شارع قصر النيل، وسط البلد",
                        lat: 30.0477,
                        lng: 31.2316
                    ),
                    ServiceBranch(
                        id: "branch_11_2",
                        label: "New Cairo - Government Hub",
                        labelAr: "فرع التجمع الخامس - مجمع المصالح",
                        distanceKm: "23.4",
                        waiting: 4,
                        isOpen: true,
                        address: "Government Complex, Sector 1, New Cairo",
                        addressAr: "مجمع المصالح الحكومية، القطاع الأول، التجمع الخامس",
                        lat: 30.0150,
                        lng: 31.4420
                    ),
                    ServiceBranch(
                        id: "branch_11_3",
                        label: "Heliopolis - Roxy Square",
                        labelAr: "فرع مصر الجديدة - ميدان روكسي",
                        distanceKm: "11.8",
                        waiting: 2,
                        isOpen: true,
                        address: "14 Roxy Sq, Heliopolis",
                        addressAr: "١٤ ميدان روكسي، مصر الجديدة",
                        lat: 30.0920,
                        lng: 31.3170
                    )
                ]
            ),
            ServicePlace(
                id: "place_12",
                name: "Smart Citizen Service Center",
                nameAr: "مركز خدمة المواطنين الذكي",
                iconName: "building.columns.fill",
                color: AppColors.deepTeal,
                category: .offices,
                starRating: 4.8,
                reviewCount: 1120,
                keywords: ["citizen", "government", "id", "passports", "خدمات", "مواطنين", "بطاقة", "جوازات", "حكومي"],
                about: "Next-generation automated citizen service hub for civil registry, ID renewals, and document issuance.",
                aboutAr: "مركز متطور لخدمات المواطنين والمعاملات الحكومية وإصدار بطاقات الرقم القومي والشهادات إلكترونياً.",
                workingHours: "08:00 AM – 06:00 PM",
                workingHoursAr: "٠٨:٠٠ ص – ٠٦:٠٠ م",
                branches: [
                    ServiceBranch(
                        id: "branch_12_1",
                        label: "1st Settlement - Naguib Axis",
                        labelAr: "فرع التجمع الأول - محور محمد نجيب",
                        distanceKm: "22.5",
                        waiting: 5,
                        isOpen: true,
                        address: "Mohamed Naguib Axis, 1st Settlement",
                        addressAr: "محور محمد نجيب، التجمع الأول، القاهرة الجديدة",
                        lat: 30.0480,
                        lng: 31.4620
                    ),
                    ServiceBranch(
                        id: "branch_12_2",
                        label: "Sheikh Zayed - 8th District",
                        labelAr: "فرع الشيخ زايد - الحي الثامن",
                        distanceKm: "27.8",
                        waiting: 3,
                        isOpen: true,
                        address: "Block 8, Behind Zayed City Council",
                        addressAr: "المجاورة الثامنة، خلف جهاز مدينة الشيخ زايد",
                        lat: 30.0210,
                        lng: 30.9760
                    ),
                    ServiceBranch(
                        id: "branch_12_3",
                        label: "Nasr City - Makram Ebeid",
                        labelAr: "فرع مدينة نصر - مكرم عبيد",
                        distanceKm: "9.2",
                        waiting: 4,
                        isOpen: true,
                        address: "78 Makram Ebeid St, Nasr City",
                        addressAr: "٧٨ شارع مكرم عبيد، مدينة نصر",
                        lat: 30.0620,
                        lng: 31.3510
                    ),
                    ServiceBranch(
                        id: "branch_12_4",
                        label: "Port Said Smart Services Hub",
                        labelAr: "فرع بورسعيد - مجمع المصالح الحكومية النموذجي",
                        distanceKm: "1.9",
                        waiting: 3,
                        isOpen: true,
                        address: "Port Said Government Complex, 23rd July St",
                        addressAr: "مجمع المصالح الحكومية، شارع ٢٣ يوليو، بورسعيد",
                        lat: 31.2575,
                        lng: 32.2890
                    )
                ]
            ),
            ServicePlace(
                id: "place_13",
                name: "Egypt Post Express Hub",
                nameAr: "مكتب البريد المصري الممتاز",
                iconName: "envelope.fill",
                color: AppColors.deepTeal,
                category: .offices,
                starRating: 4.6,
                reviewCount: 730,
                keywords: ["post", "mail", "parcel", "postal", "بريد", "حوالات", "طرود", "بوستة"],
                about: "Express parcel delivery, postal savings accounts, and financial transfers with rapid queue dispatch.",
                aboutAr: "خدمات البريد السريع والحوالات المالية والطرود وتوفير البريد عبر حجز طابور رقمي دون انتظار.",
                workingHours: "08:00 AM – 06:00 PM",
                workingHoursAr: "٠٨:٠٠ ص – ٠٦:٠٠ م",
                branches: [
                    ServiceBranch(
                        id: "branch_13_1",
                        label: "Zamalek - Shagaret El Dorr",
                        labelAr: "فرع الزمالك - شارع شجرة الدر",
                        distanceKm: "4.9",
                        waiting: 2,
                        isOpen: true,
                        address: "8 Shagaret El Dorr St, Zamalek",
                        addressAr: "٨ شارع شجرة الدر، الزمالك",
                        lat: 30.0635,
                        lng: 31.2205
                    ),
                    ServiceBranch(
                        id: "branch_13_2",
                        label: "Attaba - Main Square",
                        labelAr: "فرع العتبة - الميدان الرئيسي",
                        distanceKm: "3.8",
                        waiting: 6,
                        isOpen: true,
                        address: "Historical Post Office Bldg, Attaba Sq",
                        addressAr: "مبنى البوستة التاريخي، ميدان العتبة",
                        lat: 30.0515,
                        lng: 31.2485
                    ),
                    ServiceBranch(
                        id: "branch_13_3",
                        label: "Mohandessin - Syria St",
                        labelAr: "فرع المهندسين - شارع سوريا",
                        distanceKm: "3.9",
                        waiting: 3,
                        isOpen: true,
                        address: "19 Syria St, Mohandessin",
                        addressAr: "١٩ شارع سوريا، المهندسين",
                        lat: 30.0555,
                        lng: 31.2060
                    ),
                    ServiceBranch(
                        id: "branch_13_4",
                        label: "Port Said Historical Post Office",
                        labelAr: "فرع بورسعيد الرئيسي - شارع فلسطين وديليسبس",
                        distanceKm: "1.1",
                        waiting: 2,
                        isOpen: true,
                        address: "Palestine St, Port Said",
                        addressAr: "شارع فلسطين، بجوار الممشى السياحي، بورسعيد",
                        lat: 31.2612,
                        lng: 32.3082
                    )
                ]
            )
        ]
    }

    private static func seedAlerts() -> [AppAlertItem] {
        [
            AppAlertItem(
                type: .turn,
                titleAr: "اقترب دورك في عيادة د. سارة!",
                titleEn: "Your turn is approaching!",
                descAr: "أنت الآن الثاني في طابور عيادة الأسنان (#A-12). يرجى التوجه لغرفة 3.",
                descEn: "You are 2nd in line (#A-12) for Dr. Sara Dental Clinic. Please head to Room 3.",
                timeAgoAr: "منذ دقيقتين",
                timeAgoEn: "2m ago",
                isRead: false
            ),
            AppAlertItem(
                type: .join,
                titleAr: "تم إصدار تذكرتك بنجاح",
                titleEn: "Ticket Reserved Successfully",
                descAr: "تذكرتك رقم #A-12 محجوزة لدى عيادة د. سارة للأسنان (فرع مدينة نصر).",
                descEn: "Ticket #A-12 confirmed at Dr. Sara Dental Clinic (Nasr City).",
                timeAgoAr: "منذ 10 دقائق",
                timeAgoEn: "10m ago",
                isRead: false
            ),
            AppAlertItem(
                type: .open,
                titleAr: "صالون ميرور بدأ استقبال العملاء",
                titleEn: "Mirror Salon Queue Now Open",
                descAr: "بدأ استقبال الطابور الصباحي في فرع الزمالك. متوسط الانتظار 10 دقائق.",
                descEn: "Morning queue is now open at Zamalek branch. Average wait is 10 mins.",
                timeAgoAr: "منذ ساعة",
                timeAgoEn: "1h ago",
                isRead: true
            ),
            AppAlertItem(
                type: .go,
                titleAr: "سيارتك جاهزة للاستلام",
                titleEn: "Your Car Is Ready for Pickup",
                descAr: "انتهت صيانة سيارتك في مركز كايرو فيكس. شكراً لاختيارك طابور.",
                descEn: "Service completed at Cairo Fix Auto. Thank you for using Taboor.",
                timeAgoAr: "أمس",
                timeAgoEn: "Yesterday",
                isRead: true
            )
        ]
    }

    private static func seedHistory() -> [TicketHistoryItem] {
        [
            TicketHistoryItem(
                ticketNumber: "M-04",
                serviceName: "Mirror Beauty & Barber Salon",
                serviceNameAr: "صالون ميرور للحلاقة والتجميل",
                branchLabel: "Zamalek Branch",
                branchLabelAr: "فرع الزمالك - شارع البرازيل",
                dateString: "أمس — 04:30 م",
                status: .completed,
                durationMinutes: 18
            ),
            TicketHistoryItem(
                ticketNumber: "C-19",
                serviceName: "Cairo Fix Auto Repair",
                serviceNameAr: "مركز كايرو فيكس لصيانة السيارات",
                branchLabel: "Mohandessin Branch",
                branchLabelAr: "فرع المهندسين - شارع شهاب",
                dateString: "الأسبوع الماضي — 11:15 ص",
                status: .completed,
                durationMinutes: 42
            ),
            TicketHistoryItem(
                ticketNumber: "L-08",
                serviceName: "Downtown Notary & Legal Office",
                serviceNameAr: "مكتب وسط البلد للاستشارات والتوثيق",
                branchLabel: "Downtown Branch",
                branchLabelAr: "فرع وسط البلد - قصر النيل",
                dateString: "12 أكتوبر 2026",
                status: .cancelled,
                durationMinutes: 6
            )
        ]
    }
}
