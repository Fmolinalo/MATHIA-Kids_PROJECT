package com.example.mathia.ui.screens

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathia.AppColors
import com.example.mathia.R
import com.example.mathia.StudentViewModel
import com.example.mathia.model.MathiaAlert
import com.example.mathia.model.Student
import com.example.mathia.ui.components.AvatarIcon
import com.example.mathia.ui.components.RadarChart
import com.example.mathia.ui.components.SkillProgressBar
import com.example.mathia.ui.components.StarProgress
import com.example.mathia.ui.components.StreakCalendar
import com.example.mathia.ui.components.GamifiedLevelProgress
import com.example.mathia.ui.components.AdventureMapCard
import com.example.mathia.ui.components.WeeklyContestCard
import com.example.mathia.viewModels.ThemeViewModel


// Data class para representar cada actividad del menú infantil
data class ActivityCard(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val subtitle: String,
    val op: String,
    val gradientStart: Color,
    val gradientEnd: Color
)

@Composable
fun MainMenuScreen(
    student: Student,
    onPlay: (String) -> Unit,
    onRewards: () -> Unit,
    onExam: () -> Unit,
    onLeaderboard: () -> Unit,
    onDuels: () -> Unit,
    onLogout: () -> Unit,
    onUpdateStudent: (Student) -> Unit,
    onShowAlert: (MathiaAlert) -> Unit,
    onProfile: () -> Unit,
    viewModel: StudentViewModel,
    themeViewModel: ThemeViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Inicio, 1 = Progreso, 2 = Tienda

    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("mathia_prefs", Context.MODE_PRIVATE) }
    var currentTutor by remember { mutableStateOf(sharedPref.getString("student_tutor", "axolita") ?: "axolita") }
    var showTutorSelector by remember { mutableStateOf(false) }
    var showNicknameSetupDialog by remember { mutableStateOf(student.apodo.isBlank()) }

    if (showNicknameSetupDialog) {
        com.example.mathia.ui.components.NicknameSetupDialog(
            currentNickname = student.apodo,
            onSaveNickname = { newApodo: String, callback: (Boolean) -> Unit ->
                viewModel.validarYGuardarApodo(student.pin, newApodo) { success, msg ->
                    if (success) {
                        onUpdateStudent(student.copy(apodo = newApodo.trim()))
                        showNicknameSetupDialog = false
                    }
                    callback(success)
                }
            },
            onDismiss = {
                if (student.apodo.isNotBlank()) {
                    showNicknameSetupDialog = false
                }
            }
        )
    }

    val activities = remember {
        listOf(
            ActivityCard(
                Icons.Default.Add, "Sumas", "¡Suma y gana estrellas!", "Suma",
                Color(0xFF4FC3F7), Color(0xFF0288D1)
            ),
            ActivityCard(
                Icons.Default.Remove, "Restas", "¡Entrena tu mente!", "Resta",
                Color(0xFF81C784), Color(0xFF388E3C)
            ),
            ActivityCard(
                Icons.Default.Close, "Multiplicación", "¡Conviértete en experto!", "Multiplicacion",
                Color(0xFFFFB74D), Color(0xFFE65100)
            ),
            ActivityCard(
                Icons.Default.Percent, "Divisiones", "¡Reparte y aprende!", "Division",
                Color(0xFF26A69A), Color(0xFF004D40)
            ),
            ActivityCard(
                Icons.Default.PieChart, "Fracciones", "¡Juega con porciones!", "Fracciones",
                Color(0xFFBA68C8), Color(0xFF7B1FA2)
            ),
            ActivityCard(
                Icons.Default.LinearScale, "Series", "¡Completa el patrón!", "Series",
                Color(0xFFFF8A65), Color(0xFFBF360C)
            ),
            ActivityCard(
                Icons.Default.Psychology, "Examen IA", "¡Mide tu nivel!", "Exam",
                Color(0xFF26C6DA), Color(0xFF006064)
            ),
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.mathia_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Inicio",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppColors.MathiaPurple,
                        selectedTextColor = AppColors.MathiaPurple,
                        indicatorColor = AppColors.MathiaPurpleLight
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Progreso",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppColors.MathiaPurple,
                        selectedTextColor = AppColors.MathiaPurple,
                        indicatorColor = AppColors.MathiaPurpleLight
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            "Tienda",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppColors.MathiaPurple,
                        selectedTextColor = AppColors.MathiaPurple,
                        indicatorColor = AppColors.MathiaPurpleLight
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onRewards,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Premios", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppColors.MathiaPurple,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onLogout,
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Salir", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppColors.MathiaPurple,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            2 -> Box(modifier = Modifier.padding(paddingValues)) {
                ShopTabContent(
                    student = student,
                    onUpdateStudent = onUpdateStudent,
                    onShowAlert = onShowAlert,
                    viewModel = viewModel
                )
            }
            1 -> ProgresoTab(
                student = student,
                paddingValues = paddingValues
            )
            else -> InicioTab(
                student = student,
                activities = activities,
                onPlay = onPlay,
                onExam = onExam,
                onLeaderboard = onLeaderboard,
                onDuels = onDuels,
                onProfile = onProfile,
                paddingValues = paddingValues,
                currentTutor = currentTutor,
                onShowTutorSelector = { showTutorSelector = true }
            )
        }
    }

    if (showTutorSelector) {
        TutorSelectionDialog(
            currentTutor = currentTutor,
            onTutorSelected = { tutorKey ->
                currentTutor = tutorKey
                sharedPref.edit().putString("student_tutor", tutorKey).apply()
            },
            onDismiss = { showTutorSelector = false }
        )
    }
}
}

