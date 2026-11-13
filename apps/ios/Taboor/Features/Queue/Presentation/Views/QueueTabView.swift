import SwiftUI

// MARK: - Redesigned Native iOS Queue Tab View (Apple Wallet Style & Taboor Palette)

public struct QueueTabView: View {
    @Binding var selectedTab: TaboorTab
    @StateObject private var dataService = QueueDataService.shared
    @ObservedObject private var localization = LocalizationManager.shared
    @Environment(\.colorScheme) private var colorScheme

    @State private var showCancelAlert = false
    @State private var showAddedToCalendarAlert = false
    @State private var calendarAlertMessage: String = ""
    @State private var calendarEventDate: Date? = nil
    @State private var showCalendarPermissionAlert = false
    @State private var isAddingToCalendar = false
    @State private var postponeTrigger = false
    @State private var showAllServices = false
    @State private var selectedPlaceForDetails: ServicePlace? = nil
    @State private var showLiveTracking = false
    @State private var ticketCutoutY: CGFloat = 86

    public init(selectedTab: Binding<TaboorTab> = .constant(.queue)) {
        self._selectedTab = selectedTab
    }

    public var body: some View {
        NavigationStack {
            ZStack(alignment: .bottom) {
                ScrollView(showsIndicators: false) {
                    VStack(spacing: 20) {
                        // 1. Top Custom Header Bar
                        topHeaderBar

                        if let ticket = dataService.activeTicket {
                            // Multi-ticket carousel switcher
                            if dataService.activeTickets.count > 1 {
                                multiTicketSwitcher
                            }

                            // 2. Apple Wallet Style Virtual Ticket Card
                            activeTicketCard(ticket: ticket)

                            // 3. Quick Action Buttons
                            quickActions(ticket: ticket)
                        } else {
                            // 4. Empty State Card
                            emptyQueueCard
                        }

                        // 5. Past Visits History Section
                        if !dataService.history.isEmpty {
                            historySection
                        }

                        Spacer().frame(height: 105)
                    }
                    .padding(.horizontal, 20)
                    .padding(.top, 12)
                }
                .background(AppColors.screenBackground.ignoresSafeArea())
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(AppColors.screenBackground.ignoresSafeArea())
            .toolbar(.hidden, for: .navigationBar)
            .refreshable {
                await dataService.refreshAll()
            }
            .alert(localization.isArabic ? "تقويم آبل" : "Apple Calendar", isPresented: $showAddedToCalendarAlert) {
                if calendarEventDate != nil {
                    Button(localization.isArabic ? "فتح تطبيق التقويم" : "Open Calendar App") {
                        AppleCalendarManager.shared.openAppleCalendar(at: calendarEventDate)
                    }
                }
                Button(localization.isArabic ? "حسناً" : "OK", role: .cancel) {}
            } message: {
                Text(calendarAlertMessage)
            }
            .alert(localization.isArabic ? "إذن التقويم مطلوب" : "Calendar Permission Required", isPresented: $showCalendarPermissionAlert) {
                Button(localization.isArabic ? "فتح الإعدادات" : "Open Settings") {
                    AppleCalendarManager.shared.openAppSettings()
                }
                Button(localization.isArabic ? "إلغاء" : "Cancel", role: .cancel) {}
            } message: {
                Text(localization.isArabic
                     ? "يرجى السماح بالوصول للتقويم من إعدادات جهازك لتتمكن من إضافة المواعيد وتلقي التنبيهات الذكية."
                     : "Please allow Calendar access in device settings to add appointments and receive reminders.")
            }
            .alert(
                (dataService.activeTicket?.bookingType == .scheduled && dataService.activeTicket?.scheduledDate != "اليوم")
                    ? (localization.isArabic ? "إلغاء حجز الموعد" : "Cancel Appointment")
                    : (localization.isArabic ? "إلغاء التذكرة" : "Cancel Ticket"),
                isPresented: $showCancelAlert
            ) {
                if let ticket = dataService.activeTicket, ticket.bookingType == .scheduled, ticket.scheduledDate != "اليوم" {
                    Button(localization.isArabic ? "الاحتفاظ بالموعد" : "Keep Appointment", role: .cancel) {}
                    Button(localization.isArabic ? "نعم، إلغاء الحجز" : "Yes, Cancel Booking", role: .destructive) {
                        withAnimation(.spring(response: 0.4, dampingFraction: 0.8)) {
                            dataService.cancelActiveTicket()
                            UINotificationFeedbackGenerator().notificationOccurred(.warning)
                        }
                    }
                } else {
                    Button(localization.isArabic ? "الاحتفاظ بدوري" : "Keep My Spot", role: .cancel) {}
                    Button(localization.isArabic ? "نعم، إلغاء التذكرة" : "Yes, Cancel Ticket", role: .destructive) {
                        withAnimation(.spring(response: 0.4, dampingFraction: 0.8)) {
                            dataService.cancelActiveTicket()
                            UINotificationFeedbackGenerator().notificationOccurred(.warning)
                        }
                    }
                }
            } message: {
                if let ticket = dataService.activeTicket, ticket.bookingType == .scheduled, ticket.scheduledDate != "اليوم" {
                    Text(localization.isArabic
                         ? "هل أنت متأكد من رغبتك في إلغاء حجز موعدك (\(ticket.scheduledDate ?? "غداً") الساعة \(ticket.scheduledTime ?? ""))؟ سيتم إلغاء الموعد وإتاحته لمستخدم آخر."
                         : "Are you sure you want to cancel your scheduled appointment (\(ticket.scheduledDate ?? "Tomorrow") at \(ticket.scheduledTime ?? ""))? The slot will be released.")
                } else {
                    Text(localization.isArabic
                         ? "هل أنت متأكد من رغبتك في إلغاء دورك الحالي؟ ستفقد مكانك في الطابور."
                         : "Are you sure you want to cancel your ticket? You will lose your spot in line.")
                }
            }
            .sheet(isPresented: $showAllServices) {
                NavigationStack {
                    AllServicesView { place in
                        showAllServices = false
                        selectedPlaceForDetails = place
                    }
                }
            }
            .sheet(item: $selectedPlaceForDetails) { place in
                NavigationStack {
                    ServiceDetailsView(place: place) {
                        selectedPlaceForDetails = nil
                        showAllServices = false
                        dataService.isTabBarHidden = false
                    }
                }
            }
            .fullScreenCover(isPresented: $showLiveTracking) {
                LiveQueueTrackingView()
            }
            .onChange(of: dataService.shouldNavigateToQueueTab) { _, shouldNavigate in
                if shouldNavigate {
                    selectedPlaceForDetails = nil
                    showAllServices = false
                    dataService.isTabBarHidden = false
                }
            }
        }
        .background(AppColors.screenBackground.ignoresSafeArea())
    }

    // MARK: - 1. Top Header Bar
    private var topHeaderBar: some View {
        HStack(alignment: .center, spacing: 12) {
            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 8) {
                    Text(localization.isArabic ? "طوابيري" : "My Queues")
                        .font(.app(24, .extraBold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.ink)

                    if let ticket = dataService.activeTicket {
                        HStack(spacing: 4) {
                            Circle()
                                .fill(ticket.bookingType == .scheduled ? AppColors.amber : AppColors.accentGreen)
                                .frame(width: 6, height: 6)

                            Text(ticket.bookingType == .scheduled
                                 ? (localization.isArabic ? (ticket.scheduledDate == "غداً" ? "غداً" : "موعد") : "Scheduled")
                                 : (localization.isArabic ? "مباشر" : "Live"))
                                .font(.app(11, .bold, isArabic: localization.isArabic))
                                .foregroundColor(ticket.bookingType == .scheduled ? AppColors.amber : AppColors.accentGreen)
                        }
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background((ticket.bookingType == .scheduled ? AppColors.amber : AppColors.accentGreen).opacity(0.12))
                        .clipShape(Capsule())
                    }
                }

                Text(dataService.activeTicket != nil
                     ? (localization.isArabic ? "متابعة تذاكرك ومواعيدك المؤكدة" : "Track your active queues & bookings")
                     : (localization.isArabic ? "لا توجد تذكرة نشطة حالياً" : "No active tickets currently"))
                    .font(.app(12.5, .medium, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
            }

            Spacer()

            if dataService.activeTicket != nil {
                Button(action: {
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    showLiveTracking = true
                }) {
                    ZStack {
                        Circle()
                            .fill(Color.white)
                            .frame(width: 40, height: 40)
                            .overlay(
                                Circle().strokeBorder(AppColors.cardBorder, lineWidth: 1)
                            )
                            .shadow(color: Color.black.opacity(0.04), radius: 4, y: 2)

                        Image(systemName: "map.fill")
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(AppColors.teal)
                    }
                }
                .buttonStyle(ScaleButtonStyle(scale: 0.92, pressedOpacity: 0.85))
            }
        }
        .padding(.top, 2)
    }

    // MARK: - Multi-Ticket Switcher Filter (Full Width Apple Segmented Style)
    private var multiTicketSwitcher: some View {
        HStack(spacing: 8) {
            ForEach(Array(dataService.activeTickets.enumerated()), id: \.element.id) { index, ticket in
                let isSelected = dataService.selectedTicketIndex == index
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                        dataService.selectedTicketIndex = index
                    }
                }) {
                    HStack(spacing: 6) {
                        Circle()
                            .fill(ticket.bookingType == .scheduled ? AppColors.amber : AppColors.accentGreen)
                            .frame(width: 7, height: 7)

                        Text(ticketShortLabel(ticket))
                            .font(.app(12.5, isSelected ? .bold : .medium, isArabic: localization.isArabic))
                            .lineLimit(1)
                    }
                    .foregroundColor(isSelected ? .white : AppColors.textPrimary)
                    .frame(maxWidth: .infinity)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 9)
                    .background(
                        isSelected
                            ? AppColors.ink
                            : Color.white
                    )
                    .clipShape(Capsule())
                    .overlay(
                        Capsule().strokeBorder(isSelected ? Color.clear : AppColors.cardBorder, lineWidth: 1)
                    )
                    .shadow(color: isSelected ? AppColors.ink.opacity(0.12) : Color.black.opacity(0.02), radius: 3, y: 1)
                }
                .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.85))
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 2)
    }

    // MARK: - Helper for Short Multi-Ticket Switcher Labels (Clean, Uncrowded)
    private func ticketShortLabel(_ ticket: UserTicket) -> String {
        let name = ticket.localizedServiceName(isArabic: localization.isArabic)
        var cleanName = name.components(separatedBy: " - ").first?.components(separatedBy: " • ").first ?? name
        if localization.isArabic {
            cleanName = cleanName
                .replacingOccurrences(of: "مستشفى ", with: "")
                .replacingOccurrences(of: "عيادات ", with: "")
                .replacingOccurrences(of: "عيادة ", with: "")
                .replacingOccurrences(of: "للحلاقة والتجميل", with: "")
                .replacingOccurrences(of: "للحلاقة", with: "")
                .trimmingCharacters(in: .whitespaces)
        } else {
            cleanName = cleanName
                .replacingOccurrences(of: "Hospital", with: "")
                .replacingOccurrences(of: "& Grooming", with: "")
                .trimmingCharacters(in: .whitespaces)
        }
        let words = cleanName.components(separatedBy: " ").filter { !$0.isEmpty }
        let shortName = words.prefix(2).joined(separator: " ")
        return "\(shortName) (#\(ticket.ticketNumber))"
    }

    // MARK: - 2. Active Ticket Card (Apple Wallet Boarding Pass Elegance)
    private func activeTicketCard(ticket: UserTicket) -> some View {
        VStack(spacing: 0) {
            // Upper Header: Place Name, Branch, and Badge
            VStack(alignment: .leading, spacing: 5) {
                HStack(alignment: .top, spacing: 10) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text(ticket.localizedServiceName(isArabic: localization.isArabic))
                            .font(.app(18.5, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.ink)
                            .lineLimit(2)
                            .fixedSize(horizontal: false, vertical: true)

                        HStack(spacing: 5) {
                            Image(systemName: "mappin.circle.fill")
                                .font(.system(size: 12))
                                .foregroundColor(AppColors.teal)

                            Text(ticket.localizedBranch(isArabic: localization.isArabic))
                                .font(.app(12.5, .regular, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textSecondary)
                                .lineLimit(1)
                        }
                    }

                    Spacer(minLength: 6)

                    // Clean Status Capsule
                    if ticket.bookingType == .scheduled {
                        HStack(spacing: 4) {
                            Image(systemName: "calendar")
                                .font(.system(size: 10, weight: .bold))
                            Text(ticket.scheduledDate == "غداً"
                                 ? (localization.isArabic ? "غداً" : "Tomorrow")
                                 : (ticket.scheduledDate ?? (localization.isArabic ? "موعد" : "Scheduled")))
                                .font(.app(11, .bold, isArabic: localization.isArabic))
                        }
                        .foregroundColor(AppColors.amber)
                        .padding(.horizontal, 9)
                        .padding(.vertical, 4)
                        .background(AppColors.amber.opacity(0.12))
                        .clipShape(Capsule())
                    } else {
                        HStack(spacing: 4) {
                            Circle()
                                .fill(AppColors.accentGreen)
                                .frame(width: 6, height: 6)
                            Text(localization.isArabic ? "طابور مباشر" : "Live Queue")
                                .font(.app(11, .bold, isArabic: localization.isArabic))
                        }
                        .foregroundColor(AppColors.accentGreen)
                        .padding(.horizontal, 9)
                        .padding(.vertical, 4)
                        .background(AppColors.accentGreen.opacity(0.12))
                        .clipShape(Capsule())
                    }
                }
            }
            .padding(.horizontal, 18)
            .padding(.top, 18)
            .padding(.bottom, 14)
            .background(
                GeometryReader { geo in
                    Color.clear.preference(
                        key: TicketCutoutYPreferenceKey.self,
                        value: geo.size.height
                    )
                }
            )

            // Iconic Perforated Line (Traced cleanly between genuine inward cutouts)
            DashedLine()
                .stroke(style: StrokeStyle(lineWidth: 1, dash: [5, 4]))
                .foregroundColor(AppColors.cardBorder.opacity(0.85))
                .frame(height: 1)
                .padding(.horizontal, 14)

            // Main Core Information (Spacious & Clean — NO Nested Box Borders!)
            VStack(spacing: 14) {
                if ticket.bookingType == .scheduled {
                    // Scheduled Appointment Layout
                    HStack(alignment: .center, spacing: 20) {
                        // Left Column: Booking Number
                        VStack(alignment: .leading, spacing: 2) {
                            Text(localization.isArabic ? "رقم الحجز" : "BOOKING NO.")
                                .font(.app(11, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textTertiary)
                                .textCase(.uppercase)

                            Text(ticket.ticketNumber)
                                .font(.system(size: 38, weight: .heavy, design: .rounded))
                                .foregroundColor(AppColors.ink)
                        }

                        Spacer()

                        // Hairline Vertical Divider
                        Rectangle()
                            .fill(AppColors.cardBorder.opacity(0.7))
                            .frame(width: 1, height: 42)

                        Spacer()

                        // Right Column: Confirmed Slot
                        VStack(alignment: .trailing, spacing: 2) {
                            Text(localization.isArabic ? "الموعد المؤكد" : "CONFIRMED SLOT")
                                .font(.app(11, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textTertiary)
                                .textCase(.uppercase)

                            HStack(spacing: 5) {
                                Text(ticket.scheduledTime ?? "04:30 م")
                                    .font(.system(size: 22, weight: .heavy, design: .rounded))
                                    .foregroundColor(AppColors.teal)

                                Image(systemName: "clock.fill")
                                    .font(.system(size: 13, weight: .bold))
                                    .foregroundColor(AppColors.teal)
                            }

                            Text(ticket.scheduledDate ?? (localization.isArabic ? "غداً" : "Tomorrow"))
                                .font(.app(12, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.amber)
                        }
                    }
                    .padding(.top, 4)

                    // Quiet Reassurance Message (No loud yellow box!)
                    HStack(spacing: 7) {
                        Image(systemName: "bell.badge.fill")
                            .font(.system(size: 12))
                            .foregroundColor(AppColors.amber)

                        Text(localization.isArabic
                             ? "سنرسل لك تذكيراً ذكياً قبل موعدك بـ 45 دقيقة للتحرك في الوقت المناسب."
                             : "We'll notify you 45 mins before your appointment with traffic estimate.")
                            .font(.app(11.5, .regular, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textSecondary)

                        Spacer()
                    }
                    .padding(.top, 2)

                } else {
                    // Live Queue Layout
                    HStack(alignment: .center, spacing: 16) {
                        // Ticket Number
                        VStack(alignment: .leading, spacing: 2) {
                            Text(localization.isArabic ? "رقم تذكرتك" : "YOUR TICKET")
                                .font(.app(11, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textTertiary)
                                .textCase(.uppercase)

                            Text(ticket.ticketNumber)
                                .font(.system(size: 38, weight: .heavy, design: .rounded))
                                .foregroundColor(AppColors.ink)
                        }

                        Spacer()

                        // Hairline Vertical Divider
                        Rectangle()
                            .fill(AppColors.cardBorder.opacity(0.7))
                            .frame(width: 1, height: 42)

                        Spacer()

                        // Serving Now
                        VStack(alignment: .trailing, spacing: 2) {
                            Text(localization.isArabic ? "الدور الحالي" : "NOW SERVING")
                                .font(.app(11, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textTertiary)
                                .textCase(.uppercase)

                            HStack(spacing: 6) {
                                Circle()
                                    .fill(AppColors.accentGreen)
                                    .frame(width: 7, height: 7)

                                Text(ticket.currentServing)
                                    .font(.system(size: 22, weight: .heavy, design: .rounded))
                                    .foregroundColor(AppColors.teal)
                            }

                            Text(localization.isArabic ? "أمامك \(ticket.waitingAhead) أشخاص" : "\(ticket.waitingAhead) ahead")
                                .font(.app(12, .medium, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textSecondary)
                        }
                    }
                    .padding(.top, 4)

                    // Minimal Smooth Progress Track
                    VStack(spacing: 6) {
                        GeometryReader { geo in
                            ZStack(alignment: .leading) {
                                Capsule()
                                    .fill(AppColors.softTeal)
                                    .frame(height: 6)

                                Capsule()
                                    .fill(
                                        LinearGradient(
                                            colors: [AppColors.teal, AppColors.deepTeal],
                                            startPoint: .leading,
                                            endPoint: .trailing
                                        )
                                    )
                                    .frame(width: max(12, geo.size.width * CGFloat(ticket.progress)), height: 6)
                            }
                        }
                        .frame(height: 6)

                        HStack {
                            Text(localization.isArabic
                                 ? "شباك الخدمة: \(ticket.localizedCounter(isArabic: localization.isArabic))"
                                 : "Window: \(ticket.localizedCounter(isArabic: localization.isArabic))")
                                .font(.app(11, .regular, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textSecondary)

                            Spacer()

                            Text(localization.isArabic
                                 ? "الوقت التقديري: ~\(ticket.estMinutes) دقائق"
                                 : "Est. wait: ~\(ticket.estMinutes) mins")
                                .font(.app(11, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.teal)
                        }
                    }
                    .padding(.top, 4)
                }

                // Location & Route Footer Strip
                Divider()
                    .overlay(AppColors.cardBorder.opacity(0.6))
                    .padding(.top, 2)

                let clinicTarget = ticket.destinationCoordinate
                let distText = UserLocationService.shared.formattedDistance(to: clinicTarget, isArabic: localization.isArabic)
                let driveMins = UserLocationService.shared.estimatedDriveMinutes(to: clinicTarget)
                let driveTimeText = UserLocationService.shared.formattedDriveTime(minutes: driveMins, isArabic: localization.isArabic)

                HStack {
                    HStack(spacing: 5) {
                        Image(systemName: "car.fill")
                            .font(.system(size: 11))
                            .foregroundColor(AppColors.textTertiary)

                        Text(localization.isArabic ? "\(distText) • حوالي \(driveTimeText) بالسيارة" : "\(distText) • ~\(driveTimeText) drive")
                            .font(.app(12, .medium, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textSecondary)
                    }

                    Spacer()

                    Menu {
                        Button(action: {
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                            showLiveTracking = true
                        }) {
                            Label(localization.isArabic ? "معاينة المسار المباشر" : "Live Route in Taboor", systemImage: "map.fill")
                        }

                        Divider()

                        Button(action: {
                            NavigationLauncher.shared.open(app: .appleMaps, latitude: clinicTarget.latitude, longitude: clinicTarget.longitude, destinationName: ticket.localizedServiceName(isArabic: localization.isArabic))
                        }) {
                            Label(localization.isArabic ? "Apple Maps" : "Apple Maps", systemImage: "arrow.triangle.turn.up.right.diamond")
                        }

                        Button(action: {
                            NavigationLauncher.shared.open(app: .googleMaps, latitude: clinicTarget.latitude, longitude: clinicTarget.longitude, destinationName: ticket.localizedServiceName(isArabic: localization.isArabic))
                        }) {
                            Label(localization.isArabic ? "Google Maps" : "Google Maps", systemImage: "location.fill")
                        }
                    } label: {
                        HStack(spacing: 3) {
                            Text(localization.isArabic ? "الاتجاهات" : "Route")
                                .font(.app(12, .bold, isArabic: localization.isArabic))
                            Image(systemName: localization.isArabic ? "arrow.up.left" : "arrow.up.right")
                                .font(.system(size: 10, weight: .bold))
                        }
                        .foregroundColor(AppColors.teal)
                    }
                }
            }
            .padding(.horizontal, 18)
            .padding(.top, 14)
            .padding(.bottom, 16)
        }
        .clipShape(TicketShape(cornerRadius: 22, notchRadius: 10, notchY: ticketCutoutY))
        .background(
            TicketShape(cornerRadius: 22, notchRadius: 10, notchY: ticketCutoutY)
                .fill(Color.white)
                .shadow(color: Color.black.opacity(0.04), radius: 10, x: 0, y: 3)
        )
        .overlay(
            TicketShape(cornerRadius: 22, notchRadius: 10, notchY: ticketCutoutY)
                .stroke(AppColors.cardBorder, lineWidth: 1)
        )
        .onPreferenceChange(TicketCutoutYPreferenceKey.self) { y in
            if y > 0 && abs(self.ticketCutoutY - y) > 0.5 {
                self.ticketCutoutY = y
            }
        }
    }

    // MARK: - 3. Quick Action Buttons
    private func quickActions(ticket: UserTicket) -> some View {
        VStack(spacing: 9) {
            if ticket.bookingType == .scheduled && ticket.scheduledDate != "اليوم" {
                // SCHEDULED FOR TOMORROW / FUTURE:

                // 1. Primary Full-Width Button: Add to Apple Calendar
                Button(action: {
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    Task {
                        isAddingToCalendar = true
                        let result = await AppleCalendarManager.shared.addAppointmentEvent(ticket: ticket, isArabic: localization.isArabic)
                        isAddingToCalendar = false

                        switch result {
                        case .success(let date):
                            calendarEventDate = date
                            calendarAlertMessage = localization.isArabic
                                ? "تمت إضافة الموعد بنجاح إلى تقويم آبل الرسمي مع تذكير تلقائي قبل الموعد بـ 45 دقيقة وساعتين ✨"
                                : "Appointment successfully added to Apple Calendar with smart 45m and 2h reminders ✨"
                            UINotificationFeedbackGenerator().notificationOccurred(.success)
                            showAddedToCalendarAlert = true

                        case .permissionDenied:
                            UINotificationFeedbackGenerator().notificationOccurred(.warning)
                            showCalendarPermissionAlert = true

                        case .failed(let error):
                            calendarAlertMessage = error
                            UINotificationFeedbackGenerator().notificationOccurred(.error)
                            showAddedToCalendarAlert = true
                        }
                    }
                }) {
                    HStack(spacing: 8) {
                        if isAddingToCalendar {
                            ProgressView()
                                .tint(.white)
                                .frame(maxWidth: .infinity, alignment: .center)
                        } else {
                            Image(systemName: "calendar.badge.plus")
                                .font(.system(size: 14, weight: .bold))

                            Text(localization.isArabic ? "إضافة الموعد إلى تقويم آبل" : "Add to Apple Calendar")
                                .font(.app(13.5, .bold, isArabic: localization.isArabic))

                            Spacer()

                            Image(systemName: "checkmark.circle.fill")
                                .font(.system(size: 13))
                                .opacity(0.7)
                        }
                    }
                    .foregroundColor(.white)
                    .padding(.horizontal, 18)
                    .padding(.vertical, 13)
                    .background(
                        LinearGradient(
                            colors: [AppColors.ink, AppColors.deepTeal],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                    .shadow(color: AppColors.ink.opacity(0.16), radius: 5, y: 2)
                }
                .disabled(isAddingToCalendar)
                .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.90))

                // 2. Secondary Row: Preview Location & Share
                HStack(spacing: 10) {
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                        showLiveTracking = true
                    }) {
                        HStack(spacing: 6) {
                            Image(systemName: "map")
                                .font(.system(size: 13, weight: .bold))
                            Text(localization.isArabic ? "معاينة المسار" : "Preview Route")
                                .font(.app(12.5, .bold, isArabic: localization.isArabic))
                        }
                        .foregroundColor(AppColors.textPrimary)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 11)
                        .background(Color.white)
                        .clipShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 13, style: .continuous)
                                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
                        )
                    }
                    .buttonStyle(ScaleButtonStyle(scale: 0.97, pressedOpacity: 0.85))

                    Button(action: {
                        ShareTicketHelper.shareTicket(ticket, isArabic: localization.isArabic)
                    }) {
                        HStack(spacing: 6) {
                            Image(systemName: "square.and.arrow.up")
                                .font(.system(size: 13, weight: .bold))
                            Text(localization.isArabic ? "مشاركة الموعد" : "Share")
                                .font(.app(12.5, .bold, isArabic: localization.isArabic))
                        }
                        .foregroundColor(AppColors.textPrimary)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 11)
                        .background(Color.white)
                        .clipShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 13, style: .continuous)
                                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
                        )
                    }
                    .buttonStyle(ScaleButtonStyle(scale: 0.97, pressedOpacity: 0.85))
                }

                // 3. Refined Cancellation Button (Quiet luxury capsule with icon)
                Button(action: {
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    showCancelAlert = true
                }) {
                    HStack(spacing: 6) {
                        Image(systemName: "calendar.badge.minus")
                            .font(.system(size: 13, weight: .semibold))

                        Text(localization.isArabic ? "إلغاء حجز هذا الموعد" : "Cancel Scheduled Appointment")
                            .font(.app(12.5, .bold, isArabic: localization.isArabic))
                    }
                    .foregroundColor(AppColors.accentRed)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8.5)
                    .background(AppColors.accentRed.opacity(0.08))
                    .clipShape(Capsule())
                }
                .buttonStyle(ScaleButtonStyle(scale: 0.97, pressedOpacity: 0.85))
                .padding(.top, 2)

            } else {
                // LIVE QUEUE ACTIONS:

                // 1. Primary Full-Width CTA: Live Map Tracking
                Button(action: {
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    showLiveTracking = true
                }) {
                    HStack(spacing: 8) {
                        Image(systemName: "map.fill")
                            .font(.system(size: 14, weight: .bold))

                        Text(localization.isArabic ? "فتح شاشة التتبع والخريطة المباشرة" : "Open Live Map Tracking")
                            .font(.app(13.5, .bold, isArabic: localization.isArabic))

                        Spacer()

                        Image(systemName: localization.isArabic ? "arrow.left" : "arrow.right")
                            .font(.system(size: 12, weight: .bold))
                    }
                    .foregroundColor(.white)
                    .padding(.horizontal, 18)
                    .padding(.vertical, 13)
                    .background(
                        LinearGradient(
                            colors: [AppColors.ink, AppColors.deepTeal],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                    .shadow(color: AppColors.ink.opacity(0.16), radius: 5, y: 2)
                }
                .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.90))

                // 2. Secondary Row: Postpone & Share
                HStack(spacing: 10) {
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                        withAnimation(.spring(response: 0.35, dampingFraction: 0.7)) {
                            dataService.delayActiveTicket(minutes: 15)
                            postponeTrigger.toggle()
                        }
                    }) {
                        HStack(spacing: 6) {
                            Image(systemName: "clock.arrow.circlepath")
                                .font(.system(size: 13, weight: .bold))
                            Text(localization.isArabic ? "تأجيل (15 د)" : "Postpone (15m)")
                                .font(.app(12.5, .bold, isArabic: localization.isArabic))
                        }
                        .foregroundColor(AppColors.textPrimary)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 11)
                        .background(Color.white)
                        .clipShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 13, style: .continuous)
                                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
                        )
                    }
                    .buttonStyle(ScaleButtonStyle(scale: 0.97, pressedOpacity: 0.85))

                    Button(action: {
                        ShareTicketHelper.shareTicket(ticket, isArabic: localization.isArabic)
                    }) {
                        HStack(spacing: 6) {
                            Image(systemName: "square.and.arrow.up")
                                .font(.system(size: 13, weight: .bold))
                            Text(localization.isArabic ? "مشاركة" : "Share")
                                .font(.app(12.5, .bold, isArabic: localization.isArabic))
                        }
                        .foregroundColor(AppColors.textPrimary)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 11)
                        .background(Color.white)
                        .clipShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 13, style: .continuous)
                                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
                        )
                    }
                    .buttonStyle(ScaleButtonStyle(scale: 0.97, pressedOpacity: 0.85))
                }

                // 3. Refined Cancellation Button (Quiet luxury capsule with icon)
                Button(action: {
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    showCancelAlert = true
                }) {
                    HStack(spacing: 6) {
                        Image(systemName: "xmark.circle")
                            .font(.system(size: 13, weight: .semibold))

                        Text(localization.isArabic ? "إلغاء التذكرة والانسحاب من الدور" : "Cancel Ticket & Leave Line")
                            .font(.app(12.5, .bold, isArabic: localization.isArabic))
                    }
                    .foregroundColor(AppColors.accentRed)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8.5)
                    .background(AppColors.accentRed.opacity(0.08))
                    .clipShape(Capsule())
                }
                .buttonStyle(ScaleButtonStyle(scale: 0.97, pressedOpacity: 0.85))
                .padding(.top, 2)
            }
        }
    }

    // MARK: - 4. Empty State Card (Apple World-Class Empty State)
    private var emptyQueueCard: some View {
        VStack(spacing: 20) {
            // Layered Icon Badge with Warm Highlights
            ZStack {
                Circle()
                    .fill(AppColors.softTeal)
                    .frame(width: 86, height: 86)

                Circle()
                    .strokeBorder(AppColors.teal.opacity(0.25), lineWidth: 1.5)
                    .frame(width: 104, height: 104)

                Image(systemName: "sparkles")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(AppColors.amber)
                    .offset(x: 28, y: -26)

                Image(systemName: "ticket.fill")
                    .font(.system(size: 36, weight: .bold))
                    .foregroundColor(AppColors.teal)
            }
            .padding(.top, 10)

            // Warm & Engaging Copy
            VStack(spacing: 8) {
                Text(localization.isArabic ? "يومك رايق! ما عندكش أي طوابير نشطة ✨" : "Your day is clear! No active queues ✨")
                    .font(.app(18, .extraBold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)
                    .multilineTextAlignment(.center)

                Text(localization.isArabic
                     ? "وفّر وقتك في صالات الانتظار.. ابحث عن عيادتك أو وجهتك القادمة واحجز دورك وأنت في مكانك."
                     : "Enjoy your free time and skip waiting rooms. Discover nearby clinics, salons, and offices to book your turn ahead.")
                    .font(.app(13, .regular, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
                    .multilineTextAlignment(.center)
                    .lineSpacing(3.5)
                    .padding(.horizontal, 10)
            }

            // Micro Value Pills
            HStack(spacing: 8) {
                featurePill(icon: "bell.badge", text: localization.isArabic ? "تنبيه قبل دورك" : "Alerts")
                featurePill(icon: "timer", text: localization.isArabic ? "توفير الوقت" : "Save time")
                featurePill(icon: "shield.checkmark", text: localization.isArabic ? "بدون زحام" : "No crowd")
            }

            // CTAs
            VStack(spacing: 10) {
                // Primary: Discover Queues
                Button(action: {
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    showAllServices = true
                }) {
                    HStack(spacing: 8) {
                        Image(systemName: "magnifyingglass")
                            .font(.system(size: 13, weight: .bold))

                        Text(localization.isArabic ? "استكشاف الطوابير القريبة" : "Explore Nearby Queues")
                            .font(.app(14, .bold, isArabic: localization.isArabic))
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 13)
                    .background(
                        LinearGradient(
                            colors: [AppColors.teal, AppColors.deepTeal],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                    .shadow(color: AppColors.teal.opacity(0.28), radius: 6, y: 2)
                }
                .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.90))

                // Secondary: Demo Ticket for instant simulator preview
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    withAnimation(.spring(response: 0.4, dampingFraction: 0.8)) {
                        dataService.activeTicket = UserTicket(
                            ticketNumber: "A-12",
                            serviceName: "Dar Al Fouad Hospital",
                            serviceNameAr: "مستشفى دار الفؤاد - العيادات الخارجية",
                            branchLabel: "Internal Medicine Clinic",
                            branchLabelAr: "عيادة الباطنة والقلب • د. أحمد شاكر",
                            counterLabel: "Counter 3",
                            counterLabelAr: "شباك رقم 3",
                            waitingAhead: 2,
                            estMinutes: 8,
                            currentServing: "A-10",
                            progress: 0.75,
                            isActive: true
                        )
                    }
                }) {
                    HStack(spacing: 6) {
                        Image(systemName: "sparkles")
                            .font(.system(size: 13, weight: .semibold))
                            .foregroundColor(AppColors.amber)

                        Text(localization.isArabic ? "تجربة تذكرة نشطة (#A-12)" : "Preview Demo Ticket (#A-12)")
                            .font(.app(12.5, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.ink)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .background(AppColors.softTeal)
                    .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                }
                .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.90))
            }
            .padding(.horizontal, 4)
            .padding(.bottom, 4)
        }
        .padding(22)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 22, style: .continuous)
                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.04), radius: 12, y: 4)
    }

    private func featurePill(icon: String, text: String) -> some View {
        HStack(spacing: 4) {
            Image(systemName: icon)
                .font(.system(size: 10, weight: .semibold))
                .foregroundColor(AppColors.teal)

            Text(text)
                .font(.app(10.5, .medium, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textSecondary)
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 4.5)
        .background(Color(UIColor.systemGray6).opacity(0.7))
        .clipShape(Capsule())
    }

    // MARK: - 5. Past Visits History Section
    private var historySection: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                Label {
                    Text(localization.isArabic ? "الزيارات السابقة" : "Past Visits")
                        .font(.app(17, .extraBold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textPrimary)
                } icon: {
                    Image(systemName: "clock.arrow.circlepath")
                        .foregroundColor(AppColors.teal)
                }

                Spacer()

                Text("\(dataService.history.count)")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(AppColors.textSecondary)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 2.5)
                    .background(AppColors.cardBorder.opacity(0.5))
                    .clipShape(Capsule())
            }

            VStack(spacing: 10) {
                ForEach(dataService.history) { item in
                    HStack(spacing: 12) {
                        // Icon Squircle
                        ZStack {
                            RoundedRectangle(cornerRadius: 12, style: .continuous)
                                .fill(item.status == .completed ? AppColors.softTeal : AppColors.accentRed.opacity(0.12))
                                .frame(width: 44, height: 44)

                            Image(systemName: item.status == .completed ? "checkmark.seal.fill" : "xmark.circle.fill")
                                .font(.system(size: 20))
                                .foregroundColor(item.status == .completed ? AppColors.teal : AppColors.accentRed)
                        }

                        // Info
                        VStack(alignment: .leading, spacing: 3) {
                            Text(localization.isArabic ? item.serviceNameAr : item.serviceName)
                                .font(.app(14, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textPrimary)
                                .lineLimit(2)
                                .fixedSize(horizontal: false, vertical: true)

                            HStack(spacing: 4) {
                                Text(localization.isArabic ? item.branchLabelAr : item.branchLabel)
                                    .font(.app(11.5, .regular, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textSecondary)
                                    .lineLimit(1)

                                Text("•")
                                    .foregroundColor(AppColors.textTertiary)

                                Text(item.dateString)
                                    .font(.app(11.5, .regular, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textTertiary)
                            }
                        }

                        Spacer()

                        // Ticket & Status Pill
                        VStack(alignment: .trailing, spacing: 4) {
                            Text("#\(item.ticketNumber)")
                                .font(.system(size: 14, weight: .black, design: .rounded))
                                .foregroundColor(AppColors.ink)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 2)
                                .background(AppColors.softTeal)
                                .clipShape(RoundedRectangle(cornerRadius: 6, style: .continuous))

                            Text(item.status == .completed
                                 ? (localization.isArabic ? "مكتملة (\(item.durationMinutes)د)" : "Completed")
                                 : (localization.isArabic ? "ملغاة" : "Cancelled"))
                                .font(.app(10.5, .bold, isArabic: localization.isArabic))
                                .foregroundColor(item.status == .completed ? AppColors.accentGreen : AppColors.accentRed)
                        }
                    }
                    .padding(14)
                    .background(Color.white)
                    .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                    .overlay(
                        RoundedRectangle(cornerRadius: 18, style: .continuous)
                            .strokeBorder(AppColors.cardBorder, lineWidth: 1)
                    )
                    .shadow(color: Color.black.opacity(0.02), radius: 4, y: 2)
                    .contextMenu {
                        Button {
                            if let place = dataService.places.first(where: { $0.name == item.serviceName || $0.nameAr == item.serviceNameAr }) {
                                selectedPlaceForDetails = place
                            } else {
                                showAllServices = true
                            }
                        } label: {
                            Label(localization.isArabic ? "حجز دور مجدداً" : "Book Again", systemImage: "arrow.clockwise")
                        }

                        ShareLink(item: "\(item.serviceName) - #\(item.ticketNumber)") {
                            Label(localization.isArabic ? "مشاركة بيانات الزيارة" : "Share Visit Info", systemImage: "square.and.arrow.up")
                        }
                    }
                }
            }
        }
    }
}

