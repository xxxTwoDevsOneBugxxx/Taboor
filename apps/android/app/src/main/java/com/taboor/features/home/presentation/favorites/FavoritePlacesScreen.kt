package com.taboor.features.home.presentation.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.components.TaboorCard
import com.taboor.core.components.TaboorEmptyState
import com.taboor.core.components.TaboorTopAppBar
import com.taboor.core.localization.LocaleManager
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick
import com.taboor.features.home.data.repository.ServiceRepository
import com.taboor.features.home.domain.model.ServiceCategory
import com.taboor.features.home.domain.model.ServicePlace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritePlacesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (ServicePlace) -> Unit
) {
    val isArabic = LocaleManager.isArabic
    val favoriteIds by ServiceRepository.favoritePlaceIds.collectAsState()
    var selectedCategory by remember { mutableStateOf<ServiceCategory?>(null) }

    val allFavorites = remember(favoriteIds) {
        ServiceRepository.places.filter { favoriteIds.contains(it.id) }
    }

    val filteredFavorites = remember(allFavorites, selectedCategory) {
        if (selectedCategory == null) allFavorites
        else allFavorites.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TaboorTopAppBar(
                title = if (isArabic) "الأماكن المفضلة" else "Favorite Places",
                subtitle = if (isArabic) "وصول فوري وحجز سريع لأماكنك المعتادة" else "Instant access to your favorite places",
                onNavigateBack = onNavigateBack,
                actions = {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = AppColors.Pink.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = null,
                                tint = AppColors.Pink,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${allFavorites.size}",
                                fontFamily = Tajawal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Pink
                            )
                        }
                    }
                }
            )
        },
        containerColor = AppColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // Category Filter Row
            if (allFavorites.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        CategoryFilterPill(
                            label = if (isArabic) "الكل" else "All",
                            isSelected = selectedCategory == null,
                            onClick = { selectedCategory = null }
                        )
                    }
                    items(ServiceCategory.entries.toTypedArray()) { cat ->
                        CategoryFilterPill(
                            label = cat.localizedTitle(isArabic),
                            isSelected = selectedCategory == cat,
                            onClick = { selectedCategory = cat }
                        )
                    }
                }
            }

            // Places List or Empty State
            if (filteredFavorites.isEmpty()) {
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
                            icon = Icons.Rounded.FavoriteBorder,
                            iconTint = AppColors.Pink,
                            iconBackground = AppColors.Pink.copy(alpha = 0.12f),
                            title = if (isArabic) "لا توجد أماكن في المفضلة بعد" else "No favorite places yet",
                            description = if (isArabic)
                                "اضغط على أيقونة القلب في أي عيادة أو متجر لحفظها والوصول السريع إليها هنا."
                            else
                                "Tap the heart icon on any clinic or store to save it here."
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredFavorites, key = { it.id }) { place ->
                        FavoritePlaceCard(
                            place = place,
                            isArabic = isArabic,
                            onToggleFavorite = { ServiceRepository.toggleFavorite(place.id) },
                            onBookNow = { onNavigateToDetails(place) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .bounceClick()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) AppColors.Teal else AppColors.Paper,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) AppColors.Teal else AppColors.CardBorder
        )
    ) {
        Text(
            text = label,
            fontFamily = Tajawal,
            fontSize = 12.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else AppColors.TextPrimary,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun FavoritePlaceCard(
    place: ServicePlace,
    isArabic: Boolean,
    onToggleFavorite: () -> Unit,
    onBookNow: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = AppColors.Paper,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.CardBorder),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon Squircle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(place.color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = place.icon,
                        contentDescription = null,
                        tint = place.color,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = place.localizedName(isArabic),
                        fontFamily = Tajawal,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Ink
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = AppColors.Amber,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${place.starRating}",
                            fontFamily = Tajawal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Ink
                        )
                        Text(
                            text = "•",
                            fontSize = 10.sp,
                            color = AppColors.TextTertiary
                        )
                        Text(
                            text = if (isArabic) "${place.branches.size} فروع" else "${place.branches.size} branches",
                            fontFamily = Tajawal,
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                }

                // Heart Toggle
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = "Unfavorite",
                        tint = AppColors.Pink,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = AppColors.CardBorder)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(AppColors.Amber)
                    )
                    Text(
                        text = if (isArabic) "${place.totalWaiting} في الانتظار حالياً" else "${place.totalWaiting} waiting now",
                        fontFamily = Tajawal,
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary
                    )
                }

                Button(
                    onClick = onBookNow,
                    modifier = Modifier
                        .height(38.dp)
                        .bounceClick(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Teal),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Text(
                        text = if (isArabic) "حجز دور" else "Book Queue",
                        fontFamily = Tajawal,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
