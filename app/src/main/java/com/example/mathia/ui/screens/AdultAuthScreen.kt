package com.example.mathia.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathia.AppColors
import com.example.mathia.GoogleAuthHelper
import com.example.mathia.GoogleAuthResult
import com.example.mathia.StudentViewModel
import com.example.mathia.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdultAuthScreen(
    rol: String, // "docente" or "padres"
    onBack: () -> Unit,
    onLoginSuccess: (String, String, String) -> Unit, // (rol, email, uid)
    viewModel: StudentViewModel
) {
    val context = LocalContext.current
    val authHelper = remember { GoogleAuthHelper(context) }
    
    // Auth Mode: 0 = Login, 1 = Register
    var authMode by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }

    // Form inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }

    // Parent Registration inputs
    var hijoNombre by remember { mutableStateOf("") }
    var hijoColegio by remember { mutableStateOf("") }
    var hijoGrado by remember { mutableStateOf("Selecciona el Grado") }
    var hijoPin by remember { mutableStateOf("") }
    var isHijoGradeExpanded by remember { mutableStateOf(false) }

    // Teacher Registration inputs
    var docenteColegio by remember { mutableStateOf("") }
    var docenteGrado by remember { mutableStateOf("Selecciona tu Grado") }
    var docenteSeccion by remember { mutableStateOf("Selecciona tu Sección") }
    var isDocenteGradeExpanded by remember { mutableStateOf(false) }
    var isDocenteSeccionExpanded by remember { mutableStateOf(false) }

    val grados = listOf("1ro de Primaria", "2do de Primaria", "3ro de Primaria", "4to de Primaria", "5to de Primaria", "6to de Primaria")
    val secciones = listOf("Sección A", "Sección B", "Sección C", "Sección D", "Sección E")

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val accountResult = authHelper.parseResult(result.data)
        when (accountResult) {
            is GoogleAuthResult.Success -> {
                val account = accountResult.account
                val idToken = account.idToken ?: ""
                val userEmail = account.email ?: ""
                if (idToken.isNotEmpty()) {
                    viewModel.loginConGoogle(idToken, userEmail, rol) { success, isComplete ->
                        isLoading = false
                        if (success) {
                            val firebaseUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            val nextRole = if (rol == "docente") {
                                if (isComplete) "docente" else "docente_incomplete"
                            } else {
                                if (isComplete) "padres" else "padres_incomplete"
                            }
                            onLoginSuccess(nextRole, userEmail, firebaseUid)
                        } else {
                            Toast.makeText(context, "Error de verificación con Firebase Authentication", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    isLoading = false
                    Toast.makeText(context, "No se pudo obtener el token de Google", Toast.LENGTH_SHORT).show()
                }
            }
            is GoogleAuthResult.Error -> {
                isLoading = false
                if (accountResult.code != 12501) { // Do not show message if user cancelled
                    Toast.makeText(context, accountResult.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
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
                    text = if (rol == "docente") "Acceso Docente" else "Acceso Padres",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Purple
                )

                // Tab Selector
                TabRow(
                    selectedTabIndex = authMode,
                    containerColor = Color.Transparent,
                    contentColor = AppColors.Purple,
                    divider = {}
                ) {
                    Tab(
                        selected = authMode == 0,
                        onClick = { authMode = 0 },
                        text = { Text("Iniciar Sesión", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = authMode == 1,
                        onClick = { authMode = 1 },
                        text = { Text("Registrarse", fontWeight = FontWeight.Bold) }
                    )
                }

                Divider(color = AppColors.PurpleLight, thickness = 1.dp)

                if (isLoading) {
                    CircularProgressIndicator(color = AppColors.Purple)
                } else {
                    // Common fields
                    if (authMode == 1) {
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Nombre Completo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.Gray600)
                            OutlinedTextField(
                                value = nombre,
                                onValueChange = { nombre = it },
                                placeholder = { Text("Ej. Juan Pérez") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Email field
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Correo Electrónico", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.Gray600)
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("ejemplo@correo.com") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Password field
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Contraseña", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppColors.Gray600)
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("••••••••") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Registration specific sub-forms
                    if (authMode == 1) {
                        if (rol == "padres") {
                            Text(
                                "Registra a tu Hijo/a",
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Purple,
                                fontSize = 14.sp,
                                modifier = Modifier.align(Alignment.Start)
                            )

                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Nombre del Niño", fontSize = 11.sp, color = AppColors.Gray600)
                                OutlinedTextField(
                                    value = hijoNombre,
                                    onValueChange = { hijoNombre = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Colegio del Niño", fontSize = 11.sp, color = AppColors.Gray600)
                                OutlinedTextField(
                                    value = hijoColegio,
                                    onValueChange = { hijoColegio = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Grado del Niño", fontSize = 11.sp, color = AppColors.Gray600)
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = hijoGrado,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth().clickable { isHijoGradeExpanded = !isHijoGradeExpanded },
                                        enabled = false,
                                        shape = RoundedCornerShape(12.dp),
                                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                                    )
                                    Box(modifier = Modifier.matchParentSize().clickable { isHijoGradeExpanded = !isHijoGradeExpanded })
                                    DropdownMenu(expanded = isHijoGradeExpanded, onDismissRequest = { isHijoGradeExpanded = false }) {
                                        grados.forEach { g ->
                                            DropdownMenuItem(text = { Text(g) }, onClick = { hijoGrado = g; isHijoGradeExpanded = false })
                                        }
                                    }
                                }
                            }

                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("PIN de 4 dígitos para tu hijo", fontSize = 11.sp, color = AppColors.Gray600)
                                OutlinedTextField(
                                    value = hijoPin,
                                    onValueChange = { if (it.length <= 4) hijoPin = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    visualTransformation = PasswordVisualTransformation()
                                )
                            }
                        } else {
                            Text(
                                "Configura tu Aula",
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Purple,
                                fontSize = 14.sp,
                                modifier = Modifier.align(Alignment.Start)
                            )

                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Nombre del Colegio/Escuela", fontSize = 11.sp, color = AppColors.Gray600)
                                OutlinedTextField(
                                    value = docenteColegio,
                                    onValueChange = { docenteColegio = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Grado que enseña", fontSize = 11.sp, color = AppColors.Gray600)
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = docenteGrado,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth().clickable { isDocenteGradeExpanded = !isDocenteGradeExpanded },
                                        enabled = false,
                                        shape = RoundedCornerShape(12.dp),
                                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                                    )
                                    Box(modifier = Modifier.matchParentSize().clickable { isDocenteGradeExpanded = !isDocenteGradeExpanded })
                                    DropdownMenu(expanded = isDocenteGradeExpanded, onDismissRequest = { isDocenteGradeExpanded = false }) {
                                        grados.forEach { g ->
                                            DropdownMenuItem(text = { Text(g) }, onClick = { docenteGrado = g; isDocenteGradeExpanded = false })
                                        }
                                    }
                                }
                            }

                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Sección que enseña", fontSize = 11.sp, color = AppColors.Gray600)
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = docenteSeccion,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth().clickable { isDocenteSeccionExpanded = !isDocenteSeccionExpanded },
                                        enabled = false,
                                        shape = RoundedCornerShape(12.dp),
                                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                                    )
                                    Box(modifier = Modifier.matchParentSize().clickable { isDocenteSeccionExpanded = !isDocenteSeccionExpanded })
                                    DropdownMenu(expanded = isDocenteSeccionExpanded, onDismissRequest = { isDocenteSeccionExpanded = false }) {
                                        secciones.forEach { s ->
                                            DropdownMenuItem(text = { Text(s) }, onClick = { docenteSeccion = s; isDocenteSeccionExpanded = false })
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val isEmailValid = email.contains("@") && password.length >= 6
                    val isRegisterFormValid = isEmailValid && nombre.isNotBlank() && (
                        if (rol == "padres") {
                            hijoNombre.isNotBlank() && hijoColegio.isNotBlank() && hijoPin.length == 4 && !hijoGrado.contains("Selecciona")
                        } else {
                            docenteColegio.isNotBlank() && !docenteGrado.contains("Selecciona") && !docenteSeccion.contains("Selecciona")
                        }
                    )

                    Button(
                        onClick = {
                            if (StudentViewModel.isDemoMode) {
                                val expectedRole = if (rol == "padres") "padre" else "docente"
                                if (authMode == 0) {
                                    // Iniciar Sesión con Email/Password en Modo Demo
                                    val uid = if (email == "teacher@mathia.com") "teacher_uid" else (if (email == "parent@mathia.com") "parent_uid" else "mock_user_${email.hashCode()}")
                                    val existing = StudentViewModel.mockUsuarios[uid] as? AdultoFirebase
                                    if (existing != null && existing.rol != expectedRole) {
                                        val displayRole = if (existing.rol == "docente") "Docente" else "Padre"
                                        Toast.makeText(context, "Esta cuenta está registrada como $displayRole en Modo Demo.", Toast.LENGTH_LONG).show()
                                    } else {
                                        if (existing == null) {
                                            StudentViewModel.mockUsuarios[uid] = AdultoFirebase(
                                                uid = uid,
                                                email = email,
                                                rol = expectedRole,
                                                nombre = email.substringBefore("@"),
                                                perfil_completo = true
                                            )
                                        }
                                        onLoginSuccess(rol, email, uid)
                                    }
                                } else {
                                    // Registrar en Modo Demo
                                    val uid = "mock_user_${email.hashCode()}"
                                    if (rol == "padres") {
                                        val parentData = AdultoFirebase(
                                            uid = uid,
                                            email = email,
                                            nombre = nombre,
                                            rol = "padre",
                                            perfil_completo = true,
                                            estudiante_pin = hijoPin.toIntOrNull() ?: 1234
                                        )
                                        StudentViewModel.mockUsuarios[uid] = parentData

                                        val childData = FirebaseStudent(
                                            nombre = hijoNombre,
                                            grado = hijoGrado,
                                            seccion = "Sección A",
                                            edad = 6,
                                            colegio = hijoColegio,
                                            docente_asignado = "Sin asignar",
                                            fecha_creacion = System.currentTimeMillis(),
                                            nivel_actual = 1,
                                            pin = hijoPin.toIntOrNull() ?: 1234,
                                            padre_email = email
                                        )
                                        StudentViewModel.mockUsuarios[hijoPin] = childData
                                        Toast.makeText(context, "¡Registro Demo Exitoso!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess(rol, email, uid)
                                    } else {
                                        val teacherData = AdultoFirebase(
                                            uid = uid,
                                            email = email,
                                            rol = "docente",
                                            nombre = nombre,
                                            colegio = docenteColegio,
                                            grado = docenteGrado,
                                            seccion = docenteSeccion,
                                            perfil_completo = true
                                        )
                                        StudentViewModel.mockUsuarios[uid] = teacherData
                                        Toast.makeText(context, "¡Registro Demo Docente Exitoso!", Toast.LENGTH_SHORT).show()
                                        onLoginSuccess(rol, email, uid)
                                    }
                                }
                                return@Button
                            }

                            isLoading = true
                            val auth = FirebaseAuth.getInstance()
                            if (authMode == 0) {
                                // Iniciar Sesión con Email/Password
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnSuccessListener { result ->
                                        val uid = result.user?.uid ?: ""
                                        val db = FirebaseFirestore.getInstance()
                                        db.collection("usuarios").document(uid).get()
                                            .addOnSuccessListener { doc ->
                                                isLoading = false
                                                if (doc.exists()) {
                                                    val dbRole = doc.getString("rol") ?: ""
                                                    val expectedRole = if (rol == "padres") "padre" else "docente"
                                                    if (dbRole != expectedRole) {
                                                        auth.signOut()
                                                        val displayRole = if (dbRole == "docente") "Docente" else "Padre"
                                                        Toast.makeText(context, "Esta cuenta está registrada como $displayRole.", Toast.LENGTH_LONG).show()
                                                    } else {
                                                        val isComplete = doc.getBoolean("perfil_completo") ?: false
                                                        if (isComplete) onLoginSuccess(rol, email, uid)
                                                        else {
                                                            val nextRole = if (rol == "docente") "docente_incomplete" else "padres_incomplete"
                                                            onLoginSuccess(nextRole, email, uid)
                                                        }
                                                    }
                                                } else {
                                                    val nextRole = if (rol == "docente") "docente_incomplete" else "padres_incomplete"
                                                    onLoginSuccess(nextRole, email, uid)
                                                }
                                            }
                                            .addOnFailureListener {
                                                isLoading = false
                                                val nextRole = if (rol == "docente") "docente_incomplete" else "padres_incomplete"
                                                onLoginSuccess(nextRole, email, uid)
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        Toast.makeText(context, "Error: ${e.message}. \nPrueba activando el 'Modo Demo' en la pantalla principal si no posees conexión a Firebase.", Toast.LENGTH_LONG).show()
                                    }
                            } else {
                                // Registrar con Email/Password
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnSuccessListener { result ->
                                        val uid = result.user?.uid ?: ""
                                        val db = FirebaseFirestore.getInstance()

                                        if (rol == "padres") {
                                            // 1. Create parent profile in 'usuarios' with uid
                                            val parentData = hashMapOf(
                                                "uid" to uid,
                                                "email" to email,
                                                "nombre" to nombre,
                                                "rol" to "padre",
                                                "perfil_completo" to true,
                                                "estudiante_pin" to hijoPin.toInt()
                                            )
                                            db.collection("usuarios").document(uid).set(parentData)
                                                .addOnSuccessListener {
                                                    // 2. Create child profile in 'usuarios' with pin as doc id
                                                    val childData = hashMapOf(
                                                        "nombre" to hijoNombre,
                                                        "colegio" to hijoColegio,
                                                        "grado" to hijoGrado,
                                                        "pin" to hijoPin.toInt(),
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
                                                        "recomendaciones" to listOf("¡Realiza el Examen Adaptativo para descubrir tu nivel actual de matemáticas!")
                                                    )
                                                    db.collection("usuarios").document(hijoPin).set(childData)
                                                        .addOnSuccessListener {
                                                            isLoading = false
                                                            Toast.makeText(context, "¡Registro Exitoso!", Toast.LENGTH_SHORT).show()
                                                            onLoginSuccess(rol, email, uid)
                                                        }
                                                }
                                        } else {
                                            // 1. Create teacher profile in 'usuarios' with uid
                                            val teacherData = hashMapOf(
                                                "uid" to uid,
                                                "email" to email,
                                                "rol" to "docente",
                                                "nombre" to nombre,
                                                "colegio" to docenteColegio,
                                                "grado" to docenteGrado,
                                                "seccion" to docenteSeccion,
                                                "perfil_completo" to true
                                            )
                                            db.collection("usuarios").document(uid).set(teacherData)
                                                .addOnSuccessListener {
                                                    isLoading = false
                                                    Toast.makeText(context, "¡Registro de docente Exitoso!", Toast.LENGTH_SHORT).show()
                                                    onLoginSuccess(rol, email, uid)
                                                }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        Toast.makeText(context, "Error de autenticación: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        },
                        enabled = if (authMode == 0) isEmailValid else isRegisterFormValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Purple,
                            disabledContainerColor = AppColors.Gray400
                        ),
                        shape = RoundedCornerShape(50.dp)
                    ) {
                        Text(
                            text = if (authMode == 0) "Iniciar Sesión" else "Completar Registro",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Divider for Google Auth
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(modifier = Modifier.weight(1f), color = AppColors.Gray200)
                        Text(" O ", color = AppColors.Gray400, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
                        Divider(modifier = Modifier.weight(1f), color = AppColors.Gray200)
                    }

                    // Google Sign-In Button
                    Button(
                        onClick = {
                            if (StudentViewModel.isDemoMode) {
                                val mockEmail = if (rol == "docente") "teacher@mathia.com" else "parent@mathia.com"
                                val mockUid = if (rol == "docente") "teacher_uid" else "parent_uid"
                                Toast.makeText(context, "Simulación Google Sign-In con: $mockEmail", Toast.LENGTH_SHORT).show()
                                onLoginSuccess(rol, mockEmail, mockUid)
                                return@Button
                            }
                            isLoading = true
                            googleSignInLauncher.launch(authHelper.getSignInIntent())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = AppColors.Gray800),
                        shape = RoundedCornerShape(50.dp),
                        border = BorderStroke(2.dp, AppColors.Gray200)
                    ) {
                        Text(
                            text = if (authMode == 0) "Iniciar con Google" else "Registrarse con Google",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(50.dp),
                    border = BorderStroke(2.dp, AppColors.Purple)
                ) {
                    Text("Volver", color = AppColors.Purple, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
