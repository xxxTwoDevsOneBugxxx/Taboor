package com.taboor.features.home.presentation.tracking

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.components.TaboorMapView
import com.taboor.core.localization.LocaleManager
import com.taboor.core.location.TaboorPrecomputedRoutes
import com.taboor.core.navigation.NavigationAppBottomSheet
import com.taboor.core.services.LocationService
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick
import com.taboor.core.utils.pulseEffect
import com.taboor.features.home.data.repository.ServiceRepository
import com.taboor.features.home.domain.model.UserTicket
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap

/**
 * Uber & Talabat-grade Live Ride & Queue Tracking Screen for Taboor.
 * - 100% Edge-to-edge interactive live vector map
 * - Top Floating Turn-by-Turn Instruction HUD
 * - Floating Uber-style Camera Controls (User GPS, Destination, Fit Route)
 * - Interactive Draggable Collapsible Bottom Sheet (Peek vs Expanded)
 * - High-visibility Google Maps / Waze voice guidance launcher
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveQueueTrackingScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isArabic = LocaleManager.isArabic
    val activeTicket by ServiceRepository.activeTicket.collectAsState()

    var showCancelDialog by remember { mutableStateOf(false) }
    var showNavigationSheet by remember { mutableStateOf(false) }
    var isSheetExpanded by remember { mutableStateOf(false) }
    var maplibreMapRef by remember { mutableStateOf<MapLibreMap?>(null) }

    BackHandler(onBack = onNavigateBack)

    fun shareTicket(ticket: UserTicket) {
        val shareText = if (isArabic) {
            "تذكرتي في طابور: #${ticket.ticketNumber} في ${ticket.localizedServiceName(true)} (${ticket.localizedBranch(true)}). أمامي ${ticket.waitingAhead} أشخاص، الموعد التقديري خلال ${ticket.estMinutes} دقيقة."
        } else {
            "My Taboor ticket: #${ticket.ticketNumber} at ${ticket.localizedServiceName(false)} (${ticket.localizedBranch(false)}). ${ticket.waitingAhead} ahead, est. wait ~${ticket.estMinutes} mins."
        }
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareChooser = Intent.createChooser(sendIntent, if (isArabic) "مشاركة التذكرة عبر" else "Share ticket via")
        shareChooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(shareChooser)
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = {
                Text(
                    text = if (isArabic) "إلغاء التذكرة" else "Cancel Ticket",
                    fontFamily = Tajawal,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isArabic)
                        "هل أنت متأكد من رغبتك في إلغاء دورك الحالي؟ ستفقد مكانك في الطابور وسينتقل لدور الانتظار التالي."
                    else
                        "Are you sure you want to cancel your active ticket? You will lose your spot in line.",
                    fontFamily = Tajawal
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        ServiceRepository.cancelActiveTicket()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentRed)
                ) {
                    Text(
                        text = if (isArabic) "تأكيد الإلغاء" else "Confirm Cancel",
                        fontFamily = Tajawal,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(
                        text = if (isArabic) "تراجع" else "Keep",
                        fontFamily = Tajawal,
                        color = AppColors.TextSecondary
                    )
                }
            }
        )
    }

    Scaffold(
        containerColor = AppColors.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        val ticket = activeTicket
        if (ticket == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isArabic) "لا يوجد دور نشط حالياً" else "No active ticket",
                        fontFamily = Tajawal,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Teal)
                    ) {
                        Text(
                            text = if (isArabic) "رجوع للرئيسية" else "Back to Home",
                            fontFamily = Tajawal,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            val branchLat = ticket.latitude ?: 30.0585
            val branchLng = ticket.longitude ?: 31.3466
            val distKm = TaboorPrecomputedRoutes.getDistanceKm(ticket.branchId, branchLat, branchLng)
            val etaMin = TaboorPrecomputedRoutes.getEstimatedMinutes(ticket.branchId, branchLat, branchLng)
            val (subInstruction, mainInstruction) = TaboorPrecomputedRoutes.getTurnInstruction(ticket.branchId, isArabic)

            val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

            Box(modifier = Modifier.fillMaxSize()) {
                // ==========================================
                // 1. Full-Screen Edge-to-Edge Vector Map (Uber/Talabat style)
                // ==========================================
                TaboorMapView(
                    branchLat = branchLat,
                    branchLng = branchLng,
                    branchTitle = ticket.localizedServiceName(isArabic),
                    branchAddress = ticket.localizedBranch(isArabic),
                    branchId = ticket.branchId,
                    isFullScreen = true,
                    showControls = false,
                    onMapReady = { map ->
                        maplibreMapRef = map
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // ==========================================
                // 2. Top Floating Navigation HUD (Talabat / Uber HUD)
                // ==========================================
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(top = statusBarTop + 8.dp)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Top Bar: Back Button + Live Status Capsule + Quick Maps Launcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Close / Back Circular Glass Button
                        Surface(
                            shape = CircleShape,
                            color = AppColors.Paper.copy(alpha = 0.94f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.CardBorder),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .size(42.dp)
                                .bounceClick { onNavigateBack() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close",
                                    tint = AppColors.Ink,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Center Live Status Pill
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = AppColors.Paper.copy(alpha = 0.94f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.CardBorder),
                            shadowElevation = 6.dp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(AppColors.AccentGreen)
                                        .pulseEffect(minScale = 0.8f, maxScale = 1.3f)
                                )
                                Text(
                                    text = if (isArabic) "أنت في الطريق • دورك يقترب" else "On route • Turn near",
                                    fontFamily = Tajawal,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Ink
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = AppColors.Teal
                                ) {
                                    Text(
                                        text = if (isArabic) "~${ticket.estMinutes} د" else "~${ticket.estMinutes}m",
                                        fontFamily = Tajawal,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Quick Navigation Launcher (Maps)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = AppColors.Ink,
                            shadowElevation = 6.dp,
                            modifier = Modifier.bounceClick {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showNavigationSheet = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Navigation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isArabic) "ملاحة" else "Maps",
                                    fontFamily = Tajawal,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Turn-By-Turn Direction Guidance Card (Uber Style)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = AppColors.Paper.copy(alpha = 0.96f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.CardBorder),
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Turn Icon
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(AppColors.Teal, AppColors.DeepTeal)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.TurnRight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = subInstruction,
                                        fontFamily = Tajawal,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AppColors.Teal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(text = "•", color = AppColors.TextTertiary, fontSize = 10.sp)
                                    Text(
                                        text = if (isArabic) "$distKm كم (~$etaMin د)" else "$distKm km (~$etaMin min)",
                                        fontFamily = Tajawal,
                                        fontSize = 11.sp,
                                        color = AppColors.TextSecondary
                                    )
                                }
                                Text(
                                    text = mainInstruction,
                                    fontFamily = Tajawal,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // 3. Floating Map Controls (Uber Camera Tools)
                // ==========================================
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Fit Whole Route
                    Surface(
                        shape = CircleShape,
                        color = AppColors.Paper,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.CardBorder),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .size(42.dp)
                            .bounceClick {
                                val map = maplibreMapRef ?: return@bounceClick
                                val userPos = LocationService.getCurrentPosition(context)
                                if (userPos != null) {
                                    val bounds = LatLngBounds.Builder()
                                        .include(LatLng(branchLat, branchLng))
                                        .include(LatLng(userPos.first, userPos.second))
                                        .build()
                                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 130), 800)
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.AltRoute,
                                contentDescription = "Fit Route",
                                tint = AppColors.Teal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Recenter on Clinic
                    Surface(
                        shape = CircleShape,
                        color = AppColors.Paper,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.CardBorder),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .size(42.dp)
                            .bounceClick {
                                val map = maplibreMapRef ?: return@bounceClick
                                map.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(LatLng(branchLat, branchLng), 16.0),
                                    700
                                )
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Place,
                                contentDescription = "Clinic",
                                tint = AppColors.Ink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Recenter on My Location
                    Surface(
                        shape = CircleShape,
                        color = AppColors.Paper,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.CardBorder),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .size(42.dp)
                            .bounceClick {
                                val map = maplibreMapRef ?: return@bounceClick
                                val userPos = LocationService.getCurrentPosition(context)
                                if (userPos != null) {
                                    map.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(LatLng(userPos.first, userPos.second), 16.2),
                                        700
                                    )
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.MyLocation,
                                contentDescription = "My Location",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // ==========================================
                // 4. Interactive Uber/Talabat Draggable Bottom Sheet
                // ==========================================
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { _, dragAmount ->
                                if (dragAmount < -15 && !isSheetExpanded) {
                                    isSheetExpanded = true
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                } else if (dragAmount > 15 && isSheetExpanded) {
                                    isSheetExpanded = false
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                        },
                    shape = RoundedCornerShape(26.dp),
                    color = AppColors.Paper,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.CardBorder),
                    shadowElevation = 18.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Drag Indicator Bar (Tap to toggle)
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .bounceClick {
                                    isSheetExpanded = !isSheetExpanded
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(42.dp)
                                    .height(4.5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(AppColors.Gray300)
                            )
                        }

                        // Compact Peek Header: Place Details + Large Ticket Badge
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bounceClick { isSheetExpanded = !isSheetExpanded },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ticket.localizedServiceName(isArabic),
                                    fontFamily = Tajawal,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${ticket.localizedBranch(isArabic)} • ${ticket.localizedCounter(isArabic)}",
                                    fontFamily = Tajawal,
                                    fontSize = 12.sp,
                                    color = AppColors.TextSecondary,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Ticket Number Badge
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = AppColors.SoftTeal,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Teal.copy(alpha = 0.35f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (isArabic) "تذكرتك" else "Ticket",
                                        fontFamily = Tajawal,
                                        fontSize = 9.sp,
                                        color = AppColors.TextSecondary
                                    )
                                    Text(
                                        text = "#${ticket.ticketNumber}",
                                        fontFamily = Tajawal,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AppColors.Teal
                                    )
                                }
                            }
                        }

                        // Progress Bar & Ahead Status
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            LinearProgressIndicator(
                                progress = { ticket.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = AppColors.Teal,
                                trackColor = AppColors.Teal.copy(alpha = 0.15f)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(AppColors.Amber)
                                    )
                                    Text(
                                        text = if (isArabic) "أمامك ${ticket.waitingAhead} في الانتظار" else "${ticket.waitingAhead} ahead of you",
                                        fontFamily = Tajawal,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.Amber
                                    )
                                }

                                Text(
                                    text = if (isArabic)
                                        (if (isSheetExpanded) "تصغير الخريطة ▲" else "تفاصيل الدور والمسار ▼")
                                    else
                                        (if (isSheetExpanded) "Collapse ▲" else "Queue Details ▼"),
                                    fontFamily = Tajawal,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Teal,
                                    modifier = Modifier.clickable { isSheetExpanded = !isSheetExpanded }
                                )
                            }
                        }

                        // ==========================================
                        // Expanded Section (Queue Stepper + Actions)
                        // ==========================================
                        AnimatedVisibility(
                            visible = isSheetExpanded,
                            enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                            exit = shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = AppColors.RowDivider
                                )

                                // Queue Line Visual Stepper
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(AppColors.CardBackground)
                                        .padding(vertical = 10.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    QueueNode(
                                        number = ticket.currentServing,
                                        label = if (isArabic) "في الخدمة" else "Serving",
                                        isCurrent = false,
                                        isUser = false
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = null,
                                        tint = AppColors.TextTertiary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    QueueNode(
                                        number = "#A-11",
                                        label = if (isArabic) "التالي" else "Next",
                                        isCurrent = false,
                                        isUser = false
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = null,
                                        tint = AppColors.TextTertiary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    QueueNode(
                                        number = "#${ticket.ticketNumber}",
                                        label = if (isArabic) "أنت هنا" else "You",
                                        isCurrent = true,
                                        isUser = true
                                    )
                                }

                                // Uber / Talabat Grade Primary CTA: Launch Navigation
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bounceClick(scaleDown = 0.98f) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showNavigationSheet = true
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    color = AppColors.Teal,
                                    shadowElevation = 3.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 13.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Navigation,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isArabic)
                                                "بدء التوجيه في الخرائط (Google Maps / Waze)"
                                            else
                                                "Start Turn-by-Turn GPS Navigation",
                                            fontFamily = Tajawal,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                // Secondary Actions: Postpone + Share + Cancel
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Postpone (+10m)
                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            ServiceRepository.delayActiveTicket(10)
                                        },
                                        modifier = Modifier
                                            .weight(1.1f)
                                            .height(44.dp)
                                            .bounceClick(),
                                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.SoftTeal),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Teal.copy(alpha = 0.25f)),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Update,
                                                contentDescription = null,
                                                tint = AppColors.Teal,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = if (isArabic) "تأجيل (+10د)" else "Postpone",
                                                fontFamily = Tajawal,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppColors.Teal
                                            )
                                        }
                                    }

                                    // Share
                                    Button(
                                        onClick = { shareTicket(ticket) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .bounceClick(),
                                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.CardBackground),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.CardBorder),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Share,
                                                contentDescription = null,
                                                tint = AppColors.Ink,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = if (isArabic) "مشاركة" else "Share",
                                                fontFamily = Tajawal,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppColors.Ink
                                            )
                                        }
                                    }

                                    // Cancel
                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showCancelDialog = true
                                        },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .bounceClick(),
                                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentRed.copy(alpha = 0.12f)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.AccentRed.copy(alpha = 0.3f)),
                                        shape = RoundedCornerShape(14.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Cancel,
                                            contentDescription = "Cancel",
                                            tint = AppColors.AccentRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNavigationSheet && activeTicket != null) {
        val ticket = activeTicket!!
        val branchLat = ticket.latitude ?: 30.0585
        val branchLng = ticket.longitude ?: 31.3466
        NavigationAppBottomSheet(
            destinationLat = branchLat,
            destinationLng = branchLng,
            destinationTitle = "${ticket.localizedServiceName(isArabic)} - ${ticket.localizedBranch(isArabic)}",
            onDismiss = { showNavigationSheet = false }
        )
    }
}

@Composable
private fun QueueNode(
    number: String,
    label: String,
    isCurrent: Boolean,
    isUser: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isUser) AppColors.Teal else if (isCurrent) AppColors.Amber else AppColors.Paper,
            border = if (!isUser && !isCurrent) androidx.compose.foundation.BorderStroke(1.dp, AppColors.CardBorder) else null
        ) {
            Text(
                text = number,
                fontFamily = Tajawal,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUser || isCurrent) Color.White else AppColors.TextPrimary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Text(
            text = label,
            fontFamily = Tajawal,
            fontSize = 10.sp,
            color = if (isUser) AppColors.Teal else AppColors.TextSecondary
        )
    }
}
