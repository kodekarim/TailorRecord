package com.example.tailorrecords.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId")]
)
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val orderNumber: String = "", // Custom order number (e.g., "ORD-001")
    val itemType: String, // Shirt, Trouser, Suit, etc.
    val quantity: Int = 1,
    val price: Double = 0.0,
    val advancePaid: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val orderDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000), // 7 days from now
    val completedDate: Long? = null,
    val notes: String = ""
)

enum class OrderStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    DELIVERED,
    CANCELLED
}

