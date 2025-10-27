package com.example.tailorrecords.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurement_fields")
data class MeasurementField(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // e.g., "Upper Body", "Lower Body"
)
