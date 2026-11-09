package com.taboor.features.home.domain.model

enum class TicketBookingType {
    LIVE_QUEUE,
    SCHEDULED
}

data class UserTicket(
    val id: String = java.util.UUID.randomUUID().toString(),
    val ticketNumber: String = "A-12",
    val serviceName: String = "Dr. Sara Dental Clinic",
    val serviceNameAr: String = "عيادة د. سارة لطب وجراحة الأسنان",
    val branchLabel: String = "Main Branch - Nasr City",
    val branchLabelAr: String = "الفرع الرئيسي - مدينة نصر",
    val counterLabel: String = "Room 3 — 2nd Floor",
    val counterLabelAr: String = "عيادة 3 — الدور الثاني",
    val waitingAhead: Int = 2,
    val estMinutes: Int = 10,
    val currentServing: String = "A-07",
    val progress: Float = 0.68f,
    val isActive: Boolean = true,
    val bookingType: TicketBookingType = TicketBookingType.LIVE_QUEUE,
    val scheduledDate: String? = null,
    val scheduledTime: String? = null,
    val placeId: String? = "place_1",
    val branchId: String = "branch_1_1",
    val latitude: Double? = 30.0585,
    val longitude: Double? = 31.3466
) {
    fun localizedServiceName(isAr: Boolean): String = if (isAr) serviceNameAr else serviceName
    fun localizedBranch(isAr: Boolean): String = if (isAr) branchLabelAr else branchLabel
    fun localizedCounter(isAr: Boolean): String = if (isAr) counterLabelAr else counterLabel
}
