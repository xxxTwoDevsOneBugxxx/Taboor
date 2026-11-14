package com.taboor.features.home.domain.service

import com.taboor.core.services.LocationService
import com.taboor.features.home.domain.model.ServiceBranch
import com.taboor.features.home.domain.model.ServiceCategory
import com.taboor.features.home.domain.model.ServicePlace

data class ServiceCoordinates(val latitude: Double, val longitude: Double)

/** Shared location and branch-selection rules matching the iOS service discovery flow. */
object ServiceDiscovery {
    const val LOCAL_AREA_RADIUS_KM = 45.0

    fun distanceToBranch(branch: ServiceBranch, coordinate: ServiceCoordinates): Double? {
        val latitude = branch.lat ?: return null
        val longitude = branch.lng ?: return null
        return LocationService.distanceKm(coordinate.latitude, coordinate.longitude, latitude, longitude)
    }

    fun nearestBranch(place: ServicePlace, coordinate: ServiceCoordinates): ServiceBranch? =
        place.branches.minByOrNull { distanceToBranch(it, coordinate) ?: Double.MAX_VALUE }
            ?: place.branches.firstOrNull()

    fun localBranches(place: ServicePlace, coordinate: ServiceCoordinates): List<ServiceBranch> =
        place.branches.filter { branch ->
            (distanceToBranch(branch, coordinate) ?: Double.MAX_VALUE) <= LOCAL_AREA_RADIUS_KM
        }

    fun isAvailableInLocalArea(place: ServicePlace, coordinate: ServiceCoordinates): Boolean =
        (nearestBranch(place, coordinate)?.let { distanceToBranch(it, coordinate) } ?: Double.MAX_VALUE) <= LOCAL_AREA_RADIUS_KM

    /** Home sections fall back to the full catalog if the selected area has no services. */
    fun localPlacesOrAll(
        places: List<ServicePlace>,
        coordinate: ServiceCoordinates
    ): List<ServicePlace> {
        val local = places.filter { isAvailableInLocalArea(it, coordinate) }
        return local.ifEmpty { places }
    }

    /** All Services starts local, but an explicit search can discover nationwide matches. */
    fun filterPlaces(
        places: List<ServicePlace>,
        query: String,
        category: ServiceCategory?,
        coordinate: ServiceCoordinates,
        nationwide: Boolean
    ): List<ServicePlace> {
        fun matches(place: ServicePlace): Boolean =
            (category == null || place.category == category) && place.matchesQuery(query)

        if (nationwide) return places.filter(::matches)

        val localMatches = places.filter { matches(it) && isAvailableInLocalArea(it, coordinate) }
        return if (localMatches.isEmpty() && query.isNotBlank()) {
            places.filter(::matches)
        } else {
            localMatches
        }
    }
}
