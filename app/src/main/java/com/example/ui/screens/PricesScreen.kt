package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.AppStrings
import com.example.data.model.MandiPriceEntity
import com.example.ui.components.AudioReadoutButton
import com.example.ui.components.ConfidenceBadge
import com.example.ui.components.PriceTrendCanvas
import com.example.ui.components.WeatherAdvisoryCard
import com.example.ui.theme.AgriGreen
import com.example.ui.theme.HarvestGold
import com.example.ui.theme.TrendDownRed
import com.example.ui.theme.TrendUpGreen
import com.example.ui.viewmodel.KisanViewModel
import com.example.ui.viewmodel.TrendTimeframe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricesScreen(
    viewModel: KisanViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val prices by viewModel.prices.collectAsState()
    val weather by viewModel.weather.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedPrice by viewModel.selectedPrice.collectAsState()
    val selectedTrend by viewModel.selectedTrend.collectAsState()
    val activeTtsSpeaking by viewModel.activeTtsSpeaking.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val latestPriceTimestamp by viewModel.latestPriceTimestamp.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val lastPriceSyncStr = remember(latestPriceTimestamp) {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.format(Date(latestPriceTimestamp ?: System.currentTimeMillis()))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("prices_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Module 10: Climate & Weather Prediction Card with Room offline cache sync
        item {
            WeatherAdvisoryCard(
                weather = weather,
                language = language,
                onRefresh = { viewModel.refreshData() },
                isRefreshing = isRefreshing,
                onReadAloud = {
                    val spoken = if (language == AppLanguage.TELUGU) {
                        "నేటి వాతావరణం: ${weather.locationTe}. ఉష్ణోగ్రత ${weather.tempCelsius} డిగ్రీలు. ${weather.conditionTe}. వర్ష సూచన ${weather.rainProbabilityPercent} శాతం. సలహా: ${weather.advisoryTe}"
                    } else {
                        "Today's Weather: ${weather.locationEn}. Temperature ${weather.tempCelsius} degrees. ${weather.conditionEn}. Rain probability ${weather.rainProbabilityPercent} percent. Advisory: ${weather.advisoryEn}"
                    }
                    viewModel.readAloud(spoken)
                }
            )
        }

        // Room Database Offline Cache Status Banner
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AgriGreen.copy(alpha = 0.08f)),
                border = BorderStroke(1.2.dp, AgriGreen.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("room_offline_cache_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = AgriGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (language == AppLanguage.TELUGU)
                                        "రూమ్ ఆఫ్‌లైన్ నిల్వ చురుగ్గా ఉంది"
                                    else
                                        "Room Database Offline Cache Active",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AgriGreen
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.OfflinePin,
                                    contentDescription = null,
                                    tint = AgriGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = if (language == AppLanguage.TELUGU)
                                    "చివరి అప్‌డేట్: $lastPriceSyncStr • ఇంటర్నెట్ లేకపోయినా ధరలు లభ్యం"
                                else
                                    "Last synced: $lastPriceSyncStr • Prices available offline",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.refreshData() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("refresh_mandi_prices_button")
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = AgriGreen
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Data",
                                tint = AgriGreen
                            )
                        }
                    }
                }
            }
        }

        // Search bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_crop_input"),
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

        // Section header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.get("today_prices", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${prices.size} ${if (language == AppLanguage.TELUGU) "రకాలు" else "items"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Module 1: Mandi Price cards
        items(prices, key = { it.id }) { item ->
            MandiPriceCard(
                price = item,
                language = language,
                onClick = { viewModel.selectPrice(item) },
                onReadAloud = {
                    val text = if (language == AppLanguage.TELUGU) {
                        "${item.cropNameTe}, ${item.marketNameTe}లో సగటు ధర క్వింటాల్‌కు ₹${item.modalPrice}. కనిష్ట ధర ₹${item.minPrice}, గరిష్ట ధర ₹${item.maxPrice}. రాబోయే వారం అంచనా ₹${item.predictedPriceNextWeek}."
                    } else {
                        "${item.cropNameEn} at ${item.marketNameEn}. Modal price ₹${item.modalPrice} per quintal. Min ₹${item.minPrice}, Max ₹${item.maxPrice}. Next week forecast is ₹${item.predictedPriceNextWeek}."
                    }
                    viewModel.readAloud(text)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Modal BottomSheet for Module 2 (Trend Graphs) and Module 9 (Price Prediction)
    if (selectedPrice != null) {
        val item = selectedPrice!!
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectPrice(null) },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            PriceDetailAndTrendSheet(
                price = item,
                language = language,
                selectedTrend = selectedTrend,
                onSelectTrend = { viewModel.setTrendTimeframe(it) },
                onReadAloud = {
                    val forecastText = if (language == AppLanguage.TELUGU) {
                        "${item.cropNameTe} ధరల విశ్లేషణ: ప్రస్తుత ధర ₹${item.modalPrice}. రాబోయే వారం అంచనా ధర ₹${item.predictedPriceNextWeek}. విశ్వసనీయత ${item.predictionConfidencePercent} శాతం."
                    } else {
                        "${item.cropNameEn} analysis: Current price ₹${item.modalPrice}. Next week forecasted price ₹${item.predictedPriceNextWeek} with ${item.predictionConfidencePercent}% confidence."
                    }
                    viewModel.readAloud(forecastText)
                },
                onClose = { viewModel.selectPrice(null) }
            )
        }
    }
}

