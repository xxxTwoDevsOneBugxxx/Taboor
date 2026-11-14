package com.taboor.features.home.data.repository

import android.content.Context
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CarRepair
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.MedicalServices
import com.taboor.core.theme.AppColors
import com.taboor.features.home.domain.model.*
import org.json.JSONArray
import org.json.JSONObject

internal data class ServiceSeedData(
    val places: List<ServicePlace>,
    val tickets: List<UserTicket>,
    val alerts: List<AppAlert>,
    val history: List<TicketHistoryItem>
)

internal object ServiceSeedDataLoader {
    private const val TAG = "ServiceSeedDataLoader"

    fun load(context: Context): ServiceSeedData? = try {
        decode(
            readAsset(context, "places_seed.json"),
            readAsset(context, "active_tickets_seed.json"),
            readAsset(context, "alerts_seed.json"),
            readAsset(context, "history_seed.json")
        )
    } catch (error: Exception) {
        Log.e(TAG, "Could not load iOS seed files", error)
        null
    }

    fun decode(placesPayload: String, ticketsPayload: String, alertsPayload: String, historyPayload: String): ServiceSeedData {
        val places = parseArray(placesPayload).map(::toPlace)
        val tickets = parseArray(ticketsPayload).map { toTicket(it, places) }
        val alerts = parseArray(alertsPayload).map(::toAlert)
        val history = parseArray(historyPayload).map(::toHistory)
        return ServiceSeedData(places, tickets, alerts, history)
    }

    private fun readAsset(context: Context, filename: String): String =
        context.assets.open("seeds/$filename").bufferedReader().use { it.readText() }

    private fun parseArray(payload: String): List<JSONObject> {
        val trimmed = payload.trim()
        val array = if (trimmed.startsWith("[")) JSONArray(trimmed) else {
            val root = JSONObject(trimmed)
            root.optJSONArray("data") ?: root.optJSONArray("result") ?: JSONArray()
        }
        return (0 until array.length()).map { array.getJSONObject(it) }
    }

    private fun toPlace(json: JSONObject): ServicePlace {
        val category = category(json.optString("category"))
        val branchesJson = json.optJSONArray("branches") ?: JSONArray()
        val branches = (0 until branchesJson.length()).map { index ->
            val branch = branchesJson.getJSONObject(index)
            val countersJson = branch.optJSONArray("counters") ?: JSONArray()
            val counters = (0 until countersJson.length()).map { counterIndex ->
                val counter = countersJson.getJSONObject(counterIndex)
                ServiceCounter(
                    id = counter.string("id"),
                    number = counter.optInt("number"),
                    name = counter.string("nameEn"),
                    nameAr = counter.string("nameAr"),
                    servingTicket = counter.stringOrNull("servingTicket"),
                    isOpen = counter.optBoolean("isOpen", true)
                )
            }
            ServiceBranch(
                id = branch.string("id"),
                label = branch.string("nameEn"),
                labelAr = branch.string("nameAr"),
                distanceKm = "4.5",
                waiting = counters.count { it.isOpen },
                isOpen = counters.any { it.isOpen },
                address = branch.string("addressEn"),
                addressAr = branch.string("addressAr"),
                lat = branch.optDouble("latitude").takeIf { it.isFinite() },
                lng = branch.optDouble("longitude").takeIf { it.isFinite() },
                phone = branch.stringOrNull("phone"),
                openingHours = branch.stringOrNull("openingHours"),
                counters = counters
            )
        }
        return ServicePlace(
            id = json.string("id"),
            name = json.string("nameEn"),
            nameAr = json.string("nameAr"),
            icon = categoryIcon(category),
            color = category.color,
            category = category,
            branches = branches,
            about = "Smart queue enabled branch with live tracking and instant check-in.",
            aboutAr = "فرع معتمد بنظام طابور الذكي مع تحديثات فورية وحجز سريع.",
            workingHours = branches.firstNotNullOfOrNull { it.openingHours } ?: "09:00 AM – 10:00 PM",
            starRating = json.optDouble("rating", 4.7),
            reviewCount = json.optInt("reviewCount", 128),
            imageUrl = json.stringOrNull("imageUrl"),
            isAvailable = json.optBoolean("isOpen", true),
            activeQueuesCount = json.optInt("activeQueuesCount"),
            estimatedWaitMinutes = json.optInt("estimatedWaitMinutes"),
            keywords = listOf(json.string("nameEn").lowercase(), json.string("nameAr"))
        )
    }

