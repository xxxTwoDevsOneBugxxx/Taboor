package com.taboor.features.home.presentation.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick
import com.taboor.core.utils.pulseEffect
import com.taboor.features.home.data.repository.ServiceRepository
import com.taboor.features.home.domain.model.AlertType
import com.taboor.features.home.domain.model.AppAlert

/**
 * Customer Smart Alerts & Notifications Tab.
 * Redesigned native Android implementation with 100% parity with iOS `AlertsTabView.swift`:
 * - Top Custom Header Bar with Squircle Gradient Brand Icon & Dynamic Action Pill
 * - Filter Segmented Chips Bar (All, Unread, Queue Status, Updates)
 * - Rich Alert Cards with Brand Category Tags, Relative Time & Unread Indicators
 * - Apple-style Empty State & Clear-All Confirmation Dialog
 */
@Composable
fun AlertsTab() {
    val haptic = LocalHapticFeedback.current
    val strings = LocalAppStrings.current
    val isArabic = LocaleManager.isArabic
    val alerts by ServiceRepository.alerts.collectAsState()

    var filterIndex by remember { mutableIntStateOf(0) } // 0: All, 1: Unread, 2: Queue Status, 3: Updates
    var showClearConfirm by remember { mutableStateOf(false) }

    val unreadCount = remember(alerts) { alerts.count { !it.read } }
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val filteredAlerts = remember(alerts, filterIndex) {
        when (filterIndex) {
            1 -> alerts.filter { !it.read }
            2 -> alerts.filter { it.type == AlertType.TURN || it.type == AlertType.JOIN || it.type == AlertType.GO }
            3 -> alerts.filter { it.type == AlertType.OPEN || it.type == AlertType.THANKS }
            else -> alerts
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = statusBarTop + 14.dp,
                start = 20.dp,
                end = 20.dp,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ==========================================
            // 1. Top Custom Header Bar
            // ==========================================
            item(key = "header_bar") {
                AlertsTopHeader(
                    isArabic = isArabic,
                    alertsCount = alerts.size,
                    unreadCount = unreadCount,
                    onMarkAllRead = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        ServiceRepository.markAllAlertsRead()
                    },
                    onClearAllClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showClearConfirm = true
                    }
                )
            }

            // ==========================================
            // 2. Filter Segmented Chips Bar
            // ==========================================
            item(key = "filter_chips") {
                AlertsFilterChipsBar(
                    isArabic = isArabic,
                    totalCount = alerts.size,
                    unreadCount = unreadCount,
                    selectedIndex = filterIndex,
                    onSelectFilter = { index ->
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        filterIndex = index
                    }
                )
            }

            // ==========================================
            // 3. Alerts Content List OR Empty State
            // ==========================================
            if (filteredAlerts.isEmpty()) {
                item(key = "empty_state") {
                    Spacer(modifier = Modifier.height(10.dp))
                    EmptyAlertsCard(isArabic = isArabic)
                }
            } else {
                items(filteredAlerts, key = { it.id }) { alert ->
                    AlertCardItem(
                        alert = alert,
                        isArabic = isArabic,
                        onClick = {
                            if (!alert.read) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                ServiceRepository.markAlertRead(alert.id)
                            }
                        },
                        onDelete = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            ServiceRepository.dismissAlert(alert.id)
                        }
                    )
                }
            }
        }
    }

    // ==========================================
    // Clear All Confirmation Dialog
    // ==========================================
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = {
                Text(
                    text = if (isArabic) "مسح جميع التنبيهات" else "Clear All Alerts",
                    fontFamily = Tajawal,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isArabic)
                        "هل أنت متأكد من مسح جميع التنبيهات؟ لن تتمكن من استعادتها."
                    else
                        "Are you sure you want to clear all alerts? This cannot be undone.",
                    fontFamily = Tajawal,
                    color = AppColors.TextSecondary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirm = false
                        ServiceRepository.clearAllAlerts()
                    }
                ) {
                    Text(
                        text = if (isArabic) "مسح الكل" else "Clear All",
                        color = AppColors.Error,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Tajawal
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(
                        text = if (isArabic) "إلغاء" else "Cancel",
                        color = AppColors.TextSecondary,
                        fontFamily = Tajawal
                    )
                }
            },
            containerColor = AppColors.Paper,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// ==========================================
