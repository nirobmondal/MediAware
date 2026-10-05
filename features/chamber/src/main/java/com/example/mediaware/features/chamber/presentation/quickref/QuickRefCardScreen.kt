package com.example.mediaware.features.chamber.presentation.quickref

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.features.chamber.domain.model.PatientPresentationSummary
import com.example.mediaware.features.chamber.presentation.ChamberUiEvent
import com.example.mediaware.features.chamber.presentation.ChamberUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickRefCardScreen(
    uiState: ChamberUiState,
    onEvent: (ChamberUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val activity = LocalContext.current as? Activity

    // 1. Maintain Screen ON while in Doctor Presentation Mode
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9F9))
    ) {
        Scaffold(
            containerColor = Color(0xFFF7F9F9),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "রোগী সামারি (ডাক্তারকে দেখান)",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF191C1C)
                            )
                            Text(
                                text = "স্ক্রিন উজ্জ্বলতা ও ওয়েক-লক চালু আছে",
                                fontSize = 11.sp,
                                color = PrimaryTeal,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    navigationIcon = {
                        if (!uiState.isTouchLocked) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান", tint = Color(0xFF191C1C))
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { onEvent(ChamberUiEvent.OnToggleTouchLock(!uiState.isTouchLocked)) }) {
                            Icon(
                                imageVector = if (uiState.isTouchLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "টাচ লক",
                                tint = if (uiState.isTouchLocked) Color(0xFFBA1A1A) else PrimaryTeal
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Patient Demographics & Known Conditions
                item {
                    PatientProfileCard(summary = uiState.patientSummary)
                }

                // Section 2: Chief Complaints
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E5E5))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "প্রধান লক্ষণ ও ব্যাপ্তি:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006A6A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• ${uiState.patientSummary.chiefComplaintsBn}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF191C1C),
                                lineHeight = 19.sp
                            )
                            Text(
                                text = "• সময়কাল: ${uiState.patientSummary.complaintDurationBn}",
                                fontSize = 12.sp,
                                color = Color(0xFF556060)
                            )
                        }
                    }
                }

                // Section 3: Recent Lab Values (High Contrast Medical Layout)
                item {
                    RecentLabsCard(summary = uiState.patientSummary)
                }

                // Section 4: Longitudinal Trends
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E5E5))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "📈 পূর্ববর্তী ট্রেন্ড পর্যবেক্ষণ:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006A6A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.patientSummary.trendObservationBn,
                                fontSize = 13.sp,
                                color = Color(0xFF191C1C),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Section 5: Current Active Medications
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E5E5))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "💊 বর্তমান নিয়মিত ওষুধ:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006A6A)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = uiState.patientSummary.currentMedicinesBn,
                                fontSize = 13.sp,
                                color = Color(0xFF191C1C),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                item {
                    // Lock button CTA at bottom
                    OutlinedButton(
                        onClick = { onEvent(ChamberUiEvent.OnToggleTouchLock(true)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, PrimaryTeal)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ডাক্তারকে দেওয়ার আগে স্ক্রিন লক করুন",
                            color = PrimaryTeal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // 2. Touch Lock Intercept Overlay
        if (uiState.isTouchLocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { onEvent(ChamberUiEvent.OnToggleTouchLock(false)) }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFBA1A1A),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "টাচ লক সক্রিয় আছে",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191C1C)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ডাক্তার প্রদর্শনের সুবিধার্থে স্পর্শ বন্ধ রাখা হয়েছে।\nআনলক করতে স্ক্রিনে ২ বার দ্রুত স্পর্শ (Double-tap) করুন।",
                            fontSize = 12.sp,
                            color = Color(0xFF556060),
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PatientProfileCard(summary: PatientPresentationSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E5E5))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👤 ${summary.patientNameBn}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191C1C)
                )
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "রক্ত: ${summary.bloodGroup}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "বয়স: ${summary.ageYears.toString().toBengaliDigits()} বছর | লিঙ্গ: ${summary.genderBn}",
                fontSize = 12.sp,
                color = Color(0xFF556060)
            )

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = Color(0xFFF0F4F4))
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "পরিচিত দীর্ঘমেয়াদী রোগ: ${summary.chronicConditionsBn}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF191C1C)
            )
        }
    }
}

@Composable
fun RecentLabsCard(summary: PatientPresentationSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E5E5))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "🧪 সাম্প্রতিক টেস্ট রিপোর্ট সারসংক্ষেপ:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF006A6A)
            )
            Spacer(modifier = Modifier.height(8.dp))

            summary.recentLabs.forEach { lab ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = lab.testName,
                        fontSize = 13.sp,
                        color = Color(0xFF191C1C),
                        fontWeight = FontWeight.Medium
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = lab.valueWithUnitBn,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191C1C)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(lab.statusColorHex).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = lab.statusTag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(lab.statusColorHex),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
