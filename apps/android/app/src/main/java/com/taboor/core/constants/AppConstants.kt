package com.taboor.core.constants

/**
 * Global Application Constants for Taboor (طابور)
 * Central single-source-of-truth for preferences keys, language codes,
 * validation regexes, and UI configuration.
 */
object AppConstants {
    // App Meta Information
    const val APP_NAME = "Taboor"
    const val APP_NAME_AR = "طابور"
    const val APP_VERSION = "1.0.0"
    const val APP_TAGLINE_AR = "وفّر وقتك، ومتعيّش في الطابور"
    const val APP_TAGLINE_EN = "Save your time, skip the waiting queue"

    // SharedPreferences / DataStore Storage Keys
    const val PREFS_NAME = "taboor_app_prefs"
    const val KEY_IS_FIRST_TIME = "is_first_time"
    const val KEY_APP_LANGUAGE = "app_language"
    const val KEY_AUTH_TOKEN = "auth_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_USER_ID = "user_id"
    const val KEY_USER_NAME = "user_name"
    const val KEY_USER_EMAIL = "user_email"
    const val KEY_USER_PHONE = "user_phone"
    const val KEY_BIOMETRICS_ENABLED = "biometrics_enabled"
    const val KEY_DARK_MODE = "dark_mode"

    // Supported Locales
    const val LANG_AR = "ar"
    const val LANG_EN = "en"
    const val DEFAULT_LANG = LANG_AR

    // Timing & Animations (Milliseconds)
    const val SPLASH_DURATION_MS = 2000L
    const val SCREEN_TRANSITION_MS = 350
    const val SEARCH_DEBOUNCE_MS = 300L
    const val OTP_COUNTDOWN_SECONDS = 60

    // Validation Rules
    const val MIN_PASSWORD_LENGTH = 6
    val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
    val EGYPT_PHONE_REGEX = Regex("^(010|011|012|015)[0-9]{8}\$")
}
