package com.example.mathia.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathia.AppColors
import com.example.mathia.R
import com.example.mathia.StudentViewModel
import com.example.mathia.model.DuelMatch
import com.example.mathia.model.DuelQuestionItem
import com.example.mathia.model.DuelStatus
import com.example.mathia.model.FirebaseStudent
import com.example.mathia.model.Student
import com.example.mathia.ui.components.AvatarIcon
import com.example.mathia.ui.components.NicknameSetupDialog

@Composable
fun DuelsScreen(
    currentStudent: Student,
    onBack: () -> Unit,
    onStartDuel: (String, String) -> Unit = { _, _ -> },
    viewModel: StudentViewModel
) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("mathia_prefs", Context.MODE_PRIVATE) }
    val studentPinStr = currentStudent.pin.toString()
    val nicknamePromptKey = "nickname_prompt_handled_${studentPinStr}"

    var showNicknameDialog by remember { mutableStateOf(false) }
    var showSearchOpponentDialog by remember { mutableStateOf(false) }

    val duelosList by viewModel.duelos.collectAsState()
    var classmates by remember { mutableStateOf<List<FirebaseStudent>>(emptyList()) }
    var selectedTopic by remember { mutableStateOf("Divisiones") }
    var activeTab by remember { mutableStateOf("COMPAÑEROS") } // "COMPAÑEROS", "MIS DUELOS", "HISTORIAL"
    var selectedOpponent by remember { mutableStateOf<FirebaseStudent?>(null) }
    var showChallengeConfirmDialog by remember { mutableStateOf(false) }

    // Active Gameplay State
    var activeGameplayDuel by remember { mutableStateOf<DuelMatch?>(null) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var currentScore by remember { mutableStateOf(0) }
    var totalTimeSeconds by remember { mutableStateOf(0L) }
    var selectedOptionText by remember { mutableStateOf<String?>(null) }
    var finishedDuelResult by remember { mutableStateOf<DuelMatch?>(null) }

    val topics = listOf("Sumas", "Restas", "Multiplicación", "Divisiones", "Fracciones", "Series", "Mixto")

    // Show nickname prompt ONCE if student has no nickname and hasn't dismissed/handled it yet
    LaunchedEffect(currentStudent.apodo) {
        if (currentStudent.apodo.isBlank() && !sharedPref.getBoolean(nicknamePromptKey, false)) {
            showNicknameDialog = true
        }
    }

    // Listen to duels in real time
    LaunchedEffect(studentPinStr) {
        if (studentPinStr.isNotEmpty()) {
            viewModel.iniciarEscuchaDuelos(studentPinStr)
        }
    }

    // Load classmates from student's classroom
    LaunchedEffect(currentStudent) {
        val classId = currentStudent.aulas.firstOrNull() ?: currentStudent.classroom
        if (classId.isNotBlank()) {
            viewModel.obtenerEstudiantesDeAula(classId) { list ->
                classmates = list.filter { it.pin.toString() != studentPinStr && it.apodo.isNotBlank() }
            }
        }
    }

    if (showNicknameDialog) {
        NicknameSetupDialog(
            currentNickname = currentStudent.apodo,
            onSaveNickname = { newApodo: String, callback: (Boolean) -> Unit ->
                viewModel.validarYGuardarApodo(studentPinStr, newApodo) { success, _ ->
                    if (success) {
                        sharedPref.edit().putBoolean(nicknamePromptKey, true).apply()
                        showNicknameDialog = false
                    }
                    callback(success)
                }
            },
            onDismiss = {
                sharedPref.edit().putBoolean(nicknamePromptKey, true).apply()
                showNicknameDialog = false
            }
        )
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = AppColors.MathiaPurple)
                    }
                }
                Icon(
                    imageVector = Icons.Default.SportsKabaddi,
                    contentDescription = null,
                    tint = AppColors.MathiaPurple,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "DUELOS MATEMÁTICOS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = AppColors.Gray800
                )
            }

            Spacer(Modifier.height(12.dp))

            // REQUISITO DE APODO: Si no tiene apodo, mostrar alerta amigable
            if (currentStudent.apodo.isBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, AppColors.MathiaPurple)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = AppColors.MathiaPurple, modifier = Modifier.size(48.dp))
                        Text(
                            text = "¡Primero crea tu apodo!",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = AppColors.MathiaPurple,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Para participar en los duelos matemáticos con tus compañeros necesitas un apodo público.",
                            fontSize = 13.sp,
                            color = AppColors.Gray600,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { showNicknameDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.MathiaPurple),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Crear mi apodo", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            } else {
                // Header Hero Matchup
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AppColors.MathiaPurple.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(AppColors.MathiaPurpleLight, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AvatarIcon(currentStudent.avatar, modifier = Modifier.size(36.dp), tint = AppColors.MathiaPurple)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = currentStudent.apodo,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AppColors.Gray800
                                )
                                Text("Nivel ${currentStudent.level}", fontSize = 11.sp, color = AppColors.MathiaPurple)
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = AppColors.MathiaGoldLight),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "VS",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = AppColors.MathiaGold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(AppColors.MathiaTealLight, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedOpponent != null) {
                                        AvatarIcon(selectedOpponent!!.avatar, modifier = Modifier.size(36.dp), tint = AppColors.MathiaTealDark)
                                    } else {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = AppColors.MathiaTealDark, modifier = Modifier.size(32.dp))
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = selectedOpponent?.apodo ?: "Compañero",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AppColors.Gray800
                                )
                                Text(
                                    text = if (selectedOpponent != null) "Nivel ${selectedOpponent!!.nivel_actual}" else "Selecciona",
                                    fontSize = 11.sp,
                                    color = AppColors.MathiaTealDark
                                )
                            }
                        }

                        // Topic Selector Chips
                        Text("Tema del Duelo:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.Gray600)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            topics.take(4).forEach { topic ->
                                FilterChip(
                                    selected = selectedTopic == topic,
                                    onClick = { selectedTopic = topic },
                                    label = { Text(topic, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AppColors.MathiaPurple,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Navigation Tabs
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.9f)
                ) {
                    TabRow(
                        selectedTabIndex = when (activeTab) {
                            "MIS DUELOS" -> 1
                            "HISTORIAL" -> 2
                            else -> 0
                        },
                        containerColor = Color.Transparent
                    ) {
                        Tab(
                            selected = activeTab == "COMPAÑEROS",
                            onClick = { activeTab = "COMPAÑEROS" },
                            text = { Text("Compañeros", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                        )
                        Tab(
                            selected = activeTab == "MIS DUELOS",
                            onClick = { activeTab = "MIS DUELOS" },
                            text = {
                                val pendingCount = duelosList.count { it.status == DuelStatus.PENDING && it.challengedId == studentPinStr }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Mis Duelos", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    if (pendingCount > 0) {
                                        Badge(containerColor = AppColors.Coral) { Text("$pendingCount") }
                                    }
                                }
                            }
                        )
                        Tab(
                            selected = activeTab == "HISTORIAL",
                            onClick = { activeTab = "HISTORIAL" },
                            text = { Text("Historial", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Tab Content List
                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
                ) {
                    Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        when (activeTab) {
                            "COMPAÑEROS" -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Search Opponent By Nickname Header Card
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 10.dp)
                                            .clickable {
                                                if (currentStudent.apodo.isBlank()) {
                                                    showNicknameDialog = true
                                                } else {
                                                    showSearchOpponentDialog = true
                                                }
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = AppColors.MathiaPurple),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(Icons.Default.PersonSearch, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Seleccionar compañero", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color.White)
                                                Text("Busca a un contrincante escribiendo su apodo público", fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                                            }
                                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                                        }
                                    }

                                    if (classmates.isEmpty()) {
                                        Column(
                                            modifier = Modifier.fillMaxSize().weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Image(painterResource(R.drawable.ajolote_default), null, modifier = Modifier.size(64.dp))
                                            Spacer(Modifier.height(8.dp))
                                            Text("No hay otros compañeros en tu aula aún.", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.Gray700)
                                            Text("Usa la búsqueda por apodo arriba o pide el código de aula.", fontSize = 11.sp, color = AppColors.Gray500)
                                        }
                                    } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(classmates) { classmate ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = CardDefaults.cardColors(containerColor = AppColors.MathiaPurpleLight.copy(alpha = 0.3f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                        Box(
                                                            modifier = Modifier.size(40.dp).background(AppColors.MathiaPurpleLight, CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            AvatarIcon(classmate.avatar, modifier = Modifier.size(28.dp), tint = AppColors.MathiaPurple)
                                                        }
                                                        Column {
                                                            Text(classmate.apodo, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.Gray800)
                                                            Text("Nivel ${classmate.nivel_actual} • ${classmate.estrellas} ★", fontSize = 11.sp, color = AppColors.Gray600)
                                                        }
                                                    }
                                                    Button(
                                                        onClick = {
                                                            selectedOpponent = classmate
                                                            showChallengeConfirmDialog = true
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.MathiaPurple),
                                                        shape = RoundedCornerShape(50.dp)
                                                    ) {
                                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text("Desafiar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "MIS DUELOS" -> {
                                val activeDuelos = duelosList.filter { it.status == DuelStatus.PENDING || it.status == DuelStatus.ACCEPTED || it.status == DuelStatus.IN_PROGRESS }
                                if (activeDuelos.isEmpty()) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Image(painterResource(R.drawable.ajolote_default), null, modifier = Modifier.size(64.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text("No tienes duelos activos en este momento.", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.Gray700)
                                        Text("¡Elige un compañero en la pestaña anterior para desafiarlo!", fontSize = 11.sp, color = AppColors.Gray500, textAlign = TextAlign.Center)
                                    }
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(activeDuelos) { duel ->
                                            val isChallenger = duel.challengerId == studentPinStr
                                            val rivalApodo = if (isChallenger) duel.challengedApodo else duel.challengerApodo

                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.MathiaPurpleLight)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                        Text("VS $rivalApodo", fontWeight = FontWeight.Black, fontSize = 14.sp, color = AppColors.MathiaPurple)
                                                        Text(duel.topic, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.Gray600)
                                                    }

                                                    if (duel.status == DuelStatus.PENDING) {
                                                        if (!isChallenger) {
                                                            Text("¡Te ha desafiado a una ronda de 10 preguntas!", fontSize = 12.sp, color = AppColors.Gray700)
                                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                Button(
                                                                    onClick = {
                                                                        viewModel.aceptarDesafioDuelo(duel.duelId) { ok ->
                                                                            if (ok) Toast.makeText(context, "¡Desafío Aceptado!", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.MathiaPurple),
                                                                    modifier = Modifier.weight(1f).height(38.dp)
                                                                ) {
                                                                    Text("Aceptar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                                }
                                                                OutlinedButton(
                                                                    onClick = { viewModel.rechazarDesafioDuelo(duel.duelId) {} },
                                                                    modifier = Modifier.weight(1f).height(38.dp)
                                                                ) {
                                                                    Text("Rechazar", fontSize = 11.sp, color = AppColors.Red)
                                                                }
                                                            }
                                                        } else {
                                                            Text("Esperando a que $rivalApodo acepte...", fontSize = 12.sp, color = AppColors.Gray600)
                                                            OutlinedButton(
                                                                onClick = { viewModel.cancelarDesafioDuelo(duel.duelId) {} },
                                                                modifier = Modifier.fillMaxWidth().height(36.dp)
                                                            ) {
                                                                Text("Cancelar Desafío", fontSize = 11.sp, color = AppColors.Red)
                                                            }
                                                        }
                                                    } else if (duel.status == DuelStatus.ACCEPTED || duel.status == DuelStatus.IN_PROGRESS) {
                                                        val hasPlayed = if (isChallenger) duel.challengerFinished else duel.challengedFinished
                                                        if (hasPlayed) {
                                                            Text("✓ Ya respondiste. Esperando el turno de $rivalApodo...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.MathiaTealDark)
                                                        } else {
                                                            Text("¡Es tu turno de responder las 10 preguntas!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.MathiaPurple)
                                                            Button(
                                                                onClick = {
                                                                    activeGameplayDuel = duel
                                                                    currentQuestionIndex = 0
                                                                    currentScore = 0
                                                                    totalTimeSeconds = 0L
                                                                },
                                                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.MathiaPurple),
                                                                modifier = Modifier.fillMaxWidth().height(40.dp)
                                                            ) {
                                                                Text("Jugar Turno Ahora →", fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                    }
                                }
                            }
                        }

                        "HISTORIAL" -> {
                                val completedDuelos = duelosList.filter { it.status == DuelStatus.COMPLETED || it.status == DuelStatus.DECLINED || it.status == DuelStatus.EXPIRED }
                                if (completedDuelos.isEmpty()) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Image(painterResource(R.drawable.ajolote_default), null, modifier = Modifier.size(64.dp))
                                        Spacer(Modifier.height(8.dp))
                                        Text("Aún no tienes historial de duelos.", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.Gray700)
                                    }
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(completedDuelos) { duel ->
                                            val isChallenger = duel.challengerId == studentPinStr
                                            val rivalApodo = if (isChallenger) duel.challengedApodo else duel.challengerApodo
                                            val isWinner = duel.winnerId == studentPinStr
                                            val isDraw = duel.winnerId == "EMPATE"

                                            val resultLabel = when {
                                                duel.status == DuelStatus.DECLINED -> "Rechazado"
                                                duel.status == DuelStatus.EXPIRED -> "Expirado"
                                                isDraw -> "Empate"
                                                isWinner -> "Victoria"
                                                else -> "Derrota"
                                            }
                                            val badgeColor = when (resultLabel) {
                                                "Victoria" -> AppColors.MathiaTealDark
                                                "Empate" -> AppColors.MathiaGold
                                                "Derrota" -> AppColors.Coral
                                                else -> AppColors.Gray500
                                            }

                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.White)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text("VS $rivalApodo", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.Gray800)
                                                        Text("Tema: ${duel.topic} • ${duel.challengerScore}/10 vs ${duel.challengedScore}/10", fontSize = 11.sp, color = AppColors.Gray600)
                                                    }
                                                    Surface(shape = RoundedCornerShape(50.dp), color = badgeColor.copy(alpha = 0.2f)) {
                                                        Text(resultLabel, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = badgeColor, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
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
            }
        }
    }

    // Dialog for searching opponent by nickname
    if (showSearchOpponentDialog) {
        var searchApodoInput by remember { mutableStateOf("") }
        var searchErrorMessage by remember { mutableStateOf<String?>(null) }
        var isSearching by remember { mutableStateOf(false) }
        var foundOpponent by remember { mutableStateOf<FirebaseStudent?>(null) }

        AlertDialog(
            onDismissRequest = { showSearchOpponentDialog = false },
            title = { Text("¿A quién quieres desafiar?", fontWeight = FontWeight.Black, fontSize = 18.sp, color = AppColors.MathiaPurple) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Escribe el apodo del compañero:", fontSize = 12.sp, color = AppColors.Gray700)
                    OutlinedTextField(
                        value = searchApodoInput,
                        onValueChange = { searchApodoInput = it; searchErrorMessage = null; foundOpponent = null },
                        placeholder = { Text("Ej. LunaMath") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.MathiaPurple,
                            unfocusedBorderColor = AppColors.BorderLight
                        )
                    )

                    searchErrorMessage?.let { err ->
                        Text(err, color = AppColors.Coral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (isSearching) {
                        CircularProgressIndicator(color = AppColors.MathiaPurple, modifier = Modifier.align(Alignment.CenterHorizontally).size(28.dp))
                    }

                    foundOpponent?.let { opp ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AppColors.MathiaPurpleLight.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(modifier = Modifier.size(44.dp).background(AppColors.MathiaPurpleLight, CircleShape), contentAlignment = Alignment.Center) {
                                    AvatarIcon(opp.avatar, modifier = Modifier.size(32.dp), tint = AppColors.MathiaPurple)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(opp.apodo, fontWeight = FontWeight.Black, fontSize = 15.sp, color = AppColors.Gray800)
                                    Text("Nivel ${opp.nivel_actual} • ${opp.estrellas} ★", fontSize = 12.sp, color = AppColors.MathiaPurple)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (foundOpponent != null) {
                    Button(
                        onClick = {
                            val opp = foundOpponent!!
                            showSearchOpponentDialog = false
                            selectedOpponent = opp
                            showChallengeConfirmDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.MathiaPurple)
                    ) {
                        Text("Desafiar", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            val query = searchApodoInput.trim()
                            if (query.isBlank()) {
                                searchErrorMessage = "Escribe un apodo para buscar."
                                return@Button
                            }
                            if (currentStudent.apodo.isBlank()) {
                                searchErrorMessage = "Primero crea tu apodo antes de desafiar."
                                return@Button
                            }
                            if (query.equals(currentStudent.apodo, ignoreCase = true)) {
                                searchErrorMessage = "No puedes desafiarte a ti mismo."
                                return@Button
                            }

                            isSearching = true
                            searchErrorMessage = null
                            viewModel.buscarEstudiantePorApodo(query) { opp ->
                                isSearching = false
                                if (opp == null) {
                                    searchErrorMessage = "No encontramos a ningún compañero con ese apodo."
                                } else if (opp.apodo.isBlank()) {
                                    searchErrorMessage = "Este estudiante no ha configurado un apodo."
                                } else if (opp.pin.toString() == studentPinStr) {
                                    searchErrorMessage = "No puedes desafiarte a ti mismo."
                                } else {
                                    foundOpponent = opp
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.MathiaPurple)
                    ) {
                        Text("Buscar", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSearchOpponentDialog = false }) {
                    Text("Cancelar", color = AppColors.Gray600)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    // Confirmation Dialog for Sending Challenge
    if (showChallengeConfirmDialog && selectedOpponent != null) {
        AlertDialog(
            onDismissRequest = { showChallengeConfirmDialog = false },
            title = { Text("¿Desafiar a ${selectedOpponent!!.apodo}?", fontWeight = FontWeight.Bold, color = AppColors.MathiaPurple) },
            text = { Text("Competirán en una ronda de 10 ejercicios de $selectedTopic. Cada uno responderá en su turno.", fontSize = 13.sp, color = AppColors.Gray700) },
            confirmButton = {
                Button(
                    onClick = {
                        showChallengeConfirmDialog = false
                        viewModel.crearDesafioDuelo(selectedOpponent!!, selectedTopic) { ok, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (ok) activeTab = "MIS DUELOS"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.MathiaPurple)
                ) {
                    Text("¡Desafiar!", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChallengeConfirmDialog = false }) {
                    Text("Cancelar", color = AppColors.Gray600)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    // Active Gameplay Overlay Modal
    activeGameplayDuel?.let { duel ->
        val questions = duel.questions
        if (questions.isNotEmpty() && currentQuestionIndex < questions.size) {
            val q = questions[currentQuestionIndex]
            val options = remember(q) { listOf(q.opcionA, q.opcionB, q.opcionC, q.opcionD).filter { it.isNotBlank() } }

            AlertDialog(
                onDismissRequest = { },
                title = {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Pregunta ${currentQuestionIndex + 1} de ${questions.size}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.MathiaPurple)
                        Text(duel.topic, fontSize = 12.sp, color = AppColors.Gray600)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AppColors.MathiaPurpleLight.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = q.enunciado,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = AppColors.Gray800,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            )
                        }

                        options.forEach { opt ->
                            Button(
                                onClick = {
                                    val isCorrect = opt.trim().equals(q.correcta.trim(), ignoreCase = true)
                                    if (isCorrect) currentScore++
                                    totalTimeSeconds += 5L // simulate response time
                                    viewModel.registrarRespuestaDuelo(duel.duelId, q.questionId, opt.trim(), isCorrect, 5L)

                                    if (currentQuestionIndex + 1 < questions.size) {
                                        currentQuestionIndex++
                                    } else {
                                        // Finished 10 questions!
                                        val finalScore = currentScore
                                        val finalTime = totalTimeSeconds
                                        val finishedDuel = duel
                                        activeGameplayDuel = null
                                        viewModel.guardarProgresoYFinalizarDuelo(finishedDuel.duelId, finalScore, finalTime) {
                                            finishedDuelResult = finishedDuel.copy(
                                                challengerScore = if (finishedDuel.challengerId == studentPinStr) finalScore else finishedDuel.challengerScore,
                                                challengedScore = if (finishedDuel.challengedId == studentPinStr) finalScore else finishedDuel.challengedScore
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.MathiaPurple)
                            ) {
                                Text(opt, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                },
                confirmButton = { },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White
            )
        }
    }

    // Result Overlay Modal
    finishedDuelResult?.let { duel ->
        AlertDialog(
            onDismissRequest = { finishedDuelResult = null },
            title = {
                Text("¡Duelo Terminado!", fontWeight = FontWeight.Black, fontSize = 20.sp, color = AppColors.MathiaPurple, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Image(
                        painter = painterResource(R.drawable.ajolote_correcto),
                        contentDescription = null,
                        modifier = Modifier.size(90.dp)
                    )
                    Text("Has respondido las 10 preguntas del duelo.", fontSize = 13.sp, color = AppColors.Gray700, textAlign = TextAlign.Center)
                    Text("Tus aciertos: $currentScore / 10", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = AppColors.MathiaPurple)
                    Text("Los resultados finales se actualizarán cuando ambos completen la sesión.", fontSize = 11.sp, color = AppColors.Gray600, textAlign = TextAlign.Center)
                }
            },
            confirmButton = {
                Button(
                    onClick = { finishedDuelResult = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.MathiaPurple),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("¡Entendido!", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }
}
