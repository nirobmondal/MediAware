package com.example.mediaware.features.history.presentation.adherence

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.features.history.domain.model.AdherenceDay
import com.example.mediaware.features.history.domain.model.AdherenceRecord
import com.example.mediaware.features.history.domain.model.AdherenceStatus
import com.example.mediaware.features.history.domain.model.TitrationRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineHistoryScreen(
    viewModel: MedicineHistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ওষুধের ইতিহাস ও নিয়মানুবর্তিতা",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ডোজ টাইট্রেশন ও নিয়মিত ওষুধ সেবন ট্র্যাকার",
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
                // Mandatory Clinical Disclaimer (Guardrail #2 & #3)
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
                            text = "এটি কোনো প্রেসক্রিপশন পরিবর্তনের নির্দেশনা নয়। ডাক্তারের পরামর্শ ছাড়া ডোজ পরিবর্তন করবেন না।",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE65100),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // Compliance Score Metric Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(PrimaryTeal.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${uiState.adherencePercentage.toString().toBengaliDigits()}%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "গত ৩০ দিনের সেবন হার",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatusBadge(label = "${uiState.dosesTaken.toString().toBengaliDigits()} গৃহীত", color = Color(0xFF2E7D32))
                                StatusBadge(label = "${uiState.dosesMissed.toString().toBengaliDigits()} মিস", color = Color(0xFFBA1A1A))
                                StatusBadge(label = "${uiState.dosesSnoozed.toString().toBengaliDigits()} স্থগিত", color = Color(0xFFE65100))
                            }
                        }
                    }
                }
            }

            // 30-Day Monthly Calendar Grid View
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
                                text = "মাসিক সেবন ক্যালেন্ডার",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "দিন: ${uiState.selectedDay.toString().toBengaliDigits()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryTeal
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Weekday Headers
                        val weekdays = listOf("শনি", "রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            weekdays.forEach { dayName ->
                                Text(
                                    text = dayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Calendar Day Circles Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier.height(180.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            userScrollEnabled = false
                        ) {
                            items(uiState.calendarDays) { day ->
                                CalendarDayItem(
                                    day = day,
                                    isSelected = uiState.selectedDay == day.dayOfMonth,
                                    onClick = { viewModel.onEvent(MedicineHistoryUiEvent.OnSelectDay(day.dayOfMonth)) }
                                )
                            }
                        }
                    }
                }
            }

            // Medication Titration Evolution Log
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = PrimaryTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ডোজ টাইট্রেশন ইতিহাস",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (uiState.titrationHistory.isEmpty()) {
                            Text(
                                text = "কোনো ডোজ পরিবর্তনের ইতিহাস সংরক্ষিত নেই।",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            uiState.titrationHistory.forEach { titration ->
                                TitrationItemRow(titration = titration)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            // Today's Scheduled Dose Status Header
            item {
                Text(
                    text = "আজকের নির্ধারিত ডোজ তালিকা",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (uiState.todayRecords.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "আজকের জন্য কোনো নির্ধারিত ডোজ নেই",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "নতুন প্রেসক্রিপশন স্ক্যান করে রুটিন শিডিউল তৈরি করলে এখানে ডোজের তালিকা দেখতে পাবেন।",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Today's Scheduled Dose Items
            items(uiState.todayRecords, key = { it.id }) { record ->
                AdherenceRecordRow(
                    record = record,
                    onStatusSelected = { newStatus ->
                        viewModel.onEvent(MedicineHistoryUiEvent.OnUpdateRecordStatus(record.id, newStatus))
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun StatusBadge(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun CalendarDayItem(
    day: AdherenceDay,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val statusColor = when (day.status) {
        AdherenceStatus.TAKEN -> Color(0xFF2E7D32)
        AdherenceStatus.MISSED -> Color(0xFFBA1A1A)
        AdherenceStatus.SNOOZED -> Color(0xFFE65100)
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                if (isSelected) PrimaryTeal.copy(alpha = 0.2f) else statusColor.copy(alpha = 0.12f),
                CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.dayOfMonthBn,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) PrimaryTeal else statusColor
        )
    }
}

@Composable
fun TitrationItemRow(titration: TitrationRecord) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = titration.medicineName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = titration.titrationDateBn,
                    fontSize = 11.sp,
                    color = PrimaryTeal,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${titration.previousDose}  ➔  ${titration.newDose}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF006A6A)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${titration.doctorName} : ${titration.reasonBn}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun AdherenceRecordRow(
    record: AdherenceRecord,
    onStatusSelected: (AdherenceStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.medicineName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${record.scheduledTimeBn} — ${record.doseSlotBn}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusBadge(
                    label = record.status.labelBn,
                    color = when (record.status) {
                        AdherenceStatus.TAKEN -> Color(0xFF2E7D32)
                        AdherenceStatus.MISSED -> Color(0xFFBA1A1A)
                        AdherenceStatus.SNOOZED -> Color(0xFFE65100)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Selector Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onStatusSelected(AdherenceStatus.TAKEN) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (record.status == AdherenceStatus.TAKEN) Color(0xFF2E7D32).copy(alpha = 0.15f) else Color.Transparent
                    ),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text("✓ গৃহীত", fontSize = 11.sp, color = Color(0xFF2E7D32))
                }

                OutlinedButton(
                    onClick = { onStatusSelected(AdherenceStatus.MISSED) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (record.status == AdherenceStatus.MISSED) Color(0xFFBA1A1A).copy(alpha = 0.15f) else Color.Transparent
                    ),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text("✗ মিস", fontSize = 11.sp, color = Color(0xFFBA1A1A))
                }

                OutlinedButton(
                    onClick = { onStatusSelected(AdherenceStatus.SNOOZED) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (record.status == AdherenceStatus.SNOOZED) Color(0xFFE65100).copy(alpha = 0.15f) else Color.Transparent
                    ),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text("স্থগিত", fontSize = 11.sp, color = Color(0xFFE65100))
                }
            }
        }
    }
}
