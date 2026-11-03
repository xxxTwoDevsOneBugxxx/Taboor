package com.taboor.core.services

import android.content.Context
import android.content.SharedPreferences
import com.taboor.core.constants.AppConstants
import com.taboor.core.utils.JwtDecoder
import com.taboor.features.auth.data.api.AuthApi
import com.taboor.features.auth.domain.model.LoginResponse
import com.taboor.features.auth.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * Manages persisted user authentication session, token lifecycle,
 * and automatic JWT refresh mirroring Flutter's SessionManager.
 */
object SessionManager {

    private const val KEY_USER_DATA = "user_json_data"

    private var prefs: SharedPreferences? = null
    private val authApi = AuthApi()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun init(context: Context) {
        val sp = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs = sp

        // Load stored user if available
        val userRaw = sp.getString(KEY_USER_DATA, null)
        if (!userRaw.isNullOrBlank()) {
            try {
                val json = JSONObject(userRaw)
                _currentUser.value = User.fromJson(json)
            } catch (_: Exception) {
                _currentUser.value = null
            }
        } else {
            // Default demo user fallback for seamless initial experience
            _currentUser.value = User(
                id = "user_demo",
                email = "customer@taboor.app",
                fullName = "محمد أحمد",
                token = "jwt_token_demo",
                phoneNumber = "01098765432"
            )
        }
    }

    val isLoggedIn: Boolean
        get() = _currentUser.value != null

    val token: String?
        get() = prefs?.getString(AppConstants.KEY_AUTH_TOKEN, null) ?: _currentUser.value?.token

    val refreshToken: String?
        get() = prefs?.getString(AppConstants.KEY_REFRESH_TOKEN, null)

    fun setUser(user: User?) {
        _currentUser.value = user
        if (user != null) {
            prefs?.edit()
                ?.putString(KEY_USER_DATA, user.toJson().toString())
                ?.putString(AppConstants.KEY_AUTH_TOKEN, user.token)
                ?.putString(AppConstants.KEY_USER_ID, user.id)
                ?.putString(AppConstants.KEY_USER_NAME, user.fullName)
                ?.putString(AppConstants.KEY_USER_EMAIL, user.email)
                ?.apply()
        } else {
            clear()
        }
    }

    fun saveLogin(response: LoginResponse) {
        val token = response.token
        val refresh = response.refreshToken
        val user = response.user

        val editor = prefs?.edit()
        if (!token.isNullOrBlank()) {
            editor?.putString(AppConstants.KEY_AUTH_TOKEN, token)
        }
        if (!refresh.isNullOrBlank()) {
            editor?.putString(AppConstants.KEY_REFRESH_TOKEN, refresh)
        }
        if (user != null) {
            val fullUser = user.copy(token = token ?: user.token)
            _currentUser.value = fullUser
            editor?.putString(KEY_USER_DATA, fullUser.toJson().toString())
            editor?.putString(AppConstants.KEY_USER_ID, fullUser.id)
            editor?.putString(AppConstants.KEY_USER_NAME, fullUser.fullName)
            editor?.putString(AppConstants.KEY_USER_EMAIL, fullUser.email)
        }
        editor?.apply()
    }

    /**
     * Restores session on app startup.
     * Verifies JWT expiration; refreshes token automatically if expired.
     */
    suspend fun restore(): Boolean {
        val currentToken = prefs?.getString(AppConstants.KEY_AUTH_TOKEN, null)
        val currentRefresh = prefs?.getString(AppConstants.KEY_REFRESH_TOKEN, null)

        if (currentToken.isNullOrBlank()) {
            return _currentUser.value != null
        }

        // Token still valid
        if (!JwtDecoder.isExpired(currentToken)) {
            return true
        }

        // Token expired: attempt refresh
        if (!currentRefresh.isNullOrBlank()) {
            val refreshRes = authApi.refreshToken(currentToken, currentRefresh)
            if (refreshRes.isSuccess && refreshRes.data != null) {
                val newAccessToken = refreshRes.data.accessToken
                val newRefreshToken = refreshRes.data.refreshToken
                prefs?.edit()
                    ?.putString(AppConstants.KEY_AUTH_TOKEN, newAccessToken)
                    ?.putString(AppConstants.KEY_REFRESH_TOKEN, newRefreshToken)
                    ?.apply()

                _currentUser.value = _currentUser.value?.copy(token = newAccessToken)
                return true
            }
        }

        // Could not restore or refresh
        clear()
        return false
    }

    fun updateProfile(fullName: String, email: String, phone: String) {
        _currentUser.value = _currentUser.value?.copy(
            fullName = fullName,
            email = email,
            phoneNumber = phone
        )?.also { updated ->
            prefs?.edit()
                ?.putString(KEY_USER_DATA, updated.toJson().toString())
                ?.putString(AppConstants.KEY_USER_NAME, updated.fullName)
                ?.putString(AppConstants.KEY_USER_EMAIL, updated.email)
                ?.apply()
        }
    }

    fun logout() {
        clear()
    }

    fun logout(context: Context?) {
        clear()
    }

    fun clear() {
        _currentUser.value = null
        prefs?.edit()
            ?.remove(KEY_USER_DATA)
            ?.remove(AppConstants.KEY_AUTH_TOKEN)
            ?.remove(AppConstants.KEY_REFRESH_TOKEN)
            ?.remove(AppConstants.KEY_USER_ID)
            ?.remove(AppConstants.KEY_USER_NAME)
            ?.remove(AppConstants.KEY_USER_EMAIL)
            ?.apply()
    }
}
