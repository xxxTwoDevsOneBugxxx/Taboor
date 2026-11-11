import SwiftUI
import UIKit

// MARK: - Redesigned Native iOS Settings & Profile View (Taboor Palette & Apple HIG)

public struct ProfileTabView: View {
    @Binding var selectedTab: TaboorTab
    public let onLogout: () -> Void

    @ObservedObject private var localization = LocalizationManager.shared
    @StateObject private var dataService = QueueDataService.shared
    @Environment(\.colorScheme) private var colorScheme

    @State private var user: User? = AuthRepository.shared.getSavedUser()
    @State private var faceIdEnabled = true
    @State private var notificationsEnabled = true
    @State private var showLogoutConfirm = false
    @State private var showHelpSheet = false
    @State private var showPrivacySheet = false
    @State private var showBusinessPartnerSheet = false
    @State private var navigateToEditProfile = false
    @State private var navigateToChangePass = false
    @State private var navigateToHistory = false
    @State private var navigateToFavorites = false
    @State private var showSmartAlertPreferencesSheet = false

    public init(selectedTab: Binding<TaboorTab> = .constant(.profile), onLogout: @escaping () -> Void) {
        self._selectedTab = selectedTab
        self.onLogout = onLogout
    }

    public var body: some View {
        NavigationStack {
            ZStack(alignment: .bottom) {
                ScrollView(showsIndicators: false) {
                    VStack(spacing: 20) {
                        // 1. Top Custom Header Bar
                        topHeaderBar

                        // 2. Profile User Overview Card
                        userProfileCard

                        // 2.5 Section: Activity & Places (سجل الزيارات والمفضلة)
                        sectionContainer(title: localization.isArabic ? "نشاطك وطوابيرك" : "Activity & Places") {
                            Button(action: {
                                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                dataService.isTabBarHidden = true
                                navigateToHistory = true
                            }) {
                                settingsRow(
                                    title: localization.isArabic ? "سجل الزيارات والتذاكر السابقة" : "Ticket History & Past Visits",
                                    subtitle: localization.isArabic ? "تاريخ طوابيرك والوقت الذي وفرته" : "Past queues & saved waiting time",
                                    icon: "clock.arrow.circlepath",
                                    iconColor: AppColors.teal,
                                    iconBg: AppColors.softTeal,
                                    trailingBadge: "\(dataService.history.count)"
                                )
                            }

                            rowDivider

                            Button(action: {
                                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                dataService.isTabBarHidden = true
                                navigateToFavorites = true
                            }) {
                                settingsRow(
                                    title: localization.isArabic ? "الأماكن والعيادات المفضلة" : "Favorite Places & Clinics",
                                    subtitle: localization.isArabic ? "وصول فوري وحجز سريع لأماكنك المعتادة" : "Quick access & queue booking",
                                    icon: "heart.fill",
                                    iconColor: .pink,
                                    iconBg: Color.pink.opacity(0.12),
                                    trailingBadge: "\(dataService.favoritePlaces.count)"
                                )
                            }
                        }

                        // 3. Section: Account Settings
                        sectionContainer(title: localization.isArabic ? "إعدادات الحساب" : "Account Settings") {
                            Button(action: {
                                dataService.isTabBarHidden = true
                                navigateToEditProfile = true
                            }) {
                                settingsRow(
                                    title: localization.isArabic ? "تعديل البيانات الشخصية" : "Edit Personal Details",
                                    subtitle: localization.isArabic ? "الاسم، الهاتف، والبريد" : "Name, phone, and email",
                                    icon: "person.crop.circle",
                                    iconColor: AppColors.teal,
                                    iconBg: AppColors.softTeal
                                )
                            }

                            rowDivider

                            Button(action: {
                                dataService.isTabBarHidden = true
                                navigateToChangePass = true
                            }) {
                                settingsRow(
                                    title: localization.isArabic ? "تغيير كلمة المرور" : "Change Password",
                                    subtitle: localization.isArabic ? "تأمين وحماية الحساب" : "Account security & password",
                                    icon: "lock.shield",
                                    iconColor: AppColors.amber,
                                    iconBg: AppColors.amber.opacity(0.12)
                                )
                            }
                        }

                    // 4. Section: Preferences & Security
                    sectionContainer(title: localization.isArabic ? "التفضيلات والأمان" : "Preferences & Security") {
                        // Smart Alert Preferences Row (Modal Sheet)
                        Button(action: {
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                            showSmartAlertPreferencesSheet = true
                        }) {
                            settingsRow(
                                title: localization.isArabic ? "تخصيص تنبيهات الدور الذكية" : "Smart Queue Alert Preferences",
                                subtitle: localization.isArabic ? "توقيت الإشعار، المسافة، ووقت التحرك" : "Notice timing, distance & departure alerts",
                                icon: "slider.horizontal.3",
                                iconColor: AppColors.teal,
                                iconBg: AppColors.softTeal,
                                trailingBadge: localization.isArabic ? "قبل دورك بـ \(dataService.customersAheadNotice)" : "\(dataService.customersAheadNotice) ahead"
                            )
                        }
                        .sheet(isPresented: $showSmartAlertPreferencesSheet) {
                            SmartAlertPreferencesView()
                                .presentationDragIndicator(.visible)
                        }

                        rowDivider

                        // Face ID Toggle
                        HStack(spacing: 14) {
                            iconSquircle(icon: "faceid", color: AppColors.teal, bg: AppColors.softTeal)

                            VStack(alignment: .leading, spacing: 2) {
                                Text(localization.isArabic ? "تسجيل الدخول بـ Face ID" : "Face ID Login")
                                    .font(.app(14.5, .bold, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textPrimary)

                                Text(localization.isArabic ? "دخول سريع وآمن دون كلمة المرور" : "Quick & secure biometric login")
                                    .font(.app(11.5, .regular, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textSecondary)
                            }

                            Spacer()

                            Toggle("", isOn: $faceIdEnabled)
                                .tint(AppColors.teal)
                                .labelsHidden()
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)

                        rowDivider

                        // Notifications Toggle
                        HStack(spacing: 14) {
                            iconSquircle(icon: "bell.badge", color: AppColors.amber, bg: AppColors.amber.opacity(0.12))

                            VStack(alignment: .leading, spacing: 2) {
                                Text(localization.isArabic ? "الإشعارات المباشرة" : "Push Notifications")
                                    .font(.app(14.5, .bold, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textPrimary)

                                Text(localization.isArabic ? "تنبيهات فورية عند اقتراب دورك" : "Instant queue and turn alerts")
                                    .font(.app(11.5, .regular, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textSecondary)
                            }

                            Spacer()

                            Toggle("", isOn: $notificationsEnabled)
                                .tint(AppColors.teal)
                                .labelsHidden()
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)

                        rowDivider

                        // Language Switcher Row (Menu anchored strictly to the Pill Badge)
                        HStack(spacing: 14) {
                            iconSquircle(icon: "globe", color: AppColors.indigo, bg: AppColors.indigo.opacity(0.12))

                            VStack(alignment: .leading, spacing: 2) {
                                Text(localization.isArabic ? "لغة التطبيق" : "App Language")
                                    .font(.app(14.5, .bold, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textPrimary)

                                Text(localization.isArabic ? "تغيير لغة العرض والواجهة" : "Switch interface language")
                                    .font(.app(11.5, .regular, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textSecondary)
                            }

                            Spacer()

                            // Native iOS Menu attached only to the capsule badge
                            Menu {
                                Button(action: {
                                    guard localization.currentLanguage != .arabic else { return }
                                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                                    withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                                        localization.currentLanguage = .arabic
                                    }
                                }) {
                                    HStack {
                                        Text("العربية (Arabic)")
                                        if localization.isArabic {
                                            Image(systemName: "checkmark")
                                        }
                                    }
                                }

                                Button(action: {
                                    guard localization.currentLanguage != .english else { return }
                                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                                    withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                                        localization.currentLanguage = .english
                                    }
                                }) {
                                    HStack {
                                        Text("English (الإنجليزية)")
                                        if !localization.isArabic {
                                            Image(systemName: "checkmark")
                                        }
                                    }
                                }
                            } label: {
                                HStack(spacing: 6) {
                                    Text(localization.isArabic ? "العربية" : "English")
                                        .font(.app(12.5, .bold, isArabic: localization.isArabic))
                                        .foregroundColor(AppColors.teal)

                                    Image(systemName: "chevron.up.chevron.down")
                                        .font(.system(size: 10, weight: .bold))
                                        .foregroundColor(AppColors.teal)
                                }
                                .padding(.horizontal, 11)
                                .padding(.vertical, 6)
                                .background(AppColors.softTeal)
                                .clipShape(Capsule())
                                .overlay(
                                    Capsule().strokeBorder(AppColors.teal.opacity(0.25), lineWidth: 0.8)
                                )
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)
                    }

                    // 5. Section: Support & About
                    sectionContainer(title: localization.isArabic ? "المساعدة والمعلومات" : "Support & About") {
                        Button(action: {
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                            showHelpSheet = true
                        }) {
                            settingsRow(
                                title: localization.isArabic ? "مركز المساعدة والأسئلة الشائعة" : "Help Center & FAQ",
                                subtitle: localization.isArabic ? "إجابات على أكثر الأسئلة شيوعاً" : "Common questions & support guide",
                                icon: "questionmark.circle",
                                iconColor: AppColors.teal,
                                iconBg: AppColors.softTeal
                            )
                        }

                        rowDivider

                        Button(action: {
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                            showPrivacySheet = true
                        }) {
                            settingsRow(
                                title: localization.isArabic ? "الشروط وسياسة الخصوصية" : "Terms & Privacy Policy",
                                subtitle: localization.isArabic ? "كيف نحمي بياناتك وخصوصيتك" : "Data protection & legal terms",
                                icon: "doc.text",
                                iconColor: AppColors.teal,
                                iconBg: AppColors.softTeal
                            )
                        }

                        rowDivider

                        Button(action: {
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                            if let url = URL(string: "https://taboor.app") {
                                let activityVC = UIActivityViewController(activityItems: [url], applicationActivities: nil)
                                if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                                   let rootVC = windowScene.windows.first?.rootViewController {
                                    rootVC.present(activityVC, animated: true)
                                }
                            }
                        }) {
                            settingsRow(
                                title: localization.isArabic ? "مشاركة تطبيق طابور" : "Share Taboor App",
                                subtitle: localization.isArabic ? "ادعُ أصدقاءك لتوفير وقت الانتظار" : "Invite friends & skip waiting lines",
                                icon: "square.and.arrow.up",
                                iconColor: AppColors.amber,
                                iconBg: AppColors.amber.opacity(0.12)
                            )
                        }
                    }

                    // 5.5 Business Partner / Clinic SaaS Onboarding Card (Placed near bottom, Apple HIG & Uber/Airbnb style)
                    businessPartnerPromoCard

                    // 6. Section: Sign Out Card
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                        showLogoutConfirm = true
                    }) {
                        HStack(spacing: 12) {
                            ZStack {
                                RoundedRectangle(cornerRadius: 11, style: .continuous)
                                    .fill(AppColors.accentRed.opacity(0.12))
                                    .frame(width: 38, height: 38)

                                Image(systemName: "rectangle.portrait.and.arrow.right")
                                    .font(.system(size: 16, weight: .bold))
                                    .foregroundColor(AppColors.accentRed)
                            }

                            Text(localization.isArabic ? "تسجيل الخروج من الحساب" : "Sign Out of Account")
                                .font(.app(14.5, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.accentRed)

                            Spacer()

                            Image(systemName: localization.isArabic ? "chevron.left" : "chevron.right")
                                .font(.system(size: 11, weight: .bold))
                                .foregroundColor(AppColors.accentRed.opacity(0.6))
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 14)
                        .background(Color.white)
                        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 18, style: .continuous)
                                .strokeBorder(AppColors.accentRed.opacity(0.22), lineWidth: 1)
                        )
                        .shadow(color: Color.black.opacity(0.02), radius: 6, y: 2)
                    }
                    .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.88))

                    // 7. Version Footer
                    versionFooter

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
        .navigationDestination(isPresented: $navigateToEditProfile) {
            EditProfileView(user: user, onDismiss: {
                dataService.isTabBarHidden = false
                navigateToEditProfile = false
            }, onSaved: { updatedUser in
                self.user = updatedUser
                dataService.isTabBarHidden = false
                navigateToEditProfile = false
            })
            .onDisappear {
                dataService.isTabBarHidden = false
            }
        }
        .navigationDestination(isPresented: $navigateToChangePass) {
            ChangePasswordView(onDismiss: {
                dataService.isTabBarHidden = false
                navigateToChangePass = false
            })
            .onDisappear {
                dataService.isTabBarHidden = false
            }
        }
        .navigationDestination(isPresented: $navigateToHistory) {
            TicketHistoryView(onDismiss: {
                dataService.isTabBarHidden = false
                navigateToHistory = false
            })
            .onDisappear {
                dataService.isTabBarHidden = false
                navigateToHistory = false
            }
        }
        .navigationDestination(isPresented: $navigateToFavorites) {
            FavoritePlacesView(onDismiss: {
                dataService.isTabBarHidden = false
                navigateToFavorites = false
            })
            .onDisappear {
                dataService.isTabBarHidden = false
                navigateToFavorites = false
            }
        }
        .onChange(of: dataService.showHistoryScreen) { _, show in
            if show {
                dataService.isTabBarHidden = true
                navigateToHistory = true
                dataService.showHistoryScreen = false
            }
        }
        .onChange(of: dataService.showFavoritesScreen) { _, show in
            if show {
                dataService.isTabBarHidden = true
                navigateToFavorites = true
                dataService.showFavoritesScreen = false
            }
        }
        .onChange(of: dataService.showAlertPreferencesScreen) { _, show in
            if show {
                showSmartAlertPreferencesSheet = true
                dataService.showAlertPreferencesScreen = false
            }
        }
        .onAppear {
            user = AuthRepository.shared.getSavedUser()
            if dataService.showAlertPreferencesScreen {
                showSmartAlertPreferencesSheet = true
                dataService.showAlertPreferencesScreen = false
            }
        }
            .alert(localization.isArabic ? "تسجيل الخروج" : "Sign Out", isPresented: $showLogoutConfirm) {
                Button(localization.isArabic ? "إلغاء" : "Cancel", role: .cancel) {}
                Button(localization.isArabic ? "خروج" : "Sign Out", role: .destructive) {
                    onLogout()
                }
            } message: {
                Text(localization.isArabic
                     ? "هل أنت متأكد من رغبتك في تسجيل الخروج من حسابك؟"
                     : "Are you sure you want to sign out of your account?")
            }
            .sheet(isPresented: $showHelpSheet) {
                helpCenterSheet
            }
            .sheet(isPresented: $showPrivacySheet) {
                privacyTermsSheet
            }
            .sheet(isPresented: $showBusinessPartnerSheet) {
                BusinessPartnerSheetView()
            }
        }
        .background(AppColors.screenBackground.ignoresSafeArea())
    }

    // MARK: - 1. Top Header Bar
    private var topHeaderBar: some View {
        HStack(alignment: .center, spacing: 12) {
            // Squircle Brand Icon
            ZStack {
                RoundedRectangle(cornerRadius: 13, style: .continuous)
                    .fill(
                        LinearGradient(
                            colors: [AppColors.ink, AppColors.deepTeal],
                            startPoint: .topLeading,
                            endPoint: .bottomTrailing
                        )
                    )
                    .frame(width: 42, height: 42)
                    .overlay(
                        RoundedRectangle(cornerRadius: 13, style: .continuous)
                            .strokeBorder(Color.white.opacity(0.18), lineWidth: 1)
                    )
                    .shadow(color: AppColors.ink.opacity(0.2), radius: 6, x: 0, y: 2)

                Image(systemName: "person.crop.circle.fill")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundColor(.white)
            }

            // Title & Subtitle
            VStack(alignment: .leading, spacing: 2) {
                Text(localization.isArabic ? "الإعدادات والحساب" : "Settings & Profile")
                    .font(.app(22, .extraBold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)

                Text(localization.isArabic ? "إدارة بياناتك، التفضيلات والأمان" : "Manage account, preferences & security")
                    .font(.app(12, .medium, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
                    .lineLimit(1)
            }

            Spacer()
        }
    }

    // MARK: - 2. User Overview Card
    private var userProfileCard: some View {
        Button(action: {
            navigateToEditProfile = true
        }) {
            HStack(spacing: 14) {
                // Avatar Squircle
                ZStack {
                    RoundedRectangle(cornerRadius: 16, style: .continuous)
                        .fill(
                            LinearGradient(
                                colors: [AppColors.teal, AppColors.deepTeal],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 58, height: 58)
                        .shadow(color: AppColors.teal.opacity(0.25), radius: 6, y: 2)

                    Image(systemName: "person.fill")
                        .font(.system(size: 26, weight: .semibold))
                        .foregroundColor(.white)
                }

                // Info
                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 6) {
                        Text(user?.fullName.isEmpty == false ? user!.fullName : (localization.isArabic ? "محمد أحمد" : "Mohamed Ahmed"))
                            .font(.app(16.5, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)
                            .lineLimit(1)

                        Image(systemName: "checkmark.seal.fill")
                            .font(.system(size: 14))
                            .foregroundColor(AppColors.teal)
                    }

                    Text(user?.email.isEmpty == false ? user!.email : "mohamed@taboor.com")
                        .font(.app(12.5, .medium, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)
                        .lineLimit(1)

                    HStack(spacing: 6) {
                        Image(systemName: "phone.fill")
                            .font(.system(size: 11))
                            .foregroundColor(AppColors.teal)

                        Text(formatPhoneNumber(user?.phoneNumber ?? "+20 101 234 5678"))
                            .font(.inter(12.5, .medium))
                            .foregroundColor(AppColors.textSecondary)
                            .environment(\.layoutDirection, .leftToRight)
                    }
                    .padding(.top, 2)
                }

                Spacer()

                // Edit Action Pill
                HStack(spacing: 4) {
                    Text(localization.isArabic ? "تعديل" : "Edit")
                        .font(.app(12, .bold, isArabic: localization.isArabic))

                    Image(systemName: localization.isArabic ? "chevron.left" : "chevron.right")
                        .font(.system(size: 10, weight: .bold))
                }
                .foregroundColor(AppColors.teal)
                .padding(.horizontal, 10)
                .padding(.vertical, 5)
                .background(AppColors.softTeal)
                .clipShape(Capsule())
            }
            .padding(16)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 22, style: .continuous)
                    .strokeBorder(AppColors.cardBorder, lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.03), radius: 8, y: 3)
        }
        .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.92))
    }

    // MARK: - 2.5 Business Partner / Clinic SaaS Onboarding Card
    private var businessPartnerPromoCard: some View {
        Button(action: {
            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
            showBusinessPartnerSheet = true
        }) {
            HStack(spacing: 14) {
                // Gradient Icon Squircle
                ZStack {
                    RoundedRectangle(cornerRadius: 15, style: .continuous)
                        .fill(
                            LinearGradient(
                                colors: [AppColors.ink, AppColors.deepTeal],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 48, height: 48)

                    Image(systemName: "building.2.crop.circle.fill")
                        .font(.system(size: 22, weight: .semibold))
                        .foregroundColor(AppColors.amber)
                }

                VStack(alignment: .leading, spacing: 3) {
                    HStack(spacing: 6) {
                        Text(localization.isArabic ? "طابور للأعمال والعيادات" : "Taboor for Business")
                            .font(.app(14.5, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)

                        Text(localization.isArabic ? "جديد" : "NEW")
                            .font(.app(9, .extraBold, isArabic: localization.isArabic))
                            .foregroundColor(.white)
                            .padding(.horizontal, 6)
                            .padding(.vertical, 2)
                            .background(AppColors.amber)
                            .clipShape(Capsule())
                    }

                    Text(localization.isArabic
                         ? "صاحب عيادة أو نشاط تجاري؟ انضم لطابور ونظّم طوابيرك الآن"
                         : "Own a clinic or salon? Streamline queues & reach clients")
                        .font(.app(11.5, .regular, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)
                        .lineLimit(2)
                }

                Spacer(minLength: 4)

                Image(systemName: localization.isArabic ? "chevron.left" : "chevron.right")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundColor(AppColors.teal)
            }
            .padding(14)
            .background(
                RoundedRectangle(cornerRadius: 20, style: .continuous)
                    .fill(Color.white)
                    .overlay(
                        RoundedRectangle(cornerRadius: 20, style: .continuous)
                            .strokeBorder(
                                LinearGradient(
                                    colors: [AppColors.teal.opacity(0.4), AppColors.amber.opacity(0.35)],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                ),
                                lineWidth: 1.2
                            )
                    )
            )
            .shadow(color: AppColors.ink.opacity(0.05), radius: 8, y: 3)
        }
        .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.92))
    }

    private func formatPhoneNumber(_ raw: String) -> String {
        let cleaned = raw.replacingOccurrences(of: " ", with: "")
            .replacingOccurrences(of: "-", with: "")
        if cleaned.hasPrefix("+20") && cleaned.count >= 12 {
            let rest = String(cleaned.dropFirst(3))
            let p1 = rest.prefix(3)
            let p2 = rest.dropFirst(3).prefix(3)
            let p3 = rest.dropFirst(6)
            return "+20 \(p1) \(p2) \(p3)"
        } else if cleaned.hasPrefix("01") && cleaned.count == 11 {
            let p1 = cleaned.prefix(4)
            let p2 = cleaned.dropFirst(4).prefix(3)
            let p3 = cleaned.dropFirst(7)
            return "\(p1) \(p2) \(p3)"
        }
        return raw.isEmpty ? "+20 101 234 5678" : raw
    }

    // MARK: - Section Box Container
    private func sectionContainer<Content: View>(title: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.app(14, .bold, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textPrimary)
                .padding(.horizontal, 6)

            VStack(spacing: 0) {
                content()
            }
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 20, style: .continuous)
                    .strokeBorder(AppColors.cardBorder, lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.02), radius: 6, y: 2)
        }
    }

    // MARK: - Reusable Row Components
    private func settingsRow(
        title: String,
        subtitle: String,
        icon: String,
        iconColor: Color,
        iconBg: Color,
        trailingBadge: String? = nil
    ) -> some View {
        HStack(spacing: 14) {
            iconSquircle(icon: icon, color: iconColor, bg: iconBg)

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.app(14.5, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)

                Text(subtitle)
                    .font(.app(11.5, .regular, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
                    .lineLimit(1)
            }

            Spacer()

            if let badge = trailingBadge {
                Text(badge)
                    .font(.app(11.5, .bold, isArabic: localization.isArabic))
                    .foregroundColor(iconColor)
                    .offset(y: 1)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3.5)
                    .background(iconBg)
                    .clipShape(Capsule())
            }

            Image(systemName: localization.isArabic ? "chevron.left" : "chevron.right")
                .font(.system(size: 11, weight: .bold))
                .foregroundColor(AppColors.textTertiary)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
    }

    private func iconSquircle(icon: String, color: Color, bg: Color) -> some View {
        ZStack {
            RoundedRectangle(cornerRadius: 11, style: .continuous)
                .fill(bg)
                .frame(width: 38, height: 38)

            Image(systemName: icon)
                .font(.system(size: 16, weight: .semibold))
                .foregroundColor(color)
        }
    }

    private var rowDivider: some View {
        Rectangle()
            .fill(AppColors.cardBorder.opacity(0.8))
            .frame(height: 1)
            .padding(.horizontal, 16)
    }

    private var versionFooter: some View {
        VStack(spacing: 4) {
            HStack(spacing: 5) {
                Text("taboor.")
                    .font(.system(size: 14, weight: .black, design: .rounded))
                    .foregroundColor(AppColors.ink)

                Text("•")
                    .foregroundColor(AppColors.textTertiary)

                Text(localization.isArabic ? "الإصدار 1.0.0" : "v1.0.0 (Build 26)")
                    .font(.app(11.5, .medium, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textTertiary)
            }

            Text(localization.isArabic ? "المنصة الذكية لإدارة الطوابير والمواعيد الفورية" : "Smart Queue & Appointment Management")
                .font(.app(11, .regular, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textTertiary)
        }
        .frame(maxWidth: .infinity, alignment: .center)
        .padding(.top, 6)
    }

    // MARK: - Submodals
    private var helpCenterSheet: some View {
        HelpCenterModalView()
    }

    private var privacyTermsSheet: some View {
        PrivacyTermsModalView()
    }
}

// MARK: - Native Pushed View for Edit Profile

struct EditProfileView: View {
    let user: User?
    let onDismiss: () -> Void
    let onSaved: (User) -> Void

    @ObservedObject private var localization = LocalizationManager.shared
    @Environment(\.dismiss) private var dismiss

    @State private var fullName: String = ""
    @State private var phoneNumber: String = ""
    @State private var email: String = ""
    @State private var isSaving = false

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 20) {
                // Profile Avatar Photo
                VStack(spacing: 10) {
                    ZStack {
                        RoundedRectangle(cornerRadius: 22, style: .continuous)
                            .fill(
                                LinearGradient(
                                    colors: [AppColors.teal, AppColors.deepTeal],
                                    startPoint: .topLeading,
                                    endPoint: .bottomTrailing
                                )
                            )
                            .frame(width: 82, height: 82)
                            .shadow(color: AppColors.teal.opacity(0.25), radius: 8, y: 3)

                        Image(systemName: "person.fill")
                            .font(.system(size: 40, weight: .semibold))
                            .foregroundColor(.white)
                    }

                    Text(localization.isArabic ? "تعديل صورة الحساب" : "Change Avatar")
                        .font(.app(12.5, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.teal)
                }
                .padding(.top, 14)

                // Input Card
                VStack(spacing: 16) {
                    // Full Name
                    VStack(alignment: .leading, spacing: 7) {
                        Text(localization.isArabic ? "الاسم بالكامل" : "Full Name")
                            .font(.app(13, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)

                        HStack(spacing: 10) {
                            Image(systemName: "person.fill")
                                .font(.system(size: 15))
                                .foregroundColor(AppColors.teal)
                                .frame(width: 20)

                            TextField(localization.isArabic ? "أدخل اسمك" : "Enter full name", text: $fullName)
                                .font(.app(14.5, .medium, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textPrimary)
                        }
                        .padding(.horizontal, 14)
                        .padding(.vertical, 13)
                        .background(Color(hex: 0xFBF9F5))
                        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 14, style: .continuous)
                                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
                        )
                    }

                    // Phone Number
                    VStack(alignment: .leading, spacing: 7) {
                        Text(localization.isArabic ? "رقم الهاتف" : "Phone Number")
                            .font(.app(13, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)

                        HStack(spacing: 10) {
                            Image(systemName: "phone.fill")
                                .font(.system(size: 15))
                                .foregroundColor(AppColors.teal)
                                .frame(width: 20)

                            TextField(localization.isArabic ? "010 1234 5678" : "Phone", text: $phoneNumber)
                                .font(.inter(14.5, .medium))
                                .foregroundColor(AppColors.textPrimary)
                                .keyboardType(.phonePad)
                                .environment(\.layoutDirection, .leftToRight)
                        }
                        .padding(.horizontal, 14)
                        .padding(.vertical, 13)
                        .background(Color(hex: 0xFBF9F5))
                        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 14, style: .continuous)
                                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
                        )
                    }

                    // Email (Read-only)
                    VStack(alignment: .leading, spacing: 7) {
                        HStack {
                            Text(localization.isArabic ? "البريد الإلكتروني" : "Email Address")
                                .font(.app(13, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textPrimary)

                            Spacer()

                            Text(localization.isArabic ? "للقراءة فقط" : "Read-only")
                                .font(.app(11, .medium, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textTertiary)
                        }

                        HStack(spacing: 10) {
                            Image(systemName: "envelope.fill")
                                .font(.system(size: 14))
                                .foregroundColor(AppColors.textTertiary)
                                .frame(width: 20)

                            Text(email.isEmpty ? "mohamed@taboor.com" : email)
                                .font(.inter(14, .regular))
                                .foregroundColor(AppColors.textSecondary)

                            Spacer()

                            Image(systemName: "lock.fill")
                                .font(.system(size: 12))
                                .foregroundColor(AppColors.textTertiary)
                        }
                        .padding(.horizontal, 14)
                        .padding(.vertical, 13)
                        .background(AppColors.subtleFill.opacity(0.45))
                        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 14, style: .continuous)
                                .strokeBorder(AppColors.cardBorder.opacity(0.7), lineWidth: 1)
                        )
                    }
                }
                .padding(18)
                .background(Color.white)
                .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 22, style: .continuous)
                        .strokeBorder(AppColors.cardBorder, lineWidth: 1)
                )
                .shadow(color: Color.black.opacity(0.02), radius: 8, y: 2)

                // Save Changes CTA Button
                Button(action: saveProfile) {
                    HStack {
                        Spacer()
                        if isSaving {
                            ProgressView().tint(.white)
                        } else {
                            Text(localization.isArabic ? "حفظ التغييرات" : "Save Changes")
                                .font(.app(15, .bold, isArabic: localization.isArabic))
                                .foregroundColor(.white)
                        }
                        Spacer()
                    }
                    .padding(.vertical, 14)
                    .background(
                        LinearGradient(
                            colors: [AppColors.ink, AppColors.deepTeal],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                    .shadow(color: AppColors.ink.opacity(0.2), radius: 6, y: 2)
                }
                .disabled(isSaving || fullName.trimmingCharacters(in: .whitespaces).isEmpty)
                .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.90))

                Spacer().frame(height: 40)
            }
            .padding(.horizontal, 20)
            .padding(.top, 14)
        }
        .background(AppColors.screenBackground.ignoresSafeArea())
        .navigationBarBackButtonHidden(true)
        .toolbar(.visible, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    dismiss()
                    onDismiss()
                }) {
                    Image(systemName: localization.isArabic ? "chevron.right" : "chevron.left")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(AppColors.textPrimary)
                }
            }

            ToolbarItem(placement: .principal) {
                Text(localization.isArabic ? "تعديل البيانات الشخصية" : "Edit Profile")
                    .font(.app(17, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)
            }
        }
        .onAppear {
            fullName = user?.fullName ?? "محمد أحمد"
            phoneNumber = user?.phoneNumber ?? "+20 101 234 5678"
            email = user?.email ?? "mohamed@taboor.com"
        }
    }

    private func saveProfile() {
        guard !fullName.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        isSaving = true
        let updated = User(
            id: user?.id ?? "local-user",
            email: email,
            fullName: fullName,
            phoneNumber: phoneNumber,
            role: user?.role,
            token: user?.token
        )
        UserDefaultsManager.shared.save(updated, forKey: AppConstants.Storage.savedUser)
        UINotificationFeedbackGenerator().notificationOccurred(.success)
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
            isSaving = false
            onSaved(updated)
            dismiss()
            onDismiss()
        }
    }
}

