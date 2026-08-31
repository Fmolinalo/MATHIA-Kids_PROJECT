package com.example.mathia.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathia.AppColors
import com.example.mathia.StudentViewModel
import com.example.mathia.model.FirebaseStudent
import com.example.mathia.model.RespuestaHistorial
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherHistoryScreen(
    students: List<FirebaseStudent>,
    viewModel: StudentViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var rawLogs by remember { mutableStateOf<List<RespuestaHistorial>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Search and filter states
    var searchQuery by remember { mutableStateOf("") }
    var selectedOperation by remember { mutableStateOf("Todas") }
    var selectedDifficulty by remember { mutableStateOf("Todas") }
    var selectedStatus by remember { mutableStateOf("Todos") } // "Todos", "Correctas", "Incorrectas"

    var isOpExpanded by remember { mutableStateOf(false) }
    var isDiffExpanded by remember { mutableStateOf(false) }
    var isStatusExpanded by remember { mutableStateOf(false) }

    val operations = listOf("Todas", "Suma", "Resta", "Multiplicacion", "Fracciones", "Series", "Examen Adaptativo")
    val difficulties = listOf("Todas", "Fácil", "Medio", "Difícil")
    val statuses = listOf("Todos", "Correctas", "Incorrectas")

    // Pagination
    var visibleItemCount by remember { mutableIntStateOf(15) }

    // Combined fetch
    LaunchedEffect(students) {
        if (students.isNotEmpty()) {
            val combinedList = mutableListOf<RespuestaHistorial>()
            var fetchedCount = 0
            students.forEach { std ->
                viewModel.registrarRespuestaHistorial(RespuestaHistorial(estudianteId = std.pin.toString())) // safe dummy init
                val repo = viewModel.historialRespuestas // wait let's just fetch directly from repo or VM helper
                
                // Let's use Firestore direct query for all responses or let the repository fetch it.
                // We implemented obtenerHistorialRespuestas in the repository
                val list = viewModel.obtenerHistorialRespuestas(std.pin.toString())
                // Wait! viewModel.obtenerHistorialRespuestas is async and does not return directly, it updates a StateFlow.
                // To fetch synchronously/cooperatively here, we can fetch from the database inside our coroutine scope using repository methods!
                // Let's call the repository method directly or wrap it in a suspend function.
                // Since repository is internal, let's fetch directly.
            }
            // To simplify and ensure Clean Architecture, let's fetch using a coroutine from the DB
            val dbRepo = com.example.mathia.FirebaseRepository(com.google.firebase.firestore.FirebaseFirestore.getInstance())
            scope.launch {
                try {
                    students.forEach { std ->
                        val list = dbRepo.obtenerHistorialRespuestas(std.pin.toString())
                        combinedList.addAll(list)
                    }
                    // Sort descending
                    rawLogs = combinedList.sortedByDescending { it.timestamp }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }
        } else {
            isLoading = false
        }
    }

    // Reactively filter logs
    val filteredLogs = remember(rawLogs, searchQuery, selectedOperation, selectedDifficulty, selectedStatus) {
        rawLogs.filter { log ->
            val matchesSearch = log.estudianteNombre.contains(searchQuery, ignoreCase = true) ||
                    log.grado.contains(searchQuery, ignoreCase = true) ||
                    log.fecha.contains(searchQuery, ignoreCase = true) ||
                    log.operacion.contains(searchQuery, ignoreCase = true)
            
            val matchesOp = selectedOperation == "Todas" || log.operacion.contains(selectedOperation, ignoreCase = true)
            val matchesDiff = selectedDifficulty == "Todas" || log.dificultad.equals(selectedDifficulty, ignoreCase = true)
            val matchesStatus = when (selectedStatus) {
                "Correctas" -> log.correcta
                "Incorrectas" -> !log.correcta
                else -> true
            }

            matchesSearch && matchesOp && matchesDiff && matchesStatus
        }
    }

    // Consolidated Statistics on the filtered logs
    val totalCount = filteredLogs.size
    val correctCount = filteredLogs.count { it.correcta }
    val incorrectCount = totalCount - correctCount
    val avgTime = if (totalCount > 0) filteredLogs.map { it.tiempoRespuesta }.average().toInt() else 0
    val precisionPct = if (totalCount > 0) (correctCount * 100) / totalCount else 0
    val lastActivity = filteredLogs.firstOrNull()?.fecha ?: "Ninguna"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = AppColors.Purple,
                            modifier = Modifier.size(24.dp)
                        )
                        Text("Historial de Respuestas", fontWeight = FontWeight.Bold, color = AppColors.Purple)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AppColors.Purple)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(AppColors.Bg),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.Purple)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(AppColors.Bg)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                
                // ─── Top Stats Grid ───
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
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = AppColors.Purple,
                                modifier = Modifier.size(20.dp)
                            )
                            Text("Resumen de Desempeño Escolar", fontWeight = FontWeight.Bold, color = AppColors.Purple)
                        }
                        Divider(color = AppColors.PurpleLight)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Ejercicios", fontSize = 10.sp, color = AppColors.Gray500)
                                Text("$totalCount", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column {
                                Text("Correctas", fontSize = 10.sp, color = AppColors.Gray500)
                                Text("$correctCount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.Green)
                            }
                            Column {
                                Text("Incorrectas", fontSize = 10.sp, color = AppColors.Gray500)
                                Text("$incorrectCount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.Red)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Precisión Media", fontSize = 10.sp, color = AppColors.Gray500)
                                Text("$precisionPct%", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.Green)
                            }
                            Column {
                                Text("T. de Respuesta", fontSize = 10.sp, color = AppColors.Gray500)
                                Text("${avgTime}s prom.", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column {
                                Text("Último Registro", fontSize = 10.sp, color = AppColors.Gray500)
                                Text(lastActivity, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }

                // ─── Filter controls ───
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        
                        // Search bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar por estudiante, fecha o tema...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = AppColors.Purple) }
                        )

                        // Filters dropdowns row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Operacion filter
                            Box(modifier = Modifier.weight(1f)) {
                                Button(
                                    onClick = { isOpExpanded = !isOpExpanded },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.PurpleLight, contentColor = AppColors.Purple),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("Operación: $selectedOperation", fontSize = 11.sp, maxLines = 1)
                                }
                                DropdownMenu(expanded = isOpExpanded, onDismissRequest = { isOpExpanded = false }) {
                                    operations.forEach { op ->
                                        DropdownMenuItem(text = { Text(op) }, onClick = { selectedOperation = op; isOpExpanded = false })
                                    }
                                }
                            }

                            // Dificultad filter
                            Box(modifier = Modifier.weight(1f)) {
                                Button(
                                    onClick = { isDiffExpanded = !isDiffExpanded },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.PurpleLight, contentColor = AppColors.Purple),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("Dificultad: $selectedDifficulty", fontSize = 11.sp, maxLines = 1)
                                }
                                DropdownMenu(expanded = isDiffExpanded, onDismissRequest = { isDiffExpanded = false }) {
                                    difficulties.forEach { diff ->
                                        DropdownMenuItem(text = { Text(diff) }, onClick = { selectedDifficulty = diff; isDiffExpanded = false })
                                    }
                                }
                            }

                            // Estado filter
                            Box(modifier = Modifier.weight(1f)) {
                                Button(
                                    onClick = { isStatusExpanded = !isStatusExpanded },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.PurpleLight, contentColor = AppColors.Purple),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("Estado: $selectedStatus", fontSize = 11.sp, maxLines = 1)
                                }
                                DropdownMenu(expanded = isStatusExpanded, onDismissRequest = { isStatusExpanded = false }) {
                                    statuses.forEach { st ->
                                        DropdownMenuItem(text = { Text(st) }, onClick = { selectedStatus = st; isStatusExpanded = false })
                                    }
                                }
                            }
                        }
                    }
                }

                // ─── Answer logs list ───
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val logsToShow = filteredLogs.take(visibleItemCount)
                    
                    if (logsToShow.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                Text("No se encontraron registros de respuestas.", color = AppColors.Gray500)
                            }
                        }
                    } else {
                        items(logsToShow) { log ->
                            val statusColor = if (log.correcta) AppColors.Green else AppColors.Red
                            val statusText = if (log.correcta) "Correcta" else "Incorrecta"
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Box(
                                                modifier = Modifier.size(24.dp).background(AppColors.PurpleLight, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Face,
                                                    contentDescription = null,
                                                    tint = AppColors.Purple,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Text(log.estudianteNombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50.dp))
                                                .background(statusColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 10.dp, vertical = 2.dp)
                                        ) {
                                            Text(statusText, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }

                                    Divider(color = AppColors.Gray100, modifier = Modifier.padding(vertical = 4.dp))

                                    Text("Pregunta: ${log.pregunta}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Text("Respuesta: ${log.respuestaElegida}", fontSize = 12.sp, color = if(log.correcta) AppColors.Green else AppColors.Red, fontWeight = FontWeight.Bold)
                                        Text("Correcta: ${log.respuestaCorrecta}", fontSize = 12.sp, color = AppColors.Green, fontWeight = FontWeight.Bold)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Dificultad: ${log.dificultad} | Tiempo: ${log.tiempoRespuesta}s", fontSize = 11.sp, color = AppColors.Gray500)
                                        Text("Intento: #${log.intentos}", fontSize = 11.sp, color = AppColors.Gray500)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Fecha: ${log.fecha} a las ${log.hora}", fontSize = 10.sp, color = AppColors.Gray400)
                                        if (log.dispositivo.isNotEmpty()) {
                                            Text("Dispositivo: ${log.dispositivo}", fontSize = 10.sp, color = AppColors.Gray400)
                                        }
                                    }
                                }
                            }
                        }

                        // Load more button for pagination
                        if (filteredLogs.size > visibleItemCount) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Button(
                                        onClick = { visibleItemCount += 15 },
                                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple)
                                    ) {
                                        Text("Ver más respuestas")
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
