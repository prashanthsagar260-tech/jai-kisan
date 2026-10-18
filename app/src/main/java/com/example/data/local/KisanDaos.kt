package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ColdStorageEntity
import com.example.data.model.CropCatalogEntity
import com.example.data.model.CropPhotoEntity
import com.example.data.model.MandiLocationEntity
import com.example.data.model.MandiPriceEntity
import com.example.data.model.WeatherCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MandiPriceDao {
    @Query("SELECT * FROM mandi_prices ORDER BY modalPrice DESC")
    fun getAllPrices(): Flow<List<MandiPriceEntity>>

    @Query("SELECT * FROM mandi_prices WHERE id = :id LIMIT 1")
    suspend fun getPriceById(id: String): MandiPriceEntity?

    @Query("SELECT * FROM mandi_prices WHERE cropNameTe LIKE '%' || :query || '%' OR cropNameEn LIKE '%' || :query || '%' OR marketNameTe LIKE '%' || :query || '%' OR marketNameEn LIKE '%' || :query || '%'")
    fun searchPrices(query: String): Flow<List<MandiPriceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrices(prices: List<MandiPriceEntity>)

    @Query("SELECT COUNT(*) FROM mandi_prices")
    suspend fun getCount(): Int

    @Query("SELECT MAX(lastUpdated) FROM mandi_prices")
    fun getLatestPriceTimestamp(): Flow<Long?>
}

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_cache WHERE id = 'primary_weather' LIMIT 1")
    fun getWeatherCache(): Flow<WeatherCacheEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWeatherCache(weather: WeatherCacheEntity)

    @Query("SELECT COUNT(*) FROM weather_cache")
    suspend fun getCount(): Int
}

@Dao
interface MandiLocationDao {
    @Query("SELECT * FROM mandi_locations ORDER BY distanceKm ASC")
    fun getAllLocations(): Flow<List<MandiLocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<MandiLocationEntity>)

    @Query("SELECT COUNT(*) FROM mandi_locations")
    suspend fun getCount(): Int
}

@Dao
interface CropCatalogDao {
    @Query("SELECT * FROM crop_catalog ORDER BY nameEn ASC")
    fun getAllCrops(): Flow<List<CropCatalogEntity>>

    @Query("SELECT * FROM crop_catalog WHERE nameTe LIKE '%' || :query || '%' OR nameEn LIKE '%' || :query || '%'")
    fun searchCrops(query: String): Flow<List<CropCatalogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrops(crops: List<CropCatalogEntity>)

    @Query("SELECT COUNT(*) FROM crop_catalog")
    suspend fun getCount(): Int
}

@Dao
interface ColdStorageDao {
    @Query("SELECT * FROM cold_storages ORDER BY distanceKm ASC")
    fun getAllColdStorages(): Flow<List<ColdStorageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColdStorages(storages: List<ColdStorageEntity>)

    @Query("SELECT COUNT(*) FROM cold_storages")
    suspend fun getCount(): Int
}

@Dao
interface CropPhotoDao {
    @Query("SELECT * FROM crop_photos ORDER BY timestamp DESC")
    fun getAllPhotos(): Flow<List<CropPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: CropPhotoEntity): Long

    @Query("DELETE FROM crop_photos WHERE id = :id")
    suspend fun deletePhoto(id: Long)
}
