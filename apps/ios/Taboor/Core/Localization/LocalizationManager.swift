import SwiftUI
import Observation

public enum AppLanguage: String, CaseIterable {
    case arabic = "ar"
    case english = "en"

    public var title: String {
        switch self {
        case .arabic: return "عربي"
        case .english: return "English"
        }
    }

    public var code: String {
        switch self {
        case .arabic: return "عربي"
        case .english: return "EN"
        }
    }

    public var isArabic: Bool {
        return self == .arabic
    }

    public var layoutDirection: LayoutDirection {
        return self == .arabic ? .rightToLeft : .leftToRight
    }
}

@Observable
public final class LocalizationManager {
    public static let shared = LocalizationManager()

    public var currentLanguage: AppLanguage = .arabic {
        didSet {
            UserDefaults.standard.set(currentLanguage.rawValue, forKey: "app_language")
        }
    }

    public var isArabic: Bool {
        currentLanguage.isArabic
    }

    public var layoutDirection: LayoutDirection {
        currentLanguage.layoutDirection
    }

    public init() {
        if let saved = UserDefaults.standard.string(forKey: "app_language"),
           let lang = AppLanguage(rawValue: saved) {
            self.currentLanguage = lang
        }
    }

    public func toggleLanguage() {
        currentLanguage = currentLanguage == .arabic ? .english : .arabic
    }

    // MARK: - Localized Strings Mirroring Flutter L10n

    // Common
    public var appTitle: String { isArabic ? "طابور" : "Taboor" }
    public var back: String { isArabic ? "رجوع" : "Back" }
    public var orText: String { isArabic ? "أو" : "or" }
    public var sendCode: String { isArabic ? "إرسال الكود" : "Send code" }
    public var resendPrompt: String { isArabic ? "لم يصلك الكود؟" : "Didn't get the code?" }
    public var resend: String { isArabic ? "إعادة الإرسال" : "Resend" }
    public var verify: String { isArabic ? "تحقق" : "Verify" }

    // Onboarding
    public var onboarding1Title: String { isArabic ? "استنى وأنت في بيتك" : "Wait, from home" }
    public var onboarding1Subtitle: String {
        isArabic
            ? "سجّل مكانك من أي مكان\nواحصل على رقمك الفوري مجاناً"
            : "Reserve your spot from anywhere\nand get your instant number for free"
    }
    public var onboarding2Title: String { isArabic ? "دورك.. لحظة بلحظة" : "Your turn... live" }
    public var onboarding2Subtitle: String {
        isArabic
            ? "تابع ترتيبك في الطابور مباشر\nمع عداد واضح ومؤشر تقدم"
            : "Track your position in queue live\nwith clear counter and progress"
    }
    public var onboarding3Title: String { isArabic ? "تنبيهك قبل ما يجي دورك" : "Alerted before your turn" }
    public var onboarding3Subtitle: String {
        isArabic
            ? "مش هيفوتك أي دور أبداً\nهيوصلك إشعار فوري لما يقرب دورك"
            : "You'll never miss your turn\nInstant notification when your turn approaches"
    }
    public var next: String { isArabic ? "التالي" : "Next" }
    public var startNow: String { isArabic ? "ابدأ الآن" : "Start now" }

    // Ticket
    public var ticketNumberLabel: String { isArabic ? "رقمك" : "Your Number" }
    public var ticketSitRelax: String { isArabic ? "مجلسك وريح" : "Sit & Relax" }

    // Live Tracking
    public var liveAhead: String { isArabic ? "أمامك" : "Ahead of you" }
    public var liveTurnNear: String { isArabic ? "دورك قرب" : "Almost your turn" }

    // Alert
    public var alertLeaveTitle: String { isArabic ? "وقت الخروج!" : "Time to go!" }
    public var alertOnlyTwo: String { isArabic ? "قدامك 2 فقط" : "Only 2 ahead" }
    public var alertNow: String { isArabic ? "الآن" : "Now" }

