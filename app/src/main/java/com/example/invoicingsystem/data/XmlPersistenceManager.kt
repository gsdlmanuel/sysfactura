package com.example.invoicingsystem.data

import android.content.Context
import android.util.Xml
import com.example.invoicingsystem.model.Invoice
import com.example.invoicingsystem.model.Party
import org.xmlpull.v1.XmlSerializer
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.StringReader
import java.io.StringWriter
import java.math.BigDecimal
import com.example.invoicingsystem.model.*

class XmlPersistenceManager {

    companion object {
        private const val INVOICES_DIR = "invoices"
        private const val ACTION_LOG_FILE = "action_log.xml"
        private const val SETTINGS_FILE = "settings.xml"

        // Invoice Tags
        private const val TAG_INVOICE = "invoice"
        private const val TAG_ID = "id"
        private const val TAG_CONTINGENCY_NUMBER = "contingencyInvoiceNumber"
        private const val TAG_IS_CONTINGENCY = "isContingency"
        private const val TAG_ISSUE_DATE = "issueDate"
        private const val TAG_ISSUE_TIME = "issueTime"
        private const val TAG_ISSUER = "issuer"
        private const val TAG_RECEIVER = "receiver"
        private const val TAG_PARTY_NAME = "name"
        private const val TAG_PARTY_ADDRESS = "fiscalAddress"
        private const val TAG_PARTY_EMAIL = "email"
        private const val TAG_PARTY_TAX_ID = "taxId"
        private const val TAG_PARTY_TAX_ID_TYPE = "taxIdType"
        private const val TAG_ITEMS = "items"
        private const val TAG_ITEM = "item"
        private const val TAG_ITEM_DESC = "description"
        private const val TAG_ITEM_QTY = "quantity"
        private const val TAG_ITEM_PRICE = "unitPrice"
        private const val TAG_VAT_RATE = "vatRate"
        private const val TAG_IS_EXEMPT = "isExempt"
        private const val TAG_PRINTER = "digitalPrinterInfo"
        private const val TAG_PRINTER_NAME = "name"
        private const val TAG_PRINTER_RIF = "rif"
        private const val TAG_PRINTER_AUTH_DATE = "authorizationDate"

        // Action Log Tags
        private const val TAG_LOGS = "logs"
        private const val TAG_LOG = "log"
        private const val TAG_LOG_TIMESTAMP = "timestamp"
        private const val TAG_LOG_ACTION = "action"
        private const val TAG_LOG_INVOICE_ID = "invoiceId"
        private const val TAG_LOG_DETAILS = "details"

        // Settings Tags
        private const val TAG_SETTINGS = "settings"
        // Uses TAG_ISSUER and TAG_PRINTER from above
    }

