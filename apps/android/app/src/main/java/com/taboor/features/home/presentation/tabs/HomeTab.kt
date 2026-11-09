package com.taboor.features.home.presentation.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.components.TaboorLogoIcon
import com.taboor.core.components.TaboorMapView
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
import com.taboor.core.location.TaboorPrecomputedRoutes
import com.taboor.core.services.LocationService
import com.taboor.core.services.SessionManager
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Inter
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick
import com.taboor.core.utils.pulseEffect
import com.taboor.features.home.data.repository.ServiceRepository
import com.taboor.features.home.domain.model.ServiceCategory
import com.taboor.features.home.domain.model.ServicePlace
import com.taboor.features.home.domain.model.icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Customer discovery feed and real-time queue hub.
 * Pixel-perfect reproduction of Flutter's `home_tab.dart`:
 * - Smart Location & Greeting Header with avatar letter + online status
 * - Interactive Live Search with tune filter icon & Quick Suggestions Pills
 * - Hero Active Virtual Queue Ticket Card (gradient, amber dot, Inter ticket number, drive alert)
 * - Quick Action Highlights Bar (QR Scanner, Time Saved impact)
 * - Category Discovery Header & Capsule Pills
 * - "Fast Moving Queues" Horizontal Highlights
 * - Local Business Feed with Live Waiting Counters & "احجز الآن" CTA
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTab(
    onNavigateToDetails: (ServicePlace) -> Unit,
    onNavigateToAllServices: (ServiceCategory?) -> Unit,
    onNavigateToQueueTab: () -> Unit,
    onOpenQrScanner: () -> Unit,
    onOpenLiveTracking: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val strings = LocalAppStrings.current
    val isArabic = LocaleManager.isArabic
    val currentUser by SessionManager.currentUser.collectAsState()
    val activeTicket by ServiceRepository.activeTicket.collectAsState()
    val selectedLocation by ServiceRepository.selectedLocation.collectAsState()
    val favoriteIds by ServiceRepository.favoritePlaceIds.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showLocationSheet by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = when {
        currentHour < 12 -> strings.homeGoodMorning
        currentHour < 18 -> strings.homeGoodAfternoon
        else -> strings.homeGoodEvening
    }

    val allPlaces = ServiceRepository.places
    val filteredPlaces = remember(searchQuery) {
        allPlaces.filter { it.matchesQuery(searchQuery) }
    }
    val fastPlaces = remember {
        allPlaces.filter { it.totalWaiting <= 4 }
    }

    val displayName = currentUser?.fullName?.takeIf { it.isNotBlank() } ?: if (isArabic) "محمد أحمد" else "Mohamed Ahmed"
    val avatarInitial = displayName.trim().take(1).uppercase()

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                delay(800)
                isRefreshing = false
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = statusBarTop + 6.dp, bottom = 110.dp)
        ) {
        // ==========================================
        // 1. Unified Brand & Location Header (Matching iOS 1:1)
        // ==========================================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand Side: Logo Icon + "taboor." + Location Dropdown
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TaboorLogoIcon(size = 38.dp)

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        // Wordmark strictly LTR: "taboor."
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "taboor",
                                    fontFamily = Inter,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.8).sp,
                                    color = AppColors.Ink
                                )
                                Text(
                                    text = ".",
                                    fontFamily = Inter,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.8).sp,
                                    color = AppColors.Amber
                                )
                            }
                        }

                        // Location chip button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .bounceClick(scaleDown = 0.95f) { showLocationSheet = true }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = AppColors.Teal,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = selectedLocation,
                                fontFamily = Tajawal,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.widthIn(max = 200.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = null,
                                tint = AppColors.Teal,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action Buttons: QR Scanner & Notifications Bell
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // QR Scanner button
                    Surface(
                        shape = RoundedCornerShape(11.dp),
                        color = AppColors.CardBackground,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .bounceClick(scaleDown = 0.94f) { onOpenQrScanner() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.QrCodeScanner,
                                contentDescription = strings.scanQrTitle,
                                tint = AppColors.Ink,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Notification bell button with amber badge
                    Surface(
                        shape = RoundedCornerShape(11.dp),
                        color = AppColors.CardBackground,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .size(38.dp)
                            .bounceClick(scaleDown = 0.94f) { onNavigateToQueueTab() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Notifications,
                                contentDescription = null,
                                tint = AppColors.Ink,
                                modifier = Modifier.size(18.dp)
                            )
                            // Amber notification indicator dot
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 7.dp, end = 7.dp)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.Amber)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. Interactive Search Bar Card (Matching iOS 1:1)
        // ==========================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                // Search Bar Container
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AppColors.CardBackground,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = AppColors.Teal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = if (isArabic) "ابحث عن دكتور، عيادة، صالون، خدمة..." else "Search clinic, doctor, salon, service...",
                                    fontFamily = Tajawal,
                                    fontSize = 13.sp,
                                    color = AppColors.TextSecondary
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = Tajawal,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = null,
                                    tint = AppColors.TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            // Filter tune icon in softTeal capsule
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppColors.SoftTeal)
                                    .clickable { onNavigateToAllServices(null) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = null,
                                    tint = AppColors.Teal,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. Hero Active Virtual Queue Ticket Card or Booking Invitation
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(18.dp))
            val ticket = activeTicket
            if (ticket != null) {
                // 1:1 with iOS activeTicketCard (Uber Trip Tray with Live Route Map)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .bounceClick(scaleDown = 0.985f) { onOpenLiveTracking() },
                    shape = RoundedCornerShape(22.dp),
                    color = AppColors.CardBackground,
                    shadowElevation = 1.5.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // 1. Top Wide Map View (Tapping opens the full-screen live tracking view!)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(125.dp)
                                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                        ) {
                            // Real interactive OpenStreetMap / CartoDB map
                            TaboorMapView(
                                branchLat = ticket.latitude ?: 30.0585,
                                branchLng = ticket.longitude ?: 31.3466,
                                branchTitle = ticket.localizedServiceName(isArabic),
                                branchAddress = ticket.localizedBranch(isArabic),
                                branchId = ticket.branchId,
                                isFullScreen = false,
                                showControls = false,
                                showInfoChip = false,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Subtle gradient overlay for depth & tap-to-expand
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.08f))
                                        )
                                    )
                                    .clickable { onOpenLiveTracking() }
                            )

                            // Floating Live ETA Badge & Expand CTA
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Live Status Floating Badge
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(alpha = 0.92f),
                                    shadowElevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(AppColors.AccentGreen)
                                                .pulseEffect(minScale = 0.8f, maxScale = 1.3f)
                                        )
                                        Text(
                                            text = if (isArabic) "دورك خلال ${ticket.estMinutes} دقائق" else "Your turn in ~${ticket.estMinutes}m",
                                            fontFamily = Tajawal,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.TextPrimary
                                        )
                                    }
                                }

                                // Expand / Live Map Hint Button
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(alpha = 0.92f),
                                    shadowElevation = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Map,
                                            contentDescription = null,
                                            tint = AppColors.Teal,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = if (isArabic) "تتبع مباشر" else "Live Map",
                                            fontFamily = Tajawal,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.Teal
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = AppColors.RowDivider
                        )

                        // 2. Bottom Ticket Tray
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Title Full Width
                            Text(
                                text = ticket.localizedServiceName(isArabic),
                                fontFamily = Tajawal,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppColors.TextPrimary,
                                maxLines = 2,
                                lineHeight = 22.sp,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Subtitle & Ticket Pill Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${ticket.localizedBranch(isArabic)} • ${ticket.localizedCounter(isArabic)}",
                                    fontFamily = Tajawal,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.TextSecondary,
                                    maxLines = 2,
                                    lineHeight = 16.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Compact Ticket Pill
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = AppColors.SoftTeal.copy(alpha = 0.8f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = if (isArabic) "تذكرتك" else "Ticket",
                                            fontFamily = Tajawal,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = AppColors.TextSecondary
                                        )
                                        Text(
                                            text = ticket.ticketNumber,
                                            fontFamily = Inter,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = AppColors.Teal
                                        )
                                    }
                                }
                            }

                            // Progress Bar
                            LinearProgressIndicator(
                                progress = { ticket.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = AppColors.Teal,
                                trackColor = AppColors.SoftTeal
                            )

                            // Stats & Live Track CTA Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // People ahead with amber dot
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
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

                                // Primary Live Tracking CTA
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    shadowElevation = 1.5.dp,
                                    modifier = Modifier.bounceClick(scaleDown = 0.96f) { onOpenLiveTracking() }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(AppColors.Teal, AppColors.DeepTeal)
                                                )
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.5.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Navigation,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Text(
                                                text = if (isArabic) "تتبع مباشر" else "Live Track",
                                                fontFamily = Tajawal,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Refined Apple HIG Promo & Hero Card (When No Ticket is Active) - 1:1 with iOS emptyQueueHeroCard
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .bounceClick(scaleDown = 0.98f) { onNavigateToAllServices(null) },
                    shape = RoundedCornerShape(20.dp),
                    color = AppColors.CardBackground,
                    shadowElevation = 1.5.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Radar Live Tag
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(AppColors.Teal.copy(alpha = 0.10f))
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.Teal)
                                    .pulseEffect(minScale = 0.85f, maxScale = 1.35f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "طوابير سريعة بالقرب منك • $selectedLocation" else "Fast queues near you • $selectedLocation",
                                fontFamily = Tajawal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Teal
                            )
                        }

                        // 2. Main Row: Title / Subtitle & Radar Vector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "احجز دورك قبل ما توصل" else "Reserve your spot ahead",
                                    fontFamily = Tajawal,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                                Text(
                                    text = if (isArabic) "انضم لطابورك عن بُعد، وتابع دورك في الطريق بدون انتظار على الكراسي" else "Join remotely & track your turn on the go without waiting in lines",
                                    fontFamily = Tajawal,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp,
                                    color = AppColors.TextSecondary,
                                    maxLines = 2
                                )
                                // CTA Button
                                Button(
                                    onClick = { onNavigateToAllServices(null) },
                                    shape = RoundedCornerShape(30.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Teal),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .height(34.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(
                                            text = if (isArabic) "استكشف الطوابير" else "Explore Queues",
                                            fontFamily = Tajawal,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Radar Vector Illustration
                            RadarVectorIllustration(modifier = Modifier.size(76.dp))
                        }

                        // 3. Integrated Quick Utility Shortcuts Strip
                        HorizontalDivider(
                            color = AppColors.RowDivider,
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Shortcut 1: Scan QR
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AppColors.Background,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenQrScanner() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.QrCodeScanner,
                                        contentDescription = null,
                                        tint = AppColors.TextPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isArabic) "مسح QR الشباك" else "Scan QR",
                                        fontFamily = Tajawal,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Shortcut 2: Fast wait time
                            val fastestPlace = fastPlaces.firstOrNull()
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AppColors.Background,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        if (fastestPlace != null) onNavigateToDetails(fastestPlace)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Bolt,
                                        contentDescription = null,
                                        tint = AppColors.Amber,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isArabic) "أقل انتظار: 3 د" else "Min wait: 3m",
                                        fontFamily = Tajawal,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Shortcut 3: Map Radar
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AppColors.Background,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { showLocationSheet = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Map,
                                        contentDescription = null,
                                        tint = AppColors.Teal,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isArabic) "خريطة الطوابير" else "Map Radar",
                                        fontFamily = Tajawal,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. Categories Section (2-Column Grid matching Swift 1:1)
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "التصنيفات الرئيسية" else "Main Categories",
                    fontFamily = Tajawal,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Text(
                    text = if (isArabic) "عرض الكل" else "See all",
                    fontFamily = Tajawal,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Teal,
                    modifier = Modifier.clickable { onNavigateToAllServices(null) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val allCats = listOf(
                ServiceCategory.CLINICS,
                ServiceCategory.SALONS,
                ServiceCategory.GARAGES,
                ServiceCategory.OFFICES
            )
            val pairs = allCats.chunked(2)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pairs.forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowCategories.forEach { category ->
                            val placesCount = allPlaces.count { it.category == category }
                            val (catBg, catColor) = when (category) {
                                ServiceCategory.CLINICS -> Pair(Color(0xFFE6F7F5), Color(0xFF0D9488))
                                ServiceCategory.SALONS -> Pair(Color(0xFFF3E8FF), Color(0xFF7C3AED))
                                ServiceCategory.GARAGES -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706))
                                ServiceCategory.OFFICES -> Pair(Color(0xFFEFF6FF), Color(0xFF2563EB))
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .bounceClick(scaleDown = 0.97f) { onNavigateToAllServices(category) },
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shadowElevation = 1.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(13.dp))
                                            .background(catBg)
                                            .border(1.dp, catColor.copy(alpha = 0.25f), RoundedCornerShape(13.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = category.icon,
                                            contentDescription = null,
                                            tint = catColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = category.localizedTitle(isArabic),
                                            fontFamily = Tajawal,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isArabic) "$placesCount أماكن" else "$placesCount places",
                                            fontFamily = Tajawal,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF64748B)
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
        // 6. "Fast Moving Queues" Live Highlights
        // ==========================================
        if (searchQuery.isEmpty() && fastPlaces.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .background(AppColors.Success.copy(alpha = 0.12f))
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = AppColors.Success,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "طوابير سريعة الآن" else "Fast Queues Right Now",
                        fontFamily = Tajawal,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Ink
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Badge: "< 10 min"
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppColors.Success.copy(alpha = 0.1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.Success)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isArabic) "أقل من 10 د" else "< 10 min",
                                fontFamily = Tajawal,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Success
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(fastPlaces) { place ->
                        val branch = place.branches.first()
                        val estMinutes = (place.totalWaiting * 3.5).toInt().coerceIn(2, 10)

                        Surface(
                            modifier = Modifier
                                .width(260.dp)
                                .bounceClick(scaleDown = 0.965f) { onNavigateToDetails(place) },
                            shape = RoundedCornerShape(18.dp),
                            color = AppColors.Paper,
                            shadowElevation = 1.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Top row: icon + name + star & distance
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(place.color.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = place.icon,
                                            contentDescription = null,
                                            tint = place.color,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = place.localizedName(isArabic),
                                            fontFamily = Tajawal,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.Ink,
                                            maxLines = 2,
                                            lineHeight = 17.5.sp,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.Star,
                                                contentDescription = null,
                                                tint = AppColors.Amber,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "%.1f".format(place.starRating),
                                                fontFamily = Tajawal,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppColors.Ink
                                            )
                                            branch.distanceKm?.let { dist ->
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text("•", color = AppColors.Gray400, fontSize = 8.sp)
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    text = "$dist ${if (isArabic) "كم" else "km"}",
                                                    fontFamily = Tajawal,
                                                    fontSize = 11.sp,
                                                    color = AppColors.Gray600
                                                )
                                            }
                                        }
                                    }
                                }

                                // Bottom Row: Live wait pill + Action button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Status badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AppColors.Success.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.Bolt,
                                                contentDescription = null,
                                                tint = AppColors.Success,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = if (isArabic) "~$estMinutes د • ${place.totalWaiting} قبلك" else "~$estMinutes min • ${place.totalWaiting} ahead",
                                                fontFamily = Tajawal,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppColors.Success
                                            )
                                        }
                                    }

                                    // Quick Join CTA
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(AppColors.Teal)
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (isArabic) "احجز دورك" else "Join",
                                                fontFamily = Tajawal,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppColors.Paper
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                                contentDescription = null,
                                                tint = AppColors.Paper,
                                                modifier = Modifier.size(9.dp)
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

        // ==========================================
        // 7. Main Discovery List
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) {
                        if (isArabic) "نتائج البحث" else "Search Results"
                    } else {
                        strings.homePopularNearYou
                    },
                    fontFamily = Tajawal,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Ink
                )
                Text(
                    text = "${filteredPlaces.size} ${if (isArabic) "أماكن" else "places"}",
                    fontFamily = Tajawal,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Gray500
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Business Cards List
        if (filteredPlaces.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SearchOff,
                        contentDescription = null,
                        tint = AppColors.Gray400,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isArabic) "لا توجد نتائج مطابقة لبحثك" else "No matching places found",
                        fontFamily = Tajawal,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Ink
                    )
                }
            }
        } else {
            items(filteredPlaces) { place ->
                val mainBranch = place.branches.first()
                val isOpen = place.branches.any { it.isOpen }
                val branchDist = TaboorPrecomputedRoutes.getDistanceKm(
                    mainBranch.id,
                    mainBranch.lat ?: 30.0585,
                    mainBranch.lng ?: 31.3466
                )
                val estDriveMin = TaboorPrecomputedRoutes.getEstimatedMinutes(
                    mainBranch.id,
                    mainBranch.lat ?: 30.0585,
                    mainBranch.lng ?: 31.3466
                )

                val (catBg, catColor) = when (place.category) {
                    ServiceCategory.CLINICS -> Pair(Color(0xFFE6F7F5), Color(0xFF0D9488))
                    ServiceCategory.SALONS -> Pair(Color(0xFFF3E8FF), Color(0xFF7C3AED))
                    ServiceCategory.GARAGES -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706))
                    ServiceCategory.OFFICES -> Pair(Color(0xFFEFF6FF), Color(0xFF2563EB))
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .bounceClick(scaleDown = 0.98f) { onNavigateToDetails(place) },
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Row 1: Icon + Name/Rating + Open/Closed Tag
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(catBg)
                                    .border(1.dp, catColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = place.icon,
                                    contentDescription = null,
                                    tint = catColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = place.localizedName(isArabic),
                                        fontFamily = Tajawal,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Icon(
                                        imageVector = Icons.Rounded.Verified,
                                        contentDescription = null,
                                        tint = Color(0xFF0D9488),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "%.1f".format(place.starRating),
                                            fontFamily = Tajawal,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    }
                                    Text("•", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                    Text(
                                        text = "${place.branches.size} ${if (isArabic) "فروع" else "branches"}",
                                        fontFamily = Tajawal,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    Text("•", color = Color(0xFFCBD5E1), fontSize = 10.sp)
                                    Text(
                                        text = if (isOpen) {
                                            if (isArabic) "مفتوح الآن" else "Open"
                                        } else {
                                            if (isArabic) "مغلق" else "Closed"
                                        },
                                        fontFamily = Tajawal,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isOpen) Color(0xFF059669) else Color(0xFFDC2626)
                                    )
                                }
                            }
                        }

                        // Row 2: Bottom Status & Action Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Waiting Pill
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isOpen) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isOpen) Icons.Rounded.Bolt else Icons.Rounded.Lock,
                                        contentDescription = null,
                                        tint = if (isOpen) Color(0xFF065F46) else Color(0xFF991B1B),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (isOpen) {
                                            if (isArabic) "${place.totalWaiting} في الانتظار" else "${place.totalWaiting} in line"
                                        } else {
                                            if (isArabic) "مغلق حالياً" else "Closed"
                                        },
                                        fontFamily = Tajawal,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOpen) Color(0xFF065F46) else Color(0xFF991B1B)
                                    )
                                }
                            }

                            // Distance and drive time
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NearMe,
                                    contentDescription = null,
                                    tint = Color(0xFF0D9488),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "$branchDist ${if (isArabic) "كم" else "km"} (~$estDriveMin د)",
                                    fontFamily = Tajawal,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F766E)
                                )
                            }

                            // Action button
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isOpen) Color(0xFF0D9488) else Color(0xFFF1F5F9),
                                modifier = Modifier.bounceClick(scaleDown = 0.94f) {
                                    onNavigateToDetails(place)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = if (isOpen) strings.joinNow else if (isArabic) "تفاصيل" else "Details",
                                        fontFamily = Tajawal,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOpen) Color.White else Color(0xFF334155)
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = null,
                                        tint = if (isOpen) Color.White else Color(0xFF64748B),
                                        modifier = Modifier.size(11.dp)
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

    if (showLocationSheet) {
        com.taboor.features.home.presentation.location.LocationPickerBottomSheet(
            currentLocation = selectedLocation,
            onLocationSelected = { loc ->
                ServiceRepository.updateSelectedLocation(loc)
            },
            onDismiss = { showLocationSheet = false }
        )
    }
}

/**
 * Crisp Vector Radar Visual mirroring iOS `radarVectorIllustration`
 */
@Composable
private fun RadarVectorIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer dashed orbit
        Canvas(modifier = Modifier.size(74.dp)) {
            drawCircle(
                color = AppColors.Teal.copy(alpha = 0.22f),
                style = Stroke(
                    width = 1.2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            )
        }

        // Mid circle
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(AppColors.Teal.copy(alpha = 0.05f))
                .border(1.dp, AppColors.Teal.copy(alpha = 0.25f), CircleShape)
        )

        // Inner core
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(AppColors.Teal.copy(alpha = 0.12f))
                .border(1.5.dp, AppColors.Teal, CircleShape)
        )

        // Center User Pin
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(AppColors.Ink),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }

        // Nearby Place 1 (Teal Clinic Pin)
        Box(
            modifier = Modifier
                .offset(x = (-22).dp, y = (-16).dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(AppColors.Teal),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(9.dp)
            )
        }

        // Nearby Place 2 (Amber Salon Pin)
        Box(
            modifier = Modifier
                .offset(x = 22.dp, y = 18.dp)
                .size(14.dp)
                .clip(CircleShape)
                .background(AppColors.Amber),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}
