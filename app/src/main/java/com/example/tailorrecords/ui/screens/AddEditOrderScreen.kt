package com.example.tailorrecords.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tailorrecords.data.models.Order
import com.example.tailorrecords.data.models.OrderStatus
import com.example.tailorrecords.utils.ItemTypeManager
import com.example.tailorrecords.viewmodel.CustomerViewModel
import com.example.tailorrecords.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditOrderScreen(
    navController: NavController,
    customerId: Long?, // Nullable for edit mode
    orderId: Long? = null,
    orderViewModel: OrderViewModel = viewModel(),
    customerViewModel: CustomerViewModel = viewModel()
) {
    val context = LocalContext.current
    var customerName by remember { mutableStateOf("") }

    // Item Type State
    var itemType by remember { mutableStateOf("") }
    var customItemType by remember { mutableStateOf("") }
    var isOtherSelected by remember { mutableStateOf(false) }
    var itemTypeMenuExpanded by remember { mutableStateOf(false) }
    val itemTypes = remember { ItemTypeManager.getItemTypes(context) }

    var quantity by remember { mutableStateOf("1") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var advancePaid by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf(OrderStatus.PENDING) }
    var dueDate by remember { mutableStateOf(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)) }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val isEditMode = orderId != null
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    // Load customer and order data
    LaunchedEffect(key1 = orderId, key2 = customerId) {
        if (isEditMode && orderId != null) {
            orderViewModel.getOrderById(orderId).collect { order ->
                order?.let {
                    itemType = it.itemType
                    quantity = it.quantity.toString()
                    description = it.description
                    price = it.price.toString()
                    advancePaid = it.advancePaid.toString()
                    selectedStatus = it.status
                    dueDate = it.dueDate
                    notes = it.notes
                    // Fetch customer name using the customerId from the order
                    customerViewModel.getCustomerById(it.customerId).collect { customer ->
                        customer?.let { c -> customerName = c.name }
                    }
                }
            }
        } else if (!isEditMode && customerId != null) {
            // This is for creating a new order
            customerViewModel.getCustomerById(customerId).collect { customer ->
                customer?.let { customerName = it.name }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Order" else "New Order") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            val finalItemType = if (isOtherSelected) customItemType.trim() else itemType
            if (finalItemType.isNotBlank() && price.isNotBlank() && !isLoading) {
                FloatingActionButton(
                    onClick = {
                        isLoading = true
                        val order = Order(
                            id = orderId ?: 0,
                            customerId = customerId ?: 0, // Use the provided customerId or 0 for new
                            itemType = finalItemType,
                            quantity = quantity.toIntOrNull() ?: 1,
                            description = description.trim(),
                            price = price.toDoubleOrNull() ?: 0.0,
                            advancePaid = advancePaid.toDoubleOrNull() ?: 0.0,
                            status = selectedStatus,
                            dueDate = dueDate,
                            notes = notes.trim()
                        )

                        if (isEditMode) {
                            orderViewModel.updateOrder(order)
                            navController.navigateUp()
                        } else {
                            orderViewModel.insertOrder(order) { id ->
                                // Add the custom item type if it's new
                                if (isOtherSelected && customItemType.isNotBlank()) {
                                    ItemTypeManager.addItemType(context, customItemType.trim())
                                }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer name (read-only)
            OutlinedTextField(
                value = customerName,
                onValueChange = { },
                label = { Text("Customer") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                enabled = false
            )

            // Item type dropdown
            ExposedDropdownMenuBox(
                expanded = itemTypeMenuExpanded,
                onExpandedChange = { itemTypeMenuExpanded = !itemTypeMenuExpanded },
            ) {
                OutlinedTextField(
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    readOnly = true,
                    value = if (isOtherSelected) "Other" else itemType,
                    onValueChange = {},
                    label = { Text("Item Type *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = itemTypeMenuExpanded) },
                    isError = itemType.isBlank() && !isOtherSelected
                )
                ExposedDropdownMenu(
                    expanded = itemTypeMenuExpanded,
                    onDismissRequest = { itemTypeMenuExpanded = false },
                ) {
                    itemTypes.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                if (selectionOption == "Other") {
                                    isOtherSelected = true
                                    itemType = ""
                                } else {
                                    isOtherSelected = false
                                    itemType = selectionOption
                                    customItemType = ""
                                }
                                itemTypeMenuExpanded = false
                            }
                        )
                    }
                }
            }
            
            if (isOtherSelected) {
                OutlinedTextField(
                    value = customItemType,
                    onValueChange = { customItemType = it },
                    label = { Text("Enter Item Type *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = customItemType.isBlank()
                )
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Status dropdown
                ExposedDropdownMenuBox(
                    expanded = showStatusMenu,
                    onExpandedChange = { showStatusMenu = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedStatus.name.replace("_", " "),
                        onValueChange = { },
                        label = { Text("Status") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStatusMenu) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        OrderStatus.values().forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.name.replace("_", " ")) },
                                onClick = {
                                    selectedStatus = status
                                    showStatusMenu = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("e.g., Blue cotton shirt with collar") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Total Price *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = price.isBlank(),
                    prefix = { Text("₹") }
                )

                OutlinedTextField(
                    value = advancePaid,
                    onValueChange = { advancePaid = it },
                    label = { Text("Advance Paid") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("₹") }
                )
            }

            // Due date picker
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Due Date: ${dateFormat.format(Date(dueDate))}")
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

            // Show remaining balance
            val priceValue = price.toDoubleOrNull() ?: 0.0
            val advanceValue = advancePaid.toDoubleOrNull() ?: 0.0
            val remainingBalance = priceValue - advanceValue
            
            if (priceValue > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (remainingBalance > 0) 
                            MaterialTheme.colorScheme.errorContainer 
                        else 
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Remaining Balance:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "₹$remainingBalance",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (remainingBalance > 0) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dueDate = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

