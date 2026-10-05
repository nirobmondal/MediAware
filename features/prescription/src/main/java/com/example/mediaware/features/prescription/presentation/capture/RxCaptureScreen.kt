package com.example.mediaware.features.prescription.presentation.capture

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.features.prescription.presentation.RxUiEvent
import com.example.mediaware.features.prescription.presentation.RxUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RxCaptureScreen(
    uiState: RxUiState,
    onEvent: (RxUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var isFlashOn by remember { mutableStateOf(false) }
    var showManualInputDialog by remember { mutableStateOf(false) }
    var manualInputText by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onEvent(RxUiEvent.OnImageCaptured(uri))
        }
    }

    val popularSuggestions = listOf(
        "Seclo 20mg 1+0+0 AC",
        "Comet 500mg 1+0+1 PC",
        "Osartil 50mg 0+0+1 PC",
        "Napa 500mg 1+1+1 PC",
        "Monas 10mg 0+0+1 HS"
    )

    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Viewfinder Camera Frame Simulation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF141919)),
                contentAlignment = Alignment.Center
            ) {
                // Document Boundary Reticle
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.90f)
                        .fillMaxHeight(0.60f)
                        .border(
                            border = BorderStroke(2.dp, PrimaryTeal),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = PrimaryTeal.copy(alpha = 0.7f),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "প্রেসক্রিপশন স্লিপটি এই ফ্রেমের মাঝে রাখুন",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ওষুধের নাম ও মাত্রার অংশটি স্পষ্ট রাখুন",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Top Bar Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "ফিরে যান",
                        tint = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { isFlashOn = !isFlashOn },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "ফ্ল্যাশ",
                            tint = if (isFlashOn) Color.Yellow else Color.White
                        )
                    }

                    // Voice / Text search fallback button
                    IconButton(
                        onClick = { showManualInputDialog = true },
                        modifier = Modifier
                            .background(PrimaryTeal.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "মুখে বলুন",
                            tint = Color.White
                        )
                    }
                }
            }

            // Bottom Control Panel
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Quick suggestion chips
                Text(
                    text = "দ্রুত ডেমো নির্বাচন করুন:",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(popularSuggestions) { suggestion ->
                        AssistChip(
                            onClick = {
                                onEvent(RxUiEvent.OnVoiceInputSubmitted(suggestion))
                            },
                            label = {
                                Text(
                                    text = suggestion,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFF263333)
                            ),
                            border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.5f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gallery Picker
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { galleryLauncher.launch("image/*") }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF262E2E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "গ্যালারি",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("গ্যালারি", color = Color.White, fontSize = 11.sp)
                    }

                    // Main Capture Shutter
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
                                galleryLauncher.launch("image/*")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(PrimaryTeal)
                                .border(2.dp, Color.White, CircleShape)
                        )
                    }

                    // Text / Manual Input
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { showManualInputDialog = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF262E2E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "লিখে যোগ",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("লিখে খুঁজুন", color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryTeal)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("প্রেসক্রিপশন স্ক্যান ও বিশ্লেষণ হচ্ছে...", color = Color.White)
                    }
                }
            }
        }
    }

    if (showManualInputDialog) {
        AlertDialog(
            onDismissRequest = { showManualInputDialog = false },
            title = { Text("ওষুধের নাম ও ডোজ লিখুন বা বলুন") },
            text = {
                Column {
                    Text(
                        text = "উদাহরণ: Seclo 20mg 1+0+1 a.c. 14 days",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = manualInputText,
                        onValueChange = { manualInputText = it },
                        label = { Text("প্রেসক্রিপশন লাইন") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = {
                                Toast.makeText(context, "ভয়েস ইনপুট শুনছি: 'মেটফরমিন ৫০০mg দিনে ২ বার'", Toast.LENGTH_SHORT).show()
                                manualInputText = "Metformin 500mg 1+0+1 p.c. 30 days"
                            }) {
                                Icon(Icons.Default.Mic, contentDescription = "ভয়েস")
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (manualInputText.isNotBlank()) {
                            onEvent(RxUiEvent.OnVoiceInputSubmitted(manualInputText))
                            showManualInputDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("যাচাই করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualInputDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
