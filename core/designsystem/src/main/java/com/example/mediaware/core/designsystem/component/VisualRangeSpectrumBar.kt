package com.example.mediaware.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediaware.core.designsystem.theme.ClinicalCaution
import com.example.mediaware.core.designsystem.theme.ClinicalCritical
import com.example.mediaware.core.designsystem.theme.ClinicalNormal
import com.example.mediaware.core.designsystem.util.toBengaliDigits

object SpectrumMath {
    /**
     * Piecewise coordinate calculation mapping lab values into normalized [0f, 1f]
     * Segment 1 (Normal): [0.0f - 0.50f]
     * Segment 2 (Borderline): [0.50f - 0.75f]
     * Segment 3 (Critical High): [0.75f - 1.00f]
     */
    fun calculatePinFraction(
        value: Double,
        normalMin: Double,
        normalMax: Double,
        criticalThreshold: Double
    ): Float {
        if (normalMax <= normalMin || criticalThreshold <= normalMax) {
            // Fallback: standard linear clamping if ranges are undefined
            return ((value - normalMin) / (criticalThreshold - normalMin).coerceAtLeast(1.0))
                .coerceIn(0.0, 1.0)
                .toFloat()
        }

        return when {
            value <= normalMin -> 0.05f
            value <= normalMax -> {
                val fraction = ((value - normalMin) / (normalMax - normalMin)).coerceIn(0.0, 1.0)
                (0.05 + (fraction * 0.45)).toFloat()
            }
            value <= criticalThreshold -> {
                val fraction = ((value - normalMax) / (criticalThreshold - normalMax)).coerceIn(0.0, 1.0)
                (0.50 + (fraction * 0.25)).toFloat()
            }
            else -> {
                val excess = ((value - criticalThreshold) / criticalThreshold).coerceIn(0.0, 1.0)
                (0.75 + (excess * 0.20)).toFloat().coerceAtMost(0.95f)
            }
        }
    }
}

@Composable
fun VisualRangeSpectrumBar(
    value: Double,
    unit: String,
    normalMin: Double,
    normalMax: Double,
    criticalThreshold: Double,
    modifier: Modifier = Modifier
) {
    val pinFraction = SpectrumMath.calculatePinFraction(
        value = value,
        normalMin = normalMin,
        normalMax = normalMax,
        criticalThreshold = criticalThreshold
    )

    val currentStatusColor = when {
        value <= normalMax -> ClinicalNormal
        value <= criticalThreshold -> ClinicalCaution
        else -> ClinicalCritical
    }

    val valueBn = value.toString().toBengaliDigits()
    val minBn = normalMin.toString().toBengaliDigits()
    val maxBn = normalMax.toString().toBengaliDigits()
    val critBn = criticalThreshold.toString().toBengaliDigits()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Value badge and pointer triangle
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
        ) {
            val totalWidthPx = constraints.maxWidth.toFloat()
            val pinXPx = totalWidthPx * pinFraction
            val badgeWidthDp = 76.dp

            // Pin triangle and badge positioned directly over the fraction coordinate
            Box(
                modifier = Modifier
                    .offset(x = (maxWidth * pinFraction) - (badgeWidthDp / 2))
                    .width(badgeWidthDp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(currentStatusColor)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$valueBn $unit",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Downward pointing arrow triangle
                    Canvas(modifier = Modifier.size(width = 10.dp, height = 6.dp)) {
                        val path = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, 0f)
                            lineTo(size.width / 2f, size.height)
                            close()
                        }
                        drawPath(path = path, color = currentStatusColor)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 3-Zone Continuous Canvas Bar
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        ) {
            val width = size.width
            val height = size.height
            val normalWidth = width * 0.50f
            val cautionWidth = width * 0.25f
            val criticalWidth = width * 0.25f

            // Green Zone (Normal: 0.0 -> 0.50)
            drawRoundRect(
                color = ClinicalNormal,
                topLeft = Offset(0f, 0f),
                size = Size(normalWidth, height),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Amber Zone (Borderline: 0.50 -> 0.75)
            drawRect(
                color = ClinicalCaution,
                topLeft = Offset(normalWidth, 0f),
                size = Size(cautionWidth, height)
            )

            // Red Zone (Critical: 0.75 -> 1.00)
            drawRoundRect(
                color = ClinicalCritical,
                topLeft = Offset(normalWidth + cautionWidth, 0f),
                size = Size(criticalWidth, height),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Range Labels below the bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "🟢 স্বাভাবিক ($minBn-$maxBn)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = ClinicalNormal,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = "🟡 সতর্কসীমা ($maxBn-$critBn)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = ClinicalCaution,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Text(
                text = "🔴 বিপজ্জনক ($critBn+)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = ClinicalCritical,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}
