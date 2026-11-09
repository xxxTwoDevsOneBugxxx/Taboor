package com.taboor.features.home.domain.model

data class SmartAlertPreferences(
    val customersAheadNotice: Int = 2,
    val minutesBeforeNotice: Int = 10,
    val smartDepartureNotice: Boolean = true,
    val timeSensitiveHaptics: Boolean = true,
    val lockScreenAlert: Boolean = true
)
