package com.example.tailorrecords.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tailorrecords.data.models.Measurement
import com.example.tailorrecords.data.models.MeasurementField
import com.example.tailorrecords.viewmodel.CustomerViewModel
import com.example.tailorrecords.viewmodel.SettingsViewModel
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMeasurementScreen(
    navController: NavController,
    customerId: Long,
    measurementId: Long? = null,
    customerViewModel: CustomerViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val measurementFields by settingsViewModel.measurementFields.collectAsState()
    var measurementValues by remember { mutableStateOf(mapOf<String, String>()) }
    var notes by remember { mutableStateOf("") }
    val isEditMode = measurementId != null

    // State for dialogs
    var showAddFieldDialog by remember { mutableStateOf(false) }
    var newFieldCategory by remember { mutableStateOf("") }
    var fieldToDelete by remember { mutableStateOf<MeasurementField?>(null) }

    if (isEditMode && measurementId != null) {
        // When editing, load the specific measurement and listen for changes
        LaunchedEffect(measurementId) {
            customerViewModel.getMeasurementById(measurementId).collect { measurement ->
                if (measurement != null) {
                    measurementValues = measurement.values
                    notes = measurement.notes
                }
            }
        }
    } else {
        // When adding, pre-fill the form with the latest measurement data once
        LaunchedEffect(customerId) {
            val latest = customerViewModel.getMeasurementsByCustomerId(customerId).first().firstOrNull()
            if (latest != null) {
                measurementValues = latest.values
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Measurement" else "Add Measurement") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val finalValues = measurementValues.mapValues { it.value.trim() }.filterValues { it.isNotEmpty() }
                    val measurement = Measurement(
                        id = measurementId ?: 0,
                        customerId = customerId,
                        values = finalValues,
                        notes = notes.trim()
                    )

                    if (isEditMode) {
                        customerViewModel.updateMeasurement(measurement)
                    } else {
                        customerViewModel.insertMeasurement(measurement) {
                            navController.navigateUp()
                        }
                    }
                    if (isEditMode) {
                        navController.navigateUp()
                    }
                }
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val groupedFields = measurementFields.groupBy { it.category }
            val categories = listOf("Upper Body", "Lower Body")

            categories.forEach { category ->
                Text(
                    category,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                val fieldsInCategory = groupedFields[category] ?: emptyList()

                fieldsInCategory.forEach { field ->
                    MeasurementTextField(
                        label = field.name,
                        value = measurementValues[field.name] ?: "",
                        onValueChange = {
                            measurementValues = measurementValues + (field.name to it)
                        },
                        onDelete = {
                            fieldToDelete = field
                        }
                    )
                }

                TextButton(
                    onClick = {
                        newFieldCategory = category
                        showAddFieldDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add ${category.substringBefore(" ")} Field")
                }
                HorizontalDivider()
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }

        if (showAddFieldDialog) {
            AddMeasurementFieldDialog(
                onDismiss = { showAddFieldDialog = false },
                onAdd = { name ->
                    settingsViewModel.addMeasurementField(name, newFieldCategory)
                    showAddFieldDialog = false
                }
            )
        }

        fieldToDelete?.let { field ->
            AlertDialog(
                onDismissRequest = { fieldToDelete = null },
                title = { Text("Delete Field") },
                text = { Text("Are you sure you want to delete the '${field.name}' field? This will remove it from all measurement entries.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            settingsViewModel.deleteMeasurementField(field)
                            fieldToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { fieldToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun MeasurementTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        onDelete?.let {
            IconButton(onClick = it) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Field", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddMeasurementFieldDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Measurement Field") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Field Name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

