package com.example.invoicingsystem.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.invoicingsystem.R
import com.example.invoicingsystem.model.Invoice
import java.text.NumberFormat
import java.util.Locale

class InvoiceAdapter(
    private var invoices: List<Invoice>
) : RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_invoice, parent, false)
        return InvoiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        val invoice = invoices[position]
        holder.bind(invoice)
    }

    override fun getItemCount(): Int = invoices.size

    fun updateInvoices(newInvoices: List<Invoice>) {
        invoices = newInvoices
        notifyDataSetChanged()
    }

    class InvoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val receiverNameTextView: TextView = itemView.findViewById(R.id.receiverNameTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val totalAmountTextView: TextView = itemView.findViewById(R.id.totalAmountTextView)

        fun bind(invoice: Invoice) {
            receiverNameTextView.text = invoice.receiver.name
            dateTextView.text = formatDisplayDate(invoice.issueDate)

            val format = NumberFormat.getCurrencyInstance(Locale("es", "VE")) // For Venezuelan Bolívar
            totalAmountTextView.text = format.format(invoice.getTotalAmount())

            itemView.setOnClickListener {
                val context = it.context
                val intent = Intent(context, InvoiceDetailActivity::class.java).apply {
                    putExtra(InvoiceDetailActivity.EXTRA_INVOICE_ID, invoice.id)
                }
                context.startActivity(intent)
            }
        }

        private fun formatDisplayDate(date: String): String {
            // DDMMAAAA -> DD/MM/AAAA
            if (date.length == 8) {
                return "${date.substring(0, 2)}/${date.substring(2, 4)}/${date.substring(4)}"
            }
            return date
        }
    }
}