    fun saveInvoice(context: Context, invoice: Invoice) {
        try {
            val invoicesDir = context.getDir(INVOICES_DIR, Context.MODE_PRIVATE)
            val file = File(invoicesDir, "${invoice.id}.xml")

            file.outputStream().use { fos ->
                val serializer: XmlSerializer = Xml.newSerializer()
                val writer = StringWriter()
                serializer.setOutput(writer)
                serializer.startDocument("UTF-8", true)

                serializer.startTag(null, TAG_INVOICE)
                serializer.attribute(null, TAG_ID, invoice.id)

                // Invoice details
                serializer.writeTag(TAG_IS_CONTINGENCY, invoice.isContingency.toString())
                invoice.contingencyInvoiceNumber?.let {
                    serializer.writeTag(TAG_CONTINGENCY_NUMBER, it)
                }
                serializer.writeTag(TAG_ISSUE_DATE, invoice.issueDate)
                serializer.writeTag(TAG_ISSUE_TIME, invoice.issueTime)
                serializer.writeTag(TAG_VAT_RATE, invoice.vatRate.toPlainString())
                serializer.writeTag(TAG_IS_EXEMPT, invoice.isExempt.toString())

                // Issuer
                serializer.startTag(null, TAG_ISSUER)
                writeParty(serializer, invoice.issuer)
                serializer.endTag(null, TAG_ISSUER)

                // Receiver
                serializer.startTag(null, TAG_RECEIVER)
                writeParty(serializer, invoice.receiver)
                serializer.endTag(null, TAG_RECEIVER)

                // Items
                serializer.startTag(null, TAG_ITEMS)
                for (item in invoice.items) {
                    serializer.startTag(null, TAG_ITEM)
                    serializer.writeTag(TAG_ITEM_DESC, item.description)
                    serializer.writeTag(TAG_ITEM_QTY, item.quantity.toPlainString())
                    serializer.writeTag(TAG_ITEM_PRICE, item.unitPrice.toPlainString())
                    serializer.endTag(null, TAG_ITEM)
                }
                serializer.endTag(null, TAG_ITEMS)

                // Digital Printer
                serializer.startTag(null, TAG_PRINTER)
                serializer.writeTag(TAG_PRINTER_NAME, invoice.digitalPrinterInfo.name)
                serializer.writeTag(TAG_PRINTER_RIF, invoice.digitalPrinterInfo.rif)
                serializer.writeTag(TAG_PRINTER_AUTH_DATE, invoice.digitalPrinterInfo.authorizationDate)
                serializer.endTag(null, TAG_PRINTER)

                serializer.endTag(null, TAG_INVOICE)
                serializer.endDocument()
                fos.write(writer.toString().toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle exception
        }
    }

    fun loadAllInvoices(context: Context): List<Invoice> {
        val invoices = mutableListOf<Invoice>()
        val invoicesDir = context.getDir(INVOICES_DIR, Context.MODE_PRIVATE)
        if (invoicesDir.exists()) {
            invoicesDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.endsWith(".xml")) {
                    try {
                        val invoice = parseInvoice(file.readText())
                        invoices.add(invoice)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Handle parsing error for a single file
                    }
                }
            }
        }
        return invoices
    }