    // Login
    public var loginWelcomeTitle: String { isArabic ? "أهلًا بيك من جديد" : "Welcome back" }
    public var loginWelcomeSubtitle: String {
        isArabic ? "سجّل دخولك واستكمل طلبك في الطابور" : "Sign in and continue your request in the queue"
    }
    public var emailLabel: String { isArabic ? "البريد الإلكتروني" : "Email" }
    public var emailHint: String { "you@example.com" }
    public var passwordLabel: String { isArabic ? "كلمة المرور" : "Password" }
    public var passwordHint: String { "••••••••••" }
    public var forgotPassword: String { isArabic ? "نسيت كلمة المرور؟" : "Forgot your password?" }
    public var loginButton: String { isArabic ? "تسجيل الدخول" : "Sign in" }
    public var noAccountYet: String { isArabic ? "مش عندك حساب؟" : "Don't have an account?" }
    public var createAccount: String { isArabic ? "أنشئ حسابًا" : "Create account" }
    public var emailRequired: String { isArabic ? "اكتب إيميلك." : "Please enter your email." }
    public var invalidEmail: String { isArabic ? "اكتب إيميل صحيح." : "Please enter a valid email." }
    public var passwordRequired: String { isArabic ? "اكتب كلمة المرور." : "Please enter a password." }
    public var passwordMinLength: String { isArabic ? "كلمة المرور يجب ألا تقل عن 6 أحرف" : "Password must be at least 6 characters" }
    public var invalidCredentials: String { isArabic ? "الإيميل أو كلمة المرور غلط." : "Invalid email or password." }
    public var loggingInText: String { isArabic ? "جارٍ تسجيل الدخول..." : "Signing in..." }

    // Register
    public var createYourAccount: String { isArabic ? "أنشئ حسابك" : "Create your account" }
    public var registerSubtitle: String {
        isArabic
            ? "ابدأ بالبريد الإلكتروني\nوهنبعث لك كود التحقق"
            : "Start with your email\nand we'll send you a verification code"
    }
    public var alreadyHaveAccount: String { isArabic ? "عندك حساب بالفعل؟" : "Already have an account?" }
    public var otpTitle: String { isArabic ? "كود التحقق" : "Verification code" }
    public func otpDescription(_ email: String) -> String {
        isArabic
            ? "أدخل الكود المكون من 6 أرقام\nأُرسل إلى \(email)"
            : "Enter the 6-digit code\nsent to \(email)"
    }
    public var completeDetails: String { isArabic ? "كمل بياناتك" : "Complete your details" }
    public var detailsSubtitle: String {
        isArabic ? "الاسم وهاتفك وكلمة المرور عشان تكمّل" : "Name, phone and password to finish"
    }
    public var fullNameLabel: String { isArabic ? "الاسم الكامل" : "Full name" }
    public var fullNameHint: String { isArabic ? "مثال: محمد أحمد" : "e.g. Mohamed Ahmed" }
    public var phoneLabel: String { isArabic ? "رقم الموبايل" : "Phone number" }
    public var phoneHint: String { "01xxxxxxxxx" }
    public var confirmPasswordLabel: String { isArabic ? "تأكيد كلمة المرور" : "Confirm password" }
    public var passwordWeakLabel: String { isArabic ? "ضعيفة" : "Weak" }
    public var passwordMediumLabel: String { isArabic ? "متوسطة" : "Medium" }
    public var passwordStrongLabel: String { isArabic ? "قوية" : "Strong" }
    public var passwordVeryStrongLabel: String { isArabic ? "قوية جداً" : "Very strong" }
    public var passwordMatchOk: String { isArabic ? "كلمتا المرور متطابقتين" : "Passwords match" }
    public var passwordMismatch: String { isArabic ? "كلمتا المرور غير متطابقتين" : "Passwords don't match" }
    public var createAccountSubmit: String { isArabic ? "إنشاء الحساب" : "Create account" }
}
