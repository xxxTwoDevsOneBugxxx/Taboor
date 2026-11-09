import SwiftUI

// MARK: - Native Dynamic Island Interactive HUD & Simulation Banner
// Compact: Ticket Pass Hologram (Concept 3)
// Expanded: Visual Flow & Stepper (Concept 2) — Zero Truncation & Optimized Spacing

public struct DynamicIslandHUDView: View {
    @ObservedObject var dataService = QueueDataService.shared
    @ObservedObject private var localization = LocalizationManager.shared
    @State private var isExpanded = false

    public init() {}

    public var body: some View {
        if let ticket = dataService.activeTicket {
            VStack {
                HStack {
                    Spacer()
                    dynamicIslandContent(ticket: ticket)
                    Spacer()
                }
                .padding(.top, 10)

                Spacer()
            }
            .ignoresSafeArea()
            .zIndex(9999)
            .transition(.move(edge: .top).combined(with: .opacity))
        }
    }

    @ViewBuilder
    private func dynamicIslandContent(ticket: UserTicket) -> some View {
        ZStack {
            if isExpanded {
                expandedIslandView(ticket: ticket)
            } else {
                compactIslandView(ticket: ticket)
            }
        }
        .background(Color.black)
        .clipShape(RoundedRectangle(cornerRadius: isExpanded ? 32 : 20, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: isExpanded ? 32 : 20, style: .continuous)
                .strokeBorder(Color.white.opacity(0.14), lineWidth: 0.8)
        )
        .shadow(color: Color.black.opacity(0.45), radius: isExpanded ? 20 : 8, y: 6)
        .onTapGesture {
            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
            withAnimation(.spring(response: 0.38, dampingFraction: 0.72)) {
                isExpanded.toggle()
            }
        }
    }

