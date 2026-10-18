package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.ColdStorageEntity
import com.example.data.model.CropCatalogEntity
import com.example.data.model.CropPhotoEntity
import com.example.data.model.MandiLocationEntity
import com.example.data.model.MandiPriceEntity
import com.example.data.model.TransportVehicleOption
import com.example.data.model.WeatherAdvisoryData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class KisanRepository(private val database: AppDatabase) {
    private val priceDao = database.mandiPriceDao()
    private val locationDao = database.mandiLocationDao()
    private val cropDao = database.cropCatalogDao()
    private val coldStorageDao = database.coldStorageDao()
    private val photoDao = database.cropPhotoDao()
    private val weatherDao = database.weatherDao()

    val allPrices: Flow<List<MandiPriceEntity>> = priceDao.getAllPrices()
    val allLocations: Flow<List<MandiLocationEntity>> = locationDao.getAllLocations()
    val allCrops: Flow<List<CropCatalogEntity>> = cropDao.getAllCrops()
    val allColdStorages: Flow<List<ColdStorageEntity>> = coldStorageDao.getAllColdStorages()
    val allPhotos: Flow<List<CropPhotoEntity>> = photoDao.getAllPhotos()
    val cachedWeather: Flow<com.example.data.model.WeatherCacheEntity?> = weatherDao.getWeatherCache()
    val latestPriceTimestamp: Flow<Long?> = priceDao.getLatestPriceTimestamp()

    fun searchPrices(query: String): Flow<List<MandiPriceEntity>> {
        return priceDao.searchPrices(query)
    }

    fun searchCrops(query: String): Flow<List<CropCatalogEntity>> {
        return cropDao.searchCrops(query)
    }

    suspend fun saveCropPhoto(
        cropName: String,
        path: String,
        sizeBytes: Long,
        grade: String,
        notes: String,
        detectedDiseaseTe: String? = null,
        detectedDiseaseEn: String? = null,
        severityLevel: String? = null,
        confidencePercent: Int? = null,
        remedyOrganicTe: String? = null,
        remedyOrganicEn: String? = null,
        remedyChemicalTe: String? = null,
        remedyChemicalEn: String? = null
    ): Long {
        return withContext(Dispatchers.IO) {
            val entity = CropPhotoEntity(
                cropName = cropName,
                timestamp = System.currentTimeMillis(),
                imagePathOrUri = path,
                fileSizeBytes = sizeBytes,
                gradeTag = grade,
                notes = notes,
                detectedDiseaseTe = detectedDiseaseTe,
                detectedDiseaseEn = detectedDiseaseEn,
                severityLevel = severityLevel,
                confidencePercent = confidencePercent,
                remedyOrganicTe = remedyOrganicTe,
                remedyOrganicEn = remedyOrganicEn,
                remedyChemicalTe = remedyChemicalTe,
                remedyChemicalEn = remedyChemicalEn
            )
            photoDao.insertPhoto(entity)
        }
    }

    suspend fun deleteCropPhoto(id: Long) {
        withContext(Dispatchers.IO) {
            photoDao.deletePhoto(id)
        }
    }

    fun getTransportOptions(): List<TransportVehicleOption> {
        return listOf(
            TransportVehicleOption(
                id = "auto",
                nameTe = "షేరింగ్ ఆటో / మినీ వాన్",
                nameEn = "Shared Auto / Mini Cargo",
                capacityQuintals = 15,
                baseFare = 350,
                perKmRate = 14,
                icon = "🛺",
                bestForTe = "చిన్న పరిమాణాల కూరగాయలు మరియు తక్షణ అమ్మకాలకు అనుకూలం",
                bestForEn = "Best for small harvests and perishable local delivery"
            ),
            TransportVehicleOption(
                id = "pickup",
                nameTe = "స్థానిక పికప్ ట్రక్ (బోలేరో / డోస్త్)",
                nameEn = "Local Pickup Truck (2-3 Tonne)",
                capacityQuintals = 35,
                baseFare = 850,
                perKmRate = 22,
                icon = "🛻",
                bestForTe = "మధ్యస్థ పరిమాణాల మిర్చి, పత్తి మరియు ధాన్యం బస్తాలకు ఉత్తమం",
                bestForEn = "Ideal for medium lots of chilli, cotton and paddy bags"
            ),
            TransportVehicleOption(
                id = "lorry",
                nameTe = "సహకార సొసైటీ లారీ (10-14 టన్నులు)",
                nameEn = "Cooperative Heavy Lorry (10T+)",
                capacityQuintals = 120,
                baseFare = 2400,
                perKmRate = 42,
                icon = "🚛",
                bestForTe = "రైతు సంఘాలు లేదా పెద్ద పరిమాణాల ధాన్యం రవాణాకు తక్కువ ఖర్చు",
                bestForEn = "Cost-effective bulk transport for farmer cooperatives"
            )
        )
    }

    fun getLatestWeatherAdvisory(): WeatherAdvisoryData {
        return WeatherAdvisoryData(
            locationTe = "వరంగల్ & ఖమ్మం వ్యవసాయ మండల ప్రాంతం",
            locationEn = "Warangal & Khammam Agri Cluster",
            tempCelsius = 31,
            humidityPercent = 68,
            conditionTe = "తేలికపాటి మేఘావృతం, సాయంత్రం జల్లులు",
            conditionEn = "Partly Cloudy, Evening Isolated Showers",
            rainProbabilityPercent = 40,
            windSpeedKmh = 14,
            advisoryTe = "రాబోయే 36 గంటల్లో తేలికపాటి వర్ష సూచన ఉన్నందున క్రిమిసంహారక మందుల పిచికారీని 2 రోజులు వాయిదా వేయండి. కోత కోసిన ధాన్యాన్ని కళ్లాల్లో టార్పాలిన్లతో భద్రపరచండి.",
            advisoryEn = "Light scattered rain expected in 36 hrs. Postpone chemical pesticide spraying by 2 days. Cover harvested produce on drying yards with tarpaulins.",
            dataSource = "IMD & Govt Agri-Met Department (అధికారిక భారత వాతావరణ విభాగం)"
        )
    }

    suspend fun refreshMandiPricesAndWeather(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                val updatedPrices = sampleMandiPrices.map { p ->
                    p.copy(lastUpdated = now)
                }
                priceDao.insertPrices(updatedPrices)
                weatherDao.saveWeatherCache(
                    sampleWeatherCache.copy(lastFetchedTimestamp = now)
                )
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun seedInitialDataIfEmpty() {
        withContext(Dispatchers.IO) {
            if (priceDao.getCount() == 0) {
                priceDao.insertPrices(sampleMandiPrices)
            }
            if (weatherDao.getCount() == 0) {
                weatherDao.saveWeatherCache(sampleWeatherCache)
            }
            if (locationDao.getCount() == 0) {
                locationDao.insertLocations(sampleMandiLocations)
            }
            if (cropDao.getCount() == 0) {
                cropDao.insertCrops(sampleCropCatalog)
            }
            if (coldStorageDao.getCount() == 0) {
                coldStorageDao.insertColdStorages(sampleColdStorages)
            }
        }
    }

    companion object {
        val sampleWeatherCache = com.example.data.model.WeatherCacheEntity(
            id = "primary_weather",
            locationTe = "వరంగల్ & ఖమ్మం వ్యవసాయ మండల ప్రాంతం",
            locationEn = "Warangal & Khammam Agri Cluster",
            tempCelsius = 31,
            humidityPercent = 68,
            conditionTe = "తేలికపాటి మేఘావృతం, సాయంత్రం జల్లులు",
            conditionEn = "Partly Cloudy, Evening Isolated Showers",
            rainProbabilityPercent = 40,
            windSpeedKmh = 14,
            advisoryTe = "రాబోయే 36 గంటల్లో తేలికపాటి వర్ష సూచన ఉన్నందున క్రిమిసంహారక మందుల పిచికారీని 2 రోజులు వాయిదా వేయండి. కోత కోసిన ధాన్యాన్ని కళ్లాల్లో టార్పాలిన్లతో భద్రపరచండి.",
            advisoryEn = "Light scattered rain expected in 36 hrs. Postpone chemical pesticide spraying by 2 days. Cover harvested produce on drying yards with tarpaulins.",
            dataSource = "IMD & Govt Agri-Met Department (అధికారిక భారత వాతావరణ విభాగం)",
            lastFetchedTimestamp = System.currentTimeMillis()
        )
        val sampleMandiPrices = listOf(
            MandiPriceEntity(
                id = "wgl_chilli",
                cropId = "chilli",
                cropNameTe = "ఎండు మిర్చి (తేజ)",
                cropNameEn = "Dry Chilli (Teja)",
                marketNameTe = "వరంగల్ ఎనుమాముల వ్యవసాయ మార్కెట్",
                marketNameEn = "Warangal Enumamula Mandi",
                district = "Warangal",
                minPrice = 17500,
                maxPrice = 22800,
                modalPrice = 20500,
                dailyArrivalsTons = 420,
                priceChange = 350,
                history7DaysCsv = "19200,19600,19900,20100,20250,20400,20500",
                history30DaysCsv = "18200,18500,18800,19200,19500,20100,20500",
                predictedPriceNextWeek = 21200,
                predictionConfidencePercent = 86,
                isPerishable = true,
                iconEmoji = "🌶️"
            ),
            MandiPriceEntity(
                id = "wgl_cotton",
                cropId = "cotton",
                cropNameTe = "పత్తి (లాంగ్ స్టేపుల్)",
                cropNameEn = "Cotton (Long Staple)",
                marketNameTe = "వరంగల్ ఎనుమాముల వ్యవసాయ మార్కెట్",
                marketNameEn = "Warangal Enumamula Mandi",
                district = "Warangal",
                minPrice = 6900,
                maxPrice = 7650,
                modalPrice = 7380,
                dailyArrivalsTons = 680,
                priceChange = -70,
                history7DaysCsv = "7500,7480,7420,7400,7390,7380,7380",
                history30DaysCsv = "7600,7550,7500,7450,7400,7380,7380",
                predictedPriceNextWeek = 7450,
                predictionConfidencePercent = 78,
                isPerishable = false,
                iconEmoji = "☁️"
            ),
            MandiPriceEntity(
                id = "khammam_chilli",
                cropId = "chilli",
                cropNameTe = "మిర్చి (డీడీ / 334)",
                cropNameEn = "Chilli (DD / 334)",
                marketNameTe = "ఖమ్మం వ్యవసాయ మార్కెట్",
                marketNameEn = "Khammam Mandi",
                district = "Khammam",
                minPrice = 16800,
                maxPrice = 21200,
                modalPrice = 19400,
                dailyArrivalsTons = 310,
                priceChange = 220,
                history7DaysCsv = "18800,18900,19100,19200,19300,19350,19400",
                history30DaysCsv = "17900,18200,18500,18900,19100,19300,19400",
                predictedPriceNextWeek = 19900,
                predictionConfidencePercent = 84,
                isPerishable = true,
                iconEmoji = "🌶️"
            ),
            MandiPriceEntity(
                id = "guntur_chilli",
                cropId = "chilli",
                cropNameTe = "గుంటూరు సన్న మిర్చి",
                cropNameEn = "Guntur Sannam Chilli",
                marketNameTe = "గుంటూరు మిర్చి యార్డ్",
                marketNameEn = "Guntur Chilli Yard",
                district = "Guntur",
                minPrice = 18200,
                maxPrice = 23900,
                modalPrice = 21800,
                dailyArrivalsTons = 850,
                priceChange = 450,
                history7DaysCsv = "20200,20600,20900,21100,21400,21600,21800",
                history30DaysCsv = "19100,19500,20000,20600,21000,21500,21800",
                predictedPriceNextWeek = 22400,
                predictionConfidencePercent = 90,
                isPerishable = true,
                iconEmoji = "🌶️"
            ),
            MandiPriceEntity(
                id = "niz_turmeric",
                cropId = "turmeric",
                cropNameTe = "పసుపు (నిజామాబాద్ రకం)",
                cropNameEn = "Turmeric (Finger Quality)",
                marketNameTe = "నిజామాబాద్ వ్యవసాయ మార్కెట్",
                marketNameEn = "Nizamabad Mandi",
                district = "Nizamabad",
                minPrice = 12500,
                maxPrice = 16800,
                modalPrice = 15200,
                dailyArrivalsTons = 290,
                priceChange = 180,
                history7DaysCsv = "14600,14800,14900,15050,15100,15150,15200",
                history30DaysCsv = "13800,14100,14400,14700,14950,15100,15200",
                predictedPriceNextWeek = 15600,
                predictionConfidencePercent = 82,
                isPerishable = false,
                iconEmoji = "🟡"
            ),
            MandiPriceEntity(
                id = "wgl_paddy",
                cropId = "paddy",
                cropNameTe = "వరి (సోనా మసూరి - బిపిటి)",
                cropNameEn = "Paddy (Sona Masuri / BPT)",
                marketNameTe = "వరంగల్ ఎనుమాముల వ్యవసాయ మార్కెట్",
                marketNameEn = "Warangal Enumamula Mandi",
                district = "Warangal",
                minPrice = 2280,
                maxPrice = 2650,
                modalPrice = 2480,
                dailyArrivalsTons = 920,
                priceChange = 40,
                history7DaysCsv = "2420,2430,2440,2450,2460,2470,2480",
                history30DaysCsv = "2350,2380,2400,2430,2450,2470,2480",
                predictedPriceNextWeek = 2520,
                predictionConfidencePercent = 91,
                isPerishable = false,
                iconEmoji = "🌾"
            ),
            MandiPriceEntity(
                id = "kurnool_groundnut",
                cropId = "groundnut",
                cropNameTe = "వేరుశనగ (పల్లీ)",
                cropNameEn = "Groundnut (Pod)",
                marketNameTe = "కర్నూలు మార్కెట్ యార్డ్",
                marketNameEn = "Kurnool Market Yard",
                district = "Kurnool",
                minPrice = 6400,
                maxPrice = 7350,
                modalPrice = 6950,
                dailyArrivalsTons = 210,
                priceChange = -50,
                history7DaysCsv = "7050,7020,7000,6980,6970,6960,6950",
                history30DaysCsv = "7150,7100,7080,7020,6990,6970,6950",
                predictedPriceNextWeek = 7020,
                predictionConfidencePercent = 75,
                isPerishable = false,
                iconEmoji = "🥜"
            ),
            MandiPriceEntity(
                id = "hyd_tomato",
                cropId = "tomato",
                cropNameTe = "నాటు టమాటా",
                cropNameEn = "Country Tomato",
                marketNameTe = "బోయిన్‌పల్లి వ్యవసాయ మార్కెట్, హైదరాబాద్",
                marketNameEn = "Bowenpally Mandi, Hyderabad",
                district = "Hyderabad",
                minPrice = 1800,
                maxPrice = 2900,
                modalPrice = 2400,
                dailyArrivalsTons = 380,
                priceChange = 300,
                history7DaysCsv = "1900,2000,2100,2200,2250,2350,2400",
                history30DaysCsv = "1400,1600,1800,2000,2200,2300,2400",
                predictedPriceNextWeek = 2650,
                predictionConfidencePercent = 80,
                isPerishable = true,
                iconEmoji = "🍅"
            ),
            MandiPriceEntity(
                id = "khammam_maize",
                cropId = "maize",
                cropNameTe = "మొక్కజొన్న (ఎల్లో కార్న్)",
                cropNameEn = "Maize (Yellow Corn)",
                marketNameTe = "ఖమ్మం వ్యవసాయ మార్కెట్",
                marketNameEn = "Khammam Mandi",
                district = "Khammam",
                minPrice = 2050,
                maxPrice = 2380,
                modalPrice = 2240,
                dailyArrivalsTons = 480,
                priceChange = 60,
                history7DaysCsv = "2150,2170,2190,2200,2210,2230,2240",
                history30DaysCsv = "2080,2110,2150,2180,2200,2220,2240",
                predictedPriceNextWeek = 2290,
                predictionConfidencePercent = 88,
                isPerishable = false,
                iconEmoji = "🌽"
            )
        )

        val sampleMandiLocations = listOf(
            MandiLocationEntity(
                id = "loc_wgl",
                nameTe = "వరంగల్ ఎనుమాముల మార్కెట్",
                nameEn = "Enumamula Agricultural Market, Warangal",
                district = "Warangal",
                distanceKm = 14.2,
                contactPhone = "0870-2444321",
                operatingHours = "ఉదయం 6:00 - సాయంత్రం 5:00",
                latitude = 17.9689,
                longitude = 79.5941,
                hasElectronicWeighbridge = true
            ),
            MandiLocationEntity(
                id = "loc_khammam",
                nameTe = "ఖమ్మం మార్కెట్ యార్డ్",
                nameEn = "Khammam Market Yard",
                district = "Khammam",
                distanceKm = 48.6,
                contactPhone = "08742-232115",
                operatingHours = "ఉదయం 7:00 - మధ్యాహ్నం 3:30",
                latitude = 17.2473,
                longitude = 80.1514,
                hasElectronicWeighbridge = true
            ),
            MandiLocationEntity(
                id = "loc_jangaon",
                nameTe = "జనగామ వ్యవసాయ మార్కెట్",
                nameEn = "Jangaon Agri Market",
                district = "Jangaon",
                distanceKm = 32.0,
                contactPhone = "08716-222045",
                operatingHours = "ఉదయం 8:00 - సాయంత్రం 4:00",
                latitude = 17.7236,
                longitude = 79.1558,
                hasElectronicWeighbridge = true
            ),
            MandiLocationEntity(
                id = "loc_suryapet",
                nameTe = "సూర్యాపేట వ్యవసాయ మార్కెట్",
                nameEn = "Suryapet Agricultural Market",
                district = "Suryapet",
                distanceKm = 64.5,
                contactPhone = "08684-221234",
                operatingHours = "ఉదయం 7:30 - సాయంత్రం 4:30",
                latitude = 17.1439,
                longitude = 79.6239,
                hasElectronicWeighbridge = true
            ),
            MandiLocationEntity(
                id = "loc_guntur",
                nameTe = "గుంటూరు ఆసియా అతిపెద్ద మిర్చి యార్డ్",
                nameEn = "Guntur Chilli Yard",
                district = "Guntur",
                distanceKm = 142.0,
                contactPhone = "0863-2234500",
                operatingHours = "ఉదయం 5:30 - సాయంత్రం 6:00",
                latitude = 16.3067,
                longitude = 80.4365,
                hasElectronicWeighbridge = true
            )
        )

        val sampleCropCatalog = listOf(
            CropCatalogEntity(
                cropId = "chilli",
                nameTe = "మిర్చి (తేజ / సన్న రకాలు)",
                nameEn = "Dry Red Chilli (Teja / Sannam)",
                seasonTe = "ఖరీఫ్ (ఆగస్టు నాట్లు), రబీ (అక్టోబర్ నాట్లు)",
                seasonEn = "Kharif (Aug planting), Rabi (Oct planting)",
                typicalPriceRange = "₹18,000 - ₹24,000 / క్వింటాల్",
                localizedZoneTe = "వరంగల్, ఖమ్మం, గుంటూరు, మహబూబాబాద్ నల్లరేగడి నేలలు",
                localizedZoneEn = "Warangal, Khammam, Guntur, Mahabubabad black soil belt",
                plantingTimeTe = "నారుమడి: జూలై మొదటి వారం. ప్రధాన పొలంలో నాటడం: ఆగస్టు 15 నుండి సెప్టెంబర్ 10 లోపు.",
                plantingTimeEn = "Nursery: 1st week July. Field transplanting: Aug 15 to Sept 10.",
                soilPreparationTe = "భూమిని 3-4 సార్లు లోతుగా దున్నాలి. ఎకరానికి 8-10 టన్నుల చివికిన పశువుల ఎరువు మరియు 2 బస్తాల సింగిల్ సూపర్ ఫాస్ఫేట్ వేసి కలియదున్నాలి. 60 సెం.మీ ఎత్తు గట్లు (రిడ్జెస్) పోసుకోవాలి.",
                soilPreparationEn = "Plow 3-4 times deeply. Incorporate 8-10 tonnes well-decomposed FYM and 2 bags SSP per acre. Form raised ridges of 60cm.",
                irrigationNeedsTe = "నాటిన వెంటనే తేలికపాటి తడి ఇవ్వాలి. పూత మరియు కాయ దశల్లో 6-8 రోజులకు ఒకసారి తేమ అందించాలి. నీరు నిలవకుండా డ్రైనేజీ ఉండాలి (డ్రిప్ ఇరిగేషన్ ఉత్తమం).",
                irrigationNeedsEn = "Light irrigation immediately after planting. Provide moisture every 6-8 days during flowering/fruiting. Drip irrigation prevents root rot.",
                fertilizerScheduleTe = "ఎకరానికి మొత్తం మోతాదు: 120 కిలోల నత్రజని, 60 కిలోల భాస్వరం, 80 కిలోల పొటాష్. నాటేటప్పుడు సగం భాస్వరం, మొత్తం పొటాష్ లో సగం వేయాలి. మిగిలిన ఎరువులను 30, 60, 90 రోజులకు సమాన వాయిదాల్లో అందించాలి.",
                fertilizerScheduleEn = "Total/acre: 120kg N, 60kg P, 80kg K. Apply all P and 50% K as basal. Split N and remaining K at 30, 60, 90 days after transplanting.",
                commonPestsTe = "నల్ల తామర పురుగు (బ్లాక్ త్రిప్స్), పేనుబంక, నల్లి, మరియు కొమ్మ ఎండు / కాయకుళ్లు తెగులు (ఆంథ్రాక్నోస్).",
                commonPestsEn = "Black thrips, aphids, mites, and dieback / fruit rot (anthracnose).",
                organicTreatmentTe = "ఎకరానికి 20-25 నీలి, పసుపు రంగు జిగురు అట్టలు పెట్టాలి. 10 రోజులకు ఒకసారి వేపనూనె (10,000 ppm) 2 మి.లీ లేదా జీవామృతం 200 లీటర్లు పిచికారీ చేయాలి. పులిసిన మజ్జిగ ద్రావణం కొమ్మ ఎండుకు మంచిది.",
                organicTreatmentEn = "Set 20-25 yellow and blue sticky traps/acre. Spray neem oil (10,000 ppm) @ 2ml/L or fermented buttermilk solution every 10 days.",
                chemicalTreatmentTe = "తామర పురుగు తీవ్రత ఉంటే స్పినోటోరామ్ 11.7 SC @ 0.9 మి.లీ లేదా ఫిప్రోనిల్ @ 2 మి.లీ లీటరు నీటికి కలపండి. కాయకుళ్లు నివారణకు అజాక్సిస్ట్రోబిన్ + డైఫెనోకోనజోల్ @ 1 మి.లీ స్ప్రే చేయండి.",
                chemicalTreatmentEn = "For severe thrips: Spinetoram 11.7 SC @ 0.9 ml/L or Fipronil @ 2 ml/L. For fruit rot: Azoxystrobin + Difenoconazole @ 1 ml/L.",
                coldStorageRecommended = true,
                harvestAdviceTe = "పూర్తిగా ఎర్రబడిన కాయలను మాత్రమే కోసి, పరదాలపై 5-6 రోజులు ఆరబెట్టాలి. తేమ 9-10% ఉన్నప్పుడు గోనె సంచులలో ప్యాక్ చేసి కోల్డ్ స్టోరేజీకి తరలించాలి.",
                harvestAdviceEn = "Pick deep red ripe pods, sun-dry on tarpaulins to 9-10% moisture before bagging for cold storage.",
                iconEmoji = "🌶️"
            ),
            CropCatalogEntity(
                cropId = "cotton",
                nameTe = "పత్తి (బిటి కాటన్)",
                nameEn = "Bt Cotton",
                seasonTe = "ఖరీఫ్ (జూన్ 15 నుండి జూలై 15 విత్తడం)",
                seasonEn = "Kharif (June 15 to July 15 sowing)",
                typicalPriceRange = "₹6,800 - ₹7,800 / క్వింటాల్",
                localizedZoneTe = "వరంగల్, ఆదిలాబాద్, నల్గొండ, గుంటూరు నల్లరేగడి ప్రాంతాలు",
                localizedZoneEn = "Warangal, Adilabad, Nalgonda, Guntur black cotton soils",
                plantingTimeTe = "మొదటి ఋతుపవనాల వర్షం పడిన తర్వాత (జూన్ 15 - జూలై 10). నేలలో కనీసం 5-6 అంగుళాల లోతు తేమ ఉన్నప్పుడే విత్తాలి.",
                plantingTimeEn = "After first monsoon soaking rains (June 15 - July 10). Ensure 5-6 inch subsoil moisture.",
                soilPreparationTe = "లోతుగా దుక్కి దున్ని గుంటుక తోలాలి. ఎకరానికి 5 టన్నుల పశువుల ఎరువు, విత్తే ముందు ఎకరానికి 50 కిలోల డిఎపి లేదా సింగిల్ సూపర్ ఫాస్ఫేట్ వేయాలి. వరుసల మధ్య 90 సెం.మీ, మొక్కల మధ్య 60 సెం.మీ దూరం పాటించాలి.",
                soilPreparationEn = "Deep summer ploughing followed by harrowing. Apply 5T FYM and 50kg DAP per acre. Maintain 90cm row-to-row and 60cm plant-to-plant spacing.",
                irrigationNeedsTe = "పత్తి వర్షాధార పంట అయినప్పటికీ, వర్షాభావ పరిస్థితుల్లో పూత మరియు కాయ ఊరే దశల్లో (విత్తిన 45, 75, 100 రోజులకు) తప్పనిసరిగా రక్షక తడులు ఇవ్వాలి.",
                irrigationNeedsEn = "Mainly rainfed, but critical protective irrigations required at squaring (45d), flowering (75d), and boll development (100d).",
                fertilizerScheduleTe = "ఎకరానికి 60 కిలోల నత్రజని, 30 కిలోల భాస్వరం, 30 కిలోల పొటాష్. నత్రజనిని విత్తిన 20, 45, 65, 80 రోజులకు నాలుగు సమాన భాగాలుగా వేసి మొక్కల మొదట్లో మట్టి ఎగదోయాలి.",
                fertilizerScheduleEn = "NPK 60:30:30 kg/acre. Apply full P at basal. Split N into 4 equal top dressings at 20, 45, 65, and 80 days with earthing up.",
                commonPestsTe = "గులాబీ రంగు కాయ పురుగు (పింక్ బోల్‌వార్మ్), పచ్చదోమ, పేనుబంక, తెల్లదోమ మరియు ఆకు ఎరుపు తెగులు.",
                commonPestsEn = "Pink bollworm, leafhoppers (jassids), whiteflies, and magnesium deficiency leaf reddening.",
                organicTreatmentTe = "ఎకరానికి 4-5 లింగాకర్షక బుట్టలు (ఫెరమోన్ ట్రాప్స్) 45 రోజుల వయసు నుండి అమర్చండి. ట్రైకోగ్రామా పరాన్నజీవి గుడ్లను విడుదల చేయండి. 5% వేప గింజల కషాయం (NSKE) పిచికారీ చేయండి.",
                organicTreatmentEn = "Install 4-5 pink bollworm pheromone traps per acre from 45 DAS. Release Trichogramma parasitoids. Spray 5% NSKE extract.",
                chemicalTreatmentTe = "బోల్‌వార్మ్ రాకుండా 90 రోజుల వద్ద ప్రొఫెనోఫాస్ @ 2 మి.లీ లేదా క్లోరాంట్రానిలిప్రోల్ @ 0.3 మి.లీ పిచికారీ చేయాలి. ఆకు ఎరుపు నివారణకు 20 గ్రాముల మెగ్నీషియం సల్ఫేట్ + 10 గ్రాముల యూరియా లీటరు నీటిలో కలపండి.",
                chemicalTreatmentEn = "Profenofos @ 2 ml/L or Chlorantraniliprole @ 0.3 ml/L at 90 days. For leaf reddening: spray 2% Magnesium Sulphate + 1% Urea.",
                coldStorageRecommended = false,
                harvestAdviceTe = "ఉదయం పూట మంచు ఆరిన తర్వాత మాత్రమే దూదిని శుభ్రంగా ఏరాలి. ఎండిన ఆకులు, రొట్ట కలవకుండా చూడాలి. తేమ లేకుండా నిల్వ చేయాలి.",
                harvestAdviceEn = "Pick clean lint only after morning dew evaporates. Keep trash-free and dry under covered sheds.",
                iconEmoji = "☁️"
            ),
            CropCatalogEntity(
                cropId = "paddy",
                nameTe = "వరి (ధాన్యం - బిపిటి / సోనా మసూరి)",
                nameEn = "Paddy (Rice - BPT / Sona Masuri)",
                seasonTe = "ఖరీఫ్ (సార్వా: జూన్ - నవంబర్), రబీ (దాళ్వా: డిసెంబర్ - ఏప్రిల్)",
                seasonEn = "Kharif (June - Nov), Rabi (Dec - April)",
                typicalPriceRange = "₹2,300 - ₹2,700 / క్వింటాల్",
                localizedZoneTe = "తెలంగాణ & ఆంధ్రప్రదేశ్ నాగార్జునసాగర్, శ్రీరాంసాగర్, గోదావరి ఆయకట్టు ప్రాంతాలు",
                localizedZoneEn = "Nagarjuna Sagar, SRSP, Godavari delta ayacut in TG & AP",
                plantingTimeTe = "ఖరీఫ్ నారుపోత: జూన్ 15 - జూలై 10. నాట్లు: జూలై 15 - ఆగస్టు 10 లోపు (25 రోజుల ముదురు కాని నారు నాటాలి).",
                plantingTimeEn = "Kharif Nursery: June 15 - July 10. Transplanting: July 15 - Aug 10 (use 25-day young seedlings).",
                soilPreparationTe = "పొలాన్ని 2-3 సార్లు బురద దుక్కి (దమ్ము) చేయాలి. ఆఖరి దమ్ములో ఎకరానికి 5 టన్నుల పచ్చిరొట్ట ఎరువు (జీలుగు లేదా జనుము) కలిపి లేవెలర్ తో చదును చేయాలి.",
                soilPreparationEn = "Puddle soil 2-3 times thoroughly. Incorporate green manure crops (Dhaincha/Sunhemp) in last puddling and level the field perfectly.",
                irrigationNeedsTe = "నాటిన తర్వాత వారం రోజుల వరకు 2 సెం.మీ పలుచటి నీరు ఉంచాలి. పిలకలు తొడిగే దశలో మరియు చిరుపొట్ట దశలో 5 సెం.మీ నీరు అవసరం. కోతకు 10 రోజుల ముందు నీటిని పూర్తిగా తీసివేయాలి.",
                irrigationNeedsEn = "Maintain shallow 2cm water depth first week. Keep 5cm during tillering and panicle initiation. Drain field completely 10 days before harvest.",
                fertilizerScheduleTe = "ఎకరానికి 40-50 కిలోల నత్రజని, 24 కిలోల భాస్వరం, 20 కిలోల పొటాష్ మరియు 10 కిలోల జింక్ సల్ఫేట్. భాస్వరం మరియు జింక్ ఆఖరి దమ్ములో వేయాలి. నత్రజనిని నాట్లు, పిలక, మరియు చిరుపొట్ట దశలలో 3 వాయిదాల్లో వేయాలి.",
                fertilizerScheduleEn = "NPK 48:24:20 kg/acre + 10kg Zinc Sulphate. Apply full P and Zn at last puddling. Split N into 3 equal parts at transplanting, active tillering, and panicle initiation.",
                commonPestsTe = "కాండం తొలిచే పురుగు (స్టెమ్ బోరర్), సుడిదోమ (బిపిహెచ్), అగ్గి తెగులు (బ్లాస్ట్), మరియు ఎండ్రకాయ తెగులు (షీత్ బ్లైట్).",
                commonPestsEn = "Yellow stem borer, brown planthopper (BPH), blast, and sheath blight.",
                organicTreatmentTe = "ఎకరానికి 4 వేప పిండి బస్తాలు దమ్ములో వేయాలి. సుడిదోమ రాకుండా నీటిని 2-3 రోజులు తీసి ఆరబెట్టాలి (ఆల్టర్నేట్ వెట్టింగ్ & డ్రైయింగ్). బయో ఏజెంట్ సూడోమోనాస్ ఫ్లోరోసెన్స్ 10 గ్రాములు లీటరు నీటికి కలపాలి.",
                organicTreatmentEn = "Apply neem cake 100kg/acre at puddling. Practice Alternate Wetting and Drying (AWD) to control BPH. Foliar spray Pseudomonas fluorescens @ 10g/L.",
                chemicalTreatmentTe = "కాండం తొలిచే పురుగుకు కార్టాప్ హైడ్రోక్లోరైడ్ 4G గుళికలు ఎకరానికి 8 కిలోలు లేదా క్లోరాంట్రానిలిప్రోల్ 0.3 మి.లీ. సుడిదోమకు పైమెట్రోజైన్ 0.6 గ్రా. లీటరు నీటికి మొదళ్లు తడిచేలా పిచికారీ చేయాలి.",
                chemicalTreatmentEn = "Stem borer: Cartap Hydrochloride 4G granules @ 8kg/acre. BPH: Pymetrozine 50 WG @ 0.6g/L spraying directed at plant base.",
                coldStorageRecommended = false,
                harvestAdviceTe = "కంకిలో 85% గింజలు పసుపు రంగుకు మారినప్పుడు కోత కోయాలి. ధాన్యాన్ని కల్లాల్లో ఎండబెట్టి తేమ 13-14% ఉండేలా చూసుకోవాలి.",
                harvestAdviceEn = "Harvest when 85% grains turn golden. Sun-dry clean paddy to 13-14% moisture before sale.",
                iconEmoji = "🌾"
            ),
            CropCatalogEntity(
                cropId = "turmeric",
                nameTe = "పసుపు (దుంపలు - నిజామాబాద్ రకం)",
                nameEn = "Turmeric (Rhizomes)",
                seasonTe = "ఖరీఫ్ (మే చివరి వారం నుండి జూన్ నెలాఖరు వరకు విత్తడం)",
                seasonEn = "Kharif (May last week to end of June planting)",
                typicalPriceRange = "₹12,000 - ₹17,000 / క్వింటాల్",
                localizedZoneTe = "నిజామాబాద్, జగిత్యాల, వరంగల్, కడప ఎర్ర నేలలు",
                localizedZoneEn = "Nizamabad, Jagtial, Warangal, Kadapa red loamy tracts",
                plantingTimeTe = "మే 25 నుండి జూన్ 20 లోపు. విత్తన దుంపలను తప్పనిసరిగా శుద్ధి చేసి నాటాలి.",
                plantingTimeEn = "May 25 to June 20. Seed rhizomes must be treated prior to planting.",
                soilPreparationTe = "లోతుగా దుక్కి దున్ని ఎకరానికి 10-12 టన్నుల పశువుల ఎరువు వేయాలి. ఎత్తు బెడ్లు (రైజ్డ్ బెడ్స్) తయారు చేసుకోవాలి. వరుసల మధ్య 45 సెం.మీ, దుంపల మధ్య 20 సెం.మీ దూరం ఉంచాలి.",
                soilPreparationEn = "Prepare loose friable soil with 10-12 tonnes FYM. Make raised beds 120cm wide. Space rhizomes 45x20cm.",
                irrigationNeedsTe = "విత్తిన వెంటనే తేలికపాటి తడి ఇవ్వాలి. దుంప ఊరే దశలో (సెప్టెంబర్ - నవంబర్) నేలలో తగినంత తేమ ఉండాలి. డ్రిప్ ఇరిగేషన్ పద్ధతిలో దుంపకుళ్లు తెగులు తగ్గుతుంది.",
                irrigationNeedsEn = "Light irrigation right after planting. Maintain uniform moisture during rhizome bulking (Sept-Nov). Drip systems drastically reduce rhizome rot.",
                fertilizerScheduleTe = "ఎకరానికి 80 కిలోల నత్రజని, 50 కిలోల భాస్వరం, 80 కిలోల పొటాష్. నాటేటప్పుడు మొత్తం భాస్వరం, సగం పొటాష్ వేయాలి. నత్రజనిని విత్తిన 30, 60, 90, 120 రోజులకు 4 దఫాలుగా వేసి మట్టి ఎగదోయాలి.",
                fertilizerScheduleEn = "NPK 80:50:80 kg/acre. Full P and 50% K at planting. Split N into 4 installments at 30, 60, 90, 120 days along with earthing up.",
                commonPestsTe = "దుంప కుళ్లు (రైజోమ్ రాట్ / కొమ్మ తెగులు), ఆకు మచ్చ తెగులు, మరియు దుంప ఈగ.",
                commonPestsEn = "Rhizome rot (Pythium), leaf blotch (Taphrina), and shoot borer / rhizome fly.",
                organicTreatmentTe = "నాటే ముందు విత్తన దుంపలను ట్రైకోడెర్మా విరిడే (10 గ్రా./కేజీ) తో శుద్ధి చేయండి. ఎకరానికి 200 కిలోల వేప పిండిని భూమిలో కలపండి. జీవామృతం నెలకు రెండుసార్లు నీటితో అందించండి.",
                organicTreatmentEn = "Seed treat mother rhizomes with Trichoderma viride @ 10g/kg. Apply 200kg neem cake to soil. Drench with Jeevamrutha twice a month.",
                chemicalTreatmentTe = "దుంపకుళ్లు నివారణకు మెటలాక్సిల్ + మాంకోజెబ్ (రిడోమిల్) @ 2.5 గ్రాములు లీటరు నీటికి కలిపి దుంపల మొదళ్లు బాగా తడిచేలా ద్రావణాన్ని పోయాలి (డ్రెంచింగ్ చేయాలి).",
                chemicalTreatmentEn = "For rhizome rot drenching: Metalaxyl + Mancozeb (Ridomil) @ 2.5g/L or Copper Oxychloride @ 3g/L around rhizome zone.",
                coldStorageRecommended = false,
                harvestAdviceTe = "ఆకులు పూర్తిగా పసుపు రంగులోకి మారి ఎండిపోయినప్పుడు (8-9 నెలలకు) దుంపలు తవ్వాలి. బాయిలర్లలో ఉడికించి, ఎండలో ఆరబెట్టి పాలిష్ చేయాలి.",
                harvestAdviceEn = "Harvest when aerial leaves turn dry and yellow (8-9 months). Boil in turmeric boilers, sun-dry, and polish.",
                iconEmoji = "🟡"
            ),
            CropCatalogEntity(
                cropId = "tomato",
                nameTe = "టమాటా (హైబ్రిడ్ / నాటు)",
                nameEn = "Tomato (Hybrid / Country)",
                seasonTe = "సంవత్సరం పొడవునా (ఖరీఫ్: జూన్-జులై, రబీ: అక్టోబర్-నవంబర్)",
                seasonEn = "Year-round (Kharif: June-July, Rabi: Oct-Nov)",
                typicalPriceRange = "₹1,500 - ₹3,200 / క్వింటాల్",
                localizedZoneTe = "రంగారెడ్డి, మెదక్, చిత్తూరు, మదనపల్లె, అనంతపురం కూరగాయల బెల్ట్",
                localizedZoneEn = "Ranga Reddy, Medak, Chittoor, Madanapalle vegetable zones",
                plantingTimeTe = "ఖరీఫ్: జూన్ 15 - జూలై 15 నారు పోయడం. రబీ: సెప్టెంబర్ 20 - అక్టోబర్ 20.",
                plantingTimeEn = "Kharif nursery: June 15 - July 15. Rabi nursery: Sept 20 - Oct 20.",
                soilPreparationTe = "ఎర్ర ఇసుక నేలలు లేదా గరప నేలలు శ్రేష్టం. ఎకరానికి 10 టన్నుల పశువుల ఎరువు, సిల్వర్-బ్లాక్ మల్చింగ్ షీట్ వాడితే కలుపు తగ్గి దిగుబడి 30% పెరుగుతుంది.",
                soilPreparationEn = "Well-aerated sandy loam. Apply 10T FYM. Use silver-black polythene mulch to suppress weeds and increase yield by 30%.",
                irrigationNeedsTe = "డ్రిప్ ఇరిగేషన్ ద్వారా రోజుకు 1-2 గంటలు తేమ అందించాలి. కాయలు పగులకుండా ఉండటానికి క్రమం తప్పకుండా నీరు ఇవ్వాలి.",
                irrigationNeedsEn = "Drip irrigation for 1-2 hours daily. Regular irrigation prevents fruit cracking and blossom end rot.",
                fertilizerScheduleTe = "ఎకరానికి 75 కిలోల నత్రజని, 50 కిలోల భాస్వరం, 60 కిలోల పొటాష్. డ్రిప్ ద్వారా 19:19:19 మరియు 13:0:45 ద్రావణాలను దశలవారీగా అందించాలి (ఫెర్టిగేషన్).",
                fertilizerScheduleEn = "NPK 75:50:60 kg/acre. Apply water-soluble fertilizers (19:19:19 & 13:0:45) through drip fertigation weekly.",
                commonPestsTe = "టూటా అబ్సొల్యూటా (ఆకు తొలిచే పురుగు), కాయ తొలుచు పురుగు, బాక్టీరియల్ వడలు తెగులు, మరియు వైరస్ తెగులు.",
                commonPestsEn = "Tuta absoluta (leaf miner), fruit borer, bacterial wilt, and leaf curl virus.",
                organicTreatmentTe = "ఎకరానికి 15 ఫెరమోన్ ట్రాప్స్ (టూటా లైట్ ట్రాప్స్) పెట్టండి. బాసిల్లస్ తురింజియెన్సిస్ (Bt) @ 2 గ్రాములు లీటరు నీటికి స్ప్రే చేయండి. వైరస్ నివారణకు తెల్లదోమలను వేప నూనెతో అరికట్టండి.",
                organicTreatmentEn = "Install 15 Tuta pheromone/light traps per acre. Spray Bacillus thuringiensis (Bt) @ 2g/L. Spray neem oil to manage whitefly vectors.",
                chemicalTreatmentTe = "కాయ తొలుచు పురుగుకు క్లోరాంట్రానిలిప్రోల్ (కొరాజెన్) @ 0.3 మి.లీ. బాక్టీరియల్ వడలు నివారణకు కాపర్ ఆక్సిక్లోరైడ్ 3 గ్రా. + స్ట్రెప్టోసైక్లిన్ 0.1 గ్రా. లీటరు నీటిలో కలపండి.",
                chemicalTreatmentEn = "Fruit borer: Chlorantraniliprole @ 0.3 ml/L. Bacterial wilt: Copper Oxychloride 3g + Streptocycline 0.1g per litre drenching.",
                coldStorageRecommended = true,
                harvestAdviceTe = "దూర ప్రాంత రవాణాకు పింక్ / బ్రేకర్ స్టేజ్ లో కోయండి. ప్లాస్టిక్ క్రేట్లలో జాగ్రత్తగా భద్రపరచండి. కోల్డ్ రూంలో 10-12°C వద్ద 2 వారాలు నిల్వ చేయవచ్చు.",
                harvestAdviceEn = "Harvest at breaker/pink stage for transport. Pack in plastic crates. Store at 10-12°C in cold storage for up to 2 weeks.",
                iconEmoji = "🍅"
            ),
            CropCatalogEntity(
                cropId = "groundnut",
                nameTe = "వేరుశనగ (పల్లీ)",
                nameEn = "Groundnut (Peanut)",
                seasonTe = "ఖరీఫ్ (జూన్ - జూలై), రబీ (అక్టోబర్ - నవంబర్)",
                seasonEn = "Kharif (June - July), Rabi (Oct - Nov)",
                typicalPriceRange = "₹6,400 - ₹7,400 / క్వింటాల్",
                localizedZoneTe = "రాయలసీమ (అనంతపురం, కర్నూలు, చిత్తూరు), మహబూబ్‌నగర్ ఎర్ర నేలలు",
                localizedZoneEn = "Rayalaseema (Anantapur, Kurnool, Chittoor), Mahabubnagar red soils",
                plantingTimeTe = "ఖరీఫ్: జూన్ 20 నుండి జూలై 25 లోపు. రబీ: నవంబర్ 1 నుండి డిసెంబర్ 15 వరకు.",
                plantingTimeEn = "Kharif: June 20 to July 25. Rabi: Nov 1 to Dec 15.",
                soilPreparationTe = "ఎర్ర గరప నేలలు, తేలికపాటి ఇసుక నేలలు ఉత్తమం. ఎకరానికి 200 కిలోల జిప్సం వేయడం వలన కాయలు గట్టిపడి నూనె శాతం పెరుగుతుంది.",
                soilPreparationEn = "Sandy loam and red soil with good calcium content. Apply 200kg gypsum per acre at flowering for pod development.",
                irrigationNeedsTe = "పూత (విత్తిన 25 రోజులు), ఊడలు దిగే దశ (40-45 రోజులు), మరియు కాయ ఊరే దశ (60-70 రోజులు) అతి ముఖ్యమైన తడులు.",
                irrigationNeedsEn = "Critical irrigation stages: Flowering (25 DAS), pegging (40-45 DAS), and pod development (60-70 DAS).",
                fertilizerScheduleTe = "ఎకరానికి 10 కిలోల నత్రజని, 20 కిలోల భాస్వరం, 20 కిలోల పొటాష్. మొత్తం ఎరువులను విత్తేటప్పుడే ఆఖరి దుక్కిలో వేయాలి.",
                fertilizerScheduleEn = "NPK 8:16:16 kg/acre as basal application. Top dressing of gypsum @ 200kg/acre at 40-45 days.",
                commonPestsTe = "ఎర్ర గొంగళి పురుగు, ఆకు ముడత, తిక్కా ఆకుమచ్చ తెగులు, మరియు తుప్పు తెగులు.",
                commonPestsEn = "Red hairy caterpillar, leaf miner, Tikka leaf spot, and rust disease.",
                organicTreatmentTe = "పొలం చుట్టూ జామ, ఆముదం ఎర పంటలుగా వేయండి. తిక్కా తెగులుకు పంచగవ్య 30 మి.లీ లీటరు నీటిలో పిచికారీ చేయండి.",
                organicTreatmentEn = "Sow castor as border trap crop. Spray Panchagavya @ 30ml/L for Tikka leaf spot control.",
                chemicalTreatmentTe = "తిక్కా ఆకుమచ్చ తెగులుకు హెక్సాకొనజోల్ 2 మి.లీ లేదా కార్బెండజిమ్ + మాంకోజెబ్ 2 గ్రాములు లీటరు నీటికి స్ప్రే చేయండి.",
                chemicalTreatmentEn = "Tikka disease: Hexaconazole 5% EC @ 2 ml/L or Mancozeb @ 2.5g/L.",
                coldStorageRecommended = false,
                harvestAdviceTe = "కాయ లోపలి భాగం ముదురు నలుపు రంగుకు మారినప్పుడు మొక్కలను పీకి ఎండబెట్టాలి. తేమ 8% లోపు ఉన్నప్పుడు నిల్వ చేయాలి.",
                harvestAdviceEn = "Harvest when inner pod shell shows dark blackish veins. Dry pods to below 8% moisture.",
                iconEmoji = "🥜"
            )
        )

        val sampleColdStorages = listOf(
            ColdStorageEntity(
                id = "cs_wgl_1",
                nameTe = "శ్రీ లక్ష్మీ కోల్డ్ స్టోరేజ్ & ఆగ్రో ప్రై.లి",
                nameEn = "Sri Lakshmi Cold Storage & Agro Pvt Ltd",
                locationTe = "వరంగల్ బైపాస్ రోడ్డు, వరంగల్",
                locationEn = "Warangal Bypass Road, Warangal",
                distanceKm = 11.5,
                totalCapacityMt = 8500,
                availableCapacityMt = 2100,
                ratePerQuintalMonth = 110,
                tempRange = "2°C to 10°C (మిర్చి & చింతపండుకు ప్రత్యేక విభాగం)",
                contactPhone = "0870-2567890",
                suitableCropsTe = "ఎండు మిర్చి, పసుపు, పప్పు ధాన్యాలు",
                suitableCropsEn = "Dry Red Chilli, Turmeric, Pulses"
            ),
            ColdStorageEntity(
                id = "cs_khammam_1",
                nameTe = "గోదావరి ఆగ్రో శీతల గిడ్డంగి",
                nameEn = "Godavari Agro Refrigerated Warehouse",
                locationTe = "మండీ సర్కిల్ వద్ద, ఖమ్మం",
                locationEn = "Near Mandi Circle, Khammam",
                distanceKm = 46.0,
                totalCapacityMt = 6000,
                availableCapacityMt = 1450,
                ratePerQuintalMonth = 115,
                tempRange = "0°C to 8°C (ఆర్ద్రత నియంత్రిత)",
                contactPhone = "08742-245678",
                suitableCropsTe = "తేజ మిర్చి, మామిడి గుజ్జు, టమాటా",
                suitableCropsEn = "Teja Chilli, Mango Pulp, Tomato"
            ),
            ColdStorageEntity(
                id = "cs_jangaon",
                nameTe = "కిసాన్ మిత్ర కోల్డ్ స్టోరేజ్",
                nameEn = "Kisan Mitra Farmer Cold Storage",
                locationTe = "హైదరాబాద్ హైవే, జనగామ",
                locationEn = "Hyderabad Highway, Jangaon",
                distanceKm = 29.8,
                totalCapacityMt = 4500,
                availableCapacityMt = 980,
                ratePerQuintalMonth = 105,
                tempRange = "4°C to 12°C",
                contactPhone = "08716-234567",
                suitableCropsTe = "మిర్చి, ఉల్లిగడ్డలు, పండ్లు",
                suitableCropsEn = "Chilli, Onion, Fruits"
            ),
            ColdStorageEntity(
                id = "cs_guntur_mega",
                nameTe = "ఆంధ్ర మెగా స్పైస్ కోల్డ్ చైన్",
                nameEn = "Andhra Mega Spice Cold Chain",
                locationTe = "ఆటో నగర్, గుంటూరు",
                locationEn = "Auto Nagar, Guntur",
                distanceKm = 138.0,
                totalCapacityMt = 16000,
                availableCapacityMt = 4200,
                ratePerQuintalMonth = 120,
                tempRange = "0°C to 6°C (ఎగుమతి ప్రమాణాలు)",
                contactPhone = "0863-2289900",
                suitableCropsTe = "గుంటూరు సన్న మిర్చి, కొత్తిమీర గింజలు, సుగంధ ద్రవ్యాలు",
                suitableCropsEn = "Guntur Sannam Chilli, Coriander, Spices"
            )
        )
    }
}