// ─── TAB INICIO ──────────────────────────────────────────────────────────────
@Composable
fun InicioTab(
    student: Student,
    activities: List<ActivityCard>,
    onPlay: (String) -> Unit,
    onExam: () -> Unit,
    onLeaderboard: () -> Unit,
    onDuels: () -> Unit,
    onProfile: () -> Unit,
    paddingValues: PaddingValues,
    currentTutor: String,
    onShowTutorSelector: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Color.Transparent),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ─── HEADER & HERO WELCOME CARD ──────────────────────────────
        item {
            StudentHeaderHero(
                student = student,
                currentTutor = currentTutor,
                onProfile = onProfile,
                onContinueLearning = { onPlay("Suma") }
            )
        }

        // ─── NICKNAME PROMPT BANNER ──────────────────────────────────
        if (student.apodo.isBlank()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clickable { onShowTutorSelector() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.MathiaPurpleLight),
                    border = BorderStroke(1.5.dp, AppColors.MathiaPurple)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = AppColors.MathiaPurple,
                            modifier = Modifier.size(32.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¡Ya puedes entrar a los Duelos!",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = AppColors.MathiaPurple
                            )
                            Text(
                                text = "Primero crea tu apodo público para competir.",
                                fontSize = 12.sp,
                                color = AppColors.Gray700
                            )
                        }
                    }
                }
            }
        }

        // ─── CONCURSO SEMANAL BANNER ──────────────────────────────────
        item {
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                WeeklyContestCard(
                    onParticipate = { onPlay("Division") }
                )
            }
        }

        // ─── TARJETA PRINCIPAL DE DUELOS MATEMÁTICOS ─────────────────
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable { onDuels() },
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, AppColors.MathiaPurple)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(AppColors.MathiaPurpleLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsKabaddi,
                            contentDescription = null,
                            tint = AppColors.MathiaPurple,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Duelos Matemáticos",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = AppColors.MathiaPurple
                            )
                            Surface(shape = CircleShape, color = AppColors.Coral) {
                                Text("NUEVO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Desafía a tus compañeros en una batalla matemática",
                            fontSize = 12.sp,
                            color = AppColors.Gray700
                        )
                    }
                    Button(
                        onClick = onDuels,
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.MathiaPurple),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text("JUGAR", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }

        // ─── TARJETAS DE PROGRESO / ESTADÍSTICAS DEL NIÑO ────────────
        item {
            ChildProgressRow(student = student)
        }

        // ─── SECCIÓN DESAFÍOS MATEMÁTICOS ────────────────────────────
        item {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = AppColors.MathiaPurple,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "¡Elige tu Desafío!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = AppColors.Gray800
                )
            }
        }

        // ─── GRILLA DE ACTIVIDADES ───────────────────────────────────
        item {
            ActivityGrid(
                activities = activities,
                onPlay = onPlay,
                onExam = onExam
            )
        }

        // ─── TABLA DE CAMPEONES BANNER ───────────────────────────────
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable { onLeaderboard() },
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.MathiaGold)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Tabla de Campeones",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            "¡Compara tu puntaje y sube de rango!",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // ─── SUGERENCIAS DEL TUTOR ───────────────────────────────────
        item {
            TutorCornerCard(
                student = student,
                currentTutor = currentTutor,
                onClickTutor = onShowTutorSelector,
                onPlayTopic = onPlay
            )
        }

        // ─── MISIONES ───────────────────────────────────────────────
        item {
            MisionesCard(student = student)
        }

        item {
            Spacer(Modifier.height(12.dp))
        }
    }
}

