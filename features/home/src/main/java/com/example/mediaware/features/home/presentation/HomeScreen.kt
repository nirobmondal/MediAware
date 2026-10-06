package com.example.mediaware.features.home.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import timber.log.Timber
import java.io.File
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mediaware.core.database.entity.ConsultationEntity
import com.example.mediaware.core.database.entity.HealthRecordEntity
import com.example.mediaware.core.designsystem.component.MediAwareBottomNavBar
import com.example.mediaware.core.designsystem.component.MediAwareNavTab
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.core.designsystem.theme.EmeraldGreen
import com.example.mediaware.core.designsystem.theme.OceanBlue
import com.example.mediaware.core.designsystem.theme.WarmAmber
import com.example.mediaware.core.designsystem.theme.AiPurple
import com.example.mediaware.core.designsystem.theme.CardBgTeal
import com.example.mediaware.core.designsystem.theme.CardBgOcean
import com.example.mediaware.core.designsystem.theme.CardBgMint
import com.example.mediaware.core.designsystem.theme.CardBgAmber
import com.example.mediaware.core.designsystem.theme.CardBgPurple
import com.example.mediaware.core.designsystem.theme.BorderTealSoft
import com.example.mediaware.core.designsystem.theme.BorderOceanSoft
import com.example.mediaware.core.designsystem.theme.BorderMintSoft
import com.example.mediaware.core.designsystem.theme.BorderAmberSoft
import com.example.mediaware.core.designsystem.util.toBengaliDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToSymptomSelect: () -> Unit = {},
    onNavigateToReportCapture: () -> Unit = {},
    onNavigateToPrescription: () -> Unit = {},
    onNavigateToChamberHub: () -> Unit = {},
    onNavigateToConsultationSummary: () -> Unit = {},
    onNavigateToTimeline: () -> Unit = {},
    onNavigateToMedicineHistory: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {}
) {
    val uiState by viewModel.combinedState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MediAware",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = PrimaryTeal.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "আপনার স্বাস্থ্য সহায়ক",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryTeal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "সেটিংস", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            AiChatFloatingBubble(
                isOnline = uiState.isOnline,
                onClick = { viewModel.onEvent(HomeUiEvent.OnToggleChat) }
            )
        },
        bottomBar = {
            MediAwareBottomNavBar(
                selectedTab = MediAwareNavTab.HOME,
                onTabSelected = { tab ->
                    when (tab) {
                        MediAwareNavTab.HOME -> { /* Already on Home */ }
                        MediAwareNavTab.SYMPTOMS -> onNavigateToSymptomSelect()
                        MediAwareNavTab.SETTINGS -> onNavigateToSettings()
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))
                if (!uiState.isOnline) {
                    OfflineBanner()
                }
            }

            // Patient Profile Snapshot Card
            item {
                PatientProfileHeaderCard(uiState = uiState)
            }

            // Quick Service Actions Grid (Prescription, Lab Report, Medication, Timeline)
            item {
                QuickActionShortcuts(
                    onNavigateToPrescription = onNavigateToPrescription,
                    onNavigateToReportCapture = onNavigateToReportCapture,
                    onNavigateToMedicineHistory = onNavigateToMedicineHistory,
                    onNavigateToTimeline = onNavigateToTimeline
                )
            }

            // Doctor Consultation Audio Recording & Guideline Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToChamberHub),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBgTeal),
                    border = BorderStroke(1.2.dp, BorderTealSoft)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(PrimaryTeal, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "ডাক্তার পরামর্শ অডিও রেকর্ড",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "৫টি গাইডলাইন ও স্বয়ংক্রিয় এআই সারাংশ",
                                        fontSize = 12.sp,
                                        color = PrimaryTeal,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Surface(
                                color = PrimaryTeal,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "রেকর্ড করুন",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "চেম্বারে ডাক্তারের পরামর্শ রেকর্ড করুন ও সহজ এআই সারাংশ পান।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Symptoms Analysis & Doctor Visit Preparation Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToSymptomSelect),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBgMint),
                    border = BorderStroke(1.2.dp, BorderMintSoft)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(EmeraldGreen, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Healing,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "লক্ষণ বিশ্লেষণ ও ভিজিট প্রস্তুতি",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "লক্ষণভিত্তিক প্রশ্নাবলি ও পরামর্শ",
                                        fontSize = 12.sp,
                                        color = EmeraldGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Surface(
                                color = EmeraldGreen,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "শুরু করুন",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "শারীরিক সমস্যা জানিয়ে ডাক্তার দেখানোর প্রশ্নাবলি ও প্রস্তুতি নিন।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Next Dose / Upcoming Reminder (if available)
            uiState.upcomingReminder?.let { reminder ->
                item {
                    NextDoseCard(reminder = reminder)
                }
            }

            // Consultation Summaries & Health Memory Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সাম্প্রতিক পরামর্শ ও স্বাস্থ্য মেমোরি",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onNavigateToConsultationSummary) {
                        Text(
                            text = "সকল সারাংশ দেখুন",
                            fontSize = 12.sp,
                            color = PrimaryTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Recent Consultation Card or Clean Empty State
            if (uiState.recentConsultation != null) {
                item {
                    RecentConsultationItemCard(
                        consultation = uiState.recentConsultation!!,
                        onClick = onNavigateToConsultationSummary
                    )
                }
            } else if (uiState.recentHealthRecords.isNotEmpty()) {
                item {
                    RecentHealthRecordItemCard(
                        record = uiState.recentHealthRecords.first(),
                        onClick = onNavigateToConsultationSummary
                    )
                }
            } else {
                item {
                    EmptyConsultationsCard(onStartRecording = onNavigateToChamberHub)
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // MediAware AI Chat & OCR Scanner Sheet
    if (uiState.isChatOpen) {
        AiChatBottomSheet(
            uiState = uiState,
            onDismiss = { viewModel.onEvent(HomeUiEvent.OnToggleChat) },
            onSendMessage = { msg -> viewModel.onEvent(HomeUiEvent.OnSendChatMessage(msg)) },
            onScanUri = { uri -> viewModel.onEvent(HomeUiEvent.OnScanDocumentImage(uri.toString())) },
            onScanBitmap = { bmp -> viewModel.onEvent(HomeUiEvent.OnScanDocumentBitmap(bmp)) },
            onClearChat = { viewModel.onEvent(HomeUiEvent.OnClearChat) }
        )
    }
}

@Composable
private fun PatientProfileHeaderCard(uiState: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgTeal),
        border = BorderStroke(1.dp, BorderTealSoft)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(PrimaryTeal.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "স্বাগতম,",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.userName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                uiState.bloodGroup?.let { bg ->
                    Surface(
                        color = Color(0xFFC62828).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bloodtype,
                                contentDescription = null,
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = bg,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                }
            }

            if (uiState.userAge != null || uiState.chronicConditions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    uiState.userAge?.let { age ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "বয়স: ${age.toString().toBengaliDigits()} বছর",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (uiState.chronicConditions.isNotEmpty()) {
                        Surface(
                            color = PrimaryTeal.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonitorHeart,
                                    contentDescription = null,
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = uiState.chronicConditions.joinToString(", "),
                                    fontSize = 11.sp,
                                    color = PrimaryTeal,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionShortcuts(
    onNavigateToPrescription: () -> Unit,
    onNavigateToReportCapture: () -> Unit,
    onNavigateToMedicineHistory: () -> Unit,
    onNavigateToTimeline: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "জরুরি সেবা ও ফিচার",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickServiceCard(
                modifier = Modifier.weight(1f),
                title = "প্রেসক্রিপশন",
                subtitle = "সহজ পাঠ",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                iconTint = OceanBlue,
                bgColor = CardBgOcean,
                borderColor = BorderOceanSoft,
                onClick = onNavigateToPrescription
            )
            QuickServiceCard(
                modifier = Modifier.weight(1f),
                title = "টেস্ট রিপোর্ট",
                subtitle = "স্ক্যান ও মান",
                icon = Icons.Default.Science,
                iconTint = EmeraldGreen,
                bgColor = CardBgMint,
                borderColor = BorderMintSoft,
                onClick = onNavigateToReportCapture
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickServiceCard(
                modifier = Modifier.weight(1f),
                title = "ওষুধের রুটিন",
                subtitle = "রিমাইন্ডার",
                icon = Icons.Default.Medication,
                iconTint = WarmAmber,
                bgColor = CardBgAmber,
                borderColor = BorderAmberSoft,
                onClick = onNavigateToMedicineHistory
            )
            QuickServiceCard(
                modifier = Modifier.weight(1f),
                title = "টাইমলাইন",
                subtitle = "ইতিহাস",
                icon = Icons.Default.HistoryEdu,
                iconTint = AiPurple,
                bgColor = CardBgPurple,
                borderColor = Color(0xFFD1C4E9),
                onClick = onNavigateToTimeline
            )
        }
    }
}

@Composable
private fun QuickServiceCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    bgColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconTint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecentConsultationItemCard(
    consultation: ConsultationEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MedicalInformation,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = consultation.doctorName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Surface(
                    color = PrimaryTeal.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = consultation.dateFormattedBn,
                        fontSize = 10.sp,
                        color = PrimaryTeal,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = consultation.summaryBn,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun RecentHealthRecordItemCard(
    record: HealthRecordEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFCFD8DC))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BookmarkAdded,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = record.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = record.dateFormattedBn,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = record.summaryBn,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun EmptyConsultationsCard(onStartRecording: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.HistoryEdu,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "কোনো সংরক্ষিত পরামর্শ বা স্বাস্থ্য মেমোরি নেই",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ডাক্তার ভিজিটের সময় অডিও রেকর্ড করুন অথবা লক্ষণ বিশ্লেষণ শুরু করুন।",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AiChatFloatingBubble(
    isOnline: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    FloatingActionButton(
        onClick = onClick,
        containerColor = PrimaryTeal,
        contentColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.height(48.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) Color(0xFF00E676).copy(alpha = alphaAnim) else Color(0xFFBA1A1A))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "MediAware AI",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "MediAware AI",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class ChatSmartSuggestion(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badge: String? = null,
    val isCameraAction: Boolean = false,
    val promptMessage: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatBottomSheet(
    uiState: HomeUiState,
    onDismiss: () -> Unit,
    onSendMessage: (String) -> Unit,
    onScanUri: (Uri) -> Unit,
    onScanBitmap: (Bitmap) -> Unit = {},
    onClearChat: () -> Unit
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Camera launcher for full-resolution document scan inside chat
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            tempCameraUri?.let { uri ->
                onScanUri(uri)
            }
        }
    }

    // Fallback preview launcher if file provider is unavailable
    val takePreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            onScanBitmap(bitmap)
        }
    }

    val launchCamera = {
        try {
            val file = File.createTempFile("chat_scan_", ".jpg", context.cacheDir)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempCameraUri = uri
            takePictureLauncher.launch(uri)
        } catch (e: Exception) {
            Timber.e(e, "FileProvider camera launch failed, attempting preview fallback")
            try {
                takePreviewLauncher.launch(null)
            } catch (ex: Exception) {
                Timber.e(ex, "Preview launcher also failed")
                Toast.makeText(context, "ক্যামেরা চালু করতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Camera runtime permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(context, "ডকুমেন্ট বা ওষুধ স্ক্যান করতে ক্যামেরার অনুমতি প্রয়োজন", Toast.LENGTH_SHORT).show()
        }
    }

    val onCameraClick = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchCamera()
        } else {
            try {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            } catch (e: Exception) {
                Timber.e(e, "Camera permission request failed")
                Toast.makeText(context, "ক্যামেরার অনুমতি চাইতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Gallery launcher for document/prescription image selection
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onScanUri(uri)
        }
    }

    LaunchedEffect(uiState.chatMessages.size, uiState.isAiThinking) {
        if (uiState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatMessages.size - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxHeight(0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PrimaryTeal.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "MediAware AI",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (uiState.isOnline) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (uiState.isOnline) Color(0xFF2E7D32) else Color(0xFFBA1A1A),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.isOnline) "জেমিনি এআই অনলাইন ও সক্রিয়" else "অফলাইন মোড",
                                fontSize = 11.sp,
                                color = if (uiState.isOnline) Color(0xFF2E7D32) else Color(0xFFBA1A1A),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onClearChat) {
                        Icon(Icons.Default.Refresh, contentDescription = "রিসেট", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Smart Healthcare Suggestions Row with Icons & Action Triggers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "স্মার্ট স্বাস্থ্য সহায়িকা:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "ট্যাপ করে সরাসরি স্ক্যান বা প্রশ্ন করুন",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val smartSuggestions = listOf(
                ChatSmartSuggestion(
                    title = "ছবি দিয়ে ল্যাব রিপোর্ট বুঝুন",
                    icon = Icons.Default.Science,
                    badge = "ক্যামেরা স্ক্যান",
                    isCameraAction = true
                ),
                ChatSmartSuggestion(
                    title = "ওষুধের ব্যবহার ও তথ্য বুঝুন",
                    icon = Icons.Default.Medication,
                    promptMessage = "আমার ওষুধের খাওয়ার সঠিক নিয়ম, ডোজ এবং সাধারণ সতর্কতা সম্পর্কে বিস্তারিত বুঝিয়ে বলুন।"
                ),
                ChatSmartSuggestion(
                    title = "ছবি দিয়ে প্রেসক্রিপশন বুঝুন",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    badge = "ক্যামেরা স্ক্যান",
                    isCameraAction = true
                ),
                ChatSmartSuggestion(
                    title = "AI থেকে স্বাস্থ্য পরামর্শ নিন",
                    icon = Icons.Default.AutoAwesome,
                    promptMessage = "আমার বর্তমান স্বাস্থ্য পরিস্থিতি অনুযায়ী সুস্থ থাকতে প্রয়োজনীয় জীবনযাত্রা ও সাধারণ স্বাস্থ্য পরামর্শ দিন।"
                )
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(smartSuggestions) { item ->
                    Card(
                        onClick = {
                            if (item.isCameraAction) {
                                onCameraClick()
                            } else if (item.promptMessage.isNotBlank()) {
                                onSendMessage(item.promptMessage)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.25f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(PrimaryTeal.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            Text(
                                text = item.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (item.badge != null) {
                                Surface(
                                    color = PrimaryTeal.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = item.badge,
                                        fontSize = 9.sp,
                                        color = PrimaryTeal,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.chatMessages, key = { it.id }) { msg ->
                    ChatBubbleItem(message = msg)
                }

                if (uiState.isAiThinking) {
                    item {
                        ThinkingIndicator()
                    }
                }
            }

            // Input Row with Text Field, Camera & Gallery Document Scan Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCameraClick,
                    modifier = Modifier
                        .size(42.dp)
                        .background(PrimaryTeal.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "ক্যামেরা দিয়ে স্ক্যান করুন",
                        tint = PrimaryTeal,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        try {
                            galleryLauncher.launch("image/*")
                        } catch (e: Exception) {
                            Timber.e(e, "Gallery launch failed")
                            Toast.makeText(context, "গ্যালারি খুলতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .background(PrimaryTeal.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "গ্যালারি থেকে ছবি আপলোড করুন",
                        tint = PrimaryTeal,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("প্রশ্ন লিখুন বা ছবি তুলুন...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !uiState.isAiThinking,
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (inputText.isNotBlank() && !uiState.isAiThinking) PrimaryTeal else Color(0xFFB0BEC5),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "পাঠান",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (message.isFromUser) PrimaryTeal else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (message.savedToMemory) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(PrimaryTeal.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdded,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "স্বাস্থ্য মেমোরিতে সংরক্ষিত (WHO ও DGHS বেস)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    color = if (message.isFromUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
private fun ThinkingIndicator() {
    Row(
        modifier = Modifier.padding(start = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            strokeWidth = 2.dp,
            color = PrimaryTeal
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "জেমিনি এআই বিশ্লেষণ করছে...",
            fontSize = 11.sp,
            color = PrimaryTeal,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun NextDoseCard(reminder: UpcomingReminderUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryTeal.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(PrimaryTeal, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Medication,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "পরবর্তী ওষুধের সময়",
                    fontSize = 11.sp,
                    color = PrimaryTeal,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = reminder.titleBn,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${reminder.timeFormattedBn} • ${reminder.instructionBn}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OfflineBanner() {
    Surface(
        color = Color(0xFFBA1A1A).copy(alpha = 0.08f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFBA1A1A).copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFBA1A1A),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ইন্টারনেট সংযোগ নেই — অফলাইন মোড ও লোকাল মেমোরি সক্রিয়",
                fontSize = 11.sp,
                color = Color(0xFFBA1A1A),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
