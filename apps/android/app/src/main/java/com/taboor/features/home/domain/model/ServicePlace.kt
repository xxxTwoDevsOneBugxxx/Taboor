package com.taboor.features.home.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class ServicePlace(
    val id: String,
    val name: String,
    val nameAr: String? = null,
    val icon: ImageVector,
    val color: Color,
    val category: ServiceCategory,
    val branches: List<ServiceBranch>,
    val about: String? = null,
    val aboutAr: String? = null,
    val workingHours: String? = null,
    val starRating: Double = 4.5,
    val keywords: List<String> = emptyList()
) {
    val totalWaiting: Int
        get() = branches.sumOf { it.waiting }

    fun localizedName(isAr: Boolean): String {
        return if (isAr && nameAr != null) nameAr else name
    }

    fun localizedAbout(isAr: Boolean): String? {
        return if (isAr && aboutAr != null) aboutAr else about
    }

    fun matchesQuery(query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        if (name.lowercase().contains(q)) return true
        if (nameAr != null && nameAr.lowercase().contains(q)) return true
        if (about != null && about.lowercase().contains(q)) return true
        if (aboutAr != null && aboutAr.lowercase().contains(q)) return true
        if (keywords.any { it.lowercase().contains(q) }) return true
        if (branches.any { b ->
                b.label.lowercase().contains(q) ||
                (b.labelAr != null && b.labelAr.lowercase().contains(q)) ||
                (b.address != null && b.address.lowercase().contains(q)) ||
                (b.addressAr != null && b.addressAr.lowercase().contains(q))
            }) {
            return true
        }
        return false
    }
}
