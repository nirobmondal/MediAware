package com.example.mediaware.features.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.mediaware.core.designsystem.component.MediAwareBottomNavBar
import com.example.mediaware.core.designsystem.component.MediAwareNavTab
import androidx.hilt.navigation.compose.hiltViewModel

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
                title = { Text("হোম হাব") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "সেটিংস")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            MediAwareBottomNavBar(
                selectedTab = MediAwareNavTab.HOME,
                onTabSelected = { tab ->
                    when (tab) {
                        MediAwareNavTab.HOME -> { /* Already on Home */ }
                        MediAwareNavTab.SYMPTOMS -> onNavigateToSymptomSelect()
                        MediAwareNavTab.REPORT -> onNavigateToReportCapture()
                        MediAwareNavTab.REMINDERS -> onNavigateToReminders()
                        MediAwareNavTab.SETTINGS -> onNavigateToSettings()
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (!uiState.isOnline) {
                OfflineBanner()
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = "স্বাগতম, ${uiState.userName}",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            uiState.upcomingReminder?.let { reminder ->
                NextDoseCard(reminder = reminder)
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = "সেবা সমূহ",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            FeatureCardsGrid(
                onNavigateToSymptomSelect = onNavigateToSymptomSelect,
                onNavigateToReportCapture = onNavigateToReportCapture,
                onNavigateToPrescription = onNavigateToPrescription,
                onNavigateToChamberHub = onNavigateToChamberHub,
                onNavigateToConsultationSummary = onNavigateToConsultationSummary,
                onNavigateToTimeline = onNavigateToTimeline,
                onNavigateToMedicineHistory = onNavigateToMedicineHistory,
                onNavigateToReminders = onNavigateToReminders
            )
        }
    }
}

@Composable
fun OfflineBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFBA1A1A), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "আপনি অফলাইনে আছেন। কিছু সুবিধা সীমিত হতে পারে।",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun NextDoseCard(reminder: UpcomingReminderUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "পরবর্তী অ্যালার্ম: ${reminder.titleBn}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "সময়: ${reminder.timeFormattedBn}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "নির্দেশনা: ${reminder.instructionBn}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun FeatureCardsGrid(
    onNavigateToSymptomSelect: () -> Unit = {},
    onNavigateToReportCapture: () -> Unit = {},
    onNavigateToPrescription: () -> Unit = {},
    onNavigateToChamberHub: () -> Unit = {},
    onNavigateToConsultationSummary: () -> Unit = {},
    onNavigateToTimeline: () -> Unit = {},
    onNavigateToMedicineHistory: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {}
) {
    val features = listOf(
        FeatureItem("ল্যাব রিপোর্ট", Icons.Default.Science),
        FeatureItem("প্রেসক্রিপশন", Icons.Default.Description),
        FeatureItem("ডাক্তারের পরামর্শ", Icons.Default.ChatBubble),
        FeatureItem("চেম্বার মোড", Icons.Default.MedicalServices),
        FeatureItem("ভিজিট সারাংশ", Icons.AutoMirrored.Filled.Assignment),
        FeatureItem("স্বাস্থ্য স্মৃতি", Icons.Default.Timeline),
        FeatureItem("ওষুধ ইতিহাস", Icons.Default.History),
        FeatureItem("অ্যালার্ম সেন্টার", Icons.Default.Alarm)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(features) { feature ->
            FeatureCard(
                feature = feature,
                onClick = {
                    when (feature.title) {
                        "ডাক্তারের পরামর্শ" -> onNavigateToSymptomSelect()
                        "ল্যাব রিপোর্ট" -> onNavigateToReportCapture()
                        "প্রেসক্রিপশন" -> onNavigateToPrescription()
                        "চেম্বার মোড" -> onNavigateToChamberHub()
                        "ভিজিট সারাংশ" -> onNavigateToConsultationSummary()
                        "স্বাস্থ্য স্মৃতি" -> onNavigateToTimeline()
                        "ওষুধ ইতিহাস" -> onNavigateToMedicineHistory()
                        "অ্যালার্ম সেন্টার" -> onNavigateToReminders()
                    }
                }
            )
        }
    }
}

data class FeatureItem(val title: String, val icon: ImageVector)

@Composable
fun FeatureCard(
    feature: FeatureItem,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = feature.title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
