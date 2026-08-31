package com.example.mathia.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.mathia.AppColors
import com.example.mathia.R
import com.example.mathia.StudentViewModel
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteParentProfileScreen(
    uid: String,
    email: String,
    onCompleteSuccess: () -> Unit,
    onLogout: () -> Unit,
    viewModel: StudentViewModel
) {
    val context = LocalContext.current
    var parentName by remember { mutableStateOf(email.substringBefore("@").replaceFirstChar { it.uppercase() }) }
    
    // Child registration states
    var childName by remember { mutableStateOf("") }
    var childSchool by remember { mutableStateOf("") }
    var childGradeSelected by remember { mutableStateOf("Selecciona el Grado del Niño") }
    var childSeccionSelected by remember { mutableStateOf("Selecciona la Sección del Niño") }
    var childPin by remember { mutableStateOf("") }
    var classroomCodeInput by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    var isGradeExpanded by remember { mutableStateOf(false) }
    var isSeccionExpanded by remember { mutableStateOf(false) }

    val grades = listOf("1ro de Primaria", "2do de Primaria", "3ro de Primaria", "4to de Primaria", "5to de Primaria", "6to de Primaria")
    val secciones = listOf("Sección A", "Sección B", "Sección C", "Sección D", "Sección E")

    val imageLoader = remember(context) {
        ImageLoader.Builder(context).components {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) add(ImageDecoderDecoder.Factory())
            else add(GifDecoder.Factory())
        }.build()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Mateo welcome speech bubble & GIF
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = R.drawable.ajolote,
                        imageLoader = imageLoader
                    ),
                    contentDescription = "Mateo el Ajolote",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(16.dp))
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "¡Hola! Configura tu cuenta de padres y registra a tu pequeño matemático para ver sus avances.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Información del Padre",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Purple
                    )

                    if (isLoading) {
                        CircularProgressIndicator(color = AppColors.Purple)
                    } else {
                        // Parent Name
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Nombre Completo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.Gray600)
                            OutlinedTextField(
                                value = parentName,
                                onValueChange = { parentName = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Divider(color = AppColors.PurpleLight, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                        Text(
                            text = "Registra a tu Hijo/a",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Purple,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        // Child Name
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Nombre del Niño", fontSize = 11.sp, color = AppColors.Gray600)
                            OutlinedTextField(
                                value = childName,
                                onValueChange = { childName = it },
                                placeholder = { Text("Ej. Pedrito") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Child School
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Colegio del Niño", fontSize = 11.sp, color = AppColors.Gray600)
                            OutlinedTextField(
                                value = childSchool,
                                onValueChange = { childSchool = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Child Grade
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Grado del Niño", fontSize = 11.sp, color = AppColors.Gray600)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = childGradeSelected,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth().clickable { isGradeExpanded = !isGradeExpanded },
                                    enabled = false,
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                                )
                                Box(modifier = Modifier.matchParentSize().clickable { isGradeExpanded = !isGradeExpanded })
                                DropdownMenu(expanded = isGradeExpanded, onDismissRequest = { isGradeExpanded = false }) {
                                    grades.forEach { g ->
                                        DropdownMenuItem(text = { Text(g) }, onClick = { childGradeSelected = g; isGradeExpanded = false })
                                    }
                                }
                            }
                        }

                        // Child Section
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Sección del Niño", fontSize = 11.sp, color = AppColors.Gray600)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = childSeccionSelected,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth().clickable { isSeccionExpanded = !isSeccionExpanded },
                                    enabled = false,
                                    shape = RoundedCornerShape(12.dp),
                                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                                )
                                Box(modifier = Modifier.matchParentSize().clickable { isSeccionExpanded = !isSeccionExpanded })
                                DropdownMenu(expanded = isSeccionExpanded, onDismissRequest = { isSeccionExpanded = false }) {
                                    secciones.forEach { s ->
                                        DropdownMenuItem(text = { Text(s) }, onClick = { childSeccionSelected = s; isSeccionExpanded = false })
                                    }
                                }
                            }
                        }

                        // Child PIN
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("PIN de 4 dígitos para tu hijo", fontSize = 11.sp, color = AppColors.Gray600)
                            OutlinedTextField(
                                value = childPin,
                                onValueChange = { if (it.length <= 4) childPin = it },
                                placeholder = { Text("Ej. 1234") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = PasswordVisualTransformation()
                            )
                        }

                        // Optional Classroom Code (#18, #19, #20)
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Código del aula (opcional)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.Purple)
                            Text("Si el docente te proporcionó un código, puedes ingresarlo ahora. También podrás hacerlo más adelante.", fontSize = 11.sp, color = AppColors.Gray500)
                            OutlinedTextField(
                                value = classroomCodeInput,
                                onValueChange = { classroomCodeInput = it.uppercase() },
                                placeholder = { Text("Ej. MATH-2A-84F") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val isValid = parentName.isNotBlank() && childName.isNotBlank() && childSchool.isNotBlank() && childPin.length == 4 && !childGradeSelected.contains("Selecciona") && !childSeccionSelected.contains("Selecciona")

                        Button(
                            onClick = {
                                isLoading = true
                                val db = FirebaseFirestore.getInstance()
                                
                                val parentData = hashMapOf(
                                    "uid" to uid,
                                    "email" to email,
                                    "nombre" to parentName,
                                    "rol" to "padre",
                                    "perfil_completo" to true,
                                    "estudiante_pin" to childPin.toInt()
                                )

                                val childData = hashMapOf<String, Any>(
                                    "nombre" to childName,
                                    "colegio" to childSchool,
                                    "grado" to childGradeSelected,
                                    "seccion" to childSeccionSelected,
                                    "docente_asignado" to "Sin asignar",
                                    "fecha_creacion" to System.currentTimeMillis(),
                                    "pin" to childPin.toInt(),
                                    "estrellas" to 0,
                                    "xp" to 0,
                                    "precision" to 0.0,
                                    "nivel_actual" to 1,
                                    "padre_email" to email,
                                    "avatar" to "default",
                                    "equipped_theme" to "Lila Clásico",
                                    "unlocked_avatars" to listOf("default"),
                                    "unlocked_themes" to listOf("Lila Clásico"),
                                    "streak" to 0,
                                    "total_preguntas" to 0,
                                    "correctas" to 0,
                                    "incorrectas" to 0,
                                    "tiempo_total" to 0L,
                                    "tiempo_promedio" to 0.0,
                                    "diagnostico_realizado" to false,
                                    "skills" to mapOf(
                                        "Sumas" to 0,
                                        "Restas" to 0,
                                        "Multiplicación" to 0,
                                        "Fracciones" to 0,
                                        "Series" to 0
                                    ),
                                    "incorrectas_por_tema" to emptyMap<String, Int>(),
                                    "recomendaciones" to listOf("¡Realiza el Examen Adaptativo para descubrir tu nivel actual de matemáticas!"),
                                    "aulas" to emptyList<String>()
                                )

                                val proceedSave = { updatedChildData: Map<String, Any> ->
                                    db.collection("usuarios").document(uid).set(parentData)
                                        .addOnSuccessListener {
                                            db.collection("usuarios").document(childPin).set(updatedChildData)
                                                .addOnSuccessListener {
                                                    isLoading = false
                                                    Toast.makeText(context, "¡Perfil de padre y estudiante creados!", Toast.LENGTH_SHORT).show()
                                                    onCompleteSuccess()
                                                }
                                                .addOnFailureListener { e ->
                                                    isLoading = false
                                                    Toast.makeText(context, "Error al crear perfil del niño: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            isLoading = false
                                            Toast.makeText(context, "Error al crear perfil del padre: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }

                                if (classroomCodeInput.trim().isNotBlank()) {
                                    viewModel.validarCodigoAula(classroomCodeInput.trim()) { result ->
                                        if (result is com.example.mathia.model.ClassroomValidationResult.Success) {
                                            val validClassroom = result.classroom
                                            childData["aulas"] = listOf(validClassroom.classroomId)
                                            childData["docente_asignado"] = validClassroom.teacherName
                                            if (validClassroom.section.isNotBlank()) childData["seccion"] = validClassroom.section
                                            if (validClassroom.grade.isNotBlank()) childData["grado"] = validClassroom.grade
                                            Toast.makeText(context, "¡Aula ${validClassroom.name} vinculada con éxito!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Código de aula no encontrado. Podrás vincularlo más adelante.", Toast.LENGTH_SHORT).show()
                                        }
                                        proceedSave(childData)
                                    }
                                } else {
                                    proceedSave(childData)
                                }
                            },
                            enabled = isValid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple)
                        ) {
                            Text("Guardar y Registrar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(2.dp, AppColors.Purple)
            ) {
                Text("Cerrar Sesión", color = AppColors.Purple, fontWeight = FontWeight.Bold)
            }
        }
    }
}
