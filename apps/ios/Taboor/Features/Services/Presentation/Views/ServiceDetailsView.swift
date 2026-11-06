import SwiftUI
import MapKit

// MARK: - Service Details Screen with Real Interactive Apple Map, Branch Selection, and Queue Joining

public struct ServiceDetailsView: View {
    public let place: ServicePlace
    public let onQueueJoined: () -> Void

    @StateObject private var dataService = QueueDataService.shared
    @ObservedObject private var localization = LocalizationManager.shared
    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var colorScheme

    @State private var selectedBranchIndex: Int = 0
    @State private var isJoining: Bool = false
    @State private var showSuccessModal: Bool = false
    @State private var issuedTicketNumber: String = ""
    @State private var cameraPosition: MapCameraPosition = .automatic

    public init(place: ServicePlace, onQueueJoined: @escaping () -> Void) {
        self.place = place
        self.onQueueJoined = onQueueJoined
    }

    private var currentBranch: ServiceBranch {
        if selectedBranchIndex < place.branches.count {
            return place.branches[selectedBranchIndex]
        }
        return place.branches.first ?? ServiceBranch(
            label: "Main",
            labelAr: "الرئيسي",
            distanceKm: "1.0",
            waiting: 0,
            address: "",
            addressAr: "",
            lat: 30.0525,
            lng: 31.2408
        )
    }

