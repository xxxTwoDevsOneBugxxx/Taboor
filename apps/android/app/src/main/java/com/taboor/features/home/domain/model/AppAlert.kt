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
    val titleAr: String = "",
    val titleEn: String = "",
    val descAr: String = "",
    val descEn: String = "",
    val timeAgoAr: String = "",
    val timeAgoEn: String = "",
    val read: Boolean = false,
    val service: String = "",
    val number: String = "",
    val timestampMillis: Long = System.currentTimeMillis()
) {
    val timestamp: Date
        get() = Date(timestampMillis)

    fun localizedTitle(isArabic: Boolean): String {
        return if (titleAr.isNotBlank() || titleEn.isNotBlank()) {
            if (isArabic) titleAr else titleEn
        } else {
            service
        }
    }

    fun localizedDesc(isArabic: Boolean): String {
        return if (descAr.isNotBlank() || descEn.isNotBlank()) {
            if (isArabic) descAr else descEn
        } else {
            ""
        }
    }

    fun localizedTimeAgo(isArabic: Boolean): String {
        return if (timeAgoAr.isNotBlank() || timeAgoEn.isNotBlank()) {
            if (isArabic) timeAgoAr else timeAgoEn
        } else {
            val diff = (System.currentTimeMillis() - timestampMillis).coerceAtLeast(0)
            val min = (diff / (1000 * 60)).toInt()
            val hours = min / 60
            when {
                min < 1 -> if (isArabic) "الآن" else "Now"
                min < 60 -> if (isArabic) "منذ $min د" else "${min}m ago"
                hours < 24 -> if (isArabic) "منذ $hours س" else "${hours}h ago"
                else -> if (isArabic) "منذ ${hours / 24} ي" else "${hours / 24}d ago"
            }
        }
    }
}
