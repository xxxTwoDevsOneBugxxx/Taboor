import Foundation
import AppIntents
import ActivityKit

// MARK: - Live Activity Background Intent (No App Opening)

public struct DelayQueueLiveActivityIntent: LiveActivityIntent {
    public static var title: LocalizedStringResource = "تأجيل الدور 10 دقائق"
    public static var description = IntentDescription("تأجيل موعد دورك 10 دقائق مباشرة من الجزيرة التفاعلية في الخلفية")
    public static var openAppWhenRun: Bool = false

    public init() {}

    public func perform() async throws -> some IntentResult {
        // Update all active activities in-place without opening the app
        for activity in Activity<QueueActivityAttributes>.activities {
            var updatedState = activity.content.state
            updatedState.estMinutes += 10
            updatedState.statusMessage = "تم تأجيل دورك بمقدار 10 دقائق إضافية"
            
            await activity.update(
                .init(state: updatedState, staleDate: nil),
                alertConfiguration: nil
            )
            print("[LiveActivityIntent] Successfully delayed ticket in background by 10m")
        }

        return .result()
    }
}
