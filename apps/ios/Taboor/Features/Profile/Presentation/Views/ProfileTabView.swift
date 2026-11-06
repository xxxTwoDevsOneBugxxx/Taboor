import SwiftUI
import UIKit

// MARK: - Native iOS 26 Profile & Settings View (Native SwiftUI InsetGrouped List)

public struct ProfileTabView: View {
    public let onLogout: () -> Void

    @ObservedObject private var localization = LocalizationManager.shared
    @Environment(\.colorScheme) private var colorScheme

    @State private var user: User? = AuthRepository.shared.getSavedUser()
    @AppStorage("app_appearance_mode") private var appearanceMode: String = "system"
    @State private var faceIdEnabled = true
    @State private var notificationsEnabled = true
    @State private var showLogoutConfirm = false
    @State private var showHelpSheet = false
    @State private var showPrivacySheet = false

    public init(onLogout: @escaping () -> Void) {
        self.onLogout = onLogout
    }

    public var body: some View {
        NavigationStack {
            List {
                // 1. Profile Header Card (Pushes EditProfileView)
                Section {
                    NavigationLink(destination: EditProfileView(user: user, onSaved: { updatedUser in
                        self.user = updatedUser
                    })) {
                        HStack(spacing: 14) {
                            ZStack {
                                Circle()
                                    .fill(
                                        LinearGradient(
                                            colors: [AppColors.teal.opacity(0.85), AppColors.teal],
                                            startPoint: .topLeading,
                                            endPoint: .bottomTrailing
                                        )
                                    )
                                    .frame(width: 54, height: 54)

                                Image(systemName: "person.fill")
                                    .font(.system(size: 26, weight: .semibold))
                                    .foregroundColor(.white)
                            }

                            VStack(alignment: .leading, spacing: 3) {
                                HStack(spacing: 6) {
                                    Text(user?.fullName.isEmpty == false ? user!.fullName : (localization.isArabic ? "محمد أحمد" : "Mohamed Ahmed"))
                                        .font(.app(17, .bold, isArabic: localization.isArabic))
                                        .foregroundColor(AppColors.textPrimary)
                                        .lineLimit(1)

                                    Image(systemName: "checkmark.seal.fill")
                                        .font(.system(size: 13))
                                        .foregroundColor(AppColors.teal)
                                }

                                Text(user?.email.isEmpty == false ? user!.email : "mohamed@taboor.com")
                                    .font(.app(13, .regular, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textSecondary)
                                    .lineLimit(1)

                                Text(user?.phoneNumber ?? "+20 101 234 5678")
                                    .font(.inter(11, .regular))
                                    .foregroundColor(AppColors.textTertiary)
                                    .lineLimit(1)
                            }
                        }
                        .padding(.vertical, 4)
                    }
                }

                // 2. Account Section (Pushed Navigation Pages)
                Section(localization.isArabic ? "الحساب" : "Account") {
                    NavigationLink(destination: EditProfileView(user: user, onSaved: { updatedUser in
                        self.user = updatedUser
                    })) {
                        Label {
                            Text(localization.isArabic ? "تعديل البيانات الشخصية" : "Edit Personal Details")
                                .font(.app(15, .medium, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textPrimary)
                        } icon: {
                            Image(systemName: "person.crop.circle")
                                .font(.system(size: 17, weight: .medium))
                                .foregroundColor(AppColors.teal)
                                .frame(width: 26)
                        }
                    }

                    NavigationLink(destination: ChangePasswordView()) {
                        Label {
                            Text(localization.isArabic ? "تغيير كلمة المرور" : "Change Password")
                                .font(.app(15, .medium, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textPrimary)
                        } icon: {
                            Image(systemName: "lock.shield")
                                .font(.system(size: 17, weight: .medium))
                                .foregroundColor(AppColors.teal)
                                .frame(width: 26)
                        }
                    }
                }

                // 3. Preferences Section
                Section(localization.isArabic ? "التفضيلات والأمان" : "Preferences & Security") {
                    Toggle(isOn: $faceIdEnabled) {
                        Label {
                            Text(localization.isArabic ? "تسجيل الدخول بـ Face ID" : "Face ID Login")
                                .font(.app(15, .medium, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textPrimary)
                        } icon: {
                            Image(systemName: "faceid")
                                .font(.system(size: 17, weight: .medium))
                                .foregroundColor(AppColors.teal)
                                .frame(width: 26)
                        }
                    }
                    .tint(AppColors.teal)

                    Toggle(isOn: $notificationsEnabled) {
                        Label {
                            Text(localization.isArabic ? "الإشعارات المباشرة" : "Push Notifications")
                                .font(.app(15, .medium, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textPrimary)
                        } icon: {
                            Image(systemName: "bell.badge")
                                .font(.system(size: 17, weight: .medium))
                                .foregroundColor(AppColors.teal)
                                .frame(width: 26)
                        }
                    }
                    .tint(AppColors.teal)

                    Button(action: {
                        withAnimation(.spring(response: 0.35, dampingFraction: 0.8)) {
                            localization.toggleLanguage()
                        }
                    }) {
                        HStack {
                            Label {
                                Text(localization.isArabic ? "لغة التطبيق" : "Language")
                                    .font(.app(15, .medium, isArabic: localization.isArabic))
                                    .foregroundColor(AppColors.textPrimary)
                            } icon: {
                                Image(systemName: "globe")
                                    .font(.system(size: 17, weight: .medium))
                                    .foregroundColor(AppColors.teal)
                                    .frame(width: 26)
                            }

                            Spacer()

                            Text(localization.isArabic ? "العربية" : "English")
                                .font(.app(14, .regular, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textSecondary)

                            Image(systemName: localization.isArabic ? "chevron.left" : "chevron.right")
                                .font(.system(size: 12, weight: .semibold))
                                .foregroundColor(Color(UIColor.tertiaryLabel))
                        }
                    }

                    Picker(selection: $appearanceMode) {
                        Text(localization.isArabic ? "تلقائي" : "System").tag("system")
                        Text(localization.isArabic ? "فاتح" : "Light").tag("light")
                        Text(localization.isArabic ? "داكن" : "Dark").tag("dark")
                    } label: {
                        Label {
                            Text(localization.isArabic ? "المظهر" : "Appearance")
                                .font(.app(15, .medium, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.textPrimary)
                        } icon: {
                            Image(systemName: "circle.lefthalf.filled")
                                .font(.system(size: 17, weight: .medium))
                                .foregroundColor(AppColors.teal)
                                .frame(width: 26)
                        }
                    }
                    .pickerStyle(.menu)
                }

                // 4. Support & Info Section
                Section(localization.isArabic ? "المساعدة والمعلومات" : "Support & About") {
                    settingsActionRow(
                        title: localization.isArabic ? "مركز المساعدة والأسئلة الشائعة" : "Help Center & FAQ",
                        systemImage: "questionmark.circle",
                        iconColor: AppColors.teal
                    ) {
                        showHelpSheet = true
                    }

                    settingsActionRow(
                        title: localization.isArabic ? "الشروط وسياسة الخصوصية" : "Terms & Privacy Policy",
                        systemImage: "doc.text",
                        iconColor: AppColors.teal
                    ) {
                        showPrivacySheet = true
                    }

                    settingsActionRow(
                        title: localization.isArabic ? "مشاركة تطبيق طابور" : "Share Taboor",
                        systemImage: "square.and.arrow.up",
                        iconColor: AppColors.teal
                    ) {
                        if let url = URL(string: "https://taboor.app") {
                            let activityVC = UIActivityViewController(activityItems: [url], applicationActivities: nil)
                            if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                               let rootVC = windowScene.windows.first?.rootViewController {
                                rootVC.present(activityVC, animated: true)
                            }
                        }
                    }
                }

                // 5. Sign Out Section
                Section {
                    Button(role: .destructive, action: {
                        showLogoutConfirm = true
                    }) {
                        HStack {
                            Spacer()
                            Label(localization.isArabic ? "تسجيل الخروج" : "Sign Out", systemImage: "rectangle.portrait.and.arrow.right")
                                .font(.app(15, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.accentRed)
                            Spacer()
                        }
                    }
                } footer: {
                    versionFooter
                }
            }
            .listStyle(.insetGrouped)
            .navigationTitle(localization.isArabic ? "الإعدادات" : "Settings")
            .navigationBarTitleDisplayMode(.large)
            .safeAreaInset(edge: .bottom) {
                Color.clear.frame(height: 50)
            }
            .onAppear {
                user = AuthRepository.shared.getSavedUser()
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
        }
    }

    // MARK: - Helper Row Component (Native List Row with Chevron)
    private func settingsActionRow(
        title: String,
        systemImage: String,
        iconColor: Color,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            HStack {
                Label {
                    Text(title)
                        .font(.app(15, .medium, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textPrimary)
                } icon: {
                    Image(systemName: systemImage)
                        .font(.system(size: 17, weight: .medium))
                        .foregroundColor(iconColor)
                        .frame(width: 26)
                }

                Spacer()

                Image(systemName: localization.isArabic ? "chevron.left" : "chevron.right")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(Color(UIColor.tertiaryLabel))
            }
        }
    }

    private var versionFooter: some View {
        VStack(spacing: 3) {
            Text(localization.isArabic ? "طابور (Taboor) — الإصدار 1.0.0" : "Taboor — Version 1.0.0")
                .font(.app(11, .medium, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textTertiary)

            Text(localization.isArabic ? "منصة إدارة الطوابير والمواعيد الذكية" : "Smart Queue & Appointment Management")
                .font(.app(10, .regular, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textTertiary.opacity(0.8))
        }
        .frame(maxWidth: .infinity, alignment: .center)
        .padding(.top, 8)
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
    let onSaved: (User) -> Void

    @ObservedObject private var localization = LocalizationManager.shared
    @Environment(\.dismiss) private var dismiss

    @State private var fullName: String = ""
    @State private var phoneNumber: String = ""
    @State private var email: String = ""
    @State private var isSaving = false

    var body: some View {
        Form {
            // Profile Photo Header Section
            Section {
                HStack {
                    Spacer()
                    VStack(spacing: 8) {
                        ZStack {
                            Circle()
                                .fill(
                                    LinearGradient(
                                        colors: [AppColors.teal.opacity(0.85), AppColors.teal],
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                                .frame(width: 76, height: 76)

                            Image(systemName: "person.fill")
                                .font(.system(size: 38, weight: .semibold))
                                .foregroundColor(.white)
                        }

                        Text(localization.isArabic ? "صورة الملف الشخصي" : "Profile Photo")
                            .font(.app(13, .medium, isArabic: localization.isArabic))
                            .foregroundColor(AppColors.textSecondary)
                    }
                    .padding(.vertical, 6)
                    Spacer()
                }
                .listRowBackground(Color.clear)
            }

            // Info Fields
            Section {
                HStack(spacing: 12) {
                    Text(localization.isArabic ? "الاسم" : "Name")
                        .font(.app(14, .medium, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)
                        .frame(width: 70, alignment: .leading)

                    TextField(localization.isArabic ? "الاسم بالكامل" : "Full Name", text: $fullName)
                        .font(.app(15, .regular, isArabic: localization.isArabic))
                }

                HStack(spacing: 12) {
                    Text(localization.isArabic ? "الهاتف" : "Phone")
                        .font(.app(14, .medium, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)
                        .frame(width: 70, alignment: .leading)

                    TextField(localization.isArabic ? "رقم الهاتف" : "Phone Number", text: $phoneNumber)
                        .font(.inter(15, .regular))
                        .keyboardType(.phonePad)
                }

                HStack(spacing: 12) {
                    Text(localization.isArabic ? "البريد" : "Email")
                        .font(.app(14, .medium, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.textSecondary)
                        .frame(width: 70, alignment: .leading)

                    TextField(localization.isArabic ? "البريد الإلكتروني" : "Email", text: $email)
                        .font(.inter(15, .regular))
                        .disabled(true)
                        .foregroundColor(AppColors.textTertiary)
                }
            } header: {
                Text(localization.isArabic ? "البيانات الأساسية" : "BASIC INFORMATION")
            } footer: {
                Text(localization.isArabic
                     ? "لا يمكن تعديل البريد الإلكتروني المرتبط بالحساب مباشرة."
                     : "Email address associated with this account cannot be modified directly.")
                    .font(.app(11, .regular, isArabic: localization.isArabic))
            }

            // Action Button
            Section {
                Button(action: saveProfile) {
                    HStack {
                        Spacer()
                        if isSaving {
                            ProgressView().tint(AppColors.teal)
                        } else {
                            Text(localization.isArabic ? "حفظ التغييرات" : "Save Changes")
                                .font(.app(15, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.teal)
                        }
                        Spacer()
                    }
                }
                .disabled(isSaving || fullName.trimmingCharacters(in: .whitespaces).isEmpty)
            }
        }
        .navigationTitle(localization.isArabic ? "الملف الشخصي" : "Edit Profile")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button(action: saveProfile) {
                    if isSaving {
                        ProgressView().tint(AppColors.teal)
                    } else {
                        Text(localization.isArabic ? "حفظ" : "Save")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(AppColors.teal)
                    }
                }
                .disabled(isSaving || fullName.trimmingCharacters(in: .whitespaces).isEmpty)
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
        }
    }
}

// MARK: - Native Pushed View for Change Password

struct ChangePasswordView: View {
    @ObservedObject private var localization = LocalizationManager.shared
    @Environment(\.dismiss) private var dismiss

    @State private var currentPass = ""
    @State private var newPass = ""
    @State private var confirmPass = ""
    @State private var errorMessage: String? = nil
    @State private var isUpdating = false

    var body: some View {
        Form {
            Section(localization.isArabic ? "كلمة المرور الحالية" : "CURRENT PASSWORD") {
                SecureField(localization.isArabic ? "أدخل كلمة المرور الحالية" : "Enter current password", text: $currentPass)
                    .font(.system(size: 15))
            }

            Section {
                SecureField(localization.isArabic ? "كلمة المرور الجديدة" : "New password", text: $newPass)
                    .font(.system(size: 15))

                SecureField(localization.isArabic ? "تأكيد كلمة المرور الجديدة" : "Confirm new password", text: $confirmPass)
                    .font(.system(size: 15))
            } header: {
                Text(localization.isArabic ? "كلمة المرور الجديدة" : "NEW PASSWORD")
            } footer: {
                if let err = errorMessage {
                    Text(err)
                        .font(.app(12, .medium, isArabic: localization.isArabic))
                        .foregroundColor(AppColors.accentRed)
                } else {
                    Text(localization.isArabic ? "يجب أن تحتوي كلمة المرور على 6 أحرف على الأقل." : "Password must be at least 6 characters.")
                        .font(.app(11, .regular, isArabic: localization.isArabic))
                }
            }

            Section {
                Button(action: updatePassword) {
                    HStack {
                        Spacer()
                        if isUpdating {
                            ProgressView().tint(AppColors.teal)
                        } else {
                            Text(localization.isArabic ? "تحديث كلمة المرور" : "Update Password")
                                .font(.app(15, .bold, isArabic: localization.isArabic))
                                .foregroundColor(AppColors.teal)
                        }
                        Spacer()
                    }
                }
                .disabled(isUpdating || currentPass.isEmpty || newPass.isEmpty)
            }
        }
        .navigationTitle(localization.isArabic ? "تغيير كلمة المرور" : "Change Password")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button(action: updatePassword) {
                    if isUpdating {
                        ProgressView().tint(AppColors.teal)
                    } else {
                        Text(localization.isArabic ? "تحديث" : "Update")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(AppColors.teal)
                    }
                }
                .disabled(isUpdating || currentPass.isEmpty || newPass.isEmpty)
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
        }
    }
}

struct HelpCenterModalView: View {
    @ObservedObject private var localization = LocalizationManager.shared
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
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
                        aAr: "يمكنك استخدام زر 'تأجيل الدور (+10 دقائق)' في شاشة طابوري لتأخير دورك بسهولة ودون أن تفقد حجزك.",
                        aEn: "You can tap 'Postpone (+10m)' in the My Queue tab to push your turn back without having to re-book."
                    )

                    faqItem(
                        qAr: "كيف أتواصل مع الدعم الفني؟",
                        qEn: "How to contact customer support?",
                        aAr: "فريق دعم طابور متاح 24/7 عبر البريد الإلكتروني support@taboor.app أو عبر واتساب.",
                        aEn: "Taboor support is available 24/7 via support@taboor.app or via WhatsApp."
                    )
                }
                .padding(24)
            }
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
                .font(.app(13, .regular, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textSecondary)
                .lineSpacing(3)
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(AppColors.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).strokeBorder(AppColors.cardBorder, lineWidth: 1))
    }
}

struct PrivacyTermsModalView: View {
    @ObservedObject private var localization = LocalizationManager.shared
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ScrollView {
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
                .padding(24)
            }
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
