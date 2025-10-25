package com.example.tailorrecords.data.dao

import androidx.room.*
import com.example.tailorrecords.data.models.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>
    
    @Query("SELECT * FROM customers WHERE id = :customerId")
    fun getCustomerById(customerId: Long): Flow<Customer?>
    
    @Query("SELECT * FROM customers WHERE name LIKE '%' || :searchQuery || '%' OR phoneNumber LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun searchCustomers(searchQuery: String): Flow<List<Customer>>
    
    @Query("SELECT * FROM customers WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getCustomerByPhoneNumber(phoneNumber: String): Customer?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long
    
    @Update
    suspend fun updateCustomer(customer: Customer)
    
    @Delete
    suspend fun deleteCustomer(customer: Customer)
    
    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteCustomerById(customerId: Long)
}