// ─── HEADER & HERO WELCOME CARD ──────────────────────────────────────────────
@Composable
fun StudentHeaderHero(
    student: Student,
    currentTutor: String,
    onProfile: () -> Unit,
    onContinueLearning: () -> Unit
) {
    val tutorRes = when (currentTutor) {
        "prof_axol" -> R.drawable.ajolote_teacher_male
        "prof_axolina" -> R.drawable.ajolote_teacher_female
        else -> R.drawable.ajolote_student
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Bar: Avatar, Nombre, Grado, Racha
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.clickable { onProfile() }
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(AppColors.MathiaPurpleLight)
                        .border(2.dp, AppColors.MathiaPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AvatarIcon(
                        avatarKey = student.avatar,
                        tint = AppColors.MathiaPurple,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Column {
                    Text(
                        text = "¡Hola, ${student.apodo.ifEmpty { student.name }}!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.Gray800
                    )

                    Text(
                        text = "${student.grade} • ${student.colegio.ifEmpty { "MathIA Kids" }}",
                        fontSize = 12.sp,
                        color = AppColors.Gray500,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Streak Flame Badge
            val activeStreak = remember(student.asistencia) {
                com.example.mathia.model.StreakCalculator.getActiveStreak(student.asistencia)
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = AppColors.MathiaGoldLight),
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(1.dp, AppColors.MathiaGold.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = null,
                        tint = AppColors.MathiaGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "$activeStreak",
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.Gray800,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Hero Welcome Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(1.5.dp, AppColors.CardBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(AppColors.MathiaPurple, Color(0xFF8E44AD))
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "¡Hola, ${student.name}!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "¿Listo para aprender matemáticas?",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Gamified Level XP Progress Bar
                        GamifiedLevelProgress(xp = student.xp, level = student.level)

                        Spacer(modifier = Modifier.height(4.dp))

                        // Large Primary CTA Button

                        Button(
                            onClick = onContinueLearning,
                            modifier = Modifier
                                .height(50.dp)
                                .fillMaxWidth(0.95f),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.MathiaGreen),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CONTINUAR APRENDIENDO",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = tutorRes),
                            contentDescription = "Mascota MathIA",
                            modifier = Modifier.size(66.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── TARJETAS DE PROGRESO DEL NIÑO ───────────────────────────────────────────
@Composable
fun ChildProgressRow(student: Student) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val activeStreak = remember(student.asistencia) {
            com.example.mathia.model.StreakCalculator.getActiveStreak(student.asistencia)
        }
        val stats = listOf(
            Quadruple(Icons.Default.Star, "${student.stars}", "Estrellas", AppColors.MathiaGold, AppColors.MathiaGoldLight),
            Quadruple(Icons.Default.ElectricBolt, "${student.xp}", "XP", AppColors.MathiaPurple, AppColors.MathiaPurpleLight),
            Quadruple(Icons.Default.EmojiEvents, "Nv. ${student.level}", "Nivel", AppColors.MathiaTealDark, AppColors.MathiaTealLight),
            Quadruple(Icons.Default.Whatshot, "${activeStreak}d", "Racha", AppColors.MathiaGold, AppColors.MathiaGoldLight)
        )


        stats.forEach { (icon, value, label, accentColor, bgColor) ->
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, AppColors.CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = value,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = AppColors.Gray800
                    )
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.Gray500
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

// ─── GRILLA DE ACTIVIDADES 2 COLUMNAS ────────────────────────────────────────
@Composable
fun ActivityGrid(
    activities: List<ActivityCard>,
    onPlay: (String) -> Unit,
    onExam: () -> Unit
) {
    val rowCount = (activities.size + 1) / 2
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for (row in 0 until rowCount) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (col in 0..1) {
                    val index = row * 2 + col
                    if (index < activities.size) {
                        val activity = activities[index]
                        ActivityCardItem(
                            activity = activity,
                            index = index,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                when (activity.op) {
                                    "Exam" -> onExam()
                                    else -> onPlay(activity.op)
                                }
                            }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ─── CARD INDIVIDUAL DE ACTIVIDAD ────────────────────────────────────────────
@Composable
fun ActivityCardItem(
    activity: ActivityCard,
    index: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "card_${activity.op}")
    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800 + (index * 200),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_${activity.op}"
    )

    Card(
        modifier = modifier
            .height(135.dp)
            .scale(animatedScale)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(activity.gradientStart, activity.gradientEnd)
                    )
                )
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = activity.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = activity.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = activity.subtitle,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

// ─── TUTOR CORNER CARD ───────────────────────────────────────────────────────
@Composable
fun TutorCornerCard(
    student: Student,
    currentTutor: String,
    onClickTutor: () -> Unit,
    onPlayTopic: (String) -> Unit
) {
    val tutorInfo = remember(currentTutor) {
        when (currentTutor) {
            "prof_axol" -> Triple("Profesor Axol", R.drawable.ajolote_teacher_male, "¡Hola! Analicemos tus metas de hoy.")
            "prof_axolina" -> Triple("Profesora Axolina", R.drawable.ajolote_teacher_female, "¡Hola, cariño! Vamos a aprender a tu ritmo.")
            else -> Triple("Axolita", R.drawable.ajolote_student, "¡Hola! ¡Qué chulo verte hoy! ¿Jugamos?")
        }
    }

    val tips = remember(student.recomendaciones) {
        student.recomendaciones.ifEmpty {
            listOf(
                "¡Completa desafíos para ganar estrellas y subir de nivel!",
                "¡Practica todos los días para mantener tu racha!"
            )
        }
    }

    val speechPrefix = remember(currentTutor) {
        when (currentTutor) {
            "prof_axol" -> "Lógica recomendada: "
            "prof_axolina" -> "Consejo con amor: "
            else -> "¡Tip rápido! "
        }
    }

    val recommendedTopic = remember(tips) {
        tips.firstOrNull()?.let { com.example.mathia.ui.components.extractTopicFromRecommendation(it) } ?: "Division"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.5.dp, AppColors.CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.MathiaPurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = tutorInfo.second),
                    contentDescription = tutorInfo.first,
                    modifier = Modifier.size(70.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${tutorInfo.first} (Tutor Guía)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = AppColors.MathiaPurple
                )
                Text(
                    text = tutorInfo.third,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Gray700,
                    lineHeight = 14.sp
                )
                Spacer(Modifier.height(6.dp))

                tips.take(2).forEach { tip ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = AppColors.MathiaGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "$speechPrefix$tip",
                            fontSize = 11.sp,
                            color = AppColors.Gray600,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cambiar tutor 🔄",
                        fontSize = 11.sp,
                        color = AppColors.MathiaPurple,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onClickTutor() }
                    )
                    Button(
                        onClick = { onPlayTopic(recommendedTopic) },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.MathiaPurple),
                        shape = RoundedCornerShape(50.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Practicar Ahora", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }
        }
    }
}

// ─── CONCURSO SEMANAL CARD ──────────────────────────────────────────────────
@Composable
fun WeeklyContestCard(
    weeklyCompetition: com.example.mathia.model.WeeklyCompetition = com.example.mathia.model.WeeklyCompetition(),
    currentExercises: Int = 12,
    studentRank: String = "#4",
    onParticipate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.5.dp, AppColors.MathiaPurple.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFF3E5F5), Color.White)
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = AppColors.MathiaPurple,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "CONCURSO SEMANAL",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = AppColors.MathiaPurple
                    )
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = AppColors.MathiaGoldLight),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AppColors.MathiaGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${weeklyCompetition.rewardStars} estrellas",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Gray800
                        )
                    }
                }
            }

            Text(
                text = weeklyCompetition.title.uppercase(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = AppColors.Gray800
            )

            Text(
                text = "\"Demuestra cuánto sabes en este desafío semanal\"",
                fontSize = 12.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = AppColors.Gray600
            )

            val progressFloat = (currentExercises.toFloat() / weeklyCompetition.targetExercises).coerceIn(0f, 1f)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progreso del Desafío",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Gray700
                    )
                    Text(
                        text = "$currentExercises / ${weeklyCompetition.targetExercises} ejercicios",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.MathiaPurple
                    )
                }

                LinearProgressIndicator(
                    progress = { progressFloat },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = AppColors.MathiaPurple,
                    trackColor = AppColors.Gray200
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tu posición actual: $studentRank",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.MathiaTealDark
                )

                Button(
                    onClick = onParticipate,
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.MathiaPurple)
                ) {
                    Text(
                        text = "PARTICIPAR",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// ─── MISIONES ────────────────────────────────────────────────────────────────
@Composable
fun MisionesCard(student: Student) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.5.dp, AppColors.CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = AppColors.MathiaPurple,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Misiones de Hoy",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = AppColors.Gray800
                )
            }

            val dailyTarget = 5
            val dailyPct = (student.dailyMissionProgress.toFloat() / dailyTarget).coerceIn(0f, 1f)
            MisionRow(
                icon = Icons.Default.WbSunny,
                label = "Diaria: Resuelve 5 desafíos",
                current = student.dailyMissionProgress,
                target = dailyTarget,
                progress = dailyPct,
                color = AppColors.MathiaGold
            )

            val weeklyTarget = 20
            val weeklyPct = (student.weeklyMissionProgress.toFloat() / weeklyTarget).coerceIn(0f, 1f)
            MisionRow(
                icon = Icons.Default.CalendarMonth,
                label = "Semanal: Resuelve 20 desafíos",
                current = student.weeklyMissionProgress,
                target = weeklyTarget,
                progress = weeklyPct,
                color = AppColors.MathiaPurple
            )
        }
    }
}

