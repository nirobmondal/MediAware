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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.mediaware.core.designsystem.component.UserAvatarView
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.CalendarToday
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToSymptomSelect: () -> Unit = {},
    onNavigateToReportCapture: () -> Unit = {},
    onNavigateToPrescription: () -> Unit = {},
    onNavigateToChamberHub: () -> Unit = {},
    onNavigateToChamberRecorder: () -> Unit = {},
    onNavigateToConsultationSummary: () -> Unit = {},
    onNavigateToTimeline: () -> Unit = {},
    onNavigateToMedicineHistory: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {}
) {
    val uiState by viewModel.combinedState.collectAsState()
    var showRecordGuideDialog by remember { mutableStateOf(false) }
    var selectedPrepGuideForSheet by remember { mutableStateOf<HealthRecordEntity?>(null) }

    Scaffold(
        topBar = {
            if (uiState.isChatOpen) {
                GeminiChatTopBar(
                    isOnline = uiState.isOnline,
                    onBack = { viewModel.onEvent(HomeUiEvent.OnToggleChat) },
                    onClearChat = { viewModel.onEvent(HomeUiEvent.OnClearChat) }
                )
            } else {
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
            }
        },
        floatingActionButton = {
            if (!uiState.isChatOpen) {
                AiChatFloatingBubble(
                    isOnline = uiState.isOnline,
                    onClick = { viewModel.onEvent(HomeUiEvent.OnToggleChat) }
                )
            }
        },
        bottomBar = {
            MediAwareBottomNavBar(
                selectedTab = MediAwareNavTab.HOME,
                onTabSelected = { tab ->
                    when (tab) {
                        MediAwareNavTab.HOME -> {
                            if (uiState.isChatOpen) {
                                viewModel.onEvent(HomeUiEvent.OnToggleChat)
                            }
                        }
                        MediAwareNavTab.SYMPTOMS -> {
                            if (uiState.isChatOpen) {
                                viewModel.onEvent(HomeUiEvent.OnToggleChat)
                            }
                            onNavigateToSymptomSelect()
                        }
                        MediAwareNavTab.SETTINGS -> {
                            if (uiState.isChatOpen) {
                                viewModel.onEvent(HomeUiEvent.OnToggleChat)
                            }
                            onNavigateToSettings()
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isChatOpen) {
            GeminiChatScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                uiState = uiState,
                onSendMessage = { msg -> viewModel.onEvent(HomeUiEvent.OnSendChatMessage(msg)) },
                onScanUri = { uri -> viewModel.onEvent(HomeUiEvent.OnScanDocumentImage(uri.toString())) },
                onScanBitmap = { bmp -> viewModel.onEvent(HomeUiEvent.OnScanDocumentBitmap(bmp)) }
            )
        } else {
            HomeDashboardContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                uiState = uiState,
                onRequestRecordAudio = { showRecordGuideDialog = true },
                onNavigateToSymptomSelect = onNavigateToSymptomSelect,
                onNavigateToConsultationSummary = onNavigateToConsultationSummary,
                onSelectPrepGuide = { guide -> selectedPrepGuideForSheet = guide }
            )
        }

        if (showRecordGuideDialog) {
            AlertDialog(
                onDismissRequest = { showRecordGuideDialog = false },
                icon = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(PrimaryTeal.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "ডাক্তার পরামর্শ অডিও রেকর্ড নির্দেশিকা",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "১. ডাক্তারের সাথে আলোচনার সময় ফোনটি সামনে রাখুন যাতে কথা স্পষ্ট রেকর্ড হয়।",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Text(
                            text = "২. কথোপকথন শেষে স্বয়ংক্রিয়ভাবে সহজ ভাষায় এআই পরামর্শ সারাংশ তৈরি হবে।",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                        Text(
                            text = "৩. ডাক্তার নির্দেশিত ফলো-আপ ভিজিট ও গুরুত্বপূর্ণ দিকনির্দেশনা হোম পেজে সংরক্ষিত থাকবে।",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showRecordGuideDialog = false
                            onNavigateToChamberRecorder()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("রেকর্ড শুরু করুন", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRecordGuideDialog = false }) {
                        Text("বাতিল", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        selectedPrepGuideForSheet?.let { guide ->
            ModalBottomSheet(
                onDismissRequest = { selectedPrepGuideForSheet = null },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                ConsultationPrepDetailSheetContent(
                    record = guide,
                    onDismiss = { selectedPrepGuideForSheet = null }
                )
            }
        }
    }
}

@Composable
private fun HomeDashboardContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onRequestRecordAudio: () -> Unit,
    onNavigateToSymptomSelect: () -> Unit,
    onNavigateToConsultationSummary: () -> Unit,
    onSelectPrepGuide: (HealthRecordEntity) -> Unit
) {
    LazyColumn(
        modifier = modifier,
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

        // Doctor Consultation Audio Recording Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onRequestRecordAudio),
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
                                    text = "চেম্বারের কথোপকথন রেকর্ড ও এআই সারাংশ",
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
                        text = "চেম্বারে ডাক্তারের পরামর্শ সরাসরি রেকর্ড করুন। স্বয়ংক্রিয় এআই সারাংশ ও ফলো-আপ পরিকল্পনা তৈরি হবে।",
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
                                    text = "লক্ষণভিত্তিক প্রশ্নাবলি ও প্রস্তুতি গাইড",
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
                        text = "শারীরিক লক্ষণ জানিয়ে ডাক্তার দেখানোর ৩০ সেকেন্ড স্পিকিং পয়েন্ট ও প্রশ্নাবলি প্রস্তুত করুন।",
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

        // Doctor Suggestions Section (ডাক্তার পরামর্শ ও নির্দেশনা)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ডাক্তার পরামর্শ ও নির্দেশনা",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (uiState.recentConsultations.isNotEmpty()) {
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
        }

        if (uiState.recentConsultations.isNotEmpty()) {
            val displayConsultations = uiState.recentConsultations.take(3)
            items(displayConsultations, key = { it.id }) { consultation ->
                DoctorSuggestionItemCard(
                    consultation = consultation,
                    onClick = onNavigateToConsultationSummary
                )
            }
        } else {
            item {
                EmptyDoctorSuggestionsCard(onStartRecording = onRequestRecordAudio)
            }
        }

        // Consultation Preparation Guide Section (কনসালটেশন প্রস্তুতি গাইড)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "কনসালটেশন প্রস্তুতি গাইড",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (uiState.recentPreparationGuides.isNotEmpty()) {
                    Surface(
                        color = EmeraldGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${uiState.recentPreparationGuides.size.toString().toBengaliDigits()}টি গাইড",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        if (uiState.recentPreparationGuides.isNotEmpty()) {
            val displayGuides = uiState.recentPreparationGuides.take(3)
            items(displayGuides, key = { it.id }) { guide ->
                ConsultationPrepGuideItemCard(
                    guide = guide,
                    onClick = { onSelectPrepGuide(guide) }
                )
            }
        } else {
            item {
                EmptyPrepGuidesCard(onStartSymptomAnalysis = onNavigateToSymptomSelect)
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
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
                    UserAvatarView(
                        avatarId = uiState.selectedAvatarId,
                        photoUriString = uiState.customPhotoUri,
                        size = 40.dp
                    )
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
private fun DoctorSuggestionItemCard(
    consultation: ConsultationEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgOcean),
        border = BorderStroke(1.dp, BorderOceanSoft)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .size(32.dp)
                            .background(OceanBlue.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalInformation,
                            contentDescription = null,
                            tint = OceanBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = consultation.doctorName.ifBlank { "ডাক্তার পরামর্শ" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (consultation.followUpReasonBn.isNotBlank()) {
                            Text(
                                text = consultation.followUpReasonBn,
                                fontSize = 11.sp,
                                color = OceanBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Surface(
                    color = OceanBlue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = consultation.dateFormattedBn,
                        fontSize = 10.sp,
                        color = OceanBlue,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = consultation.summaryBn,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                lineHeight = 16.sp
            )

            if (consultation.followUpDays > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = OceanBlue,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${consultation.followUpDays.toString().toBengaliDigits()} দিন পর পরবর্তী ফলো-আপ ভিজিট",
                        fontSize = 11.sp,
                        color = OceanBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDoctorSuggestionsCard(onStartRecording: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgTeal),
        border = BorderStroke(1.dp, BorderTealSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(PrimaryTeal.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MedicalInformation,
                    contentDescription = null,
                    tint = PrimaryTeal,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "কোনো সংরক্ষিত পরামর্শ নির্দেশনা নেই",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ডাক্তার ভিজিটের সময় অডিও রেকর্ড করুন। গুরুত্বপূর্ণ পরামর্শ ও ফলো-আপ এখানে সংরক্ষিত থাকবে।",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onStartRecording,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "রেকর্ডিং শুরু করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ConsultationPrepGuideItemCard(
    guide: HealthRecordEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgMint),
        border = BorderStroke(1.dp, BorderMintSoft)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .size(32.dp)
                            .background(EmeraldGreen.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdded,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = guide.title.ifBlank { "ভিজিট প্রস্তুতি গাইড" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = EmeraldGreen.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = guide.dateFormattedBn,
                        fontSize = 10.sp,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = guide.summaryBn,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = EmeraldGreen.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "৩০ সেকেন্ড পয়েন্ট",
                            fontSize = 10.sp,
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        color = PrimaryTeal.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "প্রশ্নাবলি",
                            fontSize = 10.sp,
                            color = PrimaryTeal,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "সম্পূর্ণ গাইড দেখুন",
                        fontSize = 11.sp,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPrepGuidesCard(onStartSymptomAnalysis: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgMint),
        border = BorderStroke(1.dp, BorderMintSoft)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(EmeraldGreen.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Healing,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "কোনো সংরক্ষিত ভিজিট গাইড নেই",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ডাক্তার দেখানোর পূর্বে লক্ষণ বিশ্লেষণ করে প্রস্তুতি গাইড তৈরি করুন।",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onStartSymptomAnalysis,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "লক্ষণ বিশ্লেষণ শুরু করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ConsultationPrepDetailSheetContent(
    record: HealthRecordEntity,
    onDismiss: () -> Unit
) {
    val parsedData = remember(record) {
        var symList = listOf<String>()
        var spkList = listOf<String>()
        var showList = listOf<String>()
        var qList = listOf<String>()
        var careAdvice = ""
        var spec = ""
        try {
            val jsonStr = record.detailsJson
            if (!jsonStr.isNullOrBlank()) {
                val json = JSONObject(jsonStr)
                json.optJSONArray("symptoms")?.let { arr ->
                    symList = (0 until arr.length()).map { arr.getString(it) }
                }
                json.optJSONArray("speakingPoints")?.let { arr ->
                    spkList = (0 until arr.length()).map { arr.getString(it) }
                }
                json.optJSONArray("whatToShowDoctor")?.let { arr ->
                    showList = (0 until arr.length()).map { arr.getString(it) }
                }
                json.optJSONArray("cheatQuestions")?.let { arr ->
                    qList = (0 until arr.length()).map { arr.getString(it) }
                }
                careAdvice = json.optString("homeCareAdvice", "")
                spec = json.optString("suggestedSpecialist", "")
            }
        } catch (_: Exception) { }
        PrepGuideParsedData(
            symptoms = symList,
            speakingPoints = spkList,
            whatToShowDoctor = showList,
            cheatQuestions = qList,
            homeCareAdvice = careAdvice,
            suggestedSpecialist = spec
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = record.title.ifBlank { "ডাক্তার ভিজিট প্রস্তুতি গাইড" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = record.dateFormattedBn,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ করুন")
                }
            }
        }

        if (parsedData.suggestedSpecialist.isNotBlank() || parsedData.symptoms.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (parsedData.suggestedSpecialist.isNotBlank()) {
                        Surface(
                            color = PrimaryTeal.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "প্রস্তাবিত বিশেষজ্ঞ: ${parsedData.suggestedSpecialist}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                    if (parsedData.symptoms.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "লক্ষণসমূহ:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            parsedData.symptoms.forEach { sym ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = sym,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Summary / Condition Assessment
        if (record.summaryBn.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBgMint),
                    border = BorderStroke(1.dp, BorderMintSoft)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "লক্ষণ পর্যালোচনা",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = record.summaryBn,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Section 1: Speaking Points (ডাক্তারের কাছে যেভাবে তুলে ধরবেন)
        if (parsedData.speakingPoints.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBgOcean),
                    border = BorderStroke(1.dp, BorderOceanSoft)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ডাক্তারের কাছে যেভাবে তুলে ধরবেন (৩০ সেকেন্ড পয়েন্ট)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        parsedData.speakingPoints.forEachIndexed { index, point ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${(index + 1).toString().toBengaliDigits()}. ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OceanBlue
                                )
                                Text(
                                    text = point,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: What to show doctor (যা যা ডাক্তারকে দেখাতে হবে)
        if (parsedData.whatToShowDoctor.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBgAmber),
                    border = BorderStroke(1.dp, BorderAmberSoft)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ডাক্তারকে যা যা দেখাতে হবে",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarmAmber
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        parsedData.whatToShowDoctor.forEach { item ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "• ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarmAmber
                                )
                                Text(
                                    text = item,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Cheat Questions (ডাক্তারকে যেসব প্রশ্ন করবেন)
        if (parsedData.cheatQuestions.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBgPurple),
                    border = BorderStroke(1.dp, BorderTealSoft)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ডাক্তারকে যেসব প্রশ্ন করবেন",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AiPurple
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        parsedData.cheatQuestions.forEachIndexed { index, q ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "${(index + 1).toString().toBengaliDigits()}. ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AiPurple
                                )
                                Text(
                                    text = q,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Home Care Advice (if present)
        if (parsedData.homeCareAdvice.isNotBlank()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "প্রাথমিক সতর্কতা ও পরামর্শ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = parsedData.homeCareAdvice,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Mandatory Clinical Safety Disclaimer
        item {
            Surface(
                color = Color(0xFFC62828).copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "এটি কোনো প্রেসক্রিপশন নয়। যেকোনো চিকিৎসাগত সিদ্ধান্তে রেজিস্টার্ড ডাক্তারের পরামর্শ নিন।",
                    fontSize = 11.sp,
                    color = Color(0xFFC62828),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private data class PrepGuideParsedData(
    val symptoms: List<String>,
    val speakingPoints: List<String>,
    val whatToShowDoctor: List<String>,
    val cheatQuestions: List<String>,
    val homeCareAdvice: String,
    val suggestedSpecialist: String
)

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
            Image(
                painter = painterResource(id = com.example.mediaware.core.designsystem.R.drawable.ic_mediaware_logo),
                contentDescription = "MediAware AI",
                modifier = Modifier.size(20.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiChatTopBar(
    isOnline: Boolean,
    onBack: () -> Unit,
    onClearChat: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "ড্যাশবোর্ডে ফিরে যান",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, PrimaryTeal.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = com.example.mediaware.core.designsystem.R.drawable.ic_mediaware_logo),
                        contentDescription = "MediAware AI",
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "MediAware AI স্বাস্থ্য সহকারী",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) Color(0xFF00C853) else Color(0xFFBA1A1A))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isOnline) "অনলাইন স্বাস্থ্য সহকারী" else "অফলাইন মোড",
                            fontSize = 11.sp,
                            color = if (isOnline) Color(0xFF00C853) else Color(0xFFBA1A1A),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(onClick = onClearChat) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "নতুন চ্যাট শুরু করুন",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun GeminiChatScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onSendMessage: (String) -> Unit,
    onScanUri: (Uri) -> Unit,
    onScanBitmap: (Bitmap) -> Unit = {}
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main chat scrollable area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val hasUserMessages = uiState.chatMessages.any { it.isFromUser }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // If user hasn't asked anything yet, show Gemini greeting hero & suggestion prompt cards
                if (!hasUserMessages) {
                    item {
                        GeminiGreetingHero(
                            userName = uiState.userName,
                            onPromptClick = { prompt ->
                                onSendMessage(prompt)
                            },
                            onScanClick = onCameraClick
                        )
                    }
                }

                // Chat message bubbles
                items(uiState.chatMessages) { message ->
                    GeminiChatBubble(message = message)
                }

                // AI thinking state
                if (uiState.isAiThinking) {
                    item {
                        GeminiThinkingIndicator()
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Bottom Input section with safety disclaimer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚠️ এটি কোনো প্রেসক্রিপশন নয়। জরুরি প্রয়োজনে ডাক্তারের পরামর্শ নিন।",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Gemini floating pill input
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCameraClick,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "ক্যামেরা দিয়ে স্ক্যান করুন",
                            tint = PrimaryTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            try {
                                galleryLauncher.launch("image/*")
                            } catch (e: Exception) {
                                Timber.e(e, "Gallery launch failed")
                                Toast.makeText(context, "গ্যালারি খুলতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "ছবি আপলোড করুন",
                            tint = PrimaryTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        decorationBox = { innerTextField ->
                            if (inputText.isEmpty()) {
                                Text(
                                    text = "স্বাস্থ্য বিষয়ক প্রশ্ন লিখুন...",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        },
                        maxLines = 4
                    )

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText.trim())
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !uiState.isAiThinking,
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (inputText.isNotBlank() && !uiState.isAiThinking) PrimaryTeal else Color(0xFFCFD8DC),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "পাঠান",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GeminiGreetingHero(
    userName: String,
    onPromptClick: (String) -> Unit,
    onScanClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.5.dp, PrimaryTeal.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = com.example.mediaware.core.designsystem.R.drawable.ic_mediaware_logo),
                contentDescription = "MediAware AI লোগো",
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (userName.isNotBlank()) "স্বাগতম, $userName" else "স্বাগতম!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "আমি আপনার স্বাস্থ্য সহকারী। কীভাবে সাহায্য করতে পারি?",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Suggestion Cards: Strictly 4 health guidance cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GeminiPromptCard(
                    modifier = Modifier.weight(1f),
                    title = "ওষুধ নিয়ে জানুন (টেক্সট বা ছবি দিয়ে)",
                    desc = "ব্যবহার ও ডোজের সঠিক নিয়ম",
                    icon = Icons.Default.Medication,
                    tint = WarmAmber,
                    bgColor = CardBgAmber,
                    borderColor = BorderAmberSoft,
                    onClick = { onPromptClick("ওষুধের নাম লিখুন বা প্যাকেটের ছবি দিন, আমি এর সঠিক ব্যবহার, খাওয়ার নিয়ম ও সতর্কতা বুঝিয়ে দেব।") }
                )
                GeminiPromptCard(
                    modifier = Modifier.weight(1f),
                    title = "রিপোর্ট বুঝুন (ছবি দিয়ে)",
                    desc = "ল্যাব পরীক্ষার ছবি বিশ্লেষণ",
                    icon = Icons.Default.Science,
                    tint = OceanBlue,
                    bgColor = CardBgOcean,
                    borderColor = BorderOceanSoft,
                    onClick = onScanClick
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GeminiPromptCard(
                    modifier = Modifier.weight(1f),
                    title = "প্রেসক্রিপশন বুঝুন (ছবি দিয়ে)",
                    desc = "ব্যবস্থাপত্রের ছবি বিশ্লেষণ",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    tint = EmeraldGreen,
                    bgColor = CardBgMint,
                    borderColor = BorderMintSoft,
                    onClick = onScanClick
                )
                GeminiPromptCard(
                    modifier = Modifier.weight(1f),
                    title = "এআই স্বাস্থ্য পরামর্শ নিন",
                    desc = "লক্ষণ বা স্বাস্থ্য জিজ্ঞাসা করুন",
                    icon = Icons.Default.MonitorHeart,
                    tint = AiPurple,
                    bgColor = CardBgPurple,
                    borderColor = BorderTealSoft,
                    onClick = { onPromptClick("আমার শারীরিক সুস্থতা বা লক্ষণ সংক্রান্ত পরামর্শ দিন।") }
                )
            }
        }
    }
}

@Composable
private fun GeminiPromptCard(
    modifier: Modifier = Modifier,
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    bgColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(tint.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = desc,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun GeminiChatBubble(message: ChatMessage) {
    if (message.isFromUser) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Surface(
                color = PrimaryTeal,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 4.dp
                ),
                modifier = Modifier.widthIn(max = 290.dp)
            ) {
                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    color = Color.White,
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, PrimaryTeal.copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = com.example.mediaware.core.designsystem.R.drawable.ic_mediaware_logo),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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
                                text = "স্বাস্থ্য মেমোরিতে সংরক্ষিত",
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
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GeminiThinkingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, PrimaryTeal.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = com.example.mediaware.core.designsystem.R.drawable.ic_mediaware_logo),
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            strokeWidth = 2.dp,
            color = PrimaryTeal
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "জেমিনি এআই বিশ্লেষণ করছে...",
            fontSize = 12.sp,
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
        colors = CardDefaults.cardColors(containerColor = CardBgAmber),
        border = BorderStroke(1.dp, BorderAmberSoft)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(WarmAmber, CircleShape),
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
                    color = WarmAmber,
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
