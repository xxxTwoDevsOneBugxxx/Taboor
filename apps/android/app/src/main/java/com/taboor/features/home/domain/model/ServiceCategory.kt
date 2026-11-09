package com.taboor.features.home.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CarRepair
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.taboor.core.theme.AppColors

enum class ServiceCategory {
    CLINICS,
    SALONS,
    GARAGES,
    OFFICES;

    fun localizedTitle(isAr: Boolean): String = when (this) {
        CLINICS -> if (isAr) "عيادات ومستشفيات" else "Clinics"
        SALONS -> if (isAr) "صالونات وتجميل" else "Salons"
        GARAGES -> if (isAr) "ورش وصيانة" else "Auto Repair"
        OFFICES -> if (isAr) "مكاتب وخدمات" else "Offices"
    }

    val color: Color
        get() = when (this) {
            CLINICS -> AppColors.Teal
            SALONS -> AppColors.Indigo
            GARAGES -> AppColors.Amber
            OFFICES -> AppColors.DeepTeal
        }
}

val ServiceCategory.icon: ImageVector
    get() = when (this) {
        ServiceCategory.CLINICS -> Icons.Rounded.MedicalServices
        ServiceCategory.SALONS -> Icons.Rounded.ContentCut
        ServiceCategory.GARAGES -> Icons.Rounded.CarRepair
        ServiceCategory.OFFICES -> Icons.Rounded.Business
    }
