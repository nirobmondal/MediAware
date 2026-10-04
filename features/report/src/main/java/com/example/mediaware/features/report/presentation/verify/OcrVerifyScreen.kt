package com.example.mediaware.features.report.presentation.verify

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.core.designsystem.util.toEnglishDigits
import com.example.mediaware.features.report.domain.model.ExtractedLabItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrVerifyScreen(
    rawOcrText: String,
    viewModel: OcrVerifyViewModel = hiltViewModel(),
    onNavigateToAnalysis: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(rawOcrText) {
        viewModel.onEvent(OcrVerifyUiEvent.ParseRawOcr(rawOcrText))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OcrVerifySideEffect.NavigateToAnalysis -> onNavigateToAnalysis(effect.itemsJson)
                is OcrVerifySideEffect.ShowToast -> Toast.makeText(context, effect.messageBn, Toast.LENGTH_SHORT).show()
                OcrVerifySideEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "মান যাচাই করুন",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "ভুল মান থাকলে পেন্সিল আইকনে ট্যাপ করে ঠিক করুন",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = { viewModel.onEvent(OcrVerifyUiEvent.OnConfirmAndAnalyze) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text(
                        text = "✅ মান সঠিক আছে, বিশ্লেষণ করুন ➡️",
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
            Spacer(modifier = Modifier.height(10.dp))

            // Safety Informational Alert Box (Heuristic #5: Error Prevention)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "স্বয়ংক্রিয় স্ক্যানে কোনো সংখ্যা অস্পষ্ট বা ভুল মনে হলে অনুগ্রহ করে এখনই সংশোধন করে নিন।",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PrimaryTeal,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "শনাক্তকৃত ল্যাব টেস্টসমূহ (${uiState.labItems.size.toString().toBengaliDigits()}টি)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = { viewModel.onEvent(OcrVerifyUiEvent.OnShowAddDialog) }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("নতুন যোগ করুন")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Editable Lab Items Table
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(uiState.labItems, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        modifier = Modifier.fillMaxWidth()
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
                                    text = item.testNameBn,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "স্বাভাবিক পরিসীমা: ${item.normalMin.toString().toBengaliDigits()} - ${item.normalMax.toString().toBengaliDigits()} ${item.unit}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }

                            // Value display with pencil button
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF0F4F4),
                                    border = BorderStroke(1.dp, PrimaryTeal),
                                    modifier = Modifier.clickable { viewModel.onEvent(OcrVerifyUiEvent.OnEditItemClicked(item)) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${item.numericValue.toString().toBengaliDigits()} ${item.unit}",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = PrimaryTeal
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "সম্পাদনা",
                                            tint = PrimaryTeal,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = { viewModel.onEvent(OcrVerifyUiEvent.OnDeleteItem(item.id)) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "মুছুন",
                                        tint = Color(0xFFBA1A1A),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Item Value Dialog
    uiState.editingItem?.let { item ->
        var tempValueText by remember(item) { mutableStateOf(item.numericValue.toString()) }

        AlertDialog(
            onDismissRequest = { viewModel.onEvent(OcrVerifyUiEvent.OnDismissEditDialog) },
            title = { Text(text = "${item.testNameBn} সংশোধন") },
            text = {
                Column {
                    Text(
                        text = "ল্যাব রিপোর্টে উল্লেখিত সঠিক মানটি লিখুন (${item.unit}):",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempValueText,
                        onValueChange = { tempValueText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = tempValueText.toEnglishDigits().toDoubleOrNull()
                        if (parsed != null && parsed > 0.0) {
                            viewModel.onEvent(OcrVerifyUiEvent.OnSaveItemValue(item.id, parsed))
                        } else {
                            Toast.makeText(context, "সঠিক সংখ্যা দিন", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(OcrVerifyUiEvent.OnDismissEditDialog) }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Add New Test Dialog
    if (uiState.showAddDialog) {
        var selectedKey by remember { mutableStateOf("fbs") }
        var inputValText by remember { mutableStateOf("100.0") }

        AlertDialog(
            onDismissRequest = { viewModel.onEvent(OcrVerifyUiEvent.OnDismissEditDialog) },
            title = { Text("নতুন টেস্টের মান যোগ করুন") },
            text = {
                Column {
                    Text("টেস্ট বাছাই করুন:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    val testOptions = listOf(
                        "fbs" to "Fasting Blood Sugar",
                        "rbs" to "Random Blood Sugar",
                        "creatinine" to "Serum Creatinine",
                        "hemoglobin" to "Hemoglobin",
                        "hba1c" to "HbA1c",
                        "cholesterol" to "Total Cholesterol"
                    )
                    testOptions.forEach { (k, name) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedKey = k }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedKey == k,
                                onClick = { selectedKey = k }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(name, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("রিপোর্টের মান:", style = MaterialTheme.typography.labelMedium)
                    OutlinedTextField(
                        value = inputValText,
                        onValueChange = { inputValText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = inputValText.toEnglishDigits().toDoubleOrNull()
                        if (parsed != null && parsed > 0.0) {
                            viewModel.onEvent(OcrVerifyUiEvent.OnAddNewItem(selectedKey, parsed))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("যোগ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(OcrVerifyUiEvent.OnDismissEditDialog) }) {
                    Text("বাতিল")
                }
            }
        )
    }
}
