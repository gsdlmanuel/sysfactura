package com.example.invoicingsystem.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.invoicingsystem.data.XmlPersistenceManager
import com.example.invoicingsystem.databinding.ActivitySettingsBinding
import com.example.invoicingsystem.model.DigitalPrinter
import com.example.invoicingsystem.model.Party
import com.example.invoicingsystem.model.TaxIdType

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var persistenceManager: XmlPersistenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        persistenceManager = XmlPersistenceManager()

        loadSettings()

        binding.saveSettingsButton.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadSettings() {
        val (issuer, printer) = persistenceManager.loadSettings(this)
        if (issuer != null && printer != null) {
            binding.issuerNameEditText.setText(issuer.name)
            binding.issuerAddressEditText.setText(issuer.fiscalAddress)
            binding.issuerEmailEditText.setText(issuer.email)
            binding.issuerRifEditText.setText(issuer.taxId)

            binding.printerNameEditText.setText(printer.name)
            binding.printerRifEditText.setText(printer.rif)
            binding.printerAuthDateEditText.setText(printer.authorizationDate)
        }
    }

    private fun saveSettings() {
        if (!validateInput()) {
            Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_LONG).show()
            return
        }

        val issuer = Party(
            name = binding.issuerNameEditText.text.toString(),
            fiscalAddress = binding.issuerAddressEditText.text.toString(),
            email = binding.issuerEmailEditText.text.toString(),
            taxId = binding.issuerRifEditText.text.toString(),
            taxIdType = TaxIdType.RIF // Issuer is always RIF
        )

        val printer = DigitalPrinter(
            name = binding.printerNameEditText.text.toString(),
            rif = binding.printerRifEditText.text.toString(),
            authorizationDate = binding.printerAuthDateEditText.text.toString()
        )

        persistenceManager.saveSettings(this, issuer, printer)
        Toast.makeText(this, "Configuración guardada exitosamente.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun validateInput(): Boolean {
        return with(binding) {
            issuerNameEditText.text.isNotBlank() &&
            issuerAddressEditText.text.isNotBlank() &&
            issuerEmailEditText.text.isNotBlank() &&
            issuerRifEditText.text.isNotBlank() &&
            printerNameEditText.text.isNotBlank() &&
            printerRifEditText.text.isNotBlank() &&
            printerAuthDateEditText.text.isNotBlank()
        }
    }
}
