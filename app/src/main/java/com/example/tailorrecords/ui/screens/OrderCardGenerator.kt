package com.example.tailorrecords.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import android.content.pm.PackageManager
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

private const val TAG = "OrderCardGenerator"

// Function to generate and share order card with QR code
suspend fun shareOrderCard(context: Context, order: Order, customer: Customer) {
    Log.d(TAG, "shareOrderCard called - Order ID: ${order.id}, Customer: ${customer.name}")
    try {
        Log.d(TAG, "Starting bitmap generation...")
        val bitmap = withContext(Dispatchers.IO) {
            generateOrderCardBitmap(order, customer)
        }
        Log.d(TAG, "Bitmap generated successfully: ${bitmap.width}x${bitmap.height}")
        
        Log.d(TAG, "Saving bitmap to file...")
        val uri = withContext(Dispatchers.IO) {
            saveBitmapAndGetUri(context, bitmap, "order_${order.id}.png")
        }
        Log.d(TAG, "File saved successfully: $uri")
        
        // Share on Main thread
        Log.d(TAG, "Launching WhatsApp share...")
        withContext(Dispatchers.Main) {
            shareToWhatsApp(context, customer.phoneNumber, uri)
        }
        Log.d(TAG, "WhatsApp share completed")
    } catch (e: Exception) {
        Log.e(TAG, "Error in shareOrderCard", e)
        e.printStackTrace()
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
    
    // Order Number
    val orderIdPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 45f
        textAlign = Paint.Align.CENTER
    }
    val orderDisplayText = if (order.orderNumber.isNotEmpty()) {
        "Order #${order.orderNumber}"
    } else {
        "Order #${order.id}"
    }
    canvas.drawText(orderDisplayText, width / 2f, 170f, orderIdPaint)
    
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
    
    if (order.orderNumber.isNotEmpty()) {
        drawDetailRow("Order Number:", order.orderNumber)
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
    val whatsappPackage = "com.whatsapp"

    // Proactively grant URI permission to WhatsApp (helps on some OEMs)
    try {
        context.grantUriPermission(whatsappPackage, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    } catch (_: Exception) { }

    val isWhatsAppInstalled = try {
        context.packageManager.getPackageInfo(whatsappPackage, 0)
        true
    } catch (_: Exception) { false }

    if (isWhatsAppInstalled) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Here are your order details. Please show the QR code when picking up your order.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            `package` = whatsappPackage

            // Attach JID only if number looks valid with country code (WhatsApp requirement)
            val whatsappNumber = phoneNumber.replace(Regex("[^0-9]"), "")
            if (whatsappNumber.length >= 8 && whatsappNumber.any()) {
                putExtra("jid", "$whatsappNumber@s.whatsapp.net")
            }
        }
        try {
            context.startActivity(intent)
            return
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch WhatsApp share intent", e)
        }
    }

    // Fallback: general share chooser
    val generalIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "Here are your order details. Please show the QR code when picking up your order.")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(generalIntent, "Share Order Card").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}




