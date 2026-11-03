package com.taboor.features.onboarding.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import kotlin.math.roundToInt

enum class OnboardingIllustrationType { TICKET, LIVE, ALERT }

@Composable
fun OnboardingIllustration(
    type: OnboardingIllustrationType,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "IllustrationLoop")
    val t by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "t"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        contentAlignment = Alignment.Center
    ) {
        when (type) {
            OnboardingIllustrationType.TICKET -> TicketIllustration(t = t)
            OnboardingIllustrationType.LIVE -> LiveTrackingIllustration(t = t)
            OnboardingIllustrationType.ALERT -> AlertIllustration(t = t)
        }
    }
}

// =================================================================
// 1. TICKET ILLUSTRATION
// =================================================================
@Composable
private fun TicketIllustration(t: Float) {
    val strings = LocalAppStrings.current

    Box(
        modifier = Modifier
            .size(width = 240.dp, height = 230.dp)
            .rotate(-3f + t * 6f),
        contentAlignment = Alignment.Center
    ) {
        // Backdrop QR Code Card
        Box(
            modifier = Modifier
                .offset(x = (16 - t * 8).dp, y = 10.dp)
                .size(width = 150.dp, height = 190.dp)
                .shadow(elevation = 20.dp, shape = RoundedCornerShape(28.dp), spotColor = AppColors.Teal)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(AppColors.Indigo, AppColors.Teal)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCode2,
                contentDescription = null,
                tint = Color(0x99FFFDF8),
                modifier = Modifier.size(92.dp)
            )
        }

        // Foreground Ticket Card
        Box(
            modifier = Modifier
                .offset(x = (-10 + t * 6).dp, y = (-14).dp)
                .size(width = 168.dp, height = 208.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(22.dp),
                    spotColor = AppColors.DeepTeal.copy(alpha = 0.35f)
                )
                .clip(RoundedCornerShape(22.dp))
                .background(AppColors.Paper)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // App Brand header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(AppColors.DeepTeal, AppColors.Teal)
                                )
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.appTitle,
                        fontFamily = Tajawal,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.DeepTeal
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Cut line with scissors
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(1.dp)
                        .background(AppColors.Gray300),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(AppColors.Paper),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = null,
                            tint = AppColors.Gray400,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Ticket Number
                Text(
                    text = strings.ticketNumberLabel,
                    fontFamily = Tajawal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.Gray500
                )
                Text(
                    text = "07",
                    fontFamily = Tajawal,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.DeepTeal,
                    lineHeight = 44.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Pill Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(AppColors.DeepTeal, AppColors.Teal)
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = strings.ticketSitRelax,
                        fontFamily = Tajawal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Paper
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Barcode simulation dashes
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 14) {
                        val barWidth = if (i % 3 == 0) 3.dp else 1.5.dp
                        Box(
                            modifier = Modifier
                                .size(width = barWidth, height = 12.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(AppColors.Gray600)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

// =================================================================
// 2. LIVE TRACKING ILLUSTRATION
// =================================================================
@Composable
private fun LiveTrackingIllustration(t: Float) {
    val strings = LocalAppStrings.current

    Box(
        modifier = Modifier.size(width = 250.dp, height = 235.dp),
        contentAlignment = Alignment.Center
    ) {
        // Pulsating radar radial background
        Box(
            modifier = Modifier
                .size(210.dp)
                .alpha(0.5f + t * 0.3f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AppColors.Indigo.copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Center circular counter
        Box(
            modifier = Modifier
                .scale(0.97f + t * 0.04f)
                .size(176.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = CircleShape,
                    spotColor = AppColors.Indigo.copy(alpha = 0.3f)
                )
                .clip(CircleShape)
                .background(AppColors.Paper),
            contentAlignment = Alignment.Center
        ) {
            // Arc progress painter
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                val strokeWidth = 7.dp.toPx()
                // Track circle
                drawCircle(
                    color = AppColors.SoftTeal,
                    radius = size.minDimension / 2f - strokeWidth / 2f,
                    style = Stroke(width = strokeWidth)
                )

                // Amber Arc indicator
                val sweepAngle = -360f * (1f - t)
                drawArc(
                    color = AppColors.Amber,
                    startAngle = 0f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Counter Texts inside
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AppColors.Teal)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.liveAhead,
                        fontFamily = Tajawal,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.DeepTeal
                    )
                }

                // Dynamic Countdown 05..01
                val countValue = (6 - t * 5).roundToInt().coerceIn(1, 6)
                Text(
                    text = String.format("%02d", countValue),
                    fontFamily = Tajawal,
                    fontSize = 58.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.DeepTeal,
                    lineHeight = 58.sp
                )

                // Badge: "دورك قرب" with trend icon
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppColors.SoftTeal)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = AppColors.Teal,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.liveTurnNear,
                            fontFamily = Tajawal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Teal
                        )
                    }
                }
            }
        }
    }
}

// =================================================================
// 3. ALERT ILLUSTRATION
// =================================================================
@Composable
private fun AlertIllustration(t: Float) {
    val strings = LocalAppStrings.current

    Box(
        modifier = Modifier.size(width = 250.dp, height = 235.dp),
        contentAlignment = Alignment.Center
    ) {
        // Expanding sound ripple rings
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxRadius = size.minDimension / 2f - 8.dp.toPx()
            val currentRadius = maxRadius * (0.5f + t * 0.5f)
            val alpha = ((1f - t) * 0.9f).coerceIn(0f, 1f)

            drawCircle(
                color = AppColors.Indigo.copy(alpha = alpha * 0.35f),
                radius = currentRadius,
                style = Stroke(width = 3.dp.toPx())
            )
        }

        // Active Bell Circle
        Box(
            modifier = Modifier
                .offset(x = (24 - t * 6).dp, y = (18 - t * 4).dp)
                .size(128.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = CircleShape,
                    spotColor = AppColors.Teal.copy(alpha = 0.5f)
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(AppColors.DeepTeal, AppColors.Teal)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = AppColors.Paper,
                modifier = Modifier.size(54.dp)
            )

            // Pulsing notification dot
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 26.dp)
                    .scale(1f + t * 0.25f)
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(AppColors.Amber)
                    .border(2.5.dp, AppColors.DeepTeal, CircleShape)
            )
        }

        // Notification Card Popup
        Box(
            modifier = Modifier
                .offset(x = (-24 + t * 14).dp, y = 0.dp)
                .size(width = 176.dp, height = 124.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(18.dp),
                    spotColor = AppColors.DeepTeal.copy(alpha = 0.3f)
                )
                .clip(RoundedCornerShape(18.dp))
                .background(AppColors.Paper)
        ) {
            // Amber accent strip on leading edge
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 0.dp)
                    .size(width = 4.dp, height = 80.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AppColors.Amber)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(AppColors.DeepTeal, AppColors.Teal)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = AppColors.Paper,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.appTitle,
                        fontFamily = Tajawal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.DeepTeal
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = strings.alertNow,
                        fontFamily = Tajawal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.Gray500
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Title
                Text(
                    text = strings.alertLeaveTitle,
                    fontFamily = Tajawal,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.DeepTeal
                )
                Spacer(modifier = Modifier.height(2.dp))
                // Subtitle
                Text(
                    text = strings.alertOnlyTwo,
                    fontFamily = Tajawal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = AppColors.Gray600
                )
            }
        }
    }
}
