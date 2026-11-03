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
import kotlin.math.roundToInt

data class RouteResult(
    val points: List<Pair<Double, Double>>,
    val distanceKm: Double,
    val durationMinutes: Int
)

/**
 * Android counterpart to Flutter's RoutingService.
 * Calls OSRM (Open Source Routing Machine) to get turn-by-turn geometry points and travel duration.
 */
object RoutingService {

    private const val TAG = "RoutingService"

    suspend fun getRoute(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): RouteResult? = withContext(Dispatchers.IO) {
        var conn: HttpURLConnection? = null
        try {
            val urlStr = "https://router.project-osrm.org/route/v1/driving/" +
                    "$startLng,$startLat;$endLng,$endLat?overview=full&geometries=geojson"

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

                    if (coords != null) {
                        for (i in 0 until coords.length()) {
                            val coordArr = coords.getJSONArray(i)
                            val lng = coordArr.getDouble(0)
                            val lat = coordArr.getDouble(1)
                            pointsList.add(Pair(lat, lng))
                        }
                    }

                    return@withContext RouteResult(
                        points = pointsList,
                        distanceKm = distanceMeters / 1000.0,
                        durationMinutes = (durationSeconds / 60.0).roundToInt().coerceAtLeast(1)
                    )
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
        RouteResult(
            points = listOf(Pair(startLat, startLng), Pair(endLat, endLng)),
            distanceKm = distance,
            durationMinutes = minutes
        )
    }
}
