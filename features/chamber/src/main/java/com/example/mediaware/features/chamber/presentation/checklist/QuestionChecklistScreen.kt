package com.example.mediaware.features.chamber.presentation.checklist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.features.chamber.domain.model.DoctorQuestionItem
import com.example.mediaware.features.chamber.presentation.ChamberUiEvent
import com.example.mediaware.features.chamber.presentation.ChamberUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionChecklistScreen(
    uiState: ChamberUiState,
    onEvent: (ChamberUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    var showAddQuestionDialog by remember { mutableStateOf(false) }
    var newQuestionText by remember { mutableStateOf("") }

    val progressFraction = if (uiState.totalQuestionCount > 0) {
        uiState.discussedCount.toFloat() / uiState.totalQuestionCount.toFloat()
    } else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ডাক্তারকে প্রশ্ন তালিকা",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "আলোচনা হলে টিকচিহ্ন দিয়ে চিহ্নিত করুন",
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
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "আলোচনা সম্পন্ন (ফিরে যান) ➡️",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Progress Tracker Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryTeal.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "আলোচনার অগ্রগতি:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                            Text(
                                text = uiState.progressFormattedBn,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // AI Cheat Questions Header
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PrimaryTeal.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "জেমিনাই এআই দ্বারা সাজানো ব্যক্তিগত প্রশ্নাবলি",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                        )
                    }
                }
            }

            items(uiState.questions, key = { it.id }) { question ->
                QuestionChecklistItem(
                    item = question,
                    onToggle = { onEvent(ChamberUiEvent.OnToggleQuestion(question.id)) }
                )
            }

            item {
                OutlinedButton(
                    onClick = { showAddQuestionDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PrimaryTeal)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "➕ নতুন প্রশ্ন যোগ করুন",
                        color = PrimaryTeal,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showAddQuestionDialog) {
        AlertDialog(
            onDismissRequest = { showAddQuestionDialog = false },
            title = { Text("নতুন প্রশ্ন লিখুন") },
            text = {
                Column {
                    Text(
                        text = "ডাক্তারকে জিজ্ঞেস করার জন্য আপনার প্রশ্নটি লিখুন:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newQuestionText,
                        onValueChange = { newQuestionText = it },
                        label = { Text("প্রশ্ন") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newQuestionText.isNotBlank()) {
                            onEvent(ChamberUiEvent.OnAddCustomQuestion(newQuestionText))
                            newQuestionText = ""
                            showAddQuestionDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("সংরক্ষণ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddQuestionDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun QuestionChecklistItem(
    item: DoctorQuestionItem,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isDiscussed) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (item.isDiscussed) Color(0xFF86EFAC) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (item.isDiscussed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (item.isDiscussed) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = if (item.isDiscussed) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = item.categoryBn,
                        fontSize = 10.sp,
                        color = if (item.isDiscussed) Color(0xFF15803D) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.questionBn,
                    fontSize = 13.sp,
                    fontWeight = if (item.isDiscussed) FontWeight.Normal else FontWeight.Medium,
                    color = if (item.isDiscussed) Color(0xFF6B7280) else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (item.isDiscussed) TextDecoration.LineThrough else TextDecoration.None,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
