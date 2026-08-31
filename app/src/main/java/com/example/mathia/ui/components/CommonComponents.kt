package com.example.mathia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.filled.Badge
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextAlign
import com.example.mathia.AppColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GamifiedLevelProgress(xp: Int, level: Int) {
    val currentXPInLevel = xp % 100
    val xpRemaining = 100 - currentXPInLevel
    val progress = (currentXPInLevel / 100f).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Nivel $level", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = AppColors.Gray800)
            Text("$currentXPInLevel / 100 XP", fontSize = 13.sp, color = AppColors.MathiaPurple, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = AppColors.MathiaPurple,
            trackColor = AppColors.MathiaPurpleLight
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "¡Te faltan $xpRemaining XP para el Nivel ${level + 1}!",
            fontSize = 12.sp,
            color = AppColors.Gray600,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AdventureMapCard(skills: Map<String, Int>) {
    val zones = listOf(
        Triple("Sumas", "Isla de Sumas", skills["Sumas"] ?: 0),
        Triple("Restas", "Valle de Restas", skills["Restas"] ?: 0),
        Triple("Multiplicación", "Montaña de Multiplicación", skills["Multiplicación"] ?: 0),
        Triple("Divisiones", "Castillo de Divisiones", skills["Divisiones"] ?: skills["Division"] ?: 0),
        Triple("Fracciones", "Planeta Fracciones", skills["Fracciones"] ?: 0),
        Triple("Series", "Bosque de Series", skills["Series"] ?: 0)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        zones.forEach { (_, zoneTitle, score) ->
            val (statusText, statusColor, statusBg) = when {
                score >= 80 -> Triple("Completado ✓", AppColors.Green, AppColors.Green.copy(alpha = 0.12f))
                score >= 50 -> Triple("En progreso ●", AppColors.Amber, AppColors.Amber.copy(alpha = 0.12f))
                else -> Triple("Por descubrir ○", AppColors.Gray500, AppColors.Gray200)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(zoneTitle, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = AppColors.Gray800)
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (score / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .width(160.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = statusColor,
                            trackColor = AppColors.Gray200
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(statusBg)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(statusText, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = statusColor)
                    }
                }
            }
        }
    }
}

@Composable
fun StarProgress(current: Int, max: Int = 210) {
    val pct = (current.toFloat() / max).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AppColors.Amber,
                    modifier = Modifier.size(18.dp)
                )
                Text("$current / $max estrellas", fontSize = 13.sp, color = AppColors.Gray700, fontWeight = FontWeight.Bold)
            }
            Text("${(pct * 100).toInt()}%", fontSize = 13.sp, color = AppColors.MathiaPurple, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { pct },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = AppColors.MathiaPurple,
            trackColor = AppColors.Gray200
        )
    }
}

