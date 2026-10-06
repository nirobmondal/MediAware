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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.VolumeMute
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
import com.example.mediaware.core.designsystem.theme.EmeraldGreen
import com.example.mediaware.core.designsystem.theme.OceanBlue
import com.example.mediaware.core.designsystem.theme.AiPurple
import com.example.mediaware.core.designsystem.theme.CardBgTeal
import com.example.mediaware.core.designsystem.theme.CardBgOcean
import com.example.mediaware.core.designsystem.theme.CardBgMint
import com.example.mediaware.core.designsystem.theme.CardBgPurple
import com.example.mediaware.core.designsystem.theme.BorderTealSoft
import com.example.mediaware.core.designsystem.theme.BorderOceanSoft
import com.example.mediaware.core.designsystem.theme.BorderMintSoft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitPrepScreen(
    symptomIds: List<String>,
    severity: Int,
    durationBn: String,
    viewModel: VisitPrepViewModel = hiltViewModel(),
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
                            imageVector = if (uiState.isPlayingTts) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                            contentDescription = "অডিও প্লেয়ার",
                            tint = PrimaryTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            if (!uiState.isLoading && uiState.visitCard != null) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp)
                    ) {
                        Button(
                            onClick = { viewModel.onEvent(VisitPrepUiEvent.OnFinishAndGoHome) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Text(
                                text = "প্রস্তুতি সম্পন্ন (হোমে যান)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    CircularProgressIndicator(color = PrimaryTeal, modifier = Modifier.size(44.dp), strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "জেমিনাই এআই প্রস্তুতি গাইড তৈরি করছে...",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "আপনার নির্দিষ্ট লক্ষণের ওপর ভিত্তি করে ডাক্তারকে বলার পয়েন্ট ও প্রয়োজনীয় প্রশ্নাবলি প্রস্তুত করা হচ্ছে।",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        } else if (uiState.isNetworkError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFFFFF3E0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "ইন্টারনেট সংযোগ প্রয়োজন",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "আপনার লক্ষণের ওপর ভিত্তি করে সম্পূর্ণ বাস্তবসম্মত ও সুনির্দিষ্ট এআই পরামর্শ তৈরি করতে সক্রিয় ইন্টারনেট সংযোগ চালু করে পুনরায় চেষ্টা করুন।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.onEvent(VisitPrepUiEvent.OnRetry) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("পুনরায় চেষ্টা করুন", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.onEvent(VisitPrepUiEvent.OnFinishAndGoHome) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("হোমে ফিরে যান", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
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
                            text = card?.mandatoryDisclaimerBn ?: "এটি কোনো প্রেসক্রিপশন নয়। যেকোনো সিদ্ধান্তে ডাক্তারের পরামর্শ নিন।",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Specialist Suggestion Card
                card?.let { prep ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBgOcean),
                        border = BorderStroke(1.dp, BorderOceanSoft),
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
                                    .size(40.dp)
                                    .background(OceanBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "সুপারিশকৃত বিশেষজ্ঞ বিভাগ",
                                    fontSize = 11.sp,
                                    color = OceanBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = prep.suggestedSpecialistBn,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // AI Symptoms & Disease Analysis Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBgTeal),
                        border = BorderStroke(1.dp, BorderTealSoft),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(PrimaryTeal, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "এআই লক্ষণ ও স্বাস্থ্য মূল্যায়ন",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
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

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = uiState.aiSymptomAnalysisBn ?: if (uiState.isAiAnalyzing) "জেমিনাই এআই আপনার লক্ষণগুলো বিশ্লেষণ করছে..." else "লক্ষণ পর্যালোচনা করা হচ্ছে...",
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 30-Second Doctor Presentation Points
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBgMint),
                        border = BorderStroke(1.dp, BorderMintSoft),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(EmeraldGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "৩০ সেকেন্ডে ডাক্তারকে যা বলবেন",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            prep.doctorSpeakingPointsBn.forEach { point ->
                                Row(
                                    modifier = Modifier.padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "•",
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = point,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Saved to Health Memory Status Pill
                    if (uiState.isSavedToHealthMemory) {
                        Surface(
                            color = PrimaryTeal.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BorderTealSoft),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkAdded,
                                    contentDescription = null,
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "এই প্রস্তুতি পরিকল্পনা আপনার স্বাস্থ্য মেমোরিতে সংরক্ষিত হয়েছে।",
                                    fontSize = 11.sp,
                                    color = PrimaryTeal,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // What to Show Doctor Card (ডাক্তারকে যা দেখাতে হবে)
                    val showItems = uiState.whatToShowDoctor
                    if (showItems.isNotEmpty()) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBgOcean),
                            border = BorderStroke(1.dp, BorderOceanSoft),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(OceanBlue, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FolderShared,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "ডাক্তারকে যা যা দেখাতে হবে",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                showItems.forEach { item ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "•",
                                            fontWeight = FontWeight.Bold,
                                            color = OceanBlue,
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text(
                                            text = item,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Targeted AI Doctor Cheat Questions (চিট-প্রশ্নাবলী)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBgPurple),
                        border = BorderStroke(1.dp, BorderTealSoft),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(AiPurple, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QuestionAnswer,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "ডাক্তারকে জিজ্ঞেস করার এআই প্রশ্নাবলি",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            val questionsToDisplay = uiState.aiCheatQuestions.ifEmpty { prep.doctorQuestionsBn }
                            questionsToDisplay.forEach { question ->
                                Row(
                                    modifier = Modifier.padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "•",
                                        fontWeight = FontWeight.Bold,
                                        color = AiPurple,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        text = question,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
