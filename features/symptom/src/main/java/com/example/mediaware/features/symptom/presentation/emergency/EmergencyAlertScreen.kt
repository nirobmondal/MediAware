package com.example.mediaware.features.symptom.presentation.emergency

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun EmergencyAlertScreen(
    reasonBn: String,
    matchedSymptoms: List<String> = emptyList(),
    viewModel: EmergencyAlertViewModel = hiltViewModel(),
    onProceedToPrep: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(reasonBn) {
        viewModel.onEvent(EmergencyAlertUiEvent.SetEmergencyReason(reasonBn, matchedSymptoms))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                EmergencyAlertSideEffect.TriggerEmergencyCall -> {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:999")
                    }
                    context.startActivity(intent)
                }
                is EmergencyAlertSideEffect.SendCaregiverSms -> {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, effect.alertText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "স্বজনকে সতর্কবার্তা পাঠান"))
                }
                EmergencyAlertSideEffect.ProceedToVisitPrep -> {
                    onProceedToPrep()
                }
            }
        }
    }

    // Pulsating animation for red warning icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Scaffold(
        containerColor = Color(0xFFBA1A1A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Pulsating Warning Icon Badge
                Box(
                    modifier = Modifier
                        .scale(scale)
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "জরুরি সতর্কতা",
                        tint = Color(0xFFBA1A1A),
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "জরুরি সতর্কতা!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "এক মুহূর্তও দেরি না করে এখনই হাসপাতালে যান",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFDAD6)
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Matched Reason Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "কেন জরুরি?",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFBA1A1A)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.reasonBn,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = Color(0xFF191C1C),
                                lineHeight = 24.sp
                            )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // 1-Tap 999 Direct Emergency Call Button
                Button(
                    onClick = { viewModel.onEvent(EmergencyAlertUiEvent.OnCall999Clicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFFBA1A1A)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "৯৯৯ কল করুন",
                        tint = Color(0xFFBA1A1A),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "জাতীয় জরুরি সেবা: ৯৯৯ (কল করুন)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFBA1A1A)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Caregiver SMS / Alert Button
                OutlinedButton(
                    onClick = { viewModel.onEvent(EmergencyAlertUiEvent.OnAlertCaregiverClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color.White)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "স্বজনকে সতর্কবার্তা পাঠান",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "স্বজনকে জরুরি বার্তা পাঠান",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Secondary escape hatch
                TextButton(
                    onClick = { viewModel.onEvent(EmergencyAlertUiEvent.OnBypassEmergencyClicked) }
                ) {
                    Text(
                        text = "(জরুরি বিপদ না হলে স্বাভাবিকভাবে ভিজিট কার্ড দেখুন)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFFFDAD6),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
