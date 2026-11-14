package com.taboor.features.home.presentation.allservices

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.taboor.core.services.LocationService
import com.taboor.features.home.domain.model.ServicePlace
import com.taboor.features.home.domain.model.ServiceBranch
import com.taboor.features.home.domain.model.ServiceCategory
import com.taboor.features.home.domain.service.ServiceCoordinates
import com.taboor.features.home.domain.service.ServiceDiscovery
import com.taboor.core.localization.LocaleManager
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style

@Composable
fun AllServicesMapView(
    places: List<ServicePlace>,
    userCoordinate: ServiceCoordinates,
    nationwideMode: Boolean,
    selectedPlaceId: String?,
    selectedBranchId: String?,
    onBranchSelected: (ServicePlace, ServiceBranch) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isArabic = LocaleManager.isArabic
    val currentPlaces by rememberUpdatedState(places)
    val currentOnBranchSelected by rememberUpdatedState(onBranchSelected)
    val placePinIcons = remember(context, places, selectedPlaceId, selectedBranchId) {
        places.associate { place ->
            val isSelected = place.id == selectedPlaceId && place.branches.any { it.id == selectedBranchId }
            place.id to createServicePin(context, place.category, isSelected)
        }
    }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var styleLoaded by remember { mutableStateOf(false) }
    var cameraInitialized by remember { mutableStateOf(false) }

    val mapView = remember(context) {
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

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(mapView) {
        mapView.getMapAsync { readyMap ->
            map = readyMap
            readyMap.uiSettings.isAttributionEnabled = false
            readyMap.uiSettings.isLogoEnabled = false
            readyMap.uiSettings.isCompassEnabled = true
            readyMap.setOnMarkerClickListener { marker ->
                val place = currentPlaces.firstOrNull { it.id == marker.title }
                val branch = place?.branches?.firstOrNull { it.id == marker.snippet }
                if (place != null && branch != null) {
                    currentOnBranchSelected(place, branch)
                    readyMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 13.5), 350)
                }
                true
            }
            readyMap.setStyle(Style.Builder().fromUri("https://basemaps.cartocdn.com/gl/voyager-gl-style/style.json")) {
                styleLoaded = true
            }
        }
    }

    LaunchedEffect(styleLoaded, places, isArabic, userCoordinate, nationwideMode) {
        if (!styleLoaded) return@LaunchedEffect
        val readyMap = map ?: return@LaunchedEffect
        val markers = places.flatMap { place ->
            val visibleBranches = if (nationwideMode) place.branches else {
                ServiceDiscovery.localBranches(place, userCoordinate).ifEmpty { place.branches }
            }
            visibleBranches.mapNotNull { branch ->
                val lat = branch.lat ?: return@mapNotNull null
                val lng = branch.lng ?: return@mapNotNull null
                Triple(place, branch, LatLng(lat, lng))
            }
        }

        readyMap.clear()
        markers.forEach { (place, branch, position) ->
            readyMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(place.id)
                    .snippet(branch.id)
                    .icon(placePinIcons[place.id])
            )
        }

        if (!cameraInitialized) {
            readyMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(LatLng(userCoordinate.latitude, userCoordinate.longitude), 11.5)
            )
            cameraInitialized = true
        }
    }

    LaunchedEffect(map, places, selectedPlaceId, selectedBranchId, cameraInitialized) {
        if (!cameraInitialized) return@LaunchedEffect
        val selectedBranch = places
            .firstOrNull { it.id == selectedPlaceId }
            ?.branches
            ?.firstOrNull { it.id == selectedBranchId }
        val lat = selectedBranch?.lat ?: return@LaunchedEffect
        val lng = selectedBranch.lng ?: return@LaunchedEffect
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 12.3),
            400
        )
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}

private fun createServicePin(context: Context, category: ServiceCategory, selected: Boolean): Icon {
    val density = context.resources.displayMetrics.density
    val markerScale = if (selected) 1.10f else 1f
    val width = (36 * density * markerScale).toInt()
    val height = (42 * density * markerScale).toInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val cx = width / 2f
    val cy = 15f * density * markerScale
    val radius = 13f * density * markerScale
    val markerColor = if (selected) {
        android.graphics.Color.rgb(31, 119, 116)
    } else {
        when (category) {
            ServiceCategory.CLINICS -> android.graphics.Color.rgb(55, 132, 127)
            ServiceCategory.SALONS -> android.graphics.Color.rgb(119, 105, 157)
            ServiceCategory.GARAGES -> android.graphics.Color.rgb(184, 125, 55)
            ServiceCategory.OFFICES -> android.graphics.Color.rgb(77, 119, 139)
        }
    }
    val pointer = Path().apply {
        moveTo(cx - 5f * density * markerScale, cy + 7f * density * markerScale)
        lineTo(cx + 5f * density * markerScale, cy + 7f * density * markerScale)
        lineTo(cx, cy + 17f * density * markerScale)
        close()
    }
    paint.color = markerColor
    canvas.drawPath(pointer, paint)
    canvas.drawCircle(cx, cy, radius, paint)

    // A warm, low contrast edge keeps the badge distinct without the stark white pin.
    paint.color = android.graphics.Color.rgb(244, 237, 224)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 1.5f * density
    canvas.drawCircle(cx, cy, radius - paint.strokeWidth / 2f, paint)
    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.rgb(255, 245, 225)
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 2f * density
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeJoin = Paint.Join.ROUND
    val r = width * 0.14f
    when (category) {
        ServiceCategory.CLINICS -> {
            canvas.drawLine(cx - r, cy, cx + r, cy, paint)
            canvas.drawLine(cx, cy - r, cx, cy + r, paint)
        }
        ServiceCategory.SALONS -> {
            canvas.drawCircle(cx - r * 0.75f, cy - r * 0.75f, r * 0.55f, paint)
            canvas.drawCircle(cx + r * 0.75f, cy - r * 0.75f, r * 0.55f, paint)
            canvas.drawLine(cx - r * 0.4f, cy - r * 0.2f, cx + r, cy + r, paint)
            canvas.drawLine(cx + r * 0.4f, cy - r * 0.2f, cx - r, cy + r, paint)
        }
        ServiceCategory.GARAGES -> {
            val car = Path().apply {
                moveTo(cx - r * 1.3f, cy + r * 0.4f)
                lineTo(cx - r, cy - r * 0.35f)
                lineTo(cx + r, cy - r * 0.35f)
                lineTo(cx + r * 1.3f, cy + r * 0.4f)
                lineTo(cx + r * 1.3f, cy + r)
                lineTo(cx - r * 1.3f, cy + r)
                close()
            }
            canvas.drawPath(car, paint)
        }
        ServiceCategory.OFFICES -> {
            canvas.drawRect(cx - r, cy - r * 1.3f, cx + r, cy + r * 1.3f, paint)
            canvas.drawLine(cx, cy - r, cx, cy + r * 0.9f, paint)
            canvas.drawLine(cx - r * 0.7f, cy - r * 0.4f, cx - r * 0.3f, cy - r * 0.4f, paint)
            canvas.drawLine(cx + r * 0.3f, cy - r * 0.4f, cx + r * 0.7f, cy - r * 0.4f, paint)
        }
    }
    paint.style = Paint.Style.FILL
    return IconFactory.getInstance(context).fromBitmap(bitmap)
}
