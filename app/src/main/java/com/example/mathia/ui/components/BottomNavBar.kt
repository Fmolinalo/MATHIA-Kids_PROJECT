package com.example.mathia.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathia.AppColors

enum class BottomNavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    HOME("menu", "Inicio", Icons.Default.Home),
    LEARN("game", "Aprender", Icons.Default.School),
    DUELS("duelos", "Duelos", Icons.Default.EmojiEvents),
    CALENDAR("calendario", "Calendario", Icons.Default.CalendarToday),
    PROFILE("perfil", "Perfil", Icons.Default.Person)
}

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavDestination.values().forEach { destination ->
                val isSelected = currentRoute == destination.route

                val animatedBg by animateColorAsState(
                    targetValue = if (isSelected) AppColors.PrimaryBlue.copy(alpha = 0.15f) else Color.Transparent,
                    animationSpec = tween(300),
                    label = "NavBg"
                )

                val animatedContentColor by animateColorAsState(
                    targetValue = if (isSelected) AppColors.PrimaryBlue else AppColors.TextSecondary,
                    animationSpec = tween(300),
                    label = "NavColor"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(animatedBg)
                        .clickable { onNavigate(destination.route) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.title,
                            tint = animatedContentColor,
                            modifier = Modifier.size(22.dp)
                        )
                        if (isSelected) {
                            Text(
                                text = destination.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = animatedContentColor
                            )
                        }
                    }
                }
            }
        }
    }
}
