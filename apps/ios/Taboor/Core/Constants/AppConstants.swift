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

    // MARK: - API Configuration (Matching ASP.NET Core Backend & Flutter ApiEndpoints)
    public enum API {
        /// Production API Base URL pointing to the live hosted Taboor backend
        public static let baseUrl = "https://taboorapi.runasp.net/api/"

        /// Master switch: When true, loads seeded JSON mock data with simulated server delay.
        /// When false, sends real HTTP requests to the backend API.
        public static var useMockData: Bool = true

        public static let timeoutInterval: TimeInterval = 25.0

        private static let authRoot = "Auth"
        private static let placesRoot = "Places"
        private static let queuesRoot = "Queues"
        private static let alertsRoot = "Alerts"
        private static let historyRoot = "History"
        private static let usersRoot = "Users"

        // Backend Endpoints mirroring ASP.NET Core & Flutter api_endpoints
        public enum Endpoints {
            // 1. Auth Endpoints
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

            // 2. Places & Services Endpoints
            public static let places = "\(placesRoot)"
            public static let placeDetails = { (id: String) in "\(placesRoot)/\(id)" }
            public static let categories = "\(placesRoot)/categories"
            public static let toggleFavoritePlace = { (id: String) in "\(placesRoot)/\(id)/favorite" }

            // 3. Queues & Tickets Endpoints
            public static let activeTickets = "\(queuesRoot)/my-active-tickets"
            public static let joinQueue = "\(queuesRoot)/join"
            public static let bookAppointment = "\(queuesRoot)/book-appointment"
            public static let delayTicket = { (id: String) in "\(queuesRoot)/\(id)/delay" }
            public static let cancelTicket = { (id: String) in "\(queuesRoot)/\(id)/cancel" }

            // 4. Alerts & Notifications Endpoints
            public static let alerts = "\(alertsRoot)"
            public static let markAlertRead = { (id: String) in "\(alertsRoot)/\(id)/read" }
            public static let markAllAlertsRead = "\(alertsRoot)/read-all"
            public static let alertPreferences = "\(alertsRoot)/preferences"

            // 5. History Endpoints
            public static let ticketHistory = "\(historyRoot)/my-history"

            // 6. User Profile Endpoints
            public static let userProfile = "\(usersRoot)/me"
            public static let updateProfile = "\(usersRoot)/profile"
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