@Composable
fun MandiPriceCard(
    price: MandiPriceEntity,
    language: AppLanguage,
    onClick: () -> Unit,
    onReadAloud: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("price_card_${price.cropId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(AgriGreen.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = price.iconEmoji, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.TELUGU) price.cropNameTe else price.cropNameEn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (language == AppLanguage.TELUGU) price.marketNameTe else price.marketNameEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                AudioReadoutButton(
                    onClick = onReadAloud,
                    language = language
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = AppStrings.get("tab_prices", language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹${price.modalPrice}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = " / Q",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Price change badge
                val isPositive = price.priceChange >= 0
                val changeColor = if (isPositive) TrendUpGreen else TrendDownRed
                Surface(
                    color = changeColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = changeColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${if (isPositive) "+" else ""}₹${price.priceChange}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = changeColor
                        )
                    }
                }

                // Min - Max Range
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (language == AppLanguage.TELUGU) "కనిష్ట" else "Min"}: ₹${price.minPrice}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${if (language == AppLanguage.TELUGU) "గరిష్ట" else "Max"}: ₹${price.maxPrice}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Forecast pill preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = AgriGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${if (language == AppLanguage.TELUGU) "వచ్చే వారం అంచనా" else "Next Week Forecast"}: ₹${price.predictedPriceNextWeek}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.TELUGU) "గ్రాఫ్ చూడండి" else "View Graph",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = AgriGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.TELUGU) "రూమ్ ఆఫ్‌లైన్ నిల్వ (Room DB)" else "Room DB Offline Cached",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AgriGreen
                    )
                }

                Text(
                    text = "${if (language == AppLanguage.TELUGU) "రాక: " else "Arrivals: "}${price.dailyArrivalsTons} T",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PriceDetailAndTrendSheet(
    price: MandiPriceEntity,
    language: AppLanguage,
    selectedTrend: TrendTimeframe,
    onSelectTrend: (TrendTimeframe) -> Unit,
    onReadAloud: () -> Unit,
    onClose: () -> Unit
) {
    val history7List = price.history7DaysCsv.split(",").mapNotNull { it.trim().toIntOrNull() }
    val history30List = price.history30DaysCsv.split(",").mapNotNull { it.trim().toIntOrNull() }

    val points = when (selectedTrend) {
        TrendTimeframe.SEVEN_DAYS -> history7List
        TrendTimeframe.THIRTY_DAYS -> history30List
        TrendTimeframe.SEASONAL -> history30List
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("price_detail_bottom_sheet")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = price.iconEmoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (language == AppLanguage.TELUGU) price.cropNameTe else price.cropNameEn,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (language == AppLanguage.TELUGU) price.marketNameTe else price.marketNameEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AudioReadoutButton(onClick = onReadAloud, language = language)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Timeframe selector filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedTrend == TrendTimeframe.SEVEN_DAYS,
                onClick = { onSelectTrend(TrendTimeframe.SEVEN_DAYS) },
                label = { Text(AppStrings.get("seven_days", language)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AgriGreen,
                    selectedLabelColor = Color.White
                )
            )

            FilterChip(
                selected = selectedTrend == TrendTimeframe.THIRTY_DAYS,
                onClick = { onSelectTrend(TrendTimeframe.THIRTY_DAYS) },
                label = { Text(AppStrings.get("thirty_days", language)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AgriGreen,
                    selectedLabelColor = Color.White
                )
            )

            FilterChip(
                selected = selectedTrend == TrendTimeframe.SEASONAL,
                onClick = { onSelectTrend(TrendTimeframe.SEASONAL) },
                label = { Text(AppStrings.get("seasonal", language)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AgriGreen,
                    selectedLabelColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Module 2: Price Trend Canvas
        PriceTrendCanvas(
            pricePoints = points,
            timeframeLabel = when (selectedTrend) {
                TrendTimeframe.SEVEN_DAYS -> AppStrings.get("seven_days", language)
                TrendTimeframe.THIRTY_DAYS -> AppStrings.get("thirty_days", language)
                TrendTimeframe.SEASONAL -> AppStrings.get("seasonal", language)
            },
            language = language
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Module 9: Price Prediction Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppStrings.get("price_prediction", language),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    ConfidenceBadge(
                        confidencePercent = price.predictionConfidencePercent,
                        language = language
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${price.predictedPriceNextWeek} / Q",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = AgriGreen
                    )

                    val delta = price.predictedPriceNextWeek - price.modalPrice
                    val deltaLabel = if (delta >= 0) "+₹$delta" else "-₹${-delta}"
                    Text(
                        text = "${if (language == AppLanguage.TELUGU) "మార్పు అంచనా" else "Expected Change"}: $deltaLabel",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (delta >= 0) TrendUpGreen else TrendDownRed
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = AppStrings.get("prediction_disclaimer", language),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
