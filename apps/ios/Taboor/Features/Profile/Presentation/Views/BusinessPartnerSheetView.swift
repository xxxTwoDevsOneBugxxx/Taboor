import SwiftUI

// MARK: - Native iOS Business Partner & Clinic Onboarding Sheet
// Displays the business value proposition, SaaS plans, and direct link to the business platform

public struct BusinessPartnerSheetView: View {
    @Environment(\.dismiss) private var dismiss
    @ObservedObject private var localization = LocalizationManager.shared
    @Environment(\.colorScheme) private var colorScheme

    private let businessWebsiteURL = URL(string: "https://taboor.app/business")!

    public init() {}

    public var body: some View {
        ZStack(alignment: .bottom) {
            // 1. Scrollable Main Content
            ScrollView(showsIndicators: false) {
                VStack(spacing: 20) {
                    // 1. Hero Header
                    heroHeaderView
                        .padding(.top, 14)

                    // 2. Business Value Pillars
                    valuePillarsSection

                    // 3. SaaS Plans Preview
                    plansPreviewSection

                    // 4. Trial Guarantee Badge
                    trialGuaranteeBanner

                    // Bottom Spacer for Floating CTA
                    Spacer().frame(height: 95)
                }
                .padding(.horizontal, 20)
            }
            .background(AppColors.screenBackground.ignoresSafeArea())

            // 2. Floating Bottom CTA Bar
            bottomFloatingCTA

            // 3. Top Floating Dismiss Button (Native Apple Liquid Glass Style)
            VStack {
                HStack {
                    Spacer()

                    Button(action: {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        dismiss()
                    }) {
                        Image(systemName: "xmark")
                            .font(.system(size: 15, weight: .semibold))
                            .foregroundColor(AppColors.deepTeal)
                            .frame(width: 24, height: 24)
                    }
                    .buttonStyle(.glass)
                    .buttonBorderShape(.circle)
                }
                .padding(.horizontal, 18)
                .padding(.top, 14)

                Spacer()
            }
        }
        .presentationDragIndicator(.visible)
    }

