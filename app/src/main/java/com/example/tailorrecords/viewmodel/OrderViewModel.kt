package com.example.tailorrecords.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tailorrecords.data.TailorDatabase
import com.example.tailorrecords.data.models.Order
import com.example.tailorrecords.data.models.OrderStatus
import com.example.tailorrecords.data.models.OrderWithCustomer
import com.example.tailorrecords.data.repository.TailorRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OrderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TailorRepository
    
    private val _selectedStatus = MutableStateFlow<OrderStatus?>(null)
    val selectedStatus: StateFlow<OrderStatus?> = _selectedStatus.asStateFlow()
    
    val ordersWithCustomers: StateFlow<List<OrderWithCustomer>>
    
    init {
        val database = TailorDatabase.getDatabase(application)
        repository = TailorRepository(
            database.customerDao(),
            database.measurementDao(),
            database.orderDao()
        )
        
        ordersWithCustomers = selectedStatus.flatMapLatest { status ->
            if (status == null) {
                repository.getAllOrdersWithCustomers()
            } else {
                repository.getOrdersWithCustomersByStatus(status)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
    
    fun filterByStatus(status: OrderStatus?) {
        _selectedStatus.value = status
    }
    
    fun getOrdersByCustomerId(customerId: Long): Flow<List<Order>> {
        return repository.getOrdersByCustomerId(customerId)
    }
    
    fun getOrderById(orderId: Long): Flow<Order?> {
        return repository.getOrderById(orderId)
    }
    
    fun insertOrder(order: Order, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertOrder(order)
            onComplete(id)
        }
    }
    
    fun updateOrder(order: Order) {
        viewModelScope.launch {
            repository.updateOrder(order)
        }
    }
    
    fun updateOrderStatus(order: Order, newStatus: OrderStatus) {
        viewModelScope.launch {
            val updatedOrder = order.copy(
                status = newStatus,
                completedDate = if (newStatus == OrderStatus.COMPLETED || newStatus == OrderStatus.DELIVERED) {
                    System.currentTimeMillis()
                } else {
                    order.completedDate
                }
            )
            repository.updateOrder(updatedOrder)
        }
    }
    
    fun deleteOrder(order: Order) {
        viewModelScope.launch {
            repository.deleteOrder(order)
        }
    }
    
    fun getRemainingBalance(order: Order): Double {
        return order.price - order.advancePaid
    }
}

