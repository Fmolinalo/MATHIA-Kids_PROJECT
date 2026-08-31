package com.example.mathia.ui.screens

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.mathia.R
import com.example.mathia.AppColors
import com.example.mathia.StudentViewModel
import com.example.mathia.model.Student
import com.example.mathia.model.RespuestaHistorial
import com.example.mathia.model.ReporteSesion
import com.mathia.kids.data.model.HintRequest
import com.mathia.kids.data.model.HintResponse
import com.example.mathia.ui.components.AjoloteMascotView
import com.example.mathia.ui.components.MascotState
import kotlinx.coroutines.delay

import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun GameScreen(
    student: Student,
    operation: String, // "Suma", "Resta", "Multiplicacion", "Fracciones", "Series"
    onScore: (Int, Int, String, Long) -> Unit, // (stars, xp, topic, responseTimeSeconds)
    onBack: () -> Unit,
    viewModel: StudentViewModel
) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("mathia_prefs", Context.MODE_PRIVATE) }
    val currentTutor = remember { sharedPref.getString("student_tutor", "axolita") ?: "axolita" }

    val tutorImageRes = when (currentTutor) {
        "prof_axol" -> R.drawable.ajolote_teacher_male
        "prof_axolina" -> R.drawable.ajolote_teacher_female
        else -> R.drawable.ajolote_student
    }

    val tutorName = when (currentTutor) {
        "prof_axol" -> "El Profesor Axol"
        "prof_axolina" -> "La Profesora Axolina"
        else -> "Axolita"
    }

    val tutorGreeting = when (currentTutor) {
        "prof_axol" -> "¡Excelente trabajo! Has completado el análisis con éxito."
        "prof_axolina" -> "¡Qué gran esfuerzo, mi cielo! Estoy muy orgullosa de ti."
        else -> "¡Increíble! ¡Eres súper veloz resolviendo matemáticas!"
    }

    var questionText by rememberSaveable { mutableStateOf("") }
    var correctAnswer by rememberSaveable { mutableIntStateOf(0) }
    var userAnswer by rememberSaveable { mutableStateOf("") }
    var msg by rememberSaveable { mutableStateOf("¡Resuelve el desafío!") }
    var currentStreak by rememberSaveable { mutableIntStateOf(0) }
    var currentQuestionStart by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var currentTime by rememberSaveable { mutableLongStateOf(0L) }
    
    // Session state tracking
    var questionsAnsweredInSession by rememberSaveable { mutableIntStateOf(0) }
    var sessionCorrectAnswers by rememberSaveable { mutableIntStateOf(0) }
    var sessionIncorrectAnswers by rememberSaveable { mutableIntStateOf(0) }
    var sessionTimeSpent by rememberSaveable { mutableLongStateOf(0L) }
    var attemptsCount by rememberSaveable { mutableIntStateOf(1) }
    var incorrectasConsecutivas by rememberSaveable { mutableIntStateOf(0) }
    val listaDeIntentos = remember { mutableStateListOf<String>() }
    var activeHintResponse by remember { mutableStateOf<HintResponse?>(null) }
    var isLoadingHint by remember { mutableStateOf(false) }
    var showHintDialog by remember { mutableStateOf(false) }
    
    var totalStarsGained by rememberSaveable { mutableIntStateOf(0) }
    var totalXPGained by rememberSaveable { mutableIntStateOf(0) }
    var showSummary by rememberSaveable { mutableStateOf(false) }
    var isSavingReport by remember { mutableStateOf(false) }

    var mascotState by remember { mutableStateOf(MascotState.DEFAULT) }
    val scope = rememberCoroutineScope()

    // Real Adaptive Difficulty setup
    val historialRespuestas by viewModel.historialRespuestas.collectAsState()
    val temaNorm = remember(operation) { com.example.mathia.AdaptiveEngine.normalizarTema(operation) }
    val initialTopicLevel = remember(student, temaNorm) { student.dificultadPorTema[temaNorm] ?: student.level.coerceIn(1, 5) }
    val topicAccuracy = student.skills[operation] ?: student.accuracy
    val adaptiveResult = remember(historialRespuestas, temaNorm, initialTopicLevel, topicAccuracy) {
        com.example.mathia.AdaptiveEngine.calcularDificultadAdaptativa(temaNorm, initialTopicLevel, historialRespuestas, topicAccuracy.toDouble())
    }

    fun generateNewQuestion() {
        mascotState = MascotState.DEFAULT
        val activeLevel = adaptiveResult.nivelCalculado
        val (qText, ans) = com.example.mathia.data.generator.DynamicExerciseGenerator.generateQuestion(operation, activeLevel)

        questionText = qText
        correctAnswer = ans
        attemptsCount = 1
        incorrectasConsecutivas = 0
        listaDeIntentos.clear()
        activeHintResponse = null
        showHintDialog = false
    }

    LaunchedEffect(Unit) {
        generateNewQuestion()
    }

    var shakeTrigger by remember { mutableStateOf(false) }
    val shakeOffset by animateFloatAsState(
        targetValue = if (shakeTrigger) 12f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium),
        finishedListener = { if (it != 0f) shakeTrigger = false },
        label = "shake"
    )

    var showConfetti by remember { mutableStateOf(false) }
    val confettiColors = listOf(Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Cyan, AppColors.Pink)
    val confettiList = remember {
        List(20) {
            Triple(
                Offset((50..950).random().toFloat(), (100..1500).random().toFloat()),
                (12..28).random().toFloat(),
                confettiColors.random()
            )
        }
    }

    LaunchedEffect(currentQuestionStart, userAnswer, showSummary) {
        if (!showSummary) {
            while (true) {
                currentTime = (System.currentTimeMillis() - currentQuestionStart) / 1000
                delay(1000)
            }
        }
    }

    val gameBgColor = when (operation) {
        "Suma" -> Color(0xFFE3F2FD) // Lila pastel/Azul suave
        "Resta" -> Color(0xFFFFF3E0) // Naranja suave
        "Multiplicacion" -> Color(0xFFF1F8E9) // Verde suave
        "Fracciones" -> Color(0xFFF3E5F5) // Lila morado suave
        "Series" -> Color(0xFFFFEBEE) // Rosa suave
        else -> Color(0xFFE0F2F1)
    }

    val headerColor = when (operation) {
        "Suma" -> Color(0xFF2196F3)
        "Resta" -> Color(0xFFFF9800)
        "Multiplicacion" -> Color(0xFF8BC34A)
        "Fracciones" -> Color(0xFF9C27B0)
        "Series" -> Color(0xFFE91E63)
        else -> AppColors.Purple
    }

    if (showSummary) {
        // SUMMARY RECAP FOR GAMEPLAY
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Bg)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = AppColors.MathiaGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "¡Sesión Completada!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Purple
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(2.dp, AppColors.MathiaGold, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = tutorImageRes),
                            contentDescription = tutorName,
                            modifier = Modifier.size(95.dp)
                        )
                    }
                    
                    Text(
                        text = "$tutorName dice: \"$tutorGreeting\"",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.MathiaNavy,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    Divider(color = AppColors.PurpleLight, thickness = 1.dp)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Estrellas", fontSize = 12.sp, color = AppColors.Gray500)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AppColors.Amber,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("$totalStarsGained", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.Amber)
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("XP", fontSize = 12.sp, color = AppColors.Gray500)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    tint = AppColors.Purple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("$totalXPGained XP", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.Purple)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Precisión", fontSize = 12.sp, color = AppColors.Gray500)
                            val prec = if (questionsAnsweredInSession > 0) (sessionCorrectAnswers * 100) / questionsAnsweredInSession else 0
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = AppColors.Green,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("$prec%", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.Green)
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("T. Promedio", fontSize = 12.sp, color = AppColors.Gray500)
                            val avgTime = if (questionsAnsweredInSession > 0) sessionTimeSpent / questionsAnsweredInSession else 0L
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = AppColors.Blue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("${avgTime}s", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.Blue)
                            }
                        }
                    }

                    // Evolución Card (Demuestra el delta de mejora respecto al nivel previo)
                    val sessionPrec = if (questionsAnsweredInSession > 0) (sessionCorrectAnswers * 100) / questionsAnsweredInSession else 0
                    val prevSkillPrec = student.skills[operation] ?: student.accuracy
                    val deltaPrec = sessionPrec - prevSkillPrec

                    Surface(
                        color = AppColors.Green.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, AppColors.Green.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.TrendingUp, null, tint = AppColors.Green, modifier = Modifier.size(24.dp))
                            Column {
                                Text("¡Evolución Registrada!", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppColors.Green)
                                Text(
                                    text = "Precisión en $operation: ${prevSkillPrec}% ➔ ${sessionPrec}% (${if (deltaPrec >= 0) "+$deltaPrec%" else "$deltaPrec%"})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.Gray800
                                )
                            }
                        }
                    }

                    // Adaptive Difficulty Card
                    Surface(
                        color = AppColors.PurpleLight.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, AppColors.Purple.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Tune, null, tint = AppColors.Purple, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Dificultad Adaptativa: ${com.example.mathia.AdaptiveEngine.obtenerNombreNivel(adaptiveResult.nivelCalculado)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AppColors.Purple
                                )
                            }
                            Text(
                                text = adaptiveResult.razon,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.Gray800
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isSavingReport) {
                        CircularProgressIndicator(color = AppColors.Purple)
                    } else {
                        Button(
                            onClick = {
                                isSavingReport = true
                                scope.launch {
                                    val now = Date()
                                    val fFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    val hFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                    
                                    val skillsDom = student.skills.filter { it.value >= 75 }.keys.toList()
                                    val skillsRef = student.skills.filter { it.value < 75 }.keys.toList()
                                    
                                    val reporte = ReporteSesion(
                                        id = "",
                                        estudianteId = student.pin,
                                        estudianteNombre = "${student.name} ${student.lastName}",
                                        grado = student.grade,
                                        seccion = student.seccion,
                                        docenteId = "", // assigned dynamically in repository
                                        padreEmail = student.padreEmail, // we map parent email
                                        tipo = "Práctica",
                                        preguntasCount = questionsAnsweredInSession,
                                        correctas = sessionCorrectAnswers,
                                        incorrectas = sessionIncorrectAnswers,
                                        tiempoPromedio = if (questionsAnsweredInSession > 0) sessionTimeSpent.toDouble() / questionsAnsweredInSession else 0.0,
                                        nivel = student.level,
                                        competenciasDominadas = skillsDom,
                                        competenciasReforzar = skillsRef,
                                        recomendaciones = student.recomendaciones,
                                        estrellasObtenidas = totalStarsGained,
                                        fecha = fFormat.format(now),
                                        timestamp = now.time
                                    )
                                    
                                    viewModel.guardarReporteSesion(reporte) {
                                        // Auto update topic difficulty level in Firestore & ViewModel
                                        viewModel.actualizarDificultadTema(student.pin, operation, adaptiveResult.nivelCalculado)
                                        
                                        // Also trigger alerts
                                        viewModel.notificarPadre(student.pin, student.name, if (questionsAnsweredInSession > 0) (sessionCorrectAnswers * 100) / questionsAnsweredInSession else 0)
                                        viewModel.notificarDocente(student.pin, student.name, if (questionsAnsweredInSession > 0) (sessionCorrectAnswers * 100) / questionsAnsweredInSession else 0)
                                        
                                        isSavingReport = false
                                        onBack()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple)
                        ) {
                            Text("Guardar y Volver", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    } else {
        // ACTIVE PLAY SCREEN
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gameBgColor)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = headerColor) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(student.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.Gray800)
                        Text("Pregunta: ${questionsAnsweredInSession + 1}/10", fontSize = 12.sp, color = AppColors.Gray500, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AppColors.Purple,
                            modifier = Modifier.size(18.dp)
                        )
                        Text("${student.stars + totalStarsGained}", color = AppColors.Purple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                // Progress Bar
                LinearProgressIndicator(
                    progress = { questionsAnsweredInSession / 10f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = headerColor,
                    trackColor = AppColors.Gray200
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = headerColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Tiempo: ${currentTime}s",
                        fontSize = 16.sp,
                        color = headerColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (currentStreak > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AppColors.Amber),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Racha: $currentStreak",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = questionText, fontSize = 26.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
                        Text(
                            text = "= ${userAnswer.ifEmpty { "?" }}",
                            fontSize = 40.sp,
                            color = if (userAnswer.isEmpty()) AppColors.Gray400 else AppColors.Purple,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .offset(x = shakeOffset.dp)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    AjoloteMascotView(
                        mascotState = mascotState,
                        message = msg,
                        size = 80.dp
                    )
                }

                val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", "OK")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    keys.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { key ->
                                Button(
                                    onClick = {
                                        when (key) {
                                            "C" -> userAnswer = ""
                                            "OK" -> {
                                                val ansVal = userAnswer.toIntOrNull()
                                                val timeSpent = (System.currentTimeMillis() - currentQuestionStart) / 1000
                                                sessionTimeSpent += timeSpent
                                                
                                                val isCorrect = ansVal == correctAnswer
                                                
                                                // Create history record
                                                val fFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                                val hFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                                                val now = Date()
                                                
                                                val diff = when (operation) {
                                                    "Suma", "Resta" -> "Fácil"
                                                    "Multiplicacion" -> "Medio"
                                                    else -> "Difícil"
                                                }
                                                
                                                val starsGained = if (isCorrect) (if (currentStreak >= 1) 15 else 10) else 0
                                                val xpGained = if (isCorrect) (if (currentStreak >= 1) 15 else 10) else 0

                                                val log = RespuestaHistorial(
                                                    id = "",
                                                    estudianteId = student.pin,
                                                    estudianteNombre = "${student.name} ${student.lastName}",
                                                    grado = student.grade,
                                                    seccion = student.seccion,
                                                    docenteAsignado = student.docenteAsignado,
                                                    padreAsociado = student.padreEmail, // maps to email
                                                    fecha = fFormat.format(now),
                                                    hora = hFormat.format(now),
                                                    timestamp = now.time,
                                                    operacion = operation,
                                                    pregunta = questionText,
                                                    respuestaCorrecta = correctAnswer.toString(),
                                                    respuestaElegida = userAnswer,
                                                    tiempoRespuesta = timeSpent,
                                                    dificultad = diff,
                                                    correcta = isCorrect,
                                                    intentos = attemptsCount,
                                                    estrellasObtenidas = starsGained,
                                                    experienciaGanada = xpGained,
                                                    dispositivo = "${Build.MANUFACTURER} ${Build.MODEL}"
                                                )
                                                
                                                viewModel.registrarRespuestaHistorial(log)

                                                if (isCorrect) {
                                                    totalStarsGained += starsGained
                                                    totalXPGained += xpGained
                                                    
                                                    // Add score to VM for stats updates
                                                    onScore(starsGained, xpGained, operation, timeSpent)
                                                    
                                                    sessionCorrectAnswers++
                                                    questionsAnsweredInSession++
                                                    currentStreak++
                                                    incorrectasConsecutivas = 0
                                                    showHintDialog = false
                                                    activeHintResponse = null
                                                    mascotState = MascotState.CORRECT
                                                    msg = "¡Excelente! +$starsGained estrellas | +$xpGained XP"
                                                    
                                                    if (questionsAnsweredInSession >= 10) {
                                                        showSummary = true
                                                    } else {
                                                        generateNewQuestion()
                                                        userAnswer = ""
                                                        currentQuestionStart = System.currentTimeMillis()
                                                    }

                                                    showConfetti = true
                                                    scope.launch {
                                                        delay(1200)
                                                        showConfetti = false
                                                    }
                                                } else {
                                                    mascotState = MascotState.INCORRECT
                                                    sessionIncorrectAnswers++
                                                    incorrectasConsecutivas++
                                                    if (userAnswer.isNotEmpty()) {
                                                        listaDeIntentos.add(userAnswer)
                                                    }
                                                    val hintPedagogica = when (operation) {
                                                        "Division", "División" -> "Pista: Multiplica divisor × cociente o revisa el residuo."
                                                        "Multiplicacion", "Multiplicación" -> "Pista: Suma el número repetidamente según el multiplicador."
                                                        "Resta" -> "Pista: Si el número de arriba es menor, pide prestado a la decena."
                                                        "Fracciones" -> "Pista: El numerador es la parte superior y el denominador el total."
                                                        "Series" -> "Pista: Fíjate si la serie aumenta o disminuye de 2 en 2 o de 5 en 5."
                                                        else -> "Pista: Descompón los números en decenas y unidades."
                                                    }
                                                    msg = "¡Casi! $hintPedagogica"
                                                    currentStreak = 0
                                                    val lastAnswer = userAnswer
                                                    userAnswer = ""
                                                    shakeTrigger = true
                                                    
                                                    if (viewModel.shouldRequestHint(attemptsCount, incorrectasConsecutivas)) {
                                                        isLoadingHint = true
                                                        val hintReq = HintRequest(
                                                            edad = if (student.edad > 0) student.edad else 7,
                                                            idioma = "es",
                                                            tema = operation,
                                                            enunciado = questionText,
                                                            respuestaCorrecta = correctAnswer.toString(),
                                                            listaDeIntentos = listaDeIntentos.toList(),
                                                            intentosFallidos = incorrectasConsecutivas,
                                                            dificultad = diff,
                                                            studentId = student.pin
                                                        )
                                                        viewModel.obtenerPistaAdaptativa(hintReq) { resp ->
                                                            activeHintResponse = resp
                                                            isLoadingHint = false
                                                            showHintDialog = true
                                                        }
                                                    }
                                                    attemptsCount++
                                                }

                                            }
                                            else -> if (userAnswer.length < 3) userAnswer += key
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(60.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when (key) {
                                            "OK" -> AppColors.Green
                                            "C" -> AppColors.Red
                                            else -> Color.White
                                        },
                                        contentColor = if (key == "OK" || key == "C") Color.White else AppColors.Gray800
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    border = if (key != "OK" && key != "C") BorderStroke(1.5.dp, AppColors.Gray200) else null
                                ) {
                                    Text(key, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // MathIA AI Tutor Hint Speech Bubble Dialog Overlay
            if (showHintDialog || isLoadingHint) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(AppColors.PurpleLight)
                                            .border(2.dp, AppColors.Purple, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = tutorImageRes),
                                            contentDescription = tutorName,
                                            modifier = Modifier.size(46.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = tutorName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = AppColors.Purple
                                        )
                                        Text(
                                            text = "Tutor de IA MathIA",
                                            fontSize = 11.sp,
                                            color = AppColors.Gray500
                                        )
                                    }
                                }

                                activeHintResponse?.let { hint ->
                                    val (badgeBg, badgeText) = when (hint.nivelPista) {
                                        "directa" -> Pair(Color(0xFFFFEBEE), "Pista Directa")
                                        "moderada" -> Pair(Color(0xFFFFF3E0), "Pista Moderada")
                                        else -> Pair(Color(0xFFE8F5E9), "Pista Leve")
                                    }
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = badgeBg),
                                        shape = RoundedCornerShape(50.dp)
                                    ) {
                                        Text(
                                            text = badgeText,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.Gray800
                                        )
                                    }
                                }
                            }

                            Divider(color = AppColors.Gray200)

                            if (isLoadingHint) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                ) {
                                    CircularProgressIndicator(color = AppColors.Purple)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "$tutorName está preparando una pista pedagógica...",
                                        fontSize = 13.sp,
                                        color = AppColors.Gray600,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                activeHintResponse?.let { hint ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                                        shape = RoundedCornerShape(18.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Lightbulb,
                                                    contentDescription = null,
                                                    tint = AppColors.Amber,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Pista Adaptativa:",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = AppColors.Purple
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = hint.pista,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = AppColors.MathiaNavy
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "“${hint.tonoAnimo}”",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppColors.Pink
                                            )

                                            if (hint.isFallback) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "⚡ Pista rápida de respaldo local",
                                                    fontSize = 10.sp,
                                                    color = AppColors.Gray500
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = { showHintDialog = false },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple)
                                    ) {
                                        Text("¡Entendido! Lo intentaré", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showConfetti) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    confettiList.forEach { (pos, size, color) ->
                        drawCircle(color = color, radius = size, center = pos)
                    }
                }
            }
        }
    }
}

