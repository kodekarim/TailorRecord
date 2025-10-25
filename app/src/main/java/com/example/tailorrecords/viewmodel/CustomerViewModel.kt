package com.example.tailorrecords.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tailorrecords.data.TailorDatabase
import com.example.tailorrecords.data.models.Customer
import com.example.tailorrecords.data.models.Measurement
import com.example.tailorrecords.data.repository.TailorRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CustomerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TailorRepository
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    val customers: StateFlow<List<Customer>>
    
    init {
        val database = TailorDatabase.getDatabase(application)
        repository = TailorRepository(
            database.customerDao(),
            database.measurementDao(),
            database.orderDao()
        )
        
        customers = searchQuery.flatMapLatest { query ->
            if (query.isEmpty()) {
                repository.getAllCustomers()
            } else {
                repository.searchCustomers(query)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun getCustomerById(customerId: Long): Flow<Customer?> {
        return repository.getCustomerById(customerId)
    }

    fun getMeasurementsByCustomerId(customerId: Long): Flow<List<Measurement>> {
        return repository.getMeasurementsByCustomerId(customerId)
    }

    suspend fun findCustomerByPhone(phoneNumber: String): Customer? {
        if (phoneNumber.isBlank()) return null
        return repository.getCustomerByPhoneNumber(phoneNumber)
    }
    
    fun insertCustomer(customer: Customer, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertCustomer(customer)
            onComplete(id)
        }
    }
    
    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }
    
    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }
    
    fun insertMeasurement(measurement: Measurement, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertMeasurement(measurement)
            onComplete()
        }
    }
    
    fun updateMeasurement(measurement: Measurement) {
        viewModelScope.launch {
            repository.updateMeasurement(measurement)
        }
    }
    
    fun deleteMeasurement(measurement: Measurement) {
        viewModelScope.launch {
            repository.deleteMeasurement(measurement)
        }
    }
}

