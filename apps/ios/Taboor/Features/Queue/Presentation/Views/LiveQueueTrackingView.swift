import SwiftUI
import MapKit

// MARK: - Full-Screen Uber / Talabat Live Route Tracking Modal View

public struct LiveQueueTrackingView: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var dataService = QueueDataService.shared
    @ObservedObject private var localization = LocalizationManager.shared
    @Environment(\.colorScheme) private var colorScheme

    @State private var showCancelAlert = false
    @State private var postponeTrigger = false

    // Interactive Bottom Sheet Detents (Scrollable/Draggable)
    @State private var isSheetCollapsed: Bool = false
    @State private var dragOffset: CGFloat = 0
    private let collapsedOffset: CGFloat = 150

    private var currentSheetOffset: CGFloat {
        let base: CGFloat = isSheetCollapsed ? collapsedOffset : 0
        let raw = base + dragOffset
        if raw < -20 {
            return -20 + (raw + 20) * 0.2
        } else if raw > collapsedOffset + 30 {
            return collapsedOffset + 30 + (raw - (collapsedOffset + 30)) * 0.2
        }
        return raw
    }

    // Real Route Coordinates (Cairo - Nasr City)
    private let userCoord = CLLocationCoordinate2D(latitude: 30.0460, longitude: 31.3340)
    private let navCoord = CLLocationCoordinate2D(latitude: 30.0525, longitude: 31.3405)
    private let clinicCoord = CLLocationCoordinate2D(latitude: 30.0585, longitude: 31.3465)
    private let routeCoordinates: [CLLocationCoordinate2D] = [
        CLLocationCoordinate2D(latitude: 30.0460, longitude: 31.3340),
        CLLocationCoordinate2D(latitude: 30.0490, longitude: 31.3370),
        CLLocationCoordinate2D(latitude: 30.0525, longitude: 31.3405),
        CLLocationCoordinate2D(latitude: 30.0555, longitude: 31.3435),
        CLLocationCoordinate2D(latitude: 30.0585, longitude: 31.3465)
    ]

    @State private var cameraPosition: MapCameraPosition = .region(
        MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: 30.0488, longitude: 31.3400),
            span: MKCoordinateSpan(latitudeDelta: 0.028, longitudeDelta: 0.028)
        )
    )

    public init() {}

    public var body: some View {
        Group {
            if let ticket = dataService.activeTicket {
                ZStack(alignment: .bottom) {
                    // 1. Real Full-Screen Native Apple Map
                    realAppleMapView(ticket: ticket)
                        .ignoresSafeArea()

                    // 2. Top Floating Live Status Capsule & Dismiss Button
                    VStack {
                        HStack(spacing: 12) {
                            // Floating Close Button
                            Button(action: {
                                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                dismiss()
                            }) {
                                ZStack {
                                    Circle()
                                        .fill(AppColors.cardBackground.opacity(0.92))
                                        .frame(width: 40, height: 40)
                                        .shadow(color: Color.black.opacity(0.12), radius: 6, y: 3)
                                        .overlay(
                                            Circle().strokeBorder(Color(UIColor.separator).opacity(0.2), lineWidth: 0.8)
                                        )

                                    Image(systemName: "xmark")
                                        .font(.system(size: 14, weight: .bold))
                                        .foregroundColor(AppColors.textPrimary)
                                }
                            }

                            // Top Live Status Capsule
                            topFloatingStatusCapsule(ticket: ticket)
                        }
                        .padding(.horizontal, 16)
                        .padding(.top, 10)

                        Spacer()
                    }

                    // 3. Modal Bottom Sheet
                    bottomTrackingDrawer(ticket: ticket)
                }
                .ignoresSafeArea(edges: .bottom)
                .toolbar(.hidden, for: .navigationBar)
            } else {
                VStack(spacing: 16) {
                    Text(localization.isArabic ? "لا يوجد دور نشط حالياً" : "No active ticket")
                        .font(.app(16, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)

                    Button(localization.isArabic ? "إغلاق" : "Close") {
                        dismiss()
                    }
                    .font(.app(14, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.teal)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(AppColors.screenBackground.ignoresSafeArea())
            }
        }
        .alert(localization.isArabic ? "إلغاء التذكرة" : "Cancel Ticket", isPresented: $showCancelAlert) {
            Button(localization.isArabic ? "تراجع" : "Keep", role: .cancel) {}
            Button(localization.isArabic ? "تأكيد الإلغاء" : "Cancel Ticket", role: .destructive) {
                withAnimation(.spring(response: 0.4, dampingFraction: 0.8)) {
                    dataService.cancelActiveTicket()
                    UINotificationFeedbackGenerator().notificationOccurred(.warning)
                    dismiss()
                }
            }
        } message: {
            Text(localization.isArabic
                 ? "هل أنت متأكد من رغبتك في إلغاء دورك الحالي؟ ستفقد مكانك في الطابور."
                 : "Are you sure you want to cancel your ticket? You will lose your spot in line.")
        }
        .environment(\.layoutDirection, localization.layoutDirection)
    }

    // MARK: - 1. Real Native Apple Map (MapKit)
    private func realAppleMapView(ticket: UserTicket) -> some View {
        ZStack(alignment: .topTrailing) {
            Map(position: $cameraPosition) {
                // Real Route Polyline (Teal glowing line)
                MapPolyline(coordinates: routeCoordinates)
                    .stroke(
                        AppColors.teal,
                        style: StrokeStyle(lineWidth: 6, lineCap: .round, lineJoin: .round)
                    )

                // 1. User Location Pin
                Annotation(localization.isArabic ? "موقعك" : "You", coordinate: userCoord) {
                    ZStack {
                        Circle()
                            .fill(AppColors.teal.opacity(0.25))
                            .frame(width: 32, height: 32)
                        Circle()
                            .fill(AppColors.teal)
                            .frame(width: 14, height: 14)
                        Circle()
                            .fill(Color.white)
                            .frame(width: 5, height: 5)
                    }
                }

                // 2. In-Transit Navigation Indicator (Moving vehicle/heading)
                Annotation(localization.isArabic ? "في الطريق" : "Navigating", coordinate: navCoord) {
                    ZStack {
                        Circle()
                            .fill(Color(hex: 0x0F172A))
                            .frame(width: 28, height: 28)
                            .overlay(Circle().stroke(Color.white, lineWidth: 2))
                            .shadow(color: Color.black.opacity(0.25), radius: 4)

                        Image(systemName: "location.north.fill")
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(AppColors.accentGreen)
                            .rotationEffect(.degrees(45))
                    }
                }

                // 3. Destination Clinic Pin
                Annotation(ticket.localizedServiceName(isArabic: localization.isArabic), coordinate: clinicCoord) {
                    VStack(spacing: 0) {
                        ZStack {
                            Circle()
                                .fill(AppColors.deepTeal)
                                .frame(width: 34, height: 34)
                                .overlay(Circle().stroke(Color.white, lineWidth: 2.2))
                                .shadow(color: Color.black.opacity(0.25), radius: 5, y: 3)

                            Image(systemName: "cross.fill")
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(.white)
                        }

                        Image(systemName: "arrowtriangle.down.fill")
                            .font(.system(size: 10))
                            .foregroundColor(AppColors.deepTeal)
                            .offset(y: -2.5)
                    }
                }
            }
            .mapStyle(.standard(elevation: .realistic))
            .mapControls {
                MapCompass()
                MapScaleView()
            }

            // Floating Recenter Button
            Button(action: {
                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                withAnimation(.spring(response: 0.45, dampingFraction: 0.75)) {
                    cameraPosition = .region(
                        MKCoordinateRegion(
                            center: CLLocationCoordinate2D(latitude: 30.0488, longitude: 31.3400),
                            span: MKCoordinateSpan(latitudeDelta: 0.028, longitudeDelta: 0.028)
                        )
                    )
                }
            }) {
                ZStack {
                    Circle()
                        .fill(AppColors.cardBackground)
                        .frame(width: 44, height: 44)
                        .shadow(color: Color.black.opacity(0.12), radius: 6, y: 3)
                        .overlay(Circle().strokeBorder(Color(UIColor.separator).opacity(0.2), lineWidth: 0.8))

                    Image(systemName: "location.fill")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(AppColors.teal)
                }
            }
            .padding(.trailing, 16)
            .padding(.top, 74)
        }
    }

    // MARK: - 2. Top Floating Live Status Capsule
    private func topFloatingStatusCapsule(ticket: UserTicket) -> some View {
        HStack(spacing: 10) {
            Circle()
                .fill(AppColors.accentGreen)
                .frame(width: 8, height: 8)
                .symbolEffect(.pulse)

            Text(localization.isArabic ? "أنت في الطريق للعيادة • دورك يقترب" : "On the way • Your turn is near")
                .font(.app(12, .bold, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textPrimary)
                .lineLimit(1)

            Spacer()

            HStack(spacing: 4) {
                Image(systemName: "clock.fill")
                    .font(.system(size: 10, weight: .bold))
                Text(localization.isArabic ? "~\(ticket.estMinutes) دقائق" : "~\(ticket.estMinutes)m")
                    .font(.app(11.5, .bold, isArabic: localization.isArabic))
            }
            .foregroundColor(.white)
            .padding(.horizontal, 9)
            .padding(.vertical, 4.5)
            .background(
                LinearGradient(
                    colors: [AppColors.teal, AppColors.deepTeal],
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .clipShape(Capsule())
            .shadow(color: AppColors.teal.opacity(0.35), radius: 4, y: 2)
        }
        .padding(.horizontal, 13)
        .padding(.vertical, 9)
        .background(
            RoundedRectangle(cornerRadius: 22, style: .continuous)
                .fill(AppColors.cardBackground.opacity(0.95))
                .overlay(
                    RoundedRectangle(cornerRadius: 22, style: .continuous)
                        .strokeBorder(Color(UIColor.separator).opacity(colorScheme == .dark ? 0.3 : 0.15), lineWidth: 0.8)
                )
        )
        .shadow(color: Color.black.opacity(colorScheme == .dark ? 0.35 : 0.08), radius: 10, y: 4)
    }

    // MARK: - 3. Modal Bottom Sheet (Uber / Talabat Style)
    private func bottomTrackingDrawer(ticket: UserTicket) -> some View {
        VStack(spacing: 10) {
            // Interactive Drag Handle & Peek Bar
            Button(action: {
                withAnimation(.spring(response: 0.35, dampingFraction: 0.80)) {
                    isSheetCollapsed.toggle()
                }
                UIImpactFeedbackGenerator(style: .light).impactOccurred()
            }) {
                VStack(spacing: 3) {
                    Capsule()
                        .fill(Color(UIColor.systemGray3))
                        .frame(width: 44, height: 4.5)
                        .padding(.top, 7)
                        .padding(.bottom, 2)
                }
                .frame(maxWidth: .infinity)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)

            VStack(spacing: 10) {
                // Header: Clinic Name & Branch + Ticket Badge
                HStack(alignment: .center, spacing: 12) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(ticket.localizedServiceName(isArabic: localization.isArabic))
                            .font(.app(15.5, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)
                            .lineLimit(2)
                            .fixedSize(horizontal: false, vertical: true)

                        Text("\(ticket.localizedBranch(isArabic: localization.isArabic)) • \(ticket.localizedCounter(isArabic: localization.isArabic))")
                            .font(.app(11.5, .medium, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textSecondary)
                    }

                    Spacer()

                    // Ticket Badge
                    VStack(spacing: 1) {
                        Text(localization.isArabic ? "تذكرتك" : "Ticket")
                            .font(.app(9, .medium, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textSecondary)

                        Text("#\(ticket.ticketNumber)")
                            .font(.system(size: 22, weight: .black, design: .rounded))
                            .foregroundColor(AppColors.teal)
                            .contentTransition(.numericText())
                    }
                    .padding(.horizontal, 13)
                    .padding(.vertical, 6)
                    .background(AppColors.teal.opacity(colorScheme == .dark ? 0.22 : 0.10))
                    .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                    .overlay(
                        RoundedRectangle(cornerRadius: 12, style: .continuous)
                            .strokeBorder(AppColors.teal.opacity(0.35), lineWidth: 1)
                    )
                }

                // Progress Bar & Real-time Waiting Stats
                VStack(spacing: 5) {
                    ProgressView(value: ticket.progress)
                        .tint(AppColors.teal)

                    HStack {
                        HStack(spacing: 4) {
                            Circle()
                                .fill(AppColors.amber)
                                .frame(width: 6, height: 6)
                            Text(localization.isArabic
                                 ? "أمامك \(ticket.waitingAhead) في الانتظار"
                                 : "\(ticket.waitingAhead) ahead of you")
                                .font(.app(11.5, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.amber)
                        }

                        Spacer()

                        Text(localization.isArabic
                             ? "الآن بالداخل: \(ticket.currentServing)"
                             : "Now Serving: \(ticket.currentServing)")
                            .font(.app(11, .medium, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textSecondary)
                    }
                }

                // Collapsible Actions & Stepper Area
                VStack(spacing: 10) {
                    // Real-Time Queue Line Stepper
                    HStack(spacing: 8) {
                        queueNode(number: ticket.currentServing, labelAr: "في الخدمة", labelEn: "Serving", isCurrent: false, isUser: false)
                        arrowSeparator
                        queueNode(number: "#A-11", labelAr: "التالي", labelEn: "Next", isCurrent: false, isUser: false)
                        arrowSeparator
                        queueNode(number: "#\(ticket.ticketNumber)", labelAr: "أنت هنا", labelEn: "You", isCurrent: true, isUser: true)
                    }

                    // Action Buttons (Postpone + Cancel)
                    HStack(spacing: 10) {
                        Button(action: {
                            withAnimation(.spring(response: 0.35, dampingFraction: 0.7)) {
                                dataService.delayActiveTicket(minutes: 10)
                                postponeTrigger.toggle()
                            }
                        }) {
                            HStack(spacing: 6) {
                                Image(systemName: "clock.arrow.circlepath")
                                Text(localization.isArabic ? "تأجيل الدور (+10د)" : "Postpone (+10m)")
                            }
                            .font(.app(12, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 9.5)
                            .background(Color(UIColor.secondarySystemBackground))
                            .clipShape(Capsule())
                            .overlay(
                                Capsule().strokeBorder(Color(UIColor.separator).opacity(0.25), lineWidth: 0.8)
                            )
                        }
                        .sensoryFeedback(.impact(weight: .medium), trigger: postponeTrigger)

                        Button(action: {
                            showCancelAlert = true
                        }) {
                            HStack(spacing: 6) {
                                Image(systemName: "xmark.circle")
                                Text(localization.isArabic ? "إلغاء الدور" : "Cancel")
                            }
                            .font(.app(12, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.accentRed)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 9.5)
                            .background(AppColors.accentRed.opacity(colorScheme == .dark ? 0.20 : 0.10))
                            .clipShape(Capsule())
                            .overlay(
                                Capsule().strokeBorder(AppColors.accentRed.opacity(0.30), lineWidth: 0.8)
                            )
                        }
                    }
                }
                .opacity(max(0, 1.0 - Double(currentSheetOffset / 90.0)))
                .allowsHitTesting(!isSheetCollapsed)
            }
            .padding(.horizontal, 18)
        }
        .padding(.top, 4)
        .padding(.bottom, 40) // Comfortable bottom padding in full-screen modal
        .frame(maxWidth: .infinity)
        .background(
            UnevenRoundedRectangle(topLeadingRadius: 28, bottomLeadingRadius: 0, bottomTrailingRadius: 0, topTrailingRadius: 28, style: .continuous)
                .fill(AppColors.cardBackground)
                .padding(.bottom, -350)
                .overlay(
                    UnevenRoundedRectangle(topLeadingRadius: 28, bottomLeadingRadius: 0, bottomTrailingRadius: 0, topTrailingRadius: 28, style: .continuous)
                        .strokeBorder(Color(UIColor.separator).opacity(colorScheme == .dark ? 0.35 : 0.18), lineWidth: 0.8)
                        .padding(.bottom, -350)
                )
                .shadow(color: Color.black.opacity(colorScheme == .dark ? 0.40 : 0.10), radius: 18, x: 0, y: -4)
        )
        .offset(y: currentSheetOffset)
        .gesture(
            DragGesture(minimumDistance: 10, coordinateSpace: .local)
                .onChanged { value in
                    dragOffset = value.translation.height
                }
                .onEnded { value in
                    let velocity = value.predictedEndTranslation.height - value.translation.height
                    let totalChange = value.translation.height + velocity * 0.25

                    withAnimation(.spring(response: 0.35, dampingFraction: 0.80)) {
                        if totalChange > 35 {
                            isSheetCollapsed = true
                        } else if totalChange < -35 {
                            isSheetCollapsed = false
                        }
                        dragOffset = 0
                    }
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                }
        )
    }

    private func queueNode(number: String, labelAr: String, labelEn: String, isCurrent: Bool, isUser: Bool) -> some View {
        VStack(spacing: 4) {
            ZStack {
                Circle()
                    .fill(isUser ? AppColors.teal : (isCurrent ? AppColors.amber : Color(UIColor.secondarySystemBackground)))
                    .frame(width: 36, height: 36)
                    .overlay(
                        Circle()
                            .strokeBorder(isUser ? AppColors.teal : Color(UIColor.separator).opacity(0.3), lineWidth: isUser ? 2 : 0.8)
                    )

                Text(number)
                    .font(.app(11.5, .bold, isArabic: false))
                    .foregroundColor(isUser ? .white : (isCurrent ? .white : AppColors.textPrimary))
            }

            Text(localization.isArabic ? labelAr : labelEn)
                .font(.app(10, isUser ? .bold : .medium, isArabic: localization.isArabic))
                .foregroundColor(isUser ? AppColors.teal : AppColors.textSecondary)
        }
        .frame(maxWidth: .infinity)
    }

    private var arrowSeparator: some View {
        Image(systemName: localization.isArabic ? "arrow.left" : "arrow.right")
            .font(.system(size: 12, weight: .bold))
            .foregroundColor(AppColors.textTertiary)
            .padding(.bottom, 16)
    }
}
