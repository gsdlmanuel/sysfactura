package com.example.invoicingsystem.model

import java.math.BigDecimal

data class Invoice(
    val id: String, // Unique identifier
    val contingencyInvoiceNumber: String?, // User-defined number for contingency invoices
    val isContingency: Boolean,
    val issueDate: String, // DDMMAAAA
    val issueTime: String, // HH.MM.SS
    val issuer: Party,
    val receiver: Party,
    val items: List<InvoiceItem>,
    val vatRate: BigDecimal, // e.g., 0.16 for 16%
    val isExempt: Boolean,
    val digitalPrinterInfo: DigitalPrinter
) {
    fun getBaseAmount(): BigDecimal {
        return items.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.getTotal()) }
    }

    fun getVatAmount(): BigDecimal {
        return if (isExempt) BigDecimal.ZERO else getBaseAmount().multiply(vatRate)
    }

    fun getTotalAmount(): BigDecimal {
        return getBaseAmount().add(getVatAmount())
    }
}
