package com.example.mediaware.features.symptom.presentation.select

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.features.symptom.domain.model.Symptom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomSelectScreen(
    viewModel: SymptomSelectViewModel = hiltViewModel(),
    onNavigateToFollowup: (List<String>) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SymptomSelectSideEffect.NavigateToFollowup -> onNavigateToFollowup(effect.symptomIds)
                is SymptomSelectSideEffect.ShowToast -> Toast.makeText(context, effect.messageBn, Toast.LENGTH_SHORT).show()
                SymptomSelectSideEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "কি সমস্যা হচ্ছে?",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "লক্ষণগুলো স্পর্শ করে বাছাই করুন",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = { viewModel.onEvent(SymptomSelectUiEvent.OnProceedToFollowup) },
                    enabled = uiState.selectedSymptomIds.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    val countText = uiState.selectedSymptomIds.size.toString().toBengaliDigits()
                    Text(
                        text = if (uiState.selectedSymptomIds.isNotEmpty()) {
                            "পরবর্তী ধাপ ($countText টি বাছাইকৃত)"
                        } else {
                            "কমপক্ষে একটি লক্ষণ নির্বাচন করুন"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Bengali Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onEvent(SymptomSelectUiEvent.OnSearchQueryChanged(it)) },
                placeholder = { Text("লক্ষণ বা অঙ্গ খুঁজুন (যেমন: মাথা, বুক, জ্বর)...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onEvent(SymptomSelectUiEvent.OnSearchQueryChanged("")) }) {
                            Icon(Icons.Default.Clear, contentDescription = "মুছুন")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Voice Recognition Helper Pill Bar
            Card(
                onClick = { viewModel.onEvent(SymptomSelectUiEvent.OnToggleVoiceInput) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isListeningVoice) Color(0xFFFFEBEE) else Color(0xFFE0F2F1)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isListeningVoice) Icons.Default.Mic else Icons.Default.MicNone,
                        contentDescription = "ভয়েস ইনপুট",
                        tint = if (uiState.isListeningVoice) Color(0xFFBA1A1A) else PrimaryTeal
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isListeningVoice) "শুনছি... বলুন কি সমস্যা হচ্ছে (ট্যাপ করে বন্ধ করুন)" else "মুখে বলে লক্ষণ যোগ করুন (ট্যাপ করুন)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (uiState.isListeningVoice) Color(0xFFBA1A1A) else PrimaryTeal,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            // Selected Symptoms Chips Row
            AnimatedVisibility(visible = uiState.selectedSymptomIds.isNotEmpty()) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "বাছাইকৃত লক্ষণসমূহ:",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.selectedSymptomIds.toList()) { id ->
                            val symptom = uiState.availableSymptoms.firstOrNull { it.id == id }
                            if (symptom != null) {
                                InputChip(
                                    selected = true,
                                    onClick = { viewModel.onEvent(SymptomSelectUiEvent.OnRemoveSelectedSymptom(id)) },
                                    label = { Text(symptom.nameBn) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "বাদ দিন",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = InputChipDefaults.inputChipColors(
                                        selectedContainerColor = Color(0xFFD5F5F5),
                                        selectedLabelColor = PrimaryTeal
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 18 Anatomical Symptoms Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.filteredSymptoms, key = { it.id }) { symptom ->
                    val isSelected = uiState.selectedSymptomIds.contains(symptom.id)
                    SymptomCard(
                        symptom = symptom,
                        isSelected = isSelected,
                        onClick = { viewModel.onEvent(SymptomSelectUiEvent.OnSymptomToggled(symptom.id)) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SymptomCard(
    symptom: Symptom,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE6F7F7) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) PrimaryTeal else Color(0xFFCFD9D9)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) PrimaryTeal else Color(0xFFF0F4F4)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getSymptomIcon(symptom.iconName),
                        contentDescription = symptom.nameBn,
                        tint = if (isSelected) Color.White else PrimaryTeal,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (symptom.isRedFlagPotential) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFEBEE))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "সতর্কতা",
                            color = Color(0xFFBA1A1A),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = symptom.nameBn,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) PrimaryTeal else MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 2
            )

            Text(
                text = symptom.anatomicalRegionBn,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

private fun getSymptomIcon(iconName: String): ImageVector {
    return when (iconName) {
        "head", "head_pain" -> Icons.Default.Face
        "eye" -> Icons.Default.Visibility
        "record_voice_over" -> Icons.Default.RecordVoiceOver
        "favorite" -> Icons.Default.Favorite
        "monitor_heart" -> Icons.Default.MonitorHeart
        "air" -> Icons.Default.Air
        "coronavirus" -> Icons.Default.Coronavirus
        "sick" -> Icons.Default.Sick
        "healing" -> Icons.Default.Healing
        "thermostat" -> Icons.Default.Thermostat
        "psychology" -> Icons.Default.Psychology
        "accessible" -> Icons.Default.Accessible
        "battery_alert" -> Icons.Default.BatteryAlert
        "accessibility" -> Icons.Default.Accessibility
        "water_drop" -> Icons.Default.WaterDrop
        "fitness_center" -> Icons.Default.FitnessCenter
        "water" -> Icons.Default.Water
        else -> Icons.Default.LocalHospital
    }
}