    public var body: some View {
        ZStack(alignment: .bottom) {
            ScrollView(showsIndicators: false) {
                VStack(spacing: 18) {
                    // 1. Real Interactive Apple Map at the Top
                    heroMapSection

                    // 2. Service Header & Rating Identity Card
                    serviceHeaderCard

                    // 3. Quick Real-Time Metrics Strip
                    quickMetricsStrip

                    // 4. Branch Selector Section with Live Queue Counts
                    branchSelectorSection

                    // 5. About Service Card
                    aboutSection

                    // 6. Working Hours & Open Status Banner
                    workingHoursSection

                    // Bottom Spacing for Floating Glass CTA Bar
                    Spacer().frame(height: 100)
                }
                .padding(.horizontal, 16)
                .padding(.top, 10)
            }

            // Floating Bottom Glass Action Bar
            bottomActionBar
        }
        .background(AppColors.screenBackground.ignoresSafeArea())
        .navigationTitle(place.localizedName(isArabic: localization.isArabic))
        .navigationBarTitleDisplayMode(.inline)
        .toolbarBackground(.ultraThinMaterial, for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    dismiss()
                }) {
                    Image(systemName: "xmark")
                }
            }
        }
        .id(localization.currentLanguage)
        .onAppear {
            updateCamera(animated: false)
        }
        .onChange(of: selectedBranchIndex) { _ in
            updateCamera(animated: true)
        }
        .sheet(isPresented: $showSuccessModal) {
            ticketSuccessSheet
        }
    }

    // MARK: - 1. Real Interactive Apple Map Section
    private var heroMapSection: some View {
        ZStack(alignment: .topTrailing) {
            Map(position: $cameraPosition) {
                ForEach(Array(place.branches.enumerated()), id: \.offset) { index, branch in
                    Annotation(
                        branch.localizedLabel(isArabic: localization.isArabic),
                        coordinate: CLLocationCoordinate2D(latitude: branch.lat, longitude: branch.lng)
                    ) {
                        BranchMapAnnotationPin(
                            place: place,
                            branch: branch,
                            isSelected: selectedBranchIndex == index,
                            isArabic: localization.isArabic,
                            onTap: {
                                withAnimation(.spring(response: 0.35, dampingFraction: 0.75)) {
                                    selectedBranchIndex = index
                                }
                            }
                        )
                    }
                }
            }
            .mapStyle(.standard(elevation: .realistic))
            .frame(height: 240)
            .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))

            // Floating Glass Controls (Apple Maps directions & Recenter)
            VStack(spacing: 8) {
                HStack(spacing: 8) {
                    Button(action: openInAppleMaps) {
                        HStack(spacing: 5) {
                            Image(systemName: "arrow.triangle.turn.up.right.circle.fill")
                                .font(.system(size: 13, weight: .bold))
                            Text(localization.isArabic ? "فتح في Apple Maps" : "Apple Maps")
                                .font(.app(11, .bold, isArabic: localization.isArabic))
                        }
                        .foregroundColor(AppColors.teal)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 6)
                        .background(.ultraThinMaterial)
                        .clipShape(Capsule())
                        .shadow(color: Color.black.opacity(0.12), radius: 6, y: 2)
                    }

                    Spacer()

                    Button(action: {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        updateCamera(animated: true)
                    }) {
                        Image(systemName: "location.fill")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundColor(AppColors.teal)
                            .frame(width: 32, height: 32)
                            .background(.ultraThinMaterial)
                            .clipShape(Circle())
                            .shadow(color: Color.black.opacity(0.12), radius: 6, y: 2)
                    }
                }
                .padding(10)

                Spacer()

                // Address & Distance Glass Pill at the bottom of the map
                HStack(spacing: 8) {
                    Image(systemName: "mappin.circle.fill")
                        .font(.system(size: 14))
                        .foregroundColor(AppColors.teal)

                    Text(currentBranch.localizedAddress(isArabic: localization.isArabic))
                        .font(.app(11, .medium, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textPrimary)
                        .lineLimit(1)

                    Spacer()

                    Text(localization.isArabic ? "\(currentBranch.distanceKm) كم" : "\(currentBranch.distanceKm) km")
                        .font(.system(size: 11, weight: .bold, design: .rounded))
                        .foregroundColor(AppColors.textSecondary)
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 7)
                .background(.ultraThinMaterial)
                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                .padding(10)
            }
        }
        .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 22, style: .continuous)
                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(colorScheme == .dark ? 0.3 : 0.05), radius: 10, y: 3)
    }

    private func updateCamera(animated: Bool) {
        let region = MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: currentBranch.lat, longitude: currentBranch.lng),
            span: MKCoordinateSpan(latitudeDelta: 0.012, longitudeDelta: 0.012)
        )
        if animated {
            withAnimation(.spring(response: 0.45, dampingFraction: 0.8)) {
                cameraPosition = .region(region)
            }
        } else {
            cameraPosition = .region(region)
        }
    }

    // MARK: - 2. Service Header & Rating Identity Card
    private var serviceHeaderCard: some View {
        VStack(spacing: 14) {
            HStack(spacing: 14) {
                ZStack {
                    RoundedRectangle(cornerRadius: 18, style: .continuous)
                        .fill(place.color.opacity(colorScheme == .dark ? 0.25 : 0.12))
                        .frame(width: 58, height: 58)

                    Image(systemName: place.iconName)
                        .font(.system(size: 26, weight: .semibold))
                        .foregroundColor(place.color)
                }

                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 6) {
                        Text(place.localizedName(isArabic: localization.isArabic))
                            .font(.app(17, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)
                            .lineLimit(2)

                        Image(systemName: "checkmark.seal.fill")
                            .font(.system(size: 14))
                            .foregroundColor(AppColors.teal)
                    }

                    HStack(spacing: 8) {
                        // Category Pill
                        Text(place.category.title(isArabic: localization.isArabic))
                            .font(.app(11, .medium, isArabic: localization.isArabic))
                            .foregroundColor(place.color)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(place.color.opacity(0.12))
                            .clipShape(Capsule())

                        // Star Rating
                        HStack(spacing: 3) {
                            Image(systemName: "star.fill")
                                .font(.system(size: 11))
                                .foregroundColor(AppColors.amber)
                            Text(String(format: "%.1f", place.starRating))
                                .font(.system(size: 12, weight: .bold, design: .rounded))
                                .foregroundColor(AppColors.textPrimary)
                            Text("(\(place.reviewCount))")
                                .font(.app(11, .regular, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textTertiary)
                        }
                    }
                }

                Spacer()
            }
        }
        .padding(14)
        .background(AppColors.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
        )
    }

    // MARK: - 3. Quick Real-Time Metrics Strip
    private var quickMetricsStrip: some View {
        HStack(spacing: 0) {
            metricItem(
                titleAr: "الانتظار المتوقع",
                titleEn: "Est. Wait",
                value: "~\(currentBranch.estWaitMinutes) \(localization.isArabic ? "د" : "min")",
                icon: "timer",
                color: AppColors.teal
            )

            Divider()
                .frame(height: 28)

            metricItem(
                titleAr: "أمامك بالدور",
                titleEn: "Ahead of You",
                value: "\(currentBranch.waiting) \(localization.isArabic ? "عملاء" : "ahead")",
                icon: "person.2.fill",
                color: currentBranch.waiting <= 2 ? AppColors.accentGreen : AppColors.amber
            )

            Divider()
                .frame(height: 28)

            metricItem(
                titleAr: "الحالة الآن",
                titleEn: "Status",
                value: currentBranch.isOpen ? (localization.isArabic ? "مفتوح" : "Open") : (localization.isArabic ? "مغلق" : "Closed"),
                icon: currentBranch.isOpen ? "checkmark.circle.fill" : "moon.fill",
                color: currentBranch.isOpen ? AppColors.accentGreen : AppColors.accentRed
            )
        }
        .padding(.vertical, 10)
        .background(AppColors.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
        )
    }

    private func metricItem(titleAr: String, titleEn: String, value: String, icon: String, color: Color) -> some View {
        VStack(spacing: 3) {
            HStack(spacing: 4) {
                Image(systemName: icon)
                    .font(.system(size: 10))
                    .foregroundColor(color)

                Text(localization.isArabic ? titleAr : titleEn)
                    .font(.app(10, .regular, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
            }

            Text(value)
                .font(.app(13, .bold, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textPrimary)
        }
        .frame(maxWidth: .infinity)
    }

    // MARK: - 4. Branch Selector Section with Live Queue Counts
    private var branchSelectorSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Label {
                    Text(localization.isArabic ? "اختر الفرع المطلوب" : "Select Branch")
                        .font(.app(14, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textPrimary)
                } icon: {
                    Image(systemName: "building.2.crop.circle.fill")
                        .foregroundColor(AppColors.indigo)
                }

                Spacer()

                Text("\(selectedBranchIndex + 1) / \(place.branches.count)")
                    .font(.app(11, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background(AppColors.subtleFill)
                    .clipShape(Capsule())
            }

            VStack(spacing: 10) {
                ForEach(Array(place.branches.enumerated()), id: \.offset) { index, branch in
                    let isSelected = selectedBranchIndex == index
                    Button(action: {
                        withAnimation(.spring(response: 0.32, dampingFraction: 0.72)) {
                            selectedBranchIndex = index
                        }
                    }) {
                        HStack(spacing: 12) {
                            // Radio Indicator
                            ZStack {
                                Circle()
                                    .strokeBorder(isSelected ? AppColors.teal : AppColors.cardBorder, lineWidth: 2)
                                    .frame(width: 22, height: 22)

                                if isSelected {
                                    Circle()
                                        .fill(AppColors.teal)
                                        .frame(width: 12, height: 12)
                                }
                            }

                            VStack(alignment: .leading, spacing: 3) {
                                Text(branch.localizedLabel(isArabic: localization.isArabic))
                                    .font(.app(14, isSelected ? .bold : .medium, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textPrimary)

                                Text(branch.localizedAddress(isArabic: localization.isArabic))
                                    .font(.app(12, .regular, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textSecondary)
                                    .lineLimit(1)
                            }

                            Spacer()

                            VStack(alignment: .trailing, spacing: 2) {
                                HStack(spacing: 4) {
                                    Circle()
                                        .fill(branch.waiting <= 2 ? AppColors.accentGreen : AppColors.amber)
                                        .frame(width: 7, height: 7)
                                    Text(localization.isArabic ? "\(branch.waiting) انتظار" : "\(branch.waiting) in line")
                                        .font(.app(11, .bold, isArabic: localization.isArabic))
                                        .foregroundColor(branch.waiting <= 2 ? AppColors.accentGreen : AppColors.amber)
                                }

                                Text(localization.isArabic ? "\(branch.distanceKm) كم" : "\(branch.distanceKm) km")
                                    .font(.app(11, .regular, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textTertiary)
                            }
                        }
                        .padding(13)
                        .background(
                            isSelected
                                ? AppColors.teal.opacity(colorScheme == .dark ? 0.15 : 0.06)
                                : AppColors.cardBackground
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 16, style: .continuous)
                                .strokeBorder(
                                    isSelected ? AppColors.teal : AppColors.cardBorder,
                                    lineWidth: isSelected ? 1.5 : 1
                                )
                        )
                    }
                    .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.9))
                }
            }
        }
        .padding(15)
        .background(AppColors.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
        )
    }

    // MARK: - 5. About Section
    private var aboutSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            Label {
                Text(localization.isArabic ? "عن الخدمة" : "About Service")
                    .font(.app(14, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)
            } icon: {
                Image(systemName: "info.circle.fill")
                    .foregroundColor(AppColors.teal)
            }

            Text(place.localizedAbout(isArabic: localization.isArabic))
                .font(.app(13, .regular, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textSecondary)
                .lineSpacing(4)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(15)
        .background(AppColors.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
        )
    }

    // MARK: - 6. Working Hours Section
    private var workingHoursSection: some View {
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(AppColors.amber.opacity(colorScheme == .dark ? 0.20 : 0.12))
                    .frame(width: 40, height: 40)
                Image(systemName: "clock.badge.checkmark.fill")
                    .foregroundColor(AppColors.amber)
                    .font(.system(size: 18))
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(localization.isArabic ? "مواعيد العمل الرسمية" : "Official Working Hours")
                    .font(.app(11, .medium, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)

                Text(place.localizedWorkingHours(isArabic: localization.isArabic))
                    .font(.app(13, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)
            }

            Spacer()

            Text(currentBranch.isOpen
                 ? (localization.isArabic ? "مفتوح الآن" : "Open Now")
                 : (localization.isArabic ? "مغلق" : "Closed"))
                .font(.app(11, .bold, isArabic: localization.isArabic))
                .foregroundColor(currentBranch.isOpen ? AppColors.accentGreen : AppColors.accentRed)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(
                    (currentBranch.isOpen ? AppColors.accentGreen : AppColors.accentRed)
                        .opacity(colorScheme == .dark ? 0.2 : 0.1)
                )
                .clipShape(Capsule())
        }
        .padding(14)
        .background(AppColors.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
        )
    }

    // MARK: - Floating Bottom Action Bar
    private var bottomActionBar: some View {
        VStack(spacing: 0) {
            HStack(spacing: 16) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(localization.isArabic ? "الانتظار المتوقع" : "Expected Wait")
                        .font(.app(11, .regular, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)

                    HStack(spacing: 4) {
                        Text("~\(currentBranch.estWaitMinutes)")
                            .font(.system(size: 20, weight: .black, design: .rounded))
                            .foregroundColor(AppColors.teal)
                        Text(localization.isArabic ? "دقيقة" : "mins")
                            .font(.app(13, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)
                    }
                }

                Spacer()

                Button(action: handleJoinQueue) {
                    HStack(spacing: 8) {
                        if isJoining {
                            ProgressView()
                                .tint(.white)
                        } else {
                            Image(systemName: "ticket.fill")
                                .font(.system(size: 15, weight: .bold))
                            Text(localization.isArabic ? "انضم إلى هذا الطابور" : "Join This Queue")
                                .font(.app(15, .bold, isArabic: localization.isArabic))
                        }
                    }
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(AppColors.teal)
                    .clipShape(Capsule())
                    .shadow(color: AppColors.teal.opacity(0.35), radius: 10, y: 4)
                }
                .disabled(isJoining)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 14)
            .background(.ultraThinMaterial)
            .overlay(
                Rectangle()
                    .fill(AppColors.cardBorder)
                    .frame(height: 1),
                alignment: .top
            )
        }
    }

    // MARK: - Ticket Success Sheet
    private var ticketSuccessSheet: some View {
        VStack(spacing: 20) {
            HStack {
                Spacer()
                SheetCloseButton {
                    showSuccessModal = false
                    dismiss()
                }
            }
            .padding(.top, 16)
            .padding(.horizontal, 4)

            ZStack {
                Circle()
                    .fill(AppColors.teal.opacity(0.14))
                    .frame(width: 76, height: 76)

                Image(systemName: "checkmark.circle.fill")
                    .font(.system(size: 48))
                    .foregroundColor(AppColors.teal)
                    .symbolEffect(.bounce)
            }

            VStack(spacing: 6) {
                Text(localization.isArabic ? "تم حجز دورك بنجاح! 🎉" : "Ticket Issued Successfully! 🎉")
                    .font(.app(20, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)

                Text(localization.isArabic
                     ? "رقم تذكرتك في \(place.nameAr)"
                     : "Your ticket number at \(place.name)")
                    .font(.app(14, .regular, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
            }

            // Ticket Number Capsule
            VStack(spacing: 6) {
                Text(localization.isArabic ? "رقم التذكرة" : "Ticket Number")
                    .font(.app(12, .medium, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)

                Text("#\(issuedTicketNumber)")
                    .font(.system(size: 46, weight: .black, design: .rounded))
                    .foregroundColor(AppColors.teal)

                Text(localization.isArabic
                     ? "أمامك \(currentBranch.waiting) في الانتظار — جهز نفسك قبل الموعد"
                     : "\(currentBranch.waiting) people ahead — please arrive on time")
                    .font(.app(12, .medium, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.amber)
            }
            .padding(.horizontal, 32)
            .padding(.vertical, 18)
            .frame(maxWidth: .infinity)
            .background(AppColors.cardBackground)
            .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 20, style: .continuous)
                    .strokeBorder(AppColors.teal.opacity(0.3), lineWidth: 1.5)
            )

            // Primary Button to View Ticket in Queue Tab
            Button(action: {
                showSuccessModal = false
                dismiss()
                onQueueJoined()
            }) {
                Text(localization.isArabic ? "عرض تذكرتي في طابوري" : "View My Ticket in Queue")
                    .font(.app(15, .bold, isArabic: localization.isArabic))
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(AppColors.teal)
                    .clipShape(Capsule())
            }
            .padding(.bottom, 16)
        }
        .padding(.horizontal, 24)
        .presentationDetents([.fraction(0.65)])
        .presentationDragIndicator(.visible)
    }

    private func handleJoinQueue() {
        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
        isJoining = true

        Task {
            try? await Task.sleep(nanoseconds: 500_000_000)
            let ticket = dataService.joinQueue(place: place, branch: currentBranch)
            await MainActor.run {
                isJoining = false
                issuedTicketNumber = ticket.ticketNumber
                UINotificationFeedbackGenerator().notificationOccurred(.success)
                showSuccessModal = true
            }
        }
    }

    private func openInAppleMaps() {
        let lat = currentBranch.lat
        let lng = currentBranch.lng
        if let url = URL(string: "http://maps.apple.com/?daddr=\(lat),\(lng)") {
            UIApplication.shared.open(url)
        }
    }
}

// MARK: - Custom Map Pin Annotation View
public struct BranchMapAnnotationPin: View {
    public let place: ServicePlace
    public let branch: ServiceBranch
    public let isSelected: Bool
    public let isArabic: Bool
    public let onTap: () -> Void

    @State private var pulse: Bool = false

    public var body: some View {
        Button(action: {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            onTap()
        }) {
            VStack(spacing: 2) {
                // Floating branch title bubble if selected
                if isSelected {
                    HStack(spacing: 4) {
                        Text(branch.localizedLabel(isArabic: isArabic))
                            .font(.system(size: 11, weight: .bold))
                            .foregroundColor(AppColors.textPrimary)

                        Text("•")
                            .foregroundColor(AppColors.textTertiary)

                        Text(isArabic ? "\(branch.waiting) بالانتظار" : "\(branch.waiting) wait")
                            .font(.system(size: 10, weight: .semibold))
                            .foregroundColor(branch.waiting <= 2 ? AppColors.accentGreen : AppColors.amber)
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(.ultraThinMaterial)
                    .clipShape(Capsule())
                    .shadow(color: Color.black.opacity(0.15), radius: 4, y: 2)
                    .transition(.scale.combined(with: .opacity))
                }

                // Pin Body
                ZStack {
                    // Pulsing beacon ring when selected
                    if isSelected {
                        Circle()
                            .stroke(place.color.opacity(0.35), lineWidth: 2)
                            .frame(width: 44, height: 44)
                            .scaleEffect(pulse ? 1.25 : 0.95)
                            .opacity(pulse ? 0.0 : 0.8)
                            .onAppear {
                                withAnimation(.easeInOut(duration: 1.4).repeatForever(autoreverses: false)) {
                                    pulse = true
                                }
                            }
                    }

                    Circle()
                        .fill(isSelected ? place.color : Color(UIColor.systemBackground))
                        .frame(width: isSelected ? 34 : 26, height: isSelected ? 34 : 26)
                        .shadow(color: place.color.opacity(0.4), radius: isSelected ? 6 : 3, y: 2)

                    Image(systemName: place.iconName)
                        .font(.system(size: isSelected ? 15 : 12, weight: .bold))
                        .foregroundColor(isSelected ? .white : place.color)
                }

                // Pin Point Triangle
                Triangle()
                    .fill(isSelected ? place.color : Color(UIColor.systemBackground))
                    .frame(width: 10, height: 6)
                    .offset(y: -2)
            }
        }
        .buttonStyle(.plain)
        .animation(.spring(response: 0.35, dampingFraction: 0.75), value: isSelected)
    }
}

// MARK: - Helper Shape for Pin Pointer
struct Triangle: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.minX, y: rect.minY))
        path.addLine(to: CGPoint(x: rect.maxX, y: rect.minY))
        path.addLine(to: CGPoint(x: rect.midX, y: rect.maxY))
        path.closeSubpath()
        return path
    }
}
