package com.example.mediaware.features.settings.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.core.designsystem.util.toBengaliDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit = {},
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("সেটিংস ও ক্যাশ", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ─── Patient Profile Summary Card ─────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryTeal.copy(alpha = 0.08f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToProfile() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(PrimaryTeal.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.userName.ifBlank { "ব্যবহারকারীর নাম" },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (uiState.userPhone.isNotBlank()) "মোবাইল: ${uiState.userPhone}" else "প্রোফাইল সেটআপ করুন",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.bloodGroup.isNotBlank()) {
                            Text(
                                text = "রক্তের গ্রুপ: ${uiState.bloodGroup} | বয়স: ${uiState.userAge.toString().toBengaliDigits()} বছর",
                                fontSize = 12.sp,
                                color = PrimaryTeal,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "প্রোফাইল দেখুন",
                        tint = PrimaryTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── Section 1: নিরাপত্তা ও অ্যাক্সেস ─────────────────────────────
            SettingsSectionHeader(title = "নিরাপত্তা ও অ্যাক্সেস")

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = PrimaryTeal)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("বায়োমেট্রিক ফিঙ্গারপ্রিন্ট আনলক", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text("আঙুলের ছাপ দিয়ে দ্রুত অ্যাপ খুলুন", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = uiState.isBiometricEnabled,
                            onCheckedChange = { viewModel.onEvent(SettingsUiEvent.OnToggleBiometric(it)) }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryTeal)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("পিন নিরাপত্তা কোড", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("৫-সংখ্যার পাসওয়ার্ড দিয়ে এনক্রিপ্টেড", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── Section 2: ওষুধ রিমাইন্ডার ও নোটিফিকেশন ────────────────────
            SettingsSectionHeader(title = "ওষুধ অ্যালার্ম ও সতর্কতা")

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = null, tint = PrimaryTeal)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("লক স্ক্রিন ফুল-স্ক্রিন অ্যালার্ম", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("স্ক্রিন লক থাকলেও সঠিক সময়ে পূর্ণ পর্দা জুড়ে অ্যালার্ম বাজবে", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = PrimaryTeal)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("উচ্চ অগ্রাধিকার সাউন্ড ও ভাইব্রেশন", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("সাইলেন্ট মোড বাইপাস সক্রিয়", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── Section 3: স্মার্ট ক্যাশ ও স্টোরেজ ────────────────────────
            SettingsSectionHeader(title = "স্মার্ট ক্যাশ ও অফলাইন মেমরি")

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ক্যাশ মেমরি সাইজ:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${uiState.cacheSizeMB.toString().toBengaliDigits()} MB", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("সংরক্ষিত ঔষধের তথ্য:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${uiState.medicineCacheCount.toString().toBengaliDigits()} টি", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("সংরক্ষিত টেস্টের ব্যাখ্যা:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${uiState.testCacheCount.toString().toBengaliDigits()} টি", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = { viewModel.onEvent(SettingsUiEvent.OnClearCacheClicked) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ক্যাশ মেমরি পরিষ্কার করুন", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── Section 4: জরুরি সহায়তা ও অ্যাপ তথ্য ─────────────────────
            SettingsSectionHeader(title = "জরুরি সহায়তা ও অ্যাপ পরিচিতি")

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("জাতীয় জরুরি সেবা (৯৯৯)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                        Text("২৪ ঘণ্টা অ্যাম্বুলেন্স ও পুলিশ সহায়তা", fontSize = 12.sp, color = Color(0xFFC62828).copy(alpha = 0.8f))
                    }
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:999"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("কল দিন", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "MediAware — আপনার স্বাস্থ্য সহায়ক (v১.০)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (uiState.showClearCacheDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(SettingsUiEvent.OnDismissClearCacheDialog) },
                title = { Text("ক্যাশ পরিষ্কার করবেন?") },
                text = { Text("এতে সংরক্ষিত ঔষধ এবং টেস্টের তথ্য মুছে যাবে। কিন্তু আপনার নিজস্ব প্রোফাইল বা প্রেসক্রিপশনের কোনো ডাটা মুছে যাবে না।") },
                confirmButton = {
                    TextButton(onClick = { viewModel.onEvent(SettingsUiEvent.OnConfirmClearCache) }) {
                        Text("নিশ্চিত করুন", color = MaterialTheme.colorScheme.error)
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

@Composable
private fun SettingsSectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryTeal
        )
    }
}
