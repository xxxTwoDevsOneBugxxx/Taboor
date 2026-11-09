import Foundation
import ActivityKit
import SwiftUI

// MARK: - Queue Live Activity & Dynamic Island Manager

@MainActor
public final class QueueLiveActivityManager: ObservableObject {
    public static let shared = QueueLiveActivityManager()

    @Published public private(set) var isActivityActive: Bool = false
    @Published public private(set) var activeTicketNumber: String? = nil

    private var currentActivity: Activity<QueueActivityAttributes>? = nil

    private init() {
        NotificationCenter.default.addObserver(
            forName: UIApplication.willResignActiveNotification,
            object: nil,
            queue: .main
        ) { [weak self] _ in
            Task { @MainActor [weak self] in
                guard let self = self else { return }
                if let ticket = QueueDataService.shared.activeTicket, ticket.isActive, self.currentActivity == nil {
                    print("[LiveActivity] willResignActive -> Starting Live Activity")
                    self.startLiveActivity(for: ticket)
                }
            }
        }

        NotificationCenter.default.addObserver(
            forName: UIApplication.didBecomeActiveNotification,
            object: nil,
            queue: .main
        ) { [weak self] _ in
            Task { @MainActor [weak self] in
                guard let self = self else { return }
                if self.currentActivity != nil {
                    print("[LiveActivity] didBecomeActive -> Ending Live Activity")
                    self.endLiveActivity()
                }
            }
        }
    }

    /// Handles scene phase changes to show Dynamic Island ONLY when outside the app
    public func handleScenePhaseChange(_ phase: ScenePhase, activeTicket: UserTicket?) {
        switch phase {
        case .inactive, .background:
            // When user starts swiping home or switching apps
            if let ticket = activeTicket, ticket.isActive, currentActivity == nil {
                print("[LiveActivity] App background/inactive -> Starting Dynamic Island activity")
                startLiveActivity(for: ticket)
            }
        case .active:
            // When user re-enters Taboor app
            if currentActivity != nil {
                print("[LiveActivity] App became active -> Ending Dynamic Island activity")
                endLiveActivity()
            }
        default:
            break
        }
    }

    /// Starts a Live Activity for an active user ticket
    public func startLiveActivity(for ticket: UserTicket) {
        guard ActivityAuthorizationInfo().areActivitiesEnabled else {
            print("[LiveActivity] Activities are not enabled in settings.")
            return
        }

        // End any existing activity first
        endLiveActivity()

        let attributes = QueueActivityAttributes(
            ticketNumber: ticket.ticketNumber,
            serviceName: ticket.serviceNameAr,
            branchName: ticket.branchLabelAr,
            counterName: ticket.counterLabelAr
        )

        let initialContentState = QueueActivityAttributes.ContentState(
            currentServing: ticket.currentServing,
            waitingAhead: ticket.waitingAhead,
            estMinutes: ticket.estMinutes,
            progress: ticket.progress,
            statusMessage: ticket.waitingAhead <= 1 ? "استعد! دورك القادم" : "أمامك \(ticket.waitingAhead) أشخاص"
        )

        do {
            let activity = try Activity<QueueActivityAttributes>.request(
                attributes: attributes,
                content: .init(state: initialContentState, staleDate: nil),
                pushType: nil
            )
            self.currentActivity = activity
            self.isActivityActive = true
            self.activeTicketNumber = ticket.ticketNumber
            print("[LiveActivity] Successfully requested activity ID: \(activity.id)")
        } catch {
            print("[LiveActivity] Failed to request live activity: \(error.localizedDescription)")
        }
    }

    /// Updates the Live Activity with the latest ticket metrics
    public func updateLiveActivity(with ticket: UserTicket) {
        guard let activity = currentActivity else {
            return
        }

        let updatedContentState = QueueActivityAttributes.ContentState(
            currentServing: ticket.currentServing,
            waitingAhead: ticket.waitingAhead,
            estMinutes: ticket.estMinutes,
            progress: ticket.progress,
            statusMessage: ticket.waitingAhead <= 1 ? "استعد! دورك القادم" : "أمامك \(ticket.waitingAhead) أشخاص"
        )

        Task {
            await activity.update(.init(state: updatedContentState, staleDate: nil))
            print("[LiveActivity] Updated activity ID: \(activity.id) with waiting: \(ticket.waitingAhead)")
        }
    }

    /// Ends the current Live Activity smoothly
    public func endLiveActivity() {
        // Also terminate any stale activities from previous runs
        let allActivities = Activity<QueueActivityAttributes>.activities
        for act in allActivities {
            Task {
                await act.end(nil, dismissalPolicy: .immediate)
            }
        }

        self.currentActivity = nil
        self.isActivityActive = false
        self.activeTicketNumber = nil
        print("[LiveActivity] Ended live activities.")
    }
}