// MARK: - Native Pushed View for Change Password

struct PasswordInputField: View {
    let placeholder: String
    @Binding var text: String
    @Binding var isVisible: Bool
    let icon: String
    let iconColor: Color

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: icon)
                .font(.system(size: 15))
                .foregroundColor(iconColor)
                .frame(width: 20)

            ZStack(alignment: .leading) {
                // Secure Field
                SecureField(placeholder, text: $text)
                    .font(.system(size: 14.5, weight: .medium))
                    .foregroundColor(AppColors.textPrimary)
                    .textContentType(.password)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .environment(\.layoutDirection, .leftToRight)
                    .multilineTextAlignment(.leading)
                    .opacity(isVisible ? 0 : 1)
                    .allowsHitTesting(!isVisible)

                // Plain Text Field
                TextField(placeholder, text: $text)
                    .font(.system(size: 14.5, weight: .medium))
                    .foregroundColor(AppColors.textPrimary)
                    .textContentType(.password)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                    .environment(\.layoutDirection, .leftToRight)
                    .multilineTextAlignment(.leading)
                    .opacity(isVisible ? 1 : 0)
                    .allowsHitTesting(isVisible)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            Button(action: {
                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                withAnimation(.easeInOut(duration: 0.15)) {
                    isVisible.toggle()
                }
            }) {
                Image(systemName: isVisible ? "eye.slash.fill" : "eye.fill")
                    .font(.system(size: 14))
                    .foregroundColor(AppColors.textTertiary)
                    .frame(width: 24, height: 24)
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 13)
        .background(Color(hex: 0xFBF9F5))
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 14, style: .continuous)
                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
        )
    }
}

