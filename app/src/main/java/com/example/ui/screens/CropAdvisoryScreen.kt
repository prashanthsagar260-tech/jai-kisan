package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.AppStrings
import com.example.data.model.CropCatalogEntity
import com.example.ui.components.AudioReadoutButton
import com.example.ui.theme.AgriGreen
import com.example.ui.theme.EarthBrown
import com.example.ui.theme.HarvestGold
import com.example.ui.viewmodel.KisanViewModel

@Composable
fun CropAdvisoryScreen(
    viewModel: KisanViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val crops by viewModel.crops.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var expandedCropId by remember { mutableStateOf<String?>("chilli") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("crop_advisory_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner introducing Crop Advisory module
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AgriGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.TELUGU) "రైతు పంటల సమగ్ర సాగు సలహాలు" else "Comprehensive Crop Advisory",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (language == AppLanguage.TELUGU)
                                "విత్తే సమయం, నేల తయారీ, నీటి యాజమాన్యం, ఎరువుల షెడ్యూల్ మరియు సేంద్రీయ/రసాయన తెగుళ్ల నివారణ"
                            else
                                "Planting times, soil prep, irrigation, fertilizer schedules & pest remedies",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // Search input
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_crop_advisory_input"),
                placeholder = { Text(AppStrings.get("search_crop", language)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = AgriGreen)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Crops List
        items(crops, key = { it.cropId }) { crop ->
            val isExpanded = expandedCropId == crop.cropId
            CropAdvisoryCard(
                crop = crop,
                language = language,
                isExpanded = isExpanded,
                onToggleExpand = {
                    expandedCropId = if (isExpanded) null else crop.cropId
                },
                onReadAloud = {
                    val fullAdvisoryText = if (language == AppLanguage.TELUGU) {
                        "${crop.nameTe} సాగు సలహా: విత్తే సమయం: ${crop.plantingTimeTe}. నేల తయారీ: ${crop.soilPreparationTe}. నీటి తడులు: ${crop.irrigationNeedsTe}. ఎరువుల మోతాదు: ${crop.fertilizerScheduleTe}. సేంద్రీయ నివారణ: ${crop.organicTreatmentTe}. రసాయన నివారణ: ${crop.chemicalTreatmentTe}."
                    } else {
                        "${crop.nameEn} Advisory: Planting time: ${crop.plantingTimeEn}. Soil Prep: ${crop.soilPreparationEn}. Irrigation: ${crop.irrigationNeedsEn}. Fertilizers: ${crop.fertilizerScheduleEn}. Organic Treatment: ${crop.organicTreatmentEn}. Chemical Treatment: ${crop.chemicalTreatmentEn}."
                    }
                    viewModel.readAloud(fullAdvisoryText)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CropAdvisoryCard(
    crop: CropCatalogEntity,
    language: AppLanguage,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onReadAloud: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("crop_card_${crop.cropId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, if (isExpanded) AgriGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(AgriGreen.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = crop.iconEmoji, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.TELUGU) crop.nameTe else crop.nameEn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${if (language == AppLanguage.TELUGU) "ధర పరిధి" else "Price Range"}: ${crop.typicalPriceRange}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AudioReadoutButton(
                        onClick = onReadAloud,
                        language = language
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Info summary chips
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = AgriGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.TELUGU) crop.seasonTe else crop.seasonEn,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = HarvestGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.TELUGU) "తెలంగాణ & ఏపీ అనుకూలం" else "TG & AP Recommended",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // Expanded Detailed Advisory Content
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Planting Time
                    AdvisorySection(
                        icon = Icons.Default.DateRange,
                        title = if (language == AppLanguage.TELUGU) "1. విత్తే సమయం & నారు పెంపకం" else "1. Recommended Planting Time",
                        content = if (language == AppLanguage.TELUGU) crop.plantingTimeTe else crop.plantingTimeEn,
                        accentColor = AgriGreen
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Soil Preparation
                    AdvisorySection(
                        icon = Icons.Default.Grass,
                        title = if (language == AppLanguage.TELUGU) "2. నేల తయారీ & ఎరువుల నిర్వహణ" else "2. Soil Preparation",
                        content = if (language == AppLanguage.TELUGU) crop.soilPreparationTe else crop.soilPreparationEn,
                        accentColor = EarthBrown
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Irrigation Needs
                    AdvisorySection(
                        icon = Icons.Default.WaterDrop,
                        title = if (language == AppLanguage.TELUGU) "3. నీటి యాజమాన్యం & ముఖ్య తడులు" else "3. Irrigation Needs",
                        content = if (language == AppLanguage.TELUGU) crop.irrigationNeedsTe else crop.irrigationNeedsEn,
                        accentColor = Color(0xFF0288D1)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Fertilization Schedule
                    AdvisorySection(
                        icon = Icons.Default.Science,
                        title = if (language == AppLanguage.TELUGU) "4. ఎరువుల షెడ్యూల్ (NPK మోతాదు)" else "4. Fertilization Schedule",
                        content = if (language == AppLanguage.TELUGU) crop.fertilizerScheduleTe else crop.fertilizerScheduleEn,
                        accentColor = Color(0xFF6A1B9A)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. Common Pests & Diseases
                    AdvisorySection(
                        icon = Icons.Default.BugReport,
                        title = if (language == AppLanguage.TELUGU) "5. సాధారణ తెగుళ్లు & పురుగులు" else "5. Common Pests & Diseases",
                        content = if (language == AppLanguage.TELUGU) crop.commonPestsTe else crop.commonPestsEn,
                        accentColor = Color(0xFFC62828)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 6. Organic Treatment (Natural & Eco-Friendly)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        border = BorderStroke(1.dp, Color(0xFFA5D6A7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = AgriGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.TELUGU) "🌱 సేంద్రీయ / ప్రకృతి నివారణ పద్ధతులు" else "🌱 Organic Treatment Options",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (language == AppLanguage.TELUGU) crop.organicTreatmentTe else crop.organicTreatmentEn,
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp,
                                color = Color(0xFF1B5E20)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 7. Chemical Treatment (Chemical & Dosage)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                        border = BorderStroke(1.dp, Color(0xFFFFE082)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = HarvestGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.TELUGU) "🛡️ రసాయన నివారణ & లీటరు మోతాదు" else "🛡️ Chemical Treatment & Dosage",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (language == AppLanguage.TELUGU) crop.chemicalTreatmentTe else crop.chemicalTreatmentEn,
                                style = MaterialTheme.typography.bodySmall,
                                lineHeight = 18.sp,
                                color = Color(0xFF3E2723)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Localized Agro-Climatic Zone
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (language == AppLanguage.TELUGU) "అనుకూల మండలాలు: " else "Agro-zone: "}${if (language == AppLanguage.TELUGU) crop.localizedZoneTe else crop.localizedZoneEn}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdvisorySection(
    icon: ImageVector,
    title: String,
    content: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 22.dp)
        )
    }
}
