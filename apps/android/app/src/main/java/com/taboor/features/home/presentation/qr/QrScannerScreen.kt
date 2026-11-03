package com.taboor.features.home.presentation.qr

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.features.home.data.repository.ServiceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * QR Code Scanner Screen.
 * Pixel-perfect reproduction of Flutter's `qr_scanner_screen.dart`:
 * - Dark optical center layout
 * - Top header controls (Back button, Title, Torch toggle, Camera flip)
 * - Viewfinder with animated amber scanning laser & 4 corner L-markers
 * - Top alignment guide text & Bottom auto-detect pill
 * - Auto-detect success modal dialog
 */
@Composable
fun QrScannerScreen(
    onNavigateBack: () -> Unit,
    onScanSuccess: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isArabic = LocaleManager.isArabic
    val coroutineScope = rememberCoroutineScope()

    androidx.activity.compose.BackHandler(onBack = onNavigateBack)

    var isTorchOn by remember { mutableStateOf(false) }
    var detectedCode by remember { mutableStateOf<String?>(null) }

    // Animated scanning laser bar
    val infiniteTransition = rememberInfiniteTransition(label = "LaserTransition")
    val laserFraction by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserAnim"
    )



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1E293B), Color.Black)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ==========================================
            // Top Header Controls
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.54f))
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                        contentDescription = strings.profileBack,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = if (isArabic) "مسح كود الفرع" else "Scan Branch QR",
                    fontFamily = Tajawal,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Torch Toggle
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.54f))
                            .clickable { isTorchOn = !isTorchOn },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                            contentDescription = null,
                            tint = if (isTorchOn) AppColors.Amber else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Camera Switch
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.54f))
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FlipCameraIos,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ==========================================
            // Top Alignment Guide Text Pill
            // ==========================================
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isArabic) "وجّه الكاميرا نحو رمز الـ QR بالفرع" else "Align QR code within the frame",
                    fontFamily = Tajawal,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // Center Viewfinder with Animated Laser & L-markers
            // ==========================================
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.5.dp, AppColors.Teal, RoundedCornerShape(24.dp))
                    .shadow(24.dp, RoundedCornerShape(24.dp), ambientColor = AppColors.Teal.copy(alpha = 0.25f))
                    .clickable { detectedCode = "TABOOR-BRANCH-DOWNTOWN-Q12" },
                contentAlignment = Alignment.TopCenter
            ) {
                // Animated Laser Line with Amber Glow
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (laserFraction * 260).dp)
                        .padding(horizontal = 16.dp)
                        .height(3.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    AppColors.Amber,
                                    Color.Transparent
                                )
                            )
                        )
                        .shadow(8.dp, ambientColor = AppColors.Amber)
                )

                // 4 Corner L-bracket Markers
                // Top-Left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(26.dp)
                        .border(
                            width = 4.dp,
                            color = AppColors.Amber,
                            shape = RoundedCornerShape(topStart = 8.dp)
                        )
                )
                // Top-Right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(26.dp)
                        .border(
                            width = 4.dp,
                            color = AppColors.Amber,
                            shape = RoundedCornerShape(topEnd = 8.dp)
                        )
                )
                // Bottom-Left
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(26.dp)
                        .border(
                            width = 4.dp,
                            color = AppColors.Amber,
                            shape = RoundedCornerShape(bottomStart = 8.dp)
                        )
                )
                // Bottom-Right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(26.dp)
                        .border(
                            width = 4.dp,
                            color = AppColors.Amber,
                            shape = RoundedCornerShape(bottomEnd = 8.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // Bottom Auto-detect Badge Pill
            // ==========================================
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.FlashAuto,
                        contentDescription = null,
                        tint = AppColors.Amber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "سيتم التعرف على الفرع وحجز مكانك تلقائياً" else "Auto-detects branch & books your spot",
                        fontFamily = Tajawal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.Gray300,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }

        // ==========================================
        // Code Detected Modal Dialog
        // ==========================================
        detectedCode?.let { code ->
            AlertDialog(
                onDismissRequest = { detectedCode = null },
                containerColor = AppColors.Paper,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = AppColors.Success,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isArabic) "تم مسح الكود بنجاح!" else "QR Code Detected!",
                            fontFamily = Tajawal,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Ink
                        )
                    }
                },
                text = {
                    Column {
                        Text(
                            text = if (isArabic) {
                                "تم التعرف على كود الفرع الذكي (فرع وسط البلد). جاري تسجيلك تلقائياً في الطابور المباشر:"
                            } else {
                                "Smart Branch Code recognized (Downtown Branch). Registering you in live queue:"
                            },
                            fontFamily = Tajawal,
                            fontSize = 13.sp,
                            color = AppColors.Gray600
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.SoftTeal)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = code,
                                fontFamily = Tajawal,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Ink
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            detectedCode = null
                            val firstPlace = ServiceRepository.places.first()
                            ServiceRepository.joinQueue(firstPlace, firstPlace.branches.first())
                            onScanSuccess()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Teal)
                    ) {
                        Text(
                            text = if (isArabic) "انضم للطابور فوراً" else "Join Live Queue",
                            fontFamily = Tajawal,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { detectedCode = null }) {
                        Text(
                            text = if (isArabic) "إلغاء" else "Cancel",
                            fontFamily = Tajawal,
                            color = AppColors.Gray600
                        )
                    }
                }
            )
        }
    }
}
