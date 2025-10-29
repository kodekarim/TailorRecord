package com.example.tailorrecords.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tailorrecords.data.models.Order
import com.example.tailorrecords.data.models.OrderStatus
import com.example.tailorrecords.navigation.Screen
import com.example.tailorrecords.utils.ItemTypeManager
import com.example.tailorrecords.utils.CustomizationManager
import com.example.tailorrecords.viewmodel.CustomerViewModel
import com.example.tailorrecords.viewmodel.OrderViewModel
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditOrderScreen(
    navController: NavController,
    customerId: Long?, // Nullable for edit mode
    orderId: Long? = null,
    orderViewModel: OrderViewModel = viewModel(),
    customerViewModel: CustomerViewModel = viewModel(),
    returnToCustomerTab: Int = 0 // Tab to return to when navigating back
) {
    val context = LocalContext.current
    val isEditMode = orderId != null
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val orderWithCustomer by orderViewModel.getOrderWithCustomerById(orderId ?: 0).collectAsState(initial = null)
    val order = orderWithCustomer?.order
    val customer = orderWithCustomer?.customer
    
    // Determine the effective customer ID (for navigation back)
    val effectiveCustomerId = if (isEditMode && order != null) order.customerId else (customerId ?: 0)

    // States for UI fields, initialized from the order
    var orderNumber by remember(order) { mutableStateOf(order?.orderNumber ?: "") }
    var itemType by remember(order) { mutableStateOf(order?.itemType ?: "") }
    var quantity by remember(order) { mutableStateOf(order?.quantity?.toString() ?: "1") }
    var selectedCustomizations by remember(order) { mutableStateOf<List<String>>(order?.customizations ?: emptyList()) }
    var price by remember(order) { mutableStateOf(order?.price?.toString() ?: "") }
    var advancePaid by remember(order) { mutableStateOf(order?.advancePaid?.toString() ?: "") }
    var selectedStatus by remember(order) { mutableStateOf(order?.status ?: OrderStatus.PENDING) }
    var dueDate by remember(order) { mutableStateOf(order?.dueDate ?: System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)) }
    var notes by remember(order) { mutableStateOf(order?.notes ?: "") }

    var customItemType by remember { mutableStateOf("") }
    var isOtherSelected by remember { mutableStateOf(false) }
    var itemTypeMenuExpanded by remember { mutableStateOf(false) }
    val itemTypes = remember { ItemTypeManager.getItemTypes(context) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }
    var showCustomizationDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // This effect handles loading customer name
    var customerName by remember { mutableStateOf("") }
    LaunchedEffect(key1 = customerId, key2 = customer) {
        customerName = when {
            customer != null -> customer.name
            !isEditMode && customerId != null -> {
                customerViewModel.getCustomerById(customerId).first()?.name ?: ""
            }
            else -> ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Order" else "New Order") },
                navigationIcon = {
                    IconButton(onClick = { 
                        // Navigate back to customer detail with the correct tab if we came from there
                        if (isEditMode && effectiveCustomerId > 0 && returnToCustomerTab > 0) {
                            navController.navigate(Screen.CustomerDetail.createRoute(effectiveCustomerId, returnToCustomerTab)) {
                                popUpTo(Screen.CustomerDetail.createRoute(effectiveCustomerId, returnToCustomerTab)) {
                                    inclusive = true
                                }
                            }
                        } else {
                            navController.navigateUp()
                        }
                    }) {
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
                        // Preserve immutable fields from existing order in edit mode
                        val effectiveOrderDate = if (isEditMode && order != null) order.orderDate else System.currentTimeMillis()
                        val effectiveCompletedDate = when (selectedStatus) {
                            OrderStatus.COMPLETED, OrderStatus.DELIVERED -> order?.completedDate ?: System.currentTimeMillis()
                            else -> null
                        }

                        val order = Order(
                            id = orderId ?: 0,
                            customerId = effectiveCustomerId,
                            orderNumber = orderNumber.trim(),
                            itemType = finalItemType,
                            quantity = quantity.toIntOrNull() ?: 1,
                            customizations = selectedCustomizations,
                            price = price.toDoubleOrNull() ?: 0.0,
                            advancePaid = advancePaid.toDoubleOrNull() ?: 0.0,
                            status = selectedStatus,
                            orderDate = effectiveOrderDate,
                            dueDate = dueDate,
                            completedDate = effectiveCompletedDate,
                            notes = notes.trim()
                        )

                        if (isEditMode) {
                            orderViewModel.updateOrder(order)
                            // Navigate back to customer detail with the correct tab if we came from there
                            if (effectiveCustomerId > 0 && returnToCustomerTab > 0) {
                                navController.navigate(Screen.CustomerDetail.createRoute(effectiveCustomerId, returnToCustomerTab)) {
                                    popUpTo(Screen.CustomerDetail.createRoute(effectiveCustomerId, returnToCustomerTab)) {
                                        inclusive = true
                                    }
                                }
                            } else {
                                navController.navigateUp()
                            }
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

            // Order Number
            OutlinedTextField(
                value = orderNumber,
                onValueChange = { orderNumber = it },
                label = { Text("Order Number") },
                placeholder = { Text("e.g., ORD-001") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
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

            // Customizations Section
            val finalItemType = if (isOtherSelected) customItemType.trim() else itemType
            if (finalItemType.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Customizations",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { showCustomizationDialog = true },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add")
                            }
                        }

                        if (selectedCustomizations.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                selectedCustomizations.forEach { customization ->
                                    AssistChip(
                                        onClick = {
                                            selectedCustomizations = selectedCustomizations.filter { it != customization }
                                        },
                                        label = { Text(customization) },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        } else {
                            Text(
                                "No customizations added",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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

    // Customization dialog
    if (showCustomizationDialog) {
        val finalItemType = if (isOtherSelected) customItemType.trim() else itemType
        CustomizationDialog(
            itemType = finalItemType,
            existingCustomizations = selectedCustomizations,
            onDismiss = { showCustomizationDialog = false },
            onAdd = { customization ->
                selectedCustomizations = selectedCustomizations + customization
                showCustomizationDialog = false
            }
        )
    }
}

@Composable
fun CustomizationDialog(
    itemType: String,
    existingCustomizations: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    val context = LocalContext.current
    var newCustomization by remember { mutableStateOf("") }
    val availableCustomizations = remember(itemType) {
        CustomizationManager.getCustomizations(context, itemType)
            .filter { it !in existingCustomizations }
    }
    var showNewCustomizationField by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Customization") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (availableCustomizations.isNotEmpty()) {
                    Text(
                        "Select from saved options:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(availableCustomizations.size) { index ->
                            val customization = availableCustomizations[index]
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAdd(customization)
                                    }
                            ) {
                                Text(
                                    customization,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }

                if (showNewCustomizationField) {
                    OutlinedTextField(
                        value = newCustomization,
                        onValueChange = { newCustomization = it },
                        label = { Text("New Customization") },
                        placeholder = { Text("e.g., Two Pockets") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    TextButton(
                        onClick = { showNewCustomizationField = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create New Customization")
                    }
                }
            }
        },
        confirmButton = {
            if (showNewCustomizationField && newCustomization.isNotBlank()) {
                Button(
                    onClick = {
                        CustomizationManager.addCustomization(context, itemType, newCustomization.trim())
                        onAdd(newCustomization.trim())
                    }
                ) {
                    Text("Add")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

