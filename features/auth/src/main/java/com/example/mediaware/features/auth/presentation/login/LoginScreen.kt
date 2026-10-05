package com.example.mediaware.features.auth.presentation.login

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mediaware.core.designsystem.theme.BackgroundLight
import com.example.mediaware.core.designsystem.theme.ClinicalCritical
import com.example.mediaware.core.designsystem.theme.OutlineVariantGrey
import com.example.mediaware.core.designsystem.theme.PrimaryContainerTeal
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.core.designsystem.theme.TextPrimaryDark
import com.example.mediaware.core.designsystem.theme.TextSecondaryGrey

import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.TextButton

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val launchBiometricPrompt = {
        val activity = context as? FragmentActivity
        if (activity != null) {
            val executor = ContextCompat.getMainExecutor(context)
            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        viewModel.onEvent(LoginUiEvent.OnBiometricAuthSuccess)
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            viewModel.onEvent(LoginUiEvent.OnBiometricAuthError(errString.toString()))
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("বায়োমেট্রিক দিয়ে আনলক করুন")
                .setSubtitle("আপনার আঙুলের ছাপ স্ক্যান করুন")
                .setNegativeButtonText("পিন ব্যবহার করুন")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }

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

    // Auto-prompt biometric on launch if enabled and no lockout
    LaunchedEffect(state.isBiometricAvailable) {
        if (state.isBiometricAvailable && state.lockRemainingSeconds == 0) {
            launchBiometricPrompt()
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
            // ── Top Section: Branding, Greeting & 4 PIN Dots ──────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 36.dp)
            ) {
                // Subtle glowing logo emblem
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(PrimaryContainerTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = "MediAware",
                        tint = PrimaryTeal,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (state.userName.isNotBlank()) "স্বাগতম, ${state.userName}!" else "স্বাগতম!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "আপনার ৪ সংখ্যার গোপন পিন দিন",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = TextSecondaryGrey
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // ── 4 PIN Indicator Dots ──────────────────────────────────
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < state.enteredPin.length
                        val dotSize by animateDpAsState(
                            targetValue = if (isFilled) 18.dp else 16.dp,
                            animationSpec = tween(durationMillis = 150),
                            label = "dot_size"
                        )
                        val dotColor by animateColorAsState(
                            targetValue = if (isFilled) PrimaryTeal else Color.Transparent,
                            animationSpec = tween(durationMillis = 150),
                            label = "dot_color"
                        )
                        val borderColor by animateColorAsState(
                            targetValue = if (isFilled) PrimaryTeal else OutlineVariantGrey,
                            animationSpec = tween(durationMillis = 150),
                            label = "border_color"
                        )

                        Box(
                            modifier = Modifier
                                .size(dotSize)
                                .clip(CircleShape)
                                .background(dotColor)
                                .border(
                                    width = 2.dp,
                                    color = borderColor,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Error or Lockout Message Banner ───────────────────────
                if (state.errorMessageBn != null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = state.errorMessageBn ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = ClinicalCritical,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // ── Middle / Bottom: Tactile Keypad (3x4 Grid) ─────────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                // Row 1
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadButton(text = "১") { viewModel.onEvent(LoginUiEvent.OnKeypadClick('1')) }
                    KeypadButton(text = "২") { viewModel.onEvent(LoginUiEvent.OnKeypadClick('2')) }
                    KeypadButton(text = "৩") { viewModel.onEvent(LoginUiEvent.OnKeypadClick('3')) }
                }

                // Row 2
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadButton(text = "৪") { viewModel.onEvent(LoginUiEvent.OnKeypadClick('4')) }
                    KeypadButton(text = "৫") { viewModel.onEvent(LoginUiEvent.OnKeypadClick('5')) }
                    KeypadButton(text = "৬") { viewModel.onEvent(LoginUiEvent.OnKeypadClick('6')) }
                }

                // Row 3
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    KeypadButton(text = "৭") { viewModel.onEvent(LoginUiEvent.OnKeypadClick('7')) }
                    KeypadButton(text = "৮") { viewModel.onEvent(LoginUiEvent.OnKeypadClick('8')) }
                    KeypadButton(text = "৯") { viewModel.onEvent(LoginUiEvent.OnKeypadClick('9')) }
                }

                // Row 4: Biometric | 0 | Backspace
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Biometric Sensor Button (only active if enabled in settings)
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .then(
                                if (state.isBiometricAvailable) {
                                    Modifier
                                        .background(PrimaryContainerTeal)
                                        .clickable { launchBiometricPrompt() }
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isBiometricAvailable) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "বায়োমেট্রিক ফিঙ্গারপ্রিন্ট আনলক",
                                tint = PrimaryTeal,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    // 0 Key
                    KeypadButton(text = "০") { viewModel.onEvent(LoginUiEvent.OnKeypadClick('0')) }

                    // Backspace Key
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(width = 1.dp, color = OutlineVariantGrey.copy(alpha = 0.6f), shape = CircleShape)
                            .clickable { viewModel.onEvent(LoginUiEvent.OnBackspaceClick) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "মুছুন",
                            tint = TextSecondaryGrey,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Security Assurance Pill (No Forgot PIN / No Reset PIN)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PrimaryTeal.copy(alpha = 0.06f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "নিরাপদ অফলাইন আনলক",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryTeal
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "অ্যাকাউন্ট নেই? নতুন একাউন্ট তৈরি করুন",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryTeal
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(
                width = 1.dp,
                color = OutlineVariantGrey.copy(alpha = 0.7f),
                shape = CircleShape
            )
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
