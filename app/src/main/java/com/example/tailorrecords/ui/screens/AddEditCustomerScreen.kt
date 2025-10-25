package com.example.tailorrecords.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.tailorrecords.data.models.Customer
import com.example.tailorrecords.navigation.Screen
import com.example.tailorrecords.ui.theme.WarningContainer
import com.example.tailorrecords.viewmodel.CustomerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerScreen(
    navController: NavController,
    customerId: Long? = null,
    viewModel: CustomerViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var photoUriString by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var existingCustomer by remember { mutableStateOf<Customer?>(null) }

    val isEditMode = customerId != null
    val scope = rememberCoroutineScope()

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            photoUri = it
            photoUriString = it.toString()
        }
    }

    // Check for existing customer when phone number changes
    LaunchedEffect(phoneNumber) {
        if (isEditMode) {
            scope.launch {
                val originalPhoneNumber = viewModel.getCustomerById(customerId!!).first()?.phoneNumber
                if (originalPhoneNumber == phoneNumber) {
                    existingCustomer = null
                    return@launch
                }
            }
        }

        if (phoneNumber.length >= 10) { // Simple check to avoid querying too often
            delay(300) // Debounce
            existingCustomer = viewModel.findCustomerByPhone(phoneNumber.trim())
        } else {
            existingCustomer = null
        }
    }

    // Load customer data if editing
    LaunchedEffect(customerId) {
        if (customerId != null) {
            viewModel.getCustomerById(customerId).collect { customer ->
                customer?.let {
                    name = it.name
                    phoneNumber = it.phoneNumber
                    photoUriString = it.photoUri
                    if (photoUriString.isNotEmpty()) {
                        photoUri = Uri.parse(photoUriString)
                    }
                    notes = it.notes
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Customer" else "Add Customer") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (name.isNotBlank() && phoneNumber.isNotBlank() && !isLoading) {
                FloatingActionButton(
                    onClick = {
                        isLoading = true
                        val customer = Customer(
                            id = customerId ?: 0,
                            name = name.trim(),
                            phoneNumber = phoneNumber.trim(),
                            photoUri = photoUriString,
                            notes = notes.trim()
                        )
                        
                        if (isEditMode) {
                            viewModel.updateCustomer(customer)
                            navController.navigateUp()
                        } else {
                            viewModel.insertCustomer(customer) { id ->
                                navController.navigateUp()
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Photo picker
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable { photoPickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(photoUri),
                        contentDescription = "Customer Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Add Photo",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Add Photo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = name.isBlank()
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneNumber.isBlank() || existingCustomer != null
            )

            existingCustomer?.let { customer ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate(Screen.CustomerDetail.createRoute(customer.id))
                        },
                    colors = CardDefaults.cardColors(containerColor = WarningContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Warning")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Phone number already exists for '${customer.name}'. Tap to view.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Text(
                "* Required fields",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

