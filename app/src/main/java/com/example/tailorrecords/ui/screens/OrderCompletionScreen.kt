package com.example.tailorrecords.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tailorrecords.viewmodel.OrderViewModel
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderCompletionScreen(
    navController: NavController,
    orderId: Long,
    customerName: String,
    customerPhone: String,
    viewModel: OrderViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val viewToCapture = LocalView.current

    // Load order details
    val orderWithCustomer by viewModel.getOrderWithCustomerById(orderId).collectAsState(initial = null)
    val order = orderWithCustomer?.order

    val qrCodeBitmap by remember {
        mutableStateOf(generateQrCode("order_id:$orderId,customer_name:$customerName"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Complete") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Order #$orderId",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        customerName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    order?.let { ord ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Order details
                        OrderDetailRow("Item", ord.itemType)
                        OrderDetailRow("Quantity", ord.quantity.toString())
                        OrderDetailRow("Price", "₹${ord.price}")
                        OrderDetailRow("Advance Paid", "₹${ord.advancePaid}")
                        OrderDetailRow("Balance", "₹${ord.price - ord.advancePaid}")
                        
                        if (ord.orderNumber.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Order Number: ${ord.orderNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            qrCodeBitmap?.let {
                Card(
                    modifier = Modifier.size(270.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Order QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        val bitmap = Bitmap.createBitmap(viewToCapture.width, viewToCapture.height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        viewToCapture.draw(canvas)
                        
                        val uri = saveBitmapAndGetUri(context, bitmap, "order_$orderId.png")
                        shareToWhatsApp(context, customerPhone, uri)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share via WhatsApp")
            }
        }
    }
}

@Composable
fun OrderDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}

private fun generateQrCode(data: String): Bitmap? {
    return try {
        val barcodeEncoder = BarcodeEncoder()
        barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 400, 400)
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
        `package` = "com.whatsapp"
        // Format phone number for WhatsApp
        val whatsappNumber = phoneNumber.replace(Regex("[^0-9]"), "")
        putExtra("jid", "$whatsappNumber@s.whatsapp.net")
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // WhatsApp not installed
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp"))
        context.startActivity(webIntent)
    }
}
