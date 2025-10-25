package com.example.tailorrecords.data.dao

import androidx.room.*
import com.example.tailorrecords.data.models.Order
import com.example.tailorrecords.data.models.OrderStatus
import com.example.tailorrecords.data.models.OrderWithCustomer
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Transaction
    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    fun getAllOrdersWithCustomers(): Flow<List<OrderWithCustomer>>

    @Transaction
    @Query("SELECT * FROM orders WHERE status = :status ORDER BY orderDate DESC")
    fun getOrdersWithCustomersByStatus(status: OrderStatus): Flow<List<OrderWithCustomer>>
    
    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY orderDate DESC")
    fun getOrdersByCustomerId(customerId: Long): Flow<List<Order>>
    
    @Query("SELECT * FROM orders WHERE status = :status ORDER BY orderDate DESC")
    fun getOrdersByStatus(status: OrderStatus): Flow<List<Order>>
    
    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderById(orderId: Long): Flow<Order?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long
    
    @Update
    suspend fun updateOrder(order: Order)
    
    @Delete
    suspend fun deleteOrder(order: Order)
    
    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrderById(orderId: Long)
}

