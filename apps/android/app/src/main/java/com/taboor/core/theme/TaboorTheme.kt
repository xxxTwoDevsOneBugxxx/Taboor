package com.taboor.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import com.taboor.core.localization.ArabicStrings
import com.taboor.core.localization.EnglishStrings
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager

private val TaboorLightColorScheme = lightColorScheme(
    primary = AppColors.Ink,
    onPrimary = AppColors.PrimaryWhite,
    primaryContainer = AppColors.SoftTeal,
    onPrimaryContainer = AppColors.DeepTeal,
    secondary = AppColors.Teal,
    onSecondary = AppColors.PrimaryWhite,
    tertiary = AppColors.Accent,
    background = AppColors.Background,
    surface = AppColors.CardBackground,
    surfaceVariant = AppColors.SubtleFill,
    onBackground = AppColors.TextPrimary,
    onSurface = AppColors.TextPrimary,
    onSurfaceVariant = AppColors.TextSecondary,
    outline = AppColors.CardBorder,
    error = AppColors.Error,
    onError = AppColors.PrimaryWhite
)

@Composable
fun TaboorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val currentLang by LocaleManager.currentLanguage.collectAsState()
    val isArabic = currentLang == LocaleManager.ARABIC
    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    val strings = if (isArabic) ArabicStrings else EnglishStrings

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalAppStrings provides strings,
        LocalTextStyle provides TextStyle(
            fontFamily = if (isArabic) Tajawal else Inter,
            color = AppColors.TextPrimary
        )
    ) {
        MaterialTheme(
            colorScheme = TaboorLightColorScheme,
            typography = TaboorTypography,
            content = content
        )
    }
}