@Composable
fun MisionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    current: Int,
    target: Int,
    progress: Float,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
                Text(label, fontSize = 12.sp, color = AppColors.Gray700, fontWeight = FontWeight.Medium)
            }
            Text(
                "$current/$target",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = AppColors.Gray200
        )
    }
}

// ─── TAB PROGRESO (REDISEÑADO) ────────────────────────────────────────────────
@Composable
fun ProgresoTab(student: Student, paddingValues: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Color.Transparent)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = AppColors.MathiaPurple,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Mi Progreso",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = AppColors.Gray800
                )
            }
        }

        // Racha de días
        item {
            val activeStreak = remember(student.asistencia) {
                com.example.mathia.model.StreakCalculator.getActiveStreak(student.asistencia)
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.5.dp, AppColors.CardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = null,
                        tint = AppColors.MathiaGold,
                        modifier = Modifier.size(40.dp)
                    )
                    Column {
                        Text(
                            "¡Racha de ${activeStreak} días!",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = AppColors.Gray800
                        )
                        Text(
                            "¡No rompas tu racha! Practica hoy.",
                            fontSize = 13.sp,
                            color = AppColors.Gray600
                        )
                        Spacer(Modifier.height(8.dp))
                        StreakCalendar(asistencia = student.asistencia, streak = activeStreak)
                    }
                }
            }
        }

        // Barra de progreso de estrellas
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.5.dp, AppColors.CardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    StarProgress(current = student.stars)
                }
            }
        }

        // Mapa de habilidades (REDiseñado con barras gruesas de estado)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.5.dp, AppColors.CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = AppColors.MathiaPurple,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Mapa de Aventuras Matemáticas",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = AppColors.Gray800
                            )
                            Text(
                                text = "Explora tus zonas y avanza en tu aventura",
                                fontSize = 11.sp,
                                color = AppColors.Gray500
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))

                    val mapSkills = student.skills.ifEmpty {
                        mapOf(
                            "Sumas" to 0,
                            "Restas" to 0,
                            "Multiplicación" to 0,
                            "Fracciones" to 0,
                            "Series" to 0
                        )
                    }

                    // Render Adventure Map Card Layer
                    AdventureMapCard(skills = mapSkills)
                }
            }
        }

        // Mis Logros de Aprendizaje (Sin duplicidad de métricas, enfocado en metas y logros)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = BorderStroke(1.5.dp, AppColors.CardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = AppColors.MathiaPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Mis Logros & Metas de Aprendizaje",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = AppColors.Gray800
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    val activeStreak = remember(student.asistencia) {
                        com.example.mathia.model.StreakCalculator.getActiveStreak(student.asistencia)
                    }
                    val goals = listOf(
                        Triple(Icons.Default.Star, "¡Coleccionista de Estrellas!", "${student.stars} / 500 ⭐"),
                        Triple(Icons.Default.EmojiEvents, "¡Campeón de Nivel!", "Nivel ${student.level} Desbloqueado"),
                        Triple(Icons.Default.Whatshot, "¡Racha Imparable!", "${activeStreak} / 7 Días"),
                        Triple(Icons.Default.Description, "¡Explorador de Ejercicios!", "${student.totalQuestions} Desafíos Resueltos"),
                        Triple(Icons.Default.Book, "¡Maestro de Exámenes!", "${student.examsCompleted} Exámenes Completados"),
                    )

                    goals.forEach { (icon, label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = AppColors.MathiaPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(label, fontSize = 13.sp, color = AppColors.Gray700, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                value,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = AppColors.MathiaPurple
                            )
                        }
                        HorizontalDivider(color = AppColors.Gray200, thickness = 0.5.dp)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}
