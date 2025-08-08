package com.example.invoicingsystem.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.invoicingsystem.data.XmlPersistenceManager
import com.example.invoicingsystem.databinding.ActivityInvoiceFormBinding
import com.example.invoicingsystem.model.*
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

class InvoiceFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInvoiceFormBinding
    private lateinit var persistenceManager: XmlPersistenceManager
    private var issuer: Party? = null
    private var digitalPrinter: DigitalPrinter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInvoiceFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        persistenceManager = XmlPersistenceManager()
        loadSettings()

        binding.contingencySwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.contingencyNumberLayout.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
        }

        binding.saveInvoiceButton.setOnClickListener {
            saveInvoice()
        }
    }

    private fun loadSettings() {
        val (loadedIssuer, loadedPrinter) = persistenceManager.loadSettings(this)
        if (loadedIssuer == null || loadedPrinter == null) {
            Toast.makeText(this, "Por favor, configure los datos del emisor y la imprenta en la pantalla de Configuración.", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        issuer = loadedIssuer
        digitalPrinter = loadedPrinter
    }

    private fun saveInvoice() {
        if (issuer == null || digitalPrinter == null) {
            Toast.makeText(this, "No se pueden crear facturas sin haber guardado la configuración.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (!validateInput()) {
            Toast.makeText(this, "Por favor, complete todos los campos requeridos.", Toast.LENGTH_LONG).show()
            return
        }

        // --- Create Receiver from form ---
        val receiverName = binding.receiverNameEditText.text.toString()
        val receiverAddress = binding.receiverAddressEditText.text.toString()
        val receiverEmail = binding.receiverEmailEditText.text.toString()
        val receiverTaxId = binding.receiverTaxIdEditText.text.toString()
        val taxIdType = when (binding.taxIdTypeRadioGroup.checkedRadioButtonId) {
            binding.cedulaRadioButton.id -> TaxIdType.CEDULA
            binding.passportRadioButton.id -> TaxIdType.PASAPORTE
            else -> TaxIdType.RIF
        }
        val receiver = Party(receiverName, receiverAddress, receiverEmail, receiverTaxId, taxIdType)

        // --- Create Invoice Item from form ---
        val itemDescription = binding.itemDescriptionEditText.text.toString()
        val quantity = binding.itemQuantityEditText.text.toString().toBigDecimal()
        val unitPrice = binding.itemPriceEditText.text.toString().toBigDecimal()
        val invoiceItem = InvoiceItem(itemDescription, quantity, unitPrice)

        // --- Get Date and Time ---
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH.mm.ss", Locale.getDefault())
        val issueDate = dateFormat.format(calendar.time)
        val issueTime = timeFormat.format(calendar.time)

        // --- Create Invoice ---
        val isContingency = binding.contingencySwitch.isChecked
        val contingencyNumber = if (isContingency) binding.contingencyNumberEditText.text.toString() else null

        val invoice = Invoice(
            id = UUID.randomUUID().toString(),
            contingencyInvoiceNumber = contingencyNumber,
            isContingency = isContingency,
            issueDate = issueDate,
            issueTime = issueTime,
            issuer = issuer!!,
            receiver = receiver,
            items = listOf(invoiceItem),
            vatRate = binding.vatRateEditText.text.toString().toBigDecimal().divide(BigDecimal(100)),
            isExempt = binding.exemptSwitch.isChecked,
            digitalPrinterInfo = digitalPrinter!!
        )

        // --- Persist Invoice and Log Action ---
        persistenceManager.saveInvoice(this, invoice)
        val log = ActionLog(System.currentTimeMillis(), ActionType.CREATED, invoice.id, "Factura creada para ${receiver.name}")
        persistenceManager.saveActionLog(this, log)

        Toast.makeText(this, "Factura guardada exitosamente.", Toast.LENGTH_SHORT).show()
        finish() // Go back to MainActivity
    }

    private fun validateInput(): Boolean {
        if (binding.contingencySwitch.isChecked && binding.contingencyNumberEditText.text.isNullOrBlank()) {
            binding.contingencyNumberEditText.error = "Este campo es requerido para facturas de contingencia."
            return false
        }

        return with(binding) {
            receiverNameEditText.text.toString().isNotBlank() &&
            receiverAddressEditText.text.toString().isNotBlank() &&
            receiverEmailEditText.text.toString().isNotBlank() &&
            receiverTaxIdEditText.text.toString().isNotBlank() &&
            itemDescriptionEditText.text.toString().isNotBlank() &&
            itemQuantityEditText.text.toString().isNotBlank() &&
            itemPriceEditText.text.toString().isNotBlank() &&
            vatRateEditText.text.toString().isNotBlank()
        }
    }
}
