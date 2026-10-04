package com.example.mediaware.features.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mediaware.core.designsystem.theme.BackgroundLight
import com.example.mediaware.core.designsystem.theme.ClinicalCritical
import com.example.mediaware.core.designsystem.theme.OutlineVariantGrey
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.core.designsystem.theme.TextPrimaryDark
import com.example.mediaware.core.designsystem.theme.TextSecondaryGrey

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onResetPinClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(key1 = true) {
        viewModel.effect.collect { effect ->
            when (effect) {
                LoginSideEffect.NavigateToHome -> onNavigateToHome()
                LoginSideEffect.TriggerHapticFeedback -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
        }
    }

    Scaffold(
        containerColor = BackgroundLight
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Greeting & PIN dots
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Text(
                    text = if (state.userName.isNotBlank()) "স্বাগতম, ${state.userName}!" else "স্বাগতম!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "আপনার ৪ সংখ্যার গোপন পিন দিন",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        color = TextSecondaryGrey
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 4 PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < state.enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) PrimaryTeal else Color.Transparent)
                                .border(
                                    width = 2.dp,
                                    color = if (isFilled) PrimaryTeal else OutlineVariantGrey,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error or Cooldown banner
                if (state.errorMessageBn != null) {
                    Text(
                        text = state.errorMessageBn ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = ClinicalCritical,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Keypad (3x4 Grid)
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Row 1
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadButton(text = "১", digit = '1') { viewModel.onEvent(LoginUiEvent.OnKeypadClick('1')) }
                    KeypadButton(text = "২", digit = '2') { viewModel.onEvent(LoginUiEvent.OnKeypadClick('2')) }
                    KeypadButton(text = "৩", digit = '3') { viewModel.onEvent(LoginUiEvent.OnKeypadClick('3')) }
                }

                // Row 2
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadButton(text = "৪", digit = '4') { viewModel.onEvent(LoginUiEvent.OnKeypadClick('4')) }
                    KeypadButton(text = "৫", digit = '5') { viewModel.onEvent(LoginUiEvent.OnKeypadClick('5')) }
                    KeypadButton(text = "৬", digit = '6') { viewModel.onEvent(LoginUiEvent.OnKeypadClick('6')) }
                }

                // Row 3
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadButton(text = "৭", digit = '7') { viewModel.onEvent(LoginUiEvent.OnKeypadClick('7')) }
                    KeypadButton(text = "৮", digit = '8') { viewModel.onEvent(LoginUiEvent.OnKeypadClick('8')) }
                    KeypadButton(text = "৯", digit = '9') { viewModel.onEvent(LoginUiEvent.OnKeypadClick('9')) }
                }

                // Row 4: Biometric | 0 | Backspace
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Biometric Key
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .clickable(enabled = state.isBiometricAvailable) {
                                // Biometric prompt trigger
                                viewModel.onEvent(LoginUiEvent.OnBiometricAuthSuccess)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isBiometricAvailable) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "বায়োমেট্রিক আনলক",
                                tint = PrimaryTeal,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    // 0 Key
                    KeypadButton(text = "০", digit = '0') { viewModel.onEvent(LoginUiEvent.OnKeypadClick('0')) }

                    // Backspace Key
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.onEvent(LoginUiEvent.OnBackspaceClick) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "মুছুন",
                            tint = TextSecondaryGrey,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Bottom Help
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "পিন মনে নেই? সাহায্য নিন",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PrimaryTeal,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.clickable { onResetPinClick() }
                )
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    digit: Char,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(width = 1.dp, color = OutlineVariantGrey, shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        )
    }
}
