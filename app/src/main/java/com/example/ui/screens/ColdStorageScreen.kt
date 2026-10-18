package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.AppStrings
import com.example.data.model.ColdStorageEntity
import com.example.ui.components.AudioReadoutButton
import com.example.ui.theme.AgriGreen
import com.example.ui.theme.HarvestGold
import com.example.ui.viewmodel.KisanViewModel

@Composable
fun ColdStorageScreen(
    viewModel: KisanViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val coldStorages by viewModel.coldStorages.collectAsState()
    val onlyPerishables by viewModel.onlyPerishables.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("cold_storage_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Banner
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
                                imageVector = Icons.Default.AcUnit,
                                contentDescription = null,
                                tint = Color(0xFF0288D1),
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.get("cold_storage_title", language),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        AudioReadoutButton(
                            onClick = {
                                val text = if (language == AppLanguage.TELUGU) {
                                    "శీతల గిడ్డంగి సమాచారం: మొత్తం ${coldStorages.size} కోల్డ్ స్టోరేజీలు అందుబాటులో ఉన్నాయి. నెలవారీ అద్దె క్వింటాల్‌కు ₹105 నుండి ₹120 వరకు ఉంది. మిర్చి మరియు పసుపు నిల్వకు అనుకూలం."
                                } else {
                                    "Cold Storage Info: ${coldStorages.size} facilities available. Monthly rent ranges from ₹105 to ₹120 per quintal. Suitable for chilli and turmeric."
                                }
                                viewModel.readAloud(text)
                            },
                            language = language
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (language == AppLanguage.TELUGU)
                            "పంట ధర తగ్గినప్పుడు అమ్ముకోకుండా కోల్డ్ స్టోరేజ్ లో నిల్వ చేసుకొని మంచి ధర వచ్చినప్పుడు అమ్ముకోవచ్చు."
                        else
                            "Store produce safely when market prices drop and sell when demand rises.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Perishable toggle chip
                    FilterChip(
                        selected = onlyPerishables,
                        onClick = { viewModel.onlyPerishables.value = !onlyPerishables },
                        label = {
                            Text(
                                text = AppStrings.get("perishable_filter", language),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AgriGreen,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("perishable_filter_chip")
                    )
                }
            }
        }

        // Cold Storage Items
        items(coldStorages, key = { it.id }) { item ->
            ColdStorageItemCard(
                storage = item,
                language = language,
                onCall = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${item.contactPhone}")
                    }
                    context.startActivity(intent)
                },
                onReadAloud = {
                    val spoken = if (language == AppLanguage.TELUGU) {
                        "${item.nameTe}. దూరం ${item.distanceKm} కి.మీ. అందుబాటులో ఉన్న నిల్వ ${item.availableCapacityMt} మెట్రిక్ టన్నులు. నెలవారీ అద్దె క్వింటాల్‌కు ₹${item.ratePerQuintalMonth}. ఫోన్: ${item.contactPhone}."
                    } else {
                        "${item.nameEn}. Distance ${item.distanceKm} km. Available space ${item.availableCapacityMt} MT. Rate ₹${item.ratePerQuintalMonth} per quintal monthly. Phone: ${item.contactPhone}."
                    }
                    viewModel.readAloud(spoken)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ColdStorageItemCard(
    storage: ColdStorageEntity,
    language: AppLanguage,
    onCall: () -> Unit,
    onReadAloud: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cold_storage_card_${storage.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == AppLanguage.TELUGU) storage.nameTe else storage.nameEn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (language == AppLanguage.TELUGU) storage.locationTe else storage.locationEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = Color(0xFF0288D1).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${storage.distanceKm} km",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0288D1)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Capacity & Monthly Rate Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = AppStrings.get("available_capacity", language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = AgriGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${storage.availableCapacityMt} / ${storage.totalCapacityMt} MT",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = AgriGreen
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = AppStrings.get("monthly_rent", language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${storage.ratePerQuintalMonth} / Q",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Temperature range & suitable crops
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = null,
                    tint = HarvestGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${if (language == AppLanguage.TELUGU) "ఉష్ణోగ్రత: " else "Temp: "}${storage.tempRange}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${if (language == AppLanguage.TELUGU) "అనుకూల పంటలు: " else "Suitable crops: "}${if (language == AppLanguage.TELUGU) storage.suitableCropsTe else storage.suitableCropsEn}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onCall,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGold),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.TELUGU) "గిడ్డంగికి కాల్ చేయండి" else "Call Storage",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                AudioReadoutButton(
                    onClick = onReadAloud,
                    language = language
                )
            }
        }
    }
}
