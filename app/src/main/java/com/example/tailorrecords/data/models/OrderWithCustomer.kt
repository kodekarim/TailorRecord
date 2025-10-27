package com.example.tailorrecords.data.models

import androidx.room.Embedded
import androidx.room.Relation

data class OrderWithCustomer(
    @Embedded val order: Order,
    @Relation(
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: Customer
)
