package com.example.tailorrecords.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tailorrecords.data.models.MeasurementField
import com.example.tailorrecords.utils.DataExportImport
import com.example.tailorrecords.viewmodel.SettingsViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // File picker for export (save)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            isLoading = true
            try {
                // Create temporary file to export data
                val tempFile = File(context.cacheDir, DataExportImport.getExportFileName())
                viewModel.exportData(tempFile) { success ->
                    if (success) {
                        // Copy to selected location
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            tempFile.inputStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        tempFile.delete()
                        Toast.makeText(context, "Data exported successfully!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Export failed!", Toast.LENGTH_LONG).show()
                    }
                    isLoading = false
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
                isLoading = false
            }
        }
    }

    // File picker for import (open)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            isLoading = true
            try {
                // Copy to temporary file
                val tempFile = File(context.cacheDir, "import_temp.json")
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    tempFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                viewModel.importData(tempFile) { success ->
                    tempFile.delete()
                    if (success) {
                        Toast.makeText(context, "Data imported successfully!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Import failed! Check file format.", Toast.LENGTH_LONG).show()
                    }
                    isLoading = false
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Import error: ${e.message}", Toast.LENGTH_LONG).show()
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DataManagementSection(
                    onExportClick = { showExportDialog = true },
                    onImportClick = { showImportDialog = true },
                    isLoading = isLoading
                )
            }
        }

        // Export confirmation dialog
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                icon = { Icon(Icons.Default.Upload, contentDescription = null) },
                title = { Text("Export Data") },
                text = { Text("This will create a backup file with all your customers, measurements, and orders.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showExportDialog = false
                            exportLauncher.launch(DataExportImport.getExportFileName())
                        }
                    ) {
                        Text("Export")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Import confirmation dialog
        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                icon = { Icon(Icons.Default.Download, contentDescription = null) },
                title = { Text("Import Data") },
                text = { Text("This will add the imported data to your existing records. Make sure you have a valid backup file.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showImportDialog = false
                            importLauncher.launch("application/json")
                        }
                    ) {
                        Text("Import")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun DataManagementSection(
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    isLoading: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Data Management",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            "Backup and restore your customer data, measurements, and orders.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Export Data Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onExportClick,
            enabled = !isLoading
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Upload,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Export Data",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Save all data to a JSON file",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }

        // Import Data Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onImportClick,
            enabled = !isLoading
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Import Data",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Restore data from a JSON file",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Important Notes",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "• Exported data is saved as JSON format\n" +
                            "• Importing will add data to existing records\n" +
                            "• Customer photos are included in the backup\n" +
                            "• Keep your backup files safe",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

