package com.example.invoicingsystem.model

import java.math.BigDecimal

data class InvoiceItem(
    val description: String,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal
) {
    fun getTotal(): BigDecimal {
        return quantity.multiply(unitPrice)
    }
}
