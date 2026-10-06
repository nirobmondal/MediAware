package com.example.mediaware.features.auth.presentation.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Transgender
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mediaware.core.designsystem.theme.BackgroundLight
import com.example.mediaware.core.designsystem.theme.ClinicalCritical
import com.example.mediaware.core.designsystem.theme.OutlineVariantGrey
import com.example.mediaware.core.designsystem.theme.PrimaryContainerTeal
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.core.designsystem.theme.TextPrimaryDark
import com.example.mediaware.core.designsystem.theme.TextSecondaryGrey
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.core.model.Gender

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
        "ডায়াবেটিস", "উচ্চ রক্তচাপ", "হৃদরোগ", "কিডনি রোগ",
        "অ্যাজমা / শ্বাসকষ্ট", "থাইরয়েড", "গ্যাস্ট্রিক / এসিডিটি",
        "কোনোটিই নয় / সুস্থ"
    )

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "স্বাস্থ্য প্রোফাইল তৈরি",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                        Text(
                            text = "ধাপ ${state.currentStep.toString().toBengaliDigits()} / ${state.totalSteps.toString().toBengaliDigits()}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PrimaryTeal,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                },
                navigationIcon = {
                    if (state.currentStep > 1) {
                        IconButton(onClick = { viewModel.onEvent(ProfileSetupUiEvent.OnPreviousStepClicked) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "পূর্ববর্তী ধাপ",
                                tint = PrimaryTeal
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Progress Bar ─────────────────────────────────────────────
            LinearProgressIndicator(
                progress = { state.currentStep.toFloat() / state.totalSteps.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = PrimaryTeal,
                trackColor = PrimaryContainerTeal.copy(alpha = 0.5f)
            )

            // ── Content Area with Step Transitions ───────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                AnimatedContent(
                    targetState = state.currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "profile_step_animation"
                ) { targetStep ->
                    when (targetStep) {
                        1 -> StepOneNameAndAge(state = state, viewModel = viewModel)
                        2 -> StepTwoGenderAndBlood(state = state, viewModel = viewModel, bloodGroups = bloodGroups)
                        3 -> StepThreeDiseaseHistory(
                            state = state,
                            viewModel = viewModel,
                            conditionsList = chronicConditionsList
                        )
                    }
                }

                // ── Validation Error Banner ──────────────────────────────
                if (state.validationErrorBn != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = ClinicalCritical,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.validationErrorBn ?: "",
                                color = ClinicalCritical,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Bottom Action Controls ───────────────────────────────────
            Card(
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (state.currentStep > 1) {
                        OutlinedButton(
                            onClick = { viewModel.onEvent(ProfileSetupUiEvent.OnPreviousStepClicked) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "পূর্ববর্তী",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.onEvent(ProfileSetupUiEvent.OnNextStepClicked) },
                        enabled = !state.isSaving,
                        modifier = Modifier
                            .weight(if (state.currentStep > 1) 1.5f else 1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryTeal,
                            contentColor = Color.White
                        )
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (state.currentStep == 3) "প্রোফাইল সম্পন্ন করুন" else "পরবর্তী ধাপ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (state.currentStep == 3) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 1: নাম ও বয়স (Name & Age)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StepOneNameAndAge(
    state: ProfileSetupUiState,
    viewModel: ProfileSetupViewModel
) {
    Column {
        Text(
            text = "আপনার প্রাথমিক পরিচিতি",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "ল্যাব টেস্টের স্বাভাবিক রেফারেন্স মান ও সঠিক ঔষধ মাত্রা নির্ধারণে আপনার নাম এবং বয়স প্রয়োজন।",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                color = TextSecondaryGrey,
                lineHeight = 18.sp
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Full Name Field
        Text(
            text = "আপনার পূর্ণ নাম",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark
            )
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = state.fullName,
            onValueChange = { viewModel.onEvent(ProfileSetupUiEvent.OnNameChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("যেমন: মো: আব্দুর রহিম", color = Color(0xFFA0AAB0)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = PrimaryTeal,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryTeal,
                unfocusedBorderColor = OutlineVariantGrey
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Age Field
        Text(
            text = "আপনার বয়স (বছর)",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark
            )
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = state.ageString,
            onValueChange = { viewModel.onEvent(ProfileSetupUiEvent.OnAgeChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("যেমন: ৩৫", color = Color(0xFFA0AAB0)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = PrimaryTeal,
                    modifier = Modifier.size(20.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryTeal,
                unfocusedBorderColor = OutlineVariantGrey
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Explanatory Tip Box
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryContainerTeal.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PrimaryTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "কেন বয়স জানা প্রয়োজন? বয়স অনুযায়ী রক্তচাপ, সুগার লেভেল এবং কিডনি ফাংশনের স্বাভাবিক মান ভিন্ন হতে পারে।",
                    fontSize = 12.sp,
                    color = TextPrimaryDark,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 2: লিঙ্গ ও রক্তের গ্রুপ (Gender & Blood Group)
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepTwoGenderAndBlood(
    state: ProfileSetupUiState,
    viewModel: ProfileSetupViewModel,
    bloodGroups: List<String>
) {
    Column {
        Text(
            text = "লিঙ্গ ও রক্তের গ্রুপ",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "পুরুষ ও মহিলাদের শরীরে হিমোগ্লোবিন এবং অন্যান্য ক্লিনিক্যাল মার্কারের পরিমাপ আলাদা হয়।",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                color = TextSecondaryGrey,
                lineHeight = 18.sp
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Gender Selection (Cards)
        Text(
            text = "লিঙ্গ নির্বাচন করুন",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Gender.entries.forEach { gender ->
                val isSelected = state.selectedGender == gender
                val (label, icon) = when (gender) {
                    Gender.MALE -> "পুরুষ" to Icons.Default.Male
                    Gender.FEMALE -> "মহিলা" to Icons.Default.Female
                    Gender.OTHER -> "অন্যান্য" to Icons.Default.Transgender
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) PrimaryContainerTeal else Color.White
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) PrimaryTeal else OutlineVariantGrey
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.onEvent(ProfileSetupUiEvent.OnGenderSelected(gender)) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) PrimaryTeal else TextSecondaryGrey,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PrimaryTeal else TextPrimaryDark
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Blood Group Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "রক্তের গ্রুপ (ঐচ্ছিক)",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryDark
                )
            )
            if (state.selectedBloodGroup != null) {
                Text(
                    text = "নির্বাচিত: ${state.selectedBloodGroup}",
                    fontSize = 12.sp,
                    color = PrimaryTeal,
                    fontWeight = FontWeight.Bold
                )
            }
        }

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
                    label = {
                        Text(
                            text = bg,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryTeal,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = TextPrimaryDark
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 3: পূর্ববর্তী স্বাস্থ্য ও রোগের ইতিহাস (Previous Disease History)
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StepThreeDiseaseHistory(
    state: ProfileSetupUiState,
    viewModel: ProfileSetupViewModel,
    conditionsList: List<String>
) {
    Column {
        Text(
            text = "পূর্ববর্তী স্বাস্থ্য ও রোগের ইতিহাস",
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "আপনার পূর্বের কোনো রোগ থাকলে তা চিহ্নিত করুন। এর মাধ্যমে ক্ষতিকর ঔষধের ইন্টারঅ্যাকশন ও খাবারের আগাম সতর্কতা দেওয়া সম্ভব হবে।",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                color = TextSecondaryGrey,
                lineHeight = 18.sp
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Chronic Condition Chips
        Text(
            text = "পরিচিত রোগ বা সমস্যা নির্বাচন করুন",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            conditionsList.forEach { condition ->
                val isSelected = state.selectedChronicConditions.contains(condition) ||
                        (condition == "কোনোটিই নয় / সুস্থ" && state.selectedChronicConditions.contains("নেই"))

                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onEvent(ProfileSetupUiEvent.OnToggleChronicCondition(condition)) },
                    label = {
                        Text(
                            text = condition,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryTeal,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = TextPrimaryDark
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Other condition input
        Text(
            text = "অন্যান্য রোগ বা বিশেষ অ্যালার্জি (যদি থাকে)",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark
            )
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = state.otherCondition,
            onValueChange = { viewModel.onEvent(ProfileSetupUiEvent.OnOtherConditionChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("যেমন: পেনিসিলিন এলার্জি, মাইগ্রেন", color = Color(0xFFA0AAB0)) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryTeal,
                unfocusedBorderColor = OutlineVariantGrey
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Security assurance pill
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryTeal.copy(alpha = 0.06f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = PrimaryTeal,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "আপনার ব্যক্তিগত স্বাস্থ্য তথ্য সম্পূর্ণ নিরাপদ এবং কোনো বাণিজ্যিক উদ্দেশ্যে ব্যবহৃত হয় না।",
                    fontSize = 12.sp,
                    color = PrimaryTeal,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
