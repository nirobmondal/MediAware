package com.example.mediaware.features.settings.presentation

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mediaware.core.designsystem.component.MediAwareBottomNavBar
import com.example.mediaware.core.designsystem.component.MediAwareNavTab
import com.example.mediaware.core.designsystem.theme.*
import com.example.mediaware.core.designsystem.util.toBengaliDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit = {},
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = onNavigateBack,
    onNavigateToSymptomSelect: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Local toggles for display preferences
    var isLargeTextEnabled by remember { mutableStateOf(false) }
    var isSoundAlertsEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = true) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SettingsSideEffect.ShowToast -> {
                    Toast.makeText(context, effect.messageBn, Toast.LENGTH_SHORT).show()
                }
                SettingsSideEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    val promptBiometricEnrollment = {
        val activity = context as? FragmentActivity
        if (activity != null) {
            val biometricManager = BiometricManager.from(context)
            val canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            )

            if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
                Toast.makeText(
                    context,
                    "আপনার ফোনে কোনো ফিঙ্গারপ্রিন্ট যোগ করা নেই। ফোন সেটিংস থেকে ফিঙ্গারপ্রিন্ট সেট করুন।",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val executor = ContextCompat.getMainExecutor(context)
                val biometricPrompt = BiometricPrompt(
                    activity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            viewModel.onEvent(SettingsUiEvent.OnToggleBiometric(true))
                            Toast.makeText(context, "বায়োমেট্রিক ফিঙ্গারপ্রিন্ট সক্রিয় হয়েছে", Toast.LENGTH_SHORT).show()
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                                Toast.makeText(context, "ফিঙ্গারপ্রিন্ট যাচাই ব্যর্থ: $errString", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("ফিঙ্গারপ্রিন্ট সক্রিয়করণ")
                    .setSubtitle("লগইনের জন্য আঙুলের ছাপ যাচাই করুন")
                    .setNegativeButtonText("বাতিল")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }
        } else {
            viewModel.onEvent(SettingsUiEvent.OnToggleBiometric(true))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("সেটিংস ও পছন্দসমূহ", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            MediAwareBottomNavBar(
                selectedTab = MediAwareNavTab.SETTINGS,
                onTabSelected = { tab ->
                    when (tab) {
                        MediAwareNavTab.HOME -> onNavigateToHome()
                        MediAwareNavTab.SYMPTOMS -> onNavigateToSymptomSelect()
                        MediAwareNavTab.SETTINGS -> { /* Already on Settings */ }
                    }
                }
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
            Spacer(modifier = Modifier.height(10.dp))

            // ─── Top Hero: Patient Profile Card ───────────────────────────
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.2.dp, BorderTealSoft),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PrimaryTeal, EmeraldGreen)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.userName.ifBlank { "স্বাস্থ্য প্রোফাইল" },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (uiState.userPhone.isNotBlank()) "মোবাইল: ${uiState.userPhone}" else "প্রোফাইল তথ্য যোগ করুন",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.bloodGroup.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = CoralRedContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = uiState.bloodGroup,
                                        fontSize = 11.sp,
                                        color = CoralRed,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    color = EmeraldGreenContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${uiState.userAge.toString().toBengaliDigits()} বছর",
                                        fontSize = 11.sp,
                                        color = PrimaryTeal,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "প্রোফাইল দেখুন",
                        tint = PrimaryTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ─── Category 1: অ্যাপ পছন্দ ও প্রদর্শন ──────────────────────
            CategoryHeader(title = "অ্যাপ পছন্দ ও প্রদর্শন")
            CategoryCard {
                // Language
                SettingsRow(
                    icon = Icons.Default.Language,
                    iconTint = OceanBlue,
                    title = "ভাষা",
                    subtitle = "বাংলা (ডিফল্ট)",
                    trailing = {
                        Surface(
                            color = OceanBlueContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "বাংলা",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                )

                HorizontalDivider(color = Color(0xFFF0F4F4), modifier = Modifier.padding(vertical = 4.dp))

                // Sound & Audio Alerts
                SettingsRow(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    iconTint = PrimaryTeal,
                    title = "অডিও গাইডেন্স ও সাউন্ড",
                    subtitle = "ওষুধ ও প্রেসক্রিপশন পড়ার সময় অডিও সহায়তা",
                    trailing = {
                        Switch(
                            checked = isSoundAlertsEnabled,
                            onCheckedChange = { isSoundAlertsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryTeal)
                        )
                    }
                )

                HorizontalDivider(color = Color(0xFFF0F4F4), modifier = Modifier.padding(vertical = 4.dp))

                // Text Size / Readability
                SettingsRow(
                    icon = Icons.Default.FormatSize,
                    iconTint = AiPurple,
                    title = "সহজ পাঠযোগ্য বড় টেক্সট",
                    subtitle = "প্রেসক্রিপশন ও রিপোর্ট সহজে দেখার জন্য",
                    trailing = {
                        Switch(
                            checked = isLargeTextEnabled,
                            onCheckedChange = { isLargeTextEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryTeal)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ─── Category 2: নিরাপত্তা ও বায়োমেট্রিক ───────────────────────
            CategoryHeader(title = "নিরাপত্তা ও অ্যাক্সেস")
            CategoryCard {
                // Biometric Unlock
                SettingsRow(
                    icon = Icons.Default.Fingerprint,
                    iconTint = PrimaryTeal,
                    title = "বায়োমেট্রিক ফিঙ্গারপ্রিন্ট",
                    subtitle = "আঙুলের ছাপ দিয়ে দ্রুত অ্যাপ আনলক করুন",
                    trailing = {
                        Switch(
                            checked = uiState.isBiometricEnabled,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    promptBiometricEnrollment()
                                } else {
                                    viewModel.onEvent(SettingsUiEvent.OnToggleBiometric(false))
                                    Toast.makeText(context, "বায়োমেট্রিক ফিঙ্গারপ্রিন্ট নিষ্ক্রিয় করা হয়েছে", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PrimaryTeal)
                        )
                    }
                )

                HorizontalDivider(color = Color(0xFFF0F4F4), modifier = Modifier.padding(vertical = 4.dp))

                // 4-Digit PIN Security
                SettingsRow(
                    icon = Icons.Default.Lock,
                    iconTint = PrimaryTeal,
                    title = "৪-সংখ্যার পিন নিরাপত্তা",
                    subtitle = "অফলাইন ব্যাংক-গ্রেড PBKDF2 এনক্রিপ্টেড",
                    trailing = {
                        Surface(
                            color = EmeraldGreenContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "সুরক্ষিত",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                )

                HorizontalDivider(color = Color(0xFFF0F4F4), modifier = Modifier.padding(vertical = 4.dp))

                // Device Privacy
                SettingsRow(
                    icon = Icons.Default.Shield,
                    iconTint = PrimaryTeal,
                    title = "ডিভাইস ডাটা প্রাইভেসি",
                    subtitle = "আপনার স্বাস্থ্য তথ্য শুধুমাত্র ফোনেই সংরক্ষিত থাকে",
                    trailing = null
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ─── Category 3: স্মার্ট ক্যাশ ও স্টোরেজ ───────────────────────
            CategoryHeader(title = "স্মার্ট ক্যাশ ও অফলাইন মেমরি")
            CategoryCard {
                SettingsRow(
                    icon = Icons.Default.Storage,
                    iconTint = WarmAmber,
                    title = "অফলাইন ক্যাশ মেমরি",
                    subtitle = "১৮০ দিন মেয়াদের দ্রুত অ্যাক্সেস স্টোরেজ",
                    trailing = {
                        Text(
                            text = "${uiState.cacheSizeMB.toString().toBengaliDigits()} MB",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTeal
                        )
                    }
                )

                HorizontalDivider(color = Color(0xFFF0F4F4), modifier = Modifier.padding(vertical = 4.dp))

                SettingsRow(
                    icon = Icons.Default.Medication,
                    iconTint = PrimaryTeal,
                    title = "সংরক্ষিত ওষুধ ও টেস্ট",
                    subtitle = "${uiState.medicineCacheCount.toString().toBengaliDigits()} টি ওষুধ • ${uiState.testCacheCount.toString().toBengaliDigits()} টি টেস্ট তথ্য",
                    trailing = null
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { viewModel.onEvent(SettingsUiEvent.OnClearCacheClicked) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ক্যাশ মেমরি পরিষ্কার করুন", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ─── Category 4: স্বাস্থ্য এআই সহকারী ──────────────────────────
            CategoryHeader(title = "স্বাস্থ্য এআই সহকারী (Gemini AI)")
            CategoryCard {
                SettingsRow(
                    icon = Icons.Default.AutoAwesome,
                    iconTint = AiPurple,
                    title = "জেমিনি এআই ইঞ্জিন",
                    subtitle = "প্রেসক্রিপশন ও রিপোর্ট বিশ্লেষণে সহায়তাকারী",
                    trailing = {
                        Surface(
                            color = AiPurpleContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "সংযুক্ত",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AiPurple,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                )

                HorizontalDivider(color = Color(0xFFF0F4F4), modifier = Modifier.padding(vertical = 4.dp))

                SettingsRow(
                    icon = Icons.Default.HealthAndSafety,
                    iconTint = PrimaryTeal,
                    title = "ক্লিনিক্যাল নীতি ও সুরক্ষা",
                    subtitle = "এআই পরামর্শ কখনো ডাক্তারের বিকল্প বা ডায়াগনসিস নয়",
                    trailing = null
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ─── Category 5: জরুরি সহায়তা ও স্বাস্থ্য হটলাইন ────────────────
            CategoryHeader(title = "জরুরি সহায়তা ও স্বাস্থ্য হটলাইন")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBgRose),
                border = BorderStroke(1.dp, BorderRoseSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // 999 Hotline
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("জাতীয় জরুরি সেবা (৯৯৯)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CoralRed)
                            Text("অ্যাম্বুলেন্স ও পুলিশ সহায়তা", fontSize = 12.sp, color = CoralRed.copy(alpha = 0.8f))
                        }
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:999"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CoralRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("৯৯৯", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = CoralRed.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 10.dp))

                    // 16263 Shastho Batayan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("স্বাস্থ্য বাতায়ন (১৬২৬৩)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryTeal)
                            Text("সরকারি ২৪ ঘণ্টা ডাক্তার পরামর্শ সেবা", fontSize = 12.sp, color = PrimaryTeal.copy(alpha = 0.8f))
                        }
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:16263"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("১৬২৬৩", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ─── Category 6: অ্যাপ পরিচিতি ও ভার্সন ─────────────────────────
            CategoryHeader(title = "অ্যাপ পরিচিতি")
            CategoryCard {
                SettingsRow(
                    icon = Icons.Default.Info,
                    iconTint = PrimaryTeal,
                    title = "সংস্করণ",
                    subtitle = "MediAware v২.১.০ (বিল্ড ২৪)",
                    trailing = null
                )

                HorizontalDivider(color = Color(0xFFF0F4F4), modifier = Modifier.padding(vertical = 4.dp))

                SettingsRow(
                    icon = Icons.Default.CheckCircleOutline,
                    iconTint = PrimaryTeal,
                    title = "নির্দেশিকা সমর্থন",
                    subtitle = "ডিজিএইচএস (DGHS) ও বিশ্ব স্বাস্থ্য সংস্থা (WHO)",
                    trailing = null
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        if (uiState.showClearCacheDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.onEvent(SettingsUiEvent.OnDismissClearCacheDialog) },
                title = { Text("ক্যাশ পরিষ্কার করবেন?", fontWeight = FontWeight.Bold) },
                text = { Text("এতে সংরক্ষিত ঔষধ এবং টেস্টের অফলাইন কপি মুছে যাবে। আপনার মূল প্রোফাইল সুরক্ষিত থাকবে।") },
                confirmButton = {
                    TextButton(onClick = { viewModel.onEvent(SettingsUiEvent.OnConfirmClearCache) }) {
                        Text("মুছুন", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
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
private fun CategoryHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryTeal
        )
    }
}

@Composable
private fun CategoryCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFE4ECEC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            content = content
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}
