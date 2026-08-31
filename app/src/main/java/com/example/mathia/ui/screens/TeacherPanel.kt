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
import com.example.mathia.model.Classroom
import com.example.mathia.model.Student
import com.example.mathia.model.Observacion
import com.example.mathia.model.NotificacionFirebase
import com.example.mathia.ui.components.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherPanel(
    uid: String,
    onBack: () -> Unit,
    onCreateStudentProfile: () -> Unit,
    viewModel: StudentViewModel
) {
    var tabState by remember { mutableIntStateOf(0) } // 0 = Dashboard, 1 = Alumnos, 2 = Historial, 3 = Perfil
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var alumnos by remember { mutableStateOf<List<FirebaseStudent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var teacherProfile by remember { mutableStateOf<Map<String, Any>?>(null) }
    var salonesDocente by remember { mutableStateOf<List<Classroom>>(emptyList()) }
    var selectedClassroomFilter by remember { mutableStateOf<Classroom?>(null) }
    var showCreateClassroomDialog by remember { mutableStateOf(false) }
    var classroomNameInput by remember { mutableStateOf("") }

    // Live observations and notifications
    val observations by viewModel.observaciones.collectAsState()
    val notifications by viewModel.notificaciones.collectAsState()

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(uid) {
        if (uid.isNotEmpty()) {
            try {
                val doc = db.collection("usuarios").document(uid).get().await()
                teacherProfile = doc.data
                viewModel.obtenerNotificaciones(uid)
                viewModel.obtenerSalonesProfesor(uid) { salones ->
                    salonesDocente = salones
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        viewModel.obtenerTodosAlumnos { lista ->
            alumnos = lista
            isLoading = false
        }
    }

    val teacherName = teacherProfile?.get("nombre") as? String ?: "Docente"
    val teacherSchool = teacherProfile?.get("colegio") as? String ?: "Institución Educativa"
    val teacherGrade = teacherProfile?.get("grado") as? String ?: ""
    val teacherSeccion = teacherProfile?.get("seccion") as? String ?: ""
    val teacherEmail = teacherProfile?.get("email") as? String ?: ""

    // Filter students by grade/section OR by selected classroom
    val alumnosFiltrados = alumnos.filter { student ->
        if (selectedClassroomFilter != null) {
            val salon = selectedClassroomFilter!!
            student.aulas.contains(salon.classroomId) || 
            (student.grado.equals(salon.grade, ignoreCase = true) && student.seccion.equals(salon.section, ignoreCase = true))
        } else {
            val gMatch = teacherGrade.isEmpty() || student.grado.lowercase().contains(teacherGrade.lowercase().substringBefore(" de")) || student.grado.lowercase() == teacherGrade.lowercase()
            val sMatch = teacherSeccion.isEmpty() || student.seccion.equals(teacherSeccion, ignoreCase = true)
            gMatch && sMatch
        }
    }

    val alumnosLocales = alumnosFiltrados.map { firebaseStudent ->
        val parts = firebaseStudent.nombre.split(" ")
        val firstName = parts.firstOrNull() ?: ""
        val lastName = parts.drop(1).joinToString(" ")
        Student(
            id = firebaseStudent.pin,
            name = firstName,
            lastName = lastName,
            grade = firebaseStudent.grado,
            classroom = firebaseStudent.seccion.ifEmpty { "Sección A" },
            pin = firebaseStudent.pin.toString(),
            level = firebaseStudent.nivel_actual,
            stars = firebaseStudent.estrellas,
            xp = firebaseStudent.xp,
            totalQuestions = firebaseStudent.total_preguntas,
            correctAnswers = firebaseStudent.correctas,
            examsCompleted = firebaseStudent.total_preguntas / 10 + 1,
            accuracy = firebaseStudent.precision.toInt(),
            streak = firebaseStudent.streak,
            weekData = firebaseStudent.weekData.ifEmpty { listOf(10, 20, 15, 30, 25, 40, 35) },
            monthData = firebaseStudent.monthData.ifEmpty { listOf(40, 50, 45, 60, 55, 70, 65, 80, 75, 90, 85, 95) },
            skills = firebaseStudent.skills.ifEmpty {
                mapOf(
                    "Sumas" to (firebaseStudent.nivel_actual * 20).coerceAtMost(100),
                    "Restas" to (firebaseStudent.nivel_actual * 18).coerceAtMost(100),
                    "Multiplicación" to (firebaseStudent.nivel_actual * 15).coerceAtMost(100),
                    "Fracciones" to (firebaseStudent.nivel_actual * 12).coerceAtMost(100),
                    "Series" to (firebaseStudent.nivel_actual * 14).coerceAtMost(100)
                )
            },
            incorrectasPorTema = firebaseStudent.incorrectas_por_tema,
            asistencia = firebaseStudent.asistencia,
            recomendaciones = firebaseStudent.recomendaciones,
            avatar = firebaseStudent.avatar,
            diagnosticoRealizado = firebaseStudent.diagnostico_realizado,
            tiempoTotal = firebaseStudent.tiempo_total,
            tiempoPromedio = firebaseStudent.tiempo_promedio,
            equippedTheme = firebaseStudent.equipped_theme,
            unlockedAvatars = firebaseStudent.unlocked_avatars,
            unlockedThemes = firebaseStudent.unlocked_themes,
            edad = firebaseStudent.edad,
            colegio = firebaseStudent.colegio,
            seccion = firebaseStudent.seccion,
            docenteAsignado = firebaseStudent.docente_asignado,
            fechaCreacion = firebaseStudent.fecha_creacion
        )
    }

    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(color = AppColors.PurpleLight)
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
                        icon = { Icon(Icons.Default.School, null) },
                        label = { Text("Clases", fontSize = 11.sp) },
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Fondo verde/mint exclusivo para el Perfil del Docente
            Image(
                painter = painterResource(id = R.drawable.fondo_perfil_docente),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (isLoading) {
                CircularProgressIndicator(color = AppColors.Purple, modifier = Modifier.align(Alignment.Center))
            } else if (selectedStudent != null) {
                // DETAILED VIEW WITH PROFILE AND FEEDBACK COMMENTS
                val s = selectedStudent!!
                var nuevaObservacion by remember { mutableStateOf("") }
                var isSavingObs by remember { mutableStateOf(false) }

                LaunchedEffect(s.pin) {
                    viewModel.obtenerObservaciones(s.pin)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedStudent = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AppColors.Purple)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Volver a Alumnos", fontWeight = FontWeight.Bold, color = AppColors.Purple, fontSize = 16.sp)
                    }

                    // Reusable mathia Profile card content (embedded)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(70.dp).background(AppColors.PurpleLight, CircleShape), contentAlignment = Alignment.Center) {
                                Text(s.avatar, fontSize = 36.sp)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("${s.name} ${s.lastName}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = AppColors.Purple)
                            Text("Grado: ${s.grade} | Sección: ${s.classroom}", fontSize = 12.sp, color = AppColors.Gray500)
                            Text("Precisión General: ${s.accuracy}% | Nivel ${s.level}", fontSize = 12.sp, color = AppColors.Green, fontWeight = FontWeight.Bold)
                        }
                    }

                    // ─── Observaciones del docente ───
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = AppColors.Purple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Retroalimentaciones / Observaciones", fontWeight = FontWeight.Bold, color = AppColors.Purple, fontSize = 16.sp)
                            }
                            
                            // Textfield to add observation
                            OutlinedTextField(
                                value = nuevaObservacion,
                                onValueChange = { nuevaObservacion = it },
                                placeholder = { Text("Ej: Necesita reforzar multiplicaciones en casa.") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            if (isSavingObs) {
                                CircularProgressIndicator(color = AppColors.Purple, modifier = Modifier.align(Alignment.CenterHorizontally))
                            } else {
                                Button(
                                    onClick = {
                                        if (nuevaObservacion.isNotBlank()) {
                                            isSavingObs = true
                                            val now = Date()
                                            val fFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                            val obs = Observacion(
                                                id = "",
                                                docenteId = uid,
                                                docenteNombre = teacherName,
                                                texto = nuevaObservacion,
                                                fecha = fFormat.format(now),
                                                timestamp = now.time
                                            )
                                            viewModel.guardarObservacion(s.pin, obs) {
                                                nuevaObservacion = ""
                                                isSavingObs = false
                                                Toast.makeText(context, "Observación guardada con éxito", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    enabled = nuevaObservacion.isNotBlank(),
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple)
                                ) {
                                    Text("Guardar Observación", fontWeight = FontWeight.Bold)
                                }
                            }

                            Divider(color = AppColors.Gray100, modifier = Modifier.padding(vertical = 4.dp))

                            Text("Historial de observaciones para este alumno:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = AppColors.Gray700)
                            
                            if (observations.isEmpty()) {
                                Text("Aún no has ingresado ninguna observación.", fontSize = 12.sp, color = AppColors.Gray500, modifier = Modifier.padding(vertical = 4.dp))
                            } else {
                                observations.forEach { obs ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = AppColors.Gray100)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(obs.texto, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Por: ${obs.docenteNombre}", fontSize = 11.sp, color = AppColors.Gray500)
                                                Text(obs.fecha, fontSize = 11.sp, color = AppColors.Gray500)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Embed skills breakdown progress
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = null,
                                    tint = AppColors.Purple,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Desempeño por competencia", fontWeight = FontWeight.Bold, color = AppColors.Purple)
                            }
                            s.skills.forEach { (skill, value) ->
                                SkillProgressBar(skill, value)
                            }
                        }
                    }
                }
            } else {
                when (tabState) {
                    0 -> {
                        // TABA 0: DASHBOARD ADMINISTRATIVO
                        var rankingType by remember { mutableStateOf("Estrellas") } // "Estrellas", "XP", "Precisión", "Tiempo"
                        var isRankExpanded by remember { mutableStateOf(false) }

                        val rankingList = remember(alumnosLocales, rankingType) {
                            when (rankingType) {
                                "XP" -> alumnosLocales.sortedByDescending { it.xp }
                                "Precisión" -> alumnosLocales.sortedByDescending { it.accuracy }
                                "Tiempo" -> alumnosLocales.sortedBy { if(it.tiempoPromedio > 0) it.tiempoPromedio else 999.0 }
                                else -> alumnosLocales.sortedByDescending { it.stars }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
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
                                            imageVector = Icons.Default.Assessment,
                                            contentDescription = null,
                                            tint = AppColors.Purple,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Text("Dashboard Aula", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = AppColors.Purple)
                                    }
                                    Button(
                                        onClick = { showCreateClassroomDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(50.dp)
                                    ) {
                                        Text("+ Crear Aula", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }

                            // Mis Aulas MathIA Card (Código unico, copiar, compartir, toggle activa/cerrada)
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.5.dp, AppColors.Purple.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Mis Aulas MathIA (${salonesDocente.size})", fontWeight = FontWeight.ExtraBold, color = AppColors.Purple, fontSize = 16.sp)
                                                Text("Código único para vincular a tus alumnos", fontSize = 12.sp, color = AppColors.Gray600)
                                            }
                                        }

                                        if (salonesDocente.isEmpty()) {
                                            Text(
                                                "Aún no has creado ningún aula. Pulsa '+ Crear Aula' arriba para generar tu primer código de invitación.",
                                                fontSize = 12.sp,
                                                color = AppColors.Gray500,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                        } else {
                                            for (salon in salonesDocente) {
                                                val alumnosDelSalon = alumnos.filter { student ->
                                                    student.aulas.contains(salon.classroomId) || 
                                                    (student.grado.equals(salon.grade, ignoreCase = true) && student.seccion.equals(salon.section, ignoreCase = true))
                                                }
                                                val avgAcc = if (alumnosDelSalon.isNotEmpty()) alumnosDelSalon.map { it.precision }.average().toInt() else 0
                                                val avgXP = if (alumnosDelSalon.isNotEmpty()) alumnosDelSalon.map { it.xp }.average().toInt() else 0
                                                val isSelected = selectedClassroomFilter?.classroomId == salon.classroomId

                                                Card(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                    shape = RoundedCornerShape(16.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (isSelected) AppColors.PurpleLight.copy(alpha = 0.8f) else AppColors.PurpleLight.copy(alpha = 0.4f)
                                                    ),
                                                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, AppColors.Purple)
                                                ) {
                                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column {
                                                                Text(salon.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.Purple)
                                                                Text("${salon.grade} - Sección ${salon.section}", fontSize = 11.sp, color = AppColors.Gray600)
                                                                Text(
                                                                    "${alumnosDelSalon.size} estudiantes • Precisión: ${avgAcc}% • XP: ${avgXP}",
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = AppColors.Gray800
                                                                )
                                                            }
                                                            Surface(
                                                                color = if (salon.isActive) AppColors.Green.copy(alpha = 0.15f) else AppColors.Red.copy(alpha = 0.15f),
                                                                shape = RoundedCornerShape(20.dp)
                                                            ) {
                                                                Text(
                                                                    text = if (salon.isActive) "ACTIVA 🟢" else "CERRADA 🔴",
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (salon.isActive) AppColors.Green else AppColors.Red,
                                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                                )
                                                            }
                                                        }

                                                        Button(
                                                            onClick = {
                                                                selectedClassroomFilter = if (isSelected) null else salon
                                                                tabState = 1 // Switch to Alumnos tab to view filtered students
                                                            },
                                                            modifier = Modifier.fillMaxWidth().height(36.dp),
                                                            shape = RoundedCornerShape(50.dp),
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = if (isSelected) AppColors.Purple else Color.White,
                                                                contentColor = if (isSelected) Color.White else AppColors.Purple
                                                            )
                                                        ) {
                                                            Text(
                                                                if (isSelected) "Viendo este Aula (Quitar Filtro)" else "Ver estudiantes de esta Aula →",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(10.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column {
                                                                Text("Código de Aula:", fontSize = 10.sp, color = AppColors.Gray500)
                                                                Text(salon.joinCode, fontWeight = FontWeight.Black, fontSize = 16.sp, color = AppColors.Purple)
                                                            }

                                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                Button(
                                                                    onClick = {
                                                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                                        val clip = android.content.ClipData.newPlainText("Código MathIA", salon.joinCode)
                                                                        clipboard.setPrimaryClip(clip)
                                                                        Toast.makeText(context, "Código ${salon.joinCode} copiado al portapapeles", Toast.LENGTH_SHORT).show()
                                                                    },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple),
                                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                                    shape = RoundedCornerShape(50.dp)
                                                                ) {
                                                                    Text("Copiar 📋", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                                }

                                                                Button(
                                                                    onClick = {
                                                                        val sendIntent = android.content.Intent().apply {
                                                                            action = android.content.Intent.ACTION_SEND
                                                                            putExtra(android.content.Intent.EXTRA_TEXT, "Únete a mi Aula MathIA (${salon.name})\nCódigo: ${salon.joinCode}")
                                                                            type = "text/plain"
                                                                        }
                                                                        context.startActivity(android.content.Intent.createChooser(sendIntent, "Compartir código de aula"))
                                                                    },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryBlue),
                                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                                    shape = RoundedCornerShape(50.dp)
                                                                ) {
                                                                    Text("Compartir 📤", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                                }
                                                            }
                                                        }

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            TextButton(
                                                                onClick = {
                                                                    val newStatus = !salon.isActive
                                                                    viewModel.cambiarEstadoAula(salon.classroomId, newStatus) { success ->
                                                                        if (success) {
                                                                            salonesDocente = salonesDocente.map { if (it.classroomId == salon.classroomId) it.copy(isActive = newStatus) else it }
                                                                            Toast.makeText(context, if (newStatus) "Aula activada" else "Aula cerrada para nuevas inscripciones", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    }
                                                                }
                                                            ) {
                                                                Text(if (salon.isActive) "Cerrar aula" else "Activar aula", fontSize = 11.sp, color = AppColors.Gray700, fontWeight = FontWeight.Bold)
                                                            }

                                                            val enrolledCount = alumnosLocales.count { it.seccion.equals(salon.section, ignoreCase = true) }
                                                            Text(
                                                                text = if (enrolledCount > 0) "$enrolledCount estudiantes vinculados" else "Aún no tienes estudiantes vinculados",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = AppColors.Gray600
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // General Group Performance Cards
                            item {
                                val avgAccuracy = if (alumnosLocales.isNotEmpty()) alumnosLocales.map { it.accuracy }.average().toInt() else 0
                                val totalStars = alumnosLocales.sumOf { it.stars }
                                val riskCount = alumnosLocales.count { it.accuracy < 60 }
                                val superCount = alumnosLocales.count { it.accuracy >= 85 }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("Resumen de tu Salón ($teacherGrade - $teacherSeccion)", fontWeight = FontWeight.Bold, color = AppColors.Purple)
                                        Divider(color = AppColors.PurpleLight)
                                        
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column {
                                                Text("Estudiantes", fontSize = 10.sp, color = AppColors.Gray500)
                                                Text("${alumnosLocales.size}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                            }
                                            Column {
                                                Text("Precisión Promedio", fontSize = 10.sp, color = AppColors.Gray500)
                                                Text("$avgAccuracy%", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.Green)
                                            }
                                            Column {
                                                Text("Total Estrellas", fontSize = 10.sp, color = AppColors.Gray500)
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = AppColors.Amber,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Text("$totalStars", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.Amber)
                                                }
                                            }
                                        }

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column {
                                                Text("Atención Recomendada", fontSize = 10.sp, color = AppColors.Gray500)
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Circle,
                                                        contentDescription = null,
                                                        tint = AppColors.Amber,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Text("$riskCount alumnos", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.Amber)
                                                }
                                            }
                                            Column {
                                                Text("Destacados", fontSize = 10.sp, color = AppColors.Gray500)
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Circle,
                                                        contentDescription = null,
                                                        tint = AppColors.Green,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Text("$superCount alumnos", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.Green)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Easiest / Hardest Topics & Topic Deep Dive (División)
                            item {
                                var selectedTeacherTopic by remember { mutableStateOf<String?>("División") }

                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Psychology,
                                                    contentDescription = null,
                                                    tint = AppColors.Purple,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text("Rendimiento por Temas de Clase (Toca para Analizar)", fontWeight = FontWeight.Bold, color = AppColors.Purple)
                                            }
                                            HorizontalDivider(color = AppColors.PurpleLight)

                                            val teacherTopics = listOf(
                                                Triple("Sumas", 91, AppColors.Green),
                                                Triple("Restas", 85, AppColors.Green),
                                                Triple("Multiplicación", 74, AppColors.Amber),
                                                Triple("División", 63, AppColors.Red)
                                            )

                                            teacherTopics.forEach { (topic, pct, color) ->
                                                val isSel = selectedTeacherTopic.equals(topic, ignoreCase = true)
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(if (isSel) AppColors.Purple.copy(alpha = 0.08f) else Color.Transparent)
                                                        .clickable { selectedTeacherTopic = topic }
                                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(topic, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.Gray800)
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        LinearProgressIndicator(
                                                            progress = { pct / 100f },
                                                            modifier = Modifier.width(100.dp).height(8.dp).clip(RoundedCornerShape(4.dp)),
                                                            color = color,
                                                            trackColor = AppColors.Gray200
                                                        )
                                                        Text("$pct%", fontSize = 12.sp, fontWeight = FontWeight.Black, color = color)
                                                        if (isSel) {
                                                            Text(" ▼ Detalle", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AppColors.Purple)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Deep dive card for selected topic (defaulting to División)
                                    selectedTeacherTopic?.let { topic ->
                                        val sampleStudent = alumnosLocales.firstOrNull() ?: Student(
                                            id = 1, name = "Mateo", lastName = "Torres", grade = "2° Primaria", classroom = "A", pin = "1234",
                                            level = 12, stars = 1240, accuracy = 63, totalQuestions = 24, correctAnswers = 15, tiempoPromedio = 42.0
                                        )
                                        TopicDetailAnalysisCard(
                                            student = sampleStudent,
                                            topicName = topic,
                                            onClose = { selectedTeacherTopic = null }
                                        )
                                    }
                                }
                            }

                            // Selectable Ranking Section
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                                imageVector = Icons.Default.EmojiEvents,
                                                contentDescription = null,
                                                tint = AppColors.Purple,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            Text("Tabla de Clasificación", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        }
                                        Box {
                                            Button(
                                                onClick = { isRankExpanded = !isRankExpanded },
                                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.PurpleLight, contentColor = AppColors.Purple),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Por: $rankingType ▾")
                                            }
                                            DropdownMenu(expanded = isRankExpanded, onDismissRequest = { isRankExpanded = false }) {
                                                listOf("Estrellas", "XP", "Precisión", "Tiempo").forEach { type ->
                                                    DropdownMenuItem(text = { Text(type) }, onClick = { rankingType = type; isRankExpanded = false })
                                                }
                                            }
                                        }
                                    }

                                    rankingList.take(5).forEachIndexed { index, std ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Text(
                                                        text = when (index) {
                                                            0 -> "1º"
                                                            1 -> "2º"
                                                            2 -> "3º"
                                                            else -> "#${index + 1}"
                                                        },
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp
                                                    )
                                                    Box(modifier = Modifier.size(32.dp).background(AppColors.PurpleLight, CircleShape), contentAlignment = Alignment.Center) {
                                                        AvatarIcon(avatarKey = std.avatar, modifier = Modifier.size(20.dp), tint = AppColors.Purple)
                                                    }
                                                    Text("${std.name} ${std.lastName}", fontWeight = FontWeight.Bold)
                                                }
                                                Text(
                                                    text = when (rankingType) {
                                                        "XP" -> "${std.xp} XP"
                                                        "Precisión" -> "${std.accuracy}%"
                                                        "Tiempo" -> "${std.tiempoPromedio.toInt()}s"
                                                        else -> "${std.stars} estrellas"
                                                    },
                                                    fontWeight = FontWeight.Black,
                                                    color = AppColors.Purple
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // TAB 1: LISTA DE ALUMNOS (SEMAFOROS)
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
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
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Text("Mis Alumnos", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = AppColors.Purple)
                                    }
                                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = AppColors.Purple) }
                                }
                            }

                            item {
                                Button(
                                    onClick = onCreateStudentProfile,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple)
                                ) {
                                    Text("+ Registrar Nuevo Alumno", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            if (alumnosLocales.isEmpty()) {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.Face,
                                                contentDescription = null,
                                                tint = AppColors.Purple,
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Text("No hay alumnos registrados en tu grado y sección.", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                                        }
                                    }
                                }
                            } else {
                                items(alumnosLocales) { alumno ->
                                    val semColor = when {
                                        alumno.accuracy >= 80 -> AppColors.Green
                                        alumno.accuracy >= 50 -> AppColors.Amber
                                        else -> AppColors.Red
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedStudent = alumno },
                                        shape = RoundedCornerShape(24.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(semColor))
                                                Spacer(Modifier.width(12.dp))
                                                Box(modifier = Modifier.size(40.dp).background(AppColors.PurpleLight, CircleShape), contentAlignment = Alignment.Center) {
                                                    AvatarIcon(avatarKey = alumno.avatar, modifier = Modifier.size(24.dp), tint = AppColors.Purple)
                                                }
                                                Spacer(Modifier.width(12.dp))
                                                Column {
                                                    Text("${alumno.name} ${alumno.lastName}", fontWeight = FontWeight.Bold)
                                                    Text("Precisión: ${alumno.accuracy}% | Nivel ${alumno.level}", fontSize = 12.sp, color = AppColors.Gray500)
                                                }
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = AppColors.Amber,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text("${alumno.stars}", fontWeight = FontWeight.Bold, color = AppColors.Amber)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // TAB 2: AUDITORIA DE HISTORIAL COMPLETO
                        TeacherHistoryScreen(
                            students = alumnosFiltrados,
                            viewModel = viewModel,
                            onBack = { tabState = 0 }
                        )
                    }
                    3 -> {
                        // TAB 3: PERFIL DEL DOCENTE
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = AppColors.Purple,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Text("Mi Perfil", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = AppColors.Purple)
                                }
                            }

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
                                                 painter = painterResource(id = R.drawable.ajolote_role_teacher),
                                                 contentDescription = "Docente",
                                                 modifier = Modifier.size(75.dp)
                                             )
                                         }
                                        Spacer(Modifier.height(12.dp))
                                        Text(teacherName, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = AppColors.Purple)
                                        Text("Docente de Primaria", fontSize = 14.sp, color = AppColors.Gray600)
                                        Text(teacherEmail, fontSize = 13.sp, color = AppColors.Gray500)
                                    }
                                }
                            }

                            // Classroom parameters card
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.School,
                                                contentDescription = null,
                                                tint = AppColors.Purple,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text("Configuración de Aula", fontWeight = FontWeight.Bold, color = AppColors.Purple)
                                        }
                                        Divider(color = AppColors.PurpleLight)
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                Text("Colegio:", color = AppColors.Gray600, modifier = Modifier.weight(0.6f))
                                                Text(teacherSchool, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f), textAlign = TextAlign.End)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                Text("Grado asignado:", color = AppColors.Gray600, modifier = Modifier.weight(0.6f))
                                                Text(teacherGrade, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f), textAlign = TextAlign.End)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                Text("Sección asignada:", color = AppColors.Gray600, modifier = Modifier.weight(0.6f))
                                                Text(teacherSeccion, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f), textAlign = TextAlign.End)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                Text("Cantidad de Alumnos:", color = AppColors.Gray600, modifier = Modifier.weight(0.6f))
                                                Text("${alumnosLocales.size}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f), textAlign = TextAlign.End)
                                            }
                                        }
                                    }
                                }
                            }

                            // Classroom dashboard calculations
                            item {
                                val avgAccuracy = if (alumnosLocales.isNotEmpty()) alumnosLocales.map { it.accuracy }.average().toInt() else 0
                                val bestStd = alumnosLocales.maxByOrNull { it.stars }?.let { "${it.name} (${it.stars} estrellas)" } ?: "Ninguno"
                                val difficultyStd = alumnosLocales.minByOrNull { it.accuracy }?.let { "${it.name} (${it.accuracy}% prec.)" } ?: "Ninguno"
                                val diagCount = alumnosLocales.count { it.diagnosticoRealizado }
                                val totalReviewed = alumnosLocales.sumOf { it.totalQuestions }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.BarChart,
                                                contentDescription = null,
                                                tint = AppColors.Purple,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text("Analíticas del Salón", fontWeight = FontWeight.Bold, color = AppColors.Purple)
                                        }
                                        Divider(color = AppColors.PurpleLight)
                                        
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                Text("Promedio de Precisión:", color = AppColors.Gray600, modifier = Modifier.weight(0.6f))
                                                Text("$avgAccuracy%", fontWeight = FontWeight.Bold, color = AppColors.Green, modifier = Modifier.weight(0.4f), textAlign = TextAlign.End)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                Text("Estudiante Líder:", color = AppColors.Gray600, modifier = Modifier.weight(0.6f))
                                                Text(bestStd, fontWeight = FontWeight.Bold, color = AppColors.Purple, modifier = Modifier.weight(0.4f), textAlign = TextAlign.End)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                Text("Mayor Dificultad Académica:", color = AppColors.Gray600, modifier = Modifier.weight(0.6f))
                                                Text(difficultyStd, fontWeight = FontWeight.Bold, color = AppColors.Red, modifier = Modifier.weight(0.4f), textAlign = TextAlign.End)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                Text("Diagnósticos Realizados:", color = AppColors.Gray600, modifier = Modifier.weight(0.6f))
                                                Text("$diagCount", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f), textAlign = TextAlign.End)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                Text("Preguntas Corregidas (Total):", color = AppColors.Gray600, modifier = Modifier.weight(0.6f))
                                                Text("$totalReviewed", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f), textAlign = TextAlign.End)
                                            }
                                        }
                                    }
                                }
                            }

                            // Notification log list
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
                                            Text("Alertas y Notificaciones Recientes", fontWeight = FontWeight.Bold, color = AppColors.Purple)
                                        }
                                        Divider(color = AppColors.PurpleLight)
                                        
                                        if (notifications.isEmpty()) {
                                            Text("No hay notificaciones recientes.", fontSize = 12.sp, color = AppColors.Gray500)
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

                            // Sign Out Button
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
            if (showCreateClassroomDialog) {
                AlertDialog(
                    onDismissRequest = { showCreateClassroomDialog = false },
                    title = { Text("Crear Nueva Aula MathIA", fontWeight = FontWeight.Bold, color = AppColors.Purple) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Ingresa el nombre del aula para generar el código de invitación único para tus alumnos.", fontSize = 13.sp, color = AppColors.Gray700)
                            OutlinedTextField(
                                value = classroomNameInput,
                                onValueChange = { classroomNameInput = it },
                                label = { Text("Nombre del Aula (ej: 2° A - Matemáticas)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (classroomNameInput.isNotBlank()) {
                                    val name = classroomNameInput.trim()
                                    viewModel.crearAula(
                                        teacherId = uid,
                                        teacherName = teacherName,
                                        name = name,
                                        grade = teacherGrade.ifEmpty { "Segundo" },
                                        section = teacherSeccion.ifEmpty { "A" },
                                        school = teacherSchool
                                    ) { newClassroom ->
                                        if (newClassroom != null) {
                                            salonesDocente = salonesDocente + newClassroom
                                            Toast.makeText(context, "¡Aula '${newClassroom.name}' creada! Código: ${newClassroom.joinCode}", Toast.LENGTH_LONG).show()
                                            showCreateClassroomDialog = false
                                            classroomNameInput = ""
                                        } else {
                                            Toast.makeText(context, "Error al crear el aula. Revisa tu conexión.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Crear Aula", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCreateClassroomDialog = false }) {
                            Text("Cancelar", color = AppColors.Gray600)
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

