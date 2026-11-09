import UIKit
import SwiftUI

// MARK: - Native iOS Ticket Sharing Helper (Family & Friends)

public struct ShareTicketHelper {
    public static func shareTicket(_ ticket: UserTicket, isArabic: Bool) {
        UIImpactFeedbackGenerator(style: .medium).impactOccurred()

        let serviceTitle = ticket.localizedServiceName(isArabic: isArabic)
        let branchTitle = ticket.localizedBranch(isArabic: isArabic)
        let counterTitle = ticket.localizedCounter(isArabic: isArabic)

        let shareText = isArabic
            ? """
            🎟️ تذكرة طابور الذكية
            🏢 \(serviceTitle)
            📍 \(branchTitle)
            🪟 \(counterTitle)

            🔢 رقم تذكرتك: \(ticket.ticketNumber)
            👥 أمامك في الانتظار: \(ticket.waitingAhead)
            ⏱️ الوقت المتوقع: حوالي \(ticket.estMinutes) دقائق

            📲 تابع دورك مباشرة لحظة بلحظة:
            https://taboor.app/ticket/\(ticket.ticketNumber)
            """
            : """
            🎟️ Taboor Smart Queue Ticket
            🏢 \(serviceTitle)
            📍 \(branchTitle)
            🪟 \(counterTitle)

            🔢 Ticket Number: \(ticket.ticketNumber)
            👥 People Ahead: \(ticket.waitingAhead)
            ⏱️ Estimated Wait: ~\(ticket.estMinutes) mins

            📲 Track live queue turn & status:
            https://taboor.app/ticket/\(ticket.ticketNumber)
            """

        let activityVC = UIActivityViewController(activityItems: [shareText], applicationActivities: nil)

        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootVC = windowScene.windows.first?.rootViewController {
            var topVC = rootVC
            while let presented = topVC.presentedViewController {
                topVC = presented
            }

            if let popover = activityVC.popoverPresentationController {
                popover.sourceView = topVC.view
                popover.sourceRect = CGRect(x: topVC.view.bounds.midX, y: topVC.view.bounds.midY, width: 0, height: 0)
                popover.permittedArrowDirections = []
            }

            topVC.present(activityVC, animated: true)
        }
    }
}
