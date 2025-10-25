package com.example.tailorrecords.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val photoUri: String = "", // URI to the customer's photo
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