struct ChangePasswordView: View {
    let onDismiss: () -> Void

    @ObservedObject private var localization = LocalizationManager.shared
    @Environment(\.dismiss) private var dismiss

    @State private var currentPass = ""
    @State private var newPass = ""
    @State private var confirmPass = ""
    @State private var showCurrentPass = false
    @State private var showNewPass = false
    @State private var showConfirmPass = false
    @State private var errorMessage: String? = nil
    @State private var isUpdating = false

    var body: some View {
        ScrollView(showsIndicators: false) {
            VStack(spacing: 20) {
                // Fields Card
                VStack(spacing: 16) {
                    // Current Password
                    VStack(alignment: .leading, spacing: 7) {
                        Text(localization.isArabic ? "كلمة المرور الحالية" : "Current Password")
                            .font(.app(13, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)

                        PasswordInputField(
                            placeholder: localization.isArabic ? "أدخل كلمة المرور الحالية" : "Enter current password",
                            text: $currentPass,
                            isVisible: $showCurrentPass,
                            icon: "lock.shield.fill",
                            iconColor: AppColors.amber
                        )
                    }

                    // New Password
                    VStack(alignment: .leading, spacing: 7) {
                        Text(localization.isArabic ? "كلمة المرور الجديدة" : "New Password")
                            .font(.app(13, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)

                        PasswordInputField(
                            placeholder: localization.isArabic ? "كلمة المرور الجديدة" : "New password",
                            text: $newPass,
                            isVisible: $showNewPass,
                            icon: "key.fill",
                            iconColor: AppColors.teal
                        )
                    }

                    // Confirm New Password
                    VStack(alignment: .leading, spacing: 7) {
                        Text(localization.isArabic ? "تأكيد كلمة المرور الجديدة" : "Confirm New Password")
                            .font(.app(13, .bold, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textPrimary)

                        PasswordInputField(
                            placeholder: localization.isArabic ? "تأكيد كلمة المرور" : "Confirm new password",
                            text: $confirmPass,
                            isVisible: $showConfirmPass,
                            icon: "checkmark.shield.fill",
                            iconColor: AppColors.teal
                        )
                    }

                    if let err = errorMessage {
                        HStack(spacing: 6) {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .font(.system(size: 12))
                            Text(err)
                                .font(.app(12, .medium, isArabic: localization.isArabic))
                        }
                        .foregroundColor(AppColors.accentRed)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    }
                }
                .padding(18)
                .background(Color.white)
                .clipShape(RoundedRectangle(cornerRadius: 22, style: .continuous))
                .overlay(RoundedRectangle(cornerRadius: 22, style: .continuous).strokeBorder(AppColors.cardBorder, lineWidth: 1))
                .shadow(color: Color.black.opacity(0.02), radius: 8, y: 2)

                // Update Button
                Button(action: updatePassword) {
                    HStack {
                        Spacer()
                        if isUpdating {
                            ProgressView().tint(.white)
                        } else {
                            Text(localization.isArabic ? "تحديث كلمة المرور" : "Update Password")
                                .font(.app(15, .bold, isArabic: localization.isArabic))
                                .foregroundColor(.white)
                        }
                        Spacer()
                    }
                    .padding(.vertical, 14)
                    .background(
                        LinearGradient(
                            colors: [AppColors.ink, AppColors.deepTeal],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 14))
                    .shadow(color: AppColors.ink.opacity(0.2), radius: 6, y: 2)
                }
                .disabled(isUpdating || currentPass.isEmpty || newPass.isEmpty)
                .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.90))

                Spacer().frame(height: 40)
            }
            .padding(.horizontal, 20)
            .padding(.top, 14)
        }
        .background(AppColors.screenBackground.ignoresSafeArea())
        .navigationBarBackButtonHidden(true)
        .toolbar(.visible, for: .navigationBar)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button(action: {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    dismiss()
                    onDismiss()
                }) {
                    Image(systemName: localization.isArabic ? "chevron.right" : "chevron.left")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(AppColors.textPrimary)
                }
            }

            ToolbarItem(placement: .principal) {
                Text(localization.isArabic ? "تغيير كلمة المرور" : "Change Password")
                    .font(.app(17, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)
            }
        }
    }

