package com.example.mathia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mathia.AppColors
import com.example.mathia.model.CalendarEventSource
import com.example.mathia.ui.components.MathiaMascot
import com.example.mathia.ui.components.MascotState
import com.example.mathia.viewModels.CalendarViewModel
import java.time.YearMonth

@Composable
fun CalendarScreen(
    studentName: String = "",
    studentAsistencia: List<String> = emptyList(),
    onBack: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val limaZone = remember { java.time.ZoneId.of("America/Lima") }

    LaunchedEffect(Unit) {
        viewModel.loadMonthEvents(context, YearMonth.now(limaZone), studentAsistencia)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.mathia.R.drawable.mathia_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = AppColors.PrimaryBlue)
                }
                Text(
                    text = "Calendario MathIA",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = AppColors.TextMain
                )
            }

            // Connection Status Badge / Connect Button (No Client IDs or Sensitive Credentials)
            if (uiState.isGoogleConnected) {
                Surface(
                    color = AppColors.GreenLight,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Green)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = AppColors.Green, modifier = Modifier.size(14.dp))
                        Text("Google Calendar Conectado", fontSize = 11.sp, color = AppColors.Green, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = {
                        viewModel.loadMonthEvents(context, uiState.currentMonth, studentAsistencia)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.SecondaryBlue),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = "Conectar", modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text("Conectar Google Calendar", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Ajolote Mascot Banner
        MathiaMascot(
            mascotState = MascotState.DEFAULT,
            message = "¡Organizar tu tiempo te ayuda a ganar más estrellas y mantener tu racha!",
            size = 80.dp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        // Loading State with Official Running Axolotl Sprite
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MathiaMascot(
                        mascotState = MascotState.LOADING,
                        size = 120.dp
                    )
                    Text(
                        text = "Cargando calendario...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.PrimaryBlue
                    )
                }
            }
        } else if (uiState.events.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Event, null, tint = AppColors.TextSecondary, modifier = Modifier.size(40.dp))
                        Text(
                            text = "No tienes eventos para este mes.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextMain
                        )
                        Text(
                            text = "Tus actividades completadas y reuniones aparecerán aquí.",
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                }
            }
        } else {
            // Event List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(uiState.events) { event ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            val iconColor = when (event.source) {
                                CalendarEventSource.MATHIA -> AppColors.Green
                                CalendarEventSource.GOOGLE -> AppColors.PrimaryBlue
                                CalendarEventSource.TEACHER -> AppColors.Coral
                            }

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(iconColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (event.source == CalendarEventSource.GOOGLE) Icons.Default.Event else Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = AppColors.TextMain
                                )
                                if (event.description.isNotBlank()) {
                                    Text(
                                        text = event.description,
                                        fontSize = 12.sp,
                                        color = AppColors.TextSecondary
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${event.date} ${if (event.startTime.isNotBlank()) "• ${event.startTime}" else ""}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = iconColor
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