    // MARK: - 1. Hero Header
    private var heroHeaderView: some View {
        VStack(spacing: 12) {
            // Pill Badge
            HStack(spacing: 6) {
                Image(systemName: "building.2.fill")
                    .font(.system(size: 11, weight: .bold))
                    .foregroundColor(AppColors.amber)

                Text(localization.isArabic ? "طابور للأعمال والعيادات" : "Taboor for Business")
                    .font(.app(12, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(AppColors.softTeal)
            .clipShape(Capsule())

            // Main Title
            Text(localization.isArabic ? "انضم إلى شركاء طابور" : "Join Taboor Business Partners")
                .font(.app(22, .extraBold, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textPrimary)
                .multilineTextAlignment(.center)
                .lineSpacing(3)

            // Subtitle
            Text(localization.isArabic
                 ? "نظام سحابي ذكي لإدارة الطوابير والحجوزات لعيادتك أو نشاطك التجاري بكل سهولة."
                 : "A smart cloud system for queues and appointment management for your clinic or business.")
                .font(.app(13.5, .regular, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textSecondary)
                .multilineTextAlignment(.center)
                .lineSpacing(3)
                .padding(.horizontal, 8)
        }
        .padding(.bottom, 4)
    }

    // MARK: - 2. Value Pillars (Real Business Benefits)
    private var valuePillarsSection: some View {
        VStack(spacing: 12) {
            pillarRow(
                icon: "person.3.sequence.fill",
                color: AppColors.teal,
                bg: AppColors.softTeal,
                title: localization.isArabic ? "تنظيم الحجوزات ومنع التكدس" : "Queue Control & Less Crowding",
                description: localization.isArabic
                    ? "تخلص من الزحام في صالة الانتظار ونظّم دخول العملاء والمرضى بأرقام إلكترونية دقيقة."
                    : "Eliminate crowded waiting areas and serve visitors in an orderly digital flow."
            )

            pillarRow(
                icon: "tv.fill",
                color: AppColors.indigo,
                bg: AppColors.indigo.opacity(0.12),
                title: localization.isArabic ? "شاشات عرض القاعة والنداء الصوتي" : "Lounge TV & Voice Calling",
                description: localization.isArabic
                    ? "اعرض الدور الحالي ونادي على العميل صوتياً على أي شاشة تلفزيون داخل صالتك تلقائياً."
                    : "Display live turns on any smart TV and announce turns with automatic voice calling."
            )

            pillarRow(
                icon: "bell.badge.fill",
                color: AppColors.amber,
                bg: AppColors.amber.opacity(0.14),
                title: localization.isArabic ? "تطبيق موبايل وتنبيهات فورية لعملائك" : "Client Mobile App & Alerts",
                description: localization.isArabic
                    ? "يُمكّن عملاءك من متابعة موعدهم والحضور في الوقت المناسب دون انتظار طويل في المكان."
                    : "Enable customers to arrive right on time, reducing no-shows and wasted waiting."
            )

            pillarRow(
                icon: "chart.line.uptrend.xyaxis",
                color: Color(hex: 0x00C853),
                bg: Color(hex: 0x00C853).opacity(0.12),
                title: localization.isArabic ? "تقارير دقيقة ومتابعة الفروع" : "Actionable Reports & Insights",
                description: localization.isArabic
                    ? "لوحة تحكم سحابية لمتابعة أعداد الزوار وسرعة تقديم الخدمة وأداء الموظفين لحظة بلحظة."
                    : "Track visitor volumes, average service times, and staff efficiency across all branches."
            )
        }
    }

    private func pillarRow(icon: String, color: Color, bg: Color, title: String, description: String) -> some View {
        HStack(alignment: .top, spacing: 14) {
            ZStack {
                RoundedRectangle(cornerRadius: 14, style: .continuous)
                    .fill(bg)
                    .frame(width: 44, height: 44)

                Image(systemName: icon)
                    .font(.system(size: 19, weight: .semibold))
                    .foregroundColor(color)
            }

            VStack(alignment: .leading, spacing: 3) {
                Text(title)
                    .font(.app(14.5, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)

                Text(description)
                    .font(.app(12, .regular, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
                    .lineSpacing(2)
            }

            Spacer(minLength: 0)
        }
        .padding(14)
        .background(AppColors.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .strokeBorder(AppColors.cardBorder, lineWidth: 1)
        )
    }

    // MARK: - 3. SaaS Plans Showcase
    private var plansPreviewSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(localization.isArabic ? "باقات تناسب جميع أحجام الأعمال" : "Plans for Every Business Size")
                .font(.app(15.5, .bold, isArabic: localization.isArabic))
                .foregroundColor(AppColors.textPrimary)

            HStack(spacing: 10) {
                planCard(
                    title: localization.isArabic ? "الأساسية" : "Starter",
                    badge: localization.isArabic ? "فرع واحد" : "1 Branch",
                    highlight: false
                )

                planCard(
                    title: localization.isArabic ? "المتقدمة" : "Pro",
                    badge: localization.isArabic ? "الأكثر طلباً" : "Popular",
                    highlight: true
                )

                planCard(
                    title: localization.isArabic ? "المؤسسات" : "Enterprise",
                    badge: localization.isArabic ? "فروع متعددة" : "Multi-Branch",
                    highlight: false
                )
            }
        }
    }

    private func planCard(title: String, badge: String, highlight: Bool) -> some View {
        VStack(spacing: 8) {
            Text(badge)
                .font(.app(9.5, .bold, isArabic: localization.isArabic))
                .foregroundColor(highlight ? .white : AppColors.teal)
                .padding(.horizontal, 7)
                .padding(.vertical, 3)
                .background(highlight ? AppColors.amber : AppColors.softTeal)
                .clipShape(Capsule())

            Text(title)
                .font(.app(13.5, .bold, isArabic: localization.isArabic))
                .foregroundColor(highlight ? .white : AppColors.textPrimary)

            Text(localization.isArabic ? "تجربة 14 يوم" : "14-day trial")
                .font(.app(10, .medium, isArabic: localization.isArabic))
                .foregroundColor(highlight ? .white.opacity(0.85) : AppColors.textSecondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 14)
        .padding(.horizontal, 6)
        .background(highlight ? AppColors.ink : AppColors.cardBackground)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .strokeBorder(highlight ? AppColors.amber.opacity(0.5) : AppColors.cardBorder, lineWidth: 1.2)
        )
    }

    // MARK: - 4. Trial Guarantee Banner
    private var trialGuaranteeBanner: some View {
        HStack(spacing: 12) {
            Image(systemName: "checkmark.shield.fill")
                .font(.system(size: 24))
                .foregroundColor(AppColors.teal)

            VStack(alignment: .leading, spacing: 2) {
                Text(localization.isArabic ? "14 يوماً تجربة مجانية بالكامل" : "14-Day Free Trial Guarantee")
                    .font(.app(13.5, .bold, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textPrimary)

                Text(localization.isArabic ? "لا تتطلب بطاقة ائتمان • إعداد فوري خلال 5 دقائق" : "No credit card required • Setup in 5 minutes")
                    .font(.app(11.5, .regular, isArabic: localization.isArabic))
                    .foregroundColor(AppColors.textSecondary)
            }

            Spacer()
        }
        .padding(14)
        .background(AppColors.softTeal.opacity(0.6))
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
    }

    // MARK: - 5. True Floating Bottom CTA Button
    private var bottomFloatingCTA: some View {
        VStack(spacing: 0) {
            Button(action: {
                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                UIApplication.shared.open(businessWebsiteURL)
            }) {
                HStack(spacing: 8) {
                    Text(localization.isArabic ? "زيارة موقع طابور للأعمال والتسجيل" : "Visit Taboor for Business")
                        .font(.app(15.5, .bold, isArabic: localization.isArabic))

                    Image(systemName: localization.isArabic ? "arrow.left" : "arrow.right")
                        .font(.system(size: 14, weight: .bold))
                }
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .frame(height: 52)
                .background(
                    LinearGradient(
                        colors: [AppColors.ink, AppColors.deepTeal],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .clipShape(Capsule())
                .shadow(color: AppColors.ink.opacity(0.32), radius: 10, y: 5)
            }
            .buttonStyle(ScaleButtonStyle(scale: 0.98, pressedOpacity: 0.92))
        }
        .padding(.horizontal, 20)
        .padding(.top, 14)
        .padding(.bottom, 14)
        .background(
            LinearGradient(
                colors: [
                    AppColors.screenBackground.opacity(0),
                    AppColors.screenBackground.opacity(0.8),
                    AppColors.screenBackground
                ],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea(edges: .bottom)
        )
    }
}
