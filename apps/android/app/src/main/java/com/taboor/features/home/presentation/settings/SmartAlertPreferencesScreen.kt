package com.taboor.features.home.presentation.settings

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.components.TaboorTopAppBar
import com.taboor.core.localization.LocaleManager
import com.taboor.core.notifications.TaboorNotificationManager
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick
import com.taboor.features.home.data.repository.ServiceRepository
import com.taboor.features.home.domain.model.SmartAlertPreferences
import com.taboor.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartAlertPreferencesScreen(
    onNavigateBack: () -> Unit
) {
    val isArabic = LocaleManager.isArabic
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val savedPrefs by ServiceRepository.smartAlertPreferences.collectAsState()

    var customersAhead by remember(savedPrefs) { mutableIntStateOf(savedPrefs.customersAheadNotice) }
    var minutesBefore by remember(savedPrefs) { mutableIntStateOf(savedPrefs.minutesBeforeNotice) }
    var smartDeparture by remember(savedPrefs) { mutableStateOf(savedPrefs.smartDepartureNotice) }
    var timeSensitiveHaptics by remember(savedPrefs) { mutableStateOf(savedPrefs.timeSensitiveHaptics) }
    var lockScreenAlert by remember(savedPrefs) { mutableStateOf(savedPrefs.lockScreenAlert) }
    val preferences = remember(context) {
        context.getSharedPreferences("taboor_smart_alerts", android.content.Context.MODE_PRIVATE)
    }
    var selectedChime by remember {
        mutableStateOf(preferences.getString("selected_chime", "soft_marimba") ?: "soft_marimba")
    }

    var showTestBanner by remember { mutableStateOf(false) }

    fun postPreviewNotification() {
        TaboorNotificationManager.showTestNotification(context, isArabic, customersAhead, selectedChime)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) postPreviewNotification()
    }

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

    fun previewChime(chime: TurnChime) {
        MediaPlayer.create(context, chime.audioResource)?.let { player ->
            player.setOnCompletionListener { it.release() }
            player.start()
        }
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
                    .padding(bottom = 16.dp),
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
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                            color = AppColors.TextPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (count == 2) {
                                            Surface(shape = RoundedCornerShape(6.dp), color = AppColors.Teal.copy(alpha = 0.12f)) {
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
                                    Text(
                                        text = if (count == 2) {
                                            if (isArabic) "الوقت المثالي للاستعداد والدخول مباشرة" else "Ideal time to be ready at the counter"
                                        } else {
                                            if (isArabic) "يعطيك وقتاً إضافياً للوصول للمكان" else "Provides more time to arrive"
                                        },
                                        fontFamily = Tajawal,
                                        fontSize = 11.sp,
                                        color = AppColors.TextSecondary,
                                        modifier = Modifier.padding(start = 46.dp, top = 0.dp)
                                    )
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
                    subtitle = if (isArabic) "متابعة مباشرة للدور وتنبيهات اللحظة الأخيرة" else "Ongoing lock screen updates and turn alerts"
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
                                text = if (isArabic) "إشعار مستمر على شاشة القفل" else "Ongoing Lock Screen Notification",
                                    fontFamily = Tajawal,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                                Text(
                                text = if (isArabic) "متابعة تقدم التذكرة مباشرة من شاشة القفل" else "Follow live ticket progress from your lock screen",
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

                SectionBox(
                    title = if (isArabic) "نغمة نداء الدور المخصصة" else "Turn Alert Signature Chime",
                    subtitle = if (isArabic) "اختر النغمة التي تسمعها عند نداء رقمك" else "Choose the sound played when your turn is called"
                ) {
                    Column {
                        TurnChime.entries.forEachIndexed { index, chime ->
                            val selected = selectedChime == chime.key
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) AppColors.SoftTeal.copy(alpha = 0.45f) else Color.Transparent)
                                    .clickable {
                                        selectedChime = chime.key
                                        preferences.edit().putString("selected_chime", chime.key).apply()
                                        previewChime(chime)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(CircleShape)
                                        .background(if (selected) AppColors.Teal else AppColors.SoftTeal),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(chime.icon, contentDescription = null,
                                        tint = if (selected) Color.White else AppColors.Teal,
                                        modifier = Modifier.size(18.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(if (isArabic) chime.titleAr else chime.titleEn,
                                        fontFamily = Tajawal, fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                                    Text(if (isArabic) chime.subtitleAr else chime.subtitleEn,
                                        fontFamily = Tajawal, fontSize = 11.sp, color = AppColors.TextSecondary)
                                }
                                RadioButton(selected = selected, onClick = null,
                                    colors = RadioButtonDefaults.colors(selectedColor = AppColors.Teal))
                            }
                            if (index != TurnChime.entries.lastIndex) {
                                HorizontalDivider(color = AppColors.CardBorder)
                            }
                        }
                    }
                }

                // Test Alert Button
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showTestBanner = true
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            postPreviewNotification()
                        }
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

private enum class TurnChime(
    val key: String,
    val titleAr: String,
    val titleEn: String,
    val subtitleAr: String,
    val subtitleEn: String,
    val icon: ImageVector,
    val audioResource: Int
) {
    CLASSIC("modern_bell", "جرس طابور الكلاسيكي", "Classic Taboor Bell", "نغمات صاعدة واضحة للأماكن المزدحمة", "Clear ascending chime for busy areas", Icons.Rounded.NotificationsActive, R.raw.taboor_turn_chime),
    GLASS("crystal_glass", "رنين الزجاج السائل", "Liquid Glass Ping", "نقرة زجاجية نقية متطابقة مع هوية التطبيق", "Pure crystal glass ping matching Taboor UI", Icons.Rounded.WaterDrop, R.raw.taboor_glass_chime),
    MARIMBA("soft_marimba", "ماريمبا خشبية هادئة", "Warm Marimba", "رنين خشبي دافئ ومريح مناسب للعيادات", "Warm woody chime, ideal for clinics", Icons.Rounded.MusicNote, R.raw.taboor_marimba_chime),
    ZEN("zen_bell", "هدوء التأمل", "Zen Temple Bell", "رنة هادئة ومريحة للأعصاب", "Calm and peaceful meditative bell", Icons.Rounded.Spa, R.raw.taboor_zen_chime),
    DREAMY("dreamy_celestial", "نغم أثيري ناعم", "Dreamy Celestial", "كورد ناعم وخافت للمكاتب وأماكن العمل", "Ethereal gentle chord for offices", Icons.Rounded.AutoAwesome, R.raw.taboor_dreamy_chime)
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