// Sub-component: Top Header Bar
// ==========================================
@Composable
private fun AlertsTopHeader(
    isArabic: Boolean,
    alertsCount: Int,
    unreadCount: Int,
    onMarkAllRead: () -> Unit,
    onClearAllClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Squircle Brand Gradient Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AppColors.Ink, AppColors.DeepTeal)
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(13.dp))
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsActive,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }

            // Title & Subtitle with Badge
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isArabic) "التنبيهات" else "Alerts",
                        fontFamily = Tajawal,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.TextPrimary
                    )

                    if (unreadCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = AppColors.Amber
                        ) {
                            Text(
                                text = "$unreadCount",
                                fontFamily = Tajawal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (isArabic) "إشعارات دورك وتحديثات الأماكن" else "Turn updates & place notifications",
                    fontFamily = Tajawal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Dynamic Action Button (Mark Read OR Clear Trash)
        if (unreadCount > 0) {
            Surface(
                shape = RoundedCornerShape(50),
                color = AppColors.Paper,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .border(1.dp, AppColors.Teal.copy(alpha = 0.25f), RoundedCornerShape(50))
                    .bounceClick { onMarkAllRead() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = AppColors.Teal,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (isArabic) "تحديد كمقروء" else "Mark Read",
                        fontFamily = Tajawal,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Teal
                    )
                }
            }
        } else if (alertsCount > 0) {
            Surface(
                shape = CircleShape,
                color = AppColors.Paper,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .size(36.dp)
                    .border(0.5.dp, AppColors.RowDivider, CircleShape)
                    .bounceClick { onClearAllClick() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Clear All",
                        tint = AppColors.TextTertiary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// Sub-component: Filter Segmented Chips Bar
// ==========================================
@Composable
private fun AlertsFilterChipsBar(
    isArabic: Boolean,
    totalCount: Int,
    unreadCount: Int,
    selectedIndex: Int,
    onSelectFilter: (Int) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChipPill(
            index = 0,
            label = if (isArabic) "الكل ($totalCount)" else "All ($totalCount)",
            isSelected = selectedIndex == 0,
            onClick = { onSelectFilter(0) }
        )

        FilterChipPill(
            index = 1,
            label = if (isArabic) "غير مقروءة ($unreadCount)" else "Unread ($unreadCount)",
            isSelected = selectedIndex == 1,
            onClick = { onSelectFilter(1) }
        )

        FilterChipPill(
            index = 2,
            label = if (isArabic) "حالة الدور" else "Queue Status",
            isSelected = selectedIndex == 2,
            onClick = { onSelectFilter(2) }
        )

        FilterChipPill(
            index = 3,
            label = if (isArabic) "تحديثات" else "Updates",
            isSelected = selectedIndex == 3,
            onClick = { onSelectFilter(3) }
        )
    }
}

@Composable
private fun FilterChipPill(
    index: Int,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isSelected) AppColors.Ink else AppColors.Paper,
        shadowElevation = if (isSelected) 1.5.dp else 0.5.dp,
        modifier = Modifier
            .then(
                if (!isSelected) {
                    Modifier.border(0.5.dp, AppColors.RowDivider, RoundedCornerShape(50))
                } else Modifier
            )
            .bounceClick { onClick() }
    ) {
        Text(
            text = label,
            fontFamily = Tajawal,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else AppColors.TextSecondary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

// ==========================================
// Sub-component: Alert Card Item
// ==========================================
@Composable
private fun AlertCardItem(
    alert: AppAlert,
    isArabic: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val accentColor = when (alert.type) {
        AlertType.TURN -> AppColors.Amber
        AlertType.JOIN -> AppColors.Teal
        AlertType.GO -> AppColors.AccentGreen
        AlertType.OPEN -> AppColors.Indigo
        AlertType.THANKS -> AppColors.Teal
    }

    val iconBgColor = when (alert.type) {
        AlertType.TURN -> AppColors.Amber.copy(alpha = 0.12f)
        AlertType.JOIN -> AppColors.SoftTeal
        AlertType.GO -> AppColors.AccentGreen.copy(alpha = 0.12f)
        AlertType.OPEN -> AppColors.Indigo.copy(alpha = 0.12f)
        AlertType.THANKS -> AppColors.SoftTeal
    }

    val iconVector: ImageVector = when (alert.type) {
        AlertType.TURN -> Icons.Rounded.HourglassTop
        AlertType.JOIN -> Icons.Rounded.ConfirmationNumber
        AlertType.GO -> Icons.Rounded.CheckCircle
        AlertType.OPEN -> Icons.Rounded.Storefront
        AlertType.THANKS -> Icons.Rounded.Stars
    }

    val categoryTag = when (alert.type) {
        AlertType.TURN -> if (isArabic) "اقتراب الدور" else "Turn Alert"
        AlertType.JOIN -> if (isArabic) "تأكيد الحجز" else "Reservation"
        AlertType.GO -> if (isArabic) "انتهاء الزيارة" else "Completed"
        AlertType.OPEN -> if (isArabic) "فتح الطابور" else "Queue Open"
        AlertType.THANKS -> if (isArabic) "تقييم وتجربة" else "Feedback"
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = AppColors.Paper,
        shadowElevation = if (!alert.read) 2.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!alert.read) {
                    Modifier.border(1.dp, AppColors.Amber.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                } else {
                    Modifier.border(0.5.dp, AppColors.RowDivider, RoundedCornerShape(18.dp))
                }
            )
            .bounceClick { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Leading Squircle Icon Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Alert Details Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Title + Unread Pulse Dot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = alert.localizedTitle(isArabic),
                        fontFamily = Tajawal,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary,
                        maxLines = 2,
                        lineHeight = 19.sp,
                        modifier = Modifier.weight(1f)
                    )

                    if (!alert.read) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AppColors.Amber)
                                .pulseEffect()
                        )
                    }
                }

                // Description Body
                val desc = alert.localizedDesc(isArabic)
                if (desc.isNotBlank()) {
                    Text(
                        text = desc,
                        fontFamily = Tajawal,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = AppColors.TextSecondary,
                        lineHeight = 17.5.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Footer Row: Relative Time & Category Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = AppColors.TextTertiary,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = alert.localizedTimeAgo(isArabic),
                            fontFamily = Tajawal,
                            fontSize = 11.sp,
                            color = AppColors.TextTertiary
                        )
                    }

                    Text("•", color = AppColors.TextTertiary, fontSize = 9.sp)

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = AppColors.SoftTeal
                    ) {
                        Text(
                            text = categoryTag,
                            fontFamily = Tajawal,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Teal,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// Sub-component: Empty Alerts Card
// ==========================================
@Composable
private fun EmptyAlertsCard(isArabic: Boolean) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = AppColors.Paper,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, AppColors.RowDivider, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 34.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(AppColors.SoftTeal),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsOff,
                    contentDescription = null,
                    tint = AppColors.Teal,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = if (isArabic) "لا توجد تنبيهات جديدة" else "No Alerts Found",
                    fontFamily = Tajawal,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isArabic)
                        "ستظهر هنا إشعارات دورك عند اقترابه وتحديثات الأماكن المحجوزة"
                    else
                        "Turn alerts and updates from places you joined will appear here",
                    fontFamily = Tajawal,
                    fontSize = 12.5.sp,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
