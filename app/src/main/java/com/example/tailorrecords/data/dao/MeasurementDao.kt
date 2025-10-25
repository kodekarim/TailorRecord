package com.example.tailorrecords.data.dao

import androidx.room.*
import com.example.tailorrecords.data.models.Measurement
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getMeasurementsByCustomerId(customerId: Long): Flow<List<Measurement>>
    
    @Query("SELECT * FROM measurements WHERE id = :measurementId")
    fun getMeasurementById(measurementId: Long): Flow<Measurement?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurement(measurement: Measurement): Long
    
    @Update
    suspend fun updateMeasurement(measurement: Measurement)
    
    @Delete
    suspend fun deleteMeasurement(measurement: Measurement)
    
    @Query("DELETE FROM measurements WHERE id = :measurementId")
    suspend fun deleteMeasurementById(measurementId: Long)
}

