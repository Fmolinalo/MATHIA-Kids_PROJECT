package com.example.mathia.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mathia.R

object AvatarHelper {
    fun getAvatarIcon(avatarKey: String): ImageVector {
        return when (avatarKey) {
            "👶", "default" -> Icons.Default.Face
            "🦸", "superhero" -> Icons.Default.Shield
            "🚀", "rocket", "avatar_rocket" -> Icons.Default.RocketLaunch
            "🧙", "wizard", "avatar_wizard" -> Icons.Default.AutoAwesome
            "🦄", "unicorn", "avatar_unicorn" -> Icons.Default.Pets
            "🐉", "dragon" -> Icons.Default.CrueltyFree
            "🦊", "fox" -> Icons.Default.Pets
            "🐼", "panda" -> Icons.Default.Pets
            "🦁", "lion" -> Icons.Default.Pets
            "🐯", "tiger" -> Icons.Default.Pets
            "🐨", "koala" -> Icons.Default.Pets
            "🦖", "dino", "avatar_dino" -> Icons.Default.Pets
            "👾", "alien", "avatar_alien" -> Icons.Default.SmartToy
            "👑", "crown", "avatar_crown" -> Icons.Default.MilitaryTech
            "👧", "🧒", "👧🏻", "🧑", "🐣" -> Icons.Default.Face
            else -> Icons.Default.Person
        }
    }

    fun getThemeIcon(themeVisual: String): ImageVector {
        return Icons.Default.Palette
    }
}
@Composable
fun AvatarIcon(
    avatarKey: String,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    when (avatarKey) {
        "🦖", "dino", "avatar_dino" -> {
            Image(
                painter = painterResource(id = R.drawable.dinosaurio),
                contentDescription = "Dinosaurio",
                modifier = modifier
            )
        }
        "👾", "alien", "avatar_alien" -> {
            Image(
                painter = painterResource(id = R.drawable.alienigena),
                contentDescription = "Alienígena",
                modifier = modifier
            )
        }
        "🦄", "unicorn", "avatar_unicorn" -> {
            Image(
                painter = painterResource(id = R.drawable.unicornio),
                contentDescription = "Unicornio",
                modifier = modifier
            )
        }
        "🧙", "wizard", "avatar_wizard" -> {
            Image(
                painter = painterResource(id = R.drawable.mago),
                contentDescription = "Mago",
                modifier = modifier
            )
        }
        "👑", "crown", "avatar_crown" -> {
            Image(
                painter = painterResource(id = R.drawable.corona),
                contentDescription = "Corona Real",
                modifier = modifier
            )
        }
        else -> {
            Image(
                painter = painterResource(id = R.drawable.ajolote_estudiante),
                contentDescription = "Ajolote Mateo Estudiante",
                modifier = modifier
            )
        }
    }
}