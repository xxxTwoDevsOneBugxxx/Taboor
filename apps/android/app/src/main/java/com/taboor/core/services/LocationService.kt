package com.taboor.core.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Locale
import kotlin.math.*

/**
 * Android counterpart to Flutter's LocationService.
 * Handles device coordinates, reverse geocoding to city/area, distance math, and drive time estimation.
 */
object LocationService {

    private const val TAG = "LocationService"

    // Default Cairo coordinates for fallback when location is disabled
    const val DEFAULT_LAT = 30.0444
    const val DEFAULT_LNG = 31.2357

    /**
     * Checks if location permission has been granted.
     */
    fun hasPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    /**
     * Returns the last known or current location without blocking indefinitely.
     */
    @SuppressLint("MissingPermission")
    fun getCurrentPosition(context: Context): Pair<Double, Double>? {
        if (!hasPermission(context)) return null

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        var bestLocation: Location? = null
        for (provider in providers) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null) {
                        if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                            bestLocation = loc
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Provider $provider failed: ${e.message}")
            }
        }

        return bestLocation?.let { Pair(it.latitude, it.longitude) }
    }

    /**
     * Reverse-geocodes coordinates to a clean area or city name (e.g. "مدينة نصر" or "Cairo").
     * Uses Android's native Geocoder with OpenStreetMap Nominatim graceful fallback.
     */
    suspend fun getAreaName(
        context: Context,
        lat: Double,
        lng: Double,
        isArabic: Boolean
    ): String = withContext(Dispatchers.IO) {
        val targetLocale = if (isArabic) Locale("ar", "EG") else Locale.ENGLISH

        // 1. Try Android built-in Geocoder
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, targetLocale)
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val subLocality = address.subLocality ?: address.locality ?: address.subAdminArea
                    if (!subLocality.isNullOrBlank()) {
                        return@withContext subLocality
                    }
                    val adminArea = address.adminArea
                    if (!adminArea.isNullOrBlank()) {
                        return@withContext adminArea
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Android Geocoder error: ${e.message}")
        }

        // 2. Fallback to OpenStreetMap Nominatim reverse API
        try {
            val langCode = if (isArabic) "ar" else "en"
            val url = URL(
                "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lng&format=json&accept-language=$langCode"
            )
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 4000
                readTimeout = 4000
                setRequestProperty("User-Agent", "TaboorApp-Android/1.0")
            }

            if (conn.responseCode in 200..299) {
                val text = BufferedReader(InputStreamReader(conn.inputStream, StandardCharsets.UTF_8)).use {
                    it.readText()
                }
                val json = JSONObject(text)
                val addressObj = json.optJSONObject("address")
                if (addressObj != null) {
                    val area = addressObj.optString("suburb").takeIf { it.isNotBlank() }
                        ?: addressObj.optString("city").takeIf { it.isNotBlank() }
                        ?: addressObj.optString("town").takeIf { it.isNotBlank() }
                        ?: addressObj.optString("village").takeIf { it.isNotBlank() }
                        ?: addressObj.optString("state").takeIf { it.isNotBlank() }
                    if (!area.isNullOrBlank()) {
                        return@withContext area
                    }
                }
            }
            conn.disconnect()
        } catch (_: Exception) {
            // Silently fall through
        }

        return@withContext if (isArabic) "القاهرة، مصر" else "Cairo, Egypt"
    }

    /**
     * Great-circle distance between two coordinates in kilometers.
     * Matches Flutter's Haversine formula calculation.
     */
    fun distanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Estimated city driving time in minutes (assuming 40 km/h average with traffic).
     */
    fun estimateDriveTime(km: Double): Int {
        val minutes = (km / 40.0 * 60.0).roundToInt()
        return minutes.coerceIn(1, 90)
    }
}
