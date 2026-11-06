import Foundation

// MARK: - App Constants (Direct Mirror of Flutter Constants & Live Backend API)

public enum AppConstants {

    // MARK: - Application Metadata
    public static let appName = "taboor"
    public static let appVersion = "1.0.0"
    public static let appTagline = "وِفر وقتك، ومتعيشش في الطابور"

    public enum AppInfo {
        public static let appName = AppConstants.appName
        public static let bundleId = "com.taboor.ios"
        public static let version = AppConstants.appVersion
    }

    // MARK: - API Configuration (Matching Flutter ApiEndpoints)
    public enum API {
        /// Production API Base URL pointing to the live hosted Taboor backend
        public static let baseUrl = "https://taboorapi.runasp.net/api/"

        public static let timeoutInterval: TimeInterval = 25.0

        private static let authRoot = "Auth"

        // Backend Endpoints mirroring Flutter api_endpoints.dart
        public enum Endpoints {
            public static let login = "\(authRoot)/Login"
            public static let register = "\(authRoot)/Register"
            public static let sendCode = "\(authRoot)/send-code"
            public static let verifyEmail = "\(authRoot)/verify-email"
            public static let forgotPassword = "\(authRoot)/forgot-password"
            public static let verifyResetCode = "\(authRoot)/verify-reset-code"
            public static let resetPassword = "\(authRoot)/reset-password"
            public static let refreshToken = "\(authRoot)/refresh"
            public static let revokeToken = "\(authRoot)/revoke"
            public static let externalLogin = "\(authRoot)/ExternalLogin"
            public static let externalLoginCallback = "\(authRoot)/ExternalLoginCallback"
            public static let externalLoginConfirmation = "\(authRoot)/ExternalLoginConfirmation"
            public static let googleMobile = "\(authRoot)/GoogleMobile"
            public static let facebookMobile = "\(authRoot)/FacebookMobile"
        }

        // Standard HTTP Headers
        public enum Headers {
            public static let contentType = "Content-Type"
            public static let accept = "Accept"
            public static let acceptLanguage = "Accept-Language"
            public static let authorization = "Authorization"
            public static let clientType = "X-Client-Type"
            public static let clientTypeValue = "mobile-ios"
        }
    }

    // MARK: - Storage Keys (Matching Flutter AppConstants)
    public enum Storage {
        public static let isFirstTimeKey = "isFirstTime"
        public static let appLanguageKey = "appLanguage"
        public static let accessToken = "taboor_access_token"
        public static let refreshToken = "taboor_refresh_token"
        public static let savedUser = "taboor_saved_user"

        // Semantic aliases
        public static let hasCompletedOnboarding = isFirstTimeKey
        public static let appLanguage = appLanguageKey
    }

    // MARK: - Languages
    public enum Languages {
        public static let arabic = "ar"
        public static let english = "en"
    }

    // MARK: - UI Metrics
    public enum Design {
        public static let cornerRadiusSmall: CGFloat = 8
        public static let cornerRadiusMedium: CGFloat = 14
        public static let cornerRadiusLarge: CGFloat = 20
        public static let buttonHeight: CGFloat = 54
        public static let textFieldHeight: CGFloat = 56
        public static let horizontalPadding: CGFloat = 24
    }
}
