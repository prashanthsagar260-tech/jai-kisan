package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ColdStorageEntity
import com.example.data.model.CropCatalogEntity
import com.example.data.model.CropPhotoEntity
import com.example.data.model.MandiLocationEntity
import com.example.data.model.MandiPriceEntity
import com.example.data.model.WeatherCacheEntity

@Database(
    entities = [
        MandiPriceEntity::class,
        MandiLocationEntity::class,
        CropCatalogEntity::class,
        ColdStorageEntity::class,
        CropPhotoEntity::class,
        WeatherCacheEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mandiPriceDao(): MandiPriceDao
    abstract fun mandiLocationDao(): MandiLocationDao
    abstract fun cropCatalogDao(): CropCatalogDao
    abstract fun coldStorageDao(): ColdStorageDao
    abstract fun cropPhotoDao(): CropPhotoDao
    abstract fun weatherDao(): WeatherDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jai_kisan_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
