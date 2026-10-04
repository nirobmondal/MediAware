package com.example.mediaware.features.chamber.presentation.hub

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.features.chamber.presentation.ChamberUiEvent
import com.example.mediaware.features.chamber.presentation.ChamberUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChamberHubScreen(
    uiState: ChamberUiState,
    onEvent: (ChamberUiEvent) -> Unit,
    onNavigateToQuickRef: () -> Unit,
    onNavigateToChecklist: () -> Unit,
    onNavigateToRecorder: () -> Unit,
    onNavigateToSummary: () -> Unit = {},
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "চেম্বার সাপোর্ট হাব",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ডাক্তার ভিজিটের জন্য প্রস্তুতি ও সহায়তা",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Silent Mode Prompt Card (Chamber Constraint: 0dB Noise)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isRingerMuted) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (uiState.isRingerMuted) Color(0xFF81C784) else Color(0xFFFFB74D)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (uiState.isRingerMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = if (uiState.isRingerMuted) Color(0xFF2E7D32) else Color(0xFFE65100),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (uiState.isRingerMuted) "সাইলেন্ট মোড সক্রিয় আছে" else "চেম্বারে প্রবেশের আগে ফোন সাইলেন্ট করুন",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.isRingerMuted) Color(0xFF2E7D32) else Color(0xFFE65100)
                                )
                                Text(
                                    text = if (uiState.isRingerMuted) "ভিজিটের সময় কোনো রিং বা বিপ শব্দ হবে না।" else "জরুরি পরামর্শে কোনো অপ্রত্যাশিত শব্দ যাতে বিঘ্ন না ঘটায়।",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = uiState.isRingerMuted,
                            onCheckedChange = { onEvent(ChamberUiEvent.OnToggleRingerMute) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2E7D32)
                            )
                        )
                    }
                }
            }

            item {
                Text(
                    text = "চেম্বার টুলস ও ফিচার",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Portal 1: Quick Reference Card
            item {
                ChamberActionCard(
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    title = "ডাক্তার প্রেজেন্টেশন কার্ড",
                    subtitle = "ডাক্তারকে আপনার সাম্প্রতিক সুগার, রক্তচাপ ও লক্ষণসমূহ একনজরে দেখান।",
                    badgeText = "💡 ওয়েক-লক ও টাচ-লক",
                    onClick = onNavigateToQuickRef
                )
            }

            // Portal 2: Question Checklist
            item {
                ChamberActionCard(
                    icon = Icons.AutoMirrored.Filled.FactCheck,
                    title = "ডাক্তারের জন্য প্রশ্ন তালিকা",
                    subtitle = "জরুরি প্রশ্নগুলোর উত্তর জেনে নিয়ে চেকলিস্টে টিকচিহ্ন দিন।",
                    badgeText = uiState.progressFormattedBn,
                    onClick = onNavigateToChecklist
                )
            }

            // Portal 3: Audio Recorder
            item {
                ChamberActionCard(
                    icon = Icons.Default.Mic,
                    title = "পরামর্শ অডিও রেকর্ডার",
                    subtitle = "ডাক্তারের মৌখিক অনুমতি সাপেক্ষে পরামর্শ রেকর্ড করুন।",
                    badgeText = "⏱️ ১৫ মিনিট সর্বোচ্চ সীমা",
                    onClick = onNavigateToRecorder
                )
            }

            // Portal 4: Consultation Summary & Action Plan (Screen 24)
            item {
                ChamberActionCard(
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    title = "ভিজিট সারাংশ ও কর্মপরিকল্পনা",
                    subtitle = "পরামর্শের মূল নির্দেশনা, করণীয় তালিকা ও পরবর্তী ফলো-আপ রিমাইন্ডার।",
                    badgeText = "📝 সারাংশ ও ক্যালেন্ডার",
                    onClick = onNavigateToSummary
                )
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = PrimaryTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🔒 চেম্বার মোডের সকল তথ্য ও অডিও আপনার ডিভাইসের সুরক্ষিত ভল্টে এনক্রিপ্টেড থাকে।",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ChamberActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(PrimaryTeal.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryTeal,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = PrimaryTeal.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryTeal,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
