package com.example.tailorrecords.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tailorrecords.data.models.Measurement
import com.example.tailorrecords.viewmodel.CustomerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMeasurementScreen(
    navController: NavController,
    customerId: Long,
    measurementId: Long? = null,
    viewModel: CustomerViewModel = viewModel()
) {
    var upperBodyMeasurements by remember { mutableStateOf("") }
    var lowerBodyMeasurements by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val isEditMode = measurementId != null

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
                    val measurement = Measurement(
                        id = measurementId ?: 0,
                        customerId = customerId,
                        upperBodyMeasurements = upperBodyMeasurements.trim(),
                        lowerBodyMeasurements = lowerBodyMeasurements.trim(),
                        notes = notes.trim()
                    )
                    
                    if (isEditMode) {
                        viewModel.updateMeasurement(measurement)
                    } else {
                        viewModel.insertMeasurement(measurement) {
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
            Text(
                "Upper Body Measurements",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = upperBodyMeasurements,
                onValueChange = { upperBodyMeasurements = it },
                label = { Text("Upper Body") },
                placeholder = { Text("e.g., Shirt Length: 28, Shoulder: 18, Chest: 40...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
            )

            HorizontalDivider()

            Text(
                "Lower Body Measurements",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = lowerBodyMeasurements,
                onValueChange = { lowerBodyMeasurements = it },
                label = { Text("Lower Body") },
                placeholder = { Text("e.g., Pant Length: 40, Waist: 34, Hip: 42...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
            )

            HorizontalDivider()

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }
    }
}

