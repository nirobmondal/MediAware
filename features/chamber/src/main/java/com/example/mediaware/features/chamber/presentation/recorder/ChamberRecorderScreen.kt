package com.example.mediaware.features.chamber.presentation.recorder

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.mediaware.features.chamber.presentation.ChamberUiEvent
import com.example.mediaware.features.chamber.presentation.ChamberUiState
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChamberRecorderScreen(
    uiState: ChamberUiState,
    onEvent: (ChamberUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onEvent(ChamberUiEvent.OnStartRecording)
        } else {
            Toast.makeText(context, "অডিও রেকর্ড করতে মাইক্রোফোন ব্যবহারের অনুমতি দিন", Toast.LENGTH_SHORT).show()
        }
    }

    val maxSeconds = 15 * 60f // 900 seconds
    val progressFraction = (uiState.recordingDurationSeconds.toFloat() / maxSeconds).coerceIn(0f, 1f)

    // Pulsing animation for recording red dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "পরামর্শ অডিও রেকর্ডার",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "১৫ মিনিট সর্বোচ্চ স্বয়ংক্রিয় সীমা",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Consent confirmation & 14-min warning
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 14-Minute Warning Banner
                if (uiState.is14MinWarningActive) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        border = BorderStroke(1.dp, Color(0xFFE57373)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC62828))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⚠️ ১৪ মিনিট অতিক্রান্ত! আর ১ মিনিট পর রেকর্ডিং স্বয়ংক্রিয়ভাবে সংরক্ষিত হবে।",
                                color = Color(0xFFC62828),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // Consent Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.hasDoctorConsent) Color(0xFFE8F5E9) else Color(0xFFFFF8E1)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (uiState.hasDoctorConsent) Color(0xFF81C784) else Color(0xFFFFD54F)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ডাক্তার সাহেবের অনুমতি",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.hasDoctorConsent) Color(0xFF2E7D32) else Color(0xFF8D6E63)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "রেকর্ডিং শুরুর পূর্বে চিকিৎসকের মৌখিক সম্মতি নিশ্চিত করুন।",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = uiState.hasDoctorConsent,
                            onCheckedChange = { onEvent(ChamberUiEvent.OnToggleDoctorConsent(it)) },
                            enabled = !uiState.isRecording,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2E7D32)
                            )
                        )
                    }
                }
            }

            // Middle Section: Visualizer & Timer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (uiState.isRecording) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFBA1A1A).copy(alpha = pulseAlpha))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "রেকর্ডিং চলছে...",
                            color = Color(0xFFBA1A1A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Bengali Timer Display
                Text(
                    text = uiState.durationFormattedBn,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.isRecording) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Waveform Amplitude Bars
                AudioWaveformVisualizer(
                    isRecording = uiState.isRecording,
                    amplitude = uiState.currentAmplitude
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 15-Minute Watchdog Progress Bar
                Column(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (progressFraction > 0.9f) Color(0xFFBA1A1A) else PrimaryTeal,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "১৫ মিনিট স্বয়ংক্রিয় ওয়াচডগ ক্যাপ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Bottom Section: Record/Stop Button & Security Notice
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Record / Stop Control Button
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isRecording) Color(0xFFBA1A1A) else if (uiState.hasDoctorConsent) PrimaryTeal else Color(0xFFB0BEC5))
                        .clickable(enabled = uiState.hasDoctorConsent || uiState.isRecording) {
                            if (uiState.isRecording) {
                                onEvent(ChamberUiEvent.OnStopRecording)
                            } else {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    onEvent(ChamberUiEvent.OnStartRecording)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (uiState.isRecording) "থামান" else "রেকর্ড শুরু করুন",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (uiState.isRecording) "রেকর্ডিং থামাতে চাপ দিন" else if (uiState.hasDoctorConsent) "রেকর্ডিং শুরু করতে চাপ দিন" else "অনুমতি নিশ্চিত করে রেকর্ড শুরু করুন",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "🔒 অডিও ফাইলটি আপনার ডিভাইসের সুরক্ষিত মেমরিতে (filesDir/vault/) সংরক্ষিত হয়।",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun AudioWaveformVisualizer(
    isRecording: Boolean,
    amplitude: Int
) {
    val barCount = 18
    val normAmp = (amplitude.toFloat() / 32767f).coerceIn(0f, 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val randomFactor = if (isRecording) Random.nextFloat() * 0.5f + 0.5f else 0.1f
            val barHeight = if (isRecording) {
                (12.dp + (44.dp * normAmp * randomFactor)).coerceIn(8.dp, 52.dp)
            } else {
                8.dp
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isRecording) PrimaryTeal else MaterialTheme.colorScheme.outlineVariant)
            )
        }
    }
}
