package com.taboor.features.home.presentation.tabs

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
import com.taboor.core.navigation.NavigationAppBottomSheet
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick
import com.taboor.core.utils.pulseEffect
import com.taboor.features.home.data.repository.ServiceRepository
import com.taboor.features.home.domain.model.TicketBookingType
import com.taboor.features.home.domain.model.TicketHistoryItem
import com.taboor.features.home.domain.model.TicketStatus
import com.taboor.features.home.domain.model.UserTicket
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueTab(
    onExploreServices: () -> Unit,
    onOpenLiveTracking: () -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val strings = LocalAppStrings.current
    val isArabic = LocaleManager.isArabic
    val activeTicket by ServiceRepository.activeTicket.collectAsState()
    val historyItems by ServiceRepository.history.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCancelDialog by remember { mutableStateOf(false) }
    var showNavigationSheet by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    fun addToCalendar(ticket: UserTicket) {
        try {
            val title = if (isArabic) "${ticket.localizedServiceName(true)} - تذكرة #${ticket.ticketNumber}"
            else "${ticket.localizedServiceName(false)} - Ticket #${ticket.ticketNumber}"
            val location = ticket.localizedBranch(isArabic)
            val startTime = System.currentTimeMillis() + ticket.estMinutes * 60 * 1000
            val endTime = startTime + 30 * 60 * 1000

            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                putExtra(CalendarContract.Events.DESCRIPTION, "Taboor Smart Queue Ticket #${ticket.ticketNumber}")
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

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

    fun openDirections(ticket: UserTicket) {
        val lat = ticket.latitude ?: 30.0585
        val lng = ticket.longitude ?: 31.3466
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(ticket.localizedServiceName(isArabic))})")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(mapIntent)
        } catch (_: Exception) {
            onOpenLiveTracking()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppColors.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    delay(500)
                    isRefreshing = false
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = statusBarTop + 14.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ==========================================
                // 1. Top Custom Header Bar (1:1 with iOS QueueTabView)
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isArabic) "طوابيري" else "My Queues",
                                fontFamily = Tajawal,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppColors.TextPrimary
                            )

                            if (activeTicket != null) {
                                val isScheduled = activeTicket?.bookingType == TicketBookingType.SCHEDULED
                                val tagColor = if (isScheduled) AppColors.Amber else AppColors.AccentGreen
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = tagColor.copy(alpha = 0.12f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(tagColor)
                                                .then(if (!isScheduled) Modifier.pulseEffect() else Modifier)
                                        )
                                        Text(
                                            text = if (isScheduled) {
                                                if (isArabic) "موعد مؤكد" else "Scheduled"
                                            } else {
                                                if (isArabic) "طابور مباشر" else "Live Queue"
                                            },
                                            fontFamily = Tajawal,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = tagColor
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = if (activeTicket != null) {
                                if (isArabic) "متابعة تذاكرك ومواعيدك المؤكدة" else "Track your active queues & bookings"
                            } else {
                                if (isArabic) "لا توجد تذكرة نشطة حالياً" else "No active tickets currently"
                            },
                            fontFamily = Tajawal,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextSecondary
                        )
                    }

                    if (activeTicket != null) {
                        // Floating circular map shortcut button
                        Surface(
                            shape = CircleShape,
                            color = AppColors.Paper,
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .size(40.dp)
                                .bounceClick { onOpenLiveTracking() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Map,
                                    contentDescription = "Live Route Map",
                                    tint = AppColors.Teal,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // 2. Active Ticket Card OR Empty State
                // ==========================================
                val ticket = activeTicket
                if (ticket != null) {
                    // Apple Wallet Style Virtual Ticket Card
                    WalletTicketCard(
                        ticket = ticket,
                        isArabic = isArabic,
                        onOpenLiveTracking = onOpenLiveTracking,
                        onOpenDirections = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showNavigationSheet = true
                        }
                    )

                    // Quick Action Buttons
                    QueueQuickActions(
                        ticket = ticket,
                        isArabic = isArabic,
                        onOpenLiveTracking = onOpenLiveTracking,
                        onDelayTicket = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            ServiceRepository.delayActiveTicket(15)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    if (isArabic) "تم تأجيل دورك 15 دقيقة بنجاح ⏱️" else "Turn postponed by 15 mins"
                                )
                            }
                        },
                        onShareTicket = { shareTicket(ticket) },
                        onAddToCalendar = { addToCalendar(ticket) },
                        onCancelClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showCancelDialog = true
                        }
                    )
                } else {
                    // Apple World-Class Empty State Card
                    EmptyQueueCard(
                        isArabic = isArabic,
                        onExploreServices = onExploreServices,
                        onSetDemoTicket = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            ServiceRepository.setDemoTicket()
                        }
                    )
                }

                // ==========================================
                // 3. Past Visits History Section
                // ==========================================
                if (historyItems.isNotEmpty()) {
                    HistorySection(
                        historyItems = historyItems,
                        isArabic = isArabic,
                        onExploreServices = onExploreServices
                    )
                }
            }
        }
    }

    if (showCancelDialog) {
        val ticket = activeTicket
        val isScheduled = ticket?.bookingType == TicketBookingType.SCHEDULED
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = {
                Text(
                    text = if (isScheduled) {
                        if (isArabic) "إلغاء حجز الموعد" else "Cancel Appointment"
                    } else {
                        if (isArabic) "إلغاء التذكرة" else "Cancel Ticket"
                    },
                    fontFamily = Tajawal,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isScheduled) {
                        if (isArabic) "هل أنت متأكد من رغبتك في إلغاء حجز موعدك؟ سيتم إتاحة الموعد لمستخدم آخر."
                        else "Are you sure you want to cancel your scheduled appointment? The slot will be released."
                    } else {
                        if (isArabic) "هل أنت متأكد من رغبتك في إلغاء دورك الحالي؟ ستفقد مكانك في الطابور."
                        else "Are you sure you want to cancel your ticket? You will lose your spot in line."
                    },
                    fontFamily = Tajawal,
                    color = AppColors.TextSecondary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        ServiceRepository.cancelActiveTicket()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                if (isArabic) "تم إلغاء التذكرة بنجاح" else "Ticket cancelled"
                            )
                        }
                    }
                ) {
                    Text(
                        text = if (isArabic) "نعم، إلغاء" else "Yes, Cancel",
                        color = AppColors.Error,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Tajawal
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(
                        text = if (isArabic) "الاحتفاظ بدوري" else "Keep My Spot",
                        color = AppColors.TextSecondary,
                        fontFamily = Tajawal
                    )
                }
            },
            containerColor = AppColors.Paper,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showNavigationSheet && activeTicket != null) {
        val ticket = activeTicket!!
        val lat = ticket.latitude ?: 30.0585
        val lng = ticket.longitude ?: 31.3466
        NavigationAppBottomSheet(
            destinationLat = lat,
            destinationLng = lng,
            destinationTitle = "${ticket.localizedServiceName(isArabic)} - ${ticket.localizedBranch(isArabic)}",
            onDismiss = { showNavigationSheet = false }
        )
    }
}

