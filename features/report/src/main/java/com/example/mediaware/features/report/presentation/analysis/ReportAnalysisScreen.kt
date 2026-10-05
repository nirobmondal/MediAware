package com.example.mediaware.features.report.presentation.analysis

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mediaware.core.designsystem.component.VisualRangeSpectrumBar
import com.example.mediaware.core.designsystem.theme.*
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.features.report.domain.model.ExtractedLabItem
import com.example.mediaware.features.report.domain.model.LabStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportAnalysisScreen(
    encodedItems: String,
    viewModel: ReportAnalysisViewModel = hiltViewModel(),
    onNavigateToQuestions: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(encodedItems) {
        viewModel.onEvent(ReportAnalysisUiEvent.LoadAnalysis(encodedItems))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ReportAnalysisSideEffect.NavigateToQuestions -> onNavigateToQuestions(effect.encodedItems)
                is ReportAnalysisSideEffect.ShowToast -> Toast.makeText(context, effect.messageBn, Toast.LENGTH_SHORT).show()
                ReportAnalysisSideEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ল্যাব রিপোর্ট বিশ্লেষণ",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        uiState.analysis?.let {
                            Text(
                                text = "তারিখ: ${it.reportDateFormattedBn}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(ReportAnalysisUiEvent.OnToggleFullTts) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "সম্পূর্ণ অডিও শুনুন",
                            tint = if (uiState.isPlayingTts) Color(0xFFBA1A1A) else PrimaryTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = { viewModel.onEvent(ReportAnalysisUiEvent.OnProceedToQuestions) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Icon(Icons.Default.QuestionAnswer, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "📋 ডাক্তারের জন্য প্রশ্নাবলী দেখুন ➡️",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryTeal)
            }
        } else {
            val analysis = uiState.analysis
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Mandatory Deferral Disclaimer Banner (Guardrail #1 & #3)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = analysis?.mandatoryDisclaimerBn ?: "⚠️ এটি কোনো প্রেসক্রিপশন নয়। যেকোনো সিদ্ধান্তে ডাক্তারের পরামর্শ নিন।",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFE65100),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Summary Badge Pill Row
                    analysis?.let {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusCountChip(
                                label = "স্বাভাবিক: ${it.normalCount.toString().toBengaliDigits()}",
                                color = ClinicalNormal,
                                containerColor = ClinicalNormalContainer,
                                modifier = Modifier.weight(1f)
                            )
                            StatusCountChip(
                                label = "সতর্কতা: ${it.borderlineCount.toString().toBengaliDigits()}",
                                color = ClinicalCaution,
                                containerColor = ClinicalCautionContainer,
                                modifier = Modifier.weight(1f)
                            )
                            StatusCountChip(
                                label = "উচ্চ: ${it.criticalCount.toString().toBengaliDigits()}",
                                color = ClinicalCritical,
                                containerColor = ClinicalCriticalContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // AI Overall Report Analysis Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                        border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = PrimaryTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "এআই সামগ্রিক ল্যাব রিপোর্ট পর্যালোচনা",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryTeal
                                        )
                                    )
                                }
                                if (uiState.isAiAnalyzing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = PrimaryTeal
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = uiState.overallAiAnalysisBn ?: if (uiState.isAiAnalyzing) "জেমিনাই এআই রিপোর্ট পর্যালোচনা করছে..." else "রিপোর্টের তথ্য বিশ্লেষণ করা হচ্ছে...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }

                // Individual Lab Test Analysis Cards
                items(analysis?.items ?: emptyList(), key = { it.id }) { item ->
                    LabItemCard(
                        item = item,
                        isPlayingTts = uiState.currentlyPlayingItemKey == item.key,
                        onToggleTts = { viewModel.onEvent(ReportAnalysisUiEvent.OnToggleItemTts(item.key)) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun StatusCountChip(
    label: String,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LabItemCard(
    item: ExtractedLabItem,
    isPlayingTts: Boolean,
    onToggleTts: () -> Unit
) {
    val (statusBorderColor, statusContainerColor, statusTextColor) = when (item.status) {
        LabStatus.NORMAL -> Triple(ClinicalNormal, ClinicalNormalContainer.copy(alpha = 0.4f), ClinicalNormal)
        LabStatus.BORDERLINE -> Triple(ClinicalCaution, ClinicalCautionContainer.copy(alpha = 0.5f), ClinicalCaution)
        LabStatus.CRITICAL -> Triple(ClinicalCritical, ClinicalCriticalContainer.copy(alpha = 0.5f), ClinicalCritical)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = statusContainerColor),
        border = BorderStroke(1.5.dp, statusBorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Test Name & Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.testNameBn,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = item.testNameEn,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBorderColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.status.displayNameBn,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Embedded VisualRangeSpectrumBar (Requirement 1)
            VisualRangeSpectrumBar(
                value = item.numericValue,
                unit = item.unit,
                normalMin = item.normalMin,
                normalMax = item.normalMax,
                criticalThreshold = item.criticalThreshold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bengali Clinical Explanation Box (Observational & Non-diagnostic - Guardrail #1)
            Text(
                text = item.clinicalExplanationBn,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer: 180-Day Smart Cache Badge & Voice Audio Play Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Smart Cache Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE8EAF6))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = Color(0xFF3F51B5),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "স্মার্ট ক্যাশ (১৮০ দিন)",
                        fontSize = 11.sp,
                        color = Color(0xFF3F51B5),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Audio Play Button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    modifier = Modifier.clickable(onClick = onToggleTts)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPlayingTts) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = null,
                            tint = if (isPlayingTts) Color(0xFFBA1A1A) else PrimaryTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlayingTts) "চলছে..." else "সহজ ভাষায় শুনুন",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isPlayingTts) Color(0xFFBA1A1A) else PrimaryTeal
                        )
                    }
                }
            }
        }
    }
}
