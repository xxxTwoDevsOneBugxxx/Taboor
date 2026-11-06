import SwiftUI
import Combine

// MARK: - Queue Data Service (Central Observable State for Authenticated Flows)

@MainActor
public final class QueueDataService: ObservableObject {
    public static let shared = QueueDataService()

    @Published public var activeTicket: UserTicket?
    @Published public var places: [ServicePlace] = []
    @Published public var alerts: [AppAlertItem] = []
    @Published public var history: [TicketHistoryItem] = []

    public var unreadAlertsCount: Int {
        alerts.filter { !$0.isRead }.count
    }

    private init() {
        self.places = Self.seedPlaces()
        self.alerts = Self.seedAlerts()
        self.history = Self.seedHistory()
        // Default active ticket for instant interactivity
        self.activeTicket = UserTicket(
            ticketNumber: "A-12",
            serviceName: "Dr. Sara Dental Clinic",
            serviceNameAr: "عيادة د. سارة لطب وجراحة الأسنان",
            branchLabel: "Main Branch - Nasr City",
            branchLabelAr: "الفرع الرئيسي - مدينة نصر",
            counterLabel: "Room 3 — 2nd Floor",
            counterLabelAr: "عيادة 3 — الدور الثاني",
            waitingAhead: 2,
            estMinutes: 8,
            currentServing: "A-10",
            progress: 0.70,
            isActive: true
        )
    }

    // MARK: - Queue Operations

    @discardableResult
    public func joinQueue(place: ServicePlace, branch: ServiceBranch) -> UserTicket {
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
            waitingAhead: max(1, branch.waiting),
            estMinutes: branch.estWaitMinutes,
            currentServing: "\(prefix)-0\(max(1, randomNum - branch.waiting))",
            progress: 0.15,
            joinedAt: Date(),
            isActive: true
        )

        self.activeTicket = newTicket

        // Add corresponding confirmation notification
        let newAlert = AppAlertItem(
            type: .join,
            titleAr: "تم إصدار تذكرتك بنجاح",
            titleEn: "Ticket Issued Successfully",
            descAr: "تذكرتك رقم #\(ticketNum) في \(place.nameAr) جاهزة. أمامك \(branch.waiting) فقط.",
            descEn: "Ticket #\(ticketNum) at \(place.name) is active. \(branch.waiting) ahead in line.",
            timeAgoAr: "الآن",
            timeAgoEn: "Just now",
            isRead: false
        )
        self.alerts.insert(newAlert, at: 0)

