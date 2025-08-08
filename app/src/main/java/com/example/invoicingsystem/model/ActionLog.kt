package com.example.invoicingsystem.model

enum class ActionType {
    CREATED,
    MODIFIED,
    CANCELLED,
    VIEWED // For traceability
}

data class ActionLog(
    val timestamp: Long,
    val actionType: ActionType,
    val invoiceId: String,
    val details: String
)
