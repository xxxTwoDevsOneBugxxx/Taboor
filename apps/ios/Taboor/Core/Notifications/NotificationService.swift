import Foundation
import UserNotifications
import UIKit

// MARK: - Native Interactive Notification Service (Actionable iOS Alerts)

public final class NotificationService: NSObject, ObservableObject, UNUserNotificationCenterDelegate {
    public static let shared = NotificationService()

    // Notification Action Identifiers
    public static let actionPostpone10 = "ACTION_POSTPONE_10"
    public static let actionOnMyWay = "ACTION_ON_MY_WAY"
    public static let actionCancel = "ACTION_CANCEL_TICKET"

    // Category Identifier
    public static let queueAlertCategory = "QUEUE_ALERT_CATEGORY"

    @Published public private(set) var isAuthorized: Bool = false

    private override init() {
        super.init()
    }

    /// Configures the notification center and registers interactive action categories
    public func configure() {
        let center = UNUserNotificationCenter.current()
        center.delegate = self

        // 1. Define Actions
        let postponeAction = UNNotificationAction(
            identifier: Self.actionPostpone10,
            title: "تأجيل 10 دقائق ⏱️",
            options: []
        )

        let onMyWayAction = UNNotificationAction(
            identifier: Self.actionOnMyWay,
            title: "أنا في طريقي 🚶‍♂️",
            options: [.foreground]
        )

        let cancelAction = UNNotificationAction(
            identifier: Self.actionCancel,
            title: "إلغاء التذكرة ❌",
            options: [.destructive]
        )

        // 2. Define Category with Actions
        let queueCategory = UNNotificationCategory(
            identifier: Self.queueAlertCategory,
            actions: [postponeAction, onMyWayAction, cancelAction],
            intentIdentifiers: [],
            options: [.customDismissAction]
        )

        // 3. Register Category with System
        center.setNotificationCategories([queueCategory])
    }

    /// Requests user notification permissions
    public func requestAuthorization() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            DispatchQueue.main.async {
                self.isAuthorized = granted
                if let error = error {
                    print("[Notifications] Authorization error: \(error.localizedDescription)")
                } else {
                    print("[Notifications] Permission granted: \(granted)")
                }
            }
        }
    }

    /// Sends or schedules a rich interactive notification when turn is approaching
    public func sendTurnApproachingNotification(for ticket: UserTicket) {
        let content = UNMutableNotificationContent()
        content.title = "اقترب دورك في طابور! 🔔"
        content.subtitle = ticket.serviceNameAr
        content.body = "أمامك فقط \(ticket.waitingAhead) أشخاص (الدور الحالي: \(ticket.currentServing)). شباك الخدمة: \(ticket.counterLabelAr)."
        content.sound = UNNotificationSound(named: UNNotificationSoundName("\(TaboorSensoryManager.shared.selectedChime.resourceName).caf"))
        content.categoryIdentifier = Self.queueAlertCategory
        content.userInfo = [
            "ticketNumber": ticket.ticketNumber,
            "serviceName": ticket.serviceNameAr
        ]

        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1.0, repeats: false)
        let request = UNNotificationRequest(
            identifier: "turn_approaching_\(ticket.ticketNumber)",
            content: content,
            trigger: trigger
        )

        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("[Notifications] Failed to schedule notification: \(error.localizedDescription)")
            } else {
                print("[Notifications] Successfully delivered turn alert for #\(ticket.ticketNumber)")
            }
        }
    }

    // MARK: - UNUserNotificationCenterDelegate

    // Deliver notification banner even while the app is in the foreground
    public func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound, .badge, .list])
    }

    // Handle user tapping quick actions on the notification banner (Lock Screen or Banner)
    public func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        DispatchQueue.main.async {
            switch response.actionIdentifier {
            case Self.actionPostpone10:
                print("[Notifications] User tapped Postpone 10m from notification")
                QueueDataService.shared.delayActiveTicket(minutes: 10)
                UINotificationFeedbackGenerator().notificationOccurred(.success)

            case Self.actionCancel:
                print("[Notifications] User confirmed cancel from notification")
                QueueDataService.shared.cancelActiveTicket()
                UINotificationFeedbackGenerator().notificationOccurred(.warning)

            case Self.actionOnMyWay:
                print("[Notifications] User tapped On My Way")
                UIImpactFeedbackGenerator(style: .light).impactOccurred()

            default:
                break
            }
            completionHandler()
        }
    }
}
