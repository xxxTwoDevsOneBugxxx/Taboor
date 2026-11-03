package com.taboor.features.home.presentation.details

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taboor.core.components.TaboorMapView
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
import com.taboor.core.services.LocationService
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.core.utils.bounceClick
import com.taboor.features.home.data.repository.ServiceRepository
import com.taboor.features.home.domain.model.ServiceBranch
import com.taboor.features.home.domain.model.ServicePlace
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Full details of a service with branch selection.
 * Pixel-perfect reproduction of Flutter's `service_details_screen.dart`:
 * - Full-screen In-App Map in the background
 * - Top floating rounded back button
 * - Ride-hailing style Draggable Bottom Sheet (`BottomSheetScaffold`)
 * - Header: Service icon (52x52) + name + live waiting counter & branch count
 * - "اختر الفرع" with dynamic counter chip (e.g. 1 / 2)
 * - Detailed branch cards with radio indicator, distance, wait time, and "فتح في خرائط Google"
 * - "عن الخدمة" (About description)
 * - "مواعيد العمل" (Working hours banner with schedule icon)
 * - Large full-width Teal CTA: "انضم إلى هذا الطابور"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceDetailsScreen(
    place: ServicePlace,
    onNavigateBack: () -> Unit,
    onJoinedQueue: () -> Unit = {}
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val isArabic = LocaleManager.isArabic
    val coroutineScope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()

    androidx.activity.compose.BackHandler(onBack = onNavigateBack)

    var selectedBranchIndex by remember { mutableIntStateOf(0) }
    var isJoining by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val selectedBranch = place.branches.getOrElse(selectedBranchIndex) { place.branches.first() }

    fun openInGoogleMaps(branch: ServiceBranch) {
        val lat = branch.lat ?: 30.0444
        val lng = branch.lng ?: 31.2357
        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&travelmode=driving")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 340.dp,
        sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetContainerColor = AppColors.Paper,
        sheetShadowElevation = 18.dp,
        sheetDragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 14.dp, bottom = 10.dp)
                    .width(52.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(AppColors.Gray300)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        sheetContent = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // ==========================================
                // 1. Sheet Header: Service icon + name + waiting count
                // ==========================================
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(place.color.copy(alpha = 0.12f)),
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

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = place.localizedName(isArabic),
                                fontFamily = Tajawal,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Ink
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${strings.serviceWaitingNow(place.totalWaiting)} • ${place.branches.size} ${strings.serviceBranches}",
                                fontFamily = Tajawal,
                                fontSize = 13.sp,
                                color = AppColors.Gray600
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // ==========================================
                // 2. Choose Branch Label + Counter Chip
                // ==========================================
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.serviceChooseBranch,
                            fontFamily = Tajawal,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Ink
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .background(AppColors.SoftTeal)
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${selectedBranchIndex + 1} / ${place.branches.size}",
                                fontFamily = Tajawal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AppColors.Teal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // ==========================================
                // 3. Branch Cards List
                // ==========================================
                itemsIndexed(place.branches) { index, branch ->
                    val isSelected = index == selectedBranchIndex

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .bounceClick(scaleDown = 0.98f) { selectedBranchIndex = index },
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) AppColors.SoftTeal.copy(alpha = 0.35f) else AppColors.Paper,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.6.dp else 1.dp,
                            color = if (isSelected) AppColors.Teal else AppColors.Gray200
                        ),
                        shadowElevation = if (isSelected) 3.dp else 1.dp
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Selection indicator radio circle
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = if (isSelected) 5.dp else 2.dp,
                                            color = if (isSelected) AppColors.Teal else AppColors.Gray400,
                                            shape = CircleShape
                                        )
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = branch.localizedLabel(isArabic),
                                        fontFamily = Tajawal,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.Ink
                                    )
                                    branch.localizedAddress(isArabic)?.let { addr ->
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = addr,
                                            fontFamily = Tajawal,
                                            fontSize = 12.sp,
                                            color = AppColors.Gray600
                                        )
                                    }
                                }

                                // Open in Google Maps button
                                IconButton(
                                    onClick = { openInGoogleMaps(branch) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                        contentDescription = strings.serviceOpenMaps,
                                        tint = AppColors.Teal,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Branch Live Stat Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Waiting count pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AppColors.Teal.copy(alpha = 0.1f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${branch.waitingCount} ${strings.peopleWaiting(branch.waitingCount)}",
                                        fontFamily = Tajawal,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.Teal
                                    )
                                }

                                // Est wait time pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AppColors.Amber.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "~${branch.estWaitMinutes} ${if (isArabic) "د" else "min"}",
                                        fontFamily = Tajawal,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.Amber
                                    )
                                }

                                // Distance
                                branch.distanceKm?.let { dist ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AppColors.Gray100)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = strings.distanceAway(dist),
                                            fontFamily = Tajawal,
                                            fontSize = 11.sp,
                                            color = AppColors.Gray700
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 4. "عن الخدمة" (About Section)
                // ==========================================
                place.about?.let { about ->
                    item {
                        Spacer(modifier = Modifier.height(18.dp))
                        HorizontalDivider(thickness = 1.dp, color = AppColors.Gray200)
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = strings.serviceAbout,
                            fontFamily = Tajawal,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Ink
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = about,
                            fontFamily = Tajawal,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = AppColors.Gray700
                        )
                    }
                }

                // ==========================================
                // 5. "مواعيد العمل" (Working Hours Banner)
                // ==========================================
                place.workingHours?.let { hours ->
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = AppColors.SoftTeal.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AppColors.Paper),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Schedule,
                                        contentDescription = null,
                                        tint = AppColors.Teal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = strings.serviceWorkingHours,
                                        fontFamily = Tajawal,
                                        fontSize = 10.sp,
                                        color = AppColors.Gray500
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = hours,
                                        fontFamily = Tajawal,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.Ink
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // 6. Large Full-Width Teal CTA Button
                // ==========================================
                item {
                    Spacer(modifier = Modifier.height(22.dp))
                    Button(
                        onClick = {
                            if (!isJoining) {
                                isJoining = true
                                coroutineScope.launch {
                                    delay(700)
                                    isJoining = false
                                    snackbarHostState.showSnackbar(
                                        "${place.localizedName(isArabic)} (${selectedBranch.localizedLabel(isArabic)}) — ${strings.serviceJoinQueue}"
                                    )
                                }
                            }
                        },
                        enabled = selectedBranch.isOpen && !isJoining,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Teal,
                            disabledContainerColor = AppColors.Gray300
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        if (isJoining) {
                            CircularProgressIndicator(
                                color = AppColors.Paper,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedBranch.isOpen) {
                                    "${strings.serviceJoinQueue} (${selectedBranch.localizedLabel(isArabic)})"
                                } else {
                                    strings.serviceClosed
                                },
                                fontFamily = Tajawal,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { _ ->
        // ==========================================
        // Full-screen Map Background & Floating Back Button
        // ==========================================
        Box(modifier = Modifier.fillMaxSize()) {
            TaboorMapView(
                branchLat = selectedBranch.lat ?: 30.0444,
                branchLng = selectedBranch.lng ?: 31.2357,
                branchTitle = "${place.localizedName(isArabic)} - ${selectedBranch.localizedLabel(isArabic)}",
                branchAddress = selectedBranch.localizedAddress(isArabic),
                isFullScreen = true,
                modifier = Modifier.fillMaxSize()
            )

            // Floating Top Back Button (matching Flutter)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AppColors.Paper,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .size(42.dp)
                    .bounceClick(scaleDown = 0.9f) { onNavigateBack() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                        contentDescription = strings.profileBack,
                        tint = AppColors.Ink,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
