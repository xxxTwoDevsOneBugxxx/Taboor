package com.taboor.core.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.taboor.core.localization.LocaleManager
import com.taboor.core.location.TaboorPrecomputedRoutes
import com.taboor.core.services.LocationService
import com.taboor.core.services.RoutingService
import com.taboor.core.theme.AppColors
import com.taboor.core.utils.bounceClick
import kotlinx.coroutines.launch
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

/**
 * High-Performance, Silky Smooth Native Map Component for Taboor using MapLibre Native SDK (OpenGL).
 * Designed for 120 FPS Uber/Talabat level performance:
 * - Carto Voyager GL style loaded ONCE (cached forever in memory)
 * - Zero style reload on recomposition / location update
 * - Double-stroke route polyline (halo + teal line)
 * - Custom cached high-DPI pins
 * - Smooth camera movements
 */
@Composable
fun TaboorMapView(
    branchLat: Double,
    branchLng: Double,
    branchTitle: String,
    branchAddress: String? = null,
    branchId: String? = null,
    isFullScreen: Boolean = false,
    showControls: Boolean = true,
    showInfoChip: Boolean = true,
    onMapReady: ((MapLibreMap) -> Unit)? = null,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(280.dp)
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isArabic = LocaleManager.isArabic
    val coroutineScope = rememberCoroutineScope()

    var userPosition by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var maplibreMapRef by remember { mutableStateOf<MapLibreMap?>(null) }
    var isStyleLoaded by remember { mutableStateOf(false) }

    // Carto Voyager GL Vector Style (Apple Maps / Uber aesthetic, 100% free)
    val mapStyleUrl = remember { "https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json" }

    // Cache marker icons once to avoid GC churn
    val branchIcon = remember(context) { createBranchMarkerIcon(context) }
    val userIcon = remember(context) { createUserMarkerIcon(context) }

    // Resolve user location once
    LaunchedEffect(Unit) {
        val pos = LocationService.getCurrentPosition(context)
        userPosition = pos
    }

    // Native MapView Instance (Created once)
    val mapView = remember {
        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            onCreate(null)
            onStart()
            onResume()
        }
    }

    // Lifecycle Synchronization
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Initialize MapLibre Style ONCE
    LaunchedEffect(mapView) {
        mapView.getMapAsync { map ->
            maplibreMapRef = map
            onMapReady?.invoke(map)

            map.uiSettings.isAttributionEnabled = false
            map.uiSettings.isLogoEnabled = false
            map.uiSettings.isCompassEnabled = false

            map.setStyle(Style.Builder().fromUri(mapStyleUrl)) { _ ->
                isStyleLoaded = true
            }
        }
    }

    // Update Annotations & Route smoothly when ready (WITHOUT reloading style!)
    LaunchedEffect(isStyleLoaded, branchLat, branchLng, userPosition, branchId) {
        if (!isStyleLoaded) return@LaunchedEffect
        val map = maplibreMapRef ?: return@LaunchedEffect

        map.clear()

        // Check for authentic precomputed street navigation route (1:1 with iOS)
        val targetBranchId = branchId ?: TaboorPrecomputedRoutes.findMatchingBranchId(branchLat, branchLng)
        val precomputedPoints = TaboorPrecomputedRoutes.getRoute(targetBranchId)

        if (precomputedPoints != null && precomputedPoints.isNotEmpty()) {
            val userStartCoord = precomputedPoints.first()
            val branchEndCoord = precomputedPoints.last()

            // Destination Branch Pin placed exactly at the end of the route
            map.addMarker(
                MarkerOptions()
                    .position(branchEndCoord)
                    .title(branchTitle)
                    .snippet(branchAddress ?: "")
                    .icon(branchIcon)
            )

            // User starting marker on route
            map.addMarker(
                MarkerOptions()
                    .position(userStartCoord)
                    .title(if (isArabic) "موقعك الحالي" else "Your Location")
                    .icon(userIcon)
            )

            // Double-stroke high contrast navigation polyline (1:1 with iOS Taboor Teal)
            map.addPolyline(
                PolylineOptions()
                    .addAll(precomputedPoints)
                    .color(android.graphics.Color.argb(80, 11, 76, 86))
                    .width(7.5f)
            )
            map.addPolyline(
                PolylineOptions()
                    .addAll(precomputedPoints)
                    .color(android.graphics.Color.parseColor("#159F99"))
                    .width(4.5f)
            )

            // Frame route smoothly
            val boundsBuilder = LatLngBounds.Builder()
                .include(branchEndCoord)
                .include(userStartCoord)
            for (pt in precomputedPoints) {
                boundsBuilder.include(pt)
            }
            val bounds = boundsBuilder.build()

            map.animateCamera(
                CameraUpdateFactory.newLatLngBounds(bounds, 130),
                800
            )
        } else {
            val branchLatLng = LatLng(branchLat, branchLng)
            map.addMarker(
                MarkerOptions()
                    .position(branchLatLng)
                    .title(branchTitle)
                    .snippet(branchAddress ?: "")
                    .icon(branchIcon)
            )
            val userPos = userPosition
            if (userPos != null) {
                val userLatLng = LatLng(userPos.first, userPos.second)
                map.addMarker(
                    MarkerOptions()
                        .position(userLatLng)
                        .title(if (isArabic) "موقعك الحالي" else "Your Location")
                        .icon(userIcon)
                )

                // Async route calculation fallback
                coroutineScope.launch {
                    val routeResult = RoutingService.getRoute(
                        startLat = userPos.first,
                        startLng = userPos.second,
                        endLat = branchLat,
                        endLng = branchLng
                    )

                    val points = routeResult?.points?.map { LatLng(it.first, it.second) }
                        ?: listOf(userLatLng, branchLatLng)

                    map.addPolyline(
                        PolylineOptions()
                            .addAll(points)
                            .color(android.graphics.Color.argb(80, 11, 76, 86))
                            .width(7.5f)
                    )
                    map.addPolyline(
                        PolylineOptions()
                            .addAll(points)
                            .color(android.graphics.Color.parseColor("#159F99"))
                            .width(4.5f)
                    )

                    val bounds = LatLngBounds.Builder()
                        .include(branchLatLng)
                        .include(userLatLng)
                        .build()

                    map.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(bounds, 130),
                        800
                    )
                }
            } else {
                val cameraPos = CameraPosition.Builder()
                    .target(branchLatLng)
                    .zoom(15.5)
                    .build()
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos), 600)
            }
        }
    }

    val containerModifier = if (isFullScreen || !showControls) {
        modifier.background(AppColors.Paper)
    } else {
        modifier
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.Paper)
            .border(1.dp, AppColors.RowDivider, RoundedCornerShape(24.dp))
    }

    Box(modifier = containerModifier) {
        // Native OpenGL Vector Map
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )

        // Floating Map Controls (Recenter buttons)
        if (showControls && !isFullScreen) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = AppColors.Paper,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(38.dp)
                        .bounceClick {
                            val userPos = userPosition ?: return@bounceClick
                            maplibreMapRef?.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(LatLng(userPos.first, userPos.second), 16.0),
                                600
                            )
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.MyLocation,
                            contentDescription = "My Location",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Creates custom Taboor Teal destination pin matching Uber/Apple Maps style.
 */
