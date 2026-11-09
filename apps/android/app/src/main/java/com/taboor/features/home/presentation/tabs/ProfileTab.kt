package com.taboor.features.home.presentation.tabs

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
import com.taboor.core.services.BiometricService
import com.taboor.core.services.SessionManager
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Inter
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick
import com.taboor.features.home.data.repository.ServiceRepository
import kotlinx.coroutines.launch

/**
 * Customer Profile & Settings Tab (1:1 feature parity with iOS ProfileTabView).
 * Clean, borderless Material 3 UI tailored for Android.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTab(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToAlertPreferences: () -> Unit = {},
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val strings = LocalAppStrings.current
    val isArabic = LocaleManager.isArabic
    val currentUser by SessionManager.currentUser.collectAsState()
    val favoriteIds by ServiceRepository.favoritePlaceIds.collectAsState()
    val historyItems by ServiceRepository.history.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showHelpSheet by remember { mutableStateOf(false) }
    var showPrivacySheet by remember { mutableStateOf(false) }
    var showBusinessPartnerSheet by remember { mutableStateOf(false) }
    var biometricEnabled by remember { mutableStateOf(BiometricService.isEnabled) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }

    val nameValue = currentUser?.fullName?.takeIf { it.isNotBlank() } ?: if (isArabic) "محمد أحمد" else "Mohamed Ahmed"
    val emailValue = currentUser?.email?.takeIf { it.isNotBlank() } ?: "mohamed@taboor.com"
    val phoneValue = currentUser?.phoneNumber?.takeIf { it.isNotBlank() } ?: "+20 101 234 5678"

    val userInitials = remember(nameValue) {
        val parts = nameValue.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        if (parts.isEmpty()) "?"
        else {
            val first = parts.first().take(1).uppercase()
            val last = if (parts.size > 1) parts.last().take(1).uppercase() else ""
            first + last
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppColors.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = statusBarTop + 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ==========================================
            // 1. Top Header Bar (Matching iOS 1:1)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AppColors.Ink, AppColors.DeepTeal)
                            )
                        )
                        .shadow(6.dp, RoundedCornerShape(13.dp), ambientColor = AppColors.Ink.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "الإعدادات والحساب" else "Settings & Profile",
                        fontFamily = Tajawal,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        text = if (isArabic) "إدارة بياناتك، التفضيلات والأمان" else "Manage account, preferences & security",
                        fontFamily = Tajawal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // ==========================================
            // 2. User Overview Card (With verified seal & edit pill)
            // ==========================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick(scaleDown = 0.985f) { onNavigateToEditProfile() },
                shape = RoundedCornerShape(22.dp),
                color = AppColors.Paper,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Avatar Squircle with gradient
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(AppColors.Teal, AppColors.DeepTeal)
                                )
                            )
                            .shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = AppColors.Teal.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitials,
                            fontFamily = Tajawal,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    // User Info
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = nameValue,
                                fontFamily = Tajawal,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Rounded.Verified,
                                contentDescription = "Verified",
                                tint = AppColors.Teal,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = emailValue,
                            fontFamily = Tajawal,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Phone,
                                contentDescription = null,
                                tint = AppColors.Teal,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = phoneValue,
                                fontFamily = Inter,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.TextSecondary
                            )
                        }
                    }

                    // Edit Action Pill
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = AppColors.SoftTeal
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isArabic) "تعديل" else "Edit",
                                fontFamily = Tajawal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Teal
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = AppColors.Teal,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 3. Section: Activity & Places (نشاطك وطوابيرك)
            // ==========================================
            SectionContainer(title = if (isArabic) "نشاطك وطوابيرك" else "Activity & Places") {
                SettingsGroupTileWithBadge(
                    icon = Icons.Rounded.History,
                    title = if (isArabic) "سجل الزيارات والتذاكر السابقة" else "Ticket History & Past Visits",
                    subtitle = if (isArabic) "تاريخ طوابيرك والوقت الذي وفرته" else "Past queues & saved waiting time",
                    badge = "${historyItems.size}",
                    iconColor = AppColors.Teal,
                    onClick = onNavigateToHistory
                )
                RowDivider()
                SettingsGroupTileWithBadge(
                    icon = Icons.Rounded.Favorite,
                    title = if (isArabic) "الأماكن والعيادات المفضلة" else "Favorite Places & Clinics",
                    subtitle = if (isArabic) "وصول فوري وحجز سريع لأماكنك المعتادة" else "Quick access & queue booking",
                    badge = "${favoriteIds.size}",
                    iconColor = AppColors.Pink,
                    onClick = onNavigateToFavorites
                )
            }

            // ==========================================
            // 4. Section: Account Settings (إعدادات الحساب)
            // ==========================================
            SectionContainer(title = if (isArabic) "إعدادات الحساب" else "Account Settings") {
                SettingsGroupTile(
                    icon = Icons.Rounded.PersonOutline,
                    title = if (isArabic) "تعديل البيانات الشخصية" else "Edit Personal Details",
                    subtitle = if (isArabic) "الاسم، الهاتف، والبريد" else "Name, phone, and email",
                    iconColor = AppColors.Teal,
                    onClick = onNavigateToEditProfile
                )
                RowDivider()
                SettingsGroupTile(
                    icon = Icons.Rounded.Lock,
                    title = strings.profileChangePassword,
                    subtitle = if (isArabic) "تأمين وحماية الحساب" else "Account security & password",
                    iconColor = AppColors.Amber,
                    onClick = onNavigateToChangePassword
                )
            }

            // ==========================================
            // 5. Section: Preferences & Security (التفضيلات والأمان)
            // ==========================================
            SectionContainer(title = if (isArabic) "التفضيلات والأمان" else "Preferences & Security") {
                // Smart Alert Preferences
                SettingsGroupTileWithBadge(
                    icon = Icons.Rounded.Tune,
                    title = if (isArabic) "تخصيص تنبيهات الدور الذكية" else "Smart Queue Alert Preferences",
                    subtitle = if (isArabic) "توقيت الإشعار، المسافة، ووقت التحرك" else "Notice timing, distance & departure alerts",
                    badge = if (isArabic) "قبل دورك بـ 2" else "2 ahead",
                    iconColor = AppColors.Teal,
                    onClick = onNavigateToAlertPreferences
                )

                RowDivider()

                // Biometrics Switch Tile
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(AppColors.SoftTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Fingerprint,
                            contentDescription = null,
                            tint = AppColors.Teal,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.profileBiometric,
                            fontFamily = Tajawal,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isArabic) "دخول سريع وآمن دون كلمة المرور" else "Quick & secure biometric login",
                            fontFamily = Tajawal,
                            fontSize = 11.5.sp,
                            color = AppColors.TextSecondary
                        )
                    }

                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { next ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (next) {
                                if (!BiometricService.canAuthenticate(context)) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(strings.biometricUnavailable)
                                    }
                                } else {
                                    biometricEnabled = true
                                    BiometricService.enable(context)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(strings.biometricEnabled)
                                    }
                                }
                            } else {
                                biometricEnabled = false
                                BiometricService.disable(context)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(strings.biometricDisabled)
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppColors.Paper,
                            checkedTrackColor = AppColors.Teal,
                            uncheckedThumbColor = AppColors.Gray400,
                            uncheckedTrackColor = AppColors.Gray300
                        )
                    )
                }

                RowDivider()

                // Push Notifications Switch Tile
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(AppColors.Amber.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = null,
                            tint = AppColors.Amber,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) "الإشعارات المباشرة" else "Push Notifications",
                            fontFamily = Tajawal,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isArabic) "تنبيهات فورية عند اقتراب دورك" else "Instant queue and turn alerts",
                            fontFamily = Tajawal,
                            fontSize = 11.5.sp,
                            color = AppColors.TextSecondary
                        )
                    }

                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { next ->
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            notificationsEnabled = next
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppColors.Paper,
                            checkedTrackColor = AppColors.Teal,
                            uncheckedThumbColor = AppColors.Gray400,
                            uncheckedTrackColor = AppColors.Gray300
                        )
                    )
                }

                RowDivider()

                // App Language Switcher Tile
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick(scaleDown = 0.985f) { showLanguageSheet = true }
                        .padding(horizontal = 16.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(AppColors.Indigo.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Language,
                            contentDescription = null,
                            tint = AppColors.Indigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.profileLanguage,
                            fontFamily = Tajawal,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isArabic) "تغيير لغة العرض والواجهة" else "Switch interface language",
                            fontFamily = Tajawal,
                            fontSize = 11.5.sp,
                            color = AppColors.TextSecondary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = AppColors.SoftTeal
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isArabic) "العربية" else "English",
                                fontFamily = Tajawal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Teal
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = AppColors.Teal,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 6. Section: Support & About (المساعدة والمعلومات)
            // ==========================================
            SectionContainer(title = if (isArabic) "المساعدة والمعلومات" else "Support & About") {
                // Help Center & FAQ
                SettingsGroupTile(
                    icon = Icons.Rounded.HelpOutline,
                    title = if (isArabic) "مركز المساعدة والأسئلة الشائعة" else "Help Center & FAQ",
                    subtitle = if (isArabic) "إجابات على أكثر الأسئلة شيوعاً" else "Common questions & support guide",
                    iconColor = AppColors.Teal,
                    onClick = { showHelpSheet = true }
                )

                RowDivider()

                // Terms & Privacy Policy
                SettingsGroupTile(
                    icon = Icons.Rounded.Description,
                    title = if (isArabic) "الشروط وسياسة الخصوصية" else "Terms & Privacy Policy",
                    subtitle = if (isArabic) "كيف نحمي بياناتك وخصوصيتك" else "Data protection & legal terms",
                    iconColor = AppColors.Teal,
                    onClick = { showPrivacySheet = true }
                )

                RowDivider()

                // Share Taboor App
                SettingsGroupTile(
                    icon = Icons.Rounded.Share,
                    title = if (isArabic) "مشاركة تطبيق طابور" else "Share Taboor App",
                    subtitle = if (isArabic) "ادعُ أصدقاءك لتوفير وقت الانتظار" else "Invite friends & skip waiting lines",
                    iconColor = AppColors.Amber,
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                if (isArabic)
                                    "حمّل تطبيق طابور لحجز وإدارة طوابيرك بذكاء بدون انتظار: https://taboor.app"
                                else
                                    "Download Taboor app to manage and join queues smart and easily: https://taboor.app"
                            )
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(
                            sendIntent,
                            if (isArabic) "مشاركة تطبيق طابور" else "Share Taboor App"
                        )
                        context.startActivity(shareIntent)
                    }
                )
            }

            // ==========================================
            // 7. Business Partner SaaS Card (طابور للأعمال والعيادات)
            // ==========================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick(scaleDown = 0.985f) { showBusinessPartnerSheet = true },
                shape = RoundedCornerShape(20.dp),
                color = AppColors.Paper,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(AppColors.Ink, AppColors.DeepTeal)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Business,
                            contentDescription = null,
                            tint = AppColors.Amber,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isArabic) "طابور للأعمال والعيادات" else "Taboor for Business",
                                fontFamily = Tajawal,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = AppColors.Amber
                            ) {
                                Text(
                                    text = if (isArabic) "جديد" else "NEW",
                                    fontFamily = Tajawal,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (isArabic) "صاحب عيادة أو نشاط تجاري؟ انضم لطابور ونظّم طوابيرك الآن" else "Own a clinic or salon? Streamline queues & reach clients",
                            fontFamily = Tajawal,
                            fontSize = 11.5.sp,
                            color = AppColors.TextSecondary,
                            lineHeight = 16.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = AppColors.Teal,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // ==========================================
            // 8. Sign Out Card (Dedicated Red Container)
            // ==========================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick(scaleDown = 0.985f) { showSignOutDialog = true },
                shape = RoundedCornerShape(18.dp),
                color = AppColors.Paper,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(AppColors.AccentRed.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = null,
                            tint = AppColors.AccentRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        text = if (isArabic) "تسجيل الخروج من الحساب" else "Sign Out of Account",
                        fontFamily = Tajawal,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.AccentRed,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = AppColors.AccentRed.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // ==========================================
            // 9. Version Footer
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "taboor.",
                        fontFamily = Tajawal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = AppColors.Ink
                    )
                    Text(
                        text = "•",
                        color = AppColors.TextTertiary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = if (isArabic) "الإصدار 1.0.0" else "v1.0.0",
                        fontFamily = Tajawal,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextTertiary
                    )
                }

                Text(
                    text = if (isArabic) "المنصة الذكية لإدارة الطوابير والمواعيد الفورية" else "Smart Queue & Appointment Management",
                    fontFamily = Tajawal,
                    fontSize = 11.sp,
                    color = AppColors.TextTertiary
                )
            }
        }
    }

    // ==========================================
    // Bottom Sheets & Dialogs
    // ==========================================

    // Language Selection Bottom Sheet
    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            containerColor = AppColors.Paper,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = strings.profileLanguage,
                    fontFamily = Tajawal,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                val options = listOf(
                    Pair(strings.languageArabic, true),
                    Pair(strings.languageEnglish, false)
                )

                options.forEach { (label, isAr) ->
                    val isSelected = LocaleManager.isArabic == isAr
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .bounceClick(scaleDown = 0.98f) {
                                LocaleManager.setArabic(context, isAr)
                                showLanguageSheet = false
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) AppColors.SoftTeal else AppColors.Paper
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                fontFamily = Tajawal,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AppColors.Teal else AppColors.TextPrimary
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = AppColors.Teal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Help Center & FAQ Modal Sheet
    if (showHelpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHelpSheet = false },
            containerColor = AppColors.Paper,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "مركز المساعدة والدعم" else "Help & Support",
                        fontFamily = Tajawal,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    IconButton(
                        onClick = { showHelpSheet = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = AppColors.TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                FaqCard(
                    question = if (isArabic) "كيف يعمل نظام الطابور الذكي في طابور؟" else "How does the smart queue work in Taboor?",
                    answer = if (isArabic) "يمكنك حجز دورك في أي عيادة أو مركز خدمة من أي مكان. يعرض التطبيق رقم تذكرتك وعدد المنتظرين أمامك والوقت التقديري، ويرسل لك إشعاراً عندما يقترب دورك." else "You can book your ticket remotely. The app displays your live queue number, how many people are ahead, and alerts you when your turn is close."
                )

                FaqCard(
                    question = if (isArabic) "ماذا أفعل إذا تأخرت عن موعدي؟" else "What happens if I'm running late?",
                    answer = if (isArabic) "يمكنك استخدام زر 'تأجيل الدور (+10 دقائق)' في شاشة طابوري لتأخير دورك بسهولة ودون أن تفقد حجزك." else "You can tap 'Postpone (+10m)' in the My Queue tab to push your turn back without having to re-book."
                )

                FaqCard(
                    question = if (isArabic) "كيف أتواصل مع الدعم الفني؟" else "How to contact customer support?",
                    answer = if (isArabic) "فريق دعم طابور متاح 24/7 عبر البريد الإلكتروني support@taboor.app أو عبر واتساب." else "Taboor support is available 24/7 via support@taboor.app or via WhatsApp."
                )
            }
        }
    }

    // Privacy & Terms Modal Sheet
    if (showPrivacySheet) {
        ModalBottomSheet(
            onDismissRequest = { showPrivacySheet = false },
            containerColor = AppColors.Paper,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "الشروط وسياسة الخصوصية" else "Terms & Privacy Policy",
                        fontFamily = Tajawal,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    IconButton(
                        onClick = { showPrivacySheet = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = AppColors.TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AppColors.SoftTeal.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isArabic)
                            "نحن في طابور نحترم خصوصيتك بالكامل. جميع بياناتك الشخصية وبيانات الطوابير والموقع الجغرافي محمية ومشفرة وفق أعلى معايير الأمان الدولية، ولا تتم مشاركتها مع أي أطراف خارجية."
                        else
                            "At Taboor, we respect your privacy. All your personal data, queue entries, and location information are encrypted and protected following the highest global security standards.",
                        fontFamily = Tajawal,
                        fontSize = 13.sp,
                        color = AppColors.TextSecondary,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                FaqCard(
                    question = if (isArabic) "1. حماية وتشفير البيانات" else "1. Data Protection & Encryption",
                    answer = if (isArabic) "يتم تشفير جميع الاتصالات واستخدام بروتوكولات آمنة TLS 1.3 لحماية حسابك وتفاصيل حجزك." else "All communications are encrypted using TLS 1.3 to safeguard your account and queue reservations."
                )

                FaqCard(
                    question = if (isArabic) "2. إذن الموقع الجغرافي" else "2. Location Services",
                    answer = if (isArabic) "نستخدم موقعك فقط لحساب المسافة الدقيقة بينك وبين مكان الحجز، ولتنبيهك قبل الموعد بوقت كافٍ بناءً على حركة المرور." else "Location is solely used to calculate accurate driving distance and alert you in advance based on live traffic."
                )
            }
        }
    }

    // Business Partner Sheet
    if (showBusinessPartnerSheet) {
        com.taboor.features.home.presentation.partner.BusinessPartnerBottomSheet(
            onDismiss = { showBusinessPartnerSheet = false }
        )
    }

    // Sign Out Confirmation Dialog
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    text = strings.profileSignOut,
                    fontFamily = Tajawal,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
            },
            text = {
                Text(
                    text = if (isArabic) "هل أنت متأكد من رغبتك في تسجيل الخروج من حسابك؟" else "Are you sure you want to sign out of your account?",
                    fontFamily = Tajawal,
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        SessionManager.logout(context)
                        onSignOut()
                    }
                ) {
                    Text(
                        text = strings.profileSignOutOk,
                        fontFamily = Tajawal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.AccentRed
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(
                        text = strings.profileCancel,
                        fontFamily = Tajawal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextSecondary
                    )
                }
            },
            containerColor = AppColors.Paper,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun SectionContainer(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontFamily = Tajawal,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextSecondary,
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColors.Paper,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = AppColors.RowDivider
    )
}

@Composable
private fun SettingsGroupTile(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    iconColor: Color = AppColors.Teal,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(scaleDown = 0.985f) { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = Tajawal,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            subtitle?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    fontFamily = Tajawal,
                    fontSize = 11.5.sp,
                    color = AppColors.TextSecondary
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
            contentDescription = null,
            tint = AppColors.TextTertiary,
            modifier = Modifier.size(13.dp)
        )
    }
}

@Composable
private fun SettingsGroupTileWithBadge(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    iconColor: Color = AppColors.Teal,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(scaleDown = 0.985f) { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = Tajawal,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            subtitle?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    fontFamily = Tajawal,
                    fontSize = 11.5.sp,
                    color = AppColors.TextSecondary
                )
            }
        }

        if (badge != null && badge != "0") {
            Surface(
                shape = RoundedCornerShape(50),
                color = iconColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = badge,
                    fontFamily = Tajawal,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
            contentDescription = null,
            tint = AppColors.TextTertiary,
            modifier = Modifier.size(13.dp)
        )
    }
}

@Composable
private fun FaqCard(
    question: String,
    answer: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AppColors.CardBackground,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = question,
                fontFamily = Tajawal,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Text(
                text = answer,
                fontFamily = Tajawal,
                fontSize = 12.5.sp,
                color = AppColors.TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}
