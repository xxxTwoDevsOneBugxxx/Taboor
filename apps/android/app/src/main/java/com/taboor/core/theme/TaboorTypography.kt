package com.taboor.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.taboor.R

/**
 * Taboor Fonts & Text Styles
 * Directly mirrored from Flutter: `lib/core/themes/app_text_styles.dart`
 */
val Tajawal = FontFamily(
    Font(R.font.tajawal_regular, FontWeight.Normal),
    Font(R.font.tajawal_medium, FontWeight.Medium),
    Font(R.font.tajawal_bold, FontWeight.Bold),
    Font(R.font.tajawal_extrabold, FontWeight.ExtraBold)
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

object AppTextStyles {
    fun body(
        size: TextUnit = 14.sp,
        weight: FontWeight = FontWeight.Normal,
        color: Color? = null,
        lineHeight: TextUnit = TextUnit.Unspecified
    ) = TextStyle(
        fontFamily = Tajawal,
        fontSize = size,
        fontWeight = weight,
        color = color ?: AppColors.DeepTeal,
        lineHeight = lineHeight
    )

    fun heading(
        size: TextUnit = 18.sp,
        weight: FontWeight = FontWeight.Bold,
        color: Color = AppColors.Primary
    ) = TextStyle(
        fontFamily = Tajawal,
        fontSize = size,
        fontWeight = weight,
        color = color
    )

    fun label(
        size: TextUnit = 12.sp,
        weight: FontWeight = FontWeight.Medium,
        color: Color? = null
    ) = TextStyle(
        fontFamily = Tajawal,
        fontSize = size,
        fontWeight = weight,
        color = color ?: AppColors.Gray600
    )

    fun button(
        size: TextUnit = 15.sp,
        weight: FontWeight = FontWeight.Bold,
        color: Color = AppColors.PrimaryWhite
    ) = TextStyle(
        fontFamily = Tajawal,
        fontSize = size,
        fontWeight = weight,
        color = color
    )

    fun fieldLabel(
        size: TextUnit = 14.sp,
        color: Color = AppColors.DeepTeal
    ) = TextStyle(
        fontFamily = Tajawal,
        fontSize = size,
        fontWeight = FontWeight.SemiBold,
        color = color
    )

    fun fieldHint(
        size: TextUnit = 13.sp,
        color: Color = AppColors.Gray400
    ) = TextStyle(
        fontFamily = Tajawal,
        fontSize = size,
        fontWeight = FontWeight.Medium,
        color = color
    )
}

val TaboorTypography = Typography(
    displayLarge = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Bold, fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Bold, fontSize = 45.sp),
    displaySmall = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Bold, fontSize = 36.sp),
    headlineLarge = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Medium, fontSize = 11.sp)
)
