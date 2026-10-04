package com.example.mediaware.features.settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mediaware.core.designsystem.util.toBengaliDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("সেটিংস ও ক্যাশ") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "ফিরে যান")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("স্মার্ট ক্যাশ", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ক্যাশ সাইজ: ${uiState.cacheSizeMB.toString().toBengaliDigits()} MB")
                    Text("সংরক্ষিত ঔষধের তথ্য: ${uiState.medicineCacheCount.toBengaliDigits()} টি")
                    Text("সংরক্ষিত টেস্টের তথ্য: ${uiState.testCacheCount.toBengaliDigits()} টি")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.onEvent(SettingsUiEvent.OnClearCacheClicked) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("ক্যাশ পরিষ্কার করুন")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("গোপনীয়তা", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("ফিঙ্গারপ্রিন্ট লগইন", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = uiState.isBiometricEnabled,
                    onCheckedChange = { viewModel.onEvent(SettingsUiEvent.OnToggleBiometric(it)) }
                )
            }
        }

        if (uiState.showClearCacheDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(SettingsUiEvent.OnDismissClearCacheDialog) },
                title = { Text("ক্যাশ পরিষ্কার করবেন?") },
                text = { Text("এতে সংরক্ষিত ঔষধ এবং টেস্টের তথ্য মুছে যাবে। কিন্তু আপনার নিজস্ব কোনো ডাটা মুছে যাবে না।") },
                confirmButton = {
                    TextButton(onClick = { viewModel.onEvent(SettingsUiEvent.OnConfirmClearCache) }) {
                        Text("নিশ্চিত করুন")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onEvent(SettingsUiEvent.OnDismissClearCacheDialog) }) {
                        Text("বাতিল")
                    }
                }
            )
        }
    }
}
