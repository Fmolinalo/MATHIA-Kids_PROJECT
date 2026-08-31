package com.example.mathia.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathia.AppColors
import com.example.mathia.R

enum class MascotState {
    DEFAULT,
    CORRECT,
    INCORRECT,
    LOADING,
    THINKING,
    CELEBRATING,
    STREAK,
    LEVEL_UP,
    HINT
}

enum class AjoloteState {
    NORMAL,
    HAPPY,
    CELEBRATING,
    THINKING,
    HINT,
    CONFUSED,
    ASLEEP,
    SURPRISED
}

@Composable
fun MathiaMascot(
    mascotState: MascotState = MascotState.DEFAULT,
    message: String? = null,
    size: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    AjoloteMascotView(
        mascotState = mascotState,
        message = message,
        size = size,
        modifier = modifier
    )
}

@Composable
fun AjoloteMascotView(
    mascotState: MascotState = MascotState.DEFAULT,
    state: AjoloteState = AjoloteState.NORMAL,
    message: String? = null,
    size: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AjoloteBreathe")
    
    // Scale breathe effect
    val scale by infiniteTransition.animateFloat(
        initialValue = if (mascotState == MascotState.LOADING) 0.92f else 0.96f,
        targetValue = if (mascotState == MascotState.LOADING) 1.08f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (mascotState == MascotState.LOADING) 600 else 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScaleBreathe"
    )

    // Horizontal bounce offset for loading/running state
    val offsetX by infiniteTransition.animateFloat(
        initialValue = if (mascotState == MascotState.LOADING) -12f else 0f,
        targetValue = if (mascotState == MascotState.LOADING) 12f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RunOffset"
    )

    val imageRes = when {
        mascotState == MascotState.LOADING -> R.drawable.ajolote_loading
        mascotState == MascotState.CORRECT || mascotState == MascotState.CELEBRATING || mascotState == MascotState.STREAK || mascotState == MascotState.LEVEL_UP || state == AjoloteState.HAPPY || state == AjoloteState.CELEBRATING -> R.drawable.ajolote_correcto
        mascotState == MascotState.INCORRECT || mascotState == MascotState.HINT || state == AjoloteState.CONFUSED || state == AjoloteState.HINT -> R.drawable.ajolote_incorrecto
        else -> R.drawable.ajolote_default
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "El Ajolote de MathIA",
            modifier = Modifier
                .size(size)
                .offset(x = offsetX.dp)
                .scale(scale)
        )

        if (!message.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp))
                    .background(Color.White)
                    .border(2.dp, AppColors.PrimaryBlue.copy(alpha = 0.3f), RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 4.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextMain
                )
            }
        }
    }
}
