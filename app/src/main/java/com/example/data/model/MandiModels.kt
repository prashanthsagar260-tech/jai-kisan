package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mandi_prices")
data class MandiPriceEntity(
    @PrimaryKey
    val id: String,
    val cropId: String,
    val cropNameTe: String,
    val cropNameEn: String,
    val marketNameTe: String,
    val marketNameEn: String,
    val district: String,
    val minPrice: Int,
    val maxPrice: Int,
    val modalPrice: Int,
    val dailyArrivalsTons: Int,
    val priceChange: Int, // e.g. +120 or -80
    val history7DaysCsv: String,
    val history30DaysCsv: String,
    val predictedPriceNextWeek: Int,
    val predictionConfidencePercent: Int,
    val isPerishable: Boolean,
    val iconEmoji: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "mandi_locations")
data class MandiLocationEntity(
    @PrimaryKey
    val id: String,
    val nameTe: String,
    val nameEn: String,
    val district: String,
    val distanceKm: Double,
    val contactPhone: String,
    val operatingHours: String,
    val latitude: Double,
    val longitude: Double,
    val hasElectronicWeighbridge: Boolean
)

@Entity(tableName = "crop_catalog")
data class CropCatalogEntity(
    @PrimaryKey
    val cropId: String,
    val nameTe: String,
    val nameEn: String,
    val seasonTe: String,
    val seasonEn: String,
    val typicalPriceRange: String,
    val localizedZoneTe: String,
    val localizedZoneEn: String,
    val plantingTimeTe: String,
    val plantingTimeEn: String,
    val soilPreparationTe: String,
    val soilPreparationEn: String,
    val irrigationNeedsTe: String,
    val irrigationNeedsEn: String,
    val fertilizerScheduleTe: String,
    val fertilizerScheduleEn: String,
    val commonPestsTe: String,
    val commonPestsEn: String,
    val organicTreatmentTe: String,
    val organicTreatmentEn: String,
    val chemicalTreatmentTe: String,
    val chemicalTreatmentEn: String,
    val coldStorageRecommended: Boolean,
    val harvestAdviceTe: String,
    val harvestAdviceEn: String,
    val iconEmoji: String
)

@Entity(tableName = "cold_storages")
data class ColdStorageEntity(
    @PrimaryKey
    val id: String,
    val nameTe: String,
    val nameEn: String,
    val locationTe: String,
    val locationEn: String,
    val distanceKm: Double,
    val totalCapacityMt: Int,
    val availableCapacityMt: Int,
    val ratePerQuintalMonth: Int,
    val tempRange: String,
    val contactPhone: String,
    val suitableCropsTe: String,
    val suitableCropsEn: String
)

@Entity(tableName = "crop_photos")
data class CropPhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cropName: String,
    val timestamp: Long,
    val imagePathOrUri: String,
    val fileSizeBytes: Long,
    val gradeTag: String,
    val notes: String,
    val detectedDiseaseTe: String? = null,
    val detectedDiseaseEn: String? = null,
    val severityLevel: String? = null,
    val confidencePercent: Int? = null,
    val remedyOrganicTe: String? = null,
    val remedyOrganicEn: String? = null,
    val remedyChemicalTe: String? = null,
    val remedyChemicalEn: String? = null
)

data class TransportVehicleOption(
    val id: String,
    val nameTe: String,
    val nameEn: String,
    val capacityQuintals: Int,
    val baseFare: Int,
    val perKmRate: Int,
    val icon: String,
    val bestForTe: String,
    val bestForEn: String
)

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey
    val id: String = "primary_weather",
    val locationTe: String,
    val locationEn: String,
    val tempCelsius: Int,
    val humidityPercent: Int,
    val conditionTe: String,
    val conditionEn: String,
    val rainProbabilityPercent: Int,
    val windSpeedKmh: Int,
    val advisoryTe: String,
    val advisoryEn: String,
    val dataSource: String = "IMD & Govt Agri-Met Department (అధికారిక వాతావరణ శాఖ)",
    val lastFetchedTimestamp: Long = System.currentTimeMillis()
)

data class WeatherAdvisoryData(
    val locationTe: String,
    val locationEn: String,
    val tempCelsius: Int,
    val humidityPercent: Int,
    val conditionTe: String,
    val conditionEn: String,
    val rainProbabilityPercent: Int,
    val windSpeedKmh: Int,
    val advisoryTe: String,
    val advisoryEn: String,
    val dataSource: String = "IMD & Govt Agri-Met Department (అధికారిక వాతావరణ శాఖ)",
    val lastFetchedTimestamp: Long = System.currentTimeMillis()
)
