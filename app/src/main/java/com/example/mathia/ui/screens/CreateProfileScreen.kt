package com.example.mathia.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mathia.AppColors
import com.example.mathia.ui.components.AvatarIcon

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateProfileScreen(
    onCreated: (String, String, String, String, Int, String, String, String) -> Unit, // (name, lastName, grade, seccion, edad, colegio, avatar, pin)
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var colegio by remember { mutableStateOf("") }
    var edad by remember { mutableIntStateOf(6) }
    var gradeSelected by remember { mutableStateOf("1ro de Primaria") }
    var seccionSelected by remember { mutableStateOf("Sección A") }
    var pin by remember { mutableStateOf("") }
    var avatarIdx by remember { mutableIntStateOf(0) }

    var isGradeExpanded by remember { mutableStateOf(false) }
    var isSeccionExpanded by remember { mutableStateOf(false) }
    var isEdadExpanded by remember { mutableStateOf(false) }

    val avatars = listOf("superhero", "rocket", "wizard", "unicorn", "dragon", "fox", "panda", "lion", "tiger", "koala")
    val grades = listOf("1ro de Primaria", "2do de Primaria", "3ro de Primaria", "4to de Primaria", "5to de Primaria", "6to de Primaria")
    val secciones = listOf("Sección A", "Sección B", "Sección C", "Sección D", "Sección E")
    val edades = (5..15).toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Bg)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Crear Perfil Estudiante", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = AppColors.Purple)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                // Nombre
                Column {
                    Text("¿Cómo te llamas?", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Tu nombre...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Apellido
                Column {
                    Text("Apellido", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        placeholder = { Text("Tu apellido...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Colegio
                Column {
                    Text("Nombre de tu Colegio", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = colegio,
                        onValueChange = { colegio = it },
                        placeholder = { Text("Ej: San Agustín") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Edad
                Column {
                    Text("¿Cuántos años tienes?", fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = "$edad años",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { isEdadExpanded = !isEdadExpanded },
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.Gray
                            ),
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { isEdadExpanded = !isEdadExpanded })
                        DropdownMenu(expanded = isEdadExpanded, onDismissRequest = { isEdadExpanded = false }) {
                            edades.forEach { e ->
                                DropdownMenuItem(text = { Text("$e años") }, onClick = { edad = e; isEdadExpanded = false })
                            }
                        }
                    }
                }

                // Grado escolar
                Column {
                    Text("Grado escolar", fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = gradeSelected,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { isGradeExpanded = !isGradeExpanded },
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.Gray
                            ),
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { isGradeExpanded = !isGradeExpanded })
                        DropdownMenu(expanded = isGradeExpanded, onDismissRequest = { isGradeExpanded = false }) {
                            grades.forEach { g ->
                                DropdownMenuItem(text = { Text(g) }, onClick = { gradeSelected = g; isGradeExpanded = false })
                            }
                        }
                    }
                }

                // Sección / Salón
                Column {
                    Text("Sección", fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = seccionSelected,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { isSeccionExpanded = !isSeccionExpanded },
                            enabled = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.Gray
                            ),
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { isSeccionExpanded = !isSeccionExpanded })
                        DropdownMenu(expanded = isSeccionExpanded, onDismissRequest = { isSeccionExpanded = false }) {
                            secciones.forEach { s ->
                                DropdownMenuItem(text = { Text(s) }, onClick = { seccionSelected = s; isSeccionExpanded = false })
                            }
                        }
                    }
                }

                // Avatar
                Column {
                    Text("Elige tu avatar", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        avatars.forEachIndexed { i, em ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (avatarIdx == i) AppColors.AmberLight else AppColors.Gray100)
                                    .border(2.dp, if (avatarIdx == i) AppColors.Amber else Color.Transparent, RoundedCornerShape(12.dp))
                                    .clickable { avatarIdx = i },
                                contentAlignment = Alignment.Center
                            ) {
                                AvatarIcon(avatarKey = em, modifier = Modifier.size(32.dp), tint = AppColors.Purple)
                            }
                        }
                    }
                }

                // Apodo
                Column {
                    Text("Apodo / Nombre Gamer (Opcional)", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = lastName, // using as placeholder or field
                        onValueChange = { lastName = it },
                        placeholder = { Text("Ej. SuperMathia, SofiEstelar...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // PIN -> Código Secreto
                Column {
                    Text("Crea tu Código Secreto de 4 dígitos", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 4) pin = it },
                        placeholder = { Text("1234") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                
                Spacer(modifier = Modifier.height(8.dp))

                val isValid = name.isNotBlank() && lastName.isNotBlank() && colegio.isNotBlank() && pin.length == 4

                Button(
                    onClick = {
                        if (isValid) {
                            onCreated(name, lastName, gradeSelected, seccionSelected, edad, colegio, avatars[avatarIdx], pin)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple),
                    enabled = isValid
                ) {
                    Text("Continuar →", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(50.dp),
                    border = BorderStroke(2.dp, AppColors.Purple)
                ) {
                    Text("Volver", color = AppColors.Purple, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
