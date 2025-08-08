package com.example.invoicingsystem.model

enum class TaxIdType {
    RIF,
    CEDULA,
    PASAPORTE
}

data class Party(
    val name: String,
    val fiscalAddress: String,
    val email: String,
    val taxId: String,
    val taxIdType: TaxIdType
)
