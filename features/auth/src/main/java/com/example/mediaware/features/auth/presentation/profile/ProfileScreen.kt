package com.example.mediaware.features.auth.presentation.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.core.designsystem.util.toBengaliDigits

@OptIn(ExperimentalMaterial3Api::class)
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
                title = { Text("ব্যক্তিগত প্রোফাইল", fontWeight = FontWeight.Bold) },
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
                            fontSize = 16.sp
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Avatar Banner
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(PrimaryTeal.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = PrimaryTeal,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = state.fullName.ifBlank { "আপনার নাম" },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "মোবাইল: ${state.phoneNumber}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Clinical Profile Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ক্লিনিক্যাল তথ্য",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTeal
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (state.isEditing) {
                        OutlinedTextField(
                            value = state.fullName,
                            onValueChange = { viewModel.onEvent(ProfileUiEvent.OnNameChanged(it)) },
                            label = { Text("পূর্ণ নাম") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = state.age,
                            onValueChange = { viewModel.onEvent(ProfileUiEvent.OnAgeChanged(it)) },
                            label = { Text("বয়স (বছর)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("বয়স:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${state.age.toBengaliDigits()} বছর", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("লিঙ্গ:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (state.gender == "FEMALE") "মহিলা" else "পুরুষ", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("রক্তের গ্রুপ:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(state.bloodGroup, fontWeight = FontWeight.Bold, color = PrimaryTeal)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Blood Group Selector in Edit Mode
            if (state.isEditing) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "রক্তের গ্রুপ নির্বাচন করুন",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            bloodGroups.take(4).forEach { bg ->
                                FilterChip(
                                    selected = state.bloodGroup == bg,
                                    onClick = { viewModel.onEvent(ProfileUiEvent.OnBloodGroupChanged(bg)) },
                                    label = { Text(bg) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            bloodGroups.takeLast(4).forEach { bg ->
                                FilterChip(
                                    selected = state.bloodGroup == bg,
                                    onClick = { viewModel.onEvent(ProfileUiEvent.OnBloodGroupChanged(bg)) },
                                    label = { Text(bg) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // Chronic Conditions Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "দীর্ঘস্থায়ী শারীরিক সমস্যা / রোগ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTeal
                    )

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
                                text = "কোনো দীর্ঘস্থায়ী রোগ লিপিবদ্ধ নেই।",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            state.selectedConditions.forEach { cond ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = PrimaryTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(cond, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (state.isEditing) {
                Button(
                    onClick = { viewModel.onEvent(ProfileUiEvent.OnSaveProfile) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("সংরক্ষণ করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
