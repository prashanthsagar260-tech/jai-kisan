package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AppLanguage
import com.example.data.model.AppStrings
import com.example.data.model.CropPhotoEntity
import com.example.ml.DiseaseDiagnosisResult
import com.example.ui.components.AudioReadoutButton
import com.example.ui.theme.AgriGreen
import com.example.ui.theme.HarvestGold
import com.example.ui.viewmodel.KisanViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropPhotoScreen(
    viewModel: KisanViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val isDataSaver by viewModel.isDataSaver.collectAsState()
    val isDiagnosing by viewModel.isDiagnosingDisease.collectAsState()
    val diagnosisResult by viewModel.diseaseDiagnosisResult.collectAsState()
    val diagnosingUri by viewModel.diagnosingImageUri.collectAsState()

    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = Community Gallery, 1 = Upload & Doctor
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var cropNameInput by remember { mutableStateOf("మిర్చి (Chilli)") }
    var notesInput by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf("A-Grade (నాణ్యమైన పంట)") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    // Android zero-permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            viewModel.saveCapturedPhoto(
                cropName = cropNameInput,
                uri = uri.toString(),
                grade = selectedGrade,
                notes = notesInput
            )
            // Immediately run ML disease detection on the picked photo
            viewModel.diagnoseCropImage(uri, cropNameInput, autoSpeak = true)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Main Top Sub-Tab Switcher: Community Gallery vs Crop Doctor Upload
            SecondaryTabRow(
                selectedTabIndex = activeSubTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = AgriGreen,
                modifier = Modifier.testTag("crop_screen_tab_row")
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.TELUGU) "రైతుల గ్యాలరీ" else "Community Gallery",
                                fontWeight = if (activeSubTab == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_community_gallery")
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.TELUGU) "పంట డాక్టర్ & అప్‌లోడ్" else "Doctor & My Uploads",
                                fontWeight = if (activeSubTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    },
                    modifier = Modifier.testTag("tab_crop_doctor")
                )
            }

            if (activeSubTab == 0) {
                CommunityGalleryScreen(
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f),
                    onNavigateToUploadDoctor = { activeSubTab = 1 }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("crop_photo_screen_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
        // Upload & ML Analysis Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = AgriGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.get("photo_upload_title", language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        AudioReadoutButton(
                            onClick = {
                                val spoken = if (language == AppLanguage.TELUGU) {
                                    "మీ పంట ఆకు లేదా కాయ ఫోటోను అప్‌లోడ్ చేయండి. మా ఆర్టిఫిషియల్ ఇంటెలిజెన్స్ మోడల్ పంట వ్యాధిని గుర్తించి సేంద్రీయ మరియు రసాయన నివారణ మందులను సూచిస్తుంది."
                                } else {
                                    "Upload a crop leaf or harvest photo. Our AI/ML pipeline detects common diseases and prescribes organic and chemical remedies with dosages."
                                }
                                viewModel.readAloud(spoken)
                            },
                            language = language
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Compression benefit notice
                    Surface(
                        color = AgriGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Compress,
                                contentDescription = null,
                                tint = AgriGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.TELUGU)
                                    "స్మార్ట్ కంప్రెషన్: 3.5 MB ఫోటోను 210 KB గా కుదించి వేగంగా అప్‌లోడ్ చేస్తుంది (డేటా ఆదా)."
                                else
                                    "Smart Compression: Shrinks 3.5 MB images to 210 KB for fast rural upload.",
                                style = MaterialTheme.typography.labelSmall,
                                color = AgriGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Crop Selection / Input
                    OutlinedTextField(
                        value = cropNameInput,
                        onValueChange = { cropNameInput = it },
                        label = { Text(if (language == AppLanguage.TELUGU) "పంట పేరు / రకం" else "Crop Name / Variety") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Crop Presets for Farmers
                    Text(
                        text = AppStrings.get("sample_test_title", language),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val sampleCrops = listOf(
                        "మిర్చి (Chilli)",
                        "పత్తి (Cotton)",
                        "వరి (Paddy)",
                        "టమాటా (Tomato)",
                        "వేరుశనగ (Groundnut)"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sampleCrops.take(3).forEach { crop ->
                            FilterChip(
                                selected = cropNameInput == crop,
                                onClick = {
                                    cropNameInput = crop
                                    viewModel.diagnoseCropImage(selectedUri, crop, autoSpeak = false)
                                },
                                label = { Text(crop.substringBefore(" "), style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AgriGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sampleCrops.drop(3).forEach { crop ->
                            FilterChip(
                                selected = cropNameInput == crop,
                                onClick = {
                                    cropNameInput = crop
                                    viewModel.diagnoseCropImage(selectedUri, crop, autoSpeak = false)
                                },
                                label = { Text(crop.substringBefore(" "), style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AgriGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons: Pick Photo + Run ML Diagnosis
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AgriGreen),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_photo_button")
                        ) {
                            Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.get("pick_gallery", language),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.diagnoseCropImage(selectedUri, cropNameInput, autoSpeak = true)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HarvestGold, contentColor = Color.Black),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("run_ml_diagnosis_button")
                        ) {
                            Icon(imageVector = Icons.Default.Science, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.get("diagnose_now", language),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // ML Inference Progress Indicator
        if (isDiagnosing) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = AgriGreen.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, AgriGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = AgriGreen,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = AppStrings.get("diagnosing", language),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AgriGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == AppLanguage.TELUGU)
                                "Gemini 3.8 Flash Vision & AI నమూనాలు ఆకు లక్షణాలు, పిగ్మెంట్లు మరియు వ్యాధి నమూనాలను విశ్లేషిస్తోంది..."
                            else
                                "Gemini 3.8 Flash Vision analyzing leaf patterns, lesions, and pathogens...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ML Disease Diagnosis Result Card
        if (diagnosisResult != null && !isDiagnosing) {
            item {
                CropDiseaseDiagnosisCard(
                    diagnosis = diagnosisResult!!,
                    language = language,
                    onReadAloud = { viewModel.readAloudInTelugu(diagnosisResult!!.spokenSummaryTe) },
                    onDismiss = { viewModel.clearDiseaseDiagnosis() },
                    onShareToCommunity = {
                        val diag = diagnosisResult!!
                        viewModel.shareCropPhotoToCommunity(
                            cropName = cropNameInput,
                            variety = "రైతు పంట నమూనా",
                            farmerName = "మీరు (రైతు)",
                            village = "స్థానిక గ్రామం",
                            district = "తెలంగాణ / ఆంధ్రప్రదేశ్",
                            imageUrlOrUri = diagnosingUri?.toString() ?: "https://images.unsplash.com/photo-1588252303782-cb80119abd6d?w=800&q=80",
                            notes = "AI పంట డాక్టర్ తెగులు నిర్ధారణ: ${diag.diseaseNameTe} (${diag.severityLevelTe}). సేంద్రీయ నివారణ: ${diag.organicRemedyTe}",
                            grade = "వ్యాధి నివారణ దశ (Under Treatment)",
                            attachDiagnosis = true
                        )
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                if (language == AppLanguage.TELUGU)
                                    "తెగులు నివేదిక రైతుల గ్యాలరీలో పంచుకోబడింది!"
                                else
                                    "Pathology report shared to Community Gallery!"
                            )
                        }
                    }
                )
            }
        }

        // Section Title: Uploaded Crop Photos
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == AppLanguage.TELUGU) "అప్‌లోడ్ చేసిన పంట నమూనాలు (${photos.size})" else "Uploaded Harvest Lots (${photos.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (photos.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (language == AppLanguage.TELUGU) "ఇంకా ఎలాంటి పంట ఫోటోలు అప్‌లోడ్ చేయలేదు." else "No crop photos uploaded yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(photos, key = { it.id }) { photo ->
                CropPhotoItemCard(
                    photo = photo,
                    language = language,
                    onDelete = { viewModel.deletePhoto(photo.id) },
                    onDiagnose = {
                        val uri = if (photo.imagePathOrUri.isNotBlank()) Uri.parse(photo.imagePathOrUri) else null
                        viewModel.diagnoseCropImage(uri, photo.cropName, autoSpeak = true)
                    },
                    onShareToCommunity = {
                        viewModel.shareLocalCropPhotoToCommunity(
                            photo = photo,
                            farmerName = "మీరు (రైతు)",
                            village = "స్థానిక గ్రామం",
                            district = "తెలంగాణ / ఆంధ్రప్రదేశ్",
                            variety = "రైతు నాణ్యమైన పంట",
                            notes = photo.notes.ifBlank { "నా పొలంలో పండించిన నాణ్యమైన పంట నమూనా." }
                        )
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                if (language == AppLanguage.TELUGU)
                                    "మీ పంట ఫోటో రైతుల గ్యాలరీలో విజయవంతంగా పంచుకోబడింది!"
                                else
                                    "Photo shared to Community Gallery!"
                            )
                        }
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
}

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
fun CropDiseaseDiagnosisCard(
    diagnosis: DiseaseDiagnosisResult,
    language: AppLanguage,
    onReadAloud: () -> Unit,
    onDismiss: () -> Unit,
    onShareToCommunity: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isSevere = diagnosis.severityLevelEn.contains("Severe", ignoreCase = true)
    val isHealthy = diagnosis.severityLevelEn.contains("Healthy", ignoreCase = true)

    val severityColor = when {
        isHealthy -> AgriGreen
        isSevere -> Color(0xFFC62828)
        else -> Color(0xFFE65100)
    }

    val containerColor = when {
        isHealthy -> AgriGreen.copy(alpha = 0.08f)
        isSevere -> Color(0xFFFFEBEE)
        else -> Color(0xFFFFF3E0)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("disease_diagnosis_result_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(2.dp, severityColor.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Disease Name & Dismiss Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = severityColor,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "🔬 ${AppStrings.get("ml_disease_title", language)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (language == AppLanguage.TELUGU) diagnosis.diseaseNameTe else diagnosis.diseaseNameEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "${diagnosis.cropName} • ${diagnosis.pathogenType}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onReadAloud,
                        modifier = Modifier
                            .background(HarvestGold, CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speak Diagnosis",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges: Severity & Confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = severityColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${AppStrings.get("severity", language)}: ${if (language == AppLanguage.TELUGU) diagnosis.severityLevelTe else diagnosis.severityLevelEn}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = severityColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = "${AppStrings.get("confidence", language)}: ${diagnosis.confidencePercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Symptoms
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = AppStrings.get("symptoms", language),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.TELUGU) diagnosis.symptomsTe else diagnosis.symptomsEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Organic Remedy (సేంద్రీయ నివారణ)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8F5E9), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = AppStrings.get("organic_remedy", language),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AgriGreen
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.TELUGU) diagnosis.organicRemedyTe else diagnosis.organicRemedyEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF1B5E20)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chemical Remedy (రసాయన నివారణ)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFF8E1), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = AppStrings.get("chemical_remedy", language),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.TELUGU) diagnosis.chemicalRemedyTe else diagnosis.chemicalRemedyEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFBF360C)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Preventive Care (ముందస్తు జాగ్రత్తలు)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = AppStrings.get("preventive_care", language),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (language == AppLanguage.TELUGU) diagnosis.preventiveCareTe else diagnosis.preventiveCareEn,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onShareToCommunity,
                colors = ButtonDefaults.buttonColors(containerColor = HarvestGold, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("share_diagnosis_card_button")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (language == AppLanguage.TELUGU) "రైతుల గ్యాలరీలో ఈ నివేదికను పంచుకోండి" else "Share Report to Community Gallery",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CropPhotoItemCard(
    photo: CropPhotoEntity,
    language: AppLanguage,
    onDelete: () -> Unit,
    onDiagnose: () -> Unit,
    onShareToCommunity: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dateStr = remember(photo.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, yyyy - hh:mm a", Locale.getDefault())
        sdf.format(Date(photo.timestamp))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("photo_item_${photo.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (photo.imagePathOrUri.isNotBlank()) {
                    AsyncImage(
                        model = photo.imagePathOrUri,
                        contentDescription = "Crop Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = AgriGreen,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = photo.cropName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = AgriGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Surface(
                    color = AgriGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = photo.gradeTag,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AgriGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${if (language == AppLanguage.TELUGU) "తేదీ: " else "Date: "}$dateStr",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${if (language == AppLanguage.TELUGU) "కుదించిన సైజు: " else "Compressed size: "}${photo.fileSizeBytes / 1024} KB",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Diagnose button for this photo item
                    OutlinedButton(
                        onClick = onDiagnose,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = AgriGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.TELUGU) "నిర్ధారించు (ML)" else "Diagnose (ML)",
                            style = MaterialTheme.typography.labelSmall,
                            color = AgriGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Share to Community Gallery button
                    Button(
                        onClick = onShareToCommunity,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HarvestGold, contentColor = Color.Black),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("share_item_button_${photo.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.TELUGU) "గ్యాలరీలో షేర్" else "Share to Gallery",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
