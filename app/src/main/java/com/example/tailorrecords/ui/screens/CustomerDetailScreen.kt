package com.example.tailorrecords.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tailorrecords.data.models.Customer
import com.example.tailorrecords.data.models.Measurement
import com.example.tailorrecords.data.models.Order
import com.example.tailorrecords.navigation.Screen
import com.example.tailorrecords.viewmodel.CustomerViewModel
import com.example.tailorrecords.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    navController: NavController,
    customerId: Long,
    viewModel: CustomerViewModel = viewModel(),
    orderViewModel: OrderViewModel = viewModel()
) {
    val customer by viewModel.getCustomerById(customerId).collectAsState(initial = null)
    val measurements by viewModel.getMeasurementsByCustomerId(customerId).collectAsState(initial = emptyList())
    val orders by orderViewModel.getOrdersByCustomerId(customerId).collectAsState(initial = emptyList())
    var selectedTab by remember { mutableStateOf(0) }

    // This LaunchedEffect is no longer needed for loading data that should auto-update.
    // We can keep it if we need to perform one-off actions when customerId changes.
    /*
    LaunchedEffect(customerId) {
        viewModel.getCustomerById(customerId).collect { 
            customer = it
        }
        viewModel.loadMeasurements(customerId)
        orderViewModel.getOrdersByCustomerId(customerId).collect {
            orders = it
        }
    }
    */

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customer?.name ?: "Customer Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        navController.navigate(Screen.EditCustomer.createRoute(customerId))
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddMeasurement.createRoute(customerId)) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Measurement")
                }
                1 -> FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddOrder.createRoute(customerId)) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Order")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Customer info card
            customer?.let { cust ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(cust.phoneNumber, style = MaterialTheme.typography.bodyLarge)
                        }
                        
                        if (cust.notes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    Icons.Default.Notes,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(cust.notes, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Measurements (${measurements.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Orders (${orders.size})") }
                )
            }

            // Tab content
            when (selectedTab) {
                0 -> MeasurementsTab(measurements, navController, customerId, viewModel)
                1 -> OrdersTab(orders, navController, orderViewModel)
            }
        }
    }
}

@Composable
fun MeasurementsTab(
    measurements: List<Measurement>,
    navController: NavController,
    customerId: Long,
    viewModel: CustomerViewModel
) {
    if (measurements.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Straighten,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No measurements yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(measurements) { measurement ->
                MeasurementCard(measurement, viewModel)
            }
        }
    }
}

@Composable
fun MeasurementCard(measurement: Measurement, viewModel: CustomerViewModel) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    dateFormat.format(Date(measurement.createdAt)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Upper Body", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            if(measurement.upperBodyMeasurements.isNotEmpty()) {
                Text(measurement.upperBodyMeasurements)
            } else {
                Text("No upper body measurements recorded.", style = MaterialTheme.typography.bodySmall)
            }


            Spacer(modifier = Modifier.height(8.dp))
            Text("Lower Body", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            if(measurement.lowerBodyMeasurements.isNotEmpty()) {
                Text(measurement.lowerBodyMeasurements)
            } else {
                Text("No lower body measurements recorded.", style = MaterialTheme.typography.bodySmall)
            }


            if (measurement.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Notes: ${measurement.notes}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Measurement") },
            text = { Text("Are you sure you want to delete this measurement?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMeasurement(measurement)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun OrdersTab(orders: List<Order>, navController: NavController, viewModel: OrderViewModel) {
    if (orders.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No orders yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(orders) { order ->
                OrderCardSmall(order, navController, viewModel)
            }
        }
    }
}

@Composable
fun OrderCardSmall(order: Order, navController: NavController, viewModel: OrderViewModel) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    order.itemType,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Due: ${dateFormat.format(Date(order.dueDate))}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "₹${order.price} (Advance: ₹${order.advancePaid})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            AssistChip(
                onClick = { },
                label = { Text(order.status.name) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = when (order.status) {
                        com.example.tailorrecords.data.models.OrderStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                        com.example.tailorrecords.data.models.OrderStatus.DELIVERED -> MaterialTheme.colorScheme.tertiaryContainer
                        com.example.tailorrecords.data.models.OrderStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            )
        }
    }
}

