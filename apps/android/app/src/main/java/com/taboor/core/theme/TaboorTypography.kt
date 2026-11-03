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
 * Taboor Typography System
 *
 * Primary: Tajawal (Clean, humanist, modern Arabic sans-serif with high legibility)
 * Numbers / Alphanumeric: Inter (Precise tabular digits and codes)
 * Alternate: Cairo (Available for optional accentuation)
 */
val Tajawal = FontFamily(
    Font(R.font.tajawal_regular, FontWeight.Normal),
    Font(R.font.tajawal_medium, FontWeight.Medium),
    Font(R.font.tajawal_bold, FontWeight.Bold),
    Font(R.font.tajawal_extrabold, FontWeight.ExtraBold)
)

val Cairo = FontFamily(
    Font(R.font.cairo, FontWeight.Normal),
    Font(R.font.cairo, FontWeight.Medium),
    Font(R.font.cairo, FontWeight.SemiBold),
    Font(R.font.cairo, FontWeight.Bold),
    Font(R.font.cairo, FontWeight.ExtraBold)
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

object AppTextStyles {
    /**
     * Body text in Tajawal for optimal line length and comfortable readability.
     */
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

    /**
     * Headings in Tajawal for unified, smooth aesthetic.
     */
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

    /**
     * Meta labels and badges in Tajawal.
     */
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

    /**
     * Call-To-Action buttons in Tajawal.
     */
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

    /**
     * Form field labels in Tajawal.
     */
    fun fieldLabel(
        size: TextUnit = 14.sp,
        color: Color = AppColors.DeepTeal
    ) = TextStyle(
        fontFamily = Tajawal,
        fontSize = size,
        fontWeight = FontWeight.SemiBold,
        color = color
    )

    /**
     * Form hints in Tajawal.
     */
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
    // Displays & Headlines in Tajawal
    displayLarge = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Bold, fontSize = 54.sp),
    displayMedium = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Bold, fontSize = 42.sp),
    displaySmall = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Bold, fontSize = 34.sp),
    headlineLarge = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp),
    headlineMedium = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Bold, fontSize = 26.sp),
    headlineSmall = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),

    // Screen Titles and Place Card Names in Tajawal
    titleLarge = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Medium, fontSize = 14.sp),

    // Body texts, descriptions, and addresses in Tajawal
    bodyLarge = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Normal, fontSize = 12.5.sp, lineHeight = 17.sp),

    // Labels, Badges, Chips, and Buttons
    labelLarge = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Bold, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = Tajawal, fontWeight = FontWeight.Medium, fontSize = 11.sp)
)
