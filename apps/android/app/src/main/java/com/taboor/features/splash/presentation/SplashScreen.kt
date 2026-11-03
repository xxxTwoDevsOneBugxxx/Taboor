package com.taboor.features.splash.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.taboor.core.components.TaboorLogo
import com.taboor.core.localization.LocaleManager
import com.taboor.core.theme.AppColors
import kotlinx.coroutines.delay

/**
 * Taboor Splash Screen
 * Exact port from Flutter: `lib/features/splash/presentation/screens/splash_screen.dart`
 */
@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // Controller for Logo entry animation (1800ms)
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimation = true
    }

    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutBack),
        label = "LogoScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 650, easing = EaseIn),
        label = "LogoAlpha"
    )

    // Infinite floating circles animation (8 seconds reverse repeat)
    val infiniteTransition = rememberInfiniteTransition(label = "FloatingBackground")
    val t by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatingT"
    )

    // Navigation timer (2000ms)
    LaunchedEffect(Unit) {
        delay(2000)
        if (LocaleManager.isFirstTime.value) {
            onNavigateToOnboarding()
        } else {
            onNavigateToLogin()
        }
    }

    // Background Gradient mirroring Flutter _SplashGradient
    val splashGradient = Brush.verticalGradient(
        colors = listOf(
            AppColors.SoftTeal.copy(alpha = 0.7f),
            AppColors.Background,
            AppColors.Paper
        )
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(splashGradient)
        ) {
            // Floating ambient circles
            FloatingBackground(t = t)

            // Center animated TaboorLogo with wordmark
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .alpha(logoAlpha)
                        .scale(logoScale)
                ) {
                    TaboorLogo(
                        size = 90.dp,
                        showWordmark = true
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingBackground(t: Float) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top-left circle (SoftTeal, 260dp)
        Box(
            modifier = Modifier
                .offset(
                    x = (-60 + t * 80).dp,
                    y = (-40 + t * 30).dp
                )
                .size(260.dp)
                .clip(CircleShape)
                .background(AppColors.SoftTeal.copy(alpha = 0.45f))
        )

        // Floating teal circle (190dp)
        Box(
            modifier = Modifier
                .offset(
                    x = (70 - t * 60).dp,
                    y = (60 - t * 40).dp
                )
                .size(190.dp)
                .clip(CircleShape)
                .background(AppColors.Teal.copy(alpha = 0.1f))
        )

        // Bottom-right amber circle (150dp)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (90).dp)
                .size(150.dp)
                .clip(CircleShape)
                .background(AppColors.Amber.copy(alpha = 0.12f))
        )
    }
}
