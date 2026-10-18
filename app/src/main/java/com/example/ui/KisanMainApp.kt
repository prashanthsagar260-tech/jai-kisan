package com.example.ui

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DataSaverOn
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.AppStrings
import com.example.ui.screens.AiCallScreen
import com.example.ui.screens.ColdStorageScreen
import com.example.ui.screens.CropAdvisoryScreen
import com.example.ui.screens.CropPhotoScreen
import com.example.ui.screens.LogisticsScreen
import com.example.ui.screens.PricesScreen
import com.example.ui.theme.AgriGreen
import com.example.ui.theme.HarvestGold
import com.example.ui.viewmodel.KisanViewModel
import java.util.Locale

enum class KisanTab(val icon: ImageVector, val tag: String) {
    PRICES(Icons.Default.MonetizationOn, "tab_prices"),
    ADVISORY(Icons.Default.Agriculture, "tab_advisory"),
    LOGISTICS(Icons.Default.LocalShipping, "tab_logistics"),
    COLD_STORAGE(Icons.Default.AcUnit, "tab_storage"),
    AI_CALL(Icons.Default.HeadsetMic, "tab_call")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KisanMainApp(
    viewModel: KisanViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val isDataSaver by viewModel.isDataSaver.collectAsState()
    val spokenText by viewModel.spokenTextToRead.collectAsState()

    var currentTab by remember { mutableStateOf(KisanTab.PRICES) }
    var showPhotoUploadScreen by remember { mutableStateOf(false) }

    // TextToSpeech setup
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsInitialized by remember { mutableStateOf(false) }
    var isTeluguVoiceAvailable by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInitialized = true
            }
        }
        tts = ttsInstance

        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    // TTS voice speech listener - Speaks in Telugu
    LaunchedEffect(spokenText, ttsInitialized) {
        val rawText = spokenText
        if (rawText != null && ttsInitialized) {
            val ttsEngine = tts
            if (ttsEngine != null) {
                val teluguLocale = Locale("te", "IN")
                val langResult = ttsEngine.setLanguage(teluguLocale)
                val isTeluguSupported = langResult != TextToSpeech.LANG_MISSING_DATA && langResult != TextToSpeech.LANG_NOT_SUPPORTED
                isTeluguVoiceAvailable = isTeluguSupported

                // Try to select an active Telugu voice if available
                val teluguVoice = ttsEngine.voices?.firstOrNull { it.locale.language == "te" }
                if (teluguVoice != null) {
                    ttsEngine.voice = teluguVoice
                } else if (!isTeluguSupported) {
                    ttsEngine.setLanguage(Locale.US)
                }

                ttsEngine.setSpeechRate(0.92f) // Comfortable clear pacing for farmers

                // Process speech text for authentic Telugu vocalization
                val speechToSpeak = TeluguSpeechHelper.getSpokenText(rawText, isTeluguSupported)

                ttsEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        viewModel.setTtsSpeaking(true)
                    }
                    override fun onDone(utteranceId: String?) {
                        viewModel.setTtsSpeaking(false)
                        viewModel.clearSpokenText()
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        viewModel.setTtsSpeaking(false)
                        viewModel.clearSpokenText()
                    }
                })

                ttsEngine.speak(speechToSpeak, TextToSpeech.QUEUE_FLUSH, null, "kisan_speech")
            }
        }
    }

    // Back button handling
    BackHandler(enabled = showPhotoUploadScreen || currentTab != KisanTab.PRICES) {
        if (showPhotoUploadScreen) {
            showPhotoUploadScreen = false
        } else {
            currentTab = KisanTab.PRICES
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AgriGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🌾", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = AppStrings.get("app_title", language),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (language == AppLanguage.TELUGU) "రైతు అగ్రి-టెక్ వేదిక" else "Farmer Agri-Tech Platform",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Data saver indicator / toggle
                    Surface(
                        onClick = { viewModel.toggleDataSaver() },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isDataSaver) AgriGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (isDataSaver) AgriGreen else Color.Transparent),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DataSaverOn,
                                contentDescription = null,
                                tint = if (isDataSaver) AgriGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.TELUGU) "డేటా ఆదా" else "Data Saver",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDataSaver) AgriGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Language Toggle Pill
                    Surface(
                        onClick = { viewModel.toggleLanguage() },
                        shape = RoundedCornerShape(20.dp),
                        color = HarvestGold,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("language_toggle_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Language",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.TELUGU) "తెలుగు / EN" else "EN / తెలుగు",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("kisan_bottom_navigation")
            ) {
                val tabs = listOf(
                    Triple(KisanTab.PRICES, AppStrings.get("tab_prices", language), Icons.Default.MonetizationOn),
                    Triple(KisanTab.ADVISORY, if (language == AppLanguage.TELUGU) "సాగు సలహాలు" else "Advisory", Icons.Default.Agriculture),
                    Triple(KisanTab.LOGISTICS, if (language == AppLanguage.TELUGU) "రవాణా" else "Logistics", Icons.Default.LocalShipping),
                    Triple(KisanTab.COLD_STORAGE, if (language == AppLanguage.TELUGU) "శీతల గిడ్డంగి" else "Storage", Icons.Default.AcUnit),
                    Triple(KisanTab.AI_CALL, AppStrings.get("tab_call", language), Icons.Default.HeadsetMic)
                )

                tabs.forEach { (tab, label, icon) ->
                    val isSelected = !showPhotoUploadScreen && currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            showPhotoUploadScreen = false
                            currentTab = tab
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AgriGreen,
                            selectedTextColor = AgriGreen,
                            indicatorColor = AgriGreen.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (!showPhotoUploadScreen && currentTab != KisanTab.AI_CALL) {
                FloatingActionButton(
                    onClick = { showPhotoUploadScreen = true },
                    containerColor = HarvestGold,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("crop_photo_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Upload Crop Photo")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.TELUGU) "పంట ఫోటో" else "Crop Photo",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showPhotoUploadScreen) {
                CropPhotoScreen(viewModel = viewModel)
            } else {
                when (currentTab) {
                    KisanTab.PRICES -> PricesScreen(viewModel = viewModel)
                    KisanTab.ADVISORY -> CropAdvisoryScreen(viewModel = viewModel)
                    KisanTab.LOGISTICS -> LogisticsScreen(viewModel = viewModel)
                    KisanTab.COLD_STORAGE -> ColdStorageScreen(viewModel = viewModel)
                    KisanTab.AI_CALL -> AiCallScreen(viewModel = viewModel)
                }
            }
        }
    }
}