    private func updatePassword() {
        errorMessage = nil
        if newPass.count < 6 {
            errorMessage = localization.isArabic ? "كلمة المرور يجب أن لا تقل عن 6 أحرف" : "Password must be at least 6 characters"
            UINotificationFeedbackGenerator().notificationOccurred(.error)
            return
        }
        if newPass != confirmPass {
            errorMessage = localization.isArabic ? "كلمتا المرور غير متطابقتين" : "Passwords do not match"
            UINotificationFeedbackGenerator().notificationOccurred(.error)
            return
        }
        isUpdating = true
        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.4) {
            isUpdating = false
            UINotificationFeedbackGenerator().notificationOccurred(.success)
            dismiss()
            onDismiss()
        }
    }
}

// MARK: - Submodal Views

struct HelpCenterModalView: View {
    @ObservedObject private var localization = LocalizationManager.shared
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: 18) {
                    Text(localization.isArabic ? "الأسئلة الأكثر شيوعاً" : "Frequently Asked Questions")
                        .font(.app(16, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textPrimary)

                    faqItem(
                        qAr: "كيف يعمل نظام الطابور الذكي في طابور؟",
                        qEn: "How does the smart queue work in Taboor?",
                        aAr: "يمكنك حجز دورك في أي عيادة أو مركز خدمة من أي مكان. يعرض التطبيق رقم تذكرتك وعدد المنتظرين أمامك والوقت التقديري، ويرسل لك إشعاراً عندما يقترب دورك.",
                        aEn: "You can book your ticket remotely. The app displays your live queue number, how many people are ahead, and alerts you when your turn is close."
                    )

                    faqItem(
                        qAr: "ماذا أفعل إذا تأخرت عن موعدي؟",
                        qEn: "What happens if I'm running late?",
                        aAr: "يمكنك استخدام زر 'تأجيل الدور (+15 دقيقة)' في شاشة طابوري لتأخير دورك بسهولة ودون أن تفقد حجزك.",
                        aEn: "You can tap 'Postpone (+15m)' in the My Queue tab to push your turn back without having to re-book."
                    )

                    faqItem(
                        qAr: "كيف أتواصل مع الدعم الفني؟",
                        qEn: "How to contact customer support?",
                        aAr: "فريق دعم طابور متاح 24/7 عبر البريد الإلكتروني support@taboor.app أو عبر واتساب.",
                        aEn: "Taboor support is available 24/7 via support@taboor.app or via WhatsApp."
                    )
                }
                .padding(20)
            }
            .background(AppColors.screenBackground.ignoresSafeArea())
            .navigationTitle(localization.isArabic ? "المساعدة والدعم" : "Help & Support")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        dismiss()
                    }) {
                        Image(systemName: "xmark")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(AppColors.textSecondary)
                    }
                }
            }
        }
    }

    private func faqItem(qAr: String, qEn: String, aAr: String, aEn: String) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(localization.isArabic ? qAr : qEn)
                .font(.app(14, .bold, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textPrimary)

            Text(localization.isArabic ? aAr : aEn)
                .font(.app(12.5, .regular, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textSecondary)
                .lineSpacing(3)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).strokeBorder(AppColors.cardBorder, lineWidth: 1))
    }
}

