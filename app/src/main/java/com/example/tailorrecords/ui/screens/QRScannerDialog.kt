package com.example.tailorrecords.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.google.zxing.BarcodeFormat

@Composable
fun QRScannerDialog(
    onDismiss: () -> Unit,
    onScanResult: (String?) -> Unit
) {
    val context = LocalContext.current
    var scannedResult by remember { mutableStateOf<String?>(null) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Scanner view
                AndroidView(
                    factory = { ctx ->
                        val scannerView = DecoratedBarcodeView(ctx).apply {
                            val formats = listOf(BarcodeFormat.QR_CODE)
                            barcodeView.decoderFactory = com.journeyapps.barcodescanner.DefaultDecoderFactory(formats)

                            decodeContinuous(object : BarcodeCallback {
                                override fun barcodeResult(result: BarcodeResult?) {
                                    val text = result?.text
                                    if (!text.isNullOrEmpty() && scannedResult == null) {
                                        scannedResult = text
                                        // Stop scanning to prevent multiple callbacks
                                        this@apply.pause()
                                        onScanResult(text)
                                    }
                                }

                                override fun possibleResultPoints(resultPoints: MutableList<com.google.zxing.ResultPoint>?) {
                                    // Not needed
                                }
                            })

                            resume()
                        }
                        scannerView
                    },
                    modifier = Modifier.fillMaxSize(),
                    onRelease = { view ->
                        view.pause()
                    }
                )
                
                // Top bar with close button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Scan QR Code",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                
                // Instructions at bottom
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    tonalElevation = 4.dp
                ) {
                    Text(
                        "Point your camera at the QR code",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

