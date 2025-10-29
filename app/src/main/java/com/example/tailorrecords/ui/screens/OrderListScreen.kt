package com.example.tailorrecords.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.tailorrecords.data.models.Order
import com.example.tailorrecords.data.models.OrderStatus
import com.example.tailorrecords.data.models.OrderWithCustomer
import com.example.tailorrecords.navigation.Screen
import com.example.tailorrecords.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    navController: NavController,
    viewModel: OrderViewModel = viewModel()
) {
    val ordersWithCustomers by viewModel.ordersWithCustomers.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orders") },
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
        ) {
            // Status filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedStatus == null,
                        onClick = { viewModel.filterByStatus(null) },
                        label = { Text("All") }
                    )
                }
                items(OrderStatus.values()) { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { viewModel.filterByStatus(status) },
                        label = { Text(status.name.replace("_", " ")) }
                    )
                }
            }

            // Order list
            if (ordersWithCustomers.isEmpty()) {
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
                            "No orders found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ordersWithCustomers) { orderWithCustomer ->
                        OrderCard(
                            orderWithCustomer = orderWithCustomer,
                            onClick = {
                                // For now, open bottom sheet
                                selectedOrder = orderWithCustomer.order
                                showBottomSheet = true
                            },
                            onStatusChange = { newStatus ->
                                viewModel.updateOrderStatus(orderWithCustomer.order, newStatus)
                            },
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        // Bottom sheet for order actions
        if (showBottomSheet && selectedOrder != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        selectedOrder?.itemType ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    ListItem(
                        headlineContent = { Text("Edit Order") },
                        leadingContent = { Icon(Icons.Default.Edit, contentDescription = "Edit") },
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.EditOrder.createRoute(selectedOrder!!.id))
                            showBottomSheet = false
                        }
                    )

                    ListItem(
                        headlineContent = { Text("Mark as In Progress") },
                        leadingContent = { Icon(Icons.Default.HourglassEmpty, contentDescription = null) },
                        modifier = Modifier.clickable {
                            viewModel.updateOrderStatus(selectedOrder!!, OrderStatus.IN_PROGRESS)
                            showBottomSheet = false
                        }
                    )
                    
                    ListItem(
                        headlineContent = { Text("Mark as Completed") },
                        leadingContent = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                        modifier = Modifier.clickable {
                            viewModel.updateOrderStatus(selectedOrder!!, OrderStatus.COMPLETED)
                            showBottomSheet = false
                        }
                    )
                    
                    ListItem(
                        headlineContent = { Text("Mark as Delivered") },
                        leadingContent = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
                        modifier = Modifier.clickable {
                            viewModel.updateOrderStatus(selectedOrder!!, OrderStatus.DELIVERED)
                            showBottomSheet = false
                        }
                    )
                    
                    ListItem(
                        headlineContent = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingContent = { 
                            Icon(
                                Icons.Default.Delete, 
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            ) 
                        },
                        modifier = Modifier.clickable {
                            viewModel.deleteOrder(selectedOrder!!)
                            showBottomSheet = false
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    orderWithCustomer: OrderWithCustomer,
    onClick: () -> Unit,
    onStatusChange: (OrderStatus) -> Unit,
    viewModel: OrderViewModel
) {
    val order = orderWithCustomer.order
    val customer = orderWithCustomer.customer
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val remainingBalance = viewModel.getRemainingBalance(order)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Customer photo
                if (customer.photoUri.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(Uri.parse(customer.photoUri)),
                        contentDescription = customer.name,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = customer.name,
                        modifier = Modifier.size(40.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                // Customer and Item info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.itemType,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                
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

            if (order.orderNumber.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Order #${order.orderNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Order Date:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        dateFormat.format(Date(order.orderDate)),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Due Date:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        dateFormat.format(Date(order.dueDate)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (order.dueDate < System.currentTimeMillis() && 
                                   order.status != OrderStatus.COMPLETED && 
                                   order.status != OrderStatus.DELIVERED) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Divider()

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Total:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "₹${order.price}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Advance:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "₹${order.advancePaid}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Balance:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "₹$remainingBalance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (remainingBalance > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

