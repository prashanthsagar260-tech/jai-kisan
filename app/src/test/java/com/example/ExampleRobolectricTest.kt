package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.MandiPriceEntity
import com.example.data.model.WeatherCacheEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Jai Kisan", appName)
    }

    @Test
    fun `test Room offline caching for mandi prices`() = runBlocking {
        val samplePrice = MandiPriceEntity(
            id = "test_chilli",
            cropId = "chilli",
            cropNameTe = "ఎండు మిర్చి",
            cropNameEn = "Dry Chilli",
            marketNameTe = "వరంగల్ యార్డ్",
            marketNameEn = "Warangal Yard",
            district = "Warangal",
            minPrice = 18000,
            maxPrice = 22000,
            modalPrice = 20000,
            dailyArrivalsTons = 400,
            priceChange = 150,
            history7DaysCsv = "19000,19500,20000",
            history30DaysCsv = "18000,19000,20000",
            predictedPriceNextWeek = 20500,
            predictionConfidencePercent = 85,
            isPerishable = true,
            iconEmoji = "🌶️",
            lastUpdated = 1700000000000L
        )

        db.mandiPriceDao().insertPrices(listOf(samplePrice))

        val cachedPrices = db.mandiPriceDao().getAllPrices().first()
        assertEquals(1, cachedPrices.size)
        assertEquals("test_chilli", cachedPrices.first().id)
        assertEquals(20000, cachedPrices.first().modalPrice)
    }

    @Test
    fun `test Room offline caching for weather advisory`() = runBlocking {
        val sampleWeather = WeatherCacheEntity(
            id = "primary_weather",
            locationTe = "వరంగల్",
            locationEn = "Warangal",
            tempCelsius = 32,
            humidityPercent = 65,
            conditionTe = "తేలికపాటి ఎండ",
            conditionEn = "Sunny",
            rainProbabilityPercent = 20,
            windSpeedKmh = 12,
            advisoryTe = "పంట కోతకు అనుకూలం",
            advisoryEn = "Good for harvest",
            dataSource = "IMD",
            lastFetchedTimestamp = 1700000000000L
        )

        db.weatherDao().saveWeatherCache(sampleWeather)

        val cachedWeather = db.weatherDao().getWeatherCache().first()
        assertNotNull(cachedWeather)
        assertEquals(32, cachedWeather?.tempCelsius)
        assertEquals("వరంగల్", cachedWeather?.locationTe)
    }

    @Test
    fun `test CropDiseaseMlPipeline diagnoses crop diseases and provides remedies`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pipeline = com.example.ml.CropDiseaseMlPipeline(context)

        // Test chilli disease detection
        val chilliResult = pipeline.analyzeCropImage(null, "మిర్చి (Chilli)")
        assertNotNull(chilliResult)
        assertEquals("మిర్చి (Chilli)", chilliResult.cropName)
        assertTrue(chilliResult.diseaseNameTe.isNotEmpty())
        assertTrue(chilliResult.organicRemedyTe.isNotEmpty())
        assertTrue(chilliResult.chemicalRemedyTe.isNotEmpty())
        assertTrue(chilliResult.confidencePercent > 80)

        // Test cotton pink bollworm detection
        val cottonResult = pipeline.analyzeCropImage(null, "పత్తి (Cotton)")
        assertNotNull(cottonResult)
        assertEquals("పత్తి (Cotton)", cottonResult.cropName)
        assertTrue(cottonResult.diseaseNameTe.contains("గులాబీ రంగు"))
        assertTrue(cottonResult.spokenSummaryTe.isNotEmpty())
    }
}
