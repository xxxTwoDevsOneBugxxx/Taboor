package com.taboor.features.home.presentation.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.components.TaboorTopAppBar
import com.taboor.core.localization.LocaleManager
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick
import com.taboor.features.home.data.repository.ServiceRepository
import com.taboor.features.home.domain.model.SmartAlertPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartAlertPreferencesScreen(
    onNavigateBack: () -> Unit
) {
    val isArabic = LocaleManager.isArabic
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val savedPrefs by ServiceRepository.smartAlertPreferences.collectAsState()

    var customersAhead by remember(savedPrefs) { mutableIntStateOf(savedPrefs.customersAheadNotice) }
    var minutesBefore by remember(savedPrefs) { mutableIntStateOf(savedPrefs.minutesBeforeNotice) }
    var smartDeparture by remember(savedPrefs) { mutableStateOf(savedPrefs.smartDepartureNotice) }
    var timeSensitiveHaptics by remember(savedPrefs) { mutableStateOf(savedPrefs.timeSensitiveHaptics) }
    var lockScreenAlert by remember(savedPrefs) { mutableStateOf(savedPrefs.lockScreenAlert) }

    var showTestBanner by remember { mutableStateOf(false) }

    fun save() {
        val updated = SmartAlertPreferences(
            customersAheadNotice = customersAhead,
            minutesBeforeNotice = minutesBefore,
            smartDepartureNotice = smartDeparture,
            timeSensitiveHaptics = timeSensitiveHaptics,
            lockScreenAlert = lockScreenAlert
        )
        ServiceRepository.updateAlertPreferences(updated)
    }

    Scaffold(
        topBar = {
            TaboorTopAppBar(
                title = if (isArabic) "إعدادات التنبيهات الذكية" else "Smart Alert Preferences",
                subtitle = if (isArabic) "توقيت الإشعارات ووقت التحرك للدور" else "Queue notice timing & departure alerts",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = AppColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Test Feedback Banner
                AnimatedVisibility(visible = showTestBanner) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = AppColors.Teal.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Teal.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = AppColors.Teal,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) "دورك اقترب! أمامك $customersAhead فقط" else "Your turn is near! Only $customersAhead ahead",
                                    fontFamily = Tajawal,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Teal
                                )
                                Text(
                                    text = if (isArabic) "هكذا سيصلك التنبيه الذكي قبل موعدك بدقة ✨" else "This is how your smart notification will look ✨",
                                    fontFamily = Tajawal,
                                    fontSize = 11.5.sp,
                                    color = AppColors.TextSecondary
                                )
                            }
                        }
                    }
                }

                // Section 1: Turn Alert by Ahead Customers
                SectionBox(
                    title = if (isArabic) "توقيت تنبيه اقتراب الدور (بعدد الأشخاص)" else "Turn Alert by Ahead Customers",
                    subtitle = if (isArabic) "اختر متى تفضل أن نرسل لك إشعاراً قبل دخولك" else "Choose when to get notified before your turn"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2, 3, 5).forEach { count ->
                            val isSelected = customersAhead == count
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bounceClick()
                                    .clickable {
                                        customersAhead = count
                                        save()
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) AppColors.SoftTeal else AppColors.CardBackground,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) AppColors.Teal else AppColors.CardBorder
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = null,
                                            colors = RadioButtonDefaults.colors(selectedColor = AppColors.Teal)
                                        )
                                        Text(
                                            text = if (isArabic) "قبل دوري بـ $count عملاء في الانتظار" else "When $count customers are ahead",
                                            fontFamily = Tajawal,
                                            fontSize = 13.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = AppColors.TextPrimary
                                        )
                                    }

                                    if (count == 2) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = AppColors.Teal.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = if (isArabic) "موصى به" else "Recommended",
                                                fontFamily = Tajawal,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppColors.Teal,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Estimated Time Notice
                SectionBox(
                    title = if (isArabic) "تنبيه بالوقت التقديري المتبقي" else "Estimated Time Notice",
                    subtitle = if (isArabic) "تنبيه إضافي عند اقتراب موعدك زمنياً" else "Additional alert based on minutes left"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 15, 20).forEach { mins ->
                            val isSelected = minutesBefore == mins
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .bounceClick()
                                    .clickable {
                                        minutesBefore = mins
                                        save()
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AppColors.Teal else AppColors.CardBackground,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) AppColors.Teal else AppColors.CardBorder
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$mins",
                                        fontFamily = Tajawal,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else AppColors.TextPrimary
                                    )
                                    Text(
                                        text = if (isArabic) "دقائق" else "mins",
                                        fontFamily = Tajawal,
                                        fontSize = 10.5.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else AppColors.TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 3: Smart Travel & Departure
                SectionBox(
                    title = if (isArabic) "حساب المسافة ووقت التحرك الذكي" else "Smart Departure & Travel Time",
                    subtitle = if (isArabic) "تنبيهات استباقية بالخروج من المنزل أو المكتب" else "Proactive departure reminders"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.Teal.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DirectionsCar,
                                contentDescription = null,
                                tint = AppColors.Teal,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) "تنبيه وقت الانطلاق (Smart Departure)" else "Smart Departure Alert",
                                fontFamily = Tajawal,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )
                            Text(
                                text = if (isArabic)
                                    "حساب وقت القيادة وحركة المرور وتنبيهك متى تخرج لتصل قبل دورك بدقيقتين."
                                else
                                    "Calculates drive time and reminds you when to leave.",
                                fontFamily = Tajawal,
                                fontSize = 11.5.sp,
                                color = AppColors.TextSecondary,
                                lineHeight = 16.sp
                            )
                        }

                        Switch(
                            checked = smartDeparture,
                            onCheckedChange = {
                                smartDeparture = it
                                save()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AppColors.Teal)
                        )
                    }
                }

                // Section 4: Lock screen & Vibration
                SectionBox(
                    title = if (isArabic) "شاشة القفل والاهتزاز الحركي" else "Lock Screen & Haptics",
                    subtitle = if (isArabic) "متابعة الدور وتنبيهات اللحظة الأخيرة" else "Live updates on notification drawer"
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AppColors.Indigo.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Vibration,
                                    contentDescription = null,
                                    tint = AppColors.Indigo,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) "اهتزاز قوي عند اقتراب الدور" else "Haptic Alerts on Turn Near",
                                    fontFamily = Tajawal,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                                Text(
                                    text = if (isArabic) "نمط اهتزاز مخصص يضمن انتباهك حتى لو الهاتف صامت" else "Distinct vibration pattern so you don't miss your turn",
                                    fontFamily = Tajawal,
                                    fontSize = 11.5.sp,
                                    color = AppColors.TextSecondary
                                )
                            }

                            Switch(
                                checked = timeSensitiveHaptics,
                                onCheckedChange = {
                                    timeSensitiveHaptics = it
                                    save()
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AppColors.Teal)
                            )
                        }

                        HorizontalDivider(color = AppColors.CardBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AppColors.Amber.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ScreenLockPortrait,
                                    contentDescription = null,
                                    tint = AppColors.Amber,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) "إشعار مثبت في شاشة القفل" else "Ongoing Lock Screen Notification",
                                    fontFamily = Tajawal,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                                Text(
                                    text = if (isArabic) "إبقاء التذكرة محدثة مباشرة أثناء قفل الشاشة" else "Keep ticket live progress on notification center",
                                    fontFamily = Tajawal,
                                    fontSize = 11.5.sp,
                                    color = AppColors.TextSecondary
                                )
                            }

                            Switch(
                                checked = lockScreenAlert,
                                onCheckedChange = {
                                    lockScreenAlert = it
                                    save()
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = AppColors.Teal)
                            )
                        }
                    }
                }

                // Test Alert Button
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showTestBanner = true
                        coroutineScope.launch {
                            delay(4000)
                            showTestBanner = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .bounceClick(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Paper),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Teal.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayCircleOutline,
                            contentDescription = null,
                            tint = AppColors.Teal,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isArabic) "تجربة نموذج التنبيه الذكي" else "Preview Smart Alert",
                            fontFamily = Tajawal,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Teal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionBox(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = AppColors.Paper,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.CardBorder),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontFamily = Tajawal,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Ink
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = Tajawal,
                fontSize = 11.5.sp,
                color = AppColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}