// ==========================================
// Ticket Shape for Authentic Cutout Virtual Ticket (1:1 with iOS TicketShape)
// ==========================================
class TicketShape(
    private val cornerRadius: Dp = 22.dp,
    private val notchRadius: Dp = 10.dp,
    private val notchYPx: Float = 0f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r = with(density) { cornerRadius.toPx() }
        val nr = with(density) { notchRadius.toPx() }
        val w = size.width
        val h = size.height
        val rawNy = if (notchYPx > 0f) notchYPx else (h * 0.28f)
        val ny = rawNy.coerceIn(r + nr, h - r - nr)

        val path = Path().apply {
            reset()
            moveTo(r, 0f)
            lineTo(w - r, 0f)
            arcTo(
                rect = Rect(w - 2 * r, 0f, w, 2 * r),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(w, ny - nr)
            arcTo(
                rect = Rect(w - nr, ny - nr, w + nr, ny + nr),
                startAngleDegrees = -90f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            lineTo(w, h - r)
            arcTo(
                rect = Rect(w - 2 * r, h - 2 * r, w, h),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(r, h)
            arcTo(
                rect = Rect(0f, h - 2 * r, 2 * r, h),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(0f, ny + nr)
            arcTo(
                rect = Rect(-nr, ny - nr, nr, ny + nr),
                startAngleDegrees = 90f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            lineTo(0f, r)
            arcTo(
                rect = Rect(0f, 0f, 2 * r, 2 * r),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            close()
        }
        return Outline.Generic(path)
    }
}

// ==========================================
// Sub-component: Apple Wallet Style Ticket Card
// ==========================================
@Composable
private fun WalletTicketCard(
    ticket: UserTicket,
    isArabic: Boolean,
    onOpenLiveTracking: () -> Unit,
    onOpenDirections: () -> Unit
) {
    var notchYPx by remember { mutableFloatStateOf(0f) }
    val ticketShape = remember(notchYPx) {
        TicketShape(
            cornerRadius = 22.dp,
            notchRadius = 10.dp,
            notchYPx = notchYPx
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 1.5.dp, shape = ticketShape)
            .clip(ticketShape)
            .background(AppColors.Paper)
            .border(0.5.dp, AppColors.RowDivider, ticketShape)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Upper Section: Venue Name, Branch, and Status Pill
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = ticket.localizedServiceName(isArabic),
                            fontFamily = Tajawal,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Ink,
                            maxLines = 2,
                            lineHeight = 23.sp,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = AppColors.Teal,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = ticket.localizedBranch(isArabic),
                                fontFamily = Tajawal,
                                fontSize = 12.sp,
                                color = AppColors.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Status Capsule Pill
                    val isScheduled = ticket.bookingType == TicketBookingType.SCHEDULED
                    val statusColor = if (isScheduled) AppColors.Amber else AppColors.AccentGreen
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = statusColor.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (isScheduled) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    contentDescription = null,
                                    tint = statusColor,
                                    modifier = Modifier.size(11.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                            }
                            Text(
                                text = if (isScheduled) {
                                    ticket.scheduledDate ?: (if (isArabic) "موعد" else "Scheduled")
                                } else {
                                    if (isArabic) "طابور مباشر" else "Live Queue"
                                },
                                fontFamily = Tajawal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                }
            }

            // Perforated Dashed Divider Row (Spanning across genuine notch cutouts, 1:1 with iOS)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .onGloballyPositioned { coordinates ->
                        val centerY = coordinates.positionInParent().y + (coordinates.size.height / 2f)
                        if (kotlin.math.abs(notchYPx - centerY) > 0.5f) {
                            notchYPx = centerY
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                DashedDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    color = Color(0x300B4C56),
                    dashWidth = 5.dp,
                    dashGap = 4.dp,
                    thickness = 1.dp
                )
            }

            // Core Information Body (Clean, Spacious, Border-Free)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val isScheduled = ticket.bookingType == TicketBookingType.SCHEDULED

                if (isScheduled) {
                    // Scheduled Appointment Layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = if (isArabic) "رقم الحجز" else "BOOKING NO.",
                                fontFamily = Tajawal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextTertiary
                            )
                            Text(
                                text = "#${ticket.ticketNumber}",
                                fontFamily = Tajawal,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = AppColors.Ink
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(42.dp)
                                .background(AppColors.RowDivider)
                        )

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = if (isArabic) "الموعد المؤكد" else "CONFIRMED SLOT",
                                fontFamily = Tajawal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextTertiary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = ticket.scheduledTime ?: "04:30 م",
                                    fontFamily = Tajawal,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AppColors.Teal
                                )
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = AppColors.Teal,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = ticket.scheduledDate ?: (if (isArabic) "غداً" else "Tomorrow"),
                                fontFamily = Tajawal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Amber
                            )
                        }
                    }

                    // Quiet Reassurance Message
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = AppColors.Amber,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isArabic) "سنرسل لك تذكيراً ذكياً قبل موعدك بـ 45 دقيقة للتحرك في الوقت المناسب."
                            else "We'll notify you 45 mins before your appointment with traffic estimate.",
                            fontFamily = Tajawal,
                            fontSize = 11.5.sp,
                            color = AppColors.TextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                } else {
                    // Live Queue Layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Ticket Number
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = if (isArabic) "رقم تذكرتك" else "YOUR TICKET",
                                fontFamily = Tajawal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextTertiary
                            )
                            Text(
                                text = "#${ticket.ticketNumber}",
                                fontFamily = Tajawal,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = AppColors.Ink
                            )
                        }

                        // Hairline Vertical Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(42.dp)
                                .background(AppColors.RowDivider)
                        )

                        // Serving Now
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = if (isArabic) "الدور الحالي" else "NOW SERVING",
                                fontFamily = Tajawal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextTertiary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(AppColors.AccentGreen)
                                )
                                Text(
                                    text = ticket.currentServing,
                                    fontFamily = Tajawal,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AppColors.Teal
                                )
                            }
                            Text(
                                text = if (isArabic) "أمامك ${ticket.waitingAhead} أشخاص" else "${ticket.waitingAhead} ahead",
                                fontFamily = Tajawal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.TextSecondary
                            )
                        }
                    }

                    // Minimal Smooth Progress Track
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(AppColors.SoftTeal)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = ticket.progress.coerceIn(0.08f, 1f))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(AppColors.Teal, AppColors.DeepTeal)
                                        )
                                    )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isArabic) "شباك الخدمة: ${ticket.localizedCounter(true)}" else "Window: ${ticket.localizedCounter(false)}",
                                fontFamily = Tajawal,
                                fontSize = 11.5.sp,
                                color = AppColors.TextSecondary
                            )
                            Text(
                                text = if (isArabic) "الوقت التقديري: ~${ticket.estMinutes} دقائق" else "Est. wait: ~${ticket.estMinutes} mins",
                                fontFamily = Tajawal,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Teal
                            )
                        }
                    }
                }

                // Location & Route Footer Strip
                HorizontalDivider(thickness = 0.5.dp, color = AppColors.RowDivider)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DirectionsCar,
                            contentDescription = null,
                            tint = AppColors.TextTertiary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isArabic) "2.1 كم • حوالي 8 د بالسيارة" else "2.1 km • ~8m drive",
                            fontFamily = Tajawal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextSecondary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .bounceClick { onOpenDirections() }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = if (isArabic) "الاتجاهات" else "Route",
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
    }
}

