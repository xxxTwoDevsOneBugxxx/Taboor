import Foundation
import CoreLocation
import SwiftUI

// MARK: - Production API Data Transfer Objects (DTOs)
// Matching ASP.NET Core Clean Architecture API Contracts

// MARK: - 1. Place & Branch DTOs
public struct CounterDTO: Codable {
    public let id: String
    public let number: Int
    public let nameEn: String
    public let nameAr: String
    public let servingTicket: String?
    public let isOpen: Bool
}

public struct BranchDTO: Codable {
    public let id: String
    public let nameEn: String
    public let nameAr: String
    public let addressEn: String
    public let addressAr: String
    public let latitude: Double
    public let longitude: Double
    public let phone: String?
    public let openingHours: String?
    public let counters: [CounterDTO]?

    public func toDomain() -> ServiceBranch {
        ServiceBranch(
            id: id,
            label: nameEn,
            labelAr: nameAr,
            distanceKm: "4.5",
            waiting: counters?.filter { $0.isOpen }.count ?? 2,
            isOpen: true,
            address: addressEn,
            addressAr: addressAr,
            lat: latitude,
            lng: longitude
        )
    }
}

public struct PlaceDTO: Codable {
    public let id: String
    public let nameEn: String
    public let nameAr: String
    public let category: String
    public let rating: Double
    public let reviewCount: Int
    public let isOpen: Bool?
    public let activeQueuesCount: Int?
    public let estimatedWaitMinutes: Int?
    public let imageUrl: String?
    public let branches: [BranchDTO]

    public func toDomain() -> ServicePlace {
        let domainCat = ServiceCategory(rawValue: category) ?? .clinics
        let domainBranches = branches.map { $0.toDomain() }

        return ServicePlace(
            id: id,
            name: nameEn,
            nameAr: nameAr,
            iconName: domainCat.iconName,
            color: domainCat.color,
            category: domainCat,
            starRating: rating,
            reviewCount: reviewCount,
            keywords: [nameEn.lowercased(), nameAr],
            about: "Smart queue enabled branch with live tracking and instant check-in.",
            aboutAr: "فرع معتمد بنظام طابور الذكي مع تحديثات فورية وحجز سريع.",
            workingHours: "09:00 AM – 10:00 PM",
            workingHoursAr: "٠٩:٠٠ ص – ١٠:٠٠ م",
            branches: domainBranches
        )
    }
}

// MARK: - 2. Active Ticket & Booking DTOs
public struct TicketDTO: Codable {
    public let id: String
    public let ticketNumber: String
    public let serviceName: String
    public let serviceNameAr: String
    public let branchLabel: String
    public let branchLabelAr: String
    public let counterLabel: String
    public let counterLabelAr: String
    public let waitingAhead: Int
    public let estMinutes: Int
    public let currentServing: String
    public let progress: Double
    public let isActive: Bool
    public let bookingType: String
    public let placeId: String
    public let latitude: Double
    public let longitude: Double
    public let scheduledDate: String?
    public let scheduledTimeSlot: String?
    public let doctorNameAr: String?
    public let specialityAr: String?

    public func toDomain() -> UserTicket {
        let type: TicketBookingType = (bookingType == "scheduled") ? .scheduled : .liveQueue

        return UserTicket(
            id: id,
            ticketNumber: ticketNumber,
            serviceName: serviceName,
            serviceNameAr: serviceNameAr,
            branchLabel: branchLabel,
            branchLabelAr: branchLabelAr,
            counterLabel: counterLabel,
            counterLabelAr: counterLabelAr,
            waitingAhead: waitingAhead,
            estMinutes: estMinutes,
            currentServing: currentServing,
            progress: progress,
            isActive: isActive,
            bookingType: type,
            scheduledDate: scheduledDate,
            scheduledTime: scheduledTimeSlot,
            placeId: placeId,
            latitude: latitude,
            longitude: longitude
        )
    }
}

// MARK: - 3. History DTO
public struct TicketHistoryDTO: Codable {
    public let id: String
    public let ticketNumber: String
    public let placeNameAr: String
    public let placeNameEn: String
    public let branchNameAr: String
    public let branchNameEn: String
    public let serviceCategory: String
    public let dateStringAr: String
    public let dateStringEn: String
    public let status: String
    public let durationMinutes: Int
    public let counterNumber: String
    public let placeId: String

    public func toDomain() -> TicketHistoryItem {
        let st: TicketStatus = (status == "cancelled") ? .cancelled : .completed

        return TicketHistoryItem(
            id: id,
            ticketNumber: ticketNumber,
            serviceName: placeNameEn,
            serviceNameAr: placeNameAr,
            branchLabel: branchNameEn,
            branchLabelAr: branchNameAr,
            dateString: dateStringAr,
            status: st,
            durationMinutes: durationMinutes
        )
    }
}

// MARK: - 4. Alert / Notification DTO
public struct AlertItemDTO: Codable {
    public let id: String
    public let type: String
    public let titleAr: String
    public let titleEn: String
    public let descAr: String
    public let descEn: String
    public let timeAgoAr: String
    public let timeAgoEn: String
    public let isRead: Bool

    public func toDomain() -> AppAlertItem {
        let alertType: AlertType
        switch type {
        case "turn": alertType = .turn
        case "join": alertType = .join
        case "open": alertType = .open
        case "go": alertType = .go
        case "thanks": alertType = .thanks
        default: alertType = .turn
        }

        return AppAlertItem(
            id: id,
            type: alertType,
            titleAr: titleAr,
            titleEn: titleEn,
            descAr: descAr,
            descEn: descEn,
            timeAgoAr: timeAgoAr,
            timeAgoEn: timeAgoEn,
            isRead: isRead
        )
    }
}

// MARK: - 5. Requests (Paylods for Write Operations)
public struct JoinQueueRequestDTO: Encodable {
    public let placeId: String
    public let branchId: String
    public let counterId: String?

    public init(placeId: String, branchId: String, counterId: String? = nil) {
        self.placeId = placeId
        self.branchId = branchId
        self.counterId = counterId
    }
}

public struct BookAppointmentRequestDTO: Encodable {
    public let placeId: String
    public let branchId: String
    public let date: String
    public let timeSlot: String
    public let doctorName: String?

    public init(placeId: String, branchId: String, date: String, timeSlot: String, doctorName: String? = nil) {
        self.placeId = placeId
        self.branchId = branchId
        self.date = date
        self.timeSlot = timeSlot
        self.doctorName = doctorName
    }
}

public struct DelayTicketRequestDTO: Encodable {
    public let ticketId: String
    public let delayMinutes: Int

    public init(ticketId: String, delayMinutes: Int) {
        self.ticketId = ticketId
        self.delayMinutes = delayMinutes
    }
}
