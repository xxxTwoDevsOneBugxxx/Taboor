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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.theme.AppColors
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
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
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
                        onOpenQrScanner = onOpenQrScanner
                    )
                    HomeBottomTab.QUEUE -> QueueTab(
                        onExploreServices = { onTabChange(HomeBottomTab.HOME) }
                    )
                    HomeBottomTab.ALERTS -> AlertsTab()
                    HomeBottomTab.PROFILE -> ProfileTab(
                        onNavigateToEditProfile = onNavigateToEditProfile,
                        onNavigateToChangePassword = onNavigateToChangePassword,
                        onSignOut = onSignOut
                    )
                }
            }

            // Floating Capsule Bottom Navigation Bar with sliding pill indicator
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                FloatingCapsuleNavBar(
                    currentTab = selectedTab,
                    onTabSelected = onTabChange
                )
            }
        }
    }
}

@Composable
private fun FloatingCapsuleNavBar(
    currentTab: HomeBottomTab,
    onTabSelected: (HomeBottomTab) -> Unit
) {
    val strings = LocalAppStrings.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(66.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = AppColors.DeepTeal.copy(alpha = 0.12f),
                spotColor = AppColors.DeepTeal.copy(alpha = 0.22f)
            ),
        shape = RoundedCornerShape(24.dp),
        color = AppColors.Paper.copy(alpha = 0.94f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f))
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            val itemWidth = maxWidth / 4

            // Mathematical calculation of bias across 4 tabs:
            // 0 -> -1f (Start), 1 -> -0.333f, 2 -> +0.333f, 3 -> +1f (End)
            // BiasAlignment in Compose automatically handles RTL flipping!
            val targetBias = when (currentTab) {
                HomeBottomTab.HOME -> -1f
                HomeBottomTab.QUEUE -> -1f / 3f
                HomeBottomTab.ALERTS -> 1f / 3f
                HomeBottomTab.PROFILE -> 1f
            }

            val animatedBias by animateFloatAsState(
                targetValue = targetBias,
                animationSpec = spring(
                    dampingRatio = 0.82f,
                    stiffness = 380f
                ),
                label = "navPillSlide"
            )

            // Smoothly sliding active background pill
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(itemWidth - 4.dp)
                    .align(BiasAlignment(animatedBias, 0f))
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppColors.SoftTeal)
            )

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
                    isSelected = currentTab == HomeBottomTab.HOME,
                    onClick = { onTabSelected(HomeBottomTab.HOME) },
                    modifier = Modifier.weight(1f)
                )
                NavBarItem(
                    label = strings.navQueue,
                    iconOutline = Icons.Outlined.ConfirmationNumber,
                    iconFilled = Icons.Rounded.ConfirmationNumber,
                    isSelected = currentTab == HomeBottomTab.QUEUE,
                    onClick = { onTabSelected(HomeBottomTab.QUEUE) },
                    modifier = Modifier.weight(1f)
                )
                NavBarItem(
                    label = strings.navAlerts,
                    iconOutline = Icons.Outlined.Notifications,
                    iconFilled = Icons.Rounded.Notifications,
                    isSelected = currentTab == HomeBottomTab.ALERTS,
                    onClick = { onTabSelected(HomeBottomTab.ALERTS) },
                    modifier = Modifier.weight(1f)
                )
                NavBarItem(
                    label = strings.navProfile,
                    iconOutline = Icons.Outlined.Person,
                    iconFilled = Icons.Rounded.Person,
                    isSelected = currentTab == HomeBottomTab.PROFILE,
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
    modifier: Modifier = Modifier
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) AppColors.Teal else AppColors.Gray500,
        animationSpec = tween(durationMillis = 200),
        label = "navIconColor"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) AppColors.Ink else AppColors.Gray600,
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
            .clip(RoundedCornerShape(18.dp))
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
            Icon(
                imageVector = if (isSelected) iconFilled else iconOutline,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier
                    .size(23.dp)
                    .scale(iconScale)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = labelColor,
                    fontSize = 11.sp
                )
            )
        }
    }
}
