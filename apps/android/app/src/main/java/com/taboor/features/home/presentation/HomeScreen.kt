package com.taboor.features.home.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.features.home.domain.model.ServiceCategory
import com.taboor.features.home.domain.model.ServicePlace
import com.taboor.features.home.presentation.tabs.AlertsTab
import com.taboor.features.home.presentation.tabs.HomeTab
import com.taboor.features.home.presentation.tabs.ProfileTab
import com.taboor.features.home.presentation.tabs.QueueTab

enum class HomeBottomTab {
    HOME,
    QUEUE,
    ALERTS,
    PROFILE
}

@Composable
fun HomeScreen(
    selectedTab: HomeBottomTab = HomeBottomTab.HOME,
    onTabChange: (HomeBottomTab) -> Unit = {},
    onNavigateToDetails: (ServicePlace) -> Unit,
    onNavigateToAllServices: (ServiceCategory?) -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToAlertPreferences: () -> Unit = {},
    onNavigateToLiveTracking: () -> Unit = {},
    onOpenQrScanner: () -> Unit,
    onSignOut: () -> Unit
) {
    // Intercept back button when not on HOME tab: navigate back to HOME
    BackHandler(enabled = selectedTab != HomeBottomTab.HOME) {
        onTabChange(HomeBottomTab.HOME)
    }

    Scaffold(
        containerColor = AppColors.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Smooth tab transition matching Flutter _FadeIndexedStack
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) togetherWith
                            fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
                },
                label = "HomeTabTransition"
            ) { tab ->
                when (tab) {
                    HomeBottomTab.HOME -> HomeTab(
                        onNavigateToDetails = onNavigateToDetails,
                        onNavigateToAllServices = onNavigateToAllServices,
                        onNavigateToQueueTab = { onTabChange(HomeBottomTab.QUEUE) },
                        onOpenQrScanner = onOpenQrScanner,
                        onOpenLiveTracking = onNavigateToLiveTracking
                    )
                    HomeBottomTab.QUEUE -> QueueTab(
                        onExploreServices = { onTabChange(HomeBottomTab.HOME) },
                        onOpenLiveTracking = onNavigateToLiveTracking
                    )
                    HomeBottomTab.ALERTS -> AlertsTab()
                    HomeBottomTab.PROFILE -> ProfileTab(
                        onNavigateToEditProfile = onNavigateToEditProfile,
                        onNavigateToChangePassword = onNavigateToChangePassword,
                        onNavigateToHistory = onNavigateToHistory,
                        onNavigateToFavorites = onNavigateToFavorites,
                        onNavigateToAlertPreferences = onNavigateToAlertPreferences,
                        onSignOut = onSignOut
                    )
                }
            }

            // Floating Capsule Bottom Navigation Bar with sliding active indicator
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                FloatingCapsuleNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = onTabChange
                )
            }
        }
    }
}

/**
 * Floating Capsule Navigation Bar with Smooth Spring Sliding Active Indicator
 * Updated with the new clean colors (Ink / TextTertiary), Tajawal typography,
 * amber active indicator bar, and alert badge dot.
 */
@Composable
private fun FloatingCapsuleNavBar(
    selectedTab: HomeBottomTab,
    onTabSelected: (HomeBottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val tabs = remember { listOf(HomeBottomTab.HOME, HomeBottomTab.QUEUE, HomeBottomTab.ALERTS, HomeBottomTab.PROFILE) }
    val selectedIndex = tabs.indexOf(selectedTab)
    val navShape = remember { RoundedCornerShape(32.dp) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
            .shadow(
                elevation = 20.dp,
                shape = navShape,
                spotColor = AppColors.Ink.copy(alpha = 0.22f),
                ambientColor = AppColors.Ink.copy(alpha = 0.12f)
            )
            .border(
                width = 1.dp,
                color = AppColors.Ink.copy(alpha = 0.10f),
                shape = navShape
            ),
        shape = navShape,
        color = Color.White.copy(alpha = 0.96f),
        tonalElevation = 0.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            val totalWidth = maxWidth
            val itemWidth = totalWidth / 4

            // BiasAlignment: -1f is Start (Right in RTL, Left in LTR), +1f is End
            val targetBias = when (selectedIndex) {
                0 -> -1f
                1 -> -0.333f
                2 -> 0.333f
                3 -> 1f
                else -> -1f
            }

            val animatedBias by animateFloatAsState(
                targetValue = targetBias,
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = 380f
                ),
                label = "navPillSlide"
            )

            // Smoothly sliding active indicator bar (Amber bar, no background pill)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(itemWidth)
                    .align(BiasAlignment(animatedBias, 0f))
            ) {
                // Amber active indicator bar at the bottom of the active tab
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .size(width = 16.dp, height = 3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(AppColors.Amber)
                )
            }

            // Interactive Navigation Item Buttons
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavBarItem(
                    label = strings.navHome,
                    iconOutline = Icons.Outlined.Home,
                    iconFilled = Icons.Rounded.Home,
                    isSelected = selectedTab == HomeBottomTab.HOME,
                    onClick = { onTabSelected(HomeBottomTab.HOME) },
                    modifier = Modifier.weight(1f)
                )
                NavBarItem(
                    label = "طوابيري",
                    iconOutline = Icons.Outlined.ConfirmationNumber,
                    iconFilled = Icons.Rounded.ConfirmationNumber,
                    isSelected = selectedTab == HomeBottomTab.QUEUE,
                    onClick = { onTabSelected(HomeBottomTab.QUEUE) },
                    modifier = Modifier.weight(1f)
                )
                NavBarItem(
                    label = strings.navAlerts,
                    iconOutline = Icons.Outlined.Notifications,
                    iconFilled = Icons.Rounded.Notifications,
                    isSelected = selectedTab == HomeBottomTab.ALERTS,
                    onClick = { onTabSelected(HomeBottomTab.ALERTS) },
                    hasAlertBadge = true,
                    modifier = Modifier.weight(1f)
                )
                NavBarItem(
                    label = strings.navProfile,
                    iconOutline = Icons.Outlined.Person,
                    iconFilled = Icons.Rounded.Person,
                    isSelected = selectedTab == HomeBottomTab.PROFILE,
                    onClick = { onTabSelected(HomeBottomTab.PROFILE) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    label: String,
    iconOutline: ImageVector,
    iconFilled: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    hasAlertBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) AppColors.Ink else AppColors.TextTertiary,
        animationSpec = tween(durationMillis = 200),
        label = "navIconColor"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) AppColors.Ink else AppColors.TextTertiary,
        animationSpec = tween(durationMillis = 200),
        label = "navLabelColor"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "navIconScale"
    )

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = if (isSelected) iconFilled else iconOutline,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier
                        .size(22.dp)
                        .scale(iconScale)
                )
                if (hasAlertBadge) {
                    Box(
                        modifier = Modifier
                            .offset(x = 4.dp, y = (-2).dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(AppColors.Amber)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontFamily = Tajawal,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = labelColor,
                    fontSize = 11.sp
                )
            )
            Spacer(modifier = Modifier.height(3.dp))
        }
    }
}
