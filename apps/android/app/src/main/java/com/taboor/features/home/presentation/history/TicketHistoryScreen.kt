package com.taboor.features.home.presentation.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
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
import com.taboor.core.components.TaboorStatusChip
import com.taboor.core.components.TaboorStatusType
import com.taboor.core.components.TaboorTopAppBar
import com.taboor.core.localization.LocaleManager
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick
import com.taboor.features.home.data.repository.ServiceRepository
import com.taboor.features.home.domain.model.TicketHistoryItem
import com.taboor.features.home.domain.model.TicketStatus

enum class HistoryFilter { ALL, COMPLETED, CANCELLED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketHistoryScreen(
    onNavigateBack: () -> Unit
) {
    val isArabic = LocaleManager.isArabic
    val historyItems by ServiceRepository.history.collectAsState()
    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }

    val filteredList = remember(historyItems, selectedFilter) {
        when (selectedFilter) {
            HistoryFilter.ALL -> historyItems
            HistoryFilter.COMPLETED -> historyItems.filter { it.status == TicketStatus.COMPLETED }
            HistoryFilter.CANCELLED -> historyItems.filter { it.status == TicketStatus.CANCELLED }
        }
    }

    val totalSavedMinutes = remember(historyItems) {
        historyItems.filter { it.status == TicketStatus.COMPLETED }.sumOf { it.durationMinutes }
    }

    val completedCount = remember(historyItems) {
        historyItems.count { it.status == TicketStatus.COMPLETED }
    }

    Scaffold(
        topBar = {
            TaboorTopAppBar(
                title = if (isArabic) "سجل الزيارات والتذاكر" else "Ticket History",
                subtitle = if (isArabic) "تاريخ طوابيرك والوقت الذي وفرته" else "Your past queues & saved time",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = AppColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Hero Stat Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        color = AppColors.Ink,
                        shadowElevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (isArabic) "إجمالي الوقت الموفر" else "Total Time Saved",
                                    fontFamily = Tajawal,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = if (isArabic) "$totalSavedMinutes دقيقة" else "$totalSavedMinutes mins",
                                    fontFamily = Tajawal,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AppColors.TealLight
                                )
                                Text(
                                    text = if (isArabic) "عبر $completedCount زيارة مكتملة بدون انتظار في الصالة"
                                    else "Across $completedCount completed visits without waiting",
                                    fontFamily = Tajawal,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.Teal.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.HourglassTop,
                                    contentDescription = null,
                                    tint = AppColors.TealLight,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }

                // Filter Segmented Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            label = if (isArabic) "الكل (${historyItems.size})" else "All (${historyItems.size})",
                            isSelected = selectedFilter == HistoryFilter.ALL,
                            onClick = { selectedFilter = HistoryFilter.ALL },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            label = if (isArabic) "مكتملة ($completedCount)" else "Completed ($completedCount)",
                            isSelected = selectedFilter == HistoryFilter.COMPLETED,
                            onClick = { selectedFilter = HistoryFilter.COMPLETED },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            label = if (isArabic) "ملغية" else "Cancelled",
                            isSelected = selectedFilter == HistoryFilter.CANCELLED,
                            onClick = { selectedFilter = HistoryFilter.CANCELLED },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // History Items List
                if (filteredList.isEmpty()) {
                    item {
                        TaboorCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentPadding = 24.dp
                        ) {
                            TaboorEmptyState(
                                icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                                title = if (isArabic) "لا توجد تذاكر في هذا التصنيف" else "No tickets in this filter",
                                description = if (isArabic) "ستظهر التذاكر هنا بمجرد حجز أو إتمام زياراتك." else "Your tickets will appear here once booked or completed."
                            )
                        }
                    }
                } else {
                    items(filteredList, key = { it.id }) { item ->
                        HistoryCard(item = item, isArabic = isArabic)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .bounceClick()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) AppColors.Teal else AppColors.Paper,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) AppColors.Teal else AppColors.CardBorder
        )
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontFamily = Tajawal,
                fontSize = 12.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else AppColors.TextPrimary
            )
        }
    }
}

@Composable
private fun HistoryCard(
    item: TicketHistoryItem,
    isArabic: Boolean
) {
    val isCompleted = item.status == TicketStatus.COMPLETED

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = AppColors.Paper,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.CardBorder),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Ticket Number Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AppColors.SoftTeal
                ) {
                    Text(
                        text = "#${item.ticketNumber}",
                        fontFamily = Tajawal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.Teal,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Status Badge
                TaboorStatusChip(
                    text = if (isCompleted) (if (isArabic) "مكتملة" else "Completed") else (if (isArabic) "ملغية" else "Cancelled"),
                    type = if (isCompleted) TaboorStatusType.SUCCESS else TaboorStatusType.ERROR
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Service Name & Branch
            Text(
                text = item.localizedServiceName(isArabic),
                fontFamily = Tajawal,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Ink
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.localizedBranch(isArabic),
                fontFamily = Tajawal,
                fontSize = 12.sp,
                color = AppColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = AppColors.CardBorder)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = AppColors.TextTertiary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = item.dateString,
                        fontFamily = Tajawal,
                        fontSize = 11.5.sp,
                        color = AppColors.TextSecondary
                    )
                }

                // Time Saved
                if (isCompleted && item.durationMinutes > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccessTime,
                            contentDescription = null,
                            tint = AppColors.Teal,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isArabic) "وفرت ${item.durationMinutes} دقيقة" else "Saved ${item.durationMinutes}m",
                            fontFamily = Tajawal,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Teal
                        )
                    }
                }
            }
        }
    }
}
