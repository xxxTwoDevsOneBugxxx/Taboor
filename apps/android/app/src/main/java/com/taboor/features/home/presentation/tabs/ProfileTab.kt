package com.taboor.features.home.presentation.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
import com.taboor.core.services.BiometricService
import com.taboor.core.services.SessionManager
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.launch

/**
 * Customer Profile / Settings Tab.
 * Pixel-perfect reproduction of Flutter's `profile_tab.dart`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTab(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val strings = LocalAppStrings.current
    val isArabic = LocaleManager.isArabic
    val currentUser by SessionManager.currentUser.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showThemeSheet by remember { mutableStateOf(false) }
    var biometricEnabled by remember { mutableStateOf(BiometricService.isEnabled) }
    val snackbarHostState = remember { SnackbarHostState() }

    val nameValue = currentUser?.fullName ?: if (isArabic) "محمد أحمد" else "Mohamed Ahmed"
    val emailValue = currentUser?.email ?: "customer@taboor.app"
    val phoneValue = currentUser?.phoneNumber ?: "01098765432"

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ==========================================
            // 1. Double-Ring Profile Header Card
            // ==========================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = AppColors.Paper,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Double-ring softTeal avatar
                    Box(
                        modifier = Modifier
                            .size(78.dp)
                            .clip(CircleShape)
                            .background(AppColors.SoftTeal)
                            .border(3.dp, Color.White, CircleShape)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                ambientColor = AppColors.Teal.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitials,
                            fontFamily = Tajawal,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppColors.Teal
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = nameValue,
                            fontFamily = Tajawal,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Email Row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppColors.SoftTeal),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MailOutline,
                                    contentDescription = null,
                                    tint = AppColors.Teal,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = emailValue,
                                fontFamily = Tajawal,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.Gray700,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Phone Row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppColors.SoftTeal),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Phone,
                                    contentDescription = null,
                                    tint = AppColors.Teal,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = phoneValue,
                                fontFamily = Tajawal,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.Gray700,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ==========================================
            // 2. Account Section (Edit Profile Card)
            // ==========================================
            SectionTitle(text = strings.profileAccount)
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick(scaleDown = 0.98f) { onNavigateToEditProfile() },
                shape = RoundedCornerShape(20.dp),
                color = AppColors.Paper,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AppColors.SoftTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PersonOutline,
                            contentDescription = null,
                            tint = AppColors.Teal,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.editProfile,
                            fontFamily = Tajawal,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Ink
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isArabic) "حدث اسمك وبريدك ورقم هاتفك" else "Update your name, email and phone",
                            fontFamily = Tajawal,
                            fontSize = 12.sp,
                            color = AppColors.Gray600
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.SoftTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null,
                            tint = AppColors.Teal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // 3. Appearance Section (Theme + Language)
            // ==========================================
            SectionTitle(text = strings.profileAppearance)
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppColors.Paper,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsGroupTile(
                        icon = Icons.Rounded.Palette,
                        title = strings.profileTheme,
                        subtitle = strings.themeSystem,
                        onClick = { showThemeSheet = true }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 64.dp, end = 14.dp),
                        thickness = 1.dp,
                        color = AppColors.Gray200
                    )
                    SettingsGroupTile(
                        icon = Icons.Rounded.Translate,
                        title = strings.profileLanguage,
                        subtitle = if (isArabic) strings.languageArabic else strings.languageEnglish,
                        onClick = { showLanguageSheet = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // 4. Security Section (Biometrics + Change Password)
            // ==========================================
            SectionTitle(text = strings.profileSecurity)
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppColors.Paper,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Biometric Switch Tile
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
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

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.profileBiometric,
                                fontFamily = Tajawal,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.Ink
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = strings.profileBiometricSubtitle,
                                fontFamily = Tajawal,
                                fontSize = 12.sp,
                                color = AppColors.Gray600
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

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 64.dp, end = 14.dp),
                        thickness = 1.dp,
                        color = AppColors.Gray200
                    )

                    SettingsGroupTile(
                        icon = Icons.Rounded.Lock,
                        title = strings.profileChangePassword,
                        subtitle = strings.profileChangePasswordSubtitle,
                        onClick = onNavigateToChangePassword
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // 5. About & Sign Out Section
            // ==========================================
            SectionTitle(text = strings.profileAbout)
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppColors.Paper,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsGroupTile(
                        icon = Icons.Rounded.Info,
                        title = strings.profileVersion,
                        subtitle = "1.0.0",
                        onClick = {}
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 64.dp, end = 14.dp),
                        thickness = 1.dp,
                        color = AppColors.Gray200
                    )
                    // Sign out tile
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick(scaleDown = 0.98f) { showSignOutDialog = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.Error.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Logout,
                                contentDescription = null,
                                tint = AppColors.Error,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = strings.profileSignOut,
                            fontFamily = Tajawal,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }

    // ==========================================
    // Bottom Sheets & Dialogs
    // ==========================================

    // Language Selection Sheet
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
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = strings.profileLanguage,
                    fontFamily = Tajawal,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Ink
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
                        color = if (isSelected) AppColors.SoftTeal else AppColors.Paper,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AppColors.Teal else AppColors.Gray200
                        )
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
                                color = if (isSelected) AppColors.Teal else AppColors.Ink
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

    // Theme Selection Sheet
    if (showThemeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showThemeSheet = false },
            containerColor = AppColors.Paper,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = strings.profileTheme,
                    fontFamily = Tajawal,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Ink
                )
                Spacer(modifier = Modifier.height(16.dp))

                val themeOptions = listOf(
                    Triple(strings.themeSystem, Icons.Rounded.BrightnessAuto, "system"),
                    Triple(strings.themeLight, Icons.Rounded.LightMode, "light"),
                    Triple(strings.themeDark, Icons.Rounded.DarkMode, "dark")
                )

                themeOptions.forEach { (label, icon, _) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .bounceClick(scaleDown = 0.98f) { showThemeSheet = false },
                        shape = RoundedCornerShape(14.dp),
                        color = AppColors.Paper,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = AppColors.Teal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = label,
                                fontFamily = Tajawal,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.Ink
                            )
                        }
                    }
                }
            }
        }
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
                    color = AppColors.Ink
                )
            },
            text = {
                Text(
                    text = strings.profileSignOutConfirm,
                    fontFamily = Tajawal,
                    fontSize = 14.sp,
                    color = AppColors.Gray700
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
                        color = AppColors.Error
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
                        color = AppColors.Gray600
                    )
                }
            },
            containerColor = AppColors.Paper,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontFamily = Tajawal,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = AppColors.Gray600,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun SettingsGroupTile(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(scaleDown = 0.985f) { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.SoftTeal),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.Teal,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = Tajawal,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Ink
            )
            subtitle?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    fontFamily = Tajawal,
                    fontSize = 12.sp,
                    color = AppColors.Gray600
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
            contentDescription = null,
            tint = AppColors.Gray400,
            modifier = Modifier.size(14.dp)
        )
    }
}