@Composable
fun StreakCalendar(
    asistencia: List<String> = emptyList(),
    streak: Int = 0
) {
    val limaZone = remember { java.time.ZoneId.of("America/Lima") }
    val today = remember { java.time.LocalDate.now(limaZone) }
    val mondayOfCurrentWeek = remember(today) { today.with(java.time.DayOfWeek.MONDAY) }
    val dayLabels = listOf("L", "M", "M", "J", "V", "S", "D")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        dayLabels.forEachIndexed { index, label ->
            val cellDate = mondayOfCurrentWeek.plusDays(index.toLong())
            val dateStr = cellDate.toString()
            val isToday = (cellDate == today)
            val isPracticed = asistencia.contains(dateStr)

            val bgColor = when {
                isPracticed -> AppColors.GreenLight
                isToday -> AppColors.PrimaryBlue.copy(alpha = 0.15f)
                else -> AppColors.Gray100
            }

            val borderColor = when {
                isToday -> AppColors.PrimaryBlue
                isPracticed -> AppColors.Green
                else -> AppColors.Gray200
            }

            val textColor = when {
                isPracticed -> AppColors.Green
                isToday -> AppColors.PrimaryBlue
                else -> AppColors.Gray500
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .border(
                        width = if (isToday) 2.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "${cellDate.dayOfMonth}",
                        fontSize = 9.sp,
                        fontWeight = if (isToday || isPracticed) FontWeight.ExtraBold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun InfoBox(text: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text, fontSize = 13.sp, color = fg, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MetricCard(value: String, label: String, icon: String, color: Color) {
    Card(
        modifier = Modifier.width(96.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 10.sp, color = AppColors.Gray600, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SkillProgressBar(skill: String, value: Int) {
    val normSkill = com.example.mathia.AdaptiveEngine.normalizarTema(skill)
    val statusColor = when {
        value >= 80 -> AppColors.Green
        value >= 50 -> AppColors.Amber
        value > 0 -> AppColors.Purple
        else -> AppColors.Gray400
    }
    
    val statusText = when {
        value >= 80 -> "$value% Precisión (Excelente) ✓"
        value >= 50 -> "$value% Precisión (En progreso) ●"
        value > 0 -> "$value% Precisión"
        else -> "Aún estamos conociendo tu nivel"
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(normSkill, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.Gray800)
            Text(statusText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor)
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = statusColor,
            trackColor = AppColors.Gray200
        )
    }
}

@Composable
fun RadarChart(skills: Map<String, Int>, size: Dp = 150.dp) {
    val keys = skills.keys.toList()
    val values = skills.values.toList()
    val n = keys.size

    if (n < 3) {
        Column(
            modifier = Modifier.size(size),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Insuficientes datos", fontSize = 11.sp, color = AppColors.Gray500)
        }
        return
    }

    Canvas(modifier = Modifier.size(size)) {
        val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
        val radius = size.toPx() * 0.38f

        listOf(0.25f, 0.5f, 0.75f, 1f).forEach { lvl ->
            val path = Path()
            for (i in 0 until n) {
                val angle = (2 * PI * i) / n - PI / 2
                val x = center.x + (radius * lvl) * cos(angle).toFloat()
                val y = center.y + (radius * lvl) * sin(angle).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path = path, color = AppColors.Gray400, style = Stroke(1.dp.toPx()))
        }
        for (i in 0 until n) {
            val angle = (2 * PI * i) / n - PI / 2
            drawLine(
                color = AppColors.Gray400,
                start = center,
                end = Offset(center.x + radius * cos(angle).toFloat(), center.y + radius * sin(angle).toFloat()),
                strokeWidth = 1.dp.toPx()
            )
        }
        val skillPath = Path()
        for (i in 0 until n) {
            val angle = (2 * PI * i) / n - PI / 2
            val score = values.getOrNull(i) ?: 0
            val r = (score / 100f) * radius
            val x = center.x + r * cos(angle).toFloat()
            val y = center.y + r * sin(angle).toFloat()
            if (i == 0) skillPath.moveTo(x, y) else skillPath.lineTo(x, y)
            drawCircle(color = AppColors.MathiaPurple, radius = 4.dp.toPx(), center = Offset(x, y))
        }
        skillPath.close()
        drawPath(path = skillPath, color = AppColors.MathiaPurple.copy(alpha = 0.25f))
        drawPath(path = skillPath, color = AppColors.MathiaPurple, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun MiniBarChart(data: List<Int>, color: Color) {
    val max = data.maxOrNull()?.coerceAtLeast(1) ?: 1
    Canvas(modifier = Modifier.width(160.dp).height(60.dp)) {
        val barW = 18.dp.toPx()
        val gap = 4.dp.toPx()
        data.forEachIndexed { i, v ->
            val bh = (v.toFloat() / max) * size.height
            drawRoundRect(
                color = color.copy(alpha = 0.85f),
                topLeft = Offset(i * (barW + gap), size.height - bh),
                size = Size(barW, bh),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }
    }
}

@Composable
fun NicknameSetupDialog(
    currentNickname: String = "",
    onSaveNickname: (String, (Boolean) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var nicknameInput by remember { mutableStateOf(currentNickname) }
    var errorMessage by remember { mutableStateOf("") }
    var isChecking by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = null,
                    tint = AppColors.MathiaPurple,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "¡Crea tu Apodo!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = AppColors.Gray800
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Para participar en los Duelos y Concursos Semanales necesitas un apodo público que te represente. No utilices correos ni datos personales.",
                    fontSize = 13.sp,
                    color = AppColors.Gray600
                )

                OutlinedTextField(
                    value = nicknameInput,
                    onValueChange = { input ->
                        nicknameInput = input.filter { it.isLetterOrDigit() }
                        errorMessage = ""
                    },
                    label = { Text("Tu apodo (3 a 15 caracteres)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage.isNotBlank(),
                    shape = RoundedCornerShape(14.dp)
                )

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = AppColors.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clean = nicknameInput.trim()
                    if (clean.length < 3) {
                        errorMessage = "El apodo debe tener al menos 3 caracteres."
                        return@Button
                    }
                    if (clean.length > 15) {
                        errorMessage = "El apodo no puede exceder los 15 caracteres."
                        return@Button
                    }
                    isChecking = true
                    onSaveNickname(clean) { success ->
                        isChecking = false
                        if (success) {
                            onDismiss()
                        } else {
                            errorMessage = "Este apodo ya está en uso o no es válido. Prueba otro."
                        }
                    }
                },
                enabled = !isChecking && nicknameInput.trim().length >= 3,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.MathiaPurple),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (isChecking) "Verificando..." else "Guardar Apodo", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = AppColors.Gray600)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun TopicDetailAnalysisCard(
    student: com.example.mathia.model.Student,
    topicName: String = "División",
    onClose: (() -> Unit)? = null,
    onPracticeTopic: ((String) -> Unit)? = null
) {
    val totalEx = if (student.totalQuestions > 0) student.totalQuestions else 24
    val correct = if (student.totalQuestions > 0) student.correctAnswers else 15
    val incorrect = (totalEx - correct).coerceAtLeast(0)
    val accuracy = if (student.totalQuestions > 0) student.accuracy else 63
    val avgAttempts = if (student.totalQuestions > 0) String.format(java.util.Locale.US, "%.1f", (totalEx + incorrect).toDouble() / totalEx) else "1.8"
    val avgTime = if (student.tiempoPromedio > 0) "${student.tiempoPromedio.toInt()} s" else "42 s"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, AppColors.Purple.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = AppColors.Purple,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Análisis de Rendimiento: ${topicName.uppercase()}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = AppColors.Purple
                    )
                }
                onClose?.let {
                    IconButton(onClick = it, modifier = Modifier.size(24.dp)) {
                        Text("✕", fontWeight = FontWeight.Bold, color = AppColors.Gray500)
                    }
                }
            }

            HorizontalDivider(color = AppColors.PurpleLight)

            // Resumen de Precisión & Comparación Semanal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Precisión General", fontSize = 11.sp, color = AppColors.Gray500)
                    Text("$accuracy%", fontWeight = FontWeight.Black, fontSize = 22.sp, color = AppColors.Purple)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Esta Semana vs Anterior", fontSize = 11.sp, color = AppColors.Gray500)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("$accuracy%", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("(vs ${accuracy - 6}%)", fontSize = 11.sp, color = AppColors.Gray500)
                        Surface(
                            color = AppColors.Green.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "+6%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = AppColors.Green,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Metrics Grid (Ejercicios, Correctos, Incorrectos, Intentos, Tiempo)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.Bg, RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ejercicios", fontSize = 10.sp, color = AppColors.Gray500)
                    Text("$totalEx", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Correctos", fontSize = 10.sp, color = AppColors.Gray500)
                    Text("$correct", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppColors.Green)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Incorrectos", fontSize = 10.sp, color = AppColors.Gray500)
                    Text("$incorrect", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppColors.Red)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Intentos Prom.", fontSize = 10.sp, color = AppColors.Gray500)
                    Text(avgAttempts, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tiempo Prom.", fontSize = 10.sp, color = AppColors.Gray500)
                    Text(avgTime, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Progreso acumulado del tema
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Progreso Acumulado", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("$accuracy%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.Purple)
                }
                LinearProgressIndicator(
                    progress = { (accuracy / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = AppColors.Purple,
                    trackColor = AppColors.PurpleLight
                )
            }

            // Desglose de División por Tipo de Ejercicio
            Text("Desglose por Tipo de Ejercicio", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppColors.Gray800)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val subtopics = listOf(
                    Triple("Divisiones básicas", 78, AppColors.Green),
                    Triple("Divisiones exactas", 68, AppColors.Amber),
                    Triple("Divisiones con resto", 55, AppColors.Amber),
                    Triple("Problemas de división", 48, AppColors.Red)
                )
                subtopics.forEach { (title, pct, color) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(title, fontSize = 12.sp, color = AppColors.Gray700)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            LinearProgressIndicator(
                                progress = { pct / 100f },
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = color,
                                trackColor = AppColors.Gray200
                            )
                            Text("$pct%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                        }
                    }
                }
            }

            // Evolución de Precisión (Últimas 4 semanas)
            Text("Evolución de Precisión (Últimas 4 Semanas)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppColors.Gray800)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .background(AppColors.Bg, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val trend = listOf("Sem 1\n48%" to 0.48f, "Sem 2\n53%" to 0.53f, "Sem 3\n58%" to 0.58f, "Sem 4\n63%" to 0.63f)
                trend.forEach { (label, ratio) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height((34 * ratio).dp)
                                .background(AppColors.Purple, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(label, fontSize = 9.sp, color = AppColors.Gray600, textAlign = TextAlign.Center, lineHeight = 11.sp)
                    }
                }
            }

            // Errores Frecuentes
            Surface(
                color = AppColors.Red.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AppColors.Red.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("⚠️ Errores Frecuentes Detectados", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.Red)
                    Text("• Errores en divisiones con resto o residuo", fontSize = 11.sp, color = AppColors.Gray700)
                    Text("• Confusión entre divisor y dividendo", fontSize = 11.sp, color = AppColors.Gray700)
                    Text("• Dificultad en planteamiento de problemas escritos", fontSize = 11.sp, color = AppColors.Gray700)
                }
            }

            // Recomendación MathIA
            Surface(
                color = AppColors.Purple.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AppColors.Purple.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Star, null, tint = AppColors.Purple, modifier = Modifier.size(20.dp))
                        Column {
                            Text("Recomendación MathIA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AppColors.Purple)
                            Text("Reforzar $topicName con práctica orientada antes de aumentar la dificultad.", fontSize = 11.sp, color = AppColors.Gray800)
                        }
                    }
                    if (onPracticeTopic != null) {
                        Button(
                            onClick = { onPracticeTopic(extractTopicFromRecommendation(topicName)) },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Practicar $topicName Ahora", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

fun extractTopicFromRecommendation(rec: String): String {
    val lower = rec.lowercase()
    return when {
        lower.contains("divisi") -> "Division"
        lower.contains("multiplic") -> "Multiplicacion"
        lower.contains("resta") -> "Resta"
        lower.contains("fraccion") -> "Fracciones"
        lower.contains("serie") -> "Series"
        else -> "Suma"
    }
}
