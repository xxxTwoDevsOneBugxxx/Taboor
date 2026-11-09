import SwiftUI

// MARK: - Native Apple-Grade Smart Queue Alert Preferences View

public struct SmartAlertPreferencesView: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var dataService = QueueDataService.shared
    @ObservedObject private var localization = LocalizationManager.shared
    @ObservedObject private var sensoryManager = TaboorSensoryManager.shared

    @State private var selectedCustomersAhead: Int = 2
    @State private var selectedMinutesBefore: Int = 10
    @State private var smartDeparture: Bool = true
    @State private var timeSensitiveHaptics: Bool = true
    @State private var liveActivityLockScreen: Bool = true

    public init() {}

    private let customersOptions = [2, 3, 5]
    private let minutesOptions = [5, 10, 15, 20]

    public var body: some View {
        ZStack(alignment: .top) {
            AppColors.screenBackground.ignoresSafeArea()

            ScrollView(showsIndicators: false) {
                VStack(spacing: 20) {
                    // Header (Scrolls naturally with content)
                    customNavigationBar

                        // 1. Section: Notice by Customers Ahead
                        sectionContainer(
                            title: localization.isArabic ? "توقيت تنبيه اقتراب الدور (بعدد الأشخاص)" : "Turn Alert by Ahead Customers",
                            subtitle: localization.isArabic ? "اختر متى تفضل أن نرسل لك إشعاراً قبل دخولك" : "Choose when to get notified before your turn"
                        ) {
                            VStack(spacing: 8) {
                                ForEach(customersOptions, id: \.self) { count in
                                    customerOptionRow(count: count)
                                    if count != customersOptions.last {
                                        Divider().overlay(AppColors.cardBorder.opacity(0.6))
                                    }
                                }
                            }
                        }

                        // 2. Section: Notice by Remaining Minutes
                        sectionContainer(
                            title: localization.isArabic ? "تنبيه بالوقت التقديري المتبقي" : "Estimated Time Notice",
                            subtitle: localization.isArabic ? "تنبيه إضافي عند اقتراب موعدك زمنياً" : "Additional notification based on minutes"
                        ) {
                            HStack(spacing: 8) {
                                ForEach(minutesOptions, id: \.self) { mins in
                                    Button(action: {
                                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                        selectedMinutesBefore = mins
                                        savePreferences()
                                    }) {
                                        VStack(spacing: 3) {
                                            Text("\(mins)")
                                                .font(.system(size: 16, weight: .bold, design: .rounded))
                                            Text(localization.isArabic ? "دقائق" : "mins")
                                                .font(.app(10.5, .medium, isArabic: localization.isArabic))
                                        }
                                        .foregroundColor(selectedMinutesBefore == mins ? .white : AppColors.textPrimary)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 10)
                                        .background(
                                            selectedMinutesBefore == mins
                                                ? AnyView(AppColors.teal)
                                                : AnyView(AppColors.softTeal.opacity(0.6))
                                        )
                                        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                                    }
                                    .buttonStyle(ScaleButtonStyle(scale: 0.95, pressedOpacity: 0.85))
                                }
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 14)
                        }

                        // 3. Section: Smart Travel & Departure
                        sectionContainer(
                            title: localization.isArabic ? "حساب المسافة ووقت التحرك الذكي" : "Smart Departure & Travel Time",
                            subtitle: localization.isArabic ? "تنبيهات استباقية بالخروج من المنزل أو المكتب" : "Proactive departure reminders"
                        ) {
                            HStack(spacing: 14) {
                                iconSquircle(icon: "car.fill", color: AppColors.teal, bg: AppColors.softTeal)

                                VStack(alignment: .leading, spacing: 3) {
                                    Text(localization.isArabic ? "تنبيه وقت الانطلاق (Smart Departure)" : "Smart Departure Alert")
                                        .font(.app(14.5, .bold, isArabic: localization.isArabic))
                                        .foregroundColor(AppColors.textPrimary)

                                    Text(localization.isArabic
                                         ? "حساب وقت القيادة وحركة المرور وتنبيهك متى تخرج لتصل قبل دورك بدقيقتين."
                                         : "Calculates traffic and alerts you when to leave your place.")
                                        .font(.app(11.5, .regular, isArabic: localization.isArabic))
                                        .foregroundColor(AppColors.textSecondary)
                                }

                                Spacer()

                                Toggle("", isOn: $smartDeparture)
                                    .tint(AppColors.teal)
                                    .labelsHidden()
                                    .onChange(of: smartDeparture) { _, _ in
                                        savePreferences()
                                    }
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 14)
                        }

                        // 4. Section: Live Activity & Audio Haptics
                        sectionContainer(
                            title: localization.isArabic ? "شاشة القفل والـ Live Activity" : "Lock Screen & Live Activity",
                            subtitle: localization.isArabic ? "متابعة الدور وتنبيهات اللحظة الأخيرة" : "Live updates on Dynamic Island"
                        ) {
                            VStack(spacing: 0) {
                                // Dynamic Island Toggle
                                HStack(spacing: 14) {
                                    iconSquircle(icon: "dot.radiowaves.up.forward", color: AppColors.indigo, bg: AppColors.indigo.opacity(0.12))

                                    VStack(alignment: .leading, spacing: 3) {
                                        Text(localization.isArabic ? "متابعة في الـ Dynamic Island" : "Dynamic Island Tracking")
                                            .font(.app(14.5, .bold, isArabic: localization.isArabic))
                                            .foregroundColor(AppColors.textPrimary)

                                        Text(localization.isArabic ? "إبقاء التذكرة محدثة مباشرة عند الخروج من التطبيق" : "Live queue updates on Lock Screen")
                                            .font(.app(11.5, .regular, isArabic: localization.isArabic))
                                            .foregroundColor(AppColors.textSecondary)
                                    }

                                    Spacer()

                                    Toggle("", isOn: $liveActivityLockScreen)
                                        .tint(AppColors.teal)
                                        .labelsHidden()
                                        .onChange(of: liveActivityLockScreen) { _, _ in
                                            savePreferences()
                                        }
                                }
                                .padding(.horizontal, 16)
                                .padding(.vertical, 14)

                                Divider().overlay(AppColors.cardBorder.opacity(0.6))

                                // Time-sensitive Haptics Toggle
                                HStack(spacing: 14) {
                                    iconSquircle(icon: "bell.and.waves.left.and.right.fill", color: AppColors.amber, bg: AppColors.amber.opacity(0.12))

                                    VStack(alignment: .leading, spacing: 3) {
                                        Text(localization.isArabic ? "اهتزازات مخصصة عند نداء رقمك" : "Critical Turn Haptics")
                                            .font(.app(14.5, .bold, isArabic: localization.isArabic))
                                            .foregroundColor(AppColors.textPrimary)

                                        Text(localization.isArabic ? "نمط اهتزاز ونغمة قوية لضمان عدم تفويت دورك" : "Strong vibration pattern when your turn is called")
                                            .font(.app(11.5, .regular, isArabic: localization.isArabic))
                                            .foregroundColor(AppColors.textSecondary)
                                    }

                                    Spacer()

                                    Toggle("", isOn: $timeSensitiveHaptics)
                                        .tint(AppColors.teal)
                                        .labelsHidden()
                                        .onChange(of: timeSensitiveHaptics) { _, _ in
                                            savePreferences()
                                        }
                                }
                                .padding(.horizontal, 16)
                                .padding(.vertical, 14)
                            }
                        }

                        // 4.5. Section: Signature Chime Selection
                        sectionContainer(
                            title: localization.isArabic ? "نغمة نداء الدور المخصصة" : "Turn Alert Signature Chime",
                            subtitle: localization.isArabic ? "اختر النغمة المفضلة عند نداء رقمك في الطابور" : "Select the chime when your turn is called"
                        ) {
                            VStack(spacing: 0) {
                                ForEach(TaboorChimeOption.allCases) { chime in
                                    chimeOptionRow(chime: chime)
                                    if chime != TaboorChimeOption.allCases.last {
                                        Divider().overlay(AppColors.cardBorder.opacity(0.6))
                                    }
                                }
                            }
                        }
                    }
                    .padding(.top, 14)
                    .padding(.bottom, 40)
                }

            // Pinned Floating Glass Close Button (Stays accessible while scrolling)
            floatingCloseButton
        }
        .onAppear {
            self.selectedCustomersAhead = dataService.customersAheadNotice
            self.selectedMinutesBefore = dataService.minutesBeforeNotice
            self.smartDeparture = dataService.smartDepartureNotice
            self.timeSensitiveHaptics = dataService.timeSensitiveHaptics
            self.liveActivityLockScreen = dataService.liveActivityLockScreen
        }
        .environment(\.layoutDirection, localization.layoutDirection)
    }

    // MARK: - Header (Scrolls naturally with content)
    private var customNavigationBar: some View {
        HStack(alignment: .center, spacing: 14) {
            Color.clear
                .frame(width: 38, height: 38)

            VStack(alignment: .leading, spacing: 2) {
                Text(localization.isArabic ? "تخصيص تنبيهات الدور" : "Smart Alert Preferences")
                    .font(.app(18.5, .extraBold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.ink)

                Text(localization.isArabic ? "التحكم في وقت وطريقة إشعارك" : "Control when and how you are notified")
                    .font(.app(11.5, .medium, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
            }

            Spacer()

            ZStack {
                Circle()
                    .fill(AppColors.softTeal)
                    .frame(width: 38, height: 38)
                Image(systemName: "slider.horizontal.3")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(AppColors.teal)
            }
        }
        .padding(.horizontal, 20)
        .padding(.top, 24)
        .padding(.bottom, 6)
    }

    // MARK: - Floating Glass Close Button (Pinned at Top)
    private var floatingCloseButton: some View {
        HStack {
            Button(action: {
                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                dismiss()
            }) {
                Image(systemName: "xmark")
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(AppColors.deepTeal)
                    .frame(width: 24, height: 24)
            }
            .buttonStyle(.glass)
            .buttonBorderShape(.circle)

            Spacer()
        }
        .padding(.horizontal, 20)
        .padding(.top, 24)
    }

    // MARK: - Customer Option Row
    private func customerOptionRow(count: Int) -> some View {
        Button(action: {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            selectedCustomersAhead = count
            savePreferences()
        }) {
            HStack(spacing: 12) {
                ZStack {
                    Circle()
                        .strokeBorder(selectedCustomersAhead == count ? AppColors.teal : AppColors.cardBorder, lineWidth: 2)
                        .frame(width: 22, height: 22)

                    if selectedCustomersAhead == count {
                        Circle()
                            .fill(AppColors.teal)
                            .frame(width: 12, height: 12)
                    }
                }

                VStack(alignment: .leading, spacing: 2) {
                    HStack(spacing: 6) {
                        Text(localization.isArabic ? "قبل دوري بـ \(count) عملاء" : "\(count) customers ahead")
                            .font(.app(14, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)

                        if count == 2 {
                            Text(localization.isArabic ? "موصى به" : "Recommended")
                                .font(.app(10, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.teal)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(AppColors.softTeal)
                                .clipShape(Capsule())
                        }
                    }

                    Text(count == 2
                         ? (localization.isArabic ? "الوقت المثالي للاستعداد والدخول مباشرة" : "Ideal time to be ready at the counter")
                         : (localization.isArabic ? "يعطيك متسعاً أكبر للوصول للمكان" : "Provides more buffer to arrive"))
                        .font(.app(11, .regular, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)
                }

                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
        }
        .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.88))
    }

    // MARK: - Chime Option Row
    private func chimeOptionRow(chime: TaboorChimeOption) -> some View {
        let isSelected = sensoryManager.selectedChime == chime

        return Button(action: {
            sensoryManager.selectedChime = chime
            sensoryManager.previewChime(chime)
        }) {
            HStack(spacing: 12) {
                // Play / Preview Sound Button
                ZStack {
                    Circle()
                        .fill(isSelected ? AppColors.teal : AppColors.softTeal)
                        .frame(width: 36, height: 36)

                    Image(systemName: isSelected ? "speaker.wave.2.fill" : chime.icon)
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(isSelected ? .white : AppColors.teal)
                }

                VStack(alignment: .leading, spacing: 2) {
                    HStack(spacing: 6) {
                        Text(localization.isArabic ? chime.titleAr : chime.titleEn)
                            .font(.app(14, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)

                        if chime == .softMarimba {
                            Text(localization.isArabic ? "افتراضي" : "Default")
                                .font(.app(9.5, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.teal)
                                .padding(.horizontal, 6)
                                .padding(.vertical, 2)
                                .background(AppColors.softTeal)
                                .clipShape(Capsule())
                        }
                    }

                    Text(localization.isArabic ? chime.subtitleAr : chime.subtitleEn)
                        .font(.app(11, .regular, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)
                }

                Spacer()

                // Radio Button Indicator
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
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .background(isSelected ? AppColors.softTeal.opacity(0.35) : Color.clear)
        }
        .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.88))
    }

    // MARK: - Section Container Helper
    private func sectionContainer<Content: View>(title: String, subtitle: String? = nil, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.app(13.5, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)

                if let sub = subtitle {
                    Text(sub)
                        .font(.app(11, .regular, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)
                }
            }
            .padding(.horizontal, 24)

            VStack(spacing: 0) {
                content()
            }
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 18, style: .continuous)
                    .strokeBorder(AppColors.cardBorder, lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.02), radius: 6, y: 2)
            .padding(.horizontal, 20)
        }
    }

    private func iconSquircle(icon: String, color: Color, bg: Color) -> some View {
        ZStack {
            RoundedRectangle(cornerRadius: 10, style: .continuous)
                .fill(bg)
                .frame(width: 36, height: 36)

            Image(systemName: icon)
                .font(.system(size: 15, weight: .bold))
                .foregroundColor(color)
        }
    }

    private func savePreferences() {
        dataService.updateAlertPreferences(
            customersAhead: selectedCustomersAhead,
            minutesBefore: selectedMinutesBefore,
            smartDeparture: smartDeparture,
            timeSensitive: timeSensitiveHaptics,
            liveActivity: liveActivityLockScreen
        )
    }
}
