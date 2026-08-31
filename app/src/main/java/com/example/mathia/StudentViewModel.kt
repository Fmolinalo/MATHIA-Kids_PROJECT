package com.example.mathia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.mathia.model.*
import com.mathia.kids.data.model.HintRequest
import com.mathia.kids.data.model.HintResponse
import com.mathia.kids.data.model.ProgressSummaryRequest
import com.mathia.kids.data.model.ProgressSummaryResponse
import com.mathia.kids.network.MathIaApiService
import com.mathia.kids.data.fallback.LocalFallbackEngine

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.mathia.testing.MockDataStore

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val repository: FirebaseRepository
) : ViewModel() {

    companion object {
        var isDemoMode: Boolean
            get() = MockDataStore.isDemoMode
            set(value) { MockDataStore.isDemoMode = value }

        val mockUsuarios get() = MockDataStore.mockUsuarios
        val mockHistorial get() = MockDataStore.mockHistorial
        val mockReportes get() = MockDataStore.mockReportes
        val mockObservaciones get() = MockDataStore.mockObservaciones
        val mockNotificaciones get() = MockDataStore.mockNotificaciones

        fun resetMockData() {
            MockDataStore.resetMockData()
        }
    }

    fun restablecerEstadoLocal(context: android.content.Context, onComplete: () -> Unit) {
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val sharedPref = context.getSharedPreferences("mathia_prefs", android.content.Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        resetMockData()
        _alumnoActual.value = null
        _historialRespuestas.value = emptyList()
        _observaciones.value = emptyList()
        _reportesSesiones.value = emptyList()
        _notificaciones.value = emptyList()

        onComplete()
    }

    private val _alumnoActual = MutableStateFlow<FirebaseStudent?>(null)
    val alumnoActual: StateFlow<FirebaseStudent?> = _alumnoActual

    private val _historialRespuestas = MutableStateFlow<List<RespuestaHistorial>>(emptyList())
    val historialRespuestas: StateFlow<List<RespuestaHistorial>> = _historialRespuestas

    private val _observaciones = MutableStateFlow<List<Observacion>>(emptyList())
    val observaciones: StateFlow<List<Observacion>> = _observaciones

    private val _reportesSesiones = MutableStateFlow<List<ReporteSesion>>(emptyList())
    val reportesSesiones: StateFlow<List<ReporteSesion>> = _reportesSesiones

    private val _notificaciones = MutableStateFlow<List<NotificacionFirebase>>(emptyList())
    val notificaciones: StateFlow<List<NotificacionFirebase>> = _notificaciones

    private val _duelos = MutableStateFlow<List<com.example.mathia.model.DuelMatch>>(emptyList())
    val duelos: StateFlow<List<com.example.mathia.model.DuelMatch>> = _duelos

    private var duelsListener: com.google.firebase.firestore.ListenerRegistration? = null

    fun iniciarEscuchaDuelos(studentPin: String) {
        duelsListener?.remove()
        duelsListener = repository.escucharDuelosEstudiante(studentPin) { list ->
            _duelos.value = list
        }
    }

    fun buscarEstudiantePorApodo(apodo: String, onResult: (FirebaseStudent?) -> Unit) {
        viewModelScope.launch {
            val student = repository.buscarEstudiantePorApodo(apodo)
            onResult(student)
        }
    }

    fun guardarFcmToken(studentPin: String, token: String) {
        viewModelScope.launch {
            repository.guardarFcmToken(studentPin, token)
        }
    }

    fun crearDesafioDuelo(
        challenged: FirebaseStudent,
        topic: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val current = _alumnoActual.value ?: return onResult(false, "No hay alumno activo.")
        viewModelScope.launch {
            val res = repository.crearDesafioDuelo(current, challenged, topic)
            onResult(res.first, res.second)
        }
    }

    fun aceptarDesafioDuelo(duelId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = repository.aceptarDesafioDuelo(duelId)
            onResult(ok)
        }
    }

    fun rechazarDesafioDuelo(duelId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = repository.rechazarDesafioDuelo(duelId)
            onResult(ok)
        }
    }

    fun cancelarDesafioDuelo(duelId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = repository.cancelarDesafioDuelo(duelId)
            onResult(ok)
        }
    }

    fun registrarRespuestaDuelo(
        duelId: String,
        questionId: String,
        selectedAnswer: String,
        correct: Boolean,
        timeSpent: Long
    ) {
        val pin = _alumnoActual.value?.pin?.toString() ?: return
        viewModelScope.launch {
            repository.registrarRespuestaDuelo(duelId, pin, questionId, selectedAnswer, correct, timeSpent)
        }
    }

    fun guardarProgresoYFinalizarDuelo(
        duelId: String,
        score: Int,
        totalTime: Long,
        onResult: (Boolean) -> Unit
    ) {
        val pin = _alumnoActual.value?.pin?.toString() ?: return onResult(false)
        viewModelScope.launch {
            val ok = repository.guardarProgresoYFinalizarDuelo(duelId, pin, score, totalTime)
            if (ok) {
                // Refresh student XP and stars
                val updated = repository.obtenerAlumnoPorPin(pin)
                if (updated != null) {
                    _alumnoActual.value = updated
                }
            }
            onResult(ok)
        }
    }

    override fun onCleared() {
        super.onCleared()
        duelsListener?.remove()
    }

    // ============ CLASSROOM MANAGEMENT ============

    fun crearAula(
        teacherId: String,
        teacherName: String,
        name: String,
        grade: String,
        section: String,
        school: String,
        onResult: (Classroom?) -> Unit
    ) {
        viewModelScope.launch {
            val classroom = repository.crearSalom(teacherId, teacherName, name, grade, section, school)
            onResult(classroom)
        }
    }

    fun obtenerSalonesProfesor(teacherId: String, onResult: (List<Classroom>) -> Unit) {
        viewModelScope.launch {
            val salones = repository.obtenerSalonesProfesor(teacherId)
            onResult(salones)
        }
    }

    fun validarCodigoAula(joinCode: String, onResult: (ClassroomValidationResult) -> Unit) {
        viewModelScope.launch {
            val result = repository.validarCodigoAula(joinCode)
            onResult(result)
        }
    }

    fun vincularEstudianteAAula(
        classroomId: String,
        studentPin: String,
        parentEmail: String,
        onResult: (ClassroomLinkResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.vincularEstudianteAAula(classroomId, studentPin, parentEmail)
            onResult(result)
        }
    }

    fun cambiarEstadoAula(classroomId: String, isActive: Boolean, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.cambiarEstadoAula(classroomId, isActive)
            onResult(success)
        }
    }

    fun obtenerEstudiantesDeAula(classroomId: String, onResult: (List<FirebaseStudent>) -> Unit) {
        viewModelScope.launch {
            val estudiantes = repository.obtenerEstudiantesDeAula(classroomId)
            onResult(estudiantes)
        }
    }

    fun actualizarDificultadTema(studentPin: String, tema: String, nuevoNivel: Int, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.actualizarDificultadTema(studentPin, tema, nuevoNivel)
            if (success && _alumnoActual.value?.pin.toString() == studentPin) {
                val temaNorm = com.example.mathia.AdaptiveEngine.normalizarTema(tema)
                val mapActual = _alumnoActual.value?.dificultad_por_tema?.toMutableMap() ?: mutableMapOf()
                mapActual[temaNorm] = nuevoNivel
                _alumnoActual.value = _alumnoActual.value?.copy(dificultad_por_tema = mapActual)
            }
            onResult(success)
        }
    }

    fun validarYGuardarApodo(studentPin: String, apodo: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val (success, message) = repository.validarYGuardarApodo(studentPin, apodo)
            if (success && _alumnoActual.value?.pin.toString() == studentPin) {
                _alumnoActual.value = _alumnoActual.value?.copy(apodo = apodo.trim())
            }
            onResult(success, message)
        }
    }

    // ============ STUDENT AUTHENTICATION ============

    fun login(pin: String, onResult: (FirebaseStudent?) -> Unit) {
        if (isDemoMode) {
            val alumno = mockUsuarios[pin] as? FirebaseStudent
            _alumnoActual.value = alumno
            onResult(alumno)
            return
        }
        viewModelScope.launch {
            val alumno = repository.obtenerAlumnoPorPin(pin)
            _alumnoActual.value = alumno
            
            if (alumno != null) {
                // Trigger attendance calendar check-in and streak calculations
                val (newStreak, updatedAsistencia) = repository.registrarAsistenciaYRacha(pin)
                val updatedAlumno = alumno.copy(
                    streak = newStreak,
                    asistencia = updatedAsistencia
                )
                _alumnoActual.value = updatedAlumno
                
                // Regenerate AI recommendations on login to make sure they are fresh
                regenerarRecomendaciones(pin, updatedAlumno)
                onResult(updatedAlumno)
            } else {
                onResult(null)
            }
        }
    }

    fun guardarToken(pin: String, token: String) {
        Firebase.firestore
            .collection("usuarios")
            .document(pin)
            .update(mapOf("fcm_token" to token))
    }

    // ============ PROGRESS & GAMIFICATION UPDATES ============

    fun registrarActividadJuego(pin: String, starsEarned: Int, xpEarned: Int, tema: String, correcta: Boolean, respuestaTiempo: Long) {
        if (isDemoMode) {
            val current = mockUsuarios[pin] as? FirebaseStudent
            if (current != null) {
                val nextXP = current.xp + xpEarned
                val nextLevel = (nextXP / 100) + 1
                val nextStars = current.estrellas + starsEarned
                
                // Update skill
                val skillsMap = current.skills.toMutableMap()
                val currentSkill = skillsMap[tema] ?: 0
                val inc = if (correcta) 5 else 0
                skillsMap[tema] = (currentSkill + inc).coerceAtMost(100)
                
                // Update incorrectas
                val incorrectasMap = current.incorrectas_por_tema.toMutableMap()
                if (!correcta) {
                    incorrectasMap[tema] = (incorrectasMap[tema] ?: 0) + 1
                }
                
                // Increment questions counter
                val totalPreguntas = current.total_preguntas + 1
                val correctasCount = current.correctas + (if (correcta) 1 else 0)
                val incorrectasCount = current.incorrectas + (if (correcta) 0 else 1)
                val precision = (correctasCount * 100.0 / totalPreguntas)
                
                val updated = current.copy(
                    xp = nextXP,
                    nivel_actual = nextLevel,
                    estrellas = nextStars,
                    skills = skillsMap,
                    incorrectas_por_tema = incorrectasMap,
                    total_preguntas = totalPreguntas,
                    correctas = correctasCount,
                    incorrectas = incorrectasCount,
                    precision = precision
                )
                mockUsuarios[pin] = updated
                _alumnoActual.value = updated
                
                // Simulation of generating recommendations
                val recs = RecommendationEngine.generarRecomendaciones(updated)
                val updatedWithRecs = updated.copy(recomendaciones = recs)
                mockUsuarios[pin] = updatedWithRecs
                _alumnoActual.value = updatedWithRecs
            }
            return
        }
        viewModelScope.launch {
            val current = _alumnoActual.value ?: repository.obtenerAlumnoPorPin(pin)
            if (current != null) {
                // 1. Update stars and XP (handles level-up internally)
                repository.actualizarEstrellasYXP(pin, starsEarned, xpGained = xpEarned)
                
                // 2. Calculate updated skill score based on current level and topic
                val currentSkillScore = current.skills[tema] ?: 0
                val inc = if (correcta) 5 else 0
                val newSkillScore = (currentSkillScore + inc).coerceAtMost(100)
                
                // 3. Update skills progress and error counts
                repository.actualizarHabilidadesYErrores(pin, tema, correcta, newSkillScore)
                
                // 4. Update daily and weekly missions progress
                repository.actualizarMisiones(pin, dailyInc = 1, weeklyInc = 1)
                
                // Fetch the updated student profile to get fresh values
                val updated = repository.obtenerAlumnoPorPin(pin)
                if (updated != null) {
                    _alumnoActual.value = updated
                    // Regenerate AI recommendations with new stats
                    regenerarRecomendaciones(pin, updated)
                }
            }
        }
    }

    fun regenerarRecomendaciones(pin: String, student: FirebaseStudent) {
        viewModelScope.launch {
            val recs = RecommendationEngine.generarRecomendaciones(student)
            repository.guardarRecomendaciones(pin, recs)
            
            // Sync current state
            if (_alumnoActual.value?.pin == student.pin) {
                _alumnoActual.value = _alumnoActual.value?.copy(recomendaciones = recs)
            }
        }
    }

    fun actualizarEstrellas(pin: String, estrellas: Int) {
        viewModelScope.launch {
            repository.actualizarEstrellasYXP(pin, estrellas, xpGained = estrellas) // grant stars and XP equally
            val updated = repository.obtenerAlumnoPorPin(pin)
            if (updated != null) {
                _alumnoActual.value = updated
                regenerarRecomendaciones(pin, updated)
            }
        }
    }

    fun actualizarNivel(pin: String, nivel: Int) {
        viewModelScope.launch {
            Firebase.firestore.collection("usuarios").document(pin).update("nivel_actual", nivel).await()
            val updated = repository.obtenerAlumnoPorPin(pin)
            if (updated != null) {
                _alumnoActual.value = updated
                regenerarRecomendaciones(pin, updated)
            }
        }
    }

    fun actualizarPrecision(pin: String, precision: Double) {
        viewModelScope.launch {
            Firebase.firestore.collection("usuarios").document(pin).update("precision", precision).await()
            val updated = repository.obtenerAlumnoPorPin(pin)
            if (updated != null) {
                _alumnoActual.value = updated
                regenerarRecomendaciones(pin, updated)
            }
        }
    }

    fun guardarDiagnostico(
        pin: String,
        correctas: Int,
        incorrectas: Int,
        tiempoTotal: Long,
        tiempoPromedio: Double
    ) {
        if (isDemoMode) {
            val current = mockUsuarios[pin] as? FirebaseStudent
            if (current != null) {
                val updated = current.copy(
                    diagnostico_realizado = true,
                    correctas = correctas,
                    incorrectas = incorrectas,
                    tiempo_total = tiempoTotal,
                    tiempo_promedio = tiempoPromedio
                )
                mockUsuarios[pin] = updated
                _alumnoActual.value = updated
                regenerarRecomendaciones(pin, updated)
            }
            return
        }
        viewModelScope.launch {
            repository.guardarDiagnostico(pin, correctas, incorrectas, tiempoTotal, tiempoPromedio)
            val updated = repository.obtenerAlumnoPorPin(pin)
            if (updated != null) {
                _alumnoActual.value = updated
                regenerarRecomendaciones(pin, updated)
            }
        }
    }

    fun calculateDisplayStreak(studentStreak: Int, asistencia: List<String>): Int {
        return com.example.mathia.model.StreakCalculator.getActiveStreak(asistencia)
    }

    fun addStars(amount: Int) {
        val currentPin = _alumnoActual.value?.pin?.toString() ?: return
        viewModelScope.launch {
            repository.actualizarEstrellasYXP(currentPin, amount, 0)
            val updated = repository.obtenerAlumnoPorPin(currentPin)
            if (updated != null) {
                _alumnoActual.value = updated
            }
        }
    }

    fun actualizarApodo(nuevoApodo: String, onResult: (Boolean) -> Unit) {
        val currentPin = _alumnoActual.value?.pin?.toString() ?: run {
            onResult(false)
            return
        }
        validarYGuardarApodo(currentPin, nuevoApodo) { success, _ ->
            onResult(success)
        }
    }

    fun obtenerCompetenciaSemanal(onResult: (com.example.mathia.model.WeeklyCompetition) -> Unit) {
        viewModelScope.launch {
            val comp = repository.obtenerCompetenciaSemanalActual()
            onResult(comp)
        }
    }

    // ============ REPOSITORY WRAPPERS ============

    fun obtenerTodosAlumnos(onResult: (List<FirebaseStudent>) -> Unit) {
        if (isDemoMode) {
            val alumnos = mockUsuarios.values.filterIsInstance<FirebaseStudent>()
            onResult(alumnos)
            return
        }
        viewModelScope.launch {
            val alumnos = repository.obtenerTodosAlumnos()
            onResult(alumnos)
        }
    }

    fun crearAlumno(alumno: Map<String, Any>, pin: String, onResult: (Boolean) -> Unit) {
        if (isDemoMode) {
            val pinInt = pin.toIntOrNull() ?: 1234
            val nombre = alumno["nombre"] as? String ?: ""
            val grado = alumno["grado"] as? String ?: ""
            val seccion = alumno["seccion"] as? String ?: ""
            val edad = alumno["edad"] as? Int ?: 6
            val colegio = alumno["colegio"] as? String ?: ""
            val avatar = alumno["avatar"] as? String ?: "default"
            val email = alumno["padre_email"] as? String ?: ""

            val nuevo = FirebaseStudent(
                nombre = nombre,
                grado = grado,
                seccion = seccion,
                edad = edad,
                colegio = colegio,
                avatar = avatar,
                pin = pinInt,
                padre_email = email,
                nivel_actual = 1,
                fecha_creacion = System.currentTimeMillis()
            )
            mockUsuarios[pin] = nuevo
            onResult(true)
            return
        }
        viewModelScope.launch {
            val success = repository.crearAlumno(alumno, pin)
            onResult(success)
        }
    }

    fun obtenerEstudiantesPorEmailPadre(email: String, onResult: (List<FirebaseStudent>) -> Unit) {
        if (isDemoMode) {
            val list = mockUsuarios.values.filterIsInstance<FirebaseStudent>().filter { it.padre_email == email }
            onResult(list)
            return
        }
        viewModelScope.launch {
            val alumnos = repository.obtenerEstudiantesPorEmailPadre(email)
            onResult(alumnos)
        }
    }

    fun actualizarPadreEmail(pin: String, padreEmail: String, onResult: (Boolean) -> Unit) {
        if (isDemoMode) {
            val current = mockUsuarios[pin] as? FirebaseStudent
            if (current != null) {
                mockUsuarios[pin] = current.copy(padre_email = padreEmail)
            }
            onResult(true)
            return
        }
        viewModelScope.launch {
            Firebase.firestore.collection("usuarios").document(pin).update("padre_email", padreEmail).await()
            onResult(true)
        }
    }

    fun actualizarCosmeticos(pin: String, avatar: String, theme: String, onResult: (Boolean) -> Unit) {
        if (isDemoMode) {
            val current = mockUsuarios[pin] as? FirebaseStudent
            if (current != null) {
                val updated = current.copy(avatar = avatar, equipped_theme = theme)
                mockUsuarios[pin] = updated
                if (_alumnoActual.value?.pin?.toString() == pin) {
                    _alumnoActual.value = updated
                }
            }
            onResult(true)
            return
        }
        viewModelScope.launch {
            val success = repository.actualizarCosmeticos(pin, avatar, theme)
            if (success) {
                val current = _alumnoActual.value
                if (current != null && current.pin.toString() == pin) {
                    _alumnoActual.value = current.copy(avatar = avatar, equipped_theme = theme)
                }
            }
            onResult(success)
        }
    }

    fun comprarCosmetico(pin: String, nuevoCosmetico: String, esAvatar: Boolean, costo: Int, onResult: (Boolean) -> Unit) {
        if (isDemoMode) {
            val current = mockUsuarios[pin] as? FirebaseStudent
            if (current != null) {
                val nextStars = (current.estrellas - costo).coerceAtLeast(0)
                val updated = if (esAvatar) {
                    current.copy(
                        estrellas = nextStars,
                        unlocked_avatars = current.unlocked_avatars + nuevoCosmetico
                    )
                } else {
                    current.copy(
                        estrellas = nextStars,
                        unlocked_themes = current.unlocked_themes + nuevoCosmetico
                    )
                }
                mockUsuarios[pin] = updated
                if (_alumnoActual.value?.pin?.toString() == pin) {
                    _alumnoActual.value = updated
                }
            }
            onResult(true)
            return
        }
        viewModelScope.launch {
            val success = repository.comprarCosmetico(pin, nuevoCosmetico, esAvatar, costo)
            if (success) {
                val updated = repository.obtenerAlumnoPorPin(pin)
                if (updated != null) {
                    _alumnoActual.value = updated
                }
            }
            onResult(success)
        }
    }

    // ============ GOOGLE AUTHENTICATION & PROFILE ============

    fun loginConGoogle(idToken: String, email: String, rol: String, onResult: (Boolean, Boolean) -> Unit) {
        if (isDemoMode) {
            val found = mockUsuarios.values.filterIsInstance<AdultoFirebase>().firstOrNull { it.email == email }
            if (found != null) {
                onResult(true, found.perfil_completo)
            } else {
                val uid = "google_${email.hashCode()}"
                val nuevoAdulto = AdultoFirebase(
                    uid = uid,
                    email = email,
                    rol = if (rol == "docente") "docente" else "padre",
                    nombre = email.substringBefore("@"),
                    perfil_completo = false
                )
                mockUsuarios[uid] = nuevoAdulto
                onResult(true, false)
            }
            return
        }
        viewModelScope.launch {
            val (success, isComplete) = repository.loginConGoogle(idToken, email, rol)
            onResult(success, isComplete)
        }
    }

    fun actualizarPerfilDocente(
        uid: String,
        nombre: String,
        colegio: String,
        grado: String,
        seccion: String,
        onResult: (Boolean) -> Unit
    ) {
        if (isDemoMode) {
            val current = mockUsuarios[uid] as? AdultoFirebase
            if (current != null) {
                mockUsuarios[uid] = current.copy(
                    nombre = nombre,
                    colegio = colegio,
                    grado = grado,
                    seccion = seccion,
                    perfil_completo = true
                )
            } else {
                mockUsuarios[uid] = AdultoFirebase(
                    uid = uid,
                    nombre = nombre,
                    colegio = colegio,
                    grado = grado,
                    seccion = seccion,
                    rol = "docente",
                    perfil_completo = true
                )
            }
            onResult(true)
            return
        }
        viewModelScope.launch {
            val success = repository.actualizarPerfilDocente(uid, nombre, colegio, grado, seccion)
            onResult(success)
        }
    }

    // ============ NOTIFICATIONS ============

    fun enviarNotificacionExamen(pin: String, correctas: Int, precision: Int) {
        if (isDemoMode) {
            mockNotificaciones.add(
                NotificacionFirebase(
                    id = "not_${System.currentTimeMillis()}",
                    titulo = "Examen completado",
                    cuerpo = "¡Puntaje: $correctas respuestas correctas | Precisión: $precision%!",
                    timestamp = System.currentTimeMillis(),
                    read = false
                )
            )
            return
        }
        Firebase.firestore
            .collection("notifications")
            .add(
                hashMapOf(
                    "studentPin" to pin,
                    "title" to "Examen completado",
                    "body" to "¡Puntaje: $correctas respuestas correctas | Precisión: $precision%!",
                    "timestamp" to System.currentTimeMillis(),
                    "read" to false,
                    "tipo" to "alumno"
                )
            )
    }

    fun notificarPadre(pin: String, nombreAlumno: String, precision: Int) {
        if (isDemoMode) {
            mockNotificaciones.add(
                NotificacionFirebase(
                    id = "not_${System.currentTimeMillis()}",
                    titulo = "Nuevo progreso disponible",
                    cuerpo = "$nombreAlumno completó un examen con un $precision% de precisión.",
                    timestamp = System.currentTimeMillis(),
                    read = false
                )
            )
            return
        }
        Firebase.firestore
            .collection("notifications")
            .add(
                hashMapOf(
                    "studentPin" to pin,
                    "title" to "Nuevo progreso disponible",
                    "body" to "$nombreAlumno completó un examen con un $precision% de precisión.",
                    "timestamp" to System.currentTimeMillis(),
                    "read" to false,
                    "tipo" to "padre"
                )
            )
    }

    fun notificarDocente(pin: String, nombreAlumno: String, precision: Int) {
        if (isDemoMode) {
            mockNotificaciones.add(
                NotificacionFirebase(
                    id = "not_${System.currentTimeMillis()}",
                    titulo = "Evaluación completada en clase",
                    cuerpo = "$nombreAlumno terminó su examen con $precision%.",
                    timestamp = System.currentTimeMillis(),
                    read = false
                )
            )
            return
        }
        Firebase.firestore
            .collection("notifications")
            .add(
                hashMapOf(
                    "studentPin" to pin,
                    "title" to "Evaluación completada en clase",
                    "body" to "$nombreAlumno terminó su examen con $precision%.",
                    "timestamp" to System.currentTimeMillis(),
                    "read" to false,
                    "tipo" to "docente"
                )
            )
    }

    fun crearRecordatorioExamen(pin: String) {
        if (isDemoMode) {
            mockNotificaciones.add(
                NotificacionFirebase(
                    id = "not_${System.currentTimeMillis()}",
                    titulo = "Examen pendiente",
                    cuerpo = "Aún no has completado tu examen adaptativo. ¡Inténtalo hoy!",
                    timestamp = System.currentTimeMillis(),
                    read = false
                )
            )
            return
        }
        Firebase.firestore
            .collection("notifications")
            .add(
                hashMapOf(
                    "studentPin" to pin,
                    "title" to "Examen pendiente",
                    "body" to "Aún no has completado tu examen adaptativo. ¡Inténtalo hoy!",
                    "timestamp" to System.currentTimeMillis(),
                    "read" to false,
                    "tipo" to "recordatorio"
                )
            )
    }

    // ============ PLATFORM IMPROVEMENT VIEWMODEL METHODS ============

    fun registrarRespuestaHistorial(res: RespuestaHistorial, onResult: (Boolean) -> Unit = {}) {
        if (isDemoMode) {
            val nuevoLog = res.copy(id = "log_${System.currentTimeMillis()}")
            mockHistorial.add(nuevoLog)
            if (res.estudianteId == _alumnoActual.value?.pin?.toString()) {
                obtenerHistorialRespuestas(res.estudianteId)
            }
            onResult(true)
            return
        }
        viewModelScope.launch {
            val success = repository.registrarRespuestaHistorial(res)
            if (success) {
                if (res.estudianteId == _alumnoActual.value?.pin?.toString()) {
                    obtenerHistorialRespuestas(res.estudianteId)
                }
            }
            onResult(success)
        }
    }

    fun obtenerHistorialRespuestas(pin: String) {
        if (isDemoMode) {
            val lista = mockHistorial.filter { it.estudianteId == pin }
            _historialRespuestas.value = lista
            return
        }
        viewModelScope.launch {
            val lista = repository.obtenerHistorialRespuestas(pin)
            _historialRespuestas.value = lista
        }
    }

    fun guardarReporteSesion(reporte: ReporteSesion, onResult: (Boolean) -> Unit = {}) {
        if (isDemoMode) {
            val nuevoReporte = reporte.copy(id = "rep_${System.currentTimeMillis()}")
            mockReportes.add(nuevoReporte)
            if (reporte.docenteId.isNotEmpty()) {
                obtenerReportesSesionPorDocente(reporte.docenteId)
            }
            if (reporte.padreEmail.isNotEmpty()) {
                obtenerReportesSesionPorPadre(reporte.padreEmail)
            }
            onResult(true)
            return
        }
        viewModelScope.launch {
            val success = repository.guardarReporteSesion(reporte)
            if (success) {
                if (reporte.docenteId.isNotEmpty()) {
                    obtenerReportesSesionPorDocente(reporte.docenteId)
                }
                if (reporte.padreEmail.isNotEmpty()) {
                    obtenerReportesSesionPorPadre(reporte.padreEmail)
                }
            }
            onResult(success)
        }
    }

    fun obtenerReportesSesionPorDocente(docenteId: String) {
        if (isDemoMode) {
            val teacher = mockUsuarios[docenteId] as? AdultoFirebase
            val reportsList = if (teacher != null) {
                mockReportes.filter { it.grado == teacher.grado && it.seccion == teacher.seccion }
            } else {
                mockReportes
            }
            _reportesSesiones.value = reportsList
            return
        }
        viewModelScope.launch {
            val lista = repository.obtenerReportesSesionPorDocente(docenteId)
            _reportesSesiones.value = lista
        }
    }

    fun obtenerReportesSesionPorPadre(padreEmail: String) {
        if (isDemoMode) {
            val lista = mockReportes.filter { it.padreEmail == padreEmail }
            _reportesSesiones.value = lista
            return
        }
        viewModelScope.launch {
            val lista = repository.obtenerReportesSesionPorPadre(padreEmail)
            _reportesSesiones.value = lista
        }
    }

    fun guardarObservacion(pin: String, obs: Observacion, onResult: (Boolean) -> Unit = {}) {
        if (isDemoMode) {
            val nuevaObs = obs.copy(id = "obs_${System.currentTimeMillis()}")
            mockObservaciones.add(nuevaObs)
            obtenerObservaciones(pin)
            onResult(true)
            return
        }
        viewModelScope.launch {
            val success = repository.guardarObservacion(pin, obs)
            if (success) {
                obtenerObservaciones(pin)
            }
            onResult(success)
        }
    }

    fun obtenerObservaciones(pin: String) {
        if (isDemoMode) {
            val lista = mockObservaciones
            _observaciones.value = lista
            return
        }
        viewModelScope.launch {
            val lista = repository.obtenerObservaciones(pin)
            _observaciones.value = lista
        }
    }

    fun guardarNotificacion(uid: String, not: NotificacionFirebase, onResult: (Boolean) -> Unit = {}) {
        if (isDemoMode) {
            val nuevaNot = not.copy(id = "not_${System.currentTimeMillis()}")
            mockNotificaciones.add(nuevaNot)
            obtenerNotificaciones(uid)
            onResult(true)
            return
        }
        viewModelScope.launch {
            val success = repository.guardarNotificacion(uid, not)
            if (success) {
                obtenerNotificaciones(uid)
            }
            onResult(success)
        }
    }

    fun obtenerNotificaciones(uid: String) {
        if (isDemoMode) {
            _notificaciones.value = mockNotificaciones
            return
        }
        viewModelScope.launch {
            val lista = repository.obtenerNotificaciones(uid)
            _notificaciones.value = lista
        }
    }

    fun obtenerAdultoPorUid(uid: String, onResult: (AdultoFirebase?) -> Unit) {
        if (isDemoMode) {
            val adulto = mockUsuarios[uid] as? AdultoFirebase
            onResult(adulto)
            return
        }
        viewModelScope.launch {
            val adulto = repository.obtenerAdultoPorUid(uid)
            onResult(adulto)
        }
    }

    fun actualizarPerfilPadre(uid: String, nombre: String, pinHijo: Int, onResult: (Boolean) -> Unit) {
        if (isDemoMode) {
            val current = mockUsuarios[uid] as? AdultoFirebase
            if (current != null) {
                mockUsuarios[uid] = current.copy(
                    nombre = nombre,
                    estudiante_pin = pinHijo,
                    perfil_completo = true
                )
            } else {
                mockUsuarios[uid] = AdultoFirebase(
                    uid = uid,
                    nombre = nombre,
                    estudiante_pin = pinHijo,
                    rol = "padre",
                    perfil_completo = true
                )
            }
            onResult(true)
            return
        }
        viewModelScope.launch {
            val success = repository.actualizarPerfilPadre(uid, nombre, pinHijo)
            onResult(success)
        }
    }

    // --- MATHIA AI API INTEGRATION ---
    private val mathIaApiService by lazy { MathIaApiService.create() }

    fun shouldRequestHint(intentos: Int, incorrectasConsecutivas: Int): Boolean {
        return incorrectasConsecutivas >= 2 || (intentos >= 3 && incorrectasConsecutivas >= 1)
    }

    fun obtenerPistaAdaptativa(request: HintRequest, onResult: (HintResponse) -> Unit) {
        viewModelScope.launch {
            try {
                val response = mathIaApiService.getAdaptiveHint(request)
                onResult(response)
            } catch (e: Exception) {
                e.printStackTrace()
                val fallbackResponse = LocalFallbackEngine.generateHint(request)
                onResult(fallbackResponse)
            }
        }
    }

    fun obtenerResumenProgreso(request: ProgressSummaryRequest, onResult: (ProgressSummaryResponse?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = mathIaApiService.getProgressSummary(request)
                onResult(response)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }
}