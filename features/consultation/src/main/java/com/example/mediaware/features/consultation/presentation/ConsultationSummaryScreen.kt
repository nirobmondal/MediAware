package com.example.mediaware.features.consultation.presentation

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
                    Column {
                        Text(
                            text = "ডাক্তার ভিজিট ও পরামর্শ সারাংশ",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "তারিখভিত্তিক সারসংক্ষেপ ও কর্মপরিকল্পনা",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToRecordAudio) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "নতুন অডিও রেকর্ড",
                            tint = PrimaryTeal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (uiState.consultations.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToRecordAudio,
                    containerColor = PrimaryTeal,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(28.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("নতুন পরামর্শ রেকর্ড", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
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
            // Empty State
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
                            .background(PrimaryTeal.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MicNone,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = PrimaryTeal
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "এখনো কোনো পরামর্শ রেকর্ড করা হয়নি",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ডাক্তারের সাথে সাক্ষাতের সময় আলোচনা রেকর্ড করুন। আমাদের জেমিনি এআই স্বয়ংক্রিয়ভাবে তারিখভিত্তিক সারাংশ, প্রেসক্রিপশন ও করণীয় কর্মপরিকল্পনা তৈরি করে দেবে।",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateToRecordAudio,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("পরামর্শ অডিও রেকর্ড শুরু করুন", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Date-wise list of consultations (recent on top)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    // Clinical Safety Disclaimer (Guardrail #3)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "এটি কোনো প্রেসক্রিপশন নয়। শুধুমাত্র ডাক্তারের পরামর্শের সারাংশ। যেকোনো সিদ্ধান্তে চিকিৎসকের পরামর্শ নিন।",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE65100),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "সংরক্ষিত পরামর্শ সমূহ (${uiState.consultations.size.toString().toBengaliDigits()} টি)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "সর্বশেষটি শীর্ষে",
                            fontSize = 11.sp,
                            color = PrimaryTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                items(uiState.consultations, key = { it.id }) { summary ->
                    val isExpanded = uiState.expandedConsultationId == summary.id
                    val isCalendarScheduled = uiState.scheduledCalendarIds.contains(summary.id)

                    ConsultationSummaryCard(
                        summary = summary,
                        isExpanded = isExpanded,
                        isCalendarScheduled = isCalendarScheduled,
                        selectedCategory = uiState.selectedCategory,
                        onToggleExpand = { viewModel.onEvent(ConsultationSummaryUiEvent.OnToggleExpand(summary.id)) },
                        onToggleActionItem = { itemId -> viewModel.onEvent(ConsultationSummaryUiEvent.OnToggleActionItem(summary.id, itemId)) },
                        onSelectCategory = { cat -> viewModel.onEvent(ConsultationSummaryUiEvent.OnSelectCategory(cat)) },
                        onScheduleCalendar = { viewModel.onEvent(ConsultationSummaryUiEvent.OnScheduleCalendar(summary.id)) },
                        onDelete = { viewModel.onEvent(ConsultationSummaryUiEvent.OnDeleteConsultation(summary.id)) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp)) // Extra space for FAB
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
    onToggleExpand: () -> Unit,
    onToggleActionItem: (String) -> Unit,
    onSelectCategory: (ActionItemCategory?) -> Unit,
    onScheduleCalendar: () -> Unit,
    onDelete: () -> Unit
) {
    val totalCount = summary.actionItems.size
    val completedCount = summary.actionItems.count { it.isCompleted }
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val progressAnimated by animateFloatAsState(targetValue = progressFraction, label = "progress")

    val filteredItems = remember(summary.actionItems, selectedCategory) {
        if (selectedCategory == null) summary.actionItems else summary.actionItems.filter { it.category == selectedCategory }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, if (isExpanded) PrimaryTeal.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Date & Doctor
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        color = PrimaryTeal.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = summary.visitDateBn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = summary.doctorName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "মুছে ফেলুন",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
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

            Spacer(modifier = Modifier.height(8.dp))

            // Action Items Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "করণীয় অগ্রগতি:",
                    fontSize = 12.sp,
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

            Spacer(modifier = Modifier.height(10.dp))

            // Advice summary excerpt / full text
            if (!isExpanded) {
                Text(
                    text = "\"${summary.summaryBn}\"",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "\"${summary.summaryBn}\"",
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Expanded Full Details
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Filter Chips
                    Text(
                        text = "করণীয় তালিকা ফিল্টার:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Items List
                    filteredItems.forEach { item ->
                        ActionItemRow(
                            item = item,
                            onToggle = { onToggleActionItem(item.id) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Pending Questions Section
                    if (summary.pendingQuestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                            border = BorderStroke(1.dp, Color(0xFF90CAF9))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                        contentDescription = null,
                                        tint = Color(0xFF1565C0),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "জিজ্ঞেস করা বাকি বা পরবর্তী প্রশ্ন:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1565C0)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                summary.pendingQuestions.forEach { q ->
                                    Text("• $q", fontSize = 12.sp, color = Color(0xFF0D47A1), lineHeight = 16.sp)
                                }
                            }
                        }
                    }

                    // Follow-up Appointment & Calendar Card
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCalendarScheduled) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, if (isCalendarScheduled) Color(0xFF81C784) else PrimaryTeal.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isCalendarScheduled) Icons.Default.EventAvailable else Icons.Default.Event,
                                    contentDescription = null,
                                    tint = if (isCalendarScheduled) Color(0xFF2E7D32) else PrimaryTeal,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "পরবর্তী ফলো-আপ ভিজিট: ${summary.followUpDateStringBn}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = summary.followUpReasonBn,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    text = if (isCalendarScheduled) "ক্যালেন্ডারে যুক্ত হয়েছে" else "পরবর্তী ভিজিট ক্যালেন্ডারে যুক্ত করুন",
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
            if (item.isCompleted) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
