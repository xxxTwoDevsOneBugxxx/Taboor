package com.taboor.core.utils

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Idiomatic Android & Kotlin micro-interactions and tactile feedback.
 * Gives Jetpack Compose that authentic, responsive Android feel:
 * - Spring-physics bouncy press down
 * - Subtle haptic vibrations on tap
 * - Breathing pulsing animations for live indicators
 */

/**
 * Bouncy press animation using spring physics with subtle haptic tactile feedback.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.96f,
    withRipple: Boolean = false,
    rippleColor: Color = Color.Unspecified,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bounceSpring"
    )

    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = if (withRipple) ripple(color = rippleColor) else null
        ) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        }
}

/**
 * Spring press scaling that binds to a component's MutableInteractionSource.
 * Perfect for Material3 Button, OutlinedButton, Card, etc.
 */
fun Modifier.bouncePress(
    scaleDown: Float = 0.96f,
    interactionSource: MutableInteractionSource? = null
): Modifier = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bouncePressSpring"
    )

    this.scale(scale)
}

/**
 * Breathing pulse effect for live active queue badges and dots.
 */
fun Modifier.pulseEffect(
    minScale: Float = 0.92f,
    maxScale: Float = 1.15f,
    durationMs: Int = 1400
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    this.scale(pulseScale)
}

/**
 * Performs a tactile haptic tick.
 */
@Composable
fun rememberHapticTick(): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return remember(haptic) {
        { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
    }
}