struct PrivacyTermsModalView: View {
    @ObservedObject private var localization = LocalizationManager.shared
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView(showsIndicators: false) {
                VStack(alignment: .leading, spacing: 16) {
                    Text(localization.isArabic ? "سياسة الخصوصية واستخدام البيانات" : "Privacy Policy & Terms")
                        .font(.app(16, .bold, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textPrimary)

                    Text(localization.isArabic
                         ? "نحن في طابور نحترم خصوصيتك بالكامل. جميع بياناتك الشخصية وبيانات الطوابير والموقع الجغرافي محمية ومشفرة وفق أعلى معايير الأمان الدولية، ولا تتم مشاركتها مع أي أطراف خارجية."
                         : "At Taboor, we respect your privacy. All your personal data, queue entries, and location information are encrypted and protected following the highest global security standards.")
                        .font(.app(13, .regular, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)
                        .lineSpacing(4)
                }
                .padding(20)
            }
            .background(AppColors.screenBackground.ignoresSafeArea())
            .navigationTitle(localization.isArabic ? "الشروط والخصوصية" : "Terms & Privacy")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        dismiss()
                    }) {
                        Image(systemName: "xmark")
                            .font(.system(size: 13, weight: .bold))
                            .foregroundColor(AppColors.textSecondary)
                    }
                }
            }
        }
    }
}
