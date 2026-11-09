package com.taboor.features.home.domain.model

enum class TicketStatus {
    COMPLETED,
    CANCELLED
}

data class TicketHistoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val ticketNumber: String,
    val serviceName: String,
    val serviceNameAr: String,
    val branchLabel: String,
    val branchLabelAr: String,
    val dateString: String,
    val status: TicketStatus = TicketStatus.COMPLETED,
    val durationMinutes: Int = 22
) {
    fun localizedServiceName(isAr: Boolean): String = if (isAr) serviceNameAr else serviceName
    fun localizedBranch(isAr: Boolean): String = if (isAr) branchLabelAr else branchLabel
}
