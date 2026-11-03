package com.taboor.features.home.presentation.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.features.home.data.repository.ServiceRepository
import com.taboor.features.home.domain.model.AlertType
import com.taboor.features.home.domain.model.AppAlert
import java.util.Calendar

/**
 * Customer Smart Alerts Tab.
 * Pixel-perfect reproduction of Flutter's `alerts_tab.dart`:
 * - Grouped by day: Today (اليوم), Yesterday (أمس), Older (أقدم)
 * - Specific icon containers per alert type with brand accents
 * - Relative time formatting (الآن، منذ 55 دقيقة، منذ 3 ساعات)
 */
@Composable
fun AlertsTab() {
    val strings = LocalAppStrings.current
    val isArabic = LocaleManager.isArabic
    val alerts by ServiceRepository.alerts.collectAsState()

    val unreadCount = remember(alerts) { alerts.count { !it.read } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        // ==========================================
        // Header
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.navAlerts,
                fontFamily = Tajawal,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Ink
            )
            if (unreadCount > 0) {
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppColors.Teal.copy(alpha = 0.14f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$unreadCount ${strings.alertNewBadge}",
                        fontFamily = Tajawal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.Teal
                    )
                }
            }
        }

        if (alerts.isEmpty()) {
            // Empty State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(AppColors.SoftTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsNone,
                        contentDescription = null,
                        tint = AppColors.Teal,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = strings.alertsEmptyTitle,
                    fontFamily = Tajawal,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Ink,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = strings.alertsEmptySubtitle,
                    fontFamily = Tajawal,
                    fontSize = 14.sp,
                    color = AppColors.Gray600,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Group alerts by day
            val todayCal = Calendar.getInstance()
            val groups = remember(alerts, isArabic) {
                val map = LinkedHashMap<String, MutableList<AppAlert>>()
                for (alert in alerts) {
                    val alertCal = Calendar.getInstance().apply { time = alert.timestamp }
                    val sameDay = alertCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                            alertCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
                    val sameYesterday = alertCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                            alertCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR) - 1

                    val groupName = when {
                        sameDay -> strings.alertToday
                        sameYesterday -> strings.alertYesterday
                        else -> strings.alertOlder
                    }
                    map.getOrPut(groupName) { mutableListOf() }.add(alert)
                }
                map
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 110.dp)
            ) {
                groups.forEach { (groupName, groupAlerts) ->
                    item {
                        Text(
                            text = groupName,
                            fontFamily = Tajawal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppColors.Gray500,
                            modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
                        )
                    }

                    items(groupAlerts, key = { it.id }) { alert ->
                        AlertCardItem(
                            alert = alert,
                            isArabic = isArabic,
                            onDismiss = { ServiceRepository.dismissAlert(alert.id) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertCardItem(
    alert: AppAlert,
    isArabic: Boolean,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current

    val accent = when (alert.type) {
        AlertType.JOIN -> AppColors.Teal
        AlertType.OPEN -> AppColors.Success
        AlertType.TURN -> AppColors.Gray500
        AlertType.GO -> AppColors.Amber
        AlertType.THANKS -> Color(0xFFE57373)
    }

    val icon: ImageVector = when (alert.type) {
        AlertType.JOIN -> Icons.Rounded.ConfirmationNumber
        AlertType.OPEN -> Icons.Rounded.Storefront
        AlertType.TURN -> Icons.Rounded.HourglassTop
        AlertType.GO -> Icons.Rounded.DirectionsWalk
        AlertType.THANKS -> Icons.Rounded.Favorite
    }

    val title = when (alert.type) {
        AlertType.JOIN -> strings.alertQueueJoin
        AlertType.OPEN -> strings.alertQueueOpen
        AlertType.TURN -> strings.alertTurnStarted
        AlertType.GO -> strings.alertTimeToGo
        AlertType.THANKS -> strings.alertThankYou
    }

    val body = when (alert.type) {
        AlertType.JOIN -> if (isArabic) "انضممت إلى طابور ${alert.service} برقم ${alert.number}." else "Joined ${alert.service} queue as ${alert.number}."
        AlertType.OPEN -> if (isArabic) "فتح ${alert.service} أبوابه الآن وبدأ استقبال العملاء." else "${alert.service} is now open and accepting customers."
        AlertType.TURN -> if (isArabic) "بدأ دور التذكرة ${alert.number} في ${alert.service}!" else "Ticket ${alert.number} is being served at ${alert.service}!"
        AlertType.GO -> if (isArabic) "حان وقت التحرك نحو ${alert.service} لتصل في موعدك." else "Time to head towards ${alert.service} to arrive on time."
        AlertType.THANKS -> if (isArabic) "شكراً لزيارتك ${alert.service}! نتمنى لك يوماً سعيداً." else "Thank you for visiting ${alert.service}! Have a wonderful day."
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppColors.Paper,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(accent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontFamily = Tajawal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Ink
                    )
                    Text(
                        text = alert.timeAgo,
                        fontFamily = Tajawal,
                        fontSize = 11.sp,
                        color = AppColors.Gray500
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = body,
                    fontFamily = Tajawal,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = AppColors.Gray600
                )
            }
        }
    }
}
