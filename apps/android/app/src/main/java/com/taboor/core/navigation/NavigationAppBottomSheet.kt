package com.taboor.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.localization.LocaleManager
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick

/**
 * iOS-Parity Navigation Bottom Sheet.
 * Displays navigation options (Google Maps, Waze, and System Chooser) with voice guidance cues.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationAppBottomSheet(
    destinationLat: Double,
    destinationLng: Double,
    destinationTitle: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isArabic = LocaleManager.isArabic

    val isGoogleMapsAvailable = remember { NavigationLauncher.isGoogleMapsInstalled(context) }
    val isWazeAvailable = remember { NavigationLauncher.isWazeInstalled(context) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = AppColors.Paper,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = AppColors.Gray300,
                width = 38.dp,
                height = 4.dp
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar with Gradient Squircle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AppColors.Teal, AppColors.DeepTeal)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Navigation,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "فتح الملاحة والاتجاهات" else "Open Navigation & Directions",
                        fontFamily = Tajawal,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.Ink
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isArabic)
                            "توجيه صوتي مباشر بالـ GPS إلى $destinationTitle"
                        else
                            "Turn-by-turn voice GPS navigation to $destinationTitle",
                        fontFamily = Tajawal,
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            HorizontalDivider(
                color = AppColors.Gray200,
                thickness = 0.8.dp,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // App Option 1: Google Maps (Recommended)
            NavigationOptionTile(
                title = if (isArabic) "خرائط جوجل (Google Maps)" else "Google Maps",
                subtitle = if (isArabic)
                    "توجيه صوتي ومسار قيادة مباشر مع تفادي الزحام"
                else
                    "Live voice navigation with real-time traffic updates",
                badge = if (isArabic) "الأسرع وموصى به" else "Recommended",
                icon = Icons.Rounded.DirectionsCar,
                iconTint = Color(0xFF1E88E5),
                iconBg = Color(0xFFE3F2FD),
                isAvailable = isGoogleMapsAvailable,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDismiss()
                    NavigationLauncher.open(
                        context = context,
                        app = NavigationApp.GOOGLE_MAPS,
                        latitude = destinationLat,
                        longitude = destinationLng,
                        destinationName = destinationTitle
                    )
                }
            )

            // App Option 2: Waze
            NavigationOptionTile(
                title = if (isArabic) "تطبيق ويز (Waze)" else "Waze Navigation",
                subtitle = if (isArabic)
                    "تنبيهات حية لرادارات السرعة والزحام وحركة السير"
                else
                    "Community alerts for speed traps, police & hazards",
                badge = if (isWazeAvailable) (if (isArabic) "مثبت" else "Installed") else null,
                icon = Icons.Rounded.NearMe,
                iconTint = Color(0xFF00ACC1),
                iconBg = Color(0xFFE0F7FA),
                isAvailable = isWazeAvailable,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDismiss()
                    NavigationLauncher.open(
                        context = context,
                        app = NavigationApp.WAZE,
                        latitude = destinationLat,
                        longitude = destinationLng,
                        destinationName = destinationTitle
                    )
                }
            )

            // App Option 3: System / Other Apps
            NavigationOptionTile(
                title = if (isArabic) "خرائط أخرى (System Chooser)" else "Other Map Apps",
                subtitle = if (isArabic)
                    "اختيار أي تطبيق ملاحة أو خرائط مثبت على هاتفك"
                else
                    "Choose from all compatible installed map applications",
                badge = null,
                icon = Icons.AutoMirrored.Rounded.OpenInNew,
                iconTint = AppColors.Teal,
                iconBg = AppColors.SoftTeal,
                isAvailable = true,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onDismiss()
                    NavigationLauncher.open(
                        context = context,
                        app = NavigationApp.SYSTEM_DEFAULT,
                        latitude = destinationLat,
                        longitude = destinationLng,
                        destinationName = destinationTitle
                    )
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Cancel Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick { onDismiss() },
                shape = RoundedCornerShape(16.dp),
                color = AppColors.Background,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray300)
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isArabic) "إلغاء" else "Cancel",
                        fontFamily = Tajawal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationOptionTile(
    title: String,
    subtitle: String,
    badge: String?,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    isAvailable: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(scaleDown = 0.98f) { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = AppColors.CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.CardBorder),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        fontFamily = Tajawal,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Ink
                    )

                    badge?.let { b ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = AppColors.SoftTeal
                        ) {
                            Text(
                                text = b,
                                fontFamily = Tajawal,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Teal,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    fontFamily = Tajawal,
                    fontSize = 11.5.sp,
                    color = AppColors.TextSecondary,
                    lineHeight = 15.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = AppColors.TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
