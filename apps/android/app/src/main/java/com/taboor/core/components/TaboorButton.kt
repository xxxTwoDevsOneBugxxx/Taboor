package com.taboor.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick

enum class TaboorButtonVariant {
    PRIMARY,
    SECONDARY,
    OUTLINED
}

/**
 * Standardized Button for Taboor Android.
 * Supports Primary (Gradient or Ink), Secondary (Soft Teal), and Outlined variants.
 */
@Composable
fun TaboorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: TaboorButtonVariant = TaboorButtonVariant.PRIMARY,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    height: Dp = 52.dp,
    cornerRadius: Dp = 14.dp,
    gradient: Brush = Brush.horizontalGradient(
        listOf(AppColors.DeepTeal, AppColors.Teal)
    )
) {
    val isInteractive = enabled && !isLoading

    val baseModifier = modifier
        .fillMaxWidth()
        .heightIn(min = 48.dp, max = height)
        .height(height)

    when (variant) {
        TaboorButtonVariant.PRIMARY -> {
            Box(
                modifier = baseModifier
                    .shadow(
                        elevation = if (isInteractive) 6.dp else 0.dp,
                        shape = RoundedCornerShape(cornerRadius),
                        spotColor = AppColors.Teal.copy(alpha = 0.35f)
                    )
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(
                        if (enabled) gradient
                        else Brush.linearGradient(listOf(AppColors.Gray300, AppColors.Gray300))
                    )
                    .bounceClick(enabled = isInteractive, onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                ButtonContent(
                    text = text,
                    icon = icon,
                    textColor = if (enabled) Color.White else AppColors.Gray500,
                    isLoading = isLoading,
                    indicatorColor = Color.White
                )
            }
        }
        TaboorButtonVariant.SECONDARY -> {
            Box(
                modifier = baseModifier
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(if (enabled) AppColors.SoftTeal else AppColors.Gray100)
                    .bounceClick(enabled = isInteractive, onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                ButtonContent(
                    text = text,
                    icon = icon,
                    textColor = if (enabled) AppColors.Ink else AppColors.Gray400,
                    isLoading = isLoading,
                    indicatorColor = AppColors.Ink
                )
            }
        }
        TaboorButtonVariant.OUTLINED -> {
            Box(
                modifier = baseModifier
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(AppColors.Paper)
                    .border(
                        BorderStroke(
                            1.dp,
                            if (enabled) AppColors.Teal else AppColors.Gray300
                        ),
                        RoundedCornerShape(cornerRadius)
                    )
                    .bounceClick(enabled = isInteractive, onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                ButtonContent(
                    text = text,
                    icon = icon,
                    textColor = if (enabled) AppColors.Ink else AppColors.Gray400,
                    isLoading = isLoading,
                    indicatorColor = AppColors.Ink
                )
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    icon: ImageVector?,
    textColor: Color,
    isLoading: Boolean,
    indicatorColor: Color
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(22.dp),
            color = indicatorColor,
            strokeWidth = 2.5.dp
        )
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = text,
                fontFamily = Tajawal,
                fontSize = 15.5.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
