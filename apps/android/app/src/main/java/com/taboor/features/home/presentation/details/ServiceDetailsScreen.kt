package com.taboor.features.home.presentation.details

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.components.TaboorMapView
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
import com.taboor.core.location.TaboorPrecomputedRoutes
import com.taboor.core.navigation.NavigationAppBottomSheet
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick
import com.taboor.features.home.data.repository.ServiceRepository
import com.taboor.features.home.domain.model.AppointmentSlot
import com.taboor.features.home.domain.model.ServiceBranch
import com.taboor.features.home.domain.model.ServicePlace
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class BookingMode { LIVE_QUEUE, SCHEDULED }

// Taboor Warm Cream design system tokens
private val M3Bg = AppColors.Background
private val M3Surface = AppColors.Paper
private val M3Border = AppColors.RowDivider
private val M3TextPrimary = AppColors.TextPrimary
private val M3TextSecondary = AppColors.TextSecondary
private val M3TextTertiary = AppColors.TextTertiary
private val M3Primary = AppColors.Ink
private val M3PrimaryDark = AppColors.DeepTeal
private val M3PrimaryContainer = AppColors.SoftTeal
private val M3Success = AppColors.Success
private val M3Warning = AppColors.Warning

@Composable
fun ServiceDetailsScreen(
    place: ServicePlace,
    onNavigateBack: () -> Unit,
    onJoinedQueue: () -> Unit = {}
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val isArabic = LocaleManager.isArabic
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler(onBack = onNavigateBack)

    val favoriteIds by ServiceRepository.favoritePlaceIds.collectAsState()
    val isFav = favoriteIds.contains(place.id)

    var showNavigationSheet by remember { mutableStateOf(false) }
    var bookingMode by remember { mutableStateOf(BookingMode.LIVE_QUEUE) }
    var selectedBranchIndex by remember {
        mutableIntStateOf(
            place.branches.indexOfFirst { it.isOpen }.takeIf { it >= 0 } ?: 0
        )
    }
    var selectedSlotIndex by remember { mutableIntStateOf(1) } // Default 11:30 AM
    var selectedDateDay by remember { mutableIntStateOf(0) } // 0: اليوم, 1: غداً, 2: بعد غد
    var isJoining by remember { mutableStateOf(false) }

    val selectedBranch = place.branches.getOrElse(selectedBranchIndex) { place.branches.first() }
    val slots = ServiceRepository.availableAppointmentSlots
    val selectedSlot = slots.getOrElse(selectedSlotIndex) { slots.first() }

    val estMinutes = remember(selectedBranch) {
        TaboorPrecomputedRoutes.getEstimatedMinutes(
            selectedBranch.id,
            selectedBranch.lat ?: 30.0585,
            selectedBranch.lng ?: 31.3466
        )
    }

    val displayDistance = remember(selectedBranch) {
        TaboorPrecomputedRoutes.getDistanceKm(
            selectedBranch.id,
            selectedBranch.lat ?: 30.0585,
            selectedBranch.lng ?: 31.3466
        )
    }

    fun sharePlace() {
        val shareText = if (isArabic) {
            "تفقد ${place.localizedName(true)} على تطبيق طابور: احجز دورك أو موعدك مسبقاً وتجنب الزحام!"
        } else {
            "Check out ${place.localizedName(false)} on Taboor app: Book your queue ticket and skip the wait!"
        }
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareChooser = Intent.createChooser(sendIntent, if (isArabic) "مشاركة المكان" else "Share Place")
        shareChooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(shareChooser)
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(12.dp),
                    containerColor = Color(0xFF1F2937),
                    contentColor = Color.White
                )
            }
        },
        containerColor = M3Bg,
        bottomBar = {
            // Material 3 Clean Bottom Bar
            Surface(
                color = M3Surface,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, M3Border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {
                            if (!isJoining) {
                                isJoining = true
                                coroutineScope.launch {
                                    delay(400)
                                    isJoining = false
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (bookingMode == BookingMode.LIVE_QUEUE) {
                                        ServiceRepository.joinQueue(place, selectedBranch)
                                    } else {
                                        val dayName = if (isArabic) {
                                            when (selectedDateDay) {
                                                0 -> "اليوم"
                                                1 -> "غداً"
                                                else -> "بعد غد"
                                            }
                                        } else {
                                            when (selectedDateDay) {
                                                0 -> "Today"
                                                1 -> "Tomorrow"
                                                else -> "After Tomorrow"
                                            }
                                        }
                                        ServiceRepository.bookScheduledTicket(place, selectedBranch, dayName, selectedSlot)
                                    }
                                    onJoinedQueue()
                                }
                            }
                        },
                        enabled = selectedBranch.isOpen && !isJoining,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = M3Primary,
                            disabledContainerColor = Color(0xFFD1D5DB)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .bounceClick(scaleDown = 0.98f)
                    ) {
                        if (isJoining) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (bookingMode == BookingMode.LIVE_QUEUE) Icons.Rounded.ConfirmationNumber else Icons.Rounded.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(19.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (selectedBranch.isOpen) {
                                        if (bookingMode == BookingMode.LIVE_QUEUE) {
                                            if (isArabic) "انضم إلى الطابور (${selectedBranch.localizedLabel(true)})"
                                            else "Join Queue (${selectedBranch.localizedLabel(false)})"
                                        } else {
                                            if (isArabic) "تأكيد حجز الموعد (${selectedSlot.localizedTime(true)})"
                                            else "Confirm Appointment (${selectedSlot.localizedTime(false)})"
                                        }
                                    } else {
                                        strings.serviceClosed
                                    },
                                    fontFamily = Tajawal,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ==========================================
            // 1. EDGE-TO-EDGE HERO MAP HEADER (Android / Google Maps style)
            // ==========================================
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    // Full-width vector map
                    TaboorMapView(
                        branchLat = selectedBranch.lat ?: 30.0444,
                        branchLng = selectedBranch.lng ?: 31.2357,
                        branchTitle = "${place.localizedName(isArabic)} - ${selectedBranch.localizedLabel(isArabic)}",
                        branchAddress = selectedBranch.localizedAddress(isArabic),
                        branchId = selectedBranch.id,
                        isFullScreen = false,
                        showControls = false,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Subtle top gradient scrim for status bar readability
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent)
                                )
                            )
                    )

                    // Top App Bar Icons (Floating Tonal Action Buttons)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Back Button
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .bounceClick {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onNavigateBack()
                                },
                            shape = CircleShape,
                            color = M3Surface.copy(alpha = 0.95f),
                            shadowElevation = 3.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back",
                                    tint = M3TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Right Actions: Favorite & Share
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .bounceClick {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        ServiceRepository.toggleFavorite(place.id)
                                    },
                                shape = CircleShape,
                                color = M3Surface.copy(alpha = 0.95f),
                                shadowElevation = 3.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (isFav) Color(0xFFEF4444) else M3TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .bounceClick {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        sharePlace()
                                    },
                                shape = CircleShape,
                                color = M3Surface.copy(alpha = 0.95f),
                                shadowElevation = 3.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = "Share",
                                        tint = M3TextPrimary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Floating Chips over Map: Directions Button + ETA
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Directions Button
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = M3Primary,
                            shadowElevation = 4.dp,
                            modifier = Modifier.bounceClick {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showNavigationSheet = true
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Navigation,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (isArabic) "الاتجاهات" else "Directions",
                                    fontFamily = Tajawal,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // ETA Pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = M3Surface.copy(alpha = 0.96f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, M3Border),
                            shadowElevation = 3.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DirectionsCar,
                                    contentDescription = null,
                                    tint = M3Primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isArabic) "$estMinutes دقيقة قيادة" else "$estMinutes min drive",
                                    fontFamily = Tajawal,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = M3TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 2. PLACE HEADER CARD (Clean Material 3 Card)
            // ==========================================
            item {
                Surface(
                    color = M3Surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = place.localizedName(isArabic),
                                    fontFamily = Tajawal,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = M3TextPrimary,
                                    lineHeight = 26.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Rating
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Star,
                                            contentDescription = null,
                                            tint = M3Warning,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "${place.starRating}",
                                            fontFamily = Tajawal,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = M3TextPrimary
                                        )
                                    }

                                    Text(text = "•", color = M3TextTertiary, fontSize = 12.sp)

                                    Text(
                                        text = place.category.localizedTitle(isArabic),
                                        fontFamily = Tajawal,
                                        fontSize = 13.sp,
                                        color = M3TextSecondary
                                    )

                                    Text(text = "•", color = M3TextTertiary, fontSize = 12.sp)

                                    // Open status
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (selectedBranch.isOpen) M3Success else Color(0xFFEF4444))
                                        )
                                        Text(
                                            text = if (selectedBranch.isOpen) (if (isArabic) "مفتوح الآن" else "Open") else strings.serviceClosed,
                                            fontFamily = Tajawal,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (selectedBranch.isOpen) M3Success else Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }

                            // Place Icon
                            Surface(
                                modifier = Modifier.size(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = place.color.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = place.icon,
                                        contentDescription = null,
                                        tint = place.color,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Stats Strip (Material 3 Surface Pills)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3F4F6))
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isArabic) "في الانتظار" else "Waiting",
                                    fontFamily = Tajawal,
                                    fontSize = 11.5.sp,
                                    color = M3TextSecondary
                                )
                                Text(
                                    text = "${selectedBranch.waiting} ${if (isArabic) "أفراد" else "in line"}",
                                    fontFamily = Tajawal,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedBranch.waiting <= 2) M3Success else M3Warning
                                )
                            }

                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(M3Border))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isArabic) "وقت الانتظار" else "Est. Wait",
                                    fontFamily = Tajawal,
                                    fontSize = 11.5.sp,
                                    color = M3TextSecondary
                                )
                                Text(
                                    text = "~${selectedBranch.estWaitMinutes} ${if (isArabic) "دقيقة" else "min"}",
                                    fontFamily = Tajawal,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = M3Primary
                                )
                            }

                            Box(modifier = Modifier.width(1.dp).height(24.dp).background(M3Border))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isArabic) "المسافة" else "Distance",
                                    fontFamily = Tajawal,
                                    fontSize = 11.5.sp,
                                    color = M3TextSecondary
                                )
                                Text(
                                    text = "$displayDistance ${if (isArabic) "كم" else "km"}",
                                    fontFamily = Tajawal,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = M3TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 3. BRANCH SELECTION SECTION (Material 3 Clean List Style)
            // ==========================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isArabic) "اختر الفرع" else "Select Branch",
                            fontFamily = Tajawal,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = M3TextPrimary
                        )

                        Text(
                            text = if (isArabic) "${place.branches.size} فروع متاحة" else "${place.branches.size} available",
                            fontFamily = Tajawal,
                            fontSize = 12.5.sp,
                            color = M3TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Clean Material 3 Branch Cards
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        place.branches.forEachIndexed { index, branch ->
                            val isSelected = index == selectedBranchIndex
                            val isBranchOpen = branch.isOpen
                            val branchDist = TaboorPrecomputedRoutes.getDistanceKm(
                                branch.id,
                                branch.lat ?: 30.0585,
                                branch.lng ?: 31.3466
                            )

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (isBranchOpen) {
                                            Modifier.bounceClick(scaleDown = 0.99f) {
                                                if (selectedBranchIndex != index) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    selectedBranchIndex = index
                                                }
                                            }
                                        } else {
                                            Modifier.clickable(
                                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                // When clicked, closed branch cannot be selected
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                android.widget.Toast.makeText(
                                                    context,
                                                    if (isArabic) "عذراً، هذا الفرع مغلق حالياً ولا يمكن اختياره"
                                                    else "Sorry, this branch is closed and cannot be selected",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    ),
                                shape = RoundedCornerShape(14.dp),
                                color = when {
                                    !isBranchOpen -> Color(0xFFF9FAFB)
                                    isSelected -> M3PrimaryContainer
                                    else -> M3Surface
                                },
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected && isBranchOpen) 1.5.dp else 1.dp,
                                    color = when {
                                        !isBranchOpen -> Color(0xFFE5E7EB)
                                        isSelected -> M3Primary
                                        else -> M3Border
                                    }
                                ),
                                shadowElevation = if (isSelected && isBranchOpen) 2.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                        .alpha(if (isBranchOpen) 1f else 0.55f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Native Material 3 Radio Button
                                    RadioButton(
                                        selected = isSelected && isBranchOpen,
                                        onClick = if (isBranchOpen) {
                                            {
                                                if (selectedBranchIndex != index) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    selectedBranchIndex = index
                                                }
                                            }
                                        } else null,
                                        enabled = isBranchOpen,
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = M3Primary,
                                            unselectedColor = M3TextTertiary,
                                            disabledSelectedColor = Color(0xFF9CA3AF),
                                            disabledUnselectedColor = Color(0xFFD1D5DB)
                                        ),
                                        modifier = Modifier.size(22.dp)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Branch Name & Street
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = branch.localizedLabel(isArabic),
                                                fontFamily = Tajawal,
                                                fontSize = 15.sp,
                                                fontWeight = if (isSelected && isBranchOpen) FontWeight.Bold else FontWeight.SemiBold,
                                                color = when {
                                                    !isBranchOpen -> M3TextSecondary
                                                    isSelected -> M3PrimaryDark
                                                    else -> M3TextPrimary
                                                }
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = branch.localizedAddress(isArabic) ?: "",
                                            fontFamily = Tajawal,
                                            fontSize = 12.5.sp,
                                            color = M3TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Queue status & Distance
                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        if (isBranchOpen) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (branch.waiting <= 2) Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
                                            ) {
                                                Text(
                                                    text = if (isArabic) "${branch.waiting} انتظار" else "${branch.waiting} in line",
                                                    fontFamily = Tajawal,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (branch.waiting <= 2) Color(0xFF065F46) else Color(0xFF92400E),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }

                                            Text(
                                                text = "$branchDist ${if (isArabic) "كم" else "km"}",
                                                fontFamily = Tajawal,
                                                fontSize = 11.5.sp,
                                                color = M3TextSecondary
                                            )
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFFEE2E2)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Lock,
                                                        contentDescription = null,
                                                        tint = Color(0xFF991B1B),
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = if (isArabic) "مغلق حالياً" else "Closed",
                                                        fontFamily = Tajawal,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF991B1B)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = if (isArabic) "خارج ساعات العمل" else "Off hours",
                                                fontFamily = Tajawal,
                                                fontSize = 10.5.sp,
                                                color = M3TextTertiary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 4. BOOKING MODE TABS (Material 3 Segmented Control)
            // ==========================================
            item {
                Surface(
                    color = M3Surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, M3Border)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isArabic) "نوع الحجز" else "Booking Option",
                            fontFamily = Tajawal,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = M3TextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Segmented Button Container
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF3F4F6))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val isLive = bookingMode == BookingMode.LIVE_QUEUE
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .bounceClick {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        bookingMode = BookingMode.LIVE_QUEUE
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isLive) M3Primary else Color.Transparent,
                                shadowElevation = if (isLive) 2.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.FlashOn,
                                        contentDescription = null,
                                        tint = if (isLive) Color.White else M3TextSecondary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isArabic) "طابور فوري" else "Live Queue",
                                        fontFamily = Tajawal,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLive) Color.White else M3TextSecondary
                                    )
                                }
                            }

                            val isScheduled = bookingMode == BookingMode.SCHEDULED
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .bounceClick {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        bookingMode = BookingMode.SCHEDULED
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isScheduled) M3Primary else Color.Transparent,
                                shadowElevation = if (isScheduled) 2.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.EventAvailable,
                                        contentDescription = null,
                                        tint = if (isScheduled) Color.White else M3TextSecondary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isArabic) "حجز موعد مسبق" else "Scheduled",
                                        fontFamily = Tajawal,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isScheduled) Color.White else M3TextSecondary
                                    )
                                }
                            }
                        }

                        // Scheduled Appointment Details
                        AnimatedVisibility(visible = bookingMode == BookingMode.SCHEDULED) {
                            Column(modifier = Modifier.padding(top = 14.dp)) {
                                Text(
                                    text = if (isArabic) "اختر اليوم" else "Select Day",
                                    fontFamily = Tajawal,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = M3TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(
                                        if (isArabic) "اليوم" else "Today",
                                        if (isArabic) "غداً" else "Tomorrow",
                                        if (isArabic) "بعد غد" else "After Tomorrow"
                                    ).forEachIndexed { index, dayLabel ->
                                        val isDaySelected = selectedDateDay == index
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .bounceClick {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    selectedDateDay = index
                                                },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isDaySelected) M3PrimaryContainer else Color(0xFFF3F4F6),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isDaySelected) M3Primary else M3Border
                                            )
                                        ) {
                                            Text(
                                                text = dayLabel,
                                                fontFamily = Tajawal,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDaySelected) M3PrimaryDark else M3TextPrimary,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = if (isArabic) "اختر الوقت المتاح" else "Available Time",
                                    fontFamily = Tajawal,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = M3TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    slots.chunked(3).forEach { rowSlots ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            rowSlots.forEach { slot ->
                                                val isSlotSelected = slots.indexOf(slot) == selectedSlotIndex
                                                Surface(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .bounceClick {
                                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            selectedSlotIndex = slots.indexOf(slot)
                                                        },
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = if (isSlotSelected) M3PrimaryContainer else Color(0xFFF3F4F6),
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.5.dp,
                                                        if (isSlotSelected) M3Primary else M3Border
                                                    )
                                                ) {
                                                    Text(
                                                        text = slot.localizedTime(isArabic),
                                                        fontFamily = Tajawal,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isSlotSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSlotSelected) M3PrimaryDark else M3TextPrimary,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.padding(vertical = 8.dp)
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
            }

            // ==========================================
            // 5. ABOUT & WORKING HOURS
            // ==========================================
            item {
                Surface(
                    color = M3Surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, M3Border)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val aboutText = place.localizedAbout(isArabic)
                        if (!aboutText.isNullOrBlank()) {
                            Text(
                                text = strings.serviceAbout,
                                fontFamily = Tajawal,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = M3TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = aboutText,
                                fontFamily = Tajawal,
                                fontSize = 13.sp,
                                color = M3TextSecondary,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        val workingHours = place.workingHours
                        if (!workingHours.isNullOrBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF3F4F6))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = M3Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${strings.serviceWorkingHours}: $workingHours",
                                    fontFamily = Tajawal,
                                    fontSize = 12.5.sp,
                                    color = M3TextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNavigationSheet) {
        NavigationAppBottomSheet(
            destinationLat = selectedBranch.lat ?: 30.0444,
            destinationLng = selectedBranch.lng ?: 31.2357,
            destinationTitle = "${place.localizedName(isArabic)} - ${selectedBranch.localizedLabel(isArabic)}",
            onDismiss = { showNavigationSheet = false }
        )
    }
}
