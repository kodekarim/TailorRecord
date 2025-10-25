package com.example.tailorrecords.utils

import android.content.Context
import com.example.tailorrecords.data.models.Customer
import com.example.tailorrecords.data.models.Measurement
import com.example.tailorrecords.data.models.Order
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileWriter
import java.io.FileReader

data class BackupData(
    val customers: List<Customer>,
    val measurements: List<Measurement>,
    val orders: List<Order>,
    val backupDate: Long = System.currentTimeMillis()
)

object DataExportImport {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun exportToJson(
        customers: List<Customer>,
        measurements: List<Measurement>,
        orders: List<Order>,
        file: File
    ): Boolean {
        return try {
            val backupData = BackupData(customers, measurements, orders)
            val json = gson.toJson(backupData)
            FileWriter(file).use { writer ->
                writer.write(json)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importFromJson(file: File): BackupData? {
        return try {
            FileReader(file).use { reader ->
                gson.fromJson(reader, BackupData::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getExportFileName(): String {
        val timestamp = System.currentTimeMillis()
        return "tailor_records_backup_$timestamp.json"
    }
}

