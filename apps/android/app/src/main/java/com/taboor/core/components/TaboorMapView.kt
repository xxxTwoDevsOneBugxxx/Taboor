package com.taboor.core.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.viewinterop.AndroidView
import com.taboor.core.localization.LocalAppStrings
import com.taboor.core.localization.LocaleManager
import com.taboor.core.services.LocationService
import com.taboor.core.services.RoutingService
import com.taboor.core.theme.AppColors
import com.taboor.core.theme.Tajawal
import kotlinx.coroutines.launch

/**
 * Interactive In-App Map Component for Taboor.
 * Renders vector/raster CartoDB Voyager & OpenStreetMap tiles with custom styled pins,
 * user GPS location, driving route polyline, and camera controls.
 * Zero external API key dependencies required.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TaboorMapView(
    branchLat: Double,
    branchLng: Double,
    branchTitle: String,
    branchAddress: String? = null,
    isFullScreen: Boolean = false,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(280.dp)
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val isArabic = LocaleManager.isArabic
    val coroutineScope = rememberCoroutineScope()

    var userPosition by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var distanceText by remember { mutableStateOf<String?>(null) }
    var driveTimeText by remember { mutableStateOf<String?>(null) }

    // Resolve user location and calculate distance
    LaunchedEffect(branchLat, branchLng) {
        val pos = LocationService.getCurrentPosition(context)
        userPosition = pos
        if (pos != null) {
            val dist = LocationService.distanceKm(pos.first, pos.second, branchLat, branchLng)
            val time = LocationService.estimateDriveTime(dist)
            distanceText = "%.1f %s".format(dist, if (isArabic) "كم" else "km")
            driveTimeText = "%d %s".format(time, if (isArabic) "دقيقة قيادة" else "min drive")
        } else {
            distanceText = null
            driveTimeText = null
        }
    }

    val htmlContent = remember(branchLat, branchLng, branchTitle, userPosition) {
        buildMapHtml(
            branchLat = branchLat,
            branchLng = branchLng,
            branchTitle = branchTitle.replace("'", "\\'"),
            userLat = userPosition?.first,
            userLng = userPosition?.second,
            isArabic = isArabic
        )
    }

    val containerModifier = if (isFullScreen) {
        modifier.background(AppColors.Paper)
    } else {
        modifier
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, AppColors.Gray200, RoundedCornerShape(24.dp))
            .background(AppColors.Paper)
    }

    Box(
        modifier = containerModifier
    ) {
        // 1. Interactive Map View
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        useWideViewPort = true
                        loadWithOverviewMode = true
                    }
                    webViewClient = WebViewClient()
                    loadDataWithBaseURL("https://taboor.app", htmlContent, "text/html", "UTF-8", null)
                    webViewRef = this
                }
            },
            update = { wv ->
                wv.loadDataWithBaseURL("https://taboor.app", htmlContent, "text/html", "UTF-8", null)
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Floating Info Chip (Top-Start: Distance & Driving time)
        if (distanceText != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppColors.Paper.copy(alpha = 0.94f),
                shadowElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DirectionsCar,
                        contentDescription = null,
                        tint = AppColors.Teal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$distanceText • $driveTimeText",
                        fontFamily = Tajawal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Ink
                    )
                }
            }
        }

        // 3. Floating Action Buttons (Top-End: Recenter Branch, Recenter Me, Open Maps)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Recenter on Branch
            Surface(
                shape = CircleShape,
                color = AppColors.Paper,
                shadowElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200),
                modifier = Modifier
                    .size(38.dp)
                    .clickable {
                        webViewRef?.evaluateJavascript(
                            "map.flyTo([$branchLat, $branchLng], 16, { duration: 1.2 });",
                            null
                        )
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Store,
                        contentDescription = "Center Branch",
                        tint = AppColors.Teal,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Recenter on User Location
            if (userPosition != null) {
                Surface(
                    shape = CircleShape,
                    color = AppColors.Paper,
                    shadowElevation = 4.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200),
                    modifier = Modifier
                        .size(38.dp)
                        .clickable {
                            val uLat = userPosition!!.first
                            val uLng = userPosition!!.second
                            webViewRef?.evaluateJavascript(
                                "map.flyTo([$uLat, $uLng], 15, { duration: 1.2 });",
                                null
                            )
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.MyLocation,
                            contentDescription = "Center Me",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Full external Google Maps launch
            Surface(
                shape = CircleShape,
                color = AppColors.Paper,
                shadowElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Gray200),
                modifier = Modifier
                    .size(38.dp)
                    .clickable {
                        try {
                            val uri = Uri.parse("geo:$branchLat,$branchLng?q=$branchLat,$branchLng($branchTitle)")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        } catch (_: Exception) {
                            try {
                                val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$branchLat,$branchLng")
                                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                            } catch (_: Exception) {}
                        }
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                        contentDescription = "Open in Maps",
                        tint = AppColors.Gray700,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun buildMapHtml(
    branchLat: Double,
    branchLng: Double,
    branchTitle: String,
    userLat: Double?,
    userLng: Double?,
    isArabic: Boolean
): String {
    val hasUser = userLat != null && userLng != null

    val userMarkerScript = if (hasUser) {
        """
        var userIcon = L.divIcon({
            className: '',
            html: '<div class="user-pulse"><div class="user-core"></div></div>',
            iconSize: [20, 20],
            iconAnchor: [10, 10]
        });
        var userMarker = L.marker([$userLat, $userLng], { icon: userIcon }).addTo(map);
        userMarker.bindPopup("<b>${if (isArabic) "موقعك الحالي" else "Your Location"}</b>");

        // Polyline connecting user to branch
        var routeLine = L.polyline([[$userLat, $userLng], [$branchLat, $branchLng]], {
            color: '#0E7490',
            weight: 3.5,
            opacity: 0.85,
            dashArray: '6, 8'
        }).addTo(map);

        // Fit map bounds to show both points with padding
        var bounds = L.latLngBounds([[$userLat, $userLng], [$branchLat, $branchLng]]);
        map.fitBounds(bounds, { padding: [50, 50], maxZoom: 15 });
        """.trimIndent()
    } else {
        """
        map.setView([$branchLat, $branchLng], 15);
        """.trimIndent()
    }

    return """
    <!DOCTYPE html>
    <html lang="${if (isArabic) "ar" else "en"}" dir="${if (isArabic) "rtl" else "ltr"}">
    <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
        <style>
            * { margin: 0; padding: 0; box-sizing: border-box; }
            html, body, #map { width: 100%; height: 100%; background: #FAF7F2; overflow: hidden; }
            
            /* Custom Teal Branch Pin */
            .branch-pin {
                width: 36px;
                height: 36px;
                background: #0E7490;
                border-radius: 50% 50% 50% 0;
                transform: rotate(-45deg);
                display: flex;
                align-items: center;
                justify-content: center;
                border: 2.5px solid #FFFFFF;
                box-shadow: 0 4px 12px rgba(14, 116, 144, 0.45);
            }
            .pin-icon {
                transform: rotate(45deg);
                color: #FFFFFF;
                font-size: 15px;
                font-weight: bold;
                font-family: sans-serif;
            }

            /* Pulsing User Location Marker */
            .user-pulse {
                width: 22px;
                height: 22px;
                border-radius: 50%;
                background: rgba(2, 132, 199, 0.25);
                display: flex;
                align-items: center;
                justify-content: center;
                animation: pulse 2s infinite;
            }
            .user-core {
                width: 12px;
                height: 12px;
                border-radius: 50%;
                background: #0284C7;
                border: 2px solid #FFFFFF;
                box-shadow: 0 2px 6px rgba(0,0,0,0.25);
            }
            @keyframes pulse {
                0% { box-shadow: 0 0 0 0 rgba(2, 132, 199, 0.5); }
                70% { box-shadow: 0 0 0 10px rgba(2, 132, 199, 0); }
                100% { box-shadow: 0 0 0 0 rgba(2, 132, 199, 0); }
            }

            /* Leaflet popup styling */
            .leaflet-popup-content-wrapper {
                border-radius: 14px;
                box-shadow: 0 6px 16px rgba(0,0,0,0.12);
                font-family: system-ui, -apple-system, sans-serif;
                font-size: 13px;
                padding: 4px;
            }
        </style>
    </head>
    <body>
        <div id="map"></div>
        <script>
            var map = L.map('map', {
                zoomControl: false,
                attributionControl: false
            });

            // Free OpenStreetMap tiles - zero watermarks, zero API key required
            L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19
            }).addTo(map);

            // Branch Marker
            var branchIcon = L.divIcon({
                className: '',
                html: '<div class="branch-pin"><div class="pin-icon">📍</div></div>',
                iconSize: [36, 36],
                iconAnchor: [18, 36],
                popupAnchor: [0, -34]
            });

            var branchMarker = L.marker([$branchLat, $branchLng], { icon: branchIcon }).addTo(map);
            branchMarker.bindPopup("<b>$branchTitle</b>").openPopup();

            $userMarkerScript
        </script>
    </body>
    </html>
    """.trimIndent()
}
