import Foundation
import AppIntents

// MARK: - 1. Check Queue Status Intent (Siri Voice Interaction)

public struct CheckQueueStatusIntent: AppIntent {
    public static var title: LocalizedStringResource = "الاستعلام عن دوري في طابور"
    public static var description: IntentDescription = IntentDescription("معرفة رقم تذكرتك وموقعك الحالي في الطابور والوقت المتبقي")

    public static var openAppWhenRun: Bool = false

    public init() {}

    @MainActor
    public func perform() async throws -> some ProvidesDialog & ReturnsValue<String> {
        let dataService = QueueDataService.shared

        if let ticket = dataService.activeTicket {
            let message = "تذكرتك رقم \(ticket.ticketNumber) في \(ticket.serviceNameAr). أمامك \(ticket.waitingAhead) أشخاص، والدور الحالي هو \(ticket.currentServing). الوقت المتوقع حوالي \(ticket.estMinutes) دقائق."
            return .result(value: ticket.ticketNumber, dialog: IntentDialog(stringLiteral: message))
        } else {
            let message = "ليس لديك أي تذكرة نشطة حالياً في طابور. يمكنك فتح التطبيق وحجز دورك بسهولة."
            return .result(value: "لا توجد تذكرة", dialog: IntentDialog(stringLiteral: message))
        }
    }
}

// MARK: - 2. Postpone Queue Turn Intent (Action via Siri or Shortcut)

public struct PostponeQueueIntent: AppIntent {
    public static var title: LocalizedStringResource = "تأجيل دوري في طابور"
    public static var description: IntentDescription = IntentDescription("تأجيل موعد دورك في الطابور لمدة 10 دقائق إضافية")

    public static var openAppWhenRun: Bool = false

    public init() {}

    @MainActor
    public func perform() async throws -> some ProvidesDialog {
        let dataService = QueueDataService.shared

        if let ticket = dataService.activeTicket {
            dataService.delayActiveTicket(minutes: 10)
            let message = "تم تأجيل دورك في \(ticket.serviceNameAr) بنجاح بمقدار 10 دقائق إضافية. أصبح وقت الانتظار المتوقع \(ticket.estMinutes + 10) دقائق."
            return .result(dialog: IntentDialog(stringLiteral: message))
        } else {
            let message = "لا توجد تذكرة نشطة حالياً لتأجيلها."
            return .result(dialog: IntentDialog(stringLiteral: message))
        }
    }
}

// MARK: - 3. Siri Shortcuts Provider (Automatic Discovery by iOS)

public struct TaboorShortcuts: AppShortcutsProvider {
    public static var shortcutTileColor: ShortcutTileColor = .teal

    public static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: CheckQueueStatusIntent(),
            phrases: [
                "Check my \(.applicationName) status",
                "Check my status in \(.applicationName)",
                "What's my turn in \(.applicationName)",
                "What is my turn in \(.applicationName)",
                "My turn in \(.applicationName)",
                "Check \(.applicationName)",
                "دوري كام في \(.applicationName)",
                "استعلم عن دوري في \(.applicationName)",
                "حالة الطابور في \(.applicationName)",
                "دوري في \(.applicationName)"
            ],
            shortTitle: "دوري في طابور",
            systemImageName: "ticket.fill"
        )

        AppShortcut(
            intent: PostponeQueueIntent(),
            phrases: [
                "Postpone my \(.applicationName) queue",
                "Delay my \(.applicationName) turn",
                "Postpone my turn in \(.applicationName)",
                "Delay turn in \(.applicationName)",
                "أجل دوري في \(.applicationName)",
                "تأخير دوري في \(.applicationName)",
                "تأجيل طابور \(.applicationName)"
            ],
            shortTitle: "تأجيل الدور",
            systemImageName: "clock.arrow.circlepath"
        )
    }
}
