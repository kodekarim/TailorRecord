package com.example.tailorrecords.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tailorrecords.data.dao.CustomerDao
import com.example.tailorrecords.data.dao.MeasurementDao
import com.example.tailorrecords.data.dao.OrderDao
import com.example.tailorrecords.data.dao.MeasurementFieldDao
import com.example.tailorrecords.data.models.Customer
import com.example.tailorrecords.data.models.Measurement
import com.example.tailorrecords.data.models.MeasurementField
import com.example.tailorrecords.data.models.Order

@Database(
    entities = [Customer::class, Measurement::class, Order::class, MeasurementField::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TailorDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun orderDao(): OrderDao
    abstract fun measurementFieldDao(): MeasurementFieldDao

    companion object {
        @Volatile
        private var INSTANCE: TailorDatabase? = null

        fun getDatabase(context: Context): TailorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TailorDatabase::class.java,
                    "tailor_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

