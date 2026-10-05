package com.example.mediaware.features.symptom.presentation.followup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.core.designsystem.util.toBengaliDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomFollowupScreen(
    symptomIds: List<String>,
    viewModel: SymptomFollowupViewModel = hiltViewModel(),
    onNavigateToEmergency: (String, List<String>) -> Unit,
    onNavigateToVisitPrep: (List<String>, Int, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(symptomIds) {
        viewModel.onEvent(SymptomFollowupUiEvent.LoadSymptoms(symptomIds))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SymptomFollowupSideEffect.NavigateToEmergency -> {
                    onNavigateToEmergency(effect.reasonBn, effect.matchedSymptoms)
                }
                is SymptomFollowupSideEffect.NavigateToVisitPrep -> {
                    onNavigateToVisitPrep(effect.symptomIds, effect.severity, effect.durationBn)
                }
                SymptomFollowupSideEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "তীব্রতা ও ব্যাপ্তি",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = { viewModel.onEvent(SymptomFollowupUiEvent.OnEvaluateAndProceed) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text(
                        text = "যাচাই করুন ও এগিয়ে যান",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Selected symptoms summary card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F7F7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "বাছাইকৃত লক্ষণসমূহ:",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.selectedSymptoms.joinToString(" • ") { it.nameBn },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 1. Duration Selection
            Text(
                text = "১. এই লক্ষণগুলো কতদিন ধরে অনুভব করছেন?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.availableDurationsBn.forEach { duration ->
                    val isSelected = uiState.selectedDurationBn == duration
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onEvent(SymptomFollowupUiEvent.OnDurationSelected(duration)) },
                        label = { Text(duration) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD5F5F5),
                            selectedLabelColor = PrimaryTeal
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) PrimaryTeal else Color(0xFFCFD9D9)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 2. Severity Slider (1-10 in Bengali)
            Text(
                text = "২. কষ্টের তীব্রতা কতখানি? (১ থেকে ১০ এর স্কেলে)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(6.dp))

            val severityBn = uiState.severityRating.toString().toBengaliDigits()
            val severityLabel = when {
                uiState.severityRating <= 3 -> "মৃদু অস্বস্তি (সহনীয়)"
                uiState.severityRating <= 7 -> "মাঝারি তীব্রতা"
                else -> "অসহ্য তীব্র ব্যথা বা কষ্ট"
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মাত্রা: $severityBn / ১০",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.severityRating >= 8) Color(0xFFBA1A1A) else PrimaryTeal
                            )
                        )
                        Text(
                            text = severityLabel,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = if (uiState.severityRating >= 8) Color(0xFFBA1A1A) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Slider(
                        value = uiState.severityRating.toFloat(),
                        onValueChange = { viewModel.onEvent(SymptomFollowupUiEvent.OnSeverityChanged(it.toInt())) },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = if (uiState.severityRating >= 8) Color(0xFFBA1A1A) else PrimaryTeal,
                            activeTrackColor = if (uiState.severityRating >= 8) Color(0xFFBA1A1A) else PrimaryTeal,
                            inactiveTrackColor = Color(0xFFCFD9D9)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("১ (মৃদু)", style = MaterialTheme.typography.labelSmall)
                        Text("৫ (মাঝারি)", style = MaterialTheme.typography.labelSmall)
                        Text("১০ (চরম)", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Clinical Safety Notice (Guardrail #4 preview)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFB26A00),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "পরবর্তী ধাপে লক্ষণগুলোর তাৎক্ষণিক জরুরি রেড-ফ্ল্যাগ যাচাই করা হবে (০ms অফলাইন সুরক্ষা)।",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B4500))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
