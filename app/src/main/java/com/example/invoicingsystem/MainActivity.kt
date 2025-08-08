package com.example.invoicingsystem

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.invoicingsystem.data.XmlPersistenceManager
import com.example.invoicingsystem.databinding.ActivityMainBinding
import com.example.invoicingsystem.model.Invoice
import com.example.invoicingsystem.ui.ContingencyReportActivity
import com.example.invoicingsystem.ui.InvoiceAdapter
import com.example.invoicingsystem.ui.InvoiceFormActivity
import com.example.invoicingsystem.ui.SettingsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var invoiceAdapter: InvoiceAdapter
    private lateinit var persistenceManager: XmlPersistenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        persistenceManager = XmlPersistenceManager()
        setupRecyclerView()

        binding.fab.setOnClickListener {
            val intent = Intent(this, InvoiceFormActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadInvoices()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_contingency_report -> {
                startActivity(Intent(this, ContingencyReportActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        invoiceAdapter = InvoiceAdapter(emptyList())
        binding.invoicesRecyclerView.apply {
            adapter = invoiceAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun loadInvoices() {
        val invoices = persistenceManager.loadAllInvoices(this)
        invoiceAdapter.updateInvoices(invoices)
    }
}