// MARK: - Dashed Line Shape
private struct DashedLine: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: 0, y: rect.midY))
        path.addLine(to: CGPoint(x: rect.width, y: rect.midY))
        return path
    }
}

// MARK: - Ticket Cutout Y PreferenceKey
private struct TicketCutoutYPreferenceKey: PreferenceKey {
    static var defaultValue: CGFloat = 0
    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) {
        let n = nextValue()
        if n > 0 {
            value = n
        }
    }
}

// MARK: - Authentic Ticket Shape with Real Inward Semicircular Cutouts
public struct TicketShape: Shape {
    public var cornerRadius: CGFloat = 22
    public var notchRadius: CGFloat = 10
    public var notchY: CGFloat = 86

    public var animatableData: CGFloat {
        get { notchY }
        set { notchY = newValue }
    }

    public func path(in rect: CGRect) -> Path {
        var path = Path()
        let cl = rect.minX
        let cr = rect.maxX
        let ct = rect.minY
        let cb = rect.maxY
        let nr = notchRadius
        let r = cornerRadius
        let rawNy = notchY > 0 ? notchY : rect.height * 0.30
        let ny = min(max(rawNy, ct + r + nr), cb - r - nr)

        // 1. Top edge: Start after top-left corner
        path.move(to: CGPoint(x: cl + r, y: ct))

        // Top edge to top-right corner
        path.addLine(to: CGPoint(x: cr - r, y: ct))
        path.addArc(center: CGPoint(x: cr - r, y: ct + r), radius: r, startAngle: .degrees(-90), endAngle: .degrees(0), clockwise: false)

        // 2. Right edge down to notch
        path.addLine(to: CGPoint(x: cr, y: ny - nr))
        // Inward notch on right edge
        path.addArc(center: CGPoint(x: cr, y: ny), radius: nr, startAngle: .degrees(-90), endAngle: .degrees(90), clockwise: true)
        // Right edge down to bottom-right corner
        path.addLine(to: CGPoint(x: cr, y: cb - r))
        path.addArc(center: CGPoint(x: cr - r, y: cb - r), radius: r, startAngle: .degrees(0), endAngle: .degrees(90), clockwise: false)

        // 3. Bottom edge to bottom-left corner
        path.addLine(to: CGPoint(x: cl + r, y: cb))
        path.addArc(center: CGPoint(x: cl + r, y: cb - r), radius: r, startAngle: .degrees(90), endAngle: .degrees(180), clockwise: false)

        // 4. Left edge up to notch
        path.addLine(to: CGPoint(x: cl, y: ny + nr))
        // Inward notch on left edge
        path.addArc(center: CGPoint(x: cl, y: ny), radius: nr, startAngle: .degrees(90), endAngle: .degrees(270), clockwise: true)
        // Left edge up to top-left corner
        path.addLine(to: CGPoint(x: cl, y: ny - nr))
        path.addLine(to: CGPoint(x: cl, y: ct + r))
        path.addArc(center: CGPoint(x: cl + r, y: ct + r), radius: r, startAngle: .degrees(180), endAngle: .degrees(270), clockwise: false)

        path.closeSubpath()
        return path
    }
}
