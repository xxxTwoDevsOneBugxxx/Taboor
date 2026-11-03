package com.taboor.features.home.domain.model

import java.util.Date

enum class AlertType {
    JOIN,
    OPEN,
    TURN,
    GO,
    THANKS
}

data class AppAlert(
    val id: String,
    val type: AlertType,
    val service: String,
    val number: String,
    val timestampMillis: Long,
    val read: Boolean = false
) {
    val timestamp: Date
        get() = Date(timestampMillis)

    val timeAgo: String
        get() {
            val diff = (System.currentTimeMillis() - timestampMillis).coerceAtLeast(0)
            val min = (diff / (1000 * 60)).toInt()
            val hours = min / 60
            return when {
                min < 1 -> "الآن"
                min < 60 -> "منذ $min د"
                hours < 24 -> "منذ $hours س"
                else -> "منذ ${hours / 24} ي"
            }
        }
}
