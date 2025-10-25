package com.example.tailorrecords.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tailorrecords.data.TailorDatabase
import com.example.tailorrecords.data.repository.TailorRepository
import com.example.tailorrecords.utils.BackupData
import com.example.tailorrecords.utils.DataExportImport
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TailorRepository

    init {
        val database = TailorDatabase.getDatabase(application)
        repository = TailorRepository(
            database.customerDao(),
            database.measurementDao(),
            database.orderDao()
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
}

