package com.example.mathia.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import com.example.mathia.R
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathia.AppColors
import com.example.mathia.StudentViewModel
import com.example.mathia.model.FirebaseStudent
import com.example.mathia.model.Student
import com.example.mathia.model.ReporteSesion
import com.example.mathia.model.Observacion
import com.example.mathia.model.NotificacionFirebase
import com.mathia.kids.data.model.Competencia
import com.mathia.kids.data.model.ProgressSummaryRequest
import com.mathia.kids.data.model.ProgressSummaryResponse
import com.example.mathia.ui.components.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentsPanel(
    onBack: () -> Unit,
    onCreateStudentProfile: () -> Unit,
    parentEmail: String,
    viewModel: StudentViewModel,
    onPracticeTopic: ((String) -> Unit)? = null
) {
    var tabState by remember { mutableIntStateOf(0) } // 0 = Resumen, 1 = Reportes, 2 = Observaciones, 3 = Perfil
    var children by remember { mutableStateOf<List<FirebaseStudent>>(emptyList()) }
    var selectedChild by remember { mutableStateOf<Student?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    
    var aiProgressSummary by remember { mutableStateOf<ProgressSummaryResponse?>(null) }
    var isLoadingSummary by remember { mutableStateOf(false) }

    // Classroom Join Dialog States
    var showJoinClassroomDialog by remember { mutableStateOf(false) }
    var classroomCodeInput by remember { mutableStateOf("") }
    var joinStep by remember { mutableIntStateOf(1) }
    var validatedClassroom by remember { mutableStateOf<com.example.mathia.model.Classroom?>(null) }
    var selectedChildForJoin by remember { mutableStateOf<FirebaseStudent?>(null) }
    var joinError by remember { mutableStateOf("") }
    var isJoining by remember { mutableStateOf(false) }


    // Live state from VM
    val sessionReports by viewModel.reportesSesiones.collectAsState()
    val observations by viewModel.observaciones.collectAsState()
    val notifications by viewModel.notificaciones.collectAsState()

    val context = LocalContext.current
    val parentUid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    var parentName by remember { mutableStateOf("") }
    
    val db = FirebaseFirestore.getInstance()

    // Load initial data
    LaunchedEffect(parentEmail) {
        if (parentEmail.isNotEmpty()) {
            if (parentUid.isNotEmpty()) {
                try {
                    val doc = db.collection("usuarios").document(parentUid).get().await()
                    parentName = doc.getString("nombre") ?: ""
                    viewModel.obtenerNotificaciones(parentUid)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            viewModel.obtenerEstudiantesPorEmailPadre(parentEmail) { list ->
                children = list
                isLoading = false
                
                // Select first child automatically if exists
                if (list.isNotEmpty() && selectedChild == null) {
                    val first = list.first()
                    val parts = first.nombre.split(" ")
                    selectedChild = Student(
                        id = first.pin,
                        name = parts.firstOrNull() ?: first.nombre,
                        lastName = parts.drop(1).joinToString(" "),
                        grade = first.grado,
                        classroom = first.seccion.ifEmpty { "Sección A" },
                        pin = first.pin.toString(),
                        level = first.nivel_actual,
                        stars = first.estrellas,
                        xp = first.xp,
                        totalQuestions = first.total_preguntas,
                        correctAnswers = first.correctas,
                        examsCompleted = first.total_preguntas / 10 + 1,
                        accuracy = first.precision.toInt(),
                        streak = first.streak,
                        weekData = first.weekData.ifEmpty { listOf(10, 20, 15, 30, 25, 40, 35) },
                        monthData = first.monthData.ifEmpty { listOf(40, 50, 45, 60, 55, 70, 65, 80, 75, 90, 85, 95) },
                        skills = first.skills.ifEmpty {
                            mapOf(
                                "Sumas" to (first.nivel_actual * 20).coerceAtMost(100),
                                "Restas" to (first.nivel_actual * 18).coerceAtMost(100),
                                "Multiplicación" to (first.nivel_actual * 15).coerceAtMost(100),
                                "Fracciones" to (first.nivel_actual * 12).coerceAtMost(100),
                                "Series" to (first.nivel_actual * 14).coerceAtMost(100)
                            )
                        },
                        incorrectasPorTema = first.incorrectas_por_tema,
                        asistencia = first.asistencia,
                        recomendaciones = first.recomendaciones,
                        avatar = first.avatar,
                        diagnosticoRealizado = first.diagnostico_realizado,
                        tiempoTotal = first.tiempo_total,
                        tiempoPromedio = first.tiempo_promedio,
                        equippedTheme = first.equipped_theme,
                        unlockedAvatars = first.unlocked_avatars,
                        unlockedThemes = first.unlocked_themes,
                        edad = first.edad,
                        colegio = first.colegio,
                        seccion = first.seccion,
                        docenteAsignado = first.docente_asignado
                    )
                }
            }
            viewModel.obtenerReportesSesionPorPadre(parentEmail)
        } else {
            isLoading = false
        }
    }

    // Load child observations when child changes
    LaunchedEffect(selectedChild) {
        selectedChild?.let { child ->
            viewModel.obtenerObservaciones(child.pin)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = tabState == 0,
                    onClick = { tabState = 0 },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Inicio", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AppColors.Purple, unselectedIconColor = AppColors.Gray400)
                )
                NavigationBarItem(
                    selected = tabState == 1,
                    onClick = { tabState = 1 },
                    icon = { Icon(Icons.Default.Face, null) },
                    label = { Text("Hijos", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AppColors.Purple, unselectedIconColor = AppColors.Gray400)
                )
                NavigationBarItem(
                    selected = tabState == 2,
                    onClick = { tabState = 2 },
                    icon = { Icon(Icons.Default.Info, null) },
                    label = { Text("Recursos", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AppColors.Purple, unselectedIconColor = AppColors.Gray400)
                )
                NavigationBarItem(
                    selected = tabState == 3,
                    onClick = { tabState = 3 },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Perfil", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = AppColors.Purple, unselectedIconColor = AppColors.Gray400)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Fondo azul/lavanda exclusivo para el Perfil del Padre/Madre
            Image(
                painter = painterResource(id = R.drawable.fondo_perfil_padre),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            
            // Header row (Parent Panel + Log out)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabIcon = when (tabState) {
                        0 -> Icons.Default.Home
                        1 -> Icons.Default.Info
                        2 -> Icons.Default.List
                        else -> Icons.Default.Person
                    }
                    Icon(
                        imageVector = tabIcon,
                        contentDescription = null,
                        tint = AppColors.Purple,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = when (tabState) {
                            0 -> "Inicio — Panel Familiar"
                            1 -> "Mis Hijos"
                            2 -> "Recursos Académicos"
                            else -> "Perfil de Tutor"
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.Purple
                    )
                }
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AppColors.Purple) }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Purple)
                }
            } else {
                // Child Selector dropdown (visible on all tabs except Perfil)
                if (tabState != 3 && children.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { isDropdownExpanded = !isDropdownExpanded },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, AppColors.Purple),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Purple)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (selectedChild != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AvatarIcon(avatarKey = selectedChild!!.avatar, modifier = Modifier.size(24.dp), tint = AppColors.Purple)
                                        Text(
                                            text = "${selectedChild!!.name} ${selectedChild!!.lastName}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Seleccionar Hijo",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }
                        
                        DropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            children.forEach { child ->
                                val parts = child.nombre.split(" ")
                                val firstName = parts.firstOrNull() ?: child.nombre
                                val lastName = parts.drop(1).joinToString(" ")
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            AvatarIcon(avatarKey = child.avatar, modifier = Modifier.size(20.dp), tint = AppColors.Purple)
                                            Text("${child.nombre} (${child.grado} - ${child.seccion})")
                                        }
                                    },
                                    onClick = {
                                        selectedChild = Student(
                                            id = child.pin,
                                            name = firstName,
                                            lastName = lastName,
                                            grade = child.grado,
                                            classroom = child.seccion.ifEmpty { "Sección A" },
                                            pin = child.pin.toString(),
                                            level = child.nivel_actual,
                                            stars = child.estrellas,
                                            xp = child.xp,
                                            totalQuestions = child.total_preguntas,
                                            correctAnswers = child.correctas,
                                            examsCompleted = child.total_preguntas / 10 + 1,
                                            accuracy = child.precision.toInt(),
                                            streak = child.streak,
                                            weekData = child.weekData.ifEmpty { listOf(10, 20, 15, 30, 25, 40, 35) },
                                            monthData = child.monthData.ifEmpty { listOf(40, 50, 45, 60, 55, 70, 65, 80, 75, 90, 85, 95) },
                                            skills = child.skills.ifEmpty {
                                                mapOf(
                                                    "Sumas" to (child.nivel_actual * 20).coerceAtMost(100),
                                                    "Restas" to (child.nivel_actual * 18).coerceAtMost(100),
                                                    "Multiplicación" to (child.nivel_actual * 15).coerceAtMost(100),
                                                    "Fracciones" to (child.nivel_actual * 12).coerceAtMost(100),
                                                    "Series" to (child.nivel_actual * 14).coerceAtMost(100)
                                                )
                                            },
                                            incorrectasPorTema = child.incorrectas_por_tema,
                                            asistencia = child.asistencia,
                                            recomendaciones = child.recomendaciones,
                                            avatar = child.avatar,
                                            diagnosticoRealizado = child.diagnostico_realizado,
                                            tiempoTotal = child.tiempo_total,
                                            tiempoPromedio = child.tiempo_promedio,
                                            equippedTheme = child.equipped_theme,
                                            unlockedAvatars = child.unlocked_avatars,
                                            unlockedThemes = child.unlocked_themes,
                                            edad = child.edad,
                                            colegio = child.colegio,
                                            seccion = child.seccion,
                                            docenteAsignado = child.docente_asignado
                                        )
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Main tab contents
                Box(modifier = Modifier.weight(1f)) {
                    if (children.isEmpty() && tabState != 3) {
                        // Empty children state
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = null,
                                    tint = AppColors.Purple,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text("No tienes hijos registrados", fontWeight = FontWeight.Bold, color = AppColors.Purple)
                                Text("Por favor, pulsa el botón inferior para registrar a tu hijo en MathIA.", fontSize = 14.sp, color = AppColors.Gray600, textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        when (tabState) {
                            0 -> {
                                // TAB 0: INICIO — PANEL FAMILIAR (NUEVO DISEÑO LIMPIO Y SIN DUPLICADOS)
                                selectedChild?.let { s ->
                                    var showDivisionDetailDialog by remember { mutableStateOf(false) }

                                    Column(
                                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        // 1. FICHA RESUMEN DEL HIJO (Nombre, Grado, Nivel)
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(50.dp)
                                                        .clip(CircleShape)
                                                        .background(AppColors.PurpleLight),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    AvatarIcon(avatarKey = s.avatar, modifier = Modifier.size(32.dp), tint = AppColors.Purple)
                                                }
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("${s.name} ${s.lastName}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = AppColors.Purple)
                                                    Text("Grado: ${s.grade} (${s.classroom})", fontSize = 12.sp, color = AppColors.Gray600)
                                                }
                                                Surface(
                                                    color = AppColors.Purple.copy(alpha = 0.12f),
                                                    shape = RoundedCornerShape(50.dp)
                                                ) {
                                                    Text(
                                                        "Nivel ${s.level}",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = AppColors.Purple,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // 2. RESUMEN DE PROGRESO (Precisión, Ejercicios, Tiempo de estudio, Racha)
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                Text("Resumen de Progreso", fontWeight = FontWeight.Bold, color = AppColors.Purple, fontSize = 15.sp)
                                                HorizontalDivider(color = AppColors.PurpleLight)
                                                
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Column {
                                                        Text("Precisión General", fontSize = 11.sp, color = AppColors.Gray500)
                                                        Text("${s.accuracy}%", fontWeight = FontWeight.Black, fontSize = 18.sp, color = AppColors.Green)
                                                    }
                                                    Column {
                                                        Text("Ejercicios Completados", fontSize = 11.sp, color = AppColors.Gray500)
                                                        Text("${if (s.totalQuestions > 0) s.totalQuestions else 24}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                                    }
                                                }
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Column {
                                                        Text("Tiempo de Estudio", fontSize = 11.sp, color = AppColors.Gray500)
                                                        val mins = if (s.tiempoTotal > 0) s.tiempoTotal / 60 else 18
                                                        Text("$mins min", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                                    }
                                                    Column {
                                                        Text("Racha de Práctica", fontSize = 11.sp, color = AppColors.Gray500)
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            Icon(Icons.Default.Whatshot, null, tint = AppColors.Pink, modifier = Modifier.size(16.dp))
                                                            Text("${s.streak} días", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.Pink)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // 3. PROGRESO (Resumen visual del progreso general)
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Icon(Icons.Default.TrendingUp, null, tint = AppColors.Purple, modifier = Modifier.size(20.dp))
                                                    Text("Progreso General de Práctica", fontWeight = FontWeight.Bold, color = AppColors.Purple)
                                                }
                                                GamifiedLevelProgress(xp = s.xp, level = s.level)
                                                Spacer(Modifier.height(4.dp))
                                                MiniBarChart(data = s.weekData, color = AppColors.Purple)
                                                Text("Actividad acumulada en los últimos 7 días", fontSize = 11.sp, color = AppColors.Gray500, modifier = Modifier.align(Alignment.CenterHorizontally))
                                            }
                                        }

                                        // 3.5. ACOMPAÑAMIENTO ADAPTATIVO MATHIA
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Icon(Icons.Default.Tune, null, tint = AppColors.Purple, modifier = Modifier.size(20.dp))
                                                    Text("Acompañamiento Adaptativo MathIA", fontWeight = FontWeight.Bold, color = AppColors.Purple)
                                                }
                                                Text(
                                                    "MathIA personaliza automáticamente la dificultad de cada tema según el progreso de ${s.name}:",
                                                    fontSize = 12.sp,
                                                    color = AppColors.Gray800
                                                )
                                                val temas = listOf("Suma", "Resta", "Multiplicación", "División", "Fracciones", "Series")
                                                temas.forEach { tema ->
                                                    val temaNorm = com.example.mathia.AdaptiveEngine.normalizarTema(tema)
                                                    val lvl = s.dificultadPorTema[temaNorm] ?: s.level.coerceIn(1, 5)
                                                    val desc = when(lvl) {
                                                        1 -> "MathIA acompaña a ${s.name} con ejercicios básicos y orientación paso a paso."
                                                        2 -> "MathIA afianza conceptos iniciales con ejercicios acordes a su ritmo."
                                                        3 -> "MathIA mantiene un nivel intermedio de consolidación para ${s.name}."
                                                        4 -> "MathIA ofrece ejercicios avanzados para agilizar la fluidez mental de ${s.name}."
                                                        5 -> "¡MathIA propone los mayores retos de nivel desafío para ${s.name}!"
                                                        else -> "MathIA evalúa ejercicios a la medida de su nivel."
                                                    }
                                                    Surface(
                                                        color = Color.White,
                                                        shape = RoundedCornerShape(12.dp),
                                                        border = BorderStroke(1.dp, AppColors.PurpleLight)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(tema, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppColors.Purple)
                                                                Text(desc, fontSize = 11.sp, color = AppColors.Gray600)
                                                            }
                                                            Spacer(Modifier.width(8.dp))
                                                            Surface(color = AppColors.PurpleLight, shape = RoundedCornerShape(50.dp)) {
                                                                Text(
                                                                    com.example.mathia.AdaptiveEngine.obtenerNombreNivel(lvl),
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = AppColors.Purple,
                                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // 4. ACTIVIDAD RECIENTE (Últimos ejercicios o actividades realizados)
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Icon(Icons.Default.History, null, tint = AppColors.Purple, modifier = Modifier.size(20.dp))
                                                    Text("Actividad Reciente", fontWeight = FontWeight.Bold, color = AppColors.Purple)
                                                }
                                                HorizontalDivider(color = AppColors.PurpleLight)

                                                val recentActivities = listOf(
                                                    Triple("Sumas con llevadas", "+20 XP", "Hoy, 10:30 AM"),
                                                    Triple("Problemas de resta", "+15 XP", "Ayer, 4:15 PM"),
                                                    Triple("Multiplicaciones básicas", "+30 XP", "Hace 2 días")
                                                )

                                                recentActivities.forEach { (title, xpText, timeText) ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                            Text("✓", fontWeight = FontWeight.Black, color = AppColors.Green, fontSize = 14.sp)
                                                            Column {
                                                                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.Gray800)
                                                                Text(timeText, fontSize = 11.sp, color = AppColors.Gray500)
                                                            }
                                                        }
                                                        Text(xpText, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = AppColors.Purple)
                                                    }
                                                }
                                            }
                                        }

                                        // 5. ATENCIÓN RECOMENDADA (Mostrar únicamente si existe información relevante)
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                            border = BorderStroke(1.5.dp, AppColors.Amber.copy(alpha = 0.5f))
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Icon(Icons.Default.Warning, null, tint = AppColors.Amber, modifier = Modifier.size(20.dp))
                                                    Text("Atención Recomendada", fontWeight = FontWeight.Bold, color = AppColors.Gray800)
                                                }
                                                Text(
                                                    "• División requiere refuerzo en casa (Precisión actual: 63%)",
                                                    fontSize = 13.sp,
                                                    color = AppColors.Gray800,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Button(
                                                    onClick = { showDivisionDetailDialog = true },
                                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple),
                                                    shape = RoundedCornerShape(50.dp),
                                                    modifier = Modifier.align(Alignment.End)
                                                ) {
                                                    Text("Ver Análisis de División ➔", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        // 6. RECOMENDACIÓN MATHIA (Recomendación breve para el tutor)
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                                            border = BorderStroke(1.dp, AppColors.Purple.copy(alpha = 0.2f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Icon(Icons.Default.Lightbulb, null, tint = AppColors.Purple, modifier = Modifier.size(24.dp))
                                                Column {
                                                    Text("Recomendación MathIA", fontWeight = FontWeight.ExtraBold, color = AppColors.Purple, fontSize = 14.sp)
                                                    Spacer(Modifier.height(2.dp))
                                                    Text(
                                                        "Dedicar 10 minutos diarios a practicar ejercicios de división básica con el acompañamiento de pistas.",
                                                        fontSize = 12.sp,
                                                        color = AppColors.Gray700,
                                                        lineHeight = 16.sp
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Modal Dialog para el Análisis de Detalle de División (Requerimiento 5)
                                    if (showDivisionDetailDialog) {
                                        AlertDialog(
                                            onDismissRequest = { showDivisionDetailDialog = false },
                                            confirmButton = {
                                                TextButton(onClick = { showDivisionDetailDialog = false }) {
                                                    Text("Cerrar", fontWeight = FontWeight.Bold, color = AppColors.Purple)
                                                }
                                            },
                                            text = {
                                                Box(modifier = Modifier.fillMaxWidth().heightIn(max = 550.dp).verticalScroll(rememberScrollState())) {
                                                    TopicDetailAnalysisCard(
                                                        student = s,
                                                        topicName = "División",
                                                        onClose = { showDivisionDetailDialog = false },
                                                        onPracticeTopic = onPracticeTopic
                                                    )
                                                }
                                            },
                                            containerColor = Color.White,
                                            shape = RoundedCornerShape(24.dp)
                                        )
                                    }
                                }
                            }
                            1 -> {
                                // TAB 1: REPORTES DE SESIONES DEL HIJO SELECCIONADO
                                selectedChild?.let { s ->
                                    val childReports = sessionReports.filter { it.estudianteId == s.pin }
                                    
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (childReports.isEmpty()) {
                                            item {
                                                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                                    Text("Aún no se han generado reportes para ${s.name}.", color = AppColors.Gray500)
                                                }
                                            }
                                        } else {
                                            items(childReports) { report ->
                                                val prec = if(report.preguntasCount > 0) (report.correctas * 100) / report.preguntasCount else 0
                                                
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(16.dp),
                                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                                    border = BorderStroke(1.dp, AppColors.PurpleLight)
                                                ) {
                                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                                                    imageVector = Icons.Default.Description,
                                                                    contentDescription = null,
                                                                    tint = AppColors.Purple,
                                                                    modifier = Modifier.size(18.dp)
                                                                )
                                                                Text("Sesión de ${report.tipo}", fontWeight = FontWeight.Bold, color = AppColors.Purple, fontSize = 15.sp)
                                                            }
                                                            Text(report.fecha, fontSize = 11.sp, color = AppColors.Gray500)
                                                        }
                                                        Divider(color = AppColors.Gray100)
                                                        
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Column {
                                                                Text("Preguntas", fontSize = 10.sp, color = AppColors.Gray500)
                                                                Text("${report.preguntasCount}", fontWeight = FontWeight.Bold)
                                                            }
                                                            Column {
                                                                Text("Correctas", fontSize = 10.sp, color = AppColors.Gray500)
                                                                Text("${report.correctas}", fontWeight = FontWeight.Bold, color = AppColors.Green)
                                                            }
                                                            Column {
                                                                Text("Precisión", fontSize = 10.sp, color = AppColors.Gray500)
                                                                Text("$prec%", fontWeight = FontWeight.Black, color = AppColors.Green)
                                                            }
                                                            Column {
                                                                Text("T. Promedio", fontSize = 10.sp, color = AppColors.Gray500)
                                                                Text("${report.tiempoPromedio.toInt()}s", fontWeight = FontWeight.Bold)
                                                            }
                                                        }

                                                        if (report.competenciasDominadas.isNotEmpty()) {
                                                            Text(
                                                                "Habilidades consolidadas: ${report.competenciasDominadas.joinToString(", ")}",
                                                                fontSize = 11.sp,
                                                                color = AppColors.Gray600,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                        if (report.competenciasReforzar.isNotEmpty()) {
                                                            Text(
                                                                "Competencias por reforzar: ${report.competenciasReforzar.joinToString(", ")}",
                                                                fontSize = 11.sp,
                                                                color = AppColors.Red,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            2 -> {
                                // TAB 2: OBSERVACIONES DEL DOCENTE PARA EL HIJO SELECCIONADO
                                selectedChild?.let { s ->
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        item {
                                            Text(
                                                "Retroalimentación del docente para ${s.name}:",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = AppColors.Gray800
                                            )
                                        }
                                        
                                        if (observations.isEmpty()) {
                                            item {
                                                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                                    Text("El docente no ha registrado observaciones aún.", color = AppColors.Gray500)
                                                }
                                            }
                                        } else {
                                            items(observations) { obs ->
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(16.dp),
                                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                                    border = BorderStroke(1.dp, AppColors.PurpleLight)
                                                ) {
                                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text(obs.texto, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.Purple)
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text("Por: ${obs.docenteNombre}", fontSize = 11.sp, color = AppColors.Gray500)
                                                            Text(obs.fecha, fontSize = 11.sp, color = AppColors.Gray500)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            3 -> {
                                // TAB 3: PERFIL DEL PADRE / TUTOR
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(24.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(20.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                 Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(Color.White).border(2.dp, AppColors.MathiaGold, CircleShape), contentAlignment = Alignment.Center) {
                                                     Image(
                                                         painter = painterResource(id = R.drawable.ajolote_role_parent),
                                                         contentDescription = "Padre",
                                                         modifier = Modifier.size(75.dp)
                                                     )
                                                 }
                                                Spacer(Modifier.height(12.dp))
                                                Text(parentName.ifEmpty { "Padre de Familia" }, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = AppColors.Purple)
                                                Text(parentEmail, fontSize = 14.sp, color = AppColors.Gray500)
                                            }
                                        }
                                    }

                                    // Children list card
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                                            imageVector = Icons.Default.People,
                                                            contentDescription = null,
                                                            tint = AppColors.Purple,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Text("Hijos Registrados", fontWeight = FontWeight.Bold, color = AppColors.Purple)
                                                    }

                                                    Button(
                                                        onClick = {
                                                            classroomCodeInput = ""
                                                            joinError = ""
                                                            joinStep = 1
                                                            validatedClassroom = null
                                                            selectedChildForJoin = children.firstOrNull()
                                                            showJoinClassroomDialog = true
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                        shape = RoundedCornerShape(50.dp)
                                                    ) {
                                                        Text("+ Unirse a un aula", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                HorizontalDivider(color = AppColors.PurpleLight)
                                                
                                                children.forEach { child ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                                                            AvatarIcon(avatarKey = child.avatar, modifier = Modifier.size(24.dp), tint = AppColors.Purple)
                                                            Text(child.nombre, fontWeight = FontWeight.Bold)
                                                        }
                                                        Text("PIN: ${child.pin}", color = AppColors.Gray500, fontSize = 13.sp, textAlign = TextAlign.End)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Notifications log card
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Notifications,
                                                        contentDescription = null,
                                                        tint = AppColors.Purple,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Text("Historial de Notificaciones Escolares", fontWeight = FontWeight.Bold, color = AppColors.Purple)
                                                }
                                                Divider(color = AppColors.PurpleLight)
                                                
                                                if (notifications.isEmpty()) {
                                                    Text("No hay notificaciones académicas registradas.", fontSize = 12.sp, color = AppColors.Gray500)
                                                } else {
                                                    notifications.forEach { not ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(if(not.read) AppColors.Gray400 else AppColors.Pink))
                                                            Column(modifier = Modifier.weight(1f)) {
                                                                Text(not.titulo, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                                Text(not.cuerpo, fontSize = 12.sp, color = AppColors.Gray600)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Logout Button
                                    item {
                                        OutlinedButton(
                                            onClick = onBack,
                                            modifier = Modifier.fillMaxWidth().height(50.dp),
                                            shape = RoundedCornerShape(50.dp),
                                            border = BorderStroke(2.dp, AppColors.Purple)
                                        ) {
                                            Text("Cerrar Sesión", color = AppColors.Purple, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Registrar nuevo hijo button (always present if not in Perfil tab)
            if (tabState != 3) {
                Button(
                    onClick = onCreateStudentProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple)
                ) {
                    Text("+ Registrar Nuevo Hijo/a", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            if (showJoinClassroomDialog) {
                AlertDialog(
                    onDismissRequest = { if (!isJoining) showJoinClassroomDialog = false },
                    title = {
                        Text(
                            text = if (joinStep == 1) "Unirse a un Aula MathIA" else "Verificar Aula Encontrada",
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Purple
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (joinStep == 1) {
                                Text("Ingresa el código de aula proporcionado por el docente para vincular a tu hijo.", fontSize = 13.sp, color = AppColors.Gray700)
                                OutlinedTextField(
                                    value = classroomCodeInput,
                                    onValueChange = {
                                        classroomCodeInput = it.uppercase()
                                        joinError = ""
                                    },
                                    label = { Text("Código de Aula (ej: MATH-2A-84F)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                if (joinError.isNotEmpty()) {
                                    Text(joinError, color = AppColors.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                if (isJoining) {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AppColors.Purple)
                                }
                            } else {
                                validatedClassroom?.let { salon ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = AppColors.PurpleLight.copy(alpha = 0.4f)),
                                        border = BorderStroke(1.dp, AppColors.Purple.copy(alpha = 0.3f))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Aula Encontrada ✨", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.Purple)
                                            Text(salon.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = AppColors.Purple)
                                            Text("Docente: ${salon.teacherName}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.Gray800)
                                            Text("Grado: ${salon.grade} | Sección: ${salon.section}", fontSize = 12.sp, color = AppColors.Gray600)
                                        }
                                    }

                                    if (children.size > 1) {
                                        Text("¿A qué hijo deseas vincular?", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
                                        children.forEach { child ->
                                            val isSelected = selectedChildForJoin?.pin == child.pin
                                            Surface(
                                                onClick = { selectedChildForJoin = child },
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) AppColors.PurpleLight else AppColors.Gray100,
                                                border = BorderStroke(1.5.dp, if (isSelected) AppColors.Purple else Color.Transparent),
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    RadioButton(
                                                        selected = isSelected,
                                                        onClick = { selectedChildForJoin = child },
                                                        colors = RadioButtonDefaults.colors(selectedColor = AppColors.Purple)
                                                    )
                                                    AvatarIcon(avatarKey = child.avatar, modifier = Modifier.size(20.dp), tint = AppColors.Purple)
                                                    Text(child.nombre, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                }
                                            }
                                        }
                                    }

                                    selectedChildForJoin?.let { child ->
                                        Text(
                                            "¿Quieres vincular a ${child.nombre} con esta aula?",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.Gray800
                                        )
                                    }

                                    if (joinError.isNotEmpty()) {
                                        Text(joinError, color = AppColors.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (isJoining) {
                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AppColors.Purple)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        if (joinStep == 1) {
                            Button(
                                onClick = {
                                    val cleanCode = classroomCodeInput.trim()
                                    if (cleanCode.isBlank()) {
                                        joinError = "Ingresa un código de aula válido"
                                        return@Button
                                    }
                                    isJoining = true
                                    joinError = ""
                                    viewModel.validarCodigoAula(cleanCode) { result ->
                                        isJoining = false
                                        when (result) {
                                            is com.example.mathia.model.ClassroomValidationResult.Success -> {
                                                validatedClassroom = result.classroom
                                                selectedChildForJoin = children.firstOrNull()
                                                joinStep = 2
                                            }
                                            is com.example.mathia.model.ClassroomValidationResult.InvalidCode -> {
                                                joinError = "Código de aula no válido."
                                            }
                                            is com.example.mathia.model.ClassroomValidationResult.ClassroomClosed -> {
                                                joinError = "Esta aula ya no acepta nuevos estudiantes."
                                            }
                                            is com.example.mathia.model.ClassroomValidationResult.Error -> {
                                                joinError = result.message
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Continuar", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    val child = selectedChildForJoin ?: return@Button
                                    val salon = validatedClassroom ?: return@Button
                                    isJoining = true
                                    joinError = ""
                                    viewModel.vincularEstudianteAAula(
                                        classroomId = salon.classroomId,
                                        studentPin = child.pin.toString(),
                                        parentEmail = parentEmail
                                    ) { linkResult ->
                                        isJoining = false
                                        when (linkResult) {
                                            is com.example.mathia.model.ClassroomLinkResult.Success -> {
                                                Toast.makeText(context, "¡Listo! ${linkResult.studentName} ahora pertenece al aula ${linkResult.classroomName}.", Toast.LENGTH_LONG).show()
                                                showJoinClassroomDialog = false
                                                viewModel.obtenerEstudiantesPorEmailPadre(parentEmail) { freshChildren ->
                                                    children = freshChildren
                                                }
                                            }
                                            is com.example.mathia.model.ClassroomLinkResult.AlreadyLinked -> {
                                                joinError = "Este estudiante ya pertenece a esta aula."
                                            }
                                            is com.example.mathia.model.ClassroomLinkResult.Unauthorized -> {
                                                joinError = "No tienes autorización para vincular este estudiante."
                                            }
                                            is com.example.mathia.model.ClassroomLinkResult.Error -> {
                                                joinError = linkResult.message
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Vincular", fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                if (joinStep == 2) {
                                    joinStep = 1
                                } else {
                                    showJoinClassroomDialog = false
                                }
                            }
                        ) {
                            Text(if (joinStep == 2) "Atrás" else "Cancelar", color = AppColors.Gray600)
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(24.dp)
                )
            }
        }
    }
}
}
