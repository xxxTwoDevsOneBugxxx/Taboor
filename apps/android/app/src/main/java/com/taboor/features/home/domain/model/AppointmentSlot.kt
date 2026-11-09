package com.taboor.features.home.domain.model

data class AppointmentSlot(
    val id: String = java.util.UUID.randomUUID().toString(),
    val time: String,
    val timeEn: String,
    val isBooked: Boolean = false,
    val isRecommended: Boolean = false
) {
    fun localizedTime(isAr: Boolean): String = if (isAr) time else timeEn
}
