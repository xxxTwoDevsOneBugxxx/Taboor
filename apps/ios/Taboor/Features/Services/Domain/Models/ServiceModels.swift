import SwiftUI

// MARK: - Service Category

public enum ServiceCategory: String, CaseIterable, Identifiable, Hashable, Codable {
    case clinics
    case salons
    case garages
    case offices

    public var id: String { rawValue }

    public func title(isArabic: Bool) -> String {
        switch self {
        case .clinics: return isArabic ? "عيادات ومستشفيات" : "Clinics"
        case .salons: return isArabic ? "صالونات وتجميل" : "Salons"
        case .garages: return isArabic ? "ورش وصيانة" : "Auto Repair"
        case .offices: return isArabic ? "مكاتب وخدمات" : "Offices"
        }
    }

    public var iconName: String {
        switch self {
        case .clinics: return "cross.case.fill"
        case .salons: return "scissors"
        case .garages: return "wrench.and.screwdriver.fill"
        case .offices: return "building.columns.fill"
        }
    }

    public var color: Color {
        switch self {
        case .clinics: return AppColors.teal
        case .salons: return AppColors.indigo
        case .garages: return AppColors.amber
        case .offices: return AppColors.deepTeal
        }
    }
}

// MARK: - Sorting and Filtering

public enum ServiceSortFilter: String, CaseIterable, Identifiable {
    case all
    case nearest
    case fastest
    case topRated
    case openNow

    public var id: String { rawValue }

    public func title(isArabic: Bool) -> String {
        switch self {
        case .all: return isArabic ? "الكل" : "All"
        case .nearest: return isArabic ? "الأقرب" : "Nearest"
        case .fastest: return isArabic ? "أسرع طابور" : "Fastest Queue"
        case .topRated: return isArabic ? "الأعلى تقييماً" : "Top Rated"
        case .openNow: return isArabic ? "مفتوح الآن" : "Open Now"
        }
    }

    public var iconName: String {
        switch self {
        case .all: return "sparkles"
        case .nearest: return "location.fill"
        case .fastest: return "bolt.fill"
        case .topRated: return "star.fill"
        case .openNow: return "clock.badge.checkmark.fill"
        }
    }
}

// MARK: - Service Branch

public struct ServiceBranch: Identifiable, Hashable, Codable {
    public let id: String
    public let label: String
    public let labelAr: String
    public let distanceKm: String
    public var waiting: Int
    public let isOpen: Bool
    public let address: String
    public let addressAr: String
    public let lat: Double
    public let lng: Double

    public init(
        id: String = UUID().uuidString,
        label: String,
        labelAr: String,
        distanceKm: String,
        waiting: Int,
        isOpen: Bool = true,
        address: String,
        addressAr: String,
        lat: Double,
        lng: Double
    ) {
        self.id = id
        self.label = label
        self.labelAr = labelAr
        self.distanceKm = distanceKm
        self.waiting = waiting
        self.isOpen = isOpen
        self.address = address
        self.addressAr = addressAr
        self.lat = lat
        self.lng = lng
    }

    public var estWaitMinutes: Int {
        max(3, Int(Double(waiting) * 4.5))
    }

    public func localizedLabel(isArabic: Bool) -> String {
        isArabic ? labelAr : label
    }

    public func localizedAddress(isArabic: Bool) -> String {
        isArabic ? addressAr : address
    }
}

// MARK: - Service Place

public struct ServicePlace: Identifiable, Hashable {
    public let id: String
    public let name: String
    public let nameAr: String
    public let iconName: String
    public let color: Color
    public let category: ServiceCategory
    public let starRating: Double
    public let reviewCount: Int
    public let keywords: [String]
    public let about: String
    public let aboutAr: String
    public let workingHours: String
    public let workingHoursAr: String
    public var branches: [ServiceBranch]

    public init(
        id: String,
        name: String,
        nameAr: String,
        iconName: String,
        color: Color,
        category: ServiceCategory,
        starRating: Double = 4.7,
        reviewCount: Int = 128,
        keywords: [String] = [],
        about: String,
        aboutAr: String,
        workingHours: String,
        workingHoursAr: String,
        branches: [ServiceBranch]
    ) {
        self.id = id
        self.name = name
        self.nameAr = nameAr
        self.iconName = iconName
        self.color = color
        self.category = category
        self.starRating = starRating
        self.reviewCount = reviewCount
        self.keywords = keywords
        self.about = about
        self.aboutAr = aboutAr
        self.workingHours = workingHours
        self.workingHoursAr = workingHoursAr
        self.branches = branches
    }

    public var totalWaiting: Int {
        branches.reduce(0) { $0 + $1.waiting }
    }

    public var primaryBranch: ServiceBranch? {
        branches.first
    }

    public func localizedName(isArabic: Bool) -> String {
        isArabic ? nameAr : name
    }

    public func localizedAbout(isArabic: Bool) -> String {
        isArabic ? aboutAr : about
    }

    public func localizedWorkingHours(isArabic: Bool) -> String {
        isArabic ? workingHoursAr : workingHours
    }

    public func matches(query: String) -> Bool {
        let q = query.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        if q.isEmpty { return true }
        if name.lowercased().contains(q) { return true }
        if nameAr.lowercased().contains(q) { return true }
        if about.lowercased().contains(q) { return true }
        if aboutAr.lowercased().contains(q) { return true }
        if keywords.contains(where: { $0.lowercased().contains(q) }) { return true }
        if branches.contains(where: {
            $0.label.lowercased().contains(q) ||
            $0.labelAr.lowercased().contains(q) ||
            $0.address.lowercased().contains(q) ||
            $0.addressAr.lowercased().contains(q)
        }) { return true }
        return false
    }

