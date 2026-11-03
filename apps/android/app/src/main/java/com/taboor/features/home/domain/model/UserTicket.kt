package com.taboor.features.home.domain.model

data class UserTicket(
    val ticketNumber: String = "A-12",
    val serviceName: String = "Dr. Sara Dental Clinic",
    val serviceNameAr: String = "عيادة د. سارة للأسنان",
    val branchLabel: String = "Main Branch",
    val branchLabelAr: String = "الفرع الرئيسي - مدينة نصر",
    val waitingAhead: Int = 2,
    val estMinutes: Int = 10,
    val currentServing: String = "A-07",
    val progress: Float = 0.68f,
    val isActive: Boolean = true
)
