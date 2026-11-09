import Foundation
import CoreSpotlight
import UniformTypeIdentifiers
import SwiftUI

// MARK: - Native iOS CoreSpotlight Indexing Service
// Allows users to search for Taboor clinics, places, doctors, and active tickets directly from iOS Spotlight

@MainActor
public final class SpotlightService {
    public static let shared = SpotlightService()

    private let domainPlaces = "com.taboor.places"
    private let domainTickets = "com.taboor.tickets"

    private init() {}

    // MARK: - 1. Index Places / Clinics / Services
    public func indexPlaces(_ places: [ServicePlace]) {
        guard CSSearchableIndex.isIndexingAvailable() else {
            print("[Spotlight] Indexing is not available on this device")
            return
        }

        var items: [CSSearchableItem] = []

        for place in places {
            let attributeSet = CSSearchableItemAttributeSet(contentType: .content)
            
            // Localized bilingual title
            attributeSet.title = "\(place.nameAr) (\(place.name))"
            
            // Description with working hours and category
            let branchSummary = place.branches.first.map { " • \($0.labelAr)" } ?? ""
            attributeSet.contentDescription = "\(place.aboutAr)\(branchSummary) • المواعيد: \(place.workingHoursAr)"
            
            // Rich Search Keywords in Arabic and English
            var keywords: Set<String> = [
                place.name,
                place.nameAr,
                "طابور",
                "حجز",
                "عيادة",
                "مستشفى",
                "دكتور",
                "صالون",
                "ورشة",
                "مركز",
                "taboor",
                "queue",
                "booking",
                "clinic",
                "hospital",
                "doctor"
            ]
            keywords.formUnion(place.keywords)
            for branch in place.branches {
                keywords.insert(branch.label)
                keywords.insert(branch.labelAr)
            }
            attributeSet.keywords = Array(keywords)
            
            // Domain & Expiration (1 month)
            attributeSet.domainIdentifier = domainPlaces
            
            let item = CSSearchableItem(
                uniqueIdentifier: "place:\(place.id)",
                domainIdentifier: domainPlaces,
                attributeSet: attributeSet
            )
            item.expirationDate = Date().addingTimeInterval(30 * 24 * 3600)
            items.append(item)
        }

        CSSearchableIndex.default().indexSearchableItems(items) { error in
            if let error = error {
                print("[Spotlight] Failed to index places: \(error.localizedDescription)")
            } else {
                print("[Spotlight] Successfully indexed \(items.count) places into iOS Spotlight")
            }
        }
    }

    // MARK: - 2. Index Active Ticket
    public func indexActiveTicket(_ ticket: UserTicket) {
        guard CSSearchableIndex.isIndexingAvailable() else { return }

        let attributeSet = CSSearchableItemAttributeSet(contentType: .content)
        attributeSet.title = "تذكرة طابور #\(ticket.ticketNumber) • \(ticket.serviceNameAr)"
        attributeSet.contentDescription = "الدور الحالي: \(ticket.currentServing) • أمامك: \(ticket.waitingAhead) أشخاص • الوقت المتبقي: \(ticket.estMinutes)د"
        
        attributeSet.keywords = [
            "تذكرة",
            "طابور",
            "دوري",
            "دور",
            "انتظار",
            ticket.ticketNumber,
            "#\(ticket.ticketNumber)",
            ticket.serviceNameAr,
            ticket.branchLabelAr,
            "ticket",
            "taboor",
            "my turn"
        ]
        attributeSet.domainIdentifier = domainTickets

        let item = CSSearchableItem(
            uniqueIdentifier: "ticket:\(ticket.ticketNumber)",
            domainIdentifier: domainTickets,
            attributeSet: attributeSet
        )
        // Tickets expire after 12 hours
        item.expirationDate = Date().addingTimeInterval(12 * 3600)

        CSSearchableIndex.default().indexSearchableItems([item]) { error in
            if let error = error {
                print("[Spotlight] Failed to index active ticket: \(error.localizedDescription)")
            } else {
                print("[Spotlight] Successfully indexed ticket #\(ticket.ticketNumber) into iOS Spotlight")
            }
        }
    }

    // MARK: - 3. De-index Ticket (On complete / cancel)
    public func deindexTicket(ticketNumber: String) {
        CSSearchableIndex.default().deleteSearchableItems(withIdentifiers: ["ticket:\(ticketNumber)"]) { error in
            if let error = error {
                print("[Spotlight] Failed to delete ticket item: \(error.localizedDescription)")
            } else {
                print("[Spotlight] Deleted ticket #\(ticketNumber) from Spotlight")
            }
        }
    }

    // MARK: - 4. Handle Spotlight Search Selection Navigation
    public func handleSpotlightActivity(_ activity: NSUserActivity) -> Bool {
        guard activity.activityType == CSSearchableItemActionType,
              let identifier = activity.userInfo?[CSSearchableItemActivityIdentifier] as? String else {
            return false
        }

        print("[Spotlight] Handling Spotlight selection with ID: \(identifier)")

        if identifier.hasPrefix("ticket:") {
            // Open Live Queue Tracking screen
            QueueDataService.shared.openLiveTracking()
            return true
        } else if identifier.hasPrefix("place:") {
            let placeId = String(identifier.dropFirst("place:".count))
            QueueDataService.shared.selectPlaceFromSpotlight(id: placeId)
            return true
        }

        return false
    }
}