// ==========================================
// Sub-component: Quick Action Buttons
// ==========================================
@Composable
private fun QueueQuickActions(
    ticket: UserTicket,
    isArabic: Boolean,
    onOpenLiveTracking: () -> Unit,
    onDelayTicket: () -> Unit,
    onShareTicket: () -> Unit,
    onAddToCalendar: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Primary Full-Width CTA: Live Map Tracking
        Surface(
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 1.5.dp,
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick { onOpenLiveTracking() }
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(AppColors.Ink, AppColors.DeepTeal)
                        )
                    )
                    .padding(horizontal = 18.dp, vertical = 13.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Map,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = if (isArabic) "فتح شاشة التتبع والخريطة المباشرة" else "Open Live Map Tracking",
                            fontFamily = Tajawal,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // 2. Secondary Row: Postpone / Calendar + Share
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val isScheduled = ticket.bookingType == TicketBookingType.SCHEDULED

            // Postpone or Calendar button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .bounceClick {
                        if (isScheduled) onAddToCalendar() else onDelayTicket()
                    },
                shape = RoundedCornerShape(14.dp),
                color = AppColors.Paper,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isScheduled) Icons.Rounded.CalendarToday else Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = AppColors.Teal,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isScheduled) {
                            if (isArabic) "إضافة للتقويم" else "To Calendar"
                        } else {
                            if (isArabic) "تأجيل (15 د)" else "Postpone (15m)"
                        },
                        fontFamily = Tajawal,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                }
            }

            // Share Button
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .bounceClick { onShareTicket() },
                shape = RoundedCornerShape(14.dp),
                color = AppColors.Paper,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        tint = AppColors.Teal,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "مشاركة التذكرة" else "Share Ticket",
                        fontFamily = Tajawal,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                }
            }
        }

        // 3. Quiet Luxury Cancellation Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = AppColors.AccentRed.copy(alpha = 0.08f),
                modifier = Modifier.bounceClick { onCancelClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Cancel,
                        contentDescription = null,
                        tint = AppColors.AccentRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (ticket.bookingType == TicketBookingType.SCHEDULED) {
                            if (isArabic) "إلغاء حجز هذا الموعد" else "Cancel Scheduled Appointment"
                        } else {
                            if (isArabic) "إلغاء التذكرة والانسحاب من الدور" else "Cancel Ticket & Leave Line"
                        },
                        fontFamily = Tajawal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.AccentRed
                    )
                }
            }
        }
    }
}

