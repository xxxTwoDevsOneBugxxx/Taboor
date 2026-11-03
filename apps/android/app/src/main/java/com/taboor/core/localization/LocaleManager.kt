package com.taboor.core.localization

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.unit.LayoutDirection
import com.taboor.core.constants.AppConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LocaleManager {
    const val ARABIC = AppConstants.LANG_AR
    const val ENGLISH = AppConstants.LANG_EN

    private var sharedPreferences: SharedPreferences? = null

    private val _currentLanguage = MutableStateFlow(AppConstants.DEFAULT_LANG)
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _isFirstTime = MutableStateFlow(true)
    val isFirstTime: StateFlow<Boolean> = _isFirstTime.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences = prefs
        val lang = prefs.getString(AppConstants.KEY_APP_LANGUAGE, AppConstants.DEFAULT_LANG) ?: AppConstants.DEFAULT_LANG
        _currentLanguage.value = lang
        _isFirstTime.value = prefs.getBoolean(AppConstants.KEY_IS_FIRST_TIME, true)
    }

    val isArabic: Boolean
        get() = _currentLanguage.value == AppConstants.LANG_AR

    val layoutDirection: LayoutDirection
        get() = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    val currentStrings: AppStringsData
        get() = if (isArabic) ArabicStrings else EnglishStrings

    fun toggleLanguage() {
        val next = if (isArabic) AppConstants.LANG_EN else AppConstants.LANG_AR
        setLanguage(next)
    }

    fun setLanguage(language: String) {
        _currentLanguage.value = language
        sharedPreferences?.edit()?.putString(AppConstants.KEY_APP_LANGUAGE, language)?.apply()
    }

    fun setArabic(isArabic: Boolean) {
        setLanguage(if (isArabic) ARABIC else ENGLISH)
    }

    fun setArabic(context: Context?, isArabic: Boolean) {
        setArabic(isArabic)
    }

    fun setFirstTime(firstTime: Boolean) {
        _isFirstTime.value = firstTime
        sharedPreferences?.edit()?.putBoolean(AppConstants.KEY_IS_FIRST_TIME, firstTime)?.apply()
    }
}
