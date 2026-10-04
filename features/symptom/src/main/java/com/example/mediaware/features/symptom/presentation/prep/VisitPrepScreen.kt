package com.example.mediaware.features.symptom.presentation.prep

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.mediaware.core.designsystem.theme.PrimaryTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitPrepScreen(
    symptomIds: List<String>,
    severity: Int,
    durationBn: String,
    viewModel: VisitPrepViewModel = hiltViewModel(),
    onNavigateToTestPrep: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(symptomIds, severity, durationBn) {
        viewModel.onEvent(VisitPrepUiEvent.GenerateCard(symptomIds, severity, durationBn))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                VisitPrepSideEffect.NavigateToTestPrep -> onNavigateToTestPrep()
                VisitPrepSideEffect.NavigateToHome -> onNavigateToHome()
                is VisitPrepSideEffect.ShowToast -> Toast.makeText(context, effect.messageBn, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ডাক্তার ভিজিট প্রস্তুতি",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(VisitPrepUiEvent.OnToggleTts) }) {
                        Icon(
                            imageVector = if (uiState.isPlayingTts) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "অডিও প্লেয়ার",
                            tint = PrimaryTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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
            val card = uiState.visitCard
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(10.dp))

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
                            text = card?.mandatoryDisclaimerBn ?: "⚠️ এটি কোনো প্রেসক্রিপশন নয়। যেকোনো সিদ্ধান্তে ডাক্তারের পরামর্শ নিন।",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Specialist Suggestion Badge
                card?.let { prep ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD4E3FF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2B5B84)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "সুপারিশকৃত বিশেষজ্ঞ বিভাগ:",
                                    style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF2B5B84))
                                )
                                Text(
                                    text = prep.suggestedSpecialistBn,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF001B3E)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Demographics Summary
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "রোগীর সাধারণ পরিচিতি",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = PrimaryTeal,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = prep.demographicsSummaryBn,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = prep.chiefComplaintsSummaryBn,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 30-Second Doctor Presentation Points
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4FBFB)),
                        border = BorderStroke(1.5.dp, PrimaryTeal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = PrimaryTeal)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "৩০ সেকেন্ডে ডাক্তারকে যা বলবেন",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryTeal
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            prep.doctorSpeakingPointsBn.forEach { point ->
                                Text(
                                    text = point,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        lineHeight = 22.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Targeted Doctor Questions (DGHS guidelines)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFFCFD9D9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = Color(0xFF2B5B84))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ডাক্তারকে আপনার যে প্রশ্নগুলো করা উচিত",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2B5B84)
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            prep.doctorQuestionsBn.forEach { question ->
                                Text(
                                    text = question,
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Next actions: Go to Screen 11 (Test Prep Guide) or finish
                    Button(
                        onClick = { viewModel.onEvent(VisitPrepUiEvent.OnNavigateToTestPrep) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                    ) {
                        Icon(Icons.Default.Science, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ল্যাব টেস্ট প্রস্তুতি নির্দেশিকা দেখুন ➡️",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { viewModel.onEvent(VisitPrepUiEvent.OnFinishAndGoHome) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("হোমে ফিরে যান")
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
