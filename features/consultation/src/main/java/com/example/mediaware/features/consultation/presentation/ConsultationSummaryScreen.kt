package com.example.mediaware.features.consultation.presentation

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.features.consultation.domain.model.ActionItemCategory
import com.example.mediaware.features.consultation.domain.model.ConsultationActionItem
import com.example.mediaware.features.consultation.domain.model.ConsultationSummary
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultationSummaryScreen(
    viewModel: ConsultationSummaryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToRecordAudio: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collectLatest { effect ->
            when (effect) {
                is ConsultationSummarySideEffect.LaunchCalendarIntent -> {
                    launchCalendarEvent(context, effect)
                }
                is ConsultationSummarySideEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ডাক্তার পরামর্শ সারাংশ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryTeal)
            }
        } else if (uiState.consultations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(PrimaryTeal.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalInformation,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = PrimaryTeal
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "এখনো কোনো পরামর্শ সংরক্ষিত নেই",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "চেম্বারে ডাক্তারের সাথে আলোচনা রেকর্ড করলে স্বয়ংক্রিয়ভাবে তারিখভিত্তিক পরামর্শের সারসংক্ষেপ ও কর্মপরিকল্পনা প্রস্তুত হবে।",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "সংরক্ষিত পরামর্শ (${uiState.consultations.size.toString().toBengaliDigits()})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(uiState.consultations, key = { it.id }) { summary ->
                    val isExpanded = uiState.expandedConsultationId == summary.id
                    val isCalendarScheduled = uiState.scheduledCalendarIds.contains(summary.id)
                    val selectedDays = uiState.customFollowUpDaysMap[summary.id] ?: summary.followUpDays
                    val customTimestamp = uiState.customFollowUpTimestampMap[summary.id]

                    ConsultationSummaryCard(
                        summary = summary,
                        isExpanded = isExpanded,
                        isCalendarScheduled = isCalendarScheduled,
                        selectedCategory = uiState.selectedCategory,
                        selectedFollowUpDays = selectedDays,
                        customTimestamp = customTimestamp,
                        onUpdateFollowUpDays = { days ->
                            viewModel.onEvent(ConsultationSummaryUiEvent.OnUpdateFollowUpDays(summary.id, days))
                        },
                        onSetCustomDate = { millis ->
                            viewModel.onEvent(ConsultationSummaryUiEvent.OnSetCustomDate(summary.id, millis))
                        },
                        onToggleExpand = { viewModel.onEvent(ConsultationSummaryUiEvent.OnToggleExpand(summary.id)) },
                        onToggleActionItem = { itemId -> viewModel.onEvent(ConsultationSummaryUiEvent.OnToggleActionItem(summary.id, itemId)) },
                        onSelectCategory = { cat -> viewModel.onEvent(ConsultationSummaryUiEvent.OnSelectCategory(cat)) },
                        onScheduleCalendar = { viewModel.onEvent(ConsultationSummaryUiEvent.OnScheduleCalendar(summary.id)) },
                        onDelete = { viewModel.onEvent(ConsultationSummaryUiEvent.OnDeleteConsultation(summary.id)) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    // Subtle Guardrail #3 disclaimer footer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "এটি কোনো প্রেসক্রিপশন নয়। শুধুমাত্র ডাক্তারের পরামর্শের সারাংশ।",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ConsultationSummaryCard(
    summary: ConsultationSummary,
    isExpanded: Boolean,
    isCalendarScheduled: Boolean,
    selectedCategory: ActionItemCategory?,
    selectedFollowUpDays: Int,
    customTimestamp: Long?,
    onUpdateFollowUpDays: (Int) -> Unit,
    onSetCustomDate: (Long) -> Unit,
    onToggleExpand: () -> Unit,
    onToggleActionItem: (String) -> Unit,
    onSelectCategory: (ActionItemCategory?) -> Unit,
    onScheduleCalendar: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val totalCount = summary.actionItems.size
    val completedCount = summary.actionItems.count { it.isCompleted }
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val progressAnimated by animateFloatAsState(targetValue = progressFraction, label = "progress")

    val filteredItems = remember(summary.actionItems, selectedCategory) {
        if (selectedCategory == null) summary.actionItems else summary.actionItems.filter { it.category == selectedCategory }
    }

    // Prepare DatePickerDialog for custom calendar date selection
    val baseMillis = customTimestamp ?: if (selectedFollowUpDays > 0) {
        System.currentTimeMillis() + (selectedFollowUpDays.toLong() * 24L * 60L * 60L * 1000L)
    } else {
        System.currentTimeMillis() + (7L * 24L * 60L * 60L * 1000L)
    }

    val cal = Calendar.getInstance().apply { timeInMillis = baseMillis }
    val datePickerDialog = remember(summary.id, baseMillis) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 10)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                onSetCustomDate(selectedCal.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }
    }

    val displayDateBn = remember(baseMillis) {
        val dateFormat = SimpleDateFormat("d MMMM yyyy (EEEE)", Locale.forLanguageTag("bn"))
        dateFormat.format(Date(baseMillis)).toBengaliDigits()
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("রেকর্ড মুছে ফেলতে চান?") },
            text = { Text("এই পরামর্শের সমস্ত সারাংশ ও কর্মপরিকল্পনা স্থায়ীভাবে মুছে ফেলা হবে।") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("মুছে ফেলুন", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (isExpanded) PrimaryTeal.copy(alpha = 0.5f) else Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 2.dp else 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Doctor & Visit Date
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PrimaryTeal.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = summary.doctorName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = summary.visitDateBn,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "মুছে ফেলুন",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "সংকুচিত করুন" else "প্রসারিত করুন",
                            tint = PrimaryTeal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Doctor advice summary bubble
            Surface(
                color = Color(0xFFF8FAFC),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFEDF2F7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "ডাক্তারের মূল পরামর্শ:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTeal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = summary.summaryBn,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action Items Progress Bar (if items exist)
            if (totalCount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "করণীয় অগ্রগতি:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${completedCount.toString().toBengaliDigits()} / ${totalCount.toString().toBengaliDigits()} সম্পন্ন",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTeal
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progressAnimated },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PrimaryTeal,
                    trackColor = PrimaryTeal.copy(alpha = 0.15f)
                )
            }

            // Expanded Full Section
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    if (totalCount > 0) {
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Filter Chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedCategory == null,
                                    onClick = { onSelectCategory(null) },
                                    label = { Text("সকল (${totalCount.toString().toBengaliDigits()})", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryTeal,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            items(ActionItemCategory.values()) { cat ->
                                val count = summary.actionItems.count { it.category == cat }
                                if (count > 0) {
                                    FilterChip(
                                        selected = selectedCategory == cat,
                                        onClick = { onSelectCategory(cat) },
                                        label = { Text("${cat.labelBn} (${count.toString().toBengaliDigits()})", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryTeal,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Items List
                        filteredItems.forEach { item ->
                            ActionItemRow(
                                item = item,
                                onToggle = { onToggleActionItem(item.id) }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    // Pending Questions Section
                    if (summary.pendingQuestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
                            border = BorderStroke(1.dp, Color(0xFFD0E3F8))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                        contentDescription = null,
                                        tint = Color(0xFF1565C0),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "পরবর্তী ফলো-আপে ডাক্তারকে যা জিজ্ঞেস করবেন:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1565C0)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                summary.pendingQuestions.forEach { q ->
                                    Text("• $q", fontSize = 12.sp, color = Color(0xFF0D47A1), lineHeight = 17.sp)
                                }
                            }
                        }
                    }

                    // Follow-up Appointment & Custom Date Calendar Card
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCalendarScheduled) Color(0xFFE8F5E9) else Color(0xFFF4FBFB)
                        ),
                        border = BorderStroke(1.dp, if (isCalendarScheduled) Color(0xFF81C784) else PrimaryTeal.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isCalendarScheduled) Icons.Default.EventAvailable else Icons.Default.Event,
                                    contentDescription = null,
                                    tint = if (isCalendarScheduled) Color(0xFF2E7D32) else PrimaryTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    val daysText = if (summary.followUpDays > 0) "${summary.followUpDays.toString().toBengaliDigits()} দিন পর" else "প্রয়োজন অনুযায়ী"
                                    Text(
                                        text = "ডাক্তারের প্রস্তাবিত ফলো-আপ: $daysText",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val cleanReason = summary.followUpReasonBn.trim()
                                    val isValidReason = cleanReason.isNotBlank() &&
                                            !cleanReason.equals("null", ignoreCase = true) &&
                                            !cleanReason.startsWith("null", ignoreCase = true)
                                    if (isValidReason) {
                                        Text(
                                            text = cleanReason,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Interactive Date Picker Banner
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { datePickerDialog.show() }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "রিমাইন্ডারের তারিখ (ট্যাপ করে পরিবর্তন করুন):",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = displayDateBn,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryTeal
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.EditCalendar,
                                        contentDescription = "তারিখ নির্বাচন করুন",
                                        tint = PrimaryTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick Day Chips
                            val quickDayOptions = remember(summary.followUpDays) {
                                val list = mutableListOf(3, 7, 10, 15, 30)
                                if (summary.followUpDays > 0 && !list.contains(summary.followUpDays)) {
                                    list.add(summary.followUpDays)
                                    list.sort()
                                }
                                list
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(quickDayOptions) { days ->
                                    val isSelected = selectedFollowUpDays == days && customTimestamp == null
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onUpdateFollowUpDays(days) },
                                        label = {
                                            Text(
                                                text = if (days == summary.followUpDays) "${days.toString().toBengaliDigits()} দিন (প্রস্তাবিত)" else "${days.toString().toBengaliDigits()} দিন",
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryTeal,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = onScheduleCalendar,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCalendarScheduled) Color(0xFF2E7D32) else PrimaryTeal
                                )
                            ) {
                                Icon(
                                    imageVector = if (isCalendarScheduled) Icons.Default.CheckCircle else Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isCalendarScheduled) "ক্যালেন্ডারে যুক্ত হয়েছে" else "ক্যালেন্ডারে ফলো-আপ যুক্ত করুন",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
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
fun ActionItemRow(
    item: ConsultationActionItem,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (item.isCompleted) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f) else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryTeal,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = getCategoryColor(item.category).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = item.category.labelBn,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = getCategoryColor(item.category),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = item.task,
                    fontSize = 12.sp,
                    fontWeight = if (item.isCompleted) FontWeight.Normal else FontWeight.Medium,
                    color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

private fun getCategoryColor(category: ActionItemCategory): Color {
    return when (category) {
        ActionItemCategory.MEDICATION -> Color(0xFF006A6A)
        ActionItemCategory.TEST -> Color(0xFFE65100)
        ActionItemCategory.LIFESTYLE -> Color(0xFF2E7D32)
        ActionItemCategory.GENERAL -> Color(0xFF1565C0)
    }
}

private fun launchCalendarEvent(context: Context, effect: ConsultationSummarySideEffect.LaunchCalendarIntent) {
    try {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, effect.title)
            putExtra(CalendarContract.Events.DESCRIPTION, effect.description)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, effect.startEpochMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, effect.startEpochMillis + (60 * 60 * 1000))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(intent, "ক্যালেন্ডার অ্যাপ নির্বাচন করুন")
        chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "ক্যালেন্ডার ওপেন করা সম্ভব হয়নি। রিমাইন্ডার সংরক্ষিত রাখা হলো।", Toast.LENGTH_LONG).show()
    }
}
