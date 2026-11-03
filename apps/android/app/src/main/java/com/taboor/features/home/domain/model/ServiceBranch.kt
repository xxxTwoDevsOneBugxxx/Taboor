package com.taboor.features.home.domain.model

data class ServiceBranch(
    val label: String,
    val labelAr: String? = null,
    val distanceKm: String? = null,
    val waiting: Int = 0,
    val isOpen: Boolean = true,
    val address: String? = null,
    val addressAr: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
) {
    val waitingCount: Int
        get() = waiting

    val estWaitMinutes: Int
        get() = (waiting * 3.5).toInt().coerceIn(2, 60)

    fun localizedLabel(isAr: Boolean): String {
        return if (isAr && labelAr != null) labelAr else label
    }

    fun localizedAddress(isAr: Boolean): String? {
        return if (isAr && addressAr != null) addressAr else address
    }
}
