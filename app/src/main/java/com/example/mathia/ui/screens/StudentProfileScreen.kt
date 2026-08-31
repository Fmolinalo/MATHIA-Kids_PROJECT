package com.example.mathia.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathia.AppColors
import com.example.mathia.model.Student
import com.example.mathia.ui.components.MiniBarChart
import com.example.mathia.ui.components.SkillProgressBar
import com.example.mathia.ui.components.AvatarIcon
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Lightbulb
import com.example.mathia.ui.theme.Gray2Dinamico
import com.example.mathia.ui.theme.GrayDinamico
import com.example.mathia.ui.theme.Purple2Dinamico
import java.text.SimpleDateFormat

import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StudentProfileScreen(
    student: Student,
    onBack: () -> Unit,
    onPlayTopic: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    // Dynamic Achievements
    val achievements = listOf(
        Badge("Primeros Pasos", "¡Resuelve tu primera pregunta de práctica!", Icons.Default.PlayArrow, student.totalQuestions > 0),
        Badge("Velocidad Absoluta", "Tiempo promedio menor a 7s por respuesta.", Icons.Default.Speed, student.tiempoPromedio > 0.0 && student.tiempoPromedio < 7.0),
        Badge("Precisión Divina", "Alcanza un 85% de precisión general.", Icons.Default.Star, student.accuracy >= 85),
        Badge("Práctica Imparable", "Consigue una racha de 3 días de práctica.", Icons.Default.Whatshot, student.streak >= 3),
        Badge("Diagnóstico de Mateo", "Completa la prueba de nivel adaptativa.", Icons.Default.Psychology, student.diagnosticoRealizado),
        Badge("Coleccionista", "Desbloquea al menos 2 avatares en la tienda.", Icons.Default.ShoppingBag, student.unlockedAvatars.size >= 2)
    )

    // Dynamic study calendar state in America/Lima timezone
    val limaZone = remember { java.time.ZoneId.of("America/Lima") }
    var currentYearMonth by remember { mutableStateOf(java.time.YearMonth.now(limaZone)) }
    val today = remember { java.time.LocalDate.now(limaZone) }
    val activeStreak = remember(student.asistencia) {
        com.example.mathia.model.StreakCalculator.getActiveStreak(student.asistencia)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Estudiante", fontWeight = FontWeight.Bold, color = Purple2Dinamico) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Purple2Dinamico)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareText = "¡Mira mi progreso en MathIA!\nNivel: ${student.level}\nEstrellas: ${student.stars}\nPrecisión: ${student.accuracy}%\n¡Únete a entrenar con Mateo el Ajolote!"
                        clipboardManager.setText(AnnotatedString(shareText))
                        Toast.makeText(context, "¡Progreso copiado al portapapeles! Listo para compartir.", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Share, "Compartir", tint = Purple2Dinamico)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.mathia.R.drawable.fondo_perfil_estudiante),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                // ─── Header: Avatar & Name Card ───
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.93f))
                ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(AppColors.PrimaryBlue.copy(alpha = 0.12f))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        com.example.mathia.ui.components.AjoloteMascotView(
                            mascotState = com.example.mathia.ui.components.MascotState.DEFAULT,
                            size = 95.dp
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    
                    Text(
                        text = "${student.name} ${student.lastName}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Purple2Dinamico,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    // Badges details row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SuggestionChip(onClick = {}, label = { Text("${student.edad} años") })
                        SuggestionChip(onClick = {}, label = { Text(student.grade) })
                        if (student.seccion.isNotEmpty()) {
                            SuggestionChip(onClick = {}, label = { Text(student.seccion) })
                        }
                    }

                    if (student.colegio.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = AppColors.Gray500,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Colegio: ${student.colegio}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.Gray500
                            )
                        }
                    }
                }
            }

            // ─── Gamification status card ───
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Purple2Dinamico,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Nivel y Experiencia", fontWeight = FontWeight.Bold, color = Purple2Dinamico, fontSize = 16.sp)
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Nivel actual: ${student.level}", fontWeight = FontWeight.Black, fontSize = 18.sp)
                            val nextLevelXP = student.level * 100
                            val prevLevelXP = (student.level - 1) * 100
                            val relativeXP = student.xp - prevLevelXP
                            Text("$relativeXP / 100 XP para el Nivel ${student.level + 1}", fontSize = 12.sp, color = AppColors.Gray500)
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(AppColors.PurpleLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Purple2Dinamico,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    val pctXP = (student.xp % 100) / 100f
                    LinearProgressIndicator(
                        progress = { pctXP },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Purple2Dinamico,
                        trackColor = AppColors.Gray200
                    )

                    Divider(color = AppColors.Gray100, modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Star, null, tint = AppColors.Amber, modifier = Modifier.size(14.dp))
                                Text("Estrellas", fontSize = 11.sp, color = AppColors.Gray500)
                            }
                            val starProgress = (student.stars.toFloat() / com.example.mathia.model.MAX_STARS).coerceIn(0f, 1f)
                            val starPercent = (starProgress * 100).toInt()
                            Text("${student.stars} / ${com.example.mathia.model.MAX_STARS}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = AppColors.Amber)
                            Spacer(Modifier.height(2.dp))
                            LinearProgressIndicator(
                                progress = { starProgress },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = AppColors.Amber,
                                trackColor = AppColors.Gray200
                            )
                            Text("$starPercent%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AppColors.Gray600)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Whatshot, null, tint = AppColors.Pink, modifier = Modifier.size(14.dp))
                                Text("Racha de Estudio", fontSize = 11.sp, color = AppColors.Gray500)
                            }
                            Text("${activeStreak} días", fontWeight = FontWeight.Black, fontSize = 18.sp, color = AppColors.Pink)
                        }
                    }
                }
            }

            // ─── Stats summary grid ───
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Assessment, null, tint = Purple2Dinamico, modifier = Modifier.size(20.dp))
                        Text("Estadísticas de Aprendizaje", fontWeight = FontWeight.Bold, color = Purple2Dinamico, fontSize = 16.sp)
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                            Text("Tiempo Total", fontSize = 11.sp, color = AppColors.Gray500)
                            val totalMin = student.tiempoTotal / 60
                            val totalSec = student.tiempoTotal % 60
                            Text("${totalMin}m ${totalSec}s", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("Tiempo Promedio", fontSize = 11.sp, color = AppColors.Gray500)
                            Text("${student.tiempoPromedio.toInt()}s por respuesta", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                            Text("Preguntas Respondidas", fontSize = 11.sp, color = AppColors.Gray500)
                            Text("${student.totalQuestions}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("Correctas / Incorrectas", fontSize = 11.sp, color = AppColors.Gray500)
                            Text("${student.correctAnswers} / ${(student.totalQuestions - student.correctAnswers).coerceAtLeast(0)}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.Green)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                            Text("Precisión General", fontSize = 11.sp, color = AppColors.Gray500)
                            Text("${student.accuracy}%", fontWeight = FontWeight.Black, fontSize = 17.sp, color = AppColors.Green)
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("Aula asignada", fontSize = 11.sp, color = AppColors.Gray500)
                            Text(student.classroom.ifEmpty { "1ro A" }, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            // ─── Skills Breakdown ───
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Star, null, tint = Purple2Dinamico, modifier = Modifier.size(20.dp))
                        Text("Precisión por Competencia", fontWeight = FontWeight.Bold, color = Purple2Dinamico, fontSize = 16.sp)
                    }
                    student.skills.forEach { (skill, score) ->
                        SkillProgressBar(skill = skill, value = score)
                    }
                }
            }

            // ─── Calendario de estudio dinámico ───
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val spanishLocale = java.util.Locale.forLanguageTag("es-PE")
                    val monthName = currentYearMonth.month
                        .getDisplayName(java.time.format.TextStyle.FULL, spanishLocale)
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(spanishLocale) else it.toString() }

                    // Header with title and month navigation controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, null, tint = Purple2Dinamico, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Calendario ($monthName ${currentYearMonth.year})",
                                fontWeight = FontWeight.Bold,
                                color = Purple2Dinamico,
                                fontSize = 15.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { currentYearMonth = currentYearMonth.minusMonths(1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text("<", fontWeight = FontWeight.Bold, color = Purple2Dinamico, fontSize = 16.sp)
                            }

                            TextButton(
                                onClick = { currentYearMonth = java.time.YearMonth.now(limaZone) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("HOY", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Purple2Dinamico)
                            }

                            IconButton(
                                onClick = { currentYearMonth = currentYearMonth.plusMonths(1) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text(">", fontWeight = FontWeight.Bold, color = Purple2Dinamico, fontSize = 16.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Day of week labels (L M M J V S D) starting Monday
                    val weekHeaders = listOf("L", "M", "M", "J", "V", "S", "D")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        weekHeaders.forEach { header ->
                            Text(
                                header,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Gray400,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Compute grid cells dynamically for currentYearMonth
                    val firstDayOfMonth = currentYearMonth.atDay(1)
                    val leadingBlanks = (firstDayOfMonth.dayOfWeek.value - 1) // Monday = 1 -> 0 blanks
                    val daysInMonth = currentYearMonth.lengthOfMonth()

                    val monthGridCells = mutableListOf<String>()
                    for (i in 0 until leadingBlanks) {
                        monthGridCells.add("")
                    }
                    for (day in 1..daysInMonth) {
                        monthGridCells.add("%04d-%02d-%02d".format(currentYearMonth.year, currentYearMonth.monthValue, day))
                    }

                    // Render weeks
                    monthGridCells.chunked(7).forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            week.forEach { dateStr ->
                                if (dateStr.isEmpty()) {
                                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                } else {
                                    val cellDate = try { java.time.LocalDate.parse(dateStr) } catch (e: Exception) { null }
                                    val isToday = cellDate == today
                                    val practiced = student.asistencia.contains(dateStr)
                                    val dayNum = dateStr.split("-").last().toInt().toString()

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(2.dp)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    isToday -> AppColors.PrimaryBlue.copy(alpha = 0.15f)
                                                    practiced -> AppColors.GreenLight
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = if (isToday) 2.dp else 1.dp,
                                                color = when {
                                                    isToday -> AppColors.PrimaryBlue
                                                    practiced -> AppColors.Green
                                                    else -> AppColors.Gray200
                                                },
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNum,
                                            fontWeight = if (isToday || practiced) FontWeight.ExtraBold else FontWeight.Normal,
                                            color = when {
                                                isToday -> AppColors.PrimaryBlue
                                                practiced -> AppColors.Green
                                                else -> AppColors.Gray600
                                            },
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                            // Fill remaining cells in last week row if less than 7
                            if (week.size < 7) {
                                for (k in 0 until (7 - week.size)) {
                                    Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(12.dp).background(AppColors.GreenLight, RoundedCornerShape(2.dp)).border(1.dp, AppColors.Green, RoundedCornerShape(2.dp)))
                            Text("Días practicados", fontSize = 11.sp, color = AppColors.Gray600)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(12.dp).background(AppColors.PrimaryBlue.copy(alpha = 0.15f), RoundedCornerShape(2.dp)).border(2.dp, AppColors.PrimaryBlue, RoundedCornerShape(2.dp)))
                            Text("Hoy", fontSize = 11.sp, color = AppColors.Gray600, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ─── Achievements / Badges ───
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.EmojiEvents, null, tint = Purple2Dinamico, modifier = Modifier.size(20.dp))
                        Text("Logros e Insignias", fontWeight = FontWeight.Bold, color = Purple2Dinamico, fontSize = 16.sp)
                    }
                    
                    achievements.forEach { badge ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        if (badge.isUnlocked) AppColors.AmberLight else AppColors.Gray100,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (badge.isUnlocked) badge.icon else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (badge.isUnlocked) AppColors.Amber else AppColors.Gray400,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    badge.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (badge.isUnlocked) Color.Black else AppColors.Gray400
                                )
                                Text(
                                    badge.description,
                                    fontSize = 12.sp,
                                    color = AppColors.Gray500
                                )
                            }
                        }
                    }
                }
            }

            // ─── AI Recommendations Card ───
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, Purple2Dinamico.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Lightbulb, null, tint = Purple2Dinamico, modifier = Modifier.size(20.dp))
                        Text("Consejos de Estudio de Mateo", fontWeight = FontWeight.Bold, color = Purple2Dinamico, fontSize = 16.sp)
                    }
                    if (student.recomendaciones.isEmpty()) {
                        Text("Mateo está revisando tus estadísticas. ¡Sigue practicando para recibir sugerencias!", fontSize = 13.sp, color = Gray2Dinamico)
                    } else {
                        student.recomendaciones.forEach { rec ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text("• $rec", fontSize = 13.sp, color = Gray2Dinamico)
                                if (onPlayTopic != null) {
                                    val targetTopic = com.example.mathia.ui.components.extractTopicFromRecommendation(rec)
                                    Button(
                                        onClick = { onPlayTopic(targetTopic) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Purple2Dinamico),
                                        shape = RoundedCornerShape(50.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.padding(top = 4.dp).height(32.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Practicar $targetTopic Ahora", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ─── Weekly evolution mini chart ───
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Icon(Icons.Default.Assessment, null, tint = Purple2Dinamico, modifier = Modifier.size(20.dp))
                        Text("Desempeño Semanal", fontWeight = FontWeight.Bold, color = Purple2Dinamico)
                    }
                    MiniBarChart(data = student.weekData, color = Purple2Dinamico)
                    Text("Evolución de los últimos 7 días practicados", fontSize = 11.sp, color = AppColors.Gray500)
                }
            }
        }
    }
}
}

data class Badge(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean
)

