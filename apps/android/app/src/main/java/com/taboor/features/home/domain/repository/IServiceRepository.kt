package com.taboor.features.home.domain.repository

import com.taboor.features.home.domain.model.*
import kotlinx.coroutines.flow.StateFlow

interface IServiceRepository {
    val activeTicket: StateFlow<UserTicket?>
    val alerts: StateFlow<List<AppAlert>>
    val places: List<ServicePlace>
    val history: StateFlow<List<TicketHistoryItem>>
    val favoritePlaceIds: StateFlow<Set<String>>
    val smartAlertPreferences: StateFlow<SmartAlertPreferences>
    val selectedLocation: StateFlow<String>
    val availableAppointmentSlots: List<AppointmentSlot>

    fun joinQueue(place: ServicePlace, branch: ServiceBranch): UserTicket
    fun bookScheduledTicket(place: ServicePlace, branch: ServiceBranch, date: String, slot: AppointmentSlot): UserTicket
    fun delayActiveTicket(minutes: Int = 10)
    fun cancelActiveTicket()
    fun dismissAlert(alertId: String)
    fun markAlertRead(alertId: String)
    fun markAllAlertsRead()
    fun clearAllAlerts()
    fun getPlaceById(id: String): ServicePlace?
    fun filterPlaces(query: String, category: ServiceCategory?): List<ServicePlace>
    fun isFavorite(placeId: String): Boolean
    fun toggleFavorite(placeId: String)
    fun updateAlertPreferences(prefs: SmartAlertPreferences)
    fun updateSelectedLocation(location: String)
}
