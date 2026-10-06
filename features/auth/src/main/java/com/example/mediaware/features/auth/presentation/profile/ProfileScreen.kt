package com.example.mediaware.features.auth.presentation.profile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mediaware.core.designsystem.theme.*
import com.example.mediaware.core.designsystem.util.toBengaliDigits

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.saveSuccessMessage) {
        state.saveSuccessMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(ProfileUiEvent.OnClearMessage)
        }
    }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    val chronicList = listOf("ডায়াবেটিস", "উচ্চ রক্তচাপ", "হাঁপানি / শ্বাসকষ্ট", "কিডনি রোগ", "হৃদরোগ", "গ্যাস্ট্রিক / এসিডিটি")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ব্যক্তিগত স্বাস্থ্য প্রোফাইল", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (state.isEditing) {
                            viewModel.onEvent(ProfileUiEvent.OnSaveProfile)
                        } else {
                            viewModel.onEvent(ProfileUiEvent.OnToggleEditMode)
                        }
                    }) {
                        Text(
                            text = if (state.isEditing) "সংরক্ষণ" else "সম্পাদনা",
                            color = PrimaryTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // ─── Digital Health Card (Hero Banner) ──────────────────────────
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF004D40), PrimaryTeal, Color(0xFF00897B))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(38.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = state.fullName.ifBlank { "ব্যবহারকারী" },
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "মোবাইল: ${state.phoneNumber}",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Metric Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Blood Group Badge
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("রক্তের গ্রুপ", fontSize = 11.sp, color = Color(0xFF757575))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = state.bloodGroup.ifBlank { "—" },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CoralRed
                                    )
                                }
                            }

                            // Age Badge
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("বয়স", fontSize = 11.sp, color = Color(0xFF757575))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (state.age.isNotBlank()) "${state.age.toBengaliDigits()} বছর" else "—",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryTeal
                                    )
                                }
                            }

                            // Gender Badge
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("লিঙ্গ", fontSize = 11.sp, color = Color(0xFF757575))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (state.gender == "FEMALE") "মহিলা" else "পুরুষ",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OceanBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ─── Edit Mode Inputs ──────────────────────────────────────────
            if (state.isEditing) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BorderTealSoft),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "তথ্য হালনাগাদ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.fullName,
                            onValueChange = { viewModel.onEvent(ProfileUiEvent.OnNameChanged(it)) },
                            label = { Text("পূর্ণ নাম") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.age,
                            onValueChange = { viewModel.onEvent(ProfileUiEvent.OnAgeChanged(it)) },
                            label = { Text("বয়স (বছর)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "রক্তের গ্রুপ নির্বাচন করুন",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            bloodGroups.forEach { bg ->
                                FilterChip(
                                    selected = state.bloodGroup == bg,
                                    onClick = { viewModel.onEvent(ProfileUiEvent.OnBloodGroupChanged(bg)) },
                                    label = { Text(bg) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CoralRedContainer,
                                        selectedLabelColor = CoralRed
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // ─── Chronic Conditions Card ──────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFE0ECEC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonitorHeart,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "শারীরিক সমস্যা ও দীর্ঘস্থায়ী অবস্থা",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (state.isEditing) {
                        chronicList.forEach { cond ->
                            val isSelected = state.selectedConditions.contains(cond)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onEvent(ProfileUiEvent.OnToggleCondition(cond)) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { viewModel.onEvent(ProfileUiEvent.OnToggleCondition(cond)) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(cond, fontSize = 14.sp)
                            }
                        }
                    } else {
                        if (state.selectedConditions.isEmpty()) {
                            Text(
                                text = "কোনো দীর্ঘস্থায়ী সমস্যা নেই (সম্পূর্ণ সুস্থ)।",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.selectedConditions.forEach { cond ->
                                    Surface(
                                        color = EmeraldGreenContainer,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, BorderMintSoft)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = EmeraldGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = cond,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = EmeraldGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Emergency & Support Card ─────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBgTeal),
                border = BorderStroke(1.dp, BorderTealSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "প্রোফাইলের তথ্য ঔষধ নির্দেশিকা ও ডাক্তার ভিজিট কার্ড কাস্টমাইজ করতে ব্যবহৃত হয়।",
                        fontSize = 12.sp,
                        color = TextSecondaryGrey,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (state.isEditing) {
                Button(
                    onClick = { viewModel.onEvent(ProfileUiEvent.OnSaveProfile) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("সংরক্ষণ করুন", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
