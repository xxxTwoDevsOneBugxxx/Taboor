import Foundation
import ActivityKit

// MARK: - Queue Live Activity Attributes (Dynamic Island & Lock Screen Standard)

public struct QueueActivityAttributes: ActivityAttributes {
    public struct ContentState: Codable, Hashable {
        public var currentServing: String
        public var waitingAhead: Int
        public var estMinutes: Int
        public var progress: Double
        public var statusMessage: String

        public init(
            currentServing: String,
            waitingAhead: Int,
            estMinutes: Int,
            progress: Double,
            statusMessage: String
        ) {
            self.currentServing = currentServing
            self.waitingAhead = waitingAhead
            self.estMinutes = estMinutes
            self.progress = progress
            self.statusMessage = statusMessage
        }
    }

    // Static metadata fixed for this ticket session
    public var ticketNumber: String
    public var serviceName: String
    public var branchName: String
    public var counterName: String

    public init(
        ticketNumber: String,
        serviceName: String,
        branchName: String,
        counterName: String
    ) {
        self.ticketNumber = ticketNumber
        self.serviceName = serviceName
        self.branchName = branchName
        self.counterName = counterName
    }
}
