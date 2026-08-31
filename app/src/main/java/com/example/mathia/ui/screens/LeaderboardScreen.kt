package com.example.mathia.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathia.AppColors
import com.example.mathia.R
import com.example.mathia.StudentViewModel
import com.example.mathia.model.FirebaseStudent
import com.example.mathia.model.Student
import com.example.mathia.ui.components.AvatarIcon

@Composable
fun LeaderboardScreen(
    currentStudent: Student,
    onBack: () -> Unit,
    viewModel: StudentViewModel
) {
    var leaderboardList by remember { mutableStateOf<List<FirebaseStudent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.obtenerTodosAlumnos { list ->
            leaderboardList = list.filter { it.apodo.isNotBlank() }
                .sortedByDescending { it.estrellas }
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.mathia_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.9f),
                    shadowElevation = 2.dp
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = AppColors.PrimaryBlue)
                    }
                }
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = AppColors.Yellow,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Tabla de Campeones",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = AppColors.TextMain
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.ajolote_loading),
                        contentDescription = "Cargando...",
                        modifier = Modifier.size(72.dp)
                    )
                }
            } else {
                // Podium for Top 3
                if (leaderboardList.isNotEmpty()) {
                    val top3 = leaderboardList.take(3)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (top3.size > 1) {
                            PodiumColumn(student = top3[1], place = 2, height = 90.dp, color = Color(0xFFC0C0C0))
                        } else {
                            Spacer(modifier = Modifier.width(90.dp))
                        }

                        if (top3.isNotEmpty()) {
                            PodiumColumn(student = top3[0], place = 1, height = 120.dp, color = AppColors.Yellow)
                        }

                        if (top3.size > 2) {
                            PodiumColumn(student = top3[2], place = 3, height = 70.dp, color = Color(0xFFCD7F32))
                        } else {
                            Spacer(modifier = Modifier.width(90.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Ranking List
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(leaderboardList) { index, student ->
                            val isSelf = student.pin.toString() == currentStudent.pin
                            val position = index + 1

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isSelf) 2.dp else 0.dp,
                                        color = if (isSelf) AppColors.PrimaryBlue else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelf) AppColors.PrimaryBlue.copy(alpha = 0.12f) else AppColors.Gray100.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "#$position",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = if (position <= 3) AppColors.PrimaryBlue else AppColors.Gray600,
                                            modifier = Modifier.width(36.dp)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color.White, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AvatarIcon(student.avatar, modifier = Modifier.size(24.dp), tint = AppColors.PrimaryBlue)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            val displayName = if (student.apodo.isNotBlank()) student.apodo else "Jugador #${student.pin % 1000}"
                                            Text(
                                                text = displayName,
                                                fontWeight = if (isSelf) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 14.sp,
                                                color = AppColors.TextMain
                                            )
                                            Text(
                                                text = "Nivel ${student.nivel_actual} • ${student.xp} XP",
                                                fontSize = 11.sp,
                                                color = AppColors.TextSecondary
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = AppColors.Yellow,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "${student.estrellas}",
                                            fontWeight = FontWeight.Black,
                                            color = AppColors.TextMain,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PodiumColumn(
    student: FirebaseStudent,
    place: Int,
    height: androidx.compose.ui.unit.Dp,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.width(95.dp)
    ) {
        AvatarIcon(student.avatar, modifier = Modifier.size(36.dp), tint = AppColors.PrimaryBlue)
        val displayName = when {
            student.apodo.isNotBlank() -> student.apodo
            else -> "Jugador #${(student.pin % 1000).toString().padStart(3, '0')}"
        }
        Text(
            text = displayName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextMain,
            textAlign = TextAlign.Center
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = AppColors.Yellow,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "${student.estrellas}",
                fontSize = 11.sp,
                color = AppColors.TextSecondary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val placeIcon = when (place) {
                    1 -> Icons.Default.EmojiEvents
                    2 -> Icons.Default.MilitaryTech
                    else -> Icons.Default.Star
                }
                Icon(
                    imageVector = placeIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "${place}°",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
        }
    }
}