// ==========================================
// Sub-component: Apple World-Class Empty State
// ==========================================
@Composable
private fun EmptyQueueCard(
    isArabic: Boolean,
    onExploreServices: () -> Unit,
    onSetDemoTicket: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = AppColors.Paper,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Layered Icon Badge with Warm Highlights
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .padding(top = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, AppColors.Teal.copy(alpha = 0.22f), CircleShape)
                )

                // Inner filled circle
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .clip(CircleShape)
                        .background(AppColors.SoftTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ConfirmationNumber,
                        contentDescription = null,
                        tint = AppColors.Teal,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Sparkle badge
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = AppColors.Amber,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                )
            }

            // Warm & Engaging Copy
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (isArabic) "يومك رايق! ما عندكش أي طوابير نشطة ✨" else "Your day is clear! No active queues ✨",
                    fontFamily = Tajawal,
                    fontSize = 17.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isArabic) "وفّر وقتك في صالات الانتظار.. ابحث عن عيادتك أو وجهتك القادمة واحجز دورك وأنت في مكانك."
                    else "Enjoy your free time and skip waiting rooms. Discover nearby clinics, salons, and offices to book your turn ahead.",
                    fontFamily = Tajawal,
                    fontSize = 12.5.sp,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // Micro Value Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FeaturePill(icon = Icons.Rounded.Notifications, text = if (isArabic) "تنبيه قبل دورك" else "Alerts")
                    FeaturePill(icon = Icons.Rounded.Timer, text = if (isArabic) "توفير الوقت" else "Save time")
                    FeaturePill(icon = Icons.Rounded.VerifiedUser, text = if (isArabic) "بدون زحام" else "No crowd")
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary: Discover Queues
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 1.5.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick { onExploreServices() }
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(AppColors.Teal, AppColors.DeepTeal)
                                )
                            )
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isArabic) "استكشاف الطوابير والعيادات القريبة" else "Explore Nearby Queues",
                                fontFamily = Tajawal,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Secondary: Demo Ticket for instant preview
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.SoftTeal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick { onSetDemoTicket() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = AppColors.Amber,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "تجربة تذكرة نشطة (#A-12)" else "Preview Demo Ticket (#A-12)",
                            fontFamily = Tajawal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Ink
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturePill(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = AppColors.Background
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.Teal,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = text,
                fontFamily = Tajawal,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextSecondary
            )
        }
    }
}

