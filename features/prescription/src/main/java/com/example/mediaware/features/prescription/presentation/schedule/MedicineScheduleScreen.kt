package com.example.mediaware.features.prescription.presentation.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.mediaware.features.prescription.domain.model.DoseSlot
import com.example.mediaware.features.prescription.domain.model.SlotSchedule
import com.example.mediaware.features.prescription.presentation.RxUiEvent
import com.example.mediaware.features.prescription.presentation.RxUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScheduleScreen(
    uiState: RxUiState,
    onEvent: (RxUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    var editingSlot by remember { mutableStateOf<SlotSchedule?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ডোজ শিডিউল ও অ্যালার্ম",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "দৈনিক ওষুধ গ্রহণের সময় বিন্যাস",
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
                        onClick = { onEvent(RxUiEvent.OnSaveScheduleAndAlarms) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AlarmOn,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🔔 অ্যালার্ম চালু ও সংরক্ষণ করুন",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Doze-Mode Android OS Compliance Shield Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryTeal.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "স্লিপ মোডেও (Doze Mode) অ্যালার্ম বাজবে",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "AlarmManager এর সাহায্যে ফোন বন্ধ বা স্ট্যান্ডবাই থাকলেও নির্ধারিত সময়ে অ্যালার্ম শোনা যাবে।",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            items(uiState.schedules) { schedule ->
                SlotScheduleCard(
                    schedule = schedule,
                    onToggle = { enabled -> onEvent(RxUiEvent.OnToggleSlot(schedule.slot, enabled)) },
                    onEditTime = { editingSlot = schedule }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Time Picker Dialog
    if (editingSlot != null) {
        val slot = editingSlot!!
        var selectedHour by remember { mutableIntStateOf(slot.hour) }
        var selectedMinute by remember { mutableIntStateOf(slot.minute) }

        AlertDialog(
            onDismissRequest = { editingSlot = null },
            title = { Text("${slot.slot.labelBn}র সময় পরিবর্তন করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "ঘণ্টা ও মিনিট নির্ধারণ করুন (২৪-ঘণ্টা ফরম্যাটে):",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour selector
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ঘণ্টা", fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (selectedHour > 0) selectedHour-- }) {
                                    Icon(Icons.Default.Remove, contentDescription = "কমান")
                                }
                                Text(
                                    text = selectedHour.toString().toBengaliDigits(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { if (selectedHour < 23) selectedHour++ }) {
                                    Icon(Icons.Default.Add, contentDescription = "বাড়ান")
                                }
                            }
                        }

                        Text(":", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                        // Minute selector
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("মিনিট", fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (selectedMinute >= 5) selectedMinute -= 5 else selectedMinute = 55 }) {
                                    Icon(Icons.Default.Remove, contentDescription = "কমান")
                                }
                                val minFormatted = if (selectedMinute < 10) "0$selectedMinute" else "$selectedMinute"
                                Text(
                                    text = minFormatted.toBengaliDigits(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { if (selectedMinute <= 50) selectedMinute += 5 else selectedMinute = 0 }) {
                                    Icon(Icons.Default.Add, contentDescription = "বাড়ান")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEvent(RxUiEvent.OnUpdateSlotTime(slot.slot, selectedHour, selectedMinute))
                        editingSlot = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("সংরক্ষণ")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSlot = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun SlotScheduleCard(
    schedule: SlotSchedule,
    onToggle: (Boolean) -> Unit,
    onEditTime: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (schedule.isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            1.dp,
            if (schedule.isEnabled) PrimaryTeal.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val slotIcon = when (schedule.slot) {
                        DoseSlot.MORNING -> Icons.Default.WbSunny
                        DoseSlot.NOON -> Icons.Default.LightMode
                        DoseSlot.NIGHT -> Icons.Default.Bedtime
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(PrimaryTeal.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = slotIcon,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${schedule.slot.labelBn}র ওষুধ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(onClick = onEditTime)
                        ) {
                            Text(
                                text = schedule.timeStringBn,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryTeal
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "সময় সম্পাদনা",
                                tint = PrimaryTeal,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Switch(
                    checked = schedule.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryTeal
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(10.dp))

            if (schedule.medicines.isEmpty()) {
                Text(
                    text = "এই সময়ে খাওয়ার মতো কোনো ওষুধ নেই।",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    schedule.medicines.forEach { med ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("•", color = PrimaryTeal, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${med.brandName} (${med.strength})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "— ${med.mealInstructionBn}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
