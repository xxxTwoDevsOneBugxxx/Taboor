package com.taboor.core.services

import android.app.KeyguardManager
import android.content.Context
import android.content.SharedPreferences
import com.taboor.core.constants.AppConstants

/**
 * Android counterpart to Flutter's BiometricService.
 * Manages biometric unlock preference and device security capability checks.
 */
object BiometricService {

    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Whether biometric unlock is enabled in app settings.
     */
    var isEnabled: Boolean
        get() = prefs?.getBoolean(KEY_BIOMETRIC_ENABLED, true) ?: true
        set(value) {
            prefs?.edit()?.putBoolean(KEY_BIOMETRIC_ENABLED, value)?.apply()
        }

    /**
     * Checks if this device has secure lockscreen configured (PIN, pattern, fingerprint, or face).
     */
    fun isAvailable(context: Context): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return keyguardManager?.isDeviceSecure == true
    }

    fun canAuthenticate(context: Context): Boolean = isAvailable(context)

    fun enable() {
        isEnabled = true
    }

    fun enable(context: Context?) {
        enable()
    }

    fun disable() {
        isEnabled = false
    }

    fun disable(context: Context?) {
        disable()
    }
}