    private fun toTicket(json: JSONObject, places: List<ServicePlace>): UserTicket {
        val lat = json.optDouble("latitude").takeIf { it.isFinite() }
        val lng = json.optDouble("longitude").takeIf { it.isFinite() }
        val placeId = json.stringOrNull("placeId")
        val matchingPlace = places.firstOrNull { it.id == placeId }
        val branch = matchingPlace?.branches?.minByOrNull { candidate ->
            val latDelta = (candidate.lat ?: 0.0) - (lat ?: 0.0)
            val lngDelta = (candidate.lng ?: 0.0) - (lng ?: 0.0)
            latDelta * latDelta + lngDelta * lngDelta
        }
        return UserTicket(
            id = json.string("id"),
            ticketNumber = json.string("ticketNumber"),
            serviceName = json.string("serviceName"),
            serviceNameAr = json.string("serviceNameAr"),
            branchLabel = json.string("branchLabel"),
            branchLabelAr = json.string("branchLabelAr"),
            counterLabel = json.string("counterLabel"),
            counterLabelAr = json.string("counterLabelAr"),
            waitingAhead = json.optInt("waitingAhead"),
            estMinutes = json.optInt("estMinutes"),
            currentServing = json.string("currentServing"),
            progress = json.optDouble("progress").toFloat(),
            isActive = json.optBoolean("isActive", true),
            bookingType = if (json.string("bookingType") == "scheduled") TicketBookingType.SCHEDULED else TicketBookingType.LIVE_QUEUE,
            scheduledDate = json.stringOrNull("scheduledDate"),
            scheduledTime = json.stringOrNull("scheduledTimeSlot"),
            placeId = placeId,
            branchId = branch?.id ?: "",
            latitude = lat,
            longitude = lng
        )
    }

    private fun toAlert(json: JSONObject): AppAlert {
        val type = when (json.string("type").lowercase()) {
            "join" -> AlertType.JOIN
            "open" -> AlertType.OPEN
            "go" -> AlertType.GO
            "thanks" -> AlertType.THANKS
            else -> AlertType.TURN
        }
        val timeAgo = json.string("timeAgoEn").lowercase()
        val minutes = when {
            timeAgo.endsWith("m ago") -> timeAgo.substringBefore(' ').toLongOrNull()?.times(60_000L)
            timeAgo.endsWith("h ago") -> timeAgo.substringBefore(' ').toLongOrNull()?.times(3_600_000L)
            timeAgo.endsWith("d ago") -> timeAgo.substringBefore(' ').toLongOrNull()?.times(86_400_000L)
            else -> null
        }
        return AppAlert(
            id = json.string("id"),
            type = type,
            titleAr = json.string("titleAr"),
            titleEn = json.string("titleEn"),
            descAr = json.string("descAr"),
            descEn = json.string("descEn"),
            timeAgoAr = json.string("timeAgoAr"),
            timeAgoEn = json.string("timeAgoEn"),
            read = json.optBoolean("isRead"),
            timestampMillis = System.currentTimeMillis() - (minutes ?: 0L)
        )
    }

    private fun toHistory(json: JSONObject): TicketHistoryItem = TicketHistoryItem(
        id = json.string("id"),
        ticketNumber = json.string("ticketNumber"),
        serviceName = json.string("placeNameEn"),
        serviceNameAr = json.string("placeNameAr"),
        branchLabel = json.string("branchNameEn"),
        branchLabelAr = json.string("branchNameAr"),
        dateString = json.string("dateStringAr"),
        status = if (json.string("status").equals("cancelled", true)) TicketStatus.CANCELLED else TicketStatus.COMPLETED,
        durationMinutes = json.optInt("durationMinutes")
    )

    private fun category(value: String): ServiceCategory = when (value.lowercase()) {
        "salons" -> ServiceCategory.SALONS
        "garages" -> ServiceCategory.GARAGES
        "offices" -> ServiceCategory.OFFICES
        else -> ServiceCategory.CLINICS
    }

    private fun categoryIcon(category: ServiceCategory) = when (category) {
        ServiceCategory.CLINICS -> Icons.Rounded.MedicalServices
        ServiceCategory.SALONS -> Icons.Rounded.ContentCut
        ServiceCategory.GARAGES -> Icons.Rounded.CarRepair
        ServiceCategory.OFFICES -> Icons.Rounded.Business
    }

    private fun JSONObject.string(key: String): String = optString(key).takeUnless { it == "null" } ?: ""
    private fun JSONObject.stringOrNull(key: String): String? = string(key).takeIf { it.isNotBlank() }
}
