package com.example.mediaware.features.auth.presentation.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mediaware.core.designsystem.theme.BackgroundLight
import com.example.mediaware.core.designsystem.theme.ClinicalCritical
import com.example.mediaware.core.designsystem.theme.OutlineGrey
import com.example.mediaware.core.designsystem.theme.PrimaryContainerTeal
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.core.designsystem.theme.TextPrimaryDark
import com.example.mediaware.core.designsystem.theme.TextSecondaryGrey
import com.example.mediaware.core.model.Gender

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(
    viewModel: ProfileSetupViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = true) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfileSetupSideEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    val bloodGroups = listOf("A+", "B+", "AB+", "O+", "A-", "B-", "AB-", "O-")
    val chronicConditionsList = listOf(
        "ডায়াবেটিস", "উচ্চ রক্তচাপ", "কিডনি রোগ",
        "হৃদরোগ", "অ্যাজমা", "নেই"
    )

    Scaffold(
        containerColor = BackgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "👤 আপনার স্বাস্থ্য পরিচিতি",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "ল্যাব টেস্টের সঠিক মান ও স্বাভাবিক মাত্রা জানতে এই তথ্যগুলো প্রয়োজন।",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    color = TextSecondaryGrey
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Full Name Field
            Text(
                text = "পূর্ণ নাম:",
                style = MaterialTheme.typography.labelLarge.copy(color = TextPrimaryDark)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = state.fullName,
                onValueChange = { viewModel.onEvent(ProfileSetupUiEvent.OnNameChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("করিম মিয়া", color = Color(0xFF889393)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryTeal,
                    unfocusedBorderColor = OutlineGrey
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Age Field
            Text(
                text = "বয়স (বছর):",
                style = MaterialTheme.typography.labelLarge.copy(color = TextPrimaryDark)
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = state.ageString,
                onValueChange = { viewModel.onEvent(ProfileSetupUiEvent.OnAgeChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("৫০", color = Color(0xFF889393)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryTeal,
                    unfocusedBorderColor = OutlineGrey
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Gender Selector
            Text(
                text = "লিঙ্গ:",
                style = MaterialTheme.typography.labelLarge.copy(color = TextPrimaryDark)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Gender.entries.forEach { gender ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            viewModel.onEvent(ProfileSetupUiEvent.OnGenderSelected(gender))
                        }
                    ) {
                        RadioButton(
                            selected = state.selectedGender == gender,
                            onClick = { viewModel.onEvent(ProfileSetupUiEvent.OnGenderSelected(gender)) },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryTeal)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = gender.displayNameBn,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimaryDark)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Blood Group Chips
            Text(
                text = "রক্তের গ্রুপ (ঐচ্ছিক):",
                style = MaterialTheme.typography.labelLarge.copy(color = TextPrimaryDark)
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                bloodGroups.forEach { bg ->
                    val isSelected = state.selectedBloodGroup == bg
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onEvent(ProfileSetupUiEvent.OnBloodGroupSelected(bg)) },
                        label = { Text(bg) },
                        shape = RoundedCornerShape(24.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryContainerTeal,
                            selectedLabelColor = PrimaryTeal
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Known Chronic Conditions
            Text(
                text = "পরিচিত স্বাস্থ্য সমস্যা (যদি থাকে):",
                style = MaterialTheme.typography.labelLarge.copy(color = TextPrimaryDark)
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chronicConditionsList.forEach { condition ->
                    val isSelected = state.selectedChronicConditions.contains(condition)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onEvent(ProfileSetupUiEvent.OnToggleChronicCondition(condition)) },
                        label = {
                            Text(if (isSelected) "✓ $condition" else condition)
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryContainerTeal,
                            selectedLabelColor = PrimaryTeal
                        )
                    )
                }
            }

            // Validation Error Banner
            if (state.validationErrorBn != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.validationErrorBn ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ClinicalCritical,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Save CTA Button
            Button(
                onClick = { viewModel.onEvent(ProfileSetupUiEvent.SaveProfile) },
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryTeal,
                    contentColor = Color.White
                )
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "সম্পন্ন করুন ও শুরু করুন",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
