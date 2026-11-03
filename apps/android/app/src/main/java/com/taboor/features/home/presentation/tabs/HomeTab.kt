package com.taboor.features.home.presentation.tabs

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
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
    onOpenQrScanner: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val strings = LocalAppStrings.current
    val isArabic = LocaleManager.isArabic
    val currentUser by SessionManager.currentUser.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var userLocationName by remember { mutableStateOf<String?>(null) }
    var isResolvingLocation by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Resolve device GPS location
    LaunchedEffect(isArabic) {
        isResolvingLocation = true
        val pos = LocationService.getCurrentPosition(context)
        val lat = pos?.first ?: LocationService.DEFAULT_LAT
        val lng = pos?.second ?: LocationService.DEFAULT_LNG
        userLocationName = LocationService.getAreaName(context, lat, lng, isArabic)
        isResolvingLocation = false
    }

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
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
        // ==========================================
        // 1. Header (Greeting + User avatar + Location Chip + QR button)
        // ==========================================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User avatar with online status badge
                Box(modifier = Modifier.size(48.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(AppColors.Teal, AppColors.DeepTeal)
                                )
                            )
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = AppColors.Teal.copy(alpha = 0.25f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = avatarInitial,
                            fontFamily = Inter,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppColors.Paper
                        )
                    }
                    // Green online badge
                    Box(
                        modifier = Modifier
                            .size(13.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(AppColors.Success)
                            .border(2.dp, AppColors.Paper, CircleShape)
                            .pulseEffect(minScale = 0.88f, maxScale = 1.25f)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Greeting & Name
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greeting,
                        fontFamily = Tajawal,
                        fontSize = 12.5.sp,
                        color = AppColors.Gray600
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = displayName,
                        fontFamily = Tajawal,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Location Chip
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AppColors.Paper,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200),
                    shadowElevation = 2.dp,
                    modifier = Modifier.clickable {
                        isResolvingLocation = true
                        val pos = LocationService.getCurrentPosition(context)
                        val lat = pos?.first ?: LocationService.DEFAULT_LAT
                        val lng = pos?.second ?: LocationService.DEFAULT_LNG
                        coroutineScope.launch {
                            userLocationName = LocationService.getAreaName(context, lat, lng, isArabic)
                            isResolvingLocation = false
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        if (isResolvingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(13.dp),
                                color = AppColors.Teal,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = AppColors.Teal,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = userLocationName ?: (if (isArabic) "ميدان التحرير" else "Tahrir Square"),
                            fontFamily = Tajawal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Ink
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // QR Scanner Quick Launch
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AppColors.Paper,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .size(40.dp)
                        .bounceClick(scaleDown = 0.92f) { onOpenQrScanner() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.QrCodeScanner,
                            contentDescription = strings.scanQrTitle,
                            tint = AppColors.Teal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. Search Bar & Quick Suggestion Pills
        // ==========================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // Search Bar Container
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = AppColors.Paper,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200.copy(alpha = 0.8f)),
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = AppColors.Gray500,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = strings.homeSearchHint,
                                    fontFamily = Tajawal,
                                    fontSize = 13.5.sp,
                                    color = AppColors.Gray400
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
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.Ink
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = null,
                                    tint = AppColors.Gray500,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            // Filter tune icon in softTeal capsule
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AppColors.SoftTeal)
                                    .clickable { onNavigateToAllServices(null) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = null,
                                    tint = AppColors.Teal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Quick Search Suggestion Pills Row (when search query is empty)
                AnimatedVisibility(visible = searchQuery.isEmpty()) {
                    val suggestions = if (isArabic) {
                        listOf(
                            Triple(Icons.Rounded.MedicalServices, "عيادة أسنان", AppColors.Teal),
                            Triple(Icons.Rounded.ContentCut, "صالون حلاقة", AppColors.Indigo),
                            Triple(Icons.Rounded.CarRepair, "صيانة سيارات", AppColors.Amber),
                            Triple(Icons.Rounded.Visibility, "ليزك وعيون", AppColors.Teal),
                            Triple(Icons.Rounded.Business, "استشارة قانونية", AppColors.Ink)
                        )
                    } else {
                        listOf(
                            Triple(Icons.Rounded.MedicalServices, "Dental Clinic", AppColors.Teal),
                            Triple(Icons.Rounded.ContentCut, "Barber & Salon", AppColors.Indigo),
                            Triple(Icons.Rounded.CarRepair, "Car Repair", AppColors.Amber),
                            Triple(Icons.Rounded.Visibility, "Eye Clinic", AppColors.Teal),
                            Triple(Icons.Rounded.Business, "Legal Office", AppColors.Ink)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestions.forEach { (icon, label, color) ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = AppColors.Paper,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200),
                                modifier = Modifier.clickable { searchQuery = label }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = label,
                                        fontFamily = Tajawal,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.Ink
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. Hero Active Virtual Queue Ticket Card
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .bounceClick(scaleDown = 0.975f) { onNavigateToQueueTab() },
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 10.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AppColors.Ink, Color(0xFF0F5A67))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Top Row: Live Sync Status Pill + Ticket Number
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left pill with pulsing amber dot
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AppColors.Paper.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(AppColors.Amber)
                                            .pulseEffect(minScale = 0.82f, maxScale = 1.35f)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isArabic) "طابور نشط ومباشر" else "Live Queue Active",
                                        fontFamily = Tajawal,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.Paper
                                    )
                                }
                            }

                            // Right: Ticket Number
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isArabic) "رقم التذكرة" else "Ticket No.",
                                    fontFamily = Tajawal,
                                    fontSize = 11.sp,
                                    color = AppColors.Gray300
                                )
                                Text(
                                    text = "A-12",
                                    fontFamily = Inter,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AppColors.Paper,
                                    letterSpacing = 1.2.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Venue & Branch
                        Text(
                            text = if (isArabic) "عيادة د. سارة للأسنان" else "Dr. Sara Dental Clinic",
                            fontFamily = Tajawal,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Paper
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isArabic) "فرع وسط البلد" else "Downtown Branch",
                            fontFamily = Tajawal,
                            fontSize = 12.5.sp,
                            color = AppColors.Gray300
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Queue Dynamics Counter Row (Two equal stat boxes)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Box 1: People ahead & Estimated wait
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.Black.copy(alpha = 0.2f))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    Text(
                                        text = if (isArabic) "2 أفراد قبلك" else "2 people ahead",
                                        fontFamily = Tajawal,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AppColors.Paper
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isArabic) "~10 دقيقة انتظار" else "~10 min wait",
                                        fontFamily = Tajawal,
                                        fontSize = 11.sp,
                                        color = AppColors.Gray300
                                    )
                                }
                            }

                            // Box 2: Smart Travel Alert Pill ("تحرك الآن!")
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AppColors.Teal.copy(alpha = 0.35f))
                                    .border(1.dp, AppColors.Teal.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.DirectionsCar,
                                            contentDescription = null,
                                            tint = AppColors.Amber,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isArabic) "تحرك الآن!" else "Head out now!",
                                            fontFamily = Tajawal,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = AppColors.Amber
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isArabic) "المسافة 7 د بالسيارة" else "7 min drive",
                                        fontFamily = Tajawal,
                                        fontSize = 11.sp,
                                        color = AppColors.Paper
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Visual Progress Bar (Amber bar)
                        LinearProgressIndicator(
                            progress = { 0.75f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = AppColors.Amber,
                            trackColor = AppColors.Paper.copy(alpha = 0.18f)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 4. Quick Action Highlights Bar
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Action 1: QR Code
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .bounceClick(scaleDown = 0.95f) { onOpenQrScanner() },
                    shape = RoundedCornerShape(16.dp),
                    color = AppColors.Paper,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200.copy(alpha = 0.8f)),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.Teal.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.QrCodeScanner,
                                contentDescription = null,
                                tint = AppColors.Teal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isArabic) "مسح كود الفرع" else "Scan QR Code",
                                fontFamily = Tajawal,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Ink,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isArabic) "للدخول الفوري" else "Walk-in join",
                                fontFamily = Tajawal,
                                fontSize = 10.5.sp,
                                color = AppColors.Gray500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Action 2: Saved Time Stat
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .bounceClick(scaleDown = 0.95f) { onNavigateToAllServices(null) },
                    shape = RoundedCornerShape(16.dp),
                    color = AppColors.Paper,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200.copy(alpha = 0.8f)),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.Indigo.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.HourglassBottom,
                                contentDescription = null,
                                tint = AppColors.Indigo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isArabic) "وفرت 4.5 س" else "Saved 4.5 hrs",
                                fontFamily = Tajawal,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Ink,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isArabic) "من وقت الانتظار" else "Total time saved",
                                fontFamily = Tajawal,
                                fontSize = 10.5.sp,
                                color = AppColors.Gray500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 5. Category Discovery Header & Capsule Slider
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(22.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "الخدمات والأنشطة" else "Browse Categories",
                    fontFamily = Tajawal,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Ink
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigateToAllServices(null) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.homeSeeAll,
                        fontFamily = Tajawal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Teal
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                        contentDescription = null,
                        tint = AppColors.Teal,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Capsule Chips Slider
            val categories = listOf(
                Triple(ServiceCategory.CLINICS, if (isArabic) "عيادات وأطباء" else "Clinics & Doctors", AppColors.Teal),
                Triple(ServiceCategory.SALONS, if (isArabic) "صالونات وتجميل" else "Salons & Barber", AppColors.Indigo),
                Triple(ServiceCategory.GARAGES, if (isArabic) "صيانة سيارات" else "Auto Repair", AppColors.Amber),
                Triple(ServiceCategory.OFFICES, if (isArabic) "مصالح ومكاتب" else "Offices & Legal", AppColors.Ink)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { (cat, title, color) ->
                    Surface(
                        shape = RoundedCornerShape(30.dp),
                        color = AppColors.Paper,
                        border = androidx.compose.foundation.BorderStroke(1.1.dp, AppColors.Gray200),
                        shadowElevation = 2.dp,
                        modifier = Modifier.clickable { onNavigateToAllServices(cat) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = title,
                                fontFamily = Tajawal,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Ink
                            )
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
                Spacer(modifier = Modifier.height(24.dp))
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

                Spacer(modifier = Modifier.height(12.dp))

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
                                .width(250.dp)
                                .bounceClick(scaleDown = 0.965f) { onNavigateToDetails(place) },
                            shape = RoundedCornerShape(20.dp),
                            color = AppColors.Paper,
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, AppColors.Success.copy(alpha = 0.22f)),
                            shadowElevation = 3.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Top row: icon + name + star & distance
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(13.dp))
                                            .background(place.color.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = place.icon,
                                            contentDescription = null,
                                            tint = place.color,
                                            modifier = Modifier.size(22.dp)
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
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.Star,
                                                contentDescription = null,
                                                tint = AppColors.Amber,
                                                modifier = Modifier.size(13.dp)
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

                                Spacer(modifier = Modifier.height(14.dp))

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

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .bounceClick(scaleDown = 0.975f) { onNavigateToDetails(place) },
                    shape = RoundedCornerShape(20.dp),
                    color = AppColors.Paper,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200.copy(alpha = 0.8f)),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Place Icon
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(place.color.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = place.icon,
                                contentDescription = null,
                                tint = place.color,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Place Details
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = place.localizedName(isArabic),
                                fontFamily = Tajawal,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Ink,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                mainBranch.distanceKm?.let { dist ->
                                    Icon(
                                        imageVector = Icons.Rounded.LocationOn,
                                        contentDescription = null,
                                        tint = AppColors.Gray500,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = strings.distanceAway(dist),
                                        fontFamily = Tajawal,
                                        fontSize = 11.5.sp,
                                        color = AppColors.Gray600
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("•", color = AppColors.Gray400, fontSize = 10.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                Icon(
                                    imageVector = Icons.Rounded.Groups,
                                    contentDescription = null,
                                    tint = AppColors.Gray500,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = strings.peopleWaiting(place.totalWaiting),
                                    fontFamily = Tajawal,
                                    fontSize = 11.5.sp,
                                    color = AppColors.Gray600
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Teal "احجز الآن" Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.Teal)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = strings.joinNow,
                                fontFamily = Tajawal,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Paper
                            )
                        }
                    }
                }
            }
        }
    }
}
}
