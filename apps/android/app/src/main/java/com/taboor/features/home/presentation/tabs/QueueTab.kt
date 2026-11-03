package com.taboor.features.home.presentation.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
import com.taboor.core.theme.AppColors
import com.taboor.core.utils.bounceClick
import com.taboor.core.utils.bouncePress
import com.taboor.core.utils.pulseEffect
import com.taboor.features.home.data.repository.ServiceRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueTab(
    onExploreServices: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isArabic = LocaleManager.isArabic
    val activeTicket by ServiceRepository.activeTicket.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCancelDialog by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppColors.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val ticket = activeTicket
            if (ticket != null) {
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .padding(bottom = 90.dp)
                    ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.navQueue,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Ink,
                                fontSize = 24.sp
                            )
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(AppColors.Success.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = AppColors.Success,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.queueNowServing,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AppColors.Success,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Big ticket number card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Ink),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AppColors.Teal.copy(alpha = 0.22f))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = strings.ticketNumberLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AppColors.Paper,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = ticket.ticketNumber,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AppColors.Paper,
                                    fontSize = 64.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isArabic) ticket.serviceNameAr else ticket.serviceName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = AppColors.Paper.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = if (isArabic) ticket.branchLabelAr else ticket.branchLabel,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = AppColors.Paper.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Live Counter Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Paper),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = strings.queuePeopleAhead(ticket.waitingAhead),
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.Ink,
                                            fontSize = 26.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${strings.queueEstimate}: ${strings.queueMinutes(ticket.estMinutes)}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = AppColors.Gray600,
                                            fontSize = 14.sp
                                        )
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = strings.queueNowServing,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = AppColors.Gray500,
                                            fontSize = 11.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = ticket.currentServing,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = AppColors.Teal,
                                            fontSize = 24.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Progress bar
                            LinearProgressIndicator(
                                progress = { ticket.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                color = AppColors.Teal,
                                trackColor = AppColors.SoftTeal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Smart Alert Callout
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.SoftTeal)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .pulseEffect(minScale = 0.94f, maxScale = 1.08f, durationMs = 1600)
                                    .clip(CircleShape)
                                    .background(AppColors.Teal.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    tint = AppColors.Teal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "${strings.alertLeaveTitle} — ${strings.alertOnlyTwo}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = AppColors.Ink,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Actions: Delay turn / Cancel
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val delayInteractionSource = remember { MutableInteractionSource() }
                        OutlinedButton(
                            onClick = {
                                ServiceRepository.delayTicket()
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(strings.ticketDelayed)
                                }
                            },
                            interactionSource = delayInteractionSource,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AppColors.Ink
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray300),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .bouncePress(scaleDown = 0.95f, interactionSource = delayInteractionSource)
                        ) {
                            Text(
                                text = strings.delayTurn,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        val cancelInteractionSource = remember { MutableInteractionSource() }
                        Button(
                            onClick = { showCancelDialog = true },
                            interactionSource = cancelInteractionSource,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Error.copy(alpha = 0.12f),
                                contentColor = AppColors.Error
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .bouncePress(scaleDown = 0.95f, interactionSource = cancelInteractionSource)
                        ) {
                            Text(
                                text = strings.cancelTicket,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
                }
            } else {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(AppColors.SoftTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ConfirmationNumber,
                            contentDescription = null,
                            tint = AppColors.Teal,
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = strings.queueEmptyTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Ink,
                            fontSize = 20.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = strings.queueEmptySubtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = AppColors.Gray600,
                            fontSize = 14.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(28.dp))
                    val exploreInteraction = remember { MutableInteractionSource() }
                    Button(
                        onClick = onExploreServices,
                        interactionSource = exploreInteraction,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Teal),
                        modifier = Modifier
                            .height(50.dp)
                            .bouncePress(scaleDown = 0.96f, interactionSource = exploreInteraction)
                    ) {
                        Text(
                            text = strings.homeJoinQueue,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = {
                Text(
                    text = strings.cancelTicket,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = if (isArabic) "هل أنت متأكد من إلغاء تذكرتك في الطابور؟" else "Are you sure you want to cancel your queue ticket?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = AppColors.Gray700)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        ServiceRepository.cancelTicket()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(strings.ticketCancelled)
                        }
                    }
                ) {
                    Text(
                        text = strings.cancelTicket,
                        color = AppColors.Error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(
                        text = strings.profileCancel,
                        color = AppColors.Gray600
                    )
                }
            },
            containerColor = AppColors.Paper,
            shape = RoundedCornerShape(24.dp)
        )
    }
}