    public static func == (lhs: ServicePlace, rhs: ServicePlace) -> Bool {
        lhs.id == rhs.id
    }

    public func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }
}

// MARK: - User Active Queue Ticket

public struct UserTicket: Identifiable, Equatable, Codable {
    public let id: String
    public var ticketNumber: String
    public var serviceName: String
    public var serviceNameAr: String
    public var branchLabel: String
    public var branchLabelAr: String
    public var counterLabel: String
    public var counterLabelAr: String
    public var waitingAhead: Int
    public var estMinutes: Int
    public var currentServing: String
    public var progress: Double
    public var joinedAt: Date
    public var isActive: Bool

    public init(
        id: String = UUID().uuidString,
        ticketNumber: String = "A-12",
        serviceName: String = "Dr. Sara Dental Clinic",
        serviceNameAr: String = "عيادة د. سارة لطب وجراحة الأسنان",
        branchLabel: String = "Main Branch - Nasr City",
        branchLabelAr: String = "الفرع الرئيسي - مدينة نصر",
        counterLabel: String = "Room 3 — 2nd Floor",
        counterLabelAr: String = "عيادة 3 — الدور الثاني",
        waitingAhead: Int = 2,
        estMinutes: Int = 8,
        currentServing: String = "A-10",
        progress: Double = 0.70,
        joinedAt: Date = Date(),
        isActive: Bool = true
    ) {
        self.id = id
        self.ticketNumber = ticketNumber
        self.serviceName = serviceName
        self.serviceNameAr = serviceNameAr
        self.branchLabel = branchLabel
        self.branchLabelAr = branchLabelAr
        self.counterLabel = counterLabel
        self.counterLabelAr = counterLabelAr
        self.waitingAhead = waitingAhead
        self.estMinutes = estMinutes
        self.currentServing = currentServing
        self.progress = progress
        self.joinedAt = joinedAt
        self.isActive = isActive
    }

    public func localizedServiceName(isArabic: Bool) -> String {
        isArabic ? serviceNameAr : serviceName
    }

    public func localizedBranch(isArabic: Bool) -> String {
        isArabic ? branchLabelAr : branchLabel
    }

    public func localizedCounter(isArabic: Bool) -> String {
        isArabic ? counterLabelAr : counterLabel
    }
}

// MARK: - Ticket History Item

public enum TicketStatus: String, Codable {
    case completed
    case cancelled
}

public struct TicketHistoryItem: Identifiable, Equatable, Codable {
    public let id: String
    public let ticketNumber: String
    public let serviceName: String
    public let serviceNameAr: String
    public let branchLabel: String
    public let branchLabelAr: String
    public let dateString: String
    public let status: TicketStatus
    public let durationMinutes: Int

    public init(
        id: String = UUID().uuidString,
        ticketNumber: String,
        serviceName: String,
        serviceNameAr: String,
        branchLabel: String,
        branchLabelAr: String,
        dateString: String,
        status: TicketStatus = .completed,
        durationMinutes: Int = 22
    ) {
        self.id = id
        self.ticketNumber = ticketNumber
        self.serviceName = serviceName
        self.serviceNameAr = serviceNameAr
        self.branchLabel = branchLabel
        self.branchLabelAr = branchLabelAr
        self.dateString = dateString
        self.status = status
        self.durationMinutes = durationMinutes
    }
}

// MARK: - App Alert Item

public enum AlertType: String, Codable {
    case join
    case open
    case turn
    case go
    case thanks

    public var iconName: String {
        switch self {
        case .join: return "checkmark.circle.fill"
        case .open: return "door.left.hand.open"
        case .turn: return "clock.badge.fill"
        case .go: return "bell.and.waves.left.and.right.fill"
        case .thanks: return "star.circle.fill"
        }
    }

    public var color: Color {
        switch self {
        case .join: return AppColors.teal
        case .open: return AppColors.indigo
        case .turn: return AppColors.amber
        case .go: return AppColors.accentRed
        case .thanks: return AppColors.teal
        }
    }
}

public struct AppAlertItem: Identifiable, Equatable, Codable {
    public let id: String
    public let type: AlertType
    public let titleAr: String
    public let titleEn: String
    public let descAr: String
    public let descEn: String
    public let timeAgoAr: String
    public let timeAgoEn: String
    public var isRead: Bool

    public init(
        id: String = UUID().uuidString,
        type: AlertType,
        titleAr: String,
        titleEn: String,
        descAr: String,
        descEn: String,
        timeAgoAr: String,
        timeAgoEn: String,
        isRead: Bool = false
    ) {
        self.id = id
        self.type = type
        self.titleAr = titleAr
        self.titleEn = titleEn
        self.descAr = descAr
        self.descEn = descEn
        self.timeAgoAr = timeAgoAr
        self.timeAgoEn = timeAgoEn
        self.isRead = isRead
    }

    public func localizedTitle(isArabic: Bool) -> String {
        isArabic ? titleAr : titleEn
    }

    public func localizedDesc(isArabic: Bool) -> String {
        isArabic ? descAr : descEn
    }

    public func localizedTimeAgo(isArabic: Bool) -> String {
        isArabic ? timeAgoAr : timeAgoEn
    }
}
