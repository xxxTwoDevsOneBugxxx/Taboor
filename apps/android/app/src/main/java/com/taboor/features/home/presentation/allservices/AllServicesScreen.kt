package com.taboor.features.home.presentation.allservices

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.components.TaboorCard
import com.taboor.core.components.TaboorEmptyState
import com.taboor.core.components.TaboorTopAppBar
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
import com.taboor.core.location.TaboorPrecomputedRoutes
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick
import com.taboor.features.home.data.repository.ServiceRepository
import com.taboor.features.home.domain.model.ServiceCategory
import com.taboor.features.home.domain.model.ServicePlace
import com.taboor.features.home.domain.model.icon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ServiceSortFilter {
    ALL,
    NEAREST,
    FASTEST_QUEUE,
    TOP_RATED,
    OPEN_NOW
}

// Taboor Warm Cream design system tokens
private val M3Bg = AppColors.Background
private val M3Surface = AppColors.Paper
private val M3Border = AppColors.RowDivider
private val M3Primary = AppColors.Ink
private val M3PrimaryDark = AppColors.DeepTeal
private val M3PrimaryContainer = AppColors.SoftTeal
private val M3TextPrimary = AppColors.TextPrimary
private val M3TextSecondary = AppColors.TextSecondary
private val M3TextTertiary = AppColors.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllServicesScreen(
    initialCategory: ServiceCategory? = null,
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (ServicePlace) -> Unit
) {
    val strings = LocalAppStrings.current
    val isArabic = LocaleManager.isArabic
    val coroutineScope = rememberCoroutineScope()

    androidx.activity.compose.BackHandler(onBack = onNavigateBack)

    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var searchQuery by remember { mutableStateOf("") }
    var sortFilter by remember { mutableStateOf(ServiceSortFilter.ALL) }
    var isRefreshing by remember { mutableStateOf(false) }

    val allPlaces = ServiceRepository.places
    val filteredPlaces = remember(selectedCategory, searchQuery, sortFilter) {
        var list = allPlaces.filter { p ->
            val matchesCat = selectedCategory == null || p.category == selectedCategory
            val matchesQuery = p.matchesQuery(searchQuery)
            matchesCat && matchesQuery
        }

        when (sortFilter) {
            ServiceSortFilter.NEAREST -> {
                list = list.sortedBy { p ->
                    val main = p.branches.firstOrNull()
                    val dist = TaboorPrecomputedRoutes.getDistanceKm(
                        main?.id,
                        main?.lat ?: 30.0585,
                        main?.lng ?: 31.3466
                    )
                    dist.toDoubleOrNull() ?: 99.0
                }
            }
            ServiceSortFilter.FASTEST_QUEUE -> {
                list = list.sortedBy { it.totalWaiting }
            }
            ServiceSortFilter.TOP_RATED -> {
                list = list.sortedByDescending { it.starRating }
            }
            ServiceSortFilter.OPEN_NOW -> {
                list = list.filter { p -> p.branches.any { it.isOpen } }
            }
            ServiceSortFilter.ALL -> {}
        }
        list
    }

    val categoryTitle = when (selectedCategory) {
        null -> if (isArabic) "جميع الخدمات والأماكن" else "All Services & Places"
        ServiceCategory.CLINICS -> if (isArabic) "العيادات والمراكز الطبية" else "Clinics & Medical Centers"
        ServiceCategory.SALONS -> if (isArabic) "صالونات الحلاقة والتجميل" else "Salons & Spas"
        ServiceCategory.GARAGES -> if (isArabic) "مراكز صيانة السيارات" else "Auto Repair & Garages"
        ServiceCategory.OFFICES -> if (isArabic) "المصالح الحكومية والمكاتب" else "Offices & Administrative"
    }

    Scaffold(
        topBar = {
            TaboorTopAppBar(
                title = categoryTitle,
                subtitle = if (isArabic) "${filteredPlaces.size} أماكن متاحة للحجز المباشر" else "${filteredPlaces.size} places available",
                onNavigateBack = onNavigateBack,
                containerColor = AppColors.Paper,
                elevation = 1.dp
            )
        },
        containerColor = M3Bg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ==========================================
            // 1. Pinned Search & Filter Controls
            // ==========================================
            Surface(
                color = M3Surface,
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, M3Border)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Modern Search Bar
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppColors.Background,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.RowDivider),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = M3Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = if (isArabic) "ابحث عن عيادة، صالون، مركز صيانة..." else "Search place, clinic or branch...",
                                        fontFamily = Tajawal,
                                        fontSize = 13.sp,
                                        color = M3TextTertiary
                                    )
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = Tajawal,
                                    fontSize = 13.5.sp,
                                    color = M3TextPrimary
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = null,
                                        tint = M3TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Category Pill Filters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = listOf(
                            Triple(null, if (isArabic) "الكل" else "All", Icons.Rounded.GridView),
                            Triple(ServiceCategory.CLINICS, if (isArabic) "عيادات ومستشفيات" else "Clinics", Icons.Rounded.MedicalServices),
                            Triple(ServiceCategory.SALONS, if (isArabic) "صالونات وتجميل" else "Salons", Icons.Rounded.ContentCut),
                            Triple(ServiceCategory.GARAGES, if (isArabic) "ورش وصيانة" else "Auto Repair", Icons.Rounded.CarRepair),
                            Triple(ServiceCategory.OFFICES, if (isArabic) "مكاتب وخدمات" else "Offices", Icons.Rounded.Business)
                        )

                        categories.forEach { (cat, title, icon) ->
                            val isSelected = selectedCategory == cat
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) M3PrimaryContainer else Color(0xFFF8FAFC),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) M3Primary else Color(0xFFCBD5E1)
                                ),
                                modifier = Modifier.bounceClick(scaleDown = 0.95f) { selectedCategory = cat }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) M3PrimaryDark else M3TextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = title,
                                        fontFamily = Tajawal,
                                        fontSize = 12.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) M3PrimaryDark else M3TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Quick Sort & Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val filterChips = listOf(
                            Triple(ServiceSortFilter.ALL, if (isArabic) "الكل (${filteredPlaces.size})" else "All (${filteredPlaces.size})", Icons.Rounded.List),
                            Triple(ServiceSortFilter.NEAREST, if (isArabic) "الأقرب إليك" else "Nearest", Icons.Rounded.NearMe),
                            Triple(ServiceSortFilter.FASTEST_QUEUE, if (isArabic) "طابور سريع" else "Fastest Queue", Icons.Rounded.Bolt),
                            Triple(ServiceSortFilter.TOP_RATED, if (isArabic) "الأعلى تقييماً" else "Top Rated", Icons.Rounded.Star),
                            Triple(ServiceSortFilter.OPEN_NOW, if (isArabic) "مفتوح الآن" else "Open Now", Icons.Rounded.AccessTimeFilled)
                        )

                        filterChips.forEach { (filter, label, icon) ->
                            val isActive = sortFilter == filter
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isActive) M3PrimaryDark else Color(0xFFF1F5F9),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isActive) M3PrimaryDark else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.bounceClick(scaleDown = 0.95f) { sortFilter = filter }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isActive) Color.White else M3TextSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = label,
                                        fontFamily = Tajawal,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isActive) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 2. Main Service Cards List
            // ==========================================
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    coroutineScope.launch {
                        isRefreshing = true
                        delay(600)
                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                if (filteredPlaces.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TaboorCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = 24.dp
                        ) {
                            TaboorEmptyState(
                                icon = Icons.Rounded.SearchOff,
                                title = if (isArabic) "لا توجد أماكن مطابقة لبحثك" else "No matching places found",
                                description = if (isArabic) "جرب البحث بكلمات أخرى أو اختر تصنيفاً آخر" else "Try different search terms or choose another category",
                                actionText = if (isArabic) "إعادة ضبط البحث" else "Reset Filters",
                                onActionClick = {
                                    searchQuery = ""
                                    selectedCategory = null
                                    sortFilter = ServiceSortFilter.ALL
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredPlaces, key = { it.id }) { place ->
                            val main = place.branches.first()
                            val isOpen = place.branches.any { it.isOpen }

                            // Calculate actual road distance and drive time
                            val branchDist = TaboorPrecomputedRoutes.getDistanceKm(
                                main.id,
                                main.lat ?: 30.0585,
                                main.lng ?: 31.3466
                            )
                            val estDriveMin = TaboorPrecomputedRoutes.getEstimatedMinutes(
                                main.id,
                                main.lat ?: 30.0585,
                                main.lng ?: 31.3466
                            )

                            // Queue density calculation
                            val isFast = place.totalWaiting <= 3
                            val isModerate = place.totalWaiting in 4..6
                            val queuePillBg = when {
                                !isOpen -> Color(0xFFF3F4F6)
                                isFast -> Color(0xFFD1FAE5)
                                isModerate -> Color(0xFFFEF3C7)
                                else -> Color(0xFFFFE4E6)
                            }
                            val queuePillColor = when {
                                !isOpen -> Color(0xFF6B7280)
                                isFast -> Color(0xFF065F46)
                                isModerate -> Color(0xFF92400E)
                                else -> Color(0xFF9F1239)
                            }
                            val queuePillIcon = when {
                                !isOpen -> Icons.Rounded.Lock
                                isFast -> Icons.Rounded.Bolt
                                isModerate -> Icons.Rounded.Timer
                                else -> Icons.Rounded.People
                            }
                            val queuePillText = when {
                                !isOpen -> if (isArabic) "مغلق حالياً" else "Closed"
                                isFast -> if (isArabic) "طابور سريع: ${place.totalWaiting} ينتظرون" else "Fast: ${place.totalWaiting} waiting"
                                isModerate -> if (isArabic) "طابور معتدل: ${place.totalWaiting} ينتظرون" else "Moderate: ${place.totalWaiting} waiting"
                                else -> if (isArabic) "طابور نشط: ${place.totalWaiting} ينتظرون" else "Busy: ${place.totalWaiting} waiting"
                            }

                            // Category themed icon styling
                            val (catIconBg, catIconColor) = when (place.category) {
                                ServiceCategory.CLINICS -> Pair(Color(0xFFE6F7F5), Color(0xFF0D9488))
                                ServiceCategory.SALONS -> Pair(Color(0xFFF3E8FF), Color(0xFF7C3AED))
                                ServiceCategory.GARAGES -> Pair(Color(0xFFFEF3C7), Color(0xFFD97706))
                                ServiceCategory.OFFICES -> Pair(Color(0xFFEFF6FF), Color(0xFF2563EB))
                            }

                            // Clean Material 3 Card
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .bounceClick(scaleDown = 0.985f) { onNavigateToDetails(place) },
                                shape = RoundedCornerShape(18.dp),
                                color = M3Surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, M3Border),
                                shadowElevation = 1.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Row 1: Icon + Title/Rating + Open/Closed Badge
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Themed Icon Box
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(catIconBg)
                                                .border(1.dp, catIconColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = place.icon,
                                                contentDescription = null,
                                                tint = catIconColor,
                                                modifier = Modifier.size(26.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Title and Subtitles
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = place.localizedName(isArabic),
                                                    fontFamily = Tajawal,
                                                    fontSize = 15.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = M3TextPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f, fill = false)
                                                )
                                                Icon(
                                                    imageVector = Icons.Rounded.Verified,
                                                    contentDescription = null,
                                                    tint = M3Primary,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(3.dp))

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                // Rating
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Star,
                                                        contentDescription = null,
                                                        tint = Color(0xFFF59E0B),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(
                                                        text = "%.1f".format(place.starRating),
                                                        fontFamily = Tajawal,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = M3TextPrimary
                                                    )
                                                }

                                                Text("•", color = M3TextTertiary, fontSize = 10.sp)

                                                // Category label
                                                Text(
                                                    text = place.category.localizedTitle(isArabic),
                                                    fontFamily = Tajawal,
                                                    fontSize = 11.5.sp,
                                                    color = M3TextSecondary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Open / Closed Badge
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isOpen) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isOpen) Color(0xFF059669) else Color(0xFFDC2626))
                                                )
                                                Text(
                                                    text = if (isOpen) {
                                                        if (isArabic) "مفتوح" else "Open"
                                                    } else {
                                                        if (isArabic) "مغلق" else "Closed"
                                                    },
                                                    fontFamily = Tajawal,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isOpen) Color(0xFF065F46) else Color(0xFF991B1B)
                                                )
                                            }
                                        }
                                    }

                                    // Row 2: Location & Branches count
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.LocationOn,
                                            contentDescription = null,
                                            tint = M3TextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "${place.branches.size} ${if (isArabic) "فروع متاحة" else "branches"} • ${main.localizedAddress(isArabic) ?: ""}",
                                            fontFamily = Tajawal,
                                            fontSize = 12.sp,
                                            color = M3TextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    HorizontalDivider(thickness = 0.8.dp, color = Color(0xFFF1F5F9))

                                    // Row 3: Bottom Live Queue Badge & Action CTA Button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Live Queue Pill
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = queuePillBg
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = queuePillIcon,
                                                    contentDescription = null,
                                                    tint = queuePillColor,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = queuePillText,
                                                    fontFamily = Tajawal,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = queuePillColor
                                                )
                                            }
                                        }

                                        // Distance & Drive Time
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.DirectionsCar,
                                                contentDescription = null,
                                                tint = M3PrimaryDark,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "$branchDist ${if (isArabic) "كم" else "km"} (~$estDriveMin د)",
                                                fontFamily = Tajawal,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = M3PrimaryDark
                                            )
                                        }

                                        // Action Button
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isOpen) M3Primary else Color(0xFFF1F5F9),
                                            modifier = Modifier.bounceClick(scaleDown = 0.94f) {
                                                onNavigateToDetails(place)
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = if (isOpen) {
                                                        if (isArabic) "احجز الآن" else "Join"
                                                    } else {
                                                        if (isArabic) "تفاصيل" else "Details"
                                                    },
                                                    fontFamily = Tajawal,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isOpen) Color.White else M3TextPrimary
                                                )
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                                    contentDescription = null,
                                                    tint = if (isOpen) Color.White else M3TextSecondary,
                                                    modifier = Modifier.size(12.dp)
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
    }
}
