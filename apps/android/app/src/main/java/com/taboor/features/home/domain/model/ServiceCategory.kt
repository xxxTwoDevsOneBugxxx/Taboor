package com.taboor.features.home.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CarRepair
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.ui.graphics.vector.ImageVector

enum class ServiceCategory {
    CLINICS,
    SALONS,
    GARAGES,
    OFFICES
}

val ServiceCategory.icon: ImageVector
    get() = when (this) {
        ServiceCategory.CLINICS -> Icons.Rounded.MedicalServices
        ServiceCategory.SALONS -> Icons.Rounded.ContentCut
        ServiceCategory.GARAGES -> Icons.Rounded.CarRepair
        ServiceCategory.OFFICES -> Icons.Rounded.Business
    }
