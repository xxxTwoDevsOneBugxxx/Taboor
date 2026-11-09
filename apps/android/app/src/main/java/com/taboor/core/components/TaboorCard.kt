package com.taboor.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.taboor.core.theme.AppColors
import com.taboor.core.utils.bounceClick

/**
 * Standardized Card Component for Taboor Android.
 * Follows the official Warm Cream design system:
 * - Pure white paper surface on cream background.
 * - Standard 16dp rounded corner radius.
 * - Subtle border divider matching the iOS & M3 clean aesthetic.
 * - Optional micro-interaction bounce click on interaction.
 */
@Composable
fun TaboorCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = AppColors.Paper,
    contentColor: Color = AppColors.TextPrimary,
    border: BorderStroke? = BorderStroke(1.dp, AppColors.RowDivider),
    elevation: Dp = 0.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.bounceClick(enabled = enabled, onClick = onClick)
    } else {
        modifier
    }

    Surface(
        modifier = cardModifier,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        border = border,
        shadowElevation = elevation
    ) {
        Box(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}
