package com.example.tailorrecords.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
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
import com.example.tailorrecords.data.models.OrderStatus
import com.example.tailorrecords.navigation.Screen
import com.example.tailorrecords.viewmodel.CustomerViewModel
import com.example.tailorrecords.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
                1 -> OrdersTab(orders, navController, orderViewModel, customer)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
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
            items(measurements, key = { it.id }) { measurement ->
                val isLatest = measurement == measurements.first()
                var showDeleteDialog by remember { mutableStateOf(false) }

                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            showDeleteDialog = true
                            false
                        } else {
                            true
                        }
                    }
                )

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete Measurement") },
                        text = { Text("Are you sure you want to delete this measurement entry?") },
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

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val color = when(dismissState.dismissDirection) {
                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                            else -> Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.scale(if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) dismissState.progress else 0f)
                            )
                        }
                    },
                    modifier = Modifier.animateItemPlacement()
                ) {
                    MeasurementCard(
                        measurement = measurement,
                        isLatest = isLatest,
                        onEditClick = {
                            navController.navigate(Screen.EditMeasurement.createRoute(customerId, measurement.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MeasurementCard(
    measurement: Measurement,
    isLatest: Boolean,
    onEditClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val measurementItems = measurement.values.toList()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLatest) {
                        Badge { Text("Latest") }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        dateFormat.format(Date(measurement.createdAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (measurementItems.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(measurementItems) { item ->
                        MeasurementItem(label = item.first, value = item.second)
                    }
                }
            } else {
                Text("No measurements recorded for this entry.", style = MaterialTheme.typography.bodySmall)
            }


            if (measurement.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Notes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(measurement.notes, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun MeasurementItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun OrdersTab(orders: List<Order>, navController: NavController, viewModel: OrderViewModel, customer: Customer?) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
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
                CustomerOrderCard(
                    order = order,
                    customer = customer,
                    onClick = { navController.navigate(Screen.EditOrder.createRoute(order.id)) },
                    onShare = {
                        coroutineScope.launch {
                            customer?.let { cust ->
                                shareOrderCard(context, order, cust)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CustomerOrderCard(
    order: Order,
    customer: Customer?,
    onClick: () -> Unit,
    onShare: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    order.itemType,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                AssistChip(
                    onClick = { },
                    label = { Text(order.status.name.replace("_", " ")) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (order.status) {
                            OrderStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                            OrderStatus.DELIVERED -> MaterialTheme.colorScheme.tertiaryContainer
                            OrderStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondaryContainer
                            OrderStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Due: ${dateFormat.format(Date(order.dueDate))}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "₹${order.price} (Adv: ₹${order.advancePaid})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            
            // Share button
            Button(
                onClick = onShare,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Order Card")
            }
        }
    }
}

