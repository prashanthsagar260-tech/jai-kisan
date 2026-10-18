package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.ColdStorageEntity
import com.example.data.model.CropCatalogEntity
import com.example.data.model.CropPhotoEntity
import com.example.data.model.MandiLocationEntity
import com.example.data.model.MandiPriceEntity
import com.example.data.model.TransportVehicleOption
import com.example.data.model.WeatherAdvisoryData
import android.net.Uri
import com.example.data.repository.KisanRepository
import com.example.ml.CropDiseaseMlPipeline
import com.example.ml.DiseaseDiagnosisResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import android.graphics.BitmapFactory
import com.example.ai.GeminiCropDiagnosis
import com.example.ai.KisanGeminiService

enum class TrendTimeframe {
    SEVEN_DAYS,
    THIRTY_DAYS,
    SEASONAL
}

enum class CallStatus {
    IDLE,
    DIALING,
    CONNECTED,
    LISTENING,
    SPEAKING,
    ENDED
}

data class CallDialogMessage(
    val id: String,
    val isUser: Boolean,
    val textTe: String,
    val textEn: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isGeminiPowered: Boolean = false,
    val keyActionItems: List<String> = emptyList()
)

class KisanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: KisanRepository

    val language = MutableStateFlow(AppLanguage.TELUGU)
    val isDataSaver = MutableStateFlow(true)
    val searchQuery = MutableStateFlow("")
    val selectedPrice = MutableStateFlow<MandiPriceEntity?>(null)
    val selectedTrend = MutableStateFlow(TrendTimeframe.SEVEN_DAYS)

    val prices: StateFlow<List<MandiPriceEntity>>
    val locations: StateFlow<List<MandiLocationEntity>>
    val crops: StateFlow<List<CropCatalogEntity>>
    val coldStorages: StateFlow<List<ColdStorageEntity>>
    val photos: StateFlow<List<CropPhotoEntity>>
    val weather: StateFlow<WeatherAdvisoryData>

    // Logistics state
    val selectedMandiLocation = MutableStateFlow<MandiLocationEntity?>(null)
    val customDistanceKm = MutableStateFlow(14.2)
    val transportOptions: List<TransportVehicleOption>

    // Cold storage filter
    val onlyPerishables = MutableStateFlow(false)

    // Call Assistant state
    val callStatus = MutableStateFlow(CallStatus.IDLE)
    val callDurationSec = MutableStateFlow(0)
    val callMessages = MutableStateFlow<List<CallDialogMessage>>(emptyList())
    val spokenTextToRead = MutableStateFlow<String?>(null)
    val activeTtsSpeaking = MutableStateFlow(false)

    private var callTimerJob: Job? = null

    val isRefreshing = MutableStateFlow(false)
    val latestPriceTimestamp: StateFlow<Long?>

    // ML Crop Disease Diagnosis state & Gemini API Service
    private val mlPipeline = CropDiseaseMlPipeline(application)
    private val geminiService = KisanGeminiService()
    val isDiagnosingDisease = MutableStateFlow(false)
    val diseaseDiagnosisResult = MutableStateFlow<DiseaseDiagnosisResult?>(null)
    val diagnosingImageUri = MutableStateFlow<Uri?>(null)

    init {
        val database = AppDatabase.getDatabase(application)
        repository = KisanRepository(database)
        transportOptions = repository.getTransportOptions()
        latestPriceTimestamp = repository.latestPriceTimestamp.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            System.currentTimeMillis()
        )

        weather = repository.cachedWeather.map { cached ->
            if (cached != null) {
                WeatherAdvisoryData(
                    locationTe = cached.locationTe,
                    locationEn = cached.locationEn,
                    tempCelsius = cached.tempCelsius,
                    humidityPercent = cached.humidityPercent,
                    conditionTe = cached.conditionTe,
                    conditionEn = cached.conditionEn,
                    rainProbabilityPercent = cached.rainProbabilityPercent,
                    windSpeedKmh = cached.windSpeedKmh,
                    advisoryTe = cached.advisoryTe,
                    advisoryEn = cached.advisoryEn,
                    dataSource = cached.dataSource,
                    lastFetchedTimestamp = cached.lastFetchedTimestamp
                )
            } else {
                repository.getLatestWeatherAdvisory()
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            repository.getLatestWeatherAdvisory()
        )

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        prices = combine(repository.allPrices, searchQuery) { list, query ->
            if (query.isBlank()) {
                list
            } else {
                list.filter {
                    it.cropNameTe.contains(query, ignoreCase = true) ||
                    it.cropNameEn.contains(query, ignoreCase = true) ||
                    it.marketNameTe.contains(query, ignoreCase = true) ||
                    it.marketNameEn.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        locations = repository.allLocations.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        crops = combine(repository.allCrops, searchQuery) { list, query ->
            if (query.isBlank()) {
                list
            } else {
                list.filter {
                    it.nameTe.contains(query, ignoreCase = true) ||
                    it.nameEn.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        coldStorages = combine(repository.allColdStorages, onlyPerishables) { list, filterPerishables ->
            if (!filterPerishables) list
            else list.filter {
                it.suitableCropsTe.contains("మిర్చి") ||
                it.suitableCropsTe.contains("టమాటా") ||
                it.suitableCropsEn.contains("Chilli", ignoreCase = true) ||
                it.suitableCropsEn.contains("Tomato", ignoreCase = true)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        photos = repository.allPhotos.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun toggleLanguage() {
        language.value = if (language.value == AppLanguage.TELUGU) AppLanguage.ENGLISH else AppLanguage.TELUGU
    }

    fun toggleDataSaver() {
        isDataSaver.value = !isDataSaver.value
    }

    fun selectPrice(price: MandiPriceEntity?) {
        selectedPrice.value = price
    }

    fun setTrendTimeframe(timeframe: TrendTimeframe) {
        selectedTrend.value = timeframe
    }

    fun selectMandiLocation(location: MandiLocationEntity) {
        selectedMandiLocation.value = location
        customDistanceKm.value = location.distanceKm
    }

    fun updateDistance(distance: Double) {
        customDistanceKm.value = distance.coerceAtLeast(1.0)
    }

    fun readAloud(text: String) {
        spokenTextToRead.value = text
    }

    fun readAloudInTelugu(textTe: String) {
        spokenTextToRead.value = textTe
    }

    fun clearSpokenText() {
        spokenTextToRead.value = null
    }

    fun refreshData() {
        viewModelScope.launch {
            isRefreshing.value = true
            repository.refreshMandiPricesAndWeather()
            delay(500)
            isRefreshing.value = false
        }
    }

    fun setTtsSpeaking(speaking: Boolean) {
        activeTtsSpeaking.value = speaking
    }

    // Call Assistant Logic - Strictly speaks in Telugu
    fun startCall() {
        viewModelScope.launch {
            callStatus.value = CallStatus.DIALING
            callDurationSec.value = 0
            delay(1200)
            callStatus.value = CallStatus.CONNECTED
            val welcomeTe = "నమస్కారం రైతు సోదరా! నేను మీ కిసాన్ AI సహాయకుడిని. ఈరోజు మీ ప్రాంతంలో మార్కెట్ ధరలు, వాతావరణం లేదా పంట సలహాలు ఏవైనా అడగండి."
            val welcomeEn = "Namaskaram farmer friend! I am your Kisan AI assistant. Please ask about today's mandi prices, weather, or crop advisory."
            callMessages.value = listOf(
                CallDialogMessage(
                    id = "msg_welcome",
                    isUser = false,
                    textTe = welcomeTe,
                    textEn = welcomeEn
                )
            )
            // AI Assistant speaks in Telugu
            readAloudInTelugu(welcomeTe)

            callTimerJob?.cancel()
            callTimerJob = viewModelScope.launch {
                while (callStatus.value != CallStatus.ENDED && callStatus.value != CallStatus.IDLE) {
                    delay(1000)
                    callDurationSec.value += 1
                }
            }
        }
    }

    fun askAiAssistant(questionTe: String, questionEn: String) {
        viewModelScope.launch {
            val userMsg = CallDialogMessage(
                id = "usr_${System.currentTimeMillis()}",
                isUser = true,
                textTe = questionTe,
                textEn = questionEn
            )
            callMessages.value = callMessages.value + userMsg
            callStatus.value = CallStatus.LISTENING

            // Call Gemini 3.8 Flash via Firebase AI Logic
            val queryText = if (questionTe.isNotBlank()) questionTe else questionEn
            val geminiResult = geminiService.askAgriAdvisor(queryText)

            callStatus.value = CallStatus.SPEAKING

            val (replyTe, replyEn, isGemini, actionItems) = if (geminiResult.isSuccess) {
                val g = geminiResult.getOrThrow()
                ResponseAgriBundle(g.replyTelugu, g.replyEnglish, true, g.keyActionItems)
            } else {
                val fallback = generateIntelligentAgriResponse(questionTe, questionEn)
                ResponseAgriBundle(fallback.first, fallback.second, false, emptyList())
            }

            val aiMsg = CallDialogMessage(
                id = "ai_${System.currentTimeMillis()}",
                isUser = false,
                textTe = replyTe,
                textEn = replyEn,
                isGeminiPowered = isGemini,
                keyActionItems = actionItems
            )
            callMessages.value = callMessages.value + aiMsg
            // AI Assistant always speaks in Telugu
            readAloudInTelugu(replyTe)
            delay(2000)
            callStatus.value = CallStatus.CONNECTED
        }
    }

    private data class ResponseAgriBundle(
        val replyTe: String,
        val replyEn: String,
        val isGemini: Boolean,
        val actionItems: List<String>
    )

    private fun generateIntelligentAgriResponse(qTe: String, qEn: String): Pair<String, String> {
        val lowerQ = (qTe + " " + qEn).lowercase()
        return when {
            lowerQ.contains("మిర్చి") || lowerQ.contains("chilli") || lowerQ.contains("తేజ") -> {
                Pair(
                    "నేడు వరంగల్ ఎనుమాముల యార్డులో తేజ మిర్చి క్వింటాల్‌కు సగటు ధర ₹20,500 పలికింది. గరిష్ట ధర ₹22,800. గుంటూరులో ₹21,800 ఉంది. రాబోయే వారం ధర మరింత పెరిగే అవకాశం (86% విశ్వసనీయత) ఉంది.",
                    "Today Teja Chilli in Warangal mandi traded at a modal price of ₹20,500/Q (Max ₹22,800). Guntur yard is ₹21,800. Short-term forecast indicates an upward trend with 86% confidence."
                )
            }
            lowerQ.contains("పత్తి") || lowerQ.contains("cotton") || lowerQ.contains("పురుగు") -> {
                Pair(
                    "పత్తి ధర ప్రస్తుతం క్వింటాల్‌కు ₹7,380 గా ఉంది. గులాబీ రంగు పురుగు నివారణకు ఎకరాకు 4-5 లింగాకర్షక బుట్టలు (ఫెరమోన్ ట్రాప్స్) ఏర్పాటు చేసి, ప్రొఫెనోఫాస్ 2 మి.లీ లీటరు నీటికి కలిపి పిచికారీ చేయండి.",
                    "Cotton modal price is ₹7,380/Quintal. For pink bollworm control, install 4-5 pheromone traps per acre and spray Profenofos @ 2ml per litre of water."
                )
            }
            lowerQ.contains("వర్షం") || lowerQ.contains("weather") || lowerQ.contains("వాతావరణం") -> {
                Pair(
                    "రాబోయే 24 నుండి 36 గంటల్లో తేలికపాటి వర్షం పడే అవకాశం 40% ఉంది. గాలి వేగం గంటకు 14 కి.మీ. ప్రస్తుతం క్రిమిసంహారక మందులు కొట్టవద్దు, కళ్లాల్లో ధాన్యం జాగ్రత్త చేసుకోండి.",
                    "Scattered light rainfall probability is 40% over the next 24-36 hours. Wind speed 14 km/h. Avoid pesticide spraying today and protect harvested grain."
                )
            }
            lowerQ.contains("కోల్డ్") || lowerQ.contains("storage") || lowerQ.contains("గిడ్డంగి") -> {
                Pair(
                    "వరంగల్ సమీపంలోని శ్రీ లక్ష్మీ కోల్డ్ స్టోరేజీలో 2,100 మెట్రిక్ టన్నుల నిల్వ స్థలం అందుబాటులో ఉంది. నెలవారీ అద్దె క్వింటాల్‌కు ₹110 మాత్రమే. సంప్రదించండి: 0870-2567890.",
                    "Sri Lakshmi Cold Storage near Warangal has 2,100 MT capacity available at ₹110/Quintal per month. Contact: 0870-2567890."
                )
            }
            lowerQ.contains("వరి") || lowerQ.contains("paddy") || lowerQ.contains("ధాన్యం") -> {
                Pair(
                    "వరి సోనా మసూరి సగటు ధర క్వింటాల్‌కు ₹2,480 నడుస్తోంది. తేమ 14 శాతం కంటే తక్కువగా ఉండేలా ఎండబెట్టి మార్కెట్‌కు తీసుకువెళ్లండి.",
                    "Paddy Sona Masuri modal price is ₹2,480/Quintal. Ensure grain moisture is dried below 14% before taking to mandi."
                )
            }
            else -> {
                Pair(
                    "మీ ప్రశ్నకు ధన్యవాదాలు. నేటి మార్కెట్ సమాచారం ప్రకారం పత్తి, మిర్చి మరియు పసుపు ధరలు నిలకడగా ఉన్నాయి. పంట కోత మరియు విక్రయాలకు ఇది సరైన సమయం.",
                    "Thank you for asking. Today's market data indicates stable prices for chilli, cotton, and turmeric. Good conditions for harvesting and orderly transport."
                )
            }
        }
    }

    fun endCall() {
        callTimerJob?.cancel()
        callStatus.value = CallStatus.ENDED
        viewModelScope.launch {
            delay(800)
            callStatus.value = CallStatus.IDLE
        }
    }

    fun saveCapturedPhoto(cropName: String, uri: String, grade: String, notes: String) {
        viewModelScope.launch {
            // Compress size calculation simulation
            val simulatedCompressedSize = if (isDataSaver.value) 210_000L else 850_000L
            repository.saveCropPhoto(
                cropName = cropName.ifBlank { "Crop Sample" },
                path = uri,
                sizeBytes = simulatedCompressedSize,
                grade = grade,
                notes = notes
            )
        }
    }

    fun deletePhoto(id: Long) {
        viewModelScope.launch {
            repository.deleteCropPhoto(id)
        }
    }

    fun diagnoseCropImage(uri: Uri?, cropHint: String, autoSpeak: Boolean = false) {
        viewModelScope.launch {
            diagnosingImageUri.value = uri
            isDiagnosingDisease.value = true

            var finalResult: DiseaseDiagnosisResult? = null

            // Try Gemini 3.8 Flash Vision Multimodal Analysis first
            if (uri != null) {
                try {
                    val bitmap = getApplication<Application>().contentResolver.openInputStream(uri)?.use { stream ->
                        val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                        BitmapFactory.decodeStream(stream, null, opts)
                    }
                    if (bitmap != null) {
                        val geminiVision = geminiService.diagnoseCropPhoto(bitmap, cropHint)
                        if (geminiVision.isSuccess) {
                            val g = geminiVision.getOrThrow()
                            finalResult = DiseaseDiagnosisResult(
                                cropName = g.cropNameTe,
                                diseaseNameTe = g.conditionNameTe,
                                diseaseNameEn = g.conditionNameEn,
                                pathogenType = g.pathogenType,
                                severityLevelTe = g.severity,
                                severityLevelEn = if (g.severity.contains("Low") || g.severity.contains("తక్కువ")) "Low" else if (g.severity.contains("Severe") || g.severity.contains("తీవ్ర")) "Severe" else "Moderate",
                                confidencePercent = 95,
                                symptomsTe = g.symptomsTe,
                                symptomsEn = "Visual AI Diagnosis via Gemini 3.8 Flash Vision",
                                organicRemedyTe = g.organicRemedyTe,
                                organicRemedyEn = "Organic treatment recommendation",
                                chemicalRemedyTe = g.chemicalRemedyTe,
                                chemicalRemedyEn = "Chemical pesticide treatment recommendation",
                                preventiveCareTe = g.preventiveTipsTe,
                                preventiveCareEn = "Preventive agricultural practices",
                                spokenSummaryTe = g.spokenSummaryTe
                            )
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.w("KisanViewModel", "Gemini vision fallback to local pipeline", e)
                }
            }

            if (finalResult == null) {
                finalResult = mlPipeline.analyzeCropImage(uri, cropHint)
            }

            diseaseDiagnosisResult.value = finalResult
            isDiagnosingDisease.value = false
            if (autoSpeak) {
                readAloudInTelugu(finalResult.spokenSummaryTe)
            }
        }
    }

    fun clearDiseaseDiagnosis() {
        diseaseDiagnosisResult.value = null
        diagnosingImageUri.value = null
    }
}
