package com.example.tailorrecords.data.dao

import androidx.room.*
import com.example.tailorrecords.data.models.MeasurementField
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementFieldDao {
    @Query("SELECT * FROM measurement_fields ORDER BY category, name")
    fun getAllMeasurementFields(): Flow<List<MeasurementField>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeasurementField(field: MeasurementField)

    @Delete
    suspend fun deleteMeasurementField(field: MeasurementField)
}