    // MARK: - Compact Pill (Ticket Pass Hologram)
    private func compactIslandView(ticket: UserTicket) -> some View {
        HStack(spacing: 8) {
            // Leading: Taboor Brand Icon Badge + Ticket Number
            HStack(spacing: 5) {
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

                Text(ticket.ticketNumber)
                    .font(.system(size: 12.5, weight: .black, design: .rounded))
                    .foregroundColor(Color(red: 0.957, green: 0.647, blue: 0.169))
                    .lineLimit(1)
                    .minimumScaleFactor(0.85)
            }

            Spacer(minLength: 8)

            // Trailing: Waiting count & Minutes Pill
            HStack(spacing: 4) {
                Text("\(ticket.waitingAhead)")
                    .font(.system(size: 12, weight: .black, design: .rounded))
                    .foregroundColor(.white)
                Text("•")
                    .font(.system(size: 8, weight: .bold))
                    .foregroundColor(.gray)
                Text("\(ticket.estMinutes)\(localization.isArabic ? "د" : "m")")
                    .font(.system(size: 11, weight: .bold, design: .rounded))
                    .foregroundColor(Color(red: 0.15, green: 0.78, blue: 0.65))
            }
            .lineLimit(1)
            .minimumScaleFactor(0.85)
            .padding(.horizontal, 7)
            .padding(.vertical, 2.5)
            .background(Color(red: 0.15, green: 0.78, blue: 0.65).opacity(0.18))
            .clipShape(Capsule())
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .frame(width: 220, height: 38)
    }

    // MARK: - Expanded Island (Visual Flow & Stepper)
    private func expandedIslandView(ticket: UserTicket) -> some View {
        VStack(spacing: 10) {
            // Header Row: Service, Counter, Live Indicator
            HStack(alignment: .top) {
                HStack(spacing: 5) {
                    Image(systemName: "cross.case.fill")
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(Color(red: 0.15, green: 0.78, blue: 0.65))

                    VStack(alignment: .leading, spacing: 0) {
                        Text(cleanText(ticket.localizedServiceName(isArabic: localization.isArabic)))
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(.white)
                            .lineLimit(1)
                            .minimumScaleFactor(0.8)

                        Text(cleanText(ticket.localizedBranch(isArabic: localization.isArabic)))
                            .font(.system(size: 9.5, weight: .medium))
                            .foregroundColor(.gray)
                            .lineLimit(1)
                            .minimumScaleFactor(0.8)
                    }
                }

                Spacer(minLength: 8)

                VStack(alignment: .trailing, spacing: 1) {
                    HStack(spacing: 3) {
                        Circle()
                            .fill(Color.green)
                            .frame(width: 4, height: 4)
                        Text(localization.isArabic ? "مباشر" : "LIVE")
                            .font(.system(size: 9, weight: .bold))
                            .foregroundColor(.green)
                    }

                    Text(ticket.localizedCounter(isArabic: localization.isArabic))
                        .font(.system(size: 10.5, weight: .semibold))
                        .foregroundColor(.white.opacity(0.85))
                        .lineLimit(1)
                        .minimumScaleFactor(0.8)
                }
            }

            // Bento Metrics Row (Compact: 25pt height)
            HStack(spacing: 4) {
                // Ticket Number
                VStack(spacing: 0) {
                    Text(localization.isArabic ? "تذكرتك" : "Ticket")
                        .font(.system(size: 7.5, weight: .semibold))
                        .foregroundColor(.gray)
                    Text("#\(ticket.ticketNumber)")
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
                    Text(localization.isArabic ? "يخدم الآن" : "Serving")
                        .font(.system(size: 7.5, weight: .semibold))
                        .foregroundColor(.gray)
                    Text(ticket.currentServing)
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
                    Text(localization.isArabic ? "أمامك" : "Ahead")
                        .font(.system(size: 7.5, weight: .semibold))
                        .foregroundColor(.gray)
                    HStack(spacing: 2) {
                        Text("\(ticket.waitingAhead)")
                            .font(.system(size: 12.5, weight: .black, design: .rounded))
                            .foregroundColor(.white)
                        Text("(\(ticket.estMinutes)\(localization.isArabic ? "د" : "m"))")
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

            // 4-Step Stepper Timeline (Compact: 20pt height)
            stepperView(waitingAhead: ticket.waitingAhead)

            // Action Buttons (24pt height — 100% visible)
            HStack(spacing: 6) {
                Button(action: {
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    dataService.delayActiveTicket(minutes: 10)
                }) {
                    HStack(spacing: 3) {
                        Image(systemName: "hourglass")
                            .font(.system(size: 8.5))
                        Text(localization.isArabic ? "تأجيل 10د" : "Delay 10m")
                            .font(.system(size: 9.5, weight: .bold))
                            .lineLimit(1)
                            .minimumScaleFactor(0.8)
                    }
                    .foregroundColor(.white.opacity(0.92))
                    .frame(maxWidth: .infinity)
                    .frame(height: 24)
                    .background(Color.white.opacity(0.12))
                    .clipShape(Capsule())
                }

                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    withAnimation(.spring(response: 0.35, dampingFraction: 0.75)) {
                        isExpanded = false
                    }
                }) {
                    HStack(spacing: 3) {
                        Image(systemName: "chevron.up")
                            .font(.system(size: 8.5, weight: .bold))
                        Text(localization.isArabic ? "تصغير" : "Collapse")
                            .font(.system(size: 9.5, weight: .bold))
                            .lineLimit(1)
                            .minimumScaleFactor(0.8)
                    }
                    .foregroundColor(Color(red: 0.05, green: 0.08, blue: 0.12))
                    .frame(maxWidth: .infinity)
                    .frame(height: 24)
                    .background(Color(red: 0.15, green: 0.78, blue: 0.65))
                    .clipShape(Capsule())
                }
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .frame(width: 355)
    }

    private func cleanText(_ raw: String) -> String {
        if raw.contains(" - ") {
            return raw.components(separatedBy: " - ").first ?? raw
        }
        if raw.contains(" • ") {
            return raw.components(separatedBy: " • ").first ?? raw
        }
        return raw
    }

    // MARK: - Stepper View Helper
    private func stepperView(waitingAhead: Int) -> some View {
        let activeStepIndex: Int = {
            if waitingAhead == 0 { return 3 }
            if waitingAhead == 1 { return 2 }
            if waitingAhead <= 3 { return 1 }
            return 0
        }()

        let steps: [(title: String, icon: String)] = [
            (localization.isArabic ? "محجوز" : "Booked", "checkmark.circle.fill"),
            (localization.isArabic ? "استعد" : "Ready", "hourglass"),
            (localization.isArabic ? "للشباك" : "Counter", "arrow.up.circle.fill"),
            (localization.isArabic ? "الخدمة" : "Serving", "sparkles")
        ]

        return HStack(spacing: 0) {
            ForEach(0..<steps.count, id: \.self) { index in
                VStack(spacing: 1) {
                    ZStack {
                        Circle()
                            .fill(stepBgColor(for: index, activeIndex: activeStepIndex))
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

                    Text(steps[index].title)
                        .font(.system(size: 7.5, weight: index == activeStepIndex ? .bold : .medium))
                        .foregroundColor(index == activeStepIndex ? Color(red: 0.957, green: 0.647, blue: 0.169) : (index < activeStepIndex ? .white : .gray))
                        .lineLimit(1)
                        .minimumScaleFactor(0.75)
                        .allowsTightening(true)
                }
                .frame(maxWidth: .infinity)

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

    private func stepBgColor(for index: Int, activeIndex: Int) -> Color {
        if index < activeIndex {
            return Color(red: 0.15, green: 0.78, blue: 0.65)
        } else if index == activeIndex {
            return Color(red: 0.957, green: 0.647, blue: 0.169)
        } else {
            return Color.white.opacity(0.15)
        }
    }
}
