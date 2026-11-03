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
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick
import com.taboor.features.home.data.repository.ServiceRepository
import com.taboor.features.home.domain.model.ServiceCategory
import com.taboor.features.home.domain.model.ServicePlace
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ServiceSortFilter {
    ALL,
    NEAREST,
    FASTEST_QUEUE,
    TOP_RATED,
    OPEN_NOW
}

/**
 * Rich dedicated screen listing businesses by category with search, sorting, and live queue statistics.
 * Pixel-perfect reproduction of Flutter's `all_services_screen.dart`.
 */
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
                list = list.sortedBy { it.branches.firstOrNull()?.distanceKm?.toDoubleOrNull() ?: 99.0 }
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
        null -> strings.allServicesTitle
        ServiceCategory.CLINICS -> if (isArabic) "العيادات والمراكز الطبية" else "Clinics & Medical Centers"
        ServiceCategory.SALONS -> if (isArabic) "صالونات الحلاقة والتجميل" else "Salons & Spas"
        ServiceCategory.GARAGES -> if (isArabic) "مراكز صيانة السيارات" else "Auto Repair & Garages"
        ServiceCategory.OFFICES -> if (isArabic) "المصالح الحكومية والمكاتب" else "Offices & Administrative"
    }

    val categorySubtitle = when (selectedCategory) {
        null -> strings.allServicesSubtitle
        ServiceCategory.CLINICS -> if (isArabic) "احجز دورك مسبقاً وتجنب زحمة الانتظار" else "Book remotely & avoid clinic waiting rooms"
        ServiceCategory.SALONS -> if (isArabic) "احجز حلاقتك أو موعدك بدون انتظار" else "Reserve your spot without physical queues"
        ServiceCategory.GARAGES -> if (isArabic) "صيانة سيارتك في موعدها الدقيق" else "Fast car service without delays"
        ServiceCategory.OFFICES -> if (isArabic) "أنجز معاملاتك بسرعة وسهولة" else "Government & administrative queue booking"
    }

    val themeColor = when (selectedCategory) {
        null -> AppColors.Teal
        ServiceCategory.CLINICS -> AppColors.Teal
        ServiceCategory.SALONS -> AppColors.Indigo
        ServiceCategory.GARAGES -> AppColors.Amber
        ServiceCategory.OFFICES -> AppColors.Ink
    }

    val themeIcon = when (selectedCategory) {
        null -> Icons.Rounded.GridView
        ServiceCategory.CLINICS -> Icons.Rounded.MedicalServices
        ServiceCategory.SALONS -> Icons.Rounded.ContentCut
        ServiceCategory.GARAGES -> Icons.Rounded.CarRepair
        ServiceCategory.OFFICES -> Icons.Rounded.Business
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = categoryTitle,
                            fontFamily = Tajawal,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Ink
                        )
                        Text(
                            text = categorySubtitle,
                            fontFamily = Tajawal,
                            fontSize = 11.sp,
                            color = AppColors.Gray500
                        )
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AppColors.Gray100)
                            .bounceClick(scaleDown = 0.92f) { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                            contentDescription = strings.profileBack,
                            tint = AppColors.Ink,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Paper)
            )
        },
        containerColor = AppColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ==========================================
            // 1. Top Section Header / Category Banner & Search
            // ==========================================
            Surface(
                color = AppColors.Paper,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    // Category Highlight Banner
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = themeColor.copy(alpha = 0.07f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, themeColor.copy(alpha = 0.18f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(themeColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = themeIcon,
                                    contentDescription = null,
                                    tint = themeColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "تتبع مباشر وحجز فوري من البيت" else "Live queue tracking & remote booking",
                                    fontFamily = Tajawal,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Ink
                                )
                                Text(
                                    text = if (isArabic) "وفّر وقتك وسجل مكانك بالطابور بضغطة واحدة" else "Save your spot and arrive right when it's your turn",
                                    fontFamily = Tajawal,
                                    fontSize = 11.sp,
                                    color = AppColors.Gray600
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search Bar within this screen
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppColors.Background,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = AppColors.Gray400,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = if (isArabic) "ابحث بالاسم أو الفرع أو المنطقة..." else "Search by place name or branch...",
                                        fontFamily = Tajawal,
                                        fontSize = 13.sp,
                                        color = AppColors.Gray400
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
                                    color = AppColors.Ink
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
                                        tint = AppColors.Gray400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 2. Category Selector Pill Tabs
            // ==========================================
            Surface(
                color = AppColors.Paper,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf(
                        Triple(null, strings.filterAll, Icons.Rounded.GridView),
                        Triple(ServiceCategory.CLINICS, strings.categoryClinics, Icons.Rounded.MedicalServices),
                        Triple(ServiceCategory.SALONS, strings.categorySalons, Icons.Rounded.ContentCut),
                        Triple(ServiceCategory.GARAGES, strings.categoryGarages, Icons.Rounded.CarRepair),
                        Triple(ServiceCategory.OFFICES, strings.categoryOffices, Icons.Rounded.Business)
                    )

                    tabs.forEach { (cat, title, icon) ->
                        val isSelected = selectedCategory == cat
                        val activeColor = when (cat) {
                            null -> AppColors.Ink
                            ServiceCategory.CLINICS -> AppColors.Teal
                            ServiceCategory.SALONS -> AppColors.Indigo
                            ServiceCategory.GARAGES -> AppColors.Amber
                            ServiceCategory.OFFICES -> AppColors.Ink
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) activeColor else AppColors.Background,
                            border = androidx.compose.foundation.BorderStroke(
                                1.2.dp,
                                if (isSelected) activeColor else AppColors.Gray200
                            ),
                            shadowElevation = if (isSelected) 3.dp else 0.dp,
                            modifier = Modifier.bounceClick(scaleDown = 0.94f) { selectedCategory = cat }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) AppColors.Paper else AppColors.Gray600,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = title,
                                    fontFamily = Tajawal,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) AppColors.Paper else AppColors.Ink
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 3. Quick Sort & Filter Bar
            // ==========================================
            Surface(
                color = AppColors.Background,
                border = androidx.compose.foundation.BorderStroke(0.6.dp, AppColors.Gray200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            shape = RoundedCornerShape(12.dp),
                            color = if (isActive) AppColors.Ink else AppColors.Paper,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isActive) AppColors.Ink else AppColors.Gray300
                            ),
                            modifier = Modifier.bounceClick(scaleDown = 0.94f) { sortFilter = filter }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isActive) AppColors.Paper else AppColors.Gray600,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    fontFamily = Tajawal,
                                    fontSize = 11.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isActive) AppColors.Paper else AppColors.Gray700
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 4. Main Service Cards List
            // ==========================================
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    coroutineScope.launch {
                        isRefreshing = true
                        delay(700)
                        isRefreshing = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPlaces) { place ->
                        val main = place.branches.first()
                        val estMinutes = (place.totalWaiting * 4).coerceIn(3, 90)
                        val isOpen = place.branches.any { it.isOpen }

                        val isFast = place.totalWaiting <= 3
                        val isModerate = place.totalWaiting in 4..6
                        val densityColor = when {
                            isFast -> AppColors.Success
                            isModerate -> AppColors.Amber
                            else -> Color(0xFFE11D48)
                        }
                        val densityLabel = when {
                            isFast -> if (isArabic) "طابور سريع" else "Fast Queue"
                            isModerate -> if (isArabic) "طابور معتدل" else "Moderate"
                            else -> if (isArabic) "طابور نشط" else "Busy"
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .bounceClick(scaleDown = 0.98f) { onNavigateToDetails(place) },
                            shape = RoundedCornerShape(20.dp),
                            color = AppColors.Paper,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200),
                            shadowElevation = 2.dp
                        ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(place.color.copy(alpha = 0.12f))
                                        .border(1.dp, place.color.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = place.icon,
                                        contentDescription = null,
                                        tint = place.color,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = place.localizedName(isArabic),
                                            fontFamily = Tajawal,
                                            fontSize = 15.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.Ink,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Rounded.Verified,
                                            contentDescription = null,
                                            tint = AppColors.Teal,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.Star,
                                            contentDescription = null,
                                            tint = AppColors.Amber,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "%.1f".format(place.starRating),
                                            fontFamily = Tajawal,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.Ink
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("•", color = AppColors.Gray400, fontSize = 10.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        // Open / Closed dot
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(if (isOpen) AppColors.Success else AppColors.Gray400)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isOpen) {
                                                if (isArabic) "مفتوح الآن" else "Open now"
                                            } else {
                                                if (isArabic) "مغلق" else "Closed"
                                            },
                                            fontFamily = Tajawal,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isOpen) AppColors.Success else AppColors.Gray500
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Address & Branch Count
                            Text(
                                text = "${place.branches.size} ${if (isArabic) "فروع متاحة" else "branches"} • ${main.localizedAddress(isArabic) ?: ""}",
                                fontFamily = Tajawal,
                                fontSize = 12.sp,
                                color = AppColors.Gray600,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(thickness = 1.dp, color = AppColors.Gray100)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Bottom Real-time Queue Indicator & CTA
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Density pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(densityColor.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Rounded.PeopleAlt,
                                            contentDescription = null,
                                            tint = densityColor,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$densityLabel: ${strings.peopleWaiting(place.totalWaiting)}",
                                            fontFamily = Tajawal,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = densityColor
                                        )
                                    }
                                }

                                // Wait time
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Timer,
                                        contentDescription = null,
                                        tint = AppColors.Gray500,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "~$estMinutes ${if (isArabic) "د" else "min"}",
                                        fontFamily = Tajawal,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.Gray600
                                    )
                                }

                                // Distance & Arrow CTA
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    main.distanceKm?.let { dist ->
                                        Icon(
                                            imageVector = Icons.Rounded.NearMe,
                                            contentDescription = null,
                                            tint = AppColors.Teal,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = strings.distanceAway(dist),
                                            fontFamily = Tajawal,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.Teal
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(AppColors.SoftTeal),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                            contentDescription = null,
                                            tint = AppColors.Teal,
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
    }
}
