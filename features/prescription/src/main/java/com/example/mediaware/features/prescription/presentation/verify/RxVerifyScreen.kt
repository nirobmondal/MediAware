package com.example.mediaware.features.prescription.presentation.verify

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.features.prescription.domain.model.PrescriptionItem
import com.example.mediaware.features.prescription.presentation.RxUiEvent
import com.example.mediaware.features.prescription.presentation.RxUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RxVerifyScreen(
    uiState: RxUiState,
    onEvent: (RxUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ওষুধ যাচাই করুন",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "প্রেসক্রিপশন অনুযায়ী মিলিয়ে নিন",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { onEvent(RxUiEvent.OnLoadExplanations) },
                        enabled = uiState.items.isNotEmpty() && !uiState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("তথ্য বিশ্লেষণ হচ্ছে...", fontSize = 15.sp)
                        } else {
                            Text(
                                text = "সব ঠিক আছে, ওষুধের তথ্য দেখুন ➡️",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "শনাক্তকৃত ওষুধের নাম বা মাত্রা ভুল হলে ✏️ আইকনে চাপ দিয়ে সংশোধন করে নিন।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (uiState.items.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "কোনো ওষুধ শনাক্ত করা যায়নি",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "প্রেসক্রিপশনের লেখা অস্পষ্ট ছিল। উপরের '+ ওষুধ যোগ করুন' বাটনে চাপ দিয়ে ম্যানুয়ালি ওষুধ যুক্ত করুন বা পুনরায় স্পষ্ট ছবি তুলুন।",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            items(uiState.items, key = { it.id }) { item ->
                MedicineItemCard(
                    item = item,
                    onEdit = { onEvent(RxUiEvent.OnStartEdit(item)) },
                    onDelete = { onEvent(RxUiEvent.OnDeleteItem(item.id)) }
                )
            }

            item {
                OutlinedButton(
                    onClick = { onEvent(RxUiEvent.OnStartAdd) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PrimaryTeal)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("নতুন ওষুধ যোগ করুন", color = PrimaryTeal, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Edit Item Dialog
    if (uiState.editingItem != null) {
        val item = uiState.editingItem
        var editBrand by remember { mutableStateOf(item.brandName) }
        var editPattern by remember { mutableStateOf(item.rawDosagePattern) }
        var editMeal by remember { mutableStateOf(item.rawMealTiming ?: "PC") }
        var editDuration by remember { mutableStateOf(item.durationDays.toString()) }

        AlertDialog(
            onDismissRequest = { onEvent(RxUiEvent.OnDismissDialog) },
            title = { Text("ওষুধ সংশোধন করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editBrand,
                        onValueChange = { editBrand = it },
                        label = { Text("ওষুধের নাম ও পাওয়ার") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPattern,
                        onValueChange = { editPattern = it },
                        label = { Text("ডোজ কোড (যেমন: 1+0+1, OD, BD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editMeal,
                        onValueChange = { editMeal = it },
                        label = { Text("খাওয়ার নিয়ম (AC = আগে, PC = পরে)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editDuration,
                        onValueChange = { editDuration = it },
                        label = { Text("কোর্স সময়কাল (দিন সংখ্যা)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val duration = editDuration.toIntOrNull() ?: 7
                        onEvent(
                            RxUiEvent.OnSaveEdit(
                                item.copy(
                                    brandName = editBrand,
                                    rawDosagePattern = editPattern,
                                    rawMealTiming = editMeal,
                                    durationDays = duration
                                )
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("সংরক্ষণ")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(RxUiEvent.OnDismissDialog) }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Add New Item Dialog
    if (uiState.isAddingNew) {
        var newBrand by remember { mutableStateOf("") }
        var newPattern by remember { mutableStateOf("1+0+1") }
        var newMeal by remember { mutableStateOf("PC") }
        var newDuration by remember { mutableStateOf("7") }

        AlertDialog(
            onDismissRequest = { onEvent(RxUiEvent.OnDismissDialog) },
            title = { Text("নতুন ওষুধ যোগ করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newBrand,
                        onValueChange = { newBrand = it },
                        label = { Text("ওষুধের নাম (যেমন: Napa 500mg)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPattern,
                        onValueChange = { newPattern = it },
                        label = { Text("ডোজ (1+0+1, 1+0+0, 0+0+1 ইত্যাদি)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newMeal,
                        onValueChange = { newMeal = it },
                        label = { Text("খাওয়ার সময় (AC = আগে, PC = পরে)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDuration,
                        onValueChange = { newDuration = it },
                        label = { Text("কত দিন চলবে (দিন)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBrand.isNotBlank()) {
                            val duration = newDuration.toIntOrNull() ?: 7
                            onEvent(
                                RxUiEvent.OnSaveNew(
                                    brandName = newBrand,
                                    dosePattern = newPattern,
                                    mealTiming = newMeal,
                                    durationDays = duration
                                )
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("যোগ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(RxUiEvent.OnDismissDialog) }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun MedicineItemCard(
    item: PrescriptionItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                            .size(36.dp)
                            .background(PrimaryTeal.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💊", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.brandName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (item.genericName.isNotBlank() && item.genericName != item.brandName) {
                            Text(
                                text = "(${item.genericName})",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "সম্পাদনা", tint = PrimaryTeal, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(
                    onClick = {},
                    label = { Text("ডোজ: ${item.rawDosagePattern}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = PrimaryTeal.copy(alpha = 0.12f),
                        labelColor = PrimaryTeal
                    ),
                    border = null
                )

                SuggestionChip(
                    onClick = {},
                    label = { Text(item.mealInstructionBn, fontSize = 11.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = null
                )

                SuggestionChip(
                    onClick = {},
                    label = { Text("${item.durationDays.toString().toBengaliDigits()} দিন", fontSize = 11.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = null
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "সময়সূচী: ${item.timingSlotBn}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
