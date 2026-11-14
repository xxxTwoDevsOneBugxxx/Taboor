package com.taboor.features.home.presentation.allservices

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.taboor.core.components.TaboorCard
import com.taboor.core.components.TaboorEmptyState
import com.taboor.core.components.TaboorTopAppBar
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
import com.taboor.core.services.LocationService
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick
import com.taboor.features.home.data.repository.ServiceRepository
import com.taboor.features.home.domain.model.ServiceCategory
import com.taboor.features.home.domain.model.ServicePlace
import com.taboor.features.home.domain.model.icon
import com.taboor.features.home.domain.service.ServiceCoordinates
import com.taboor.features.home.domain.service.ServiceDiscovery
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
    val context = LocalContext.current

    androidx.activity.compose.BackHandler(onBack = onNavigateBack)

    var selectedCategory by rememberSaveable { mutableStateOf(initialCategory) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var sortFilter by rememberSaveable { mutableStateOf(ServiceSortFilter.ALL) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isMapMode by rememberSaveable { mutableStateOf(false) }
    var selectedMapPlaceId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedMapBranchId by rememberSaveable { mutableStateOf<String?>(null) }
    var isMapCardDismissed by rememberSaveable { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var isNationwideMode by rememberSaveable { mutableStateOf(false) }

    val allPlaces by ServiceRepository.placesFlow.collectAsState()
    val selectedLocation by ServiceRepository.selectedLocation.collectAsState()
    val userCoordinate = remember(selectedLocation) {
        LocationService.getEffectivePosition(context).let { (lat, lng) -> ServiceCoordinates(lat, lng) }
    }
    val matchingPlaces = remember(selectedCategory, searchQuery, isNationwideMode, allPlaces, userCoordinate) {
        ServiceDiscovery.filterPlaces(allPlaces, searchQuery, selectedCategory, userCoordinate, isNationwideMode)
    }
    val filteredPlaces = remember(matchingPlaces, sortFilter, userCoordinate) {
        var list = matchingPlaces

        when (sortFilter) {
            ServiceSortFilter.NEAREST -> {
                list = list.sortedBy { p -> ServiceDiscovery.nearestBranch(p, userCoordinate)?.let { ServiceDiscovery.distanceToBranch(it, userCoordinate) } ?: Double.MAX_VALUE }
            }
            ServiceSortFilter.FASTEST_QUEUE -> {
                list = list.sortedBy { place -> place.totalWaiting }
            }
            ServiceSortFilter.TOP_RATED -> {
                list = list.sortedByDescending { it.starRating }
            }
            ServiceSortFilter.OPEN_NOW -> {
                list = list.filter { p -> ServiceDiscovery.nearestBranch(p, userCoordinate)?.isOpen == true }
            }
            ServiceSortFilter.ALL -> {}
        }
        list
    }

    LaunchedEffect(isMapMode, sortFilter, selectedCategory, searchQuery, filteredPlaces, userCoordinate) {
        if (!isMapMode || isMapCardDismissed) return@LaunchedEffect
        val recommendedPlace = filteredPlaces.firstOrNull() ?: return@LaunchedEffect
        val recommendedBranch = ServiceDiscovery.nearestBranch(recommendedPlace, userCoordinate) ?: return@LaunchedEffect
        selectedMapPlaceId = recommendedPlace.id
        selectedMapBranchId = recommendedBranch.id
    }

    val categoryTitle = when (selectedCategory) {
        null -> if (isArabic) "جميع الخدمات" else "All Services"
        ServiceCategory.CLINICS -> if (isArabic) "العيادات والمراكز الطبية" else "Clinics & Medical Centers"
        ServiceCategory.SALONS -> if (isArabic) "صالونات الحلاقة والتجميل" else "Salons & Spas"
        ServiceCategory.GARAGES -> if (isArabic) "مراكز صيانة السيارات" else "Auto Repair & Garages"
        ServiceCategory.OFFICES -> if (isArabic) "المصالح الحكومية والمكاتب" else "Offices & Administrative"
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TaboorTopAppBar(
                title = categoryTitle,
                subtitle = if (isArabic) "${filteredPlaces.size} مكان • ${if (isNationwideMode) "كل مصر" else "بالقرب منك"}" else "${filteredPlaces.size} places • ${if (isNationwideMode) "Nationwide" else "Near you"}",
                navigationIcon = {
                    HeaderActionButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = strings.back,
                        selected = false,
                        startPadding = 0.dp,
                        onClick = onNavigateBack
                    )
                },
                actions = {
                    Box {
                        HeaderActionButton(
                            icon = Icons.Rounded.Tune,
                            contentDescription = if (isArabic) "ترتيب وتصفية" else "Sort and filter",
                            selected = sortFilter != ServiceSortFilter.ALL,
                            onClick = { showSortMenu = true }
                        )
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            DropdownMenuItem(
                                text = { Text(if (isArabic) "ترتيب حسب" else "Sort by", fontWeight = FontWeight.Bold) },
                                onClick = {},
                                enabled = false
                            )
                            DropdownMenuItem(
                                text = { Text(if (isNationwideMode) { if (isArabic) "كل مصر" else "Nationwide" } else { if (isArabic) "في نطاقي" else "Near me" }, fontFamily = Tajawal) },
                                leadingIcon = { Icon(if (isNationwideMode) Icons.Rounded.Public else Icons.Rounded.MyLocation, null) },
                                trailingIcon = { if (isNationwideMode) Icon(Icons.Rounded.Check, null, tint = AppColors.Teal) },
                                onClick = { isNationwideMode = !isNationwideMode; showSortMenu = false }
                            )
                            HorizontalDivider()
                            ServiceSortFilter.values().forEach { filter ->
                                val label = when (filter) {
                                    ServiceSortFilter.ALL -> if (isArabic) "الترتيب الافتراضي" else "Default order"
                                    ServiceSortFilter.NEAREST -> if (isArabic) "الأقرب إليك" else "Nearest"
                                    ServiceSortFilter.FASTEST_QUEUE -> if (isArabic) "أسرع طابور" else "Shortest wait"
                                    ServiceSortFilter.TOP_RATED -> if (isArabic) "الأعلى تقييماً" else "Top rated"
                                    ServiceSortFilter.OPEN_NOW -> if (isArabic) "مفتوح الآن" else "Open now"
                                }
                                DropdownMenuItem(
                                    text = { Text(label, fontFamily = Tajawal) },
                                    leadingIcon = {
                                        if (sortFilter == filter) Icon(Icons.Rounded.Check, null, tint = AppColors.Teal)
                                    },
                                    onClick = {
                                        sortFilter = filter
                                        showSortMenu = false
                                    }
                                )
                            }
                            if (selectedCategory != null || searchQuery.isNotBlank() || sortFilter != ServiceSortFilter.ALL) {
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text(if (isArabic) "إعادة ضبط الفلاتر" else "Reset filters", color = AppColors.Error) },
                                    leadingIcon = { Icon(Icons.Rounded.Refresh, null, tint = AppColors.Error) },
                                    onClick = {
                                        selectedCategory = null
                                        searchQuery = ""
                                        sortFilter = ServiceSortFilter.ALL
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                    HeaderActionButton(
                        icon = if (isMapMode) Icons.AutoMirrored.Rounded.FormatListBulleted else Icons.Rounded.Map,
                        contentDescription = if (isArabic) {
                            if (isMapMode) "عرض القائمة" else "عرض الخريطة"
                        } else {
                            if (isMapMode) "Show list" else "Show map"
                        },
                        selected = isMapMode,
                        onClick = {
                            isMapMode = !isMapMode
                            if (isMapMode && selectedMapPlaceId == null) {
                                val firstPlace = filteredPlaces.firstOrNull()
                                selectedMapPlaceId = firstPlace?.id
                                selectedMapBranchId = firstPlace?.let { ServiceDiscovery.nearestBranch(it, userCoordinate)?.id }
                            }
                        }
                    )
                },
                containerColor = M3Bg,
                elevation = 0.dp
            )
        },
        containerColor = M3Bg
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    isRefreshing = true
                    delay(600)
                    isRefreshing = false
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            if (isMapMode) {
                val selectedMapPlace = if (isMapCardDismissed) null else {
                    filteredPlaces.firstOrNull { it.id == selectedMapPlaceId } ?: filteredPlaces.firstOrNull()
                }
                val selectedMapBranch = selectedMapPlace?.branches?.firstOrNull { it.id == selectedMapBranchId }
                    ?: selectedMapPlace?.branches?.firstOrNull()
                Box(Modifier.fillMaxSize()) {
                    AllServicesMapView(
                        places = filteredPlaces,
                        userCoordinate = userCoordinate,
                        nationwideMode = isNationwideMode,
                        selectedPlaceId = selectedMapPlace?.id,
                        selectedBranchId = selectedMapBranch?.id,
                        onBranchSelected = { place, branch ->
                            selectedMapPlaceId = place.id
                            selectedMapBranchId = branch.id
                            isMapCardDismissed = false
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = AppColors.Paper.copy(alpha = 0.97f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.RowDivider),
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Rounded.Search, null, tint = M3TextSecondary, modifier = Modifier.size(18.dp))
                                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            if (isArabic) "ابحث على الخريطة..." else "Search on map...",
                                            fontFamily = Tajawal,
                                            fontSize = 14.sp,
                                            color = M3TextTertiary
                                        )
                                    }
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        singleLine = true,
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            fontFamily = Tajawal,
                                            fontSize = 14.sp,
                                            color = M3TextPrimary
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Rounded.Close, if (isArabic) "مسح البحث" else "Clear search", tint = M3TextTertiary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val mapCategories = listOf(
                                Triple(null, if (isArabic) "الكل" else "All", Icons.Rounded.GridView),
                                Triple(ServiceCategory.CLINICS, if (isArabic) "عيادات" else "Clinics", Icons.Rounded.MedicalServices),
                                Triple(ServiceCategory.SALONS, if (isArabic) "صالونات" else "Salons", Icons.Rounded.ContentCut),
                                Triple(ServiceCategory.GARAGES, if (isArabic) "سيارات" else "Auto", Icons.Rounded.CarRepair),
                                Triple(ServiceCategory.OFFICES, if (isArabic) "مكاتب" else "Offices", Icons.Rounded.Business)
                            )
                            mapCategories.forEach { (category, label, icon) ->
                                val isSelected = selectedCategory == category
                                Surface(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { selectedCategory = category },
                                    shape = CircleShape,
                                    color = if (isSelected) AppColors.Teal else AppColors.Paper.copy(alpha = 0.97f),
                                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, AppColors.RowDivider),
                                        shadowElevation = if (isSelected) 2.dp else 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(icon, null, tint = if (isSelected) Color.White else M3TextSecondary, modifier = Modifier.size(12.dp))
                                        Text(label, fontFamily = Tajawal, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Color.White else M3TextPrimary)
                                    }
                                }
                            }
                        }

                    }

                    if (filteredPlaces.isEmpty()) {
                        Surface(
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = AppColors.Paper,
                            shadowElevation = 4.dp
                        ) {
                            Text(
                                text = if (isArabic) "لا توجد أماكن مطابقة للفلاتر" else "No places match these filters",
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                                color = AppColors.TextPrimary,
                                fontFamily = Tajawal
                            )
                        }
                    }

                    if (selectedMapPlace != null && selectedMapBranch != null) {
                        val place = selectedMapPlace
                        val branch = selectedMapBranch
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 24.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = AppColors.Paper),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(
                                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(place.color.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(place.icon, null, tint = place.color, modifier = Modifier.size(23.dp))
                                    }
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                        Text(place.localizedName(isArabic), style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                            Text(branch.localizedLabel(isArabic), style = MaterialTheme.typography.bodySmall, color = AppColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("·", color = M3TextTertiary)
                                            Text(branch.distanceKm ?: "${place.starRating} ★", style = MaterialTheme.typography.labelMedium, color = AppColors.Teal)
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            isMapCardDismissed = true
                                            selectedMapPlaceId = null
                                            selectedMapBranchId = null
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Rounded.Close, if (isArabic) "إغلاق" else "Close", tint = M3TextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = AppColors.Success.copy(alpha = 0.10f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(Icons.Rounded.People, null, tint = AppColors.Success, modifier = Modifier.size(14.dp))
                                            Text(
                                                if (isArabic) "${branch.waiting} في الانتظار" else "${branch.waiting} waiting",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = AppColors.Success
                                            )
                                            Text("·", color = M3TextTertiary)
                                            Text(
                                                if (isArabic) "حوالي ${branch.estWaitMinutes} د" else "~${branch.estWaitMinutes}m",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = M3TextSecondary
                                            )
                                        }
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Button(
                                        onClick = {
                                            onNavigateToDetails(place.copy(branches = listOf(branch) + place.branches.filterNot { it.id == branch.id }))
                                        },
                                        shape = CircleShape,
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 9.dp)
                                    ) {
                                        Text(if (isArabic) "حجز دور" else "Book", fontFamily = Tajawal, color = Color.White)
                                        Spacer(Modifier.width(5.dp))
                                        Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
            LazyColumn(
                state = rememberLazyListState(),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ==========================================
                // 1. Search & Filter Controls (Scrollable)
                // ==========================================
                item(key = "search_and_filters") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = {
                            Icon(Icons.Rounded.Search, contentDescription = null, tint = M3Primary)
                        },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Close,
                                        contentDescription = if (isArabic) "مسح البحث" else "Clear search",
                                        tint = M3TextSecondary
                                    )
                                }
                            }
                        } else null,
                        placeholder = {
                            Text(
                                text = if (isArabic) "ابحث عن خدمة أو مكان" else "Search services or places",
                                fontFamily = Tajawal,
                                fontSize = 14.sp,
                                color = M3TextTertiary
                            )
                        },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = Tajawal,
                            fontSize = 15.sp,
                            color = M3TextPrimary
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppColors.Paper,
                            unfocusedContainerColor = AppColors.Paper,
                            focusedBorderColor = AppColors.Teal,
                            unfocusedBorderColor = AppColors.RowDivider,
                            cursorColor = AppColors.Teal
                        )
                    )

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

                }
            }

                // ==========================================
                // 2. Main Service Cards List or Empty State
                // ==========================================
                if (filteredPlaces.isEmpty()) {
                    item(key = "empty_state") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
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
                    }
                } else {
                    items(filteredPlaces, key = { it.id }) { place ->
                            val main = ServiceDiscovery.nearestBranch(place, userCoordinate) ?: return@items
                            val isOpen = main.isOpen

                            // Calculate actual road distance and drive time
                            val distanceKm = ServiceDiscovery.distanceToBranch(main, userCoordinate) ?: 0.0
                            val branchDist = String.format(java.util.Locale.US, "%.1f", distanceKm)
                            val estDriveMin = LocationService.estimateDriveTime(distanceKm)

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
                                    .padding(horizontal = 16.dp)
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

@Composable
private fun HeaderActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    selected: Boolean,
    startPadding: androidx.compose.ui.unit.Dp = 6.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(start = startPadding)
            .size(42.dp)
            .shadow(1.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) AppColors.SoftTeal else AppColors.Paper)
            .border(1.dp, if (selected) AppColors.SoftTeal else AppColors.RowDivider, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (selected) AppColors.Teal else AppColors.Ink,
            modifier = Modifier.size(19.dp)
        )
    }
}
