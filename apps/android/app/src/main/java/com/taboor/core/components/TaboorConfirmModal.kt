package com.taboor.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.localization.LocaleManager
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick

/**
 * Taboor Unified Confirmation Modal (Bottom Sheet).
 *
 * Provides a consistent, premium, and ergonomic confirmation experience across the entire app
 * (e.g. Clearing all alerts, Cancelling queue ticket, Signing out of account).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaboorConfirmModal(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    message: String,
    confirmText: String,
    cancelText: String? = null,
    icon: ImageVector? = null,
    isDestructive: Boolean = false,
    confirmButtonColor: Color = if (isDestructive) AppColors.AccentRed else AppColors.Teal,
    iconColor: Color = if (isDestructive) AppColors.AccentRed else AppColors.Teal,
    iconContainerColor: Color = if (isDestructive) AppColors.AccentRed.copy(alpha = 0.12f) else AppColors.SoftTeal
) {
    if (!isOpen) return

    val isArabic = LocaleManager.isArabic
    val effectiveCancelText = cancelText ?: if (isArabic) "إلغاء" else "Cancel"
    val effectiveIcon = icon ?: if (isDestructive) Icons.Rounded.DeleteOutline else null

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.Paper,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Highlight Icon Badge
            if (effectiveIcon != null) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(iconContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = effectiveIcon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 2. Title
            Text(
                text = title,
                fontFamily = Tajawal,
                fontSize = 18.5.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Message / Explanation
            Text(
                text = message,
                fontFamily = Tajawal,
                fontSize = 13.5.sp,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Primary Confirm Button
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = confirmButtonColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .bounceClick()
            ) {
                Text(
                    text = confirmText,
                    fontFamily = Tajawal,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Secondary Cancel Button
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.2.dp, AppColors.Gray200),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = AppColors.Paper),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .bounceClick()
            ) {
                Text(
                    text = effectiveCancelText,
                    fontFamily = Tajawal,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextSecondary
                )
            }
        }
    }
}
