import Foundation
import EventKit
import UIKit

// MARK: - Native Apple Calendar Integration Manager (EventKit)

public enum CalendarAddResult {
    case success(eventDate: Date)
    case permissionDenied
    case failed(String)
}

@MainActor
public final class AppleCalendarManager {
    public static let shared = AppleCalendarManager()

    private let eventStore = EKEventStore()

    private init() {}

    // MARK: - Add Appointment to Apple Calendar
    public func addAppointmentEvent(ticket: UserTicket, isArabic: Bool) async -> CalendarAddResult {
        let hasAccess = await requestCalendarAccess()
        guard hasAccess else {
            return .permissionDenied
        }

        guard let targetCalendar = eventStore.defaultCalendarForNewEvents ?? eventStore.calendars(for: .event).first(where: { $0.allowsContentModifications }) else {
            return .failed(isArabic ? "لم يتم العثور على تقويم متاح للكتابة" : "No writable calendar found")
        }

        let startDate = parseAppointmentDateTime(ticket: ticket)
        let endDate = startDate.addingTimeInterval(45 * 60) // 45 minutes duration

        let event = EKEvent(eventStore: eventStore)
        event.calendar = targetCalendar

        // Event Title & Location
        let service = ticket.localizedServiceName(isArabic: isArabic)
        event.title = isArabic ? "موعد: \(service)" : "Appointment: \(service)"
        event.location = ticket.localizedBranch(isArabic: isArabic)

        // Event Notes
        let dateLabel = ticket.scheduledDate ?? (isArabic ? "غداً" : "Tomorrow")
        let timeLabel = ticket.scheduledTime ?? (isArabic ? "04:30 م" : "04:30 PM")
        let branchLabel = ticket.localizedBranch(isArabic: isArabic)

        event.notes = isArabic
            ? """
            رقم الحجز: #\(ticket.ticketNumber)
            الخدمة: \(ticket.serviceNameAr)
            الفرع: \(branchLabel)
            الموعد: \(dateLabel) الساعة \(timeLabel)

            تم الحجز وتأكيد الموعد عبر تطبيق طابور (Taboor).
            سنرسل لك تذكيراً تلقائياً للتحرك في الوقت المناسب.
            """
            : """
            Booking Number: #\(ticket.ticketNumber)
            Service: \(ticket.serviceName)
            Branch: \(branchLabel)
            Scheduled: \(dateLabel) at \(timeLabel)

            Booked and confirmed via Taboor.
            """

        event.startDate = startDate
        event.endDate = endDate

        // Smart Alarms: 45 minutes before departure & 2 hours reminder
        event.alarms = [
            EKAlarm(relativeOffset: -45 * 60),
            EKAlarm(relativeOffset: -120 * 60)
        ]

        if let appUrl = URL(string: "taboor://queue") {
            event.url = appUrl
        }

        do {
            try eventStore.save(event, span: .thisEvent, commit: true)
            return .success(eventDate: startDate)
        } catch {
            return .failed(error.localizedDescription)
        }
    }

    // MARK: - Open Native Apple Calendar App at Exact Date
    public func openAppleCalendar(at date: Date? = nil) {
        if let date = date {
            let interval = date.timeIntervalSinceReferenceDate
            if let url = URL(string: "calshow:\(interval)") {
                UIApplication.shared.open(url)
                return
            }
        }

        if let fallbackUrl = URL(string: "calshow:") {
            UIApplication.shared.open(fallbackUrl)
        }
    }

    // MARK: - Open Device Settings for Permissions
    public func openAppSettings() {
        if let settingsUrl = URL(string: UIApplication.openSettingsURLString) {
            UIApplication.shared.open(settingsUrl)
        }
    }

    // MARK: - Private Helpers

    private func requestCalendarAccess() async -> Bool {
        if #available(iOS 17.0, *) {
            do {
                return try await eventStore.requestWriteOnlyAccessToEvents()
            } catch {
                return false
            }
        } else {
            return await withCheckedContinuation { continuation in
                eventStore.requestAccess(to: .event) { granted, _ in
                    continuation.resume(returning: granted)
                }
            }
        }
    }

    private func parseAppointmentDateTime(ticket: UserTicket) -> Date {
        let calendar = Calendar.current
        var targetDate = calendar.startOfDay(for: Date())

        let dateStr = (ticket.scheduledDate ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        if dateStr.contains("غداً") || dateStr.lowercased().contains("tomorrow") {
            targetDate = calendar.date(byAdding: .day, value: 1, to: targetDate) ?? targetDate
        } else if dateStr.contains("بعد غد") || dateStr.lowercased().contains("2 days") {
            targetDate = calendar.date(byAdding: .day, value: 2, to: targetDate) ?? targetDate
        }

        // Parse Time String (e.g. "04:30 م", "04:30 PM", "11:00 ص", "12:00 م")
        let rawTime = ticket.scheduledTime ?? "04:30 م"
        let normalized = rawTime
            .replacingOccurrences(of: "٠", with: "0")
            .replacingOccurrences(of: "١", with: "1")
            .replacingOccurrences(of: "٢", with: "2")
            .replacingOccurrences(of: "٣", with: "3")
            .replacingOccurrences(of: "٤", with: "4")
            .replacingOccurrences(of: "٥", with: "5")
            .replacingOccurrences(of: "٦", with: "6")
            .replacingOccurrences(of: "٧", with: "7")
            .replacingOccurrences(of: "٨", with: "8")
            .replacingOccurrences(of: "٩", with: "9")

        let isPM = normalized.contains("م") || normalized.uppercased().contains("PM")
        let isAM = normalized.contains("ص") || normalized.uppercased().contains("AM")

        let components = normalized
            .replacingOccurrences(of: "م", with: "")
            .replacingOccurrences(of: "ص", with: "")
            .replacingOccurrences(of: "PM", with: "")
            .replacingOccurrences(of: "AM", with: "")
            .trimmingCharacters(in: .whitespaces)
            .split(separator: ":")

        var hour = 16
        var minute = 30

        if components.count >= 2,
           let h = Int(components[0].trimmingCharacters(in: .whitespaces)),
           let m = Int(components[1].trimmingCharacters(in: .whitespaces)) {
            hour = h
            minute = m

            if isPM && hour < 12 {
                hour += 12
            } else if isAM && hour == 12 {
                hour = 0
            }
        }

        var dateComponents = calendar.dateComponents([.year, .month, .day], from: targetDate)
        dateComponents.hour = hour
        dateComponents.minute = minute
        dateComponents.second = 0

        return calendar.date(from: dateComponents) ?? targetDate.addingTimeInterval(Double(hour * 3600 + minute * 60))
    }
}
