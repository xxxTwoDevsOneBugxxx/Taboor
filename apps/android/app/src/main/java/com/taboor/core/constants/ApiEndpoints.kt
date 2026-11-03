package com.taboor.core.constants

/**
 * Backend API Endpoints for Taboor (طابور)
 * Mirrors the ASP.NET Core Web API (Taboor_API) controllers and routes.
 */
object ApiEndpoints {
    // Base URLs
    const val BASE_URL = "https://taboorapi.runasp.net/api/"
    const val DEV_BASE_URL = "http://10.0.2.2:5000/api/"

    // Controller Roots
    private const val AUTH = "Auth"
    private const val PLANS = "Plans"
    private const val QUEUES = "Queues"
    private const val SERVICES = "Services"
    private const val APPOINTMENTS = "Appointments"

    // ==========================================
    // 1. Authentication Endpoints (AuthController)
    // ==========================================
    const val LOGIN = "$AUTH/Login"
    const val REGISTER = "$AUTH/Register"
    const val SEND_CODE = "$AUTH/send-code"
    const val VERIFY_EMAIL = "$AUTH/verify-email"
    const val FORGOT_PASSWORD = "$AUTH/forgot-password"
    const val VERIFY_RESET_CODE = "$AUTH/verify-reset-code"
    const val RESET_PASSWORD = "$AUTH/reset-password"
    const val REFRESH_TOKEN = "$AUTH/refresh"
    const val REVOKE_TOKEN = "$AUTH/revoke"
    const val EXTERNAL_LOGIN = "$AUTH/ExternalLogin"
    const val EXTERNAL_LOGIN_CALLBACK = "$AUTH/ExternalLoginCallback"
    const val EXTERNAL_LOGIN_CONFIRMATION = "$AUTH/ExternalLoginConfirmation"
    const val GOOGLE_MOBILE = "$AUTH/GoogleMobile"
    const val FACEBOOK_MOBILE = "$AUTH/FacebookMobile"

    // ==========================================
    // 2. Subscription Plans (PlansController)
    // ==========================================
    const val GET_ALL_PLANS = PLANS
    fun getPlanById(id: Int) = "$PLANS/$id"

    // ==========================================
    // 3. Queue & Service Endpoints (Upcoming)
    // ==========================================
    const val NEARBY_SERVICES = "$SERVICES/nearby"
    const val ALL_SERVICES = "$SERVICES"
    fun getServiceDetails(serviceId: String) = "$SERVICES/$serviceId"
    const val JOIN_QUEUE = "$QUEUES/join"
    const val LEAVE_QUEUE = "$QUEUES/leave"
    const val DELAY_TICKET = "$QUEUES/delay"
    fun getActiveTicket(userId: String) = "$QUEUES/active/$userId"

    /**
     * Resolves the full URL for any API endpoint.
     */
    fun url(endpoint: String, isDevelopment: Boolean = false): String {
        val root = if (isDevelopment) DEV_BASE_URL else BASE_URL
        val cleanEndpoint = endpoint.removePrefix("/")
        return "$root$cleanEndpoint"
    }
}
