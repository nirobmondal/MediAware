package com.example.mediaware.features.history.presentation.timeline

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.features.history.domain.model.VitalRecord
import com.example.mediaware.features.history.domain.model.VitalType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthTimelineScreen(
    viewModel: HealthTimelineViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "স্বাস্থ্য স্মৃতি ও প্রগ্রেস",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "দীর্ঘমেয়াদী স্বাস্থ্যগত পরিবর্তনের গল্প",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(HealthTimelineUiEvent.OnOpenAddDialog) },
                containerColor = PrimaryTeal,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "নতুন রিডিং যুক্ত করুন")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Mandatory Clinical Safety Disclaimer (Guardrail #3)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⚠️ এটি কোনো রোগ নির্ণয় নয়। আপনার চিকিৎসকের সাথে চার্ট ও মান পর্যালোচনা করুন।",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE65100),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Vital Type Tab Selector Chips
            item {
                SecondaryScrollableTabRow(
                    selectedTabIndex = VitalType.values().indexOf(uiState.selectedVitalType),
                    edgePadding = 0.dp,
                    divider = {},
                    containerColor = Color.Transparent
                ) {
                    VitalType.values().forEach { type ->
                        val isSelected = uiState.selectedVitalType == type
                        Tab(
                            selected = isSelected,
                            onClick = { viewModel.onEvent(HealthTimelineUiEvent.OnSelectVitalType(type)) },
                            text = {
                                Text(
                                    text = type.labelBn,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) PrimaryTeal else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
            }

            // Narrative Story Trend Card (Cognitive Psychology Engine)
            item {
                val trend = uiState.trend
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (trend?.isImproving == true) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (trend?.isImproving == true) Color(0xFF81C784) else Color(0xFFFFB74D)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (trend?.isImproving == true) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color(0xFFE65100).copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (trend?.directionBn == "উর্ধ্বমুখী") Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (trend?.isImproving == true) Color(0xFF2E7D32) else Color(0xFFE65100),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "পরিবর্তনের বিবরণ:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (trend?.isImproving == true) Color(0xFF2E7D32) else Color(0xFFE65100)
                                )
                                Surface(
                                    color = if (trend?.isImproving == true) Color(0xFF2E7D32) else Color(0xFFE65100),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = trend?.directionBn ?: "স্থিতিশীল",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = trend?.narrativeBn ?: "কমপক্ষে দুটি রিডিং যুক্ত হলে তুলনামূলক প্রগ্রেস দৃশ্যমান হবে।",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Compose Canvas Trend Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "গ্রাফ চার্ট (${uiState.selectedVitalType.unit})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "স্বাভাবিক: ${uiState.selectedVitalType.normalMin.toInt().toString().toBengaliDigits()} - ${uiState.selectedVitalType.normalMax.toInt().toString().toBengaliDigits()}",
                                fontSize = 11.sp,
                                color = PrimaryTeal,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        VitalsCanvasChart(
                            records = uiState.records,
                            vitalType = uiState.selectedVitalType,
                            selectedIndex = uiState.selectedPointIndex,
                            onPointSelected = { viewModel.onEvent(HealthTimelineUiEvent.OnSelectDataPoint(it)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                        )
                    }
                }
            }

            // Historical Readings Header
            item {
                Text(
                    text = "পরিমাপের ইতিহাস (${uiState.records.size.toString().toBengaliDigits()}টি তথ্য)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (uiState.records.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "এখনও কোনো পরিমাপ যোগ করা হয়নি",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "নিচের + (প্লাস) বাটনে চাপ দিয়ে আপনার প্রথম রিডিং যুক্ত করুন।",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // List of Historical Readings
            itemsIndexed(uiState.records) { index, record ->
                val isSelected = uiState.selectedPointIndex == index
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.onEvent(HealthTimelineUiEvent.OnSelectDataPoint(index)) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) PrimaryTeal.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) PrimaryTeal else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(if (isSelected) PrimaryTeal else MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = record.dateFormattedBn,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = record.notesBn,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "${record.value.toInt().toString().toBengaliDigits()} ${record.vitalType.unit}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp)) // FAB spacing
            }
        }
    }

    if (uiState.showAddDialog) {
        AddVitalReadingDialog(
            vitalType = uiState.selectedVitalType,
            onDismiss = { viewModel.onEvent(HealthTimelineUiEvent.OnDismissAddDialog) },
            onConfirm = { value, notes ->
                viewModel.onEvent(HealthTimelineUiEvent.OnAddReading(value, notes))
            }
        )
    }
}

@Composable
fun VitalsCanvasChart(
    records: List<VitalRecord>,
    vitalType: VitalType,
    selectedIndex: Int?,
    onPointSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (records.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("কোনো রিডিং পাওয়া যায়নি", fontSize = 12.sp, color = Color.Gray)
        }
        return
    }

    val minVal = (records.minOfOrNull { it.value } ?: 0.0) * 0.85
    val maxVal = (records.maxOfOrNull { it.value } ?: 100.0) * 1.15
    val range = (maxVal - minVal).coerceAtLeast(1.0)

    Canvas(
        modifier = modifier.pointerInput(records) {
            detectTapGestures { tapOffset ->
                val stepX = size.width / (records.size - 1).coerceAtLeast(1)
                val clickedIndex = ((tapOffset.x + (stepX / 2)) / stepX).toInt().coerceIn(0, records.lastIndex)
                onPointSelected(clickedIndex)
            }
        }
    ) {
        val width = size.width
        val height = size.height
        val bottomPadding = 30f
        val topPadding = 20f
        val graphHeight = height - bottomPadding - topPadding

        // 1. Draw horizontal reference grid lines
        val gridLines = 3
        for (i in 0..gridLines) {
            val y = topPadding + (graphHeight * i / gridLines)
            drawLine(
                color = Color.LightGray.copy(alpha = 0.4f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        // 2. Compute point coordinates
        val points = records.mapIndexed { idx, record ->
            val x = if (records.size > 1) idx * (width / (records.size - 1)) else width / 2
            val fraction = ((record.value - minVal) / range).toFloat().coerceIn(0f, 1f)
            val y = topPadding + graphHeight * (1f - fraction)
            Offset(x, y)
        }

        // 3. Draw gradient fill below curve
        if (points.size > 1) {
            val fillPath = Path().apply {
                moveTo(points.first().x, topPadding + graphHeight)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, topPadding + graphHeight)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PrimaryTeal.copy(alpha = 0.25f),
                        PrimaryTeal.copy(alpha = 0.02f)
                    ),
                    startY = topPadding,
                    endY = topPadding + graphHeight
                )
            )

            // 4. Draw connecting stroke line
            val strokePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    val midX = (prev.x + curr.x) / 2
                    cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                }
            }
            drawPath(
                path = strokePath,
                color = PrimaryTeal,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // 5. Draw point dots and highlight selected
        points.forEachIndexed { idx, pt ->
            val isSelected = selectedIndex == idx
            // Outer ring
            drawCircle(
                color = if (isSelected) Color(0xFFBA1A1A) else PrimaryTeal,
                radius = if (isSelected) 8.dp.toPx() else 5.dp.toPx(),
                center = pt
            )
            // Inner white dot
            drawCircle(
                color = Color.White,
                radius = if (isSelected) 4.dp.toPx() else 2.5f.dp.toPx(),
                center = pt
            )
        }
    }
}

@Composable
fun AddVitalReadingDialog(
    vitalType: VitalType,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var valueText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${vitalType.labelBn} পরিমাপ যুক্ত করুন",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = valueText,
                    onValueChange = {
                        valueText = it
                        isError = false
                    },
                    label = { Text("পরিমাপের মান (${vitalType.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text("অনুগ্রহ করে সঠিক সংখ্যা লিখুন", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("মন্তব্য (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: খালি পেটে / ঔষধের পর") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = valueText.toDoubleOrNull()
                    if (parsed != null && parsed > 0) {
                        onConfirm(parsed, notesText)
                    } else {
                        isError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
