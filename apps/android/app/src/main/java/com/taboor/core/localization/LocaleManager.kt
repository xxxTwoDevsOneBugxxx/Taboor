package com.taboor.core.localization

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LocaleManager {
    private const val PREFS_NAME = "taboor_prefs"
    private const val KEY_LANGUAGE = "app_language"
    private const val KEY_FIRST_TIME = "is_first_time"

    const val ARABIC = "ar"
    const val ENGLISH = "en"

    private var sharedPreferences: SharedPreferences? = null

    private val _currentLanguage = MutableStateFlow(ARABIC)
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _isFirstTime = MutableStateFlow(true)
    val isFirstTime: StateFlow<Boolean> = _isFirstTime.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences = prefs
        val lang = prefs.getString(KEY_LANGUAGE, ARABIC) ?: ARABIC
        _currentLanguage.value = lang
        _isFirstTime.value = prefs.getBoolean(KEY_FIRST_TIME, true)
    }

    val isArabic: Boolean
        get() = _currentLanguage.value == ARABIC

    val layoutDirection: LayoutDirection
        get() = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    val currentStrings: AppStringsData
        get() = if (isArabic) ArabicStrings else EnglishStrings

    fun toggleLanguage() {
        val next = if (isArabic) ENGLISH else ARABIC
        setLanguage(next)
    }

    fun setLanguage(language: String) {
        _currentLanguage.value = language
        sharedPreferences?.edit()?.putString(KEY_LANGUAGE, language)?.apply()
    }

    fun setFirstTime(firstTime: Boolean) {
        _isFirstTime.value = firstTime
        sharedPreferences?.edit()?.putBoolean(KEY_FIRST_TIME, firstTime)?.apply()
    }
}
