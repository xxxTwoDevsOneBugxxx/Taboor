package com.taboor.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal

enum class TaboorStatusType {
    SUCCESS, // Green (e.g. مفتوح الآن, مكتمل)
    WARNING, // Amber (e.g. في الانتظار, جاري خدمتك)
    ERROR,   // Red (e.g. مغلق حالياً, ملغي)
    INFO,    // Blue/Teal (e.g. موعد مسبق, تنبيه)
    NEUTRAL  // Gray
}

/**
 * Standardized status badge / chip for Taboor Android.
 * Follows the 8dp corner radius rule and provides semantic tints with optimal legibility.
 */
@Composable
fun TaboorStatusChip(
    text: String,
    modifier: Modifier = Modifier,
    type: TaboorStatusType = TaboorStatusType.INFO,
    showDot: Boolean = true,
    height: Dp = 26.dp
) {
    val (bgColor, textColor, dotColor) = when (type) {
        TaboorStatusType.SUCCESS -> Triple(
            AppColors.Success.copy(alpha = 0.12f),
            Color(0xFF2E7D32),
            AppColors.Success
        )
        TaboorStatusType.WARNING -> Triple(
            AppColors.Amber.copy(alpha = 0.14f),
            Color(0xFFC67C00),
            AppColors.Amber
        )
        TaboorStatusType.ERROR -> Triple(
            AppColors.Error.copy(alpha = 0.12f),
            Color(0xFFC62828),
            AppColors.Error
        )
        TaboorStatusType.INFO -> Triple(
            AppColors.SoftTeal,
            AppColors.Ink,
            AppColors.Teal
        )
        TaboorStatusType.NEUTRAL -> Triple(
            AppColors.Gray200,
            AppColors.Gray600,
            AppColors.Gray500
        )
    }

    Row(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(0.5.dp, textColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
        Text(
            text = text,
            fontFamily = Tajawal,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
