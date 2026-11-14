package com.taboor.core.services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

data class RouteResult(
    val points: List<Pair<Double, Double>>,
    val distanceKm: Double,
    val durationMinutes: Int,
    val firstStepDistanceMeters: Double? = null,
    val instructionEn: String? = null,
    val instructionAr: String? = null
)

/**
 * Android counterpart to Flutter's RoutingService.
 * Calls OSRM (Open Source Routing Machine) to get turn-by-turn geometry points and travel duration.
 */
object RoutingService {

    private const val TAG = "RoutingService"
    private const val CACHE_TTL_MILLIS = 120_000L
    private val routeCache = ConcurrentHashMap<String, CachedRoute>()

    suspend fun getRoute(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): RouteResult? = withContext(Dispatchers.IO) {
        val cacheKey = String.format(Locale.US, "%.4f,%.4f>%.4f,%.4f", startLat, startLng, endLat, endLng)
        routeCache[cacheKey]?.takeIf { System.currentTimeMillis() - it.createdAt < CACHE_TTL_MILLIS }?.let {
            return@withContext it.result
        }
        var conn: HttpURLConnection? = null
        try {
            val urlStr = "https://router.project-osrm.org/route/v1/driving/" +
                    "$startLng,$startLat;$endLng,$endLat?overview=full&geometries=geojson&steps=true"

            val url = URL(urlStr)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("User-Agent", "TaboorApp-Android/1.0")
            }

            if (conn.responseCode in 200..299) {
                val text = BufferedReader(InputStreamReader(conn.inputStream, StandardCharsets.UTF_8)).use {
                    it.readText()
                }
                val json = JSONObject(text)
                val routes = json.optJSONArray("routes")
                if (routes != null && routes.length() > 0) {
                    val firstRoute = routes.getJSONObject(0)
                    val distanceMeters = firstRoute.optDouble("distance", 0.0)
                    val durationSeconds = firstRoute.optDouble("duration", 0.0)

                    val geometry = firstRoute.optJSONObject("geometry")
                    val coords = geometry?.optJSONArray("coordinates")
                    val pointsList = mutableListOf<Pair<Double, Double>>()
                    var firstStepDistance: Double? = null
                    var instructionEn: String? = null
                    var instructionAr: String? = null

                    val legs = firstRoute.optJSONArray("legs")
                    val steps = legs?.optJSONObject(0)?.optJSONArray("steps")
                    val firstStep = steps?.let { stepArray ->
                        (0 until stepArray.length())
                            .map { stepArray.getJSONObject(it) }
                            .firstOrNull { step ->
                                val maneuver = step.optJSONObject("maneuver")
                                maneuver?.optString("type") != "arrive"
                            }
                    }
                    if (firstStep != null) {
                        firstStepDistance = firstStep.optDouble("distance").takeIf { it.isFinite() }
                        val maneuver = firstStep.optJSONObject("maneuver")
                        val type = maneuver?.optString("type").orEmpty()
                        val modifier = maneuver?.optString("modifier").orEmpty()
                        val road = firstStep.optString("name").takeIf { it.isNotBlank() }
                            ?: firstStep.optString("ref").takeIf { it.isNotBlank() }
                        val en = maneuverInstruction(type, modifier, false)
                        val ar = maneuverInstruction(type, modifier, true)
                        instructionEn = if (road.isNullOrBlank()) en else "$en onto $road"
                        instructionAr = if (road.isNullOrBlank()) ar else "$ar إلى $road"
                    }

                    if (coords != null) {
                        for (i in 0 until coords.length()) {
                            val coordArr = coords.getJSONArray(i)
                            val lng = coordArr.getDouble(0)
                            val lat = coordArr.getDouble(1)
                            pointsList.add(Pair(lat, lng))
                        }
                    }

                    val result = RouteResult(
                        points = pointsList,
                        distanceKm = distanceMeters / 1000.0,
                        durationMinutes = (durationSeconds / 60.0).roundToInt().coerceAtLeast(1),
                        firstStepDistanceMeters = firstStepDistance,
                        instructionEn = instructionEn,
                        instructionAr = instructionAr
                    )
                    routeCache[cacheKey] = CachedRoute(result, System.currentTimeMillis())
                    return@withContext result
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "OSRM routing failed: ${e.message}")
        } finally {
            conn?.disconnect()
        }

        // Fallback: direct line calculation
        val distance = LocationService.distanceKm(startLat, startLng, endLat, endLng)
        val minutes = LocationService.estimateDriveTime(distance)
        val fallback = RouteResult(
            points = listOf(Pair(startLat, startLng), Pair(endLat, endLng)),
            distanceKm = distance,
            durationMinutes = minutes
        )
        routeCache[cacheKey] = CachedRoute(fallback, System.currentTimeMillis())
        fallback
    }

    private fun maneuverInstruction(type: String, modifier: String, isArabic: Boolean): String {
        val direction = when (modifier) {
            "left", "slight left", "sharp left" -> if (isArabic) "انعطف يسارًا" else "Turn left"
            "right", "slight right", "sharp right" -> if (isArabic) "انعطف يمينًا" else "Turn right"
            "uturn" -> if (isArabic) "انعطف للخلف" else "Make a U-turn"
            else -> if (isArabic) "استمر للأمام" else "Continue straight"
        }
        return when (type) {
            "roundabout", "rotary", "roundabout turn" -> if (isArabic) "ادخل الدوار" else "Enter the roundabout"
            "merge" -> if (isArabic) "اندمج مع الطريق" else "Merge"
            "fork" -> if (isArabic) "اسلك المخرج" else "Take the fork"
            "on ramp" -> if (isArabic) "اسلك مدخل الطريق السريع" else "Take the ramp"
            "off ramp" -> if (isArabic) "اسلك مخرج الطريق السريع" else "Take the exit"
            "depart" -> if (isArabic) "ابدأ التحرك" else "Head out"
            else -> direction
        }
    }

    private data class CachedRoute(val result: RouteResult, val createdAt: Long)
}
