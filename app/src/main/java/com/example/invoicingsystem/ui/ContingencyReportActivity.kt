package com.example.invoicingsystem.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.invoicingsystem.data.XmlPersistenceManager
import com.example.invoicingsystem.databinding.ActivityContingencyReportBinding

class ContingencyReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContingencyReportBinding
    private lateinit var persistenceManager: XmlPersistenceManager
    private lateinit var invoiceAdapter: InvoiceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContingencyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        persistenceManager = XmlPersistenceManager()
        setupRecyclerView()
        loadContingencyInvoices()
    }

    private fun setupRecyclerView() {
        invoiceAdapter = InvoiceAdapter(emptyList())
        binding.reportRecyclerView.apply {
            adapter = invoiceAdapter
            layoutManager = LinearLayoutManager(this@ContingencyReportActivity)
        }
    }

    private fun loadContingencyInvoices() {
        val allInvoices = persistenceManager.loadAllInvoices(this)
        val contingencyInvoices = allInvoices.filter { it.isContingency }
        invoiceAdapter.updateInvoices(contingencyInvoices)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
