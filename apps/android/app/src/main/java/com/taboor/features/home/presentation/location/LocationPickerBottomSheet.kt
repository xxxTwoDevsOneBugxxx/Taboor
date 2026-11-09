package com.taboor.features.home.presentation.location

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.localization.LocaleManager
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick

data class NeighborhoodOption(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val subtitleAr: String,
    val subtitleEn: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerBottomSheet(
    currentLocation: String,
    onLocationSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isArabic = LocaleManager.isArabic
    val neighborhoods = listOf(
        NeighborhoodOption("maadi", "المعادي، شارع النصر", "Maadi, El-Nasr St.", "القاهرة الكبرى", "Greater Cairo"),
        NeighborhoodOption("nasr_city", "مدينة نصر، مكرم عبيد", "Nasr City, Makram Ebeid", "شرق القاهرة", "East Cairo"),
        NeighborhoodOption("new_cairo", "التجمع الخامس، شارع التسعين", "New Cairo, 90th St.", "القاهرة الجديدة", "New Cairo"),
        NeighborhoodOption("downtown", "وسط البلد، ميدان التحرير", "Downtown, Tahrir Sq.", "وسط القاهرة", "Central Cairo"),
        NeighborhoodOption("mohandessin", "المهندسين، شارع شهاب", "Mohandessin, Shehab St.", "الجيزة", "Giza"),
        NeighborhoodOption("sheikh_zayed", "الشيخ زايد، وصلة دهشور", "Sheikh Zayed, Dahshur Link", "الجيزة / أكتوبر", "Giza / 6th Oct")
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = AppColors.Paper,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(44.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(AppColors.Gray300)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AppColors.SoftTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = AppColors.Teal,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = if (isArabic) "اختر موقعك الحالي" else "Select Your Location",
                        fontFamily = Tajawal,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Ink
                    )
                    Text(
                        text = if (isArabic) "لعرض أسرع الطوابير وأقرب الخدمات لمكانك" else "To find fastest queues nearest to you",
                        fontFamily = Tajawal,
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Use Current GPS Location Option
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .bounceClick()
                    .clickable {
                        onLocationSelected(if (isArabic) "موقعي الحالي (GPS)" else "Current Location (GPS)")
                        onDismiss()
                    },
                shape = RoundedCornerShape(16.dp),
                color = AppColors.Teal.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Teal.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MyLocation,
                        contentDescription = null,
                        tint = AppColors.Teal,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) "استخدام موقع الجهاز الدقيق" else "Use precise device GPS",
                            fontFamily = Tajawal,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Teal
                        )
                        Text(
                            text = if (isArabic) "تحديد تلقائي عبر نظام الملاحة" else "Auto-detect via GPS sensors",
                            fontFamily = Tajawal,
                            fontSize = 11.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Neighborhoods List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(neighborhoods) { item ->
                    val name = if (isArabic) item.nameAr else item.nameEn
                    val isSelected = currentLocation.contains(name) || name.contains(currentLocation)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick()
                            .clickable {
                                onLocationSelected(name)
                                onDismiss()
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) AppColors.SoftTeal else AppColors.CardBackground,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AppColors.Teal else AppColors.CardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = name,
                                    fontFamily = Tajawal,
                                    fontSize = 14.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) AppColors.Teal else AppColors.TextPrimary
                                )
                                Text(
                                    text = if (isArabic) item.subtitleAr else item.subtitleEn,
                                    fontFamily = Tajawal,
                                    fontSize = 11.5.sp,
                                    color = AppColors.TextSecondary
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(AppColors.Teal),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
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