// ==========================================
// Sub-component: Past Visits History Section
// ==========================================
@Composable
private fun HistorySection(
    historyItems: List<TicketHistoryItem>,
    isArabic: Boolean,
    onExploreServices: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = AppColors.Teal,
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    text = if (isArabic) "الزيارات السابقة" else "Past Visits",
                    fontFamily = Tajawal,
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = AppColors.SoftTeal
            ) {
                Text(
                    text = "${historyItems.size}",
                    fontFamily = Tajawal,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Teal,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            historyItems.forEach { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick { onExploreServices() },
                    shape = RoundedCornerShape(18.dp),
                    color = AppColors.Paper,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Icon Squircle
                        val isCompleted = item.status == TicketStatus.COMPLETED
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCompleted) AppColors.SoftTeal else AppColors.AccentRed.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Rounded.Verified else Icons.Rounded.Cancel,
                                contentDescription = null,
                                tint = if (isCompleted) AppColors.Teal else AppColors.AccentRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Info Body
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.localizedServiceName(isArabic),
                                fontFamily = Tajawal,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary,
                                maxLines = 2,
                                lineHeight = 17.5.sp,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = item.localizedBranch(isArabic),
                                    fontFamily = Tajawal,
                                    fontSize = 11.5.sp,
                                    color = AppColors.TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text("•", color = AppColors.TextTertiary, fontSize = 9.sp)
                                Text(
                                    text = item.dateString,
                                    fontFamily = Tajawal,
                                    fontSize = 11.sp,
                                    color = AppColors.TextTertiary
                                )
                            }
                        }

                        // Ticket & Status Pill
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AppColors.SoftTeal
                            ) {
                                Text(
                                    text = "#${item.ticketNumber}",
                                    fontFamily = Tajawal,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AppColors.Ink,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = if (isCompleted) {
                                    if (isArabic) "مكتملة (${item.durationMinutes}د)" else "Completed"
                                } else {
                                    if (isArabic) "ملغاة" else "Cancelled"
                                },
                                fontFamily = Tajawal,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) AppColors.Success else AppColors.AccentRed
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// Helper: Dashed Divider Line (1:1 with iOS StrokeStyle(lineWidth: 1, dash: [5, 4]))
// ==========================================
@Composable
private fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = Color(0x300B4C56),
    dashWidth: Dp = 5.dp,
    dashGap: Dp = 4.dp,
    thickness: Dp = 1.dp
) {
    Canvas(modifier = modifier.height(thickness)) {
        val dashWidthPx = dashWidth.toPx()
        val dashGapPx = dashGap.toPx()
        val thicknessPx = thickness.toPx()
        var currentX = 0f
        while (currentX < size.width) {
            val endX = minOf(currentX + dashWidthPx, size.width)
            if (endX > currentX) {
                drawLine(
                    color = color,
                    start = Offset(currentX, size.height / 2),
                    end = Offset(endX, size.height / 2),
                    strokeWidth = thicknessPx,
                    cap = StrokeCap.Butt
                )
            }
            currentX += dashWidthPx + dashGapPx
        }
    }
}
