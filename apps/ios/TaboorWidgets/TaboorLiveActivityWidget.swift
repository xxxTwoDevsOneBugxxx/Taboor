import ActivityKit
import WidgetKit
import SwiftUI

// MARK: - Taboor Live Activity & Dynamic Island Widget
// Compact: Ticket Pass Hologram (Concept 3)
// Expanded: Visual Flow & Stepper (Concept 2) — Pixel-Perfect Sizing & Zero Truncation

public struct TaboorLiveActivityWidget: Widget {
    public init() {}

    public var body: some WidgetConfiguration {
        ActivityConfiguration(for: QueueActivityAttributes.self) { context in
            // Lock Screen / Notification Center Banner (Supports Light & Dark)
            LockScreenStepperBanner(context: context)
                .activityBackgroundTint(Color(red: 0.05, green: 0.08, blue: 0.12))
                .activitySystemActionForegroundColor(.white)
        } dynamicIsland: { context in
            DynamicIsland {
                // MARK: - Expanded Dynamic Island (Visual Flow & Stepper — Pixel-Perfect Fit & Zero Overflow)
                DynamicIslandExpandedRegion(.leading) {
                    HStack(spacing: 4) {
                        Image(systemName: "cross.case.fill")
                            .font(.system(size: 9.5, weight: .bold))
                            .foregroundColor(Color(red: 0.15, green: 0.78, blue: 0.65))
                        
                        VStack(alignment: .leading, spacing: 0) {
                            Text(cleanServiceName(context.attributes.serviceName))
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(.white)
                                .lineLimit(1)
                                .minimumScaleFactor(0.75)
                            
                            Text(cleanBranchName(context.attributes.branchName))
                                .font(.system(size: 9, weight: .medium))
                                .foregroundColor(.gray)
                                .lineLimit(1)
                                .minimumScaleFactor(0.75)
                        }
                    }
                    .padding(.leading, 5)
                }

                DynamicIslandExpandedRegion(.trailing) {
                    VStack(alignment: .trailing, spacing: 1) {
                        HStack(spacing: 3) {
                            Circle()
                                .fill(Color.green)
                                .frame(width: 4, height: 4)
                            Text("مباشر")
                                .font(.system(size: 8.5, weight: .bold))
                                .foregroundColor(.green)
                        }

                        Text(context.attributes.counterName)
                            .font(.system(size: 10, weight: .semibold))
                            .foregroundColor(.white.opacity(0.85))
                            .lineLimit(1)
                            .minimumScaleFactor(0.75)
                    }
                    .padding(.trailing, 5)
                }

                DynamicIslandExpandedRegion(.bottom) {
                    VStack(spacing: 3.5) {
                        // 1. Bento Metrics Row (Compact 25pt height)
                        HStack(spacing: 4) {
                            // Ticket Number
                            VStack(spacing: 0) {
                                Text("تذكرتك")
                                    .font(.system(size: 7.5, weight: .semibold))
                                    .foregroundColor(.gray)
                                Text("#\(context.attributes.ticketNumber)")
                                    .font(.system(size: 12.5, weight: .black, design: .rounded))
                                    .foregroundColor(Color(red: 0.957, green: 0.647, blue: 0.169))
                                    .lineLimit(1)
                                    .minimumScaleFactor(0.8)
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 25)
                            .background(Color.white.opacity(0.07))
                            .clipShape(RoundedRectangle(cornerRadius: 6))

                            // Current Serving
                            VStack(spacing: 0) {
                                Text("يخدم الآن")
                                    .font(.system(size: 7.5, weight: .semibold))
                                    .foregroundColor(.gray)
                                Text(context.state.currentServing)
                                    .font(.system(size: 12.5, weight: .black, design: .rounded))
                                    .foregroundColor(Color(red: 0.15, green: 0.78, blue: 0.65))
                                    .lineLimit(1)
                                    .minimumScaleFactor(0.8)
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 25)
                            .background(Color.white.opacity(0.07))
                            .clipShape(RoundedRectangle(cornerRadius: 6))

                            // Remaining in queue
                            VStack(spacing: 0) {
                                Text("أمامك")
                                    .font(.system(size: 7.5, weight: .semibold))
                                    .foregroundColor(.gray)
                                HStack(spacing: 2) {
                                    Text("\(context.state.waitingAhead)")
                                        .font(.system(size: 12.5, weight: .black, design: .rounded))
                                        .foregroundColor(.white)
                                    Text("(\(context.state.estMinutes)د)")
                                        .font(.system(size: 8.5, weight: .bold))
                                        .foregroundColor(Color(red: 0.15, green: 0.78, blue: 0.65))
                                }
                                .lineLimit(1)
                                .minimumScaleFactor(0.8)
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 25)
                            .background(Color.white.opacity(0.07))
                            .clipShape(RoundedRectangle(cornerRadius: 6))
                        }

                        // 2. Stepper Timeline (Compact 20pt height)
                        QueueStepperView(waitingAhead: context.state.waitingAhead)

                        // 3. Action Buttons (24pt height — 100% visible with zero clipping)
                        HStack(spacing: 6) {
                            // Interactive Background Intent Button (Runs in background, NO app open)
                            Button(intent: DelayQueueLiveActivityIntent()) {
                                HStack(spacing: 3) {
                                    Image(systemName: "hourglass")
                                        .font(.system(size: 8.5))
                                    Text("تأجيل 10د")
                                        .font(.system(size: 9.5, weight: .bold))
                                        .lineLimit(1)
                                }
                                .foregroundColor(.white.opacity(0.92))
                                .frame(maxWidth: .infinity)
                                .frame(height: 24)
                                .background(Color.white.opacity(0.12))
                                .clipShape(Capsule())
                            }
                            .buttonStyle(.plain)

                            Link(destination: URL(string: "taboor://ticket/\(context.attributes.ticketNumber)")!) {
                                HStack(spacing: 3) {
                                    Image(systemName: "location.fill")
                                        .font(.system(size: 8.5))
                                    Text("تتبع مباشر")
                                        .font(.system(size: 9.5, weight: .bold))
                                        .lineLimit(1)
                                }
                                .foregroundColor(Color(red: 0.05, green: 0.08, blue: 0.12))
                                .frame(maxWidth: .infinity)
                                .frame(height: 24)
                                .background(Color(red: 0.15, green: 0.78, blue: 0.65))
                                .clipShape(Capsule())
                            }
                        }
                    }
                    .padding(.horizontal, 6)
                    .padding(.bottom, 2)
                }
            } compactLeading: {
                // MARK: Compact Leading — Ticket Pass Hologram (Concept 3)
                HStack(spacing: 5) {
                    // Taboor Brand Icon Badge
                    ZStack {
                        Circle()
                            .fill(
                                LinearGradient(
                                    colors: [Color(red: 0.15, green: 0.78, blue: 0.65), Color(red: 0.05, green: 0.58, blue: 0.53)],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                            .frame(width: 17, height: 17)
                        Text("T")
                            .font(.system(size: 10, weight: .black, design: .rounded))
                            .foregroundColor(.white)
                    }

                    // Ticket Number
                    Text(context.attributes.ticketNumber)
                        .font(.system(size: 12, weight: .black, design: .rounded))
                        .foregroundColor(Color(red: 0.957, green: 0.647, blue: 0.169))
                        .lineLimit(1)
                        .minimumScaleFactor(0.85)
                }
                .padding(.leading, 3)
            } compactTrailing: {
                // MARK: Compact Trailing — Ticket Pass Hologram (Concept 3)
                HStack(spacing: 3) {
                    Text("\(context.state.waitingAhead)")
                        .font(.system(size: 12, weight: .black, design: .rounded))
                        .foregroundColor(.white)
                    Text("•")
                        .font(.system(size: 8, weight: .bold))
                        .foregroundColor(.gray)
                    Text("\(context.state.estMinutes)د")
                        .font(.system(size: 11, weight: .bold, design: .rounded))
                        .foregroundColor(Color(red: 0.15, green: 0.78, blue: 0.65))
                }
                .lineLimit(1)
                .minimumScaleFactor(0.85)
                .padding(.horizontal, 6)
                .padding(.vertical, 2)
                .background(Color(red: 0.15, green: 0.78, blue: 0.65).opacity(0.18))
                .clipShape(Capsule())
                .padding(.trailing, 2)
            } minimal: {
                // MARK: Minimal Presentation (Concept 3)
                ZStack {
                    Circle()
                        .fill(Color(red: 0.15, green: 0.78, blue: 0.65))
                        .frame(width: 16, height: 16)
                    Text("T")
                        .font(.system(size: 9, weight: .black, design: .rounded))
                        .foregroundColor(.white)
                }
            }
        }
    }

    private func cleanServiceName(_ raw: String) -> String {
        let clean = raw.components(separatedBy: " - ").first ?? raw
        return clean.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    private func cleanBranchName(_ raw: String) -> String {
        let clean = raw.components(separatedBy: " • ").first ?? raw
        return clean.trimmingCharacters(in: .whitespacesAndNewlines)
    }
}

// MARK: - 4-Step Stepper Timeline (Zero Truncation & Compact Height)

private struct QueueStepperView: View {
    let waitingAhead: Int

    private var activeStepIndex: Int {
        if waitingAhead == 0 {
            return 3 // الخدمة
        } else if waitingAhead == 1 {
            return 2 // للشباك
        } else if waitingAhead <= 3 {
            return 1 // استعد
        } else {
            return 0 // محجوز
        }
    }

    private let steps: [(title: String, icon: String)] = [
        ("محجوز", "checkmark"),
        ("استعد", "hourglass"),
        ("للشباك", "arrow.up"),
        ("الخدمة", "sparkles")
    ]

    var body: some View {
        HStack(spacing: 0) {
            ForEach(0..<steps.count, id: \.self) { index in
                VStack(spacing: 1) {
                    // Node circle (10x10 pt)
                    ZStack {
                        Circle()
                            .fill(stepBgColor(for: index))
                            .frame(width: 10, height: 10)

                        if index < activeStepIndex {
                            Image(systemName: "checkmark")
                                .font(.system(size: 5.5, weight: .bold))
                                .foregroundColor(.white)
                        } else if index == activeStepIndex {
                            Circle()
                                .fill(Color.white)
                                .frame(width: 3.5, height: 3.5)
                        } else {
                            Circle()
                                .fill(Color.white.opacity(0.3))
                                .frame(width: 2.5, height: 2.5)
                        }
                    }

                    // Label (7.5pt, zero ellipsis)
                    Text(steps[index].title)
                        .font(.system(size: 7.5, weight: index == activeStepIndex ? .bold : .medium))
                        .foregroundColor(index == activeStepIndex ? Color(red: 0.957, green: 0.647, blue: 0.169) : (index < activeStepIndex ? .white : .gray))
                        .lineLimit(1)
                        .minimumScaleFactor(0.75)
                        .allowsTightening(true)
                }
                .frame(maxWidth: .infinity)

                // Connector line between nodes
                if index < steps.count - 1 {
                    Rectangle()
                        .fill(index < activeStepIndex ? Color(red: 0.15, green: 0.78, blue: 0.65) : Color.white.opacity(0.15))
                        .frame(height: 1.5)
                        .padding(.bottom, 8)
                }
            }
        }
        .padding(.horizontal, 2)
    }

    private func stepBgColor(for index: Int) -> Color {
        if index < activeStepIndex {
            return Color(red: 0.15, green: 0.78, blue: 0.65)
        } else if index == activeStepIndex {
            return Color(red: 0.957, green: 0.647, blue: 0.169)
        } else {
            return Color.white.opacity(0.15)
        }
    }
}// MARK: - Lock Screen Banner Presentation

private struct LockScreenStepperBanner: View {
    let context: ActivityViewContext<QueueActivityAttributes>

    var body: some View {
        VStack(spacing: 12) {
            // Header Row
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 3) {
                    HStack(spacing: 6) {
                        Image(systemName: "cross.case.fill")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(Color(red: 0.15, green: 0.78, blue: 0.65))
                        Text(context.attributes.serviceName)
                            .font(.system(size: 15, weight: .bold))
                            .foregroundColor(.white)
                            .lineLimit(1)
                            .minimumScaleFactor(0.8)
                    }

                    Text("\(context.attributes.branchName) • \(context.attributes.counterName)")
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(.gray)
                        .lineLimit(1)
                        .minimumScaleFactor(0.8)
                }

                Spacer()

                VStack(alignment: .trailing, spacing: 2) {
                    Text(context.attributes.ticketNumber)
                        .font(.system(size: 24, weight: .black, design: .rounded))
                        .foregroundColor(Color(red: 0.957, green: 0.647, blue: 0.169))
                    Text("تذكرتك")
                        .font(.system(size: 10, weight: .semibold))
                        .foregroundColor(.gray)
                }
            }

            // Metrics Comparison Row
            HStack(spacing: 8) {
                HStack(spacing: 6) {
                    Circle()
                        .fill(Color.green)
                        .frame(width: 8, height: 8)
                    Text("يخدم الآن: \(context.state.currentServing)")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(.white)
                }

                Spacer()

                HStack(spacing: 4) {
                    Image(systemName: "clock.fill")
                        .font(.system(size: 11))
                        .foregroundColor(Color(red: 0.15, green: 0.78, blue: 0.65))
                    Text("~ \(context.state.estMinutes) د")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(Color(red: 0.15, green: 0.78, blue: 0.65))
                }

                HStack(spacing: 4) {
                    Image(systemName: "person.2.fill")
                        .font(.system(size: 11))
                        .foregroundColor(Color(red: 0.957, green: 0.647, blue: 0.169))
                    Text("\(context.state.waitingAhead) انتظار")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(.white)
                }
            }

            // Stepper
            QueueStepperView(waitingAhead: context.state.waitingAhead)
        }
        .padding(16)
        .background(Color(red: 0.08, green: 0.11, blue: 0.15))
    }
}


