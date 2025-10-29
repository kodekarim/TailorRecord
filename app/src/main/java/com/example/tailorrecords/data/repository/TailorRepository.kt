package com.example.tailorrecords.data.repository

import com.example.tailorrecords.data.dao.CustomerDao
import com.example.tailorrecords.data.dao.MeasurementDao
import com.example.tailorrecords.data.dao.OrderDao
import com.example.tailorrecords.data.dao.MeasurementFieldDao
import com.example.tailorrecords.data.models.Customer
import com.example.tailorrecords.data.models.Measurement
import com.example.tailorrecords.data.models.Order
import com.example.tailorrecords.data.models.OrderStatus
import com.example.tailorrecords.data.models.OrderWithCustomer
import com.example.tailorrecords.data.models.MeasurementField
import kotlinx.coroutines.flow.Flow

class TailorRepository(
    private val customerDao: CustomerDao,
    private val measurementDao: MeasurementDao,
    private val orderDao: OrderDao,
    private val measurementFieldDao: MeasurementFieldDao
) {
    // Customer operations
    fun getAllCustomers(): Flow<List<Customer>> = customerDao.getAllCustomers()
    
    fun getCustomerById(customerId: Long): Flow<Customer?> = customerDao.getCustomerById(customerId)
    
    fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.searchCustomers(query)

    suspend fun getCustomerByPhoneNumber(phoneNumber: String): Customer? = customerDao.getCustomerByPhoneNumber(phoneNumber)
    
    suspend fun insertCustomer(customer: Customer): Long = customerDao.insertCustomer(customer)
    
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    
    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)
    
    suspend fun deleteCustomerById(customerId: Long) = customerDao.deleteCustomerById(customerId)
    
    // Measurement operations
    fun getMeasurementsByCustomerId(customerId: Long): Flow<List<Measurement>> = 
        measurementDao.getMeasurementsByCustomerId(customerId)
    
    fun getMeasurementById(measurementId: Long): Flow<Measurement?> = 
        measurementDao.getMeasurementById(measurementId)
    
    suspend fun insertMeasurement(measurement: Measurement): Long = 
        measurementDao.insertMeasurement(measurement)
    
    suspend fun updateMeasurement(measurement: Measurement) = 
        measurementDao.updateMeasurement(measurement)
    
    suspend fun deleteMeasurement(measurement: Measurement) = 
        measurementDao.deleteMeasurement(measurement)
    
    // Measurement Field operations
    fun getAllMeasurementFields(): Flow<List<MeasurementField>> =
        measurementFieldDao.getAllMeasurementFields()

    suspend fun insertMeasurementField(field: MeasurementField) =
        measurementFieldDao.insertMeasurementField(field)
    
    suspend fun updateMeasurementFields(fields: List<MeasurementField>) =
        measurementFieldDao.updateMeasurementFields(fields)

    suspend fun deleteMeasurementField(field: MeasurementField) =
        measurementFieldDao.deleteMeasurementField(field)
    
    // Order operations
    fun getAllOrdersWithCustomers(): Flow<List<OrderWithCustomer>> = orderDao.getAllOrdersWithCustomers()
    
    fun getOrdersWithCustomersByStatus(status: OrderStatus): Flow<List<OrderWithCustomer>> =
        orderDao.getOrdersWithCustomersByStatus(status)

    fun getOrdersByCustomerId(customerId: Long): Flow<List<Order>> = 
        orderDao.getOrdersByCustomerId(customerId)
    
    fun getOrdersByStatus(status: OrderStatus): Flow<List<Order>> = 
        orderDao.getOrdersByStatus(status)
    
    fun getOrderById(orderId: Long): Flow<Order?> = orderDao.getOrderById(orderId)

    fun getOrderWithCustomerById(orderId: Long): Flow<OrderWithCustomer?> =
        orderDao.getOrderWithCustomerById(orderId)
    
    suspend fun insertOrder(order: Order): Long = orderDao.insertOrder(order)
    
    suspend fun updateOrder(order: Order) = orderDao.updateOrder(order)
    
    suspend fun deleteOrder(order: Order) = orderDao.deleteOrder(order)
}

