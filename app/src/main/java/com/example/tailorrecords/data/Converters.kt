package com.example.tailorrecords.data

import androidx.room.TypeConverter
import com.example.tailorrecords.data.models.OrderStatus

class Converters {
    @TypeConverter
    fun fromOrderStatus(status: OrderStatus): String {
        return status.name
    }

    @TypeConverter
    fun toOrderStatus(status: String): OrderStatus {
        return OrderStatus.valueOf(status)
    }
}

