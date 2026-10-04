package com.example.mediaware.features.caregiver.presentation.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.features.caregiver.domain.model.CaregiverLink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverManageScreen(
    viewModel: CaregiverManageViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CaregiverManageSideEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    if (uiState.showRevokeDialog && uiState.selectedCaregiver != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(CaregiverManageUiEvent.ShowRevokeConfirmation(false)) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFBA1A1A)
                )
            },
            title = { Text("সংযোগ বিচ্ছিন্ন করবেন?") },
            text = {
                Text("আপনি কি নিশ্চিত যে '${uiState.selectedCaregiver?.caregiverName}'-এর সাথে আপনার স্বাস্থ্য পর্যবেক্ষণ সংযোগ বিচ্ছিন্ন করতে চান? এরপর তিনি আর আপনার কোনো তথ্য দেখতে পারবেন না।")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onEvent(CaregiverManageUiEvent.ConfirmRevokeCaregiver) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A))
                ) {
                    Text("হ্যাঁ, বিচ্ছিন্ন করুন")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.onEvent(CaregiverManageUiEvent.ShowRevokeConfirmation(false)) }
                ) {
                    Text("বাতিল")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("অনুমতি ব্যবস্থাপনা") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAdd) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "নতুন কেয়ারগিভার যুক্ত করুন"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Caregiver Selector Bar
            if (uiState.caregivers.isNotEmpty()) {
                Text(
                    text = "সংযুক্ত কেয়ারগিভার নির্বাচন করুন:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.caregivers) { cg ->
                        val isSelected = cg.linkId == uiState.selectedCaregiver?.linkId
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onEvent(CaregiverManageUiEvent.SelectCaregiver(cg)) },
                            label = {
                                Text(
                                    text = "${cg.caregiverName} (${cg.relationship.labelBn})"
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.AccountCircle,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "রোগীর সম্পূর্ণ স্বাধীনতা রয়েছে কেয়ারগিভার কোন কোন তথ্য দেখতে পাবেন তা নির্ধারণ করার। অনুমতি বিটমাস্ক দ্বারা নিরাপত্তা নিশ্চিত করা হয়।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Permission Bitmask Checkboxes Section
            Text(
                text = "প্রবেশাধিকার ও ডেটা অনুমতিসমূহ",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Doses Permission (Bit 1)
            PermissionOptionCard(
                title = "ওষুধ গ্রহণ ও শিডিউল ট্র্যাকিং",
                subtitle = "রোগী সময়মতো ওষুধ খাচ্ছেন কিনা এবং আজকের ডোজের অবস্থা দেখতে পারবে।",
                bitLabel = "বিট ১ (মান ২)",
                icon = Icons.Default.Medication,
                isChecked = uiState.permDoses,
                onCheckedChange = { viewModel.onEvent(CaregiverManageUiEvent.TogglePermDoses(it)) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Reports Permission (Bit 0)
            PermissionOptionCard(
                title = "ল্যাব রিপোর্ট ও স্বাস্থ্য স্মৃতি চার্ট",
                subtitle = "রক্তে সুগার, রক্তচাপ ও অতীতের টেস্ট ট্রেন্ডের রিপোর্ট দেখতে পারবে।",
                bitLabel = "বিট ০ (মান ১)",
                icon = Icons.Default.Analytics,
                isChecked = uiState.permReports,
                onCheckedChange = { viewModel.onEvent(CaregiverManageUiEvent.TogglePermReports(it)) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Doctor Visits Permission (Bit 2)
            PermissionOptionCard(
                title = "ডাক্তারের পরামর্শ ও জরুরি সতর্কতা",
                subtitle = "ডাক্তারের ভিজিট সারাংশ, আগামী তারিখ এবং জরুরি রেড-ফ্ল্যাগ অ্যালার্ট পাবে।",
                bitLabel = "বিট ২ (মান ৪)",
                icon = Icons.Default.LocalHospital,
                isChecked = uiState.permDoctorVisits,
                onCheckedChange = { viewModel.onEvent(CaregiverManageUiEvent.TogglePermDoctorVisits(it)) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Bitmask State Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "সক্রিয় অনুমতি বিটমাস্ক:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "${uiState.bitmaskBinaryBn} (মান: ${uiState.bitmaskValue.toBengaliDigits()})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    val badgeText = when (uiState.bitmaskValue) {
                        7 -> "পূর্ণ অনুমতি"
                        0 -> "সম্পূর্ণ বন্ধ"
                        else -> "আংশিক অনুমতি"
                    }
                    val badgeColor = when (uiState.bitmaskValue) {
                        7 -> MaterialTheme.colorScheme.primary
                        0 -> Color(0xFFBA1A1A)
                        else -> Color(0xFF0288D1)
                    }

                    Surface(
                        color = badgeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = badgeText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = badgeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = { viewModel.onEvent(CaregiverManageUiEvent.SavePermissions) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("অনুমতি সংরক্ষণ করুন", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(12.dp))

            FilledTonalButton(
                onClick = onNavigateToProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Visibility, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("সংযুক্ত প্রোফাইল ড্যাশবোর্ড দেখুন")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.onEvent(CaregiverManageUiEvent.ShowRevokeConfirmation(true)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFBA1A1A))
            ) {
                Icon(imageVector = Icons.Default.LinkOff, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("কেয়ারগিভার সংযোগ বিচ্ছিন্ন করুন")
            }
        }
    }
}

@Composable
private fun PermissionOptionCard(
    title: String,
    subtitle: String,
    bitLabel: String,
    icon: ImageVector,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) },
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isChecked) 2.dp else 0.dp),
        shape = RoundedCornerShape(14.dp),
        border = if (isChecked) {
            null
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isChecked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = bitLabel,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
