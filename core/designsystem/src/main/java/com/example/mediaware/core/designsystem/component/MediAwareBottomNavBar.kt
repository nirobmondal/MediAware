package com.example.mediaware.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.mediaware.core.designsystem.theme.PrimaryContainerTeal
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.core.designsystem.theme.TextSecondaryGrey

enum class MediAwareNavTab(
    val titleBn: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    HOME("হোম", Icons.Default.Home, "হোম হাব"),
    SYMPTOMS("লক্ষণ", Icons.Default.MedicalServices, "লক্ষণ বাছাই"),
    REPORT("রিপোর্ট", Icons.Default.Science, "ল্যাব রিপোর্ট ও প্রেসক্রিপশন"),
    REMINDERS("রিমাইন্ডার", Icons.Default.Alarm, "অ্যালার্ম ও রিমাইন্ডার সেন্টার"),
    SETTINGS("সেটিংস", Icons.Default.Settings, "সিস্টেম সেটিংস")
}

@Composable
fun MediAwareBottomNavBar(
    selectedTab: MediAwareNavTab,
    onTabSelected: (MediAwareNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        MediAwareNavTab.values().forEach { tab ->
            val isSelected = selectedTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.contentDescription
                    )
                },
                label = {
                    Text(
                        text = tab.titleBn,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryTeal,
                    selectedTextColor = PrimaryTeal,
                    indicatorColor = PrimaryContainerTeal,
                    unselectedIconColor = TextSecondaryGrey,
                    unselectedTextColor = TextSecondaryGrey
                )
            )
        }
    }
}
