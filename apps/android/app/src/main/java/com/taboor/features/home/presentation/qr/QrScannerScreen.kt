package com.taboor.features.home.presentation.qr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import com.taboor.features.home.data.repository.ServiceRepository
import com.taboor.features.home.domain.model.ServiceBranch
import com.taboor.features.home.domain.model.ServicePlace

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    onNavigateBack: () -> Unit,
    onScanSuccess: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val strings = LocalAppStrings.current
    val isArabic = LocaleManager.isArabic
    val places by ServiceRepository.placesFlow.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var permissionDenied by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    var scanMessage by remember { mutableStateOf<String?>(null) }
    var scannedMatch by remember { mutableStateOf<Pair<ServicePlace, ServiceBranch>?>(null) }
    var showManualEntry by remember { mutableStateOf(false) }
    var showScanSuccess by remember { mutableStateOf(false) }
    var manualCode by remember { mutableStateOf("") }
    var resetScanKey by remember { mutableIntStateOf(0) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        permissionDenied = !granted
        cameraError = null
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasCameraPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun scanCode(rawCode: String) {
        val normalized = rawCode.trim().lowercase()
        if (normalized.isBlank()) return
        val place = places.firstOrNull { candidate ->
            normalized.contains(candidate.id.lowercase()) ||
                normalized.contains(candidate.name.lowercase()) ||
                candidate.branches.any { branch ->
                    normalized.contains(branch.id.lowercase()) ||
                        normalized.contains(branch.label.lowercase()) ||
                        (branch.labelAr?.let { normalized.contains(it.lowercase()) } == true)
                }
        }
        val branch = place?.branches?.firstOrNull { candidate ->
            normalized.contains(candidate.id.lowercase()) ||
                normalized.contains(candidate.label.lowercase()) ||
                (candidate.labelAr?.let { normalized.contains(it.lowercase()) } == true)
        } ?: place?.branches?.firstOrNull()

        if (place == null || branch == null) {
            scanMessage = if (isArabic) "الكود غير معروف. تأكد من رمز الفرع وحاول مرة أخرى." else "This code wasn't recognized. Check the branch QR and try again."
            return
        }

        ServiceRepository.joinQueue(place, branch)
        scannedMatch = place to branch
        scanMessage = null
        showManualEntry = false
        showScanSuccess = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "qrScanLine")
    val scanLinePosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLinePosition"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF102B31), Color(0xFF071619), Color(0xFF03090B))
                )
            )
    ) {
        if (hasCameraPermission) {
            QrCameraPreview(
                torchEnabled = isTorchOn,
                resetScanKey = resetScanKey,
                onCodeDetected = ::scanCode,
                onCameraError = { cameraError = it },
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.58f), Color.Transparent, Color.Black.copy(alpha = 0.68f))
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScannerTopButton(
                    icon = Icons.Rounded.Close,
                    contentDescription = strings.back,
                    onClick = onNavigateBack
                )
                Text(
                    text = if (isArabic) "مسح رمز الفرع" else "Scan branch QR",
                    fontFamily = Tajawal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.White
                )
                ScannerTopButton(
                    icon = if (isTorchOn) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                    contentDescription = if (isArabic) "تشغيل الفلاش" else "Toggle flashlight",
                    highlighted = isTorchOn,
                    enabled = hasCameraPermission,
                    onClick = { isTorchOn = !isTorchOn }
                )
            }

            Spacer(Modifier.weight(0.85f))

            if (!hasCameraPermission) {
                CameraPermissionCard(
                    denied = permissionDenied,
                    onRequestPermission = {
                        if (permissionDenied) {
                            val settingsIntent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                            context.startActivity(settingsIntent)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isArabic) "وجّه الكاميرا إلى رمز QR" else "Point your camera at a QR code",
                        fontFamily = Tajawal,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = if (isArabic) "ضع الرمز داخل الإطار للانضمام إلى الطابور" else "Place the code inside the frame to join the queue",
                        fontFamily = Tajawal,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.78f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .size(270.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(30.dp)),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .offset(y = (scanLinePosition * 210f).dp)
                                .height(2.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, AppColors.Teal, Color.Transparent)
                                    )
                                )
                                .shadow(8.dp, ambientColor = AppColors.Teal)
                        )
                        ScannerCorner(Modifier.align(Alignment.TopStart), topStart = true)
                        ScannerCorner(Modifier.align(Alignment.TopEnd), topEnd = true)
                        ScannerCorner(Modifier.align(Alignment.BottomStart), bottomStart = true)
                        ScannerCorner(Modifier.align(Alignment.BottomEnd), bottomEnd = true)
                    }
                    if (cameraError != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = cameraError.orEmpty(),
                            fontFamily = Tajawal,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.82f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.weight(0.9f))

            if (hasCameraPermission) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(bottom = 18.dp)
                ) {
                    Surface(
                        onClick = { showManualEntry = true },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            Icon(Icons.Rounded.Keyboard, null, tint = AppColors.Teal, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (isArabic) "إدخال الرمز يدوياً" else "Enter code manually",
                                fontFamily = Tajawal,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Ink
                            )
                        }
                    }
                    Text(
                        text = if (isArabic) "المسح آمن ويتم على جهازك" else "Scanning is private and stays on your device",
                        fontFamily = Tajawal,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.66f)
                    )
                }
            } else {
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (showManualEntry) {
        ModalBottomSheet(
            onDismissRequest = { showManualEntry = false; scanMessage = null },
            containerColor = AppColors.Paper,
            contentColor = AppColors.Ink,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = { BottomSheetDefaults.DragHandle(color = AppColors.RowDivider) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 22.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isArabic) "إدخال كود الفرع" else "Enter branch code",
                    fontFamily = Tajawal,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Ink
                )
                Text(
                    text = if (isArabic) "اكتب الرمز المطبوع بجوار QR أو اختر مكاناً للتجربة." else "Type the code next to the QR, or choose a place to try it.",
                    fontFamily = Tajawal,
                    fontSize = 13.sp,
                    color = AppColors.TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    places.take(4).forEach { place ->
                        FilterChip(
                            selected = manualCode.equals(place.id, ignoreCase = true),
                            onClick = { manualCode = place.id },
                            label = {
                                Text(
                                    place.localizedName(isArabic),
                                    fontFamily = Tajawal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = { Icon(place.icon, null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AppColors.SoftTeal,
                                selectedLabelColor = AppColors.Ink,
                                selectedLeadingIconColor = AppColors.Teal
                            )
                        )
                    }
                }
                OutlinedTextField(
                    value = manualCode,
                    onValueChange = { manualCode = it; scanMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    label = { Text(if (isArabic) "رمز QR" else "QR code", fontFamily = Tajawal) },
                    leadingIcon = { Icon(Icons.Rounded.QrCode2, null, tint = AppColors.Teal) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                )
                if (scanMessage != null) {
                    Text(scanMessage.orEmpty(), fontFamily = Tajawal, color = AppColors.Error, fontSize = 12.sp)
                }
                Button(
                    onClick = { scanCode(manualCode) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = manualCode.isNotBlank(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Teal)
                ) {
                    Text(
                        if (isArabic) "تأكيد الرمز والانضمام" else "Confirm code and join",
                        fontFamily = Tajawal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showScanSuccess && scannedMatch != null) {
        val (place, branch) = scannedMatch!!
        AlertDialog(
            onDismissRequest = { showScanSuccess = false; onScanSuccess() },
            containerColor = AppColors.Paper,
            shape = RoundedCornerShape(24.dp),
            icon = { Icon(Icons.Rounded.CheckCircle, null, tint = AppColors.Success, modifier = Modifier.size(34.dp)) },
            title = {
                Text(
                    if (isArabic) "تم الانضمام للطابور" else "You're in the queue",
                    fontFamily = Tajawal,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Ink
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(place.localizedName(isArabic), fontFamily = Tajawal, fontWeight = FontWeight.Bold, color = AppColors.Ink)
                    Text(branch.localizedLabel(isArabic), fontFamily = Tajawal, color = AppColors.TextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showScanSuccess = false; onScanSuccess() },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Teal)
                ) {
                    Text(if (isArabic) "عرض التذكرة" else "View ticket", fontFamily = Tajawal)
                }
            }
        )
    }

    if (scanMessage != null && !showManualEntry && !showScanSuccess) {
        AlertDialog(
            onDismissRequest = {
                scanMessage = null
                resetScanKey++
            },
            containerColor = AppColors.Paper,
            shape = RoundedCornerShape(24.dp),
            icon = { Icon(Icons.Rounded.QrCodeScanner, null, tint = AppColors.Amber, modifier = Modifier.size(32.dp)) },
            title = {
                Text(
                    if (isArabic) "الرمز غير معروف" else "QR code not recognized",
                    fontFamily = Tajawal,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Ink
                )
            },
            text = {
                Text(scanMessage.orEmpty(), fontFamily = Tajawal, color = AppColors.TextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        scanMessage = null
                        resetScanKey++
                    },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Teal)
                ) {
                    Text(if (isArabic) "حاول مرة أخرى" else "Try again", fontFamily = Tajawal)
                }
            }
        )
    }
}

@Composable
private fun ScannerTopButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    highlighted: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = if (highlighted) AppColors.Amber.copy(alpha = 0.20f) else Color.Black.copy(alpha = 0.42f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (highlighted) AppColors.Amber else Color.White.copy(alpha = if (enabled) 1f else 0.4f),
                modifier = Modifier.size(19.dp)
            )
        }
    }
}

@Composable
private fun CameraPermissionCard(
    denied: Boolean,
    onRequestPermission: () -> Unit
) {
    val isArabic = LocaleManager.isArabic
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color(0xCC102B31),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(AppColors.Teal.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.CameraAlt, null, tint = AppColors.Teal, modifier = Modifier.size(32.dp))
            }
            Text(
                if (isArabic) "الكاميرا مطلوبة لمسح الرمز" else "Camera access is needed to scan",
                fontFamily = Tajawal,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                if (isArabic) "اسمح لطابور باستخدام الكاميرا لمسح رمز الفرع والانضمام إلى الطابور مباشرة." else "Allow Taboor to use the camera to scan a branch code and join its queue.",
                fontFamily = Tajawal,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.76f),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRequestPermission,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Teal),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp)
            ) {
                Icon(if (denied) Icons.Rounded.Settings else Icons.Rounded.CameraAlt, null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isArabic) {
                        if (denied) "فتح إعدادات التطبيق" else "السماح بالكاميرا"
                    } else {
                        if (denied) "Open app settings" else "Allow camera"
                    },
                    fontFamily = Tajawal,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ScannerCorner(
    modifier: Modifier,
    topStart: Boolean = false,
    topEnd: Boolean = false,
    bottomStart: Boolean = false,
    bottomEnd: Boolean = false
) {
    val cornerShape = RoundedCornerShape(
        topStart = if (topStart) 10.dp else 0.dp,
        topEnd = if (topEnd) 10.dp else 0.dp,
        bottomStart = if (bottomStart) 10.dp else 0.dp,
        bottomEnd = if (bottomEnd) 10.dp else 0.dp
    )
    Box(
        modifier = modifier
            .size(30.dp)
            .border(3.dp, AppColors.Teal, cornerShape)
    )
}