private fun createBranchMarkerIcon(context: Context): Icon {
    val density = context.resources.displayMetrics.density
    val w = (32 * density).toInt()
    val h = (44 * density).toInt()

    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Soft drop shadow at base
    paint.color = android.graphics.Color.argb(55, 11, 76, 86)
    canvas.drawOval(w * 0.22f, h * 0.88f, w * 0.78f, h.toFloat(), paint)

    // Pin Outer Silhouette (Taboor Ink)
    paint.color = android.graphics.Color.parseColor("#0B4C56")
    val path = android.graphics.Path().apply {
        moveTo(w / 2f, h * 0.88f)
        cubicTo(w * 0.15f, h * 0.58f, 0f, h * 0.40f, 0f, h * 0.28f)
        cubicTo(0f, h * 0.12f, w * 0.22f, 0f, w / 2f, 0f)
        cubicTo(w * 0.78f, 0f, w.toFloat(), h * 0.12f, w.toFloat(), h * 0.28f)
        cubicTo(w.toFloat(), h * 0.40f, w * 0.85f, h * 0.58f, w / 2f, h * 0.88f)
        close()
    }
    canvas.drawPath(path, paint)

    // Crisp White Outline
    paint.color = android.graphics.Color.WHITE
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 2.5f * density
    canvas.drawPath(path, paint)

    // Inner Taboor Teal Badge
    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.parseColor("#159F99")
    canvas.drawCircle(w / 2f, h * 0.28f, w * 0.22f, paint)

    // Center white dot
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(w / 2f, h * 0.28f, w * 0.08f, paint)

    return IconFactory.getInstance(context).fromBitmap(bitmap)
}

/**
 * Creates Apple Maps / Uber pulsing cyan user location dot with white ring.
 */
private fun createUserMarkerIcon(context: Context): Icon {
    val density = context.resources.displayMetrics.density
    val sizePx = (24 * density).toInt()

    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Soft cyan halo
    paint.color = android.graphics.Color.argb(50, 2, 132, 199)
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, paint)

    // White ring
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx * 0.38f, paint)

    // Solid Cyan Center
    paint.color = android.graphics.Color.parseColor("#0284C7")
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx * 0.26f, paint)

    return IconFactory.getInstance(context).fromBitmap(bitmap)
}
