package com.example.mediaware.core.designsystem.component

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.ElderlyWoman
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.mediaware.core.designsystem.theme.AiPurple
import com.example.mediaware.core.designsystem.theme.CoralRed
import com.example.mediaware.core.designsystem.theme.EmeraldGreen
import com.example.mediaware.core.designsystem.theme.OceanBlue
import com.example.mediaware.core.designsystem.theme.PrimaryTeal
import com.example.mediaware.core.designsystem.theme.WarmAmber
import java.io.File

data class AvatarOption(
    val id: String,
    val nameBn: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val gradientColors: List<Color>
)

val PRESET_AVATARS = listOf(
    AvatarOption("avatar_teal", "তরুণ পুরুষ", Icons.Default.Person, PrimaryTeal, listOf(PrimaryTeal, Color(0xFF004D40))),
    AvatarOption("avatar_mint", "তরুণী মহিলা", Icons.Default.Face, EmeraldGreen, listOf(EmeraldGreen, Color(0xFF00796B))),
    AvatarOption("avatar_ocean", "প্রবীণ", Icons.Default.Elderly, OceanBlue, listOf(OceanBlue, Color(0xFF1565C0))),
    AvatarOption("avatar_coral", "প্রবীণা", Icons.Default.ElderlyWoman, CoralRed, listOf(CoralRed, Color(0xFFC2185B))),
    AvatarOption("avatar_purple", "স্বাস্থ্য সহকারী", Icons.Default.MedicalServices, AiPurple, listOf(AiPurple, Color(0xFF512DA8))),
    AvatarOption("avatar_amber", "সুরক্ষা প্রতীক", Icons.Default.HealthAndSafety, WarmAmber, listOf(WarmAmber, Color(0xFFE65100)))
)

@Composable
fun UserAvatarView(
    avatarId: String = "avatar_teal",
    photoUriString: String? = null,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val bitmap: Bitmap? = remember(photoUriString) {
        if (!photoUriString.isNullOrBlank()) {
            try {
                if (photoUriString.startsWith("content://")) {
                    val uri = Uri.parse(photoUriString)
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                } else {
                    val file = File(photoUriString)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)
                    } else null
                }
            } catch (_: Exception) {
                null
            }
        } else null
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "প্রোফাইল ছবি",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .border(1.5.dp, Color.White, CircleShape)
        )
    } else {
        val avatar = PRESET_AVATARS.find { it.id == avatarId } ?: PRESET_AVATARS.first()
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(Brush.linearGradient(avatar.gradientColors))
                .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = avatar.icon,
                contentDescription = avatar.nameBn,
                tint = Color.White,
                modifier = Modifier.size(size * 0.58f)
            )
        }
    }
}
