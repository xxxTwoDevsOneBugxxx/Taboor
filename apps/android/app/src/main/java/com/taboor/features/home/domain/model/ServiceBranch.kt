package com.taboor.features.home.domain.model

data class ServiceBranch(
    val id: String = "",
    val label: String,
    val labelAr: String? = null,
    val distanceKm: String? = null,
    val waiting: Int = 0,
    val isOpen: Boolean = true,
    val address: String? = null,
    val addressAr: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val phone: String? = null,
    val openingHours: String? = null,
    val counters: List<ServiceCounter> = emptyList()
) {
    val waitingCount: Int
        get() = waiting

    val estWaitMinutes: Int
        get() = (waiting * 4.5).toInt().coerceAtLeast(3)

    fun localizedLabel(isAr: Boolean): String {
        return if (isAr && labelAr != null) labelAr else label
    }

    fun localizedAddress(isAr: Boolean): String? {
        return if (isAr && addressAr != null) addressAr else address
    }
}

data class ServiceCounter(
    val id: String,
    val number: Int,
    val name: String,
    val nameAr: String,
    val servingTicket: String? = null,
    val isOpen: Boolean = true
)