        return newTicket
    }

    public func delayActiveTicket(minutes: Int = 10) {
        guard var ticket = activeTicket else { return }
        ticket.estMinutes += minutes
        ticket.waitingAhead += 2
        ticket.progress = max(0.1, ticket.progress - 0.2)
        self.activeTicket = ticket
    }

    public func cancelActiveTicket() {
        guard let ticket = activeTicket else { return }

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
        self.activeTicket = nil
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
        filter: ServiceSortFilter = .all
    ) -> [ServicePlace] {
        var results = places.filter { place in
            let matchesCat = (category == nil || place.category == category)
            let matchesQ = place.matches(query: query)
            return matchesCat && matchesQ
        }

        switch filter {
        case .all:
            break
        case .nearest:
            results.sort {
                (Double($0.primaryBranch?.distanceKm ?? "99") ?? 99) < (Double($1.primaryBranch?.distanceKm ?? "99") ?? 99)
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
        [
            ServicePlace(
                id: "place_1",
                name: "Dr. Sara Dental Clinic",
                nameAr: "عيادة د. سارة لطب وجراحة الأسنان",
                iconName: "cross.case.fill",
                color: AppColors.teal,
                category: .clinics,
                starRating: 4.9,
                reviewCount: 342,
                keywords: ["dental", "teeth", "doctor", "clinic", "dentist", "اسنان", "أسنان", "طبيب", "دكتور", "سارة", "عيادة", "تقويم"],
                about: "A premier dental clinic offering digital smile design, painless checkups, pediatric dentistry, and teeth whitening with real-time queue notifications.",
                aboutAr: "عيادة أسنان تخصصية حديثة تقدم تصميم الابتسامة الرقمي وتقويم الأسنان وطب أسنان الأطفال بأحدث التقنيات مع نظام حجز طابور ذكي لتوفير وقتك.",
                workingHours: "09:00 AM – 10:00 PM",
                workingHoursAr: "٠٩:٠٠ ص – ١٠:٠٠ م",
                branches: [
                    ServiceBranch(
                        id: "branch_1_1",
                        label: "Main Branch - Nasr City",
                        labelAr: "الفرع الرئيسي - مدينة نصر",
                        distanceKm: "0.8",
                        waiting: 2,
                        isOpen: true,
                        address: "42 Makram Ebeid St., Nasr City",
                        addressAr: "٤٢ شارع مكرم عبيد، مدينة نصر",
                        lat: 30.0525,
                        lng: 31.2408
                    ),
                    ServiceBranch(
                        id: "branch_1_2",
                        label: "Downtown Branch - Tahrir",
                        labelAr: "فرع وسط البلد - التحرير",
                        distanceKm: "1.9",
                        waiting: 4,
                        isOpen: true,
                        address: "18 Talaat Harb St., Downtown",
                        addressAr: "١٨ شارع طلعت حرب، وسط البلد",
                        lat: 30.0501,
                        lng: 31.2426
                    )
                ]
            ),
            ServicePlace(
                id: "place_2",
                name: "Mirror Beauty & Barber Salon",
                nameAr: "صالون ميرور للحلاقة والتجميل",
                iconName: "scissors",
                color: AppColors.indigo,
                category: .salons,
                starRating: 4.7,
                reviewCount: 215,
                keywords: ["salon", "barber", "hair", "beauty", "حلاقة", "صالون", "شعر", "كوافير", "تجميل", "ميرور"],
                about: "Luxury grooming and styling experience with master stylists, beard care, and facial treatments in a peaceful atmosphere.",
                aboutAr: "تجربة عناية وتصفيف فاخرة مع أمهر مصففي الشعر وخبراء العناية بالبشرة واللحية في أجواء هادئة ومريحة.",
                workingHours: "10:00 AM – 11:30 PM",
                workingHoursAr: "١٠:٠٠ ص – ١١:٣٠ م",
                branches: [
                    ServiceBranch(
                        id: "branch_2_1",
                        label: "Zamalek Branch - 26th July",
                        labelAr: "الفرع الرئيسي - الزمالك",
                        distanceKm: "0.4",
                        waiting: 3,
                        isOpen: true,
                        address: "14 Brazil St., Zamalek",
                        addressAr: "١٤ شارع البرازيل، الزمالك",
                        lat: 30.0584,
                        lng: 31.2341
                    ),
                    ServiceBranch(
                        id: "branch_2_2",
                        label: "Capital Business Park - Zayed",
                        labelAr: "فرع كابيتال بيزنس بارك - الشيخ زايد",
                        distanceKm: "3.2",
                        waiting: 1,
                        isOpen: true,
                        address: "Building B4, Capital Business Park",
                        addressAr: "مبنى B4، كابيتال بيزنس بارك، الشيخ زايد",
                        lat: 30.0375,
                        lng: 31.2135
                    )
                ]
            ),
            ServicePlace(
                id: "place_3",
                name: "Cairo Fix Auto Repair",
                nameAr: "مركز كايرو فيكس لصيانة السيارات",
                iconName: "wrench.and.screwdriver.fill",
                color: AppColors.amber,
                category: .garages,
                starRating: 4.6,
                reviewCount: 189,
                keywords: ["car", "auto", "repair", "mechanic", "oil", "brakes", "صيانة", "سيارات", "ميكانيكي", "فرامل", "زيوت"],
                about: "Comprehensive computerized diagnostics, periodic maintenance, brake servicing, and quick oil changes with live bay status.",
                aboutAr: "فحص شامل بالكمبيوتر وصيانة دورية وتغيير زيوت وفرامل بأحدث الأجهزة مع متابعة حية لمرحلة صيانة سيارتك.",
                workingHours: "08:00 AM – 09:00 PM",
                workingHoursAr: "٠٨:٠٠ ص – ٠٩:٠٠ م",
                branches: [
                    ServiceBranch(
                        id: "branch_3_1",
                        label: "Mohandessin Branch - Shehab St.",
                        labelAr: "الفرع الرئيسي - المهندسين",
                        distanceKm: "1.5",
                        waiting: 5,
                        isOpen: true,
                        address: "29 Shehab St., Mohandessin",
                        addressAr: "٢٩ شارع شهاب، المهندسين",
                        lat: 30.0672,
                        lng: 31.2358
                    ),
                    ServiceBranch(
                        id: "branch_3_2",
                        label: "Maadi Branch - Laselki",
                        labelAr: "فرع المعادي - اللاسلكي",
                        distanceKm: "2.8",
                        waiting: 2,
                        isOpen: true,
                        address: "7 Laselki St., New Maadi",
                        addressAr: "٧ شارع اللاسلكي، المعادي الجديدة",
                        lat: 30.0894,
                        lng: 31.2421
                    )
                ]
            ),
            ServicePlace(
                id: "place_4",
                name: "New Cairo Eye & Lasik Center",
                nameAr: "مركز القاهرة الجديدة للعيون والليزك",
                iconName: "eye.fill",
                color: AppColors.teal,
                category: .clinics,
                starRating: 4.8,
                reviewCount: 270,
                keywords: ["eye", "lasik", "vision", "doctor", "عيون", "ليزك", "نظارات", "طبيب", "فحص"],
                about: "Advanced ophthalmic care, ultra-lasik surgery, and cornea diagnostics under supervision of senior consultants.",
                aboutAr: "مركز متخصص في جراحات الفيمتو ليزك وتصحيح الإبصار وفحوصات القرنية وعلاج المياه البيضاء بإشراف استشاريين.",
                workingHours: "10:00 AM – 08:00 PM",
                workingHoursAr: "١٠:٠٠ ص – ٠٨:٠٠ م",
                branches: [
                    ServiceBranch(
                        id: "branch_4_1",
                        label: "North 90th St. - New Cairo",
                        labelAr: "فرع التسعين الشمالي - التجمع الخامس",
                        distanceKm: "2.1",
                        waiting: 1,
                        isOpen: true,
                        address: "North 90th St., Medical Park 1",
                        addressAr: "شارع التسعين الشمالي، ميديكال بارك ١",
                        lat: 30.0187,
                        lng: 31.4725
                    )
                ]
            ),
            ServicePlace(
                id: "place_5",
                name: "Downtown Notary & Legal Office",
                nameAr: "مكتب وسط البلد للاستشارات والتوثيق",
                iconName: "building.columns.fill",
                color: AppColors.deepTeal,
                category: .offices,
                starRating: 4.3,
                reviewCount: 145,
                keywords: ["law", "notary", "office", "contracts", "توثيق", "محاماة", "قانون", "عقود", "مكتب", "شهر عقاري"],
                about: "Certified legal consultation, contract drafting, trademark registration, and notary queue scheduling.",
                aboutAr: "صياغة وتوثيق العقود الرسمية وتسجيل العلامات التجارية والاستشارات القانونية المتخصصة عبر طابور رقمي فائق التنظيم.",
                workingHours: "09:00 AM – 05:00 PM",
                workingHoursAr: "٠٩:٠٠ ص – ٠٥:٠٠ م",
                branches: [
                    ServiceBranch(
                        id: "branch_5_1",
                        label: "Qasr El Nil Branch - Downtown",
                        labelAr: "فرع وسط البلد - قصر النيل",
                        distanceKm: "3.0",
                        waiting: 3,
                        isOpen: true,
                        address: "22 Qasr El Nil St., Downtown Cairo",
                        addressAr: "٢٢ شارع قصر النيل، وسط البلد",
                        lat: 30.0477,
                        lng: 31.2316
                    )
                ]
            ),
            ServicePlace(
                id: "place_6",
                name: "Glow Spa & Wellness Lounge",
                nameAr: "صالون وسبا جلو بيوتي للسيدات",
                iconName: "sparkles",
                color: AppColors.indigo,
                category: .salons,
                starRating: 4.9,
                reviewCount: 310,
                keywords: ["spa", "glow", "massage", "nails", "skincare", "سبا", "تجميل", "مساج", "بشرة", "أظافر", "سيدات"],
                about: "Full-body relaxation, organic facials, hammam rituals, and premium nail artistry in private luxury suites.",
                aboutAr: "عناية متكاملة بالبشرة والمساج والحمام المغربي الفاخر والعناية بالأظافر في أجنحة خاصة للسيدات.",
                workingHours: "11:00 AM – 10:00 PM",
                workingHoursAr: "١١:٠٠ ص – ١٠:٠٠ م",
                branches: [
                    ServiceBranch(
                        id: "branch_6_1",
                        label: "Mossadak St. - Dokki",
                        labelAr: "فرع مصدق - الدقي",
                        distanceKm: "1.2",
                        waiting: 4,
                        isOpen: true,
                        address: "51 Mossadak St., Dokki",
                        addressAr: "٥١ شارع مصدق، الدقي",
                        lat: 30.0646,
                        lng: 31.2601
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
