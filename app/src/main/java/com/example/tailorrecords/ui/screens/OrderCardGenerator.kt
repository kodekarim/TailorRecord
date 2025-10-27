package com.example.tailorrecords.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.tailorrecords.data.models.Customer
import com.example.tailorrecords.data.models.Order
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

// Function to generate and share order card with QR code
suspend fun shareOrderCard(context: Context, order: Order, customer: Customer) {
    withContext(Dispatchers.IO) {
        try {
            val bitmap = generateOrderCardBitmap(order, customer)
            val uri = saveBitmapAndGetUri(context, bitmap, "order_${order.id}.png")
            shareToWhatsApp(context, customer.phoneNumber, uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun generateOrderCardBitmap(order: Order, customer: Customer): Bitmap {
    val width = 1080
    val height = 1400
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    // Background
    val bgPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
    
    // Header background
    val headerPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#6200EE")
        style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, width.toFloat(), 250f, headerPaint)
    
    // Title
    val titlePaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 60f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("ORDER DETAILS", width / 2f, 100f, titlePaint)
    
    // Order ID
    val orderIdPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 45f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("Order #${order.id}", width / 2f, 170f, orderIdPaint)
    
    // Customer name
    canvas.drawText(customer.name, width / 2f, 220f, orderIdPaint)
    
    // Content area
    var yPos = 320f
    val labelPaint = Paint().apply {
        color = android.graphics.Color.GRAY
        textSize = 35f
        textAlign = Paint.Align.LEFT
    }
    val valuePaint = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 40f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        textAlign = Paint.Align.RIGHT
    }
    
    // Draw order details
    fun drawDetailRow(label: String, value: String) {
        canvas.drawText(label, 80f, yPos, labelPaint)
        canvas.drawText(value, width - 80f, yPos, valuePaint)
        yPos += 70f
    }
    
    drawDetailRow("Item:", order.itemType)
    drawDetailRow("Quantity:", order.quantity.toString())
    drawDetailRow("Total Price:", "₹${order.price}")
    drawDetailRow("Advance Paid:", "₹${order.advancePaid}")
    drawDetailRow("Balance:", "₹${order.price - order.advancePaid}")
    drawDetailRow("Due Date:", dateFormat.format(Date(order.dueDate)))
    drawDetailRow("Status:", order.status.name.replace("_", " "))
    
    // QR Code
    yPos += 30f
    val qrCodeBitmap = generateQrCode("order_id:${order.id},customer_name:${customer.name}", 400)
    qrCodeBitmap?.let {
        val qrX = (width - 400) / 2f
        canvas.drawBitmap(it, qrX, yPos, null)
    }
    
    yPos += 450f
    
    // Footer text
    val footerPaint = Paint().apply {
        color = android.graphics.Color.GRAY
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("Show this QR code when picking up your order", width / 2f, yPos, footerPaint)
    canvas.drawText("Phone: ${customer.phoneNumber}", width / 2f, yPos + 50f, footerPaint)
    
    return bitmap
}

private fun generateQrCode(data: String, size: Int): Bitmap? {
    return try {
        val barcodeEncoder = BarcodeEncoder()
        barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, size, size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun saveBitmapAndGetUri(context: Context, bitmap: Bitmap, fileName: String): Uri {
    val imagesFolder = File(context.cacheDir, "images")
    imagesFolder.mkdirs()
    val file = File(imagesFolder, fileName)
    val stream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
    stream.flush()
    stream.close()
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

private fun shareToWhatsApp(context: Context, phoneNumber: String, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "Here are your order details. Please show the QR code when picking up your order.")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        `package` = "com.whatsapp"
        // Format phone number for WhatsApp
        val whatsappNumber = phoneNumber.replace(Regex("[^0-9]"), "")
        putExtra("jid", "$whatsappNumber@s.whatsapp.net")
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // If WhatsApp not installed or error, use general share
        val generalIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Here are your order details. Please show the QR code when picking up your order.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(generalIntent, "Share Order Card"))
    }
}

