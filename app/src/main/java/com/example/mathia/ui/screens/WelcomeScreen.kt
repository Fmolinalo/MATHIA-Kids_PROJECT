package com.example.mathia.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathia.AppColors
import com.example.mathia.R
import com.example.mathia.ui.components.MathiaMascot
import com.example.mathia.ui.components.MascotState

@Composable
fun WelcomeScreen(
    onCreateProfile: () -> Unit,
    onLogin: () -> Unit
) {
    // Native Compose floating/bounce animation for axolotl
    val infiniteTransition = rememberInfiniteTransition(label = "ajolote_bounce")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // ── 1. FONDO COLORIDO DE PAISAJE MATHIA (BACKGROUND) ─────────────────
        Image(
            painter = painterResource(id = R.drawable.mathia_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // ── 2. INTERFAZ COMPOSE PROGRAMÁTICA ENCIMA DEL FONDO ───────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(12.dp))

            // Logo Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "MathIA",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = AppColors.PrimaryBlue
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "KIDS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Coral
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── 3. AJOLOTE NORMAL CON ANIMACIÓN FLOTANTE NATIVA ─────────────
            Box(
                modifier = Modifier
                    .offset(y = offsetY.dp)
                    .size(170.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ajolote_default),
                    contentDescription = "El Ajolote de MathIA",
                    modifier = Modifier.size(140.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── 4. TÍTULO, DESCRIPCIÓN Y BENEFICIOS EN COMPOSE ──────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "¡Bienvenidos a MathIA!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = AppColors.TextMain,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "¡Hola! Soy tu amigo el Ajolote. ¡Vamos a aprender matemáticas juntos!",
                        fontSize = 14.sp,
                        color = AppColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(
                        color = AppColors.PrimaryBlue.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Star, null, tint = AppColors.Yellow, modifier = Modifier.size(18.dp))
                            Text("Gana Estrellas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Star, null, tint = AppColors.Coral, modifier = Modifier.size(18.dp))
                            Text("Sube de Nivel", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── 5. BOTÓN COMENZAR EN COMPOSE (ÁREA TÁCTIL 56DP) ─────────────
            Button(
                onClick = onLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryBlue),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        "Comenzar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