    fun loadInvoice(context: Context, invoiceId: String): Invoice? {
        val invoicesDir = context.getDir(INVOICES_DIR, Context.MODE_PRIVATE)
        val file = File(invoicesDir, "$invoiceId.xml")
        if (file.exists()) {
            try {
                return parseInvoice(file.readText())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    @Throws(Exception::class)
    private fun parseInvoice(xml: String): Invoice {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var eventType = parser.eventType
        var currentTag: String? = null

        // Invoice properties
        var id: String? = null
        var contingencyNumber: String? = null
        var isContingency: Boolean = false
        var issueDate: String? = null
        var issueTime: String? = null
        var issuer: Party? = null
        var receiver: Party? = null
        val items = mutableListOf<InvoiceItem>()
        var vatRate: BigDecimal? = null
        var isExempt: Boolean? = null
        var digitalPrinter: DigitalPrinter? = null

        // Item properties
        var itemDesc: String? = null
        var itemQty: BigDecimal? = null
        var itemPrice: BigDecimal? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name
                    when (currentTag) {
                        TAG_INVOICE -> id = parser.getAttributeValue(null, TAG_ID)
                        TAG_ISSUER -> issuer = readParty(parser, TAG_ISSUER)
                        TAG_RECEIVER -> receiver = readParty(parser, TAG_RECEIVER)
                        TAG_PRINTER -> digitalPrinter = readDigitalPrinter(parser)
                        TAG_ITEM -> {
                            itemDesc = null
                            itemQty = null
                            itemPrice = null
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text.trim()
                    if (text.isNotEmpty()) {
                        when (currentTag) {
                            TAG_IS_CONTINGENCY -> isContingency = text.toBoolean()
                            TAG_CONTINGENCY_NUMBER -> contingencyNumber = text
                            TAG_ISSUE_DATE -> issueDate = text
                            TAG_ISSUE_TIME -> issueTime = text
                            TAG_VAT_RATE -> vatRate = BigDecimal(text)
                            TAG_IS_EXEMPT -> isExempt = text.toBoolean()
                            TAG_ITEM_DESC -> itemDesc = text
                            TAG_ITEM_QTY -> itemQty = BigDecimal(text)
                            TAG_ITEM_PRICE -> itemPrice = BigDecimal(text)
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        TAG_ITEM -> {
                            if (itemDesc != null && itemQty != null && itemPrice != null) {
                                items.add(InvoiceItem(itemDesc!!, itemQty!!, itemPrice!!))
                            }
                        }
                        TAG_INVOICE -> {
                            // Invoice parsing finished, construct and return
                        }
                    }
                    currentTag = null
                }
            }
            eventType = parser.next()
        }

        return Invoice(
            id = id ?: throw IllegalStateException("Invoice ID missing"),
            contingencyInvoiceNumber = contingencyNumber,
            isContingency = isContingency,
            issueDate = issueDate ?: throw IllegalStateException("Issue date missing"),
            issueTime = issueTime ?: throw IllegalStateException("Issue time missing"),
            issuer = issuer ?: throw IllegalStateException("Issuer missing"),
            receiver = receiver ?: throw IllegalStateException("Receiver missing"),
            items = items,
            vatRate = vatRate ?: throw IllegalStateException("VAT rate missing"),
            isExempt = isExempt ?: false, // Default to false if not present
            digitalPrinterInfo = digitalPrinter ?: throw IllegalStateException("Digital printer info missing")
        )
    }

    private fun readParty(parser: XmlPullParser, endTag: String): Party {
        var name: String? = null
        var address: String? = null
        var email: String? = null
        var taxId: String? = null
        var taxIdType: TaxIdType? = null
        var currentTag: String? = null

        while (!(parser.next() == XmlPullParser.END_TAG && parser.name == endTag)) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> currentTag = parser.name
                XmlPullParser.TEXT -> {
                    val text = parser.text.trim()
                    if (text.isNotEmpty()) {
                        when (currentTag) {
                            TAG_PARTY_NAME -> name = text
                            TAG_PARTY_ADDRESS -> address = text
                            TAG_PARTY_EMAIL -> email = text
                            TAG_PARTY_TAX_ID -> taxId = text
                            TAG_PARTY_TAX_ID_TYPE -> taxIdType = TaxIdType.valueOf(text)
                        }
                    }
                }
            }
        }
        return Party(
            name ?: "",
            address ?: "",
            email ?: "",
            taxId ?: "",
            taxIdType ?: TaxIdType.RIF
        )
    }

    private fun readDigitalPrinter(parser: XmlPullParser): DigitalPrinter {
        var name: String? = null
        var rif: String? = null
        var authDate: String? = null
        var currentTag: String? = null

        while (!(parser.next() == XmlPullParser.END_TAG && parser.name == TAG_PRINTER)) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> currentTag = parser.name
                XmlPullParser.TEXT -> {
                    val text = parser.text.trim()
                    if (text.isNotEmpty()) {
                        when (currentTag) {
                            TAG_PRINTER_NAME -> name = text
                            TAG_PRINTER_RIF -> rif = text
                            TAG_PRINTER_AUTH_DATE -> authDate = text
                        }
                    }
                }
            }
        }
        return DigitalPrinter(name ?: "", rif ?: "", authDate ?: "")
    }

    private fun writeParty(serializer: XmlSerializer, party: Party) {
        serializer.writeTag(TAG_PARTY_NAME, party.name)
        serializer.writeTag(TAG_PARTY_ADDRESS, party.fiscalAddress)
        serializer.writeTag(TAG_PARTY_EMAIL, party.email)
        serializer.writeTag(TAG_PARTY_TAX_ID, party.taxId)
        serializer.writeTag(TAG_PARTY_TAX_ID_TYPE, party.taxIdType.name)
    }

    // Helper extension function to write a simple tag with text
    private fun XmlSerializer.writeTag(tagName: String, text: String) {
        startTag(null, tagName)
        text(text)
        endTag(null, tagName)
    }

    // --- Settings Persistence ---

    fun saveSettings(context: Context, issuer: Party, printer: DigitalPrinter) {
        val file = File(context.filesDir, SETTINGS_FILE)
        try {
            file.outputStream().use { fos ->
                val serializer = Xml.newSerializer()
                val writer = StringWriter()
                serializer.setOutput(writer)
                serializer.startDocument("UTF-8", true)
                serializer.startTag(null, TAG_SETTINGS)

                // Write Issuer
                serializer.startTag(null, TAG_ISSUER)
                writeParty(serializer, issuer)
                serializer.endTag(null, TAG_ISSUER)

                // Write Digital Printer
                serializer.startTag(null, TAG_PRINTER)
                serializer.writeTag(TAG_PRINTER_NAME, printer.name)
                serializer.writeTag(TAG_PRINTER_RIF, printer.rif)
                serializer.writeTag(TAG_PRINTER_AUTH_DATE, printer.authorizationDate)
                serializer.endTag(null, TAG_PRINTER)

                serializer.endTag(null, TAG_SETTINGS)
                serializer.endDocument()
                fos.write(writer.toString().toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadSettings(context: Context): Pair<Party?, DigitalPrinter?> {
        val file = File(context.filesDir, SETTINGS_FILE)
        if (!file.exists()) {
            return Pair(null, null)
        }

        var issuer: Party? = null
        var printer: DigitalPrinter? = null

        try {
            val xml = file.readText()
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        TAG_ISSUER -> issuer = readParty(parser, TAG_ISSUER)
                        TAG_PRINTER -> printer = readDigitalPrinter(parser)
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Pair(issuer, printer)
    }

    // --- Action Log Persistence ---

    fun saveActionLog(context: Context, logEntry: ActionLog) {
        val logs = loadAllActionLogs(context).toMutableList()
        logs.add(logEntry)

        val file = File(context.filesDir, ACTION_LOG_FILE)
        try {
            file.outputStream().use { fos ->
                val serializer = Xml.newSerializer()
                val writer = StringWriter()
                serializer.setOutput(writer)
                serializer.startDocument("UTF-8", true)
                serializer.startTag(null, TAG_LOGS)
                for (log in logs) {
                    serializer.startTag(null, TAG_LOG)
                    serializer.writeTag(TAG_LOG_TIMESTAMP, log.timestamp.toString())
                    serializer.writeTag(TAG_LOG_ACTION, log.actionType.name)
                    serializer.writeTag(TAG_LOG_INVOICE_ID, log.invoiceId)
                    serializer.writeTag(TAG_LOG_DETAILS, log.details)
                    serializer.endTag(null, TAG_LOG)
                }
                serializer.endTag(null, TAG_LOGS)
                serializer.endDocument()
                fos.write(writer.toString().toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadAllActionLogs(context: Context): List<ActionLog> {
        val logs = mutableListOf<ActionLog>()
        val file = File(context.filesDir, ACTION_LOG_FILE)
        if (!file.exists()) {
            return logs
        }

        try {
            val xml = file.readText()
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var currentTag: String? = null
            var timestamp: Long? = null
            var actionType: ActionType? = null
            var invoiceId: String? = null
            var details: String? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                        if (currentTag == TAG_LOG) {
                            timestamp = null
                            actionType = null
                            invoiceId = null
                            details = null
                        }
                    }
                    XmlPullParser.TEXT -> {
                        val text = parser.text.trim()
                        if (text.isNotEmpty()) {
                            when (currentTag) {
                                TAG_LOG_TIMESTAMP -> timestamp = text.toLong()
                                TAG_LOG_ACTION -> actionType = ActionType.valueOf(text)
                                TAG_LOG_INVOICE_ID -> invoiceId = text
                                TAG_LOG_DETAILS -> details = text
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == TAG_LOG) {
                            if (timestamp != null && actionType != null && invoiceId != null && details != null) {
                                logs.add(ActionLog(timestamp, actionType, invoiceId, details))
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return logs
    }
}
