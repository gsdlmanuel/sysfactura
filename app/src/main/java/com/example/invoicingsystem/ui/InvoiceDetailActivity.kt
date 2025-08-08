package com.example.invoicingsystem.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.invoicingsystem.R
import com.example.invoicingsystem.data.XmlPersistenceManager
import com.example.invoicingsystem.databinding.ActivityInvoiceDetailBinding
import com.example.invoicingsystem.model.Invoice
import com.example.invoicingsystem.model.InvoiceItem
import java.text.NumberFormat
import java.util.Locale

class InvoiceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInvoiceDetailBinding
    private lateinit var persistenceManager: XmlPersistenceManager
    private var currentInvoice: Invoice? = null

    companion object {
        const val EXTRA_INVOICE_ID = "extra_invoice_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvoiceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        persistenceManager = XmlPersistenceManager()

        val invoiceId = intent.getStringExtra(EXTRA_INVOICE_ID)
        if (invoiceId == null) {
            Toast.makeText(this, "Error: No se encontró el ID de la factura.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        currentInvoice = persistenceManager.loadInvoice(this, invoiceId)
        if (currentInvoice == null) {
            Toast.makeText(this, "Error: No se pudo cargar la factura.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        populateUi(currentInvoice!!)

        binding.sendEmailButton.setOnClickListener {
            sendEmail(currentInvoice!!)
        }
    }

    private fun populateUi(invoice: Invoice) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "VE"))

        binding.invoiceIdTextView.text = "Factura #: ${invoice.id.substring(0, 8)}..."
        binding.dateTimeTextView.text = "Emitida el ${formatDisplayDate(invoice.issueDate)} a las ${invoice.issueTime}"

        // Issuer
        binding.issuerNameTextView.text = invoice.issuer.name
        binding.issuerRifTextView.text = "${invoice.issuer.taxIdType.name}: ${invoice.issuer.taxId}"
        binding.issuerAddressTextView.text = invoice.issuer.fiscalAddress

        // Receiver
        binding.receiverNameTextView.text = invoice.receiver.name
        binding.receiverTaxIdTextView.text = "${invoice.receiver.taxIdType.name}: ${invoice.receiver.taxId}"
        binding.receiverAddressTextView.text = invoice.receiver.fiscalAddress
        binding.receiverEmailTextView.text = "Email: ${invoice.receiver.email}"

        // Items
        binding.itemsContainer.removeAllViews()
        invoice.items.forEach { addItemView(it, currencyFormat) }

        // Totals
        binding.baseAmountTextView.text = "Base Imponible: ${currencyFormat.format(invoice.getBaseAmount())}"
        binding.vatAmountTextView.text = "IVA (${invoice.vatRate.multiply(100.toBigDecimal())}%): ${currencyFormat.format(invoice.getVatAmount())}"
        binding.totalAmountTextView.text = "Total a Pagar: ${currencyFormat.format(invoice.getTotalAmount())}"

        // Printer
        binding.printerInfoTextView.text = "${invoice.digitalPrinterInfo.name}, RIF: ${invoice.digitalPrinterInfo.rif}, Auth: ${invoice.digitalPrinterInfo.authorizationDate}"
    }

    private fun addItemView(item: InvoiceItem, format: NumberFormat) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_invoice_detail_line, binding.itemsContainer, false)
        val description: TextView = itemView.findViewById(R.id.itemDescription)
        val details: TextView = itemView.findViewById(R.id.itemDetails)
        description.text = item.description
        details.text = "${item.quantity} x ${format.format(item.unitPrice)} = ${format.format(item.getTotal())}"
        binding.itemsContainer.addView(itemView)
    }

    private fun sendEmail(invoice: Invoice) {
        val subject = "Factura Nro: ${invoice.id.substring(0, 8)}"
        val body = buildEmailBody(invoice)

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // Only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, arrayOf(invoice.receiver.email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No se encontró una aplicación de correo.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildEmailBody(invoice: Invoice): String {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "VE"))
        val builder = StringBuilder()
        builder.append("Hola ${invoice.receiver.name},\n\n")
        builder.append("Adjuntamos los detalles de su factura:\n\n")
        builder.append("================================\n")
        builder.append("FACTURA NRO: ${invoice.id}\n")
        builder.append("FECHA: ${formatDisplayDate(invoice.issueDate)} HORA: ${invoice.issueTime}\n")
        builder.append("================================\n\n")
        builder.append("EMISOR:\n")
        builder.append("${invoice.issuer.name}\n")
        builder.append("${invoice.issuer.taxIdType.name}: ${invoice.issuer.taxId}\n\n")
        builder.append("RECEPTOR:\n")
        builder.append("${invoice.receiver.name}\n")
        builder.append("${invoice.receiver.taxIdType.name}: ${invoice.receiver.taxId}\n\n")
        builder.append("--- ARTÍCULOS ---\n")
        invoice.items.forEach {
            builder.append("- ${it.description} (${it.quantity} x ${currencyFormat.format(it.unitPrice)}) = ${currencyFormat.format(it.getTotal())}\n")
        }
        builder.append("\n--- TOTALES ---\n")
        builder.append("Base Imponible: ${currencyFormat.format(invoice.getBaseAmount())}\n")
        builder.append("IVA: ${currencyFormat.format(invoice.getVatAmount())}\n")
        builder.append("TOTAL: ${currencyFormat.format(invoice.getTotalAmount())}\n\n")
        builder.append("Gracias por su compra.\n\n")
        builder.append("---\n")
        builder.append("${invoice.issuer.name}\n")
        return builder.toString()
    }

    private fun formatDisplayDate(date: String): String {
        if (date.length == 8) {
            return "${date.substring(0, 2)}/${date.substring(2, 4)}/${date.substring(4)}"
        }
        return date
    }
}
