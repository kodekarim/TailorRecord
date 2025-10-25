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
import com.example.tailorrecords.data.models.Customer
import com.example.tailorrecords.data.models.Measurement
import com.example.tailorrecords.data.models.Order

@Database(
    entities = [Customer::class, Measurement::class, Order::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TailorDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun measurementDao(): MeasurementDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: TailorDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create a new table with the new schema
                db.execSQL("""
                    CREATE TABLE measurements_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        customerId INTEGER NOT NULL,
                        upperBodyMeasurements TEXT NOT NULL DEFAULT '',
                        lowerBodyMeasurements TEXT NOT NULL DEFAULT '',
                        notes TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0,
                        updatedAt INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(customerId) REFERENCES customers(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())

                // Copy the data
                // This is a simple migration that doesn't preserve old measurement data
                // as the fields have been completely replaced.
                // For a real-world app, you might want to concatenate old fields into the new ones.

                // Drop the old table
                db.execSQL("DROP TABLE measurements")

                // Rename the new table
                db.execSQL("ALTER TABLE measurements_new RENAME TO measurements")

                // Recreate the index
                db.execSQL("CREATE INDEX IF NOT EXISTS index_measurements_customerId ON measurements(customerId)")
            }
        }

        fun getDatabase(context: Context): TailorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TailorDatabase::class.java,
                    "tailor_database"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

