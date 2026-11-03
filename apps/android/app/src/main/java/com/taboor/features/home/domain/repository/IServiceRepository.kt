package com.taboor.features.home.domain.repository

import com.taboor.features.home.domain.model.AppAlert
import com.taboor.features.home.domain.model.ServiceBranch
import com.taboor.features.home.domain.model.ServiceCategory
import com.taboor.features.home.domain.model.ServicePlace
import com.taboor.features.home.domain.model.UserTicket
import kotlinx.coroutines.flow.StateFlow

interface IServiceRepository {
    val activeTicket: StateFlow<UserTicket?>
    val alerts: StateFlow<List<AppAlert>>
    val places: List<ServicePlace>

    fun joinQueue(place: ServicePlace, branch: ServiceBranch): UserTicket
    fun delayActiveTicket(minutes: Int = 10)
    fun cancelActiveTicket()
    fun dismissAlert(alertId: String)
    fun getPlaceById(id: String): ServicePlace?
    fun filterPlaces(query: String, category: ServiceCategory?): List<ServicePlace>
}
