package com.example.tailorrecords.data

import androidx.room.TypeConverter
import com.example.tailorrecords.data.models.OrderStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromOrderStatus(status: OrderStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toOrderStatus(status: String?): OrderStatus? {
        return status?.let { OrderStatus.valueOf(it) }
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringMap(value: String): Map<String, String>? {
        val mapType = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(value, mapType)
    }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String>? {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
}

