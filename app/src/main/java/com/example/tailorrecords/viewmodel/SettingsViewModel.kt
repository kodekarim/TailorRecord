package com.example.tailorrecords.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tailorrecords.data.TailorDatabase
import com.example.tailorrecords.data.models.MeasurementField
import com.example.tailorrecords.data.repository.TailorRepository
import com.example.tailorrecords.utils.DataExportImport
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TailorRepository
    val measurementFields: StateFlow<List<MeasurementField>>

    init {
        val database = TailorDatabase.getDatabase(application)
        repository = TailorRepository(
            database.customerDao(),
            database.measurementDao(),
            database.orderDao(),
            database.measurementFieldDao()
        )

        measurementFields = repository.getAllMeasurementFields().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun exportData(file: File, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val customers = repository.getAllCustomers().first()
                val measurements = mutableListOf<com.example.tailorrecords.data.models.Measurement>()
                val orders = repository.getAllOrdersWithCustomers().first().map { it.order }

                // Get all measurements for all customers
                customers.forEach { customer ->
                    val customerMeasurements = repository.getMeasurementsByCustomerId(customer.id).first()
                    measurements.addAll(customerMeasurements)
                }

                val success = DataExportImport.exportToJson(customers, measurements, orders, file)
                onComplete(success)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun importData(file: File, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val backupData = DataExportImport.importFromJson(file)
                if (backupData != null) {
                    // Insert all data
                    backupData.customers.forEach { customer ->
                        repository.insertCustomer(customer)
                    }
                    backupData.measurements.forEach { measurement ->
                        repository.insertMeasurement(measurement)
                    }
                    backupData.orders.forEach { order ->
                        repository.insertOrder(order)
                    }
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun addMeasurementField(name: String, category: String) {
        if (name.isBlank() || category.isBlank()) return
        viewModelScope.launch {
            val currentFields = measurementFields.value.filter { it.category == category }
            val maxOrder = currentFields.maxOfOrNull { it.displayOrder } ?: -1
            val field = MeasurementField(
                name = name.trim(),
                category = category.trim(),
                displayOrder = maxOrder + 1
            )
            repository.insertMeasurementField(field)
        }
    }
    
    fun updateMeasurementFieldsOrder(fields: List<MeasurementField>) {
        viewModelScope.launch {
            repository.updateMeasurementFields(fields)
        }
    }

    fun deleteMeasurementField(field: MeasurementField) {
        viewModelScope.launch {
            repository.deleteMeasurementField(field)
        }
    }
}

