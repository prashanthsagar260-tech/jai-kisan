package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.AgriGreen
import com.example.ui.theme.HarvestGold

@Composable
fun PriceTrendCanvas(
    pricePoints: List<Int>,
    timeframeLabel: String,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    if (pricePoints.isEmpty()) return

    val minVal = pricePoints.minOrNull() ?: 0
    val maxVal = pricePoints.maxOrNull() ?: 1
    val range = (maxVal - minVal).coerceAtLeast(1)

    var selectedIndex by remember(pricePoints) { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
            .testTag("price_trend_graph_container")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = timeframeLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            val selectedPrice = selectedIndex?.let { pricePoints.getOrNull(it) } ?: pricePoints.last()
            Surface(
                color = AgriGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "₹ $selectedPrice / Q",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = AgriGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .testTag("price_trend_canvas")
                    .pointerInput(pricePoints) {
                        detectTapGestures { offset ->
                            val stepX = size.width / (pricePoints.size - 1).coerceAtLeast(1)
                            val index = ((offset.x + stepX / 2) / stepX).toInt()
                            selectedIndex = index.coerceIn(0, pricePoints.size - 1)
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val paddingVertical = 24f
                val effectiveHeight = canvasHeight - (paddingVertical * 2)
                val stepX = canvasWidth / (pricePoints.size - 1).coerceAtLeast(1)

                // Background horizontal guidelines
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = paddingVertical + (effectiveHeight / gridLines) * i
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.25f),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Construct smooth curve path
                val points = pricePoints.mapIndexed { i, price ->
                    val normY = (price - minVal).toFloat() / range.toFloat()
                    val x = i * stepX
                    val y = canvasHeight - paddingVertical - (normY * effectiveHeight)
                    Offset(x, y)
                }

                val linePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }

                // Draw main trend stroke
                drawPath(
                    path = linePath,
                    color = AgriGreen,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw circular markers at each point
                points.forEachIndexed { idx, point ->
                    val isSelected = selectedIndex == idx || (selectedIndex == null && idx == points.size - 1)
                    val radius = if (isSelected) 7.dp.toPx() else 4.dp.toPx()
                    val markerColor = if (isSelected) HarvestGold else AgriGreen

                    drawCircle(
                        color = Color.White,
                        radius = radius + 2.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = markerColor,
                        radius = radius,
                        center = point
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (language == AppLanguage.TELUGU) "కనిష్ట: ₹$minVal" else "Min: ₹$minVal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (language == AppLanguage.TELUGU) "గరిష్ట: ₹$maxVal" else "Max: ₹$maxVal",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
