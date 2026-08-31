package com.example.mathia

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.FieldValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import com.example.mathia.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.firestore.FirebaseFirestore

import com.example.mathia.util.SecurityUtils

@Singleton
class FirebaseRepository @Inject constructor(
    private val db: FirebaseFirestore
) {

    init {
        // Objective 7: Enable local caching in Firestore
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            db.firestoreSettings = settings
            println("Firestore local cache enabled successfully.")
        } catch (e: Exception) {
            println("️ Error setting Firestore settings: ${e.message}")
        }
    }

    // ============ SECURE STUDENT CREDENTIAL OPERATIONS ============

    suspend fun crearCredencialEstudiante(studentId: String, pin: String): Boolean {
        return try {
            val salt = SecurityUtils.generateSalt()
            val hash = SecurityUtils.hashPin(pin, salt)
            val credential = StudentCredential(
                studentId = studentId,
                credentialType = "PIN",
                credentialHash = hash,
                salt = salt,
                failedAttempts = 0,
                lockedUntil = 0L,
                updatedAt = System.currentTimeMillis()
            )
            db.collection("student_credentials")
                .document(studentId)
                .set(credential)
                .await()
            true
        } catch (e: Exception) {
            println("Error creando credencial estudiante: ${e.message}")
            false
        }
    }

    suspend fun autenticarEstudianteSeguro(studentId: String, inputPin: String): StudentAuthResult {
        return try {
            val credDoc = db.collection("student_credentials")
                .document(studentId)
                .get()
                .await()

            if (!credDoc.exists()) {
                val legacyStudent = obtenerAlumnoPorPin(inputPin)
                return if (legacyStudent != null) {
                    val domainStudent = com.example.mathia.data.mapper.ModelMappers.run { legacyStudent.toDomain(studentId) }
                    StudentAuthResult.Success(domainStudent)
                } else {
                    StudentAuthResult.UserNotFound
                }
            }

            val credential = credDoc.toObject(StudentCredential::class.java) ?: return StudentAuthResult.Error("Error leyendo credencial")
            val currentTime = System.currentTimeMillis()

            if (credential.lockedUntil > currentTime) {
                return StudentAuthResult.AccountLocked(credential.lockedUntil)
            }

            val isValid = SecurityUtils.verifyPin(inputPin, credential.salt, credential.credentialHash)
            if (isValid) {
                db.collection("student_credentials")
                    .document(studentId)
                    .update("failedAttempts", 0, "lockedUntil", 0L)
                    .await()

                val studentDoc = db.collection("usuarios").document(studentId).get().await()
                val firebaseStudent = studentDoc.toObject(FirebaseStudent::class.java)
                    ?: return StudentAuthResult.UserNotFound
                val domainStudent = com.example.mathia.data.mapper.ModelMappers.run { firebaseStudent.toDomain(studentId) }
                StudentAuthResult.Success(domainStudent)
            } else {
                val newFailed = credential.failedAttempts + 1
                val isNowLocked = newFailed >= SecurityUtils.MAX_FAILED_ATTEMPTS
                val lockTime = if (isNowLocked) currentTime + SecurityUtils.LOCK_DURATION_MS else 0L

                db.collection("student_credentials")
                    .document(studentId)
                    .update("failedAttempts", newFailed, "lockedUntil", lockTime)
                    .await()

                if (isNowLocked) {
                    StudentAuthResult.AccountLocked(lockTime)
                } else {
                    StudentAuthResult.InvalidPin(SecurityUtils.MAX_FAILED_ATTEMPTS - newFailed)
                }
            }
        } catch (e: Exception) {
            println("Error en autenticación segura de estudiante: ${e.message}")
            StudentAuthResult.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun resetearPinEstudiante(studentId: String, nuevoPin: String): Boolean {
        return crearCredencialEstudiante(studentId, nuevoPin)
    }

    // ============ CLASSROOM & JOIN REQUEST OPERATIONS ============

    suspend fun crearSalom(
        teacherId: String,
        teacherName: String,
        name: String,
        grade: String,
        section: String,
        school: String
    ): Classroom? {
        return try {
            val classroomId = db.collection("classrooms").document().id
            val joinCode = com.example.mathia.util.ClassroomCodeGenerator.generateCode()
            val classroom = Classroom(
                classroomId = classroomId,
                joinCode = joinCode,
                name = name,
                grade = grade,
                section = section,
                teacherId = teacherId,
                teacherName = teacherName,
                school = school,
                createdAt = System.currentTimeMillis(),
                isActive = true
            )
            db.collection("classrooms").document(classroomId).set(classroom).await()
            classroom
        } catch (e: Exception) {
            println("Error creando salón: ${e.message}")
            null
        }
    }

    suspend fun obtenerSalonesProfesor(teacherId: String): List<Classroom> {
        return try {
            val res = db.collection("classrooms")
                .whereEqualTo("teacherId", teacherId)
                .get()
                .await()
            res.documents.mapNotNull { it.toObject(Classroom::class.java) }
        } catch (e: Exception) {
            println("Error obteniendo salones del profesor: ${e.message}")
            emptyList()
        }
    }

    suspend fun validarCodigoAula(joinCode: String): ClassroomValidationResult {
        return try {
            val cleanCode = joinCode.trim().uppercase()
            if (!com.example.mathia.util.ClassroomCodeGenerator.isValidCodeFormat(cleanCode)) {
                return ClassroomValidationResult.InvalidCode
            }

            val query = db.collection("classrooms")
                .whereEqualTo("joinCode", cleanCode)
                .get()
                .await()

            if (query.documents.isEmpty()) {
                return ClassroomValidationResult.InvalidCode
            }

            val classroom = query.documents.first().toObject(Classroom::class.java)
                ?: return ClassroomValidationResult.InvalidCode

            if (!classroom.isActive) {
                return ClassroomValidationResult.ClassroomClosed
            }

            ClassroomValidationResult.Success(classroom)
        } catch (e: Exception) {
            println("Error validando código de aula: ${e.message}")
            ClassroomValidationResult.Error(e.message ?: "Error al consultar el aula")
        }
    }

    suspend fun vincularEstudianteAAula(
        classroomId: String,
        studentPin: String,
        parentEmail: String
    ): ClassroomLinkResult {
        return try {
            val studentDoc = db.collection("usuarios").document(studentPin).get().await()
            if (!studentDoc.exists()) {
                return ClassroomLinkResult.Error("Estudiante no encontrado")
            }

            val student = studentDoc.toObject(FirebaseStudent::class.java)
                ?: return ClassroomLinkResult.Error("Error leyendo estudiante")

            // Verify parent authorization
            if (student.padre_email.isNotEmpty() && !student.padre_email.equals(parentEmail, ignoreCase = true)) {
                return ClassroomLinkResult.Unauthorized
            }

            val classroomDoc = db.collection("classrooms").document(classroomId).get().await()
            val classroom = classroomDoc.toObject(Classroom::class.java)
                ?: return ClassroomLinkResult.Error("Aula no encontrada")

            if (!classroom.isActive) {
                return ClassroomLinkResult.Error("Esta aula ya no acepta nuevos estudiantes")
            }

            // Check duplicate linkage
            if (student.aulas.contains(classroomId)) {
                return ClassroomLinkResult.AlreadyLinked
            }

            val memberDoc = db.collection("classrooms")
                .document(classroomId)
                .collection("estudiantes")
                .document(studentPin)
                .get()
                .await()

            if (memberDoc.exists()) {
                return ClassroomLinkResult.AlreadyLinked
            }

            // 1. Write student record under classroom subcollection
            val memberData = hashMapOf(
                "studentId" to studentPin,
                "studentName" to student.nombre,
                "parentEmail" to parentEmail,
                "joinedAt" to System.currentTimeMillis()
            )
            db.collection("classrooms")
                .document(classroomId)
                .collection("estudiantes")
                .document(studentPin)
                .set(memberData)
                .await()

            // 2. Update student document with arrayUnion of classroomId and teacher details
            db.collection("usuarios")
                .document(studentPin)
                .update(
                    mapOf(
                        "aulas" to FieldValue.arrayUnion(classroomId),
                        "docente_asignado" to classroom.teacherName,
                        "grado" to classroom.grade,
                        "seccion" to classroom.section,
                        "colegio" to classroom.school
                    )
                )
                .await()

            ClassroomLinkResult.Success(classroom.name, student.nombre)
        } catch (e: Exception) {
            println("Error vinculando estudiante a aula: ${e.message}")
            ClassroomLinkResult.Error(e.message ?: "Error en la vinculación")
        }
    }

    suspend fun cambiarEstadoAula(classroomId: String, isActive: Boolean): Boolean {
        return try {
            db.collection("classrooms")
                .document(classroomId)
                .update("isActive", isActive)
                .await()
            true
        } catch (e: Exception) {
            println("Error cambiando estado de aula: ${e.message}")
            false
        }
    }

    suspend fun obtenerEstudiantesDeAula(classroomId: String): List<FirebaseStudent> {
        return try {
            val members = db.collection("classrooms")
                .document(classroomId)
                .collection("estudiantes")
                .get()
                .await()

            val studentIds = members.documents.mapNotNull { it.getString("studentId") }
            if (studentIds.isEmpty()) return emptyList()

            val students = mutableListOf<FirebaseStudent>()
            for (id in studentIds) {
                val sDoc = db.collection("usuarios").document(id).get().await()
                sDoc.toObject(FirebaseStudent::class.java)?.let { students.add(it) }
            }
            students
        } catch (e: Exception) {
            println("Error obteniendo estudiantes de aula: ${e.message}")
            emptyList()
        }
    }

    suspend fun solicitarUnirseASalom(
        joinCode: String,
        studentId: String,
        studentName: String,
        parentId: String,
        parentEmail: String
    ): JoinRequestResult {
        return try {
            val cleanCode = joinCode.trim().uppercase()
            val query = db.collection("classrooms")
                .whereEqualTo("joinCode", cleanCode)
                .whereEqualTo("active", true)
                .get()
                .await()

            if (query.documents.isEmpty()) {
                return JoinRequestResult.InvalidCode
            }

            val classroom = query.documents.first().toObject(Classroom::class.java)
                ?: return JoinRequestResult.InvalidCode

            // Check if already requested or member
            val existing = db.collection("join_requests")
                .whereEqualTo("classroomId", classroom.classroomId)
                .whereEqualTo("studentId", studentId)
                .get()
                .await()

            if (!existing.documents.isEmpty()) {
                return JoinRequestResult.AlreadyMemberOrPending
            }

            val requestId = db.collection("join_requests").document().id
            val request = JoinRequest(
                requestId = requestId,
                classroomId = classroom.classroomId,
                classroomName = classroom.name,
                joinCode = cleanCode,
                studentId = studentId,
                studentName = studentName,
                parentId = parentId,
                parentEmail = parentEmail,
                teacherId = classroom.teacherId,
                status = "PENDING",
                requestedAt = System.currentTimeMillis()
            )

            db.collection("join_requests").document(requestId).set(request).await()
            JoinRequestResult.Success(request)
        } catch (e: Exception) {
            println("Error al solicitar unirse a salón: ${e.message}")
            JoinRequestResult.Error(e.message ?: "El código no es válido o ya no está disponible.")
        }
    }

    suspend fun obtenerSolicitudesPendientesProfesor(teacherId: String): List<JoinRequest> {
        return try {
            val res = db.collection("join_requests")
                .whereEqualTo("teacherId", teacherId)
                .whereEqualTo("status", "PENDING")
                .get()
                .await()
            res.documents.mapNotNull { it.toObject(JoinRequest::class.java) }
        } catch (e: Exception) {
            println("Error obteniendo solicitudes pendientes: ${e.message}")
            emptyList()
        }
    }

    suspend fun responderSolicitudUnion(requestId: String, nuevoEstado: String): Boolean {
        return try {
            val docRef = db.collection("join_requests").document(requestId)
            val doc = docRef.get().await()
            val req = doc.toObject(JoinRequest::class.java) ?: return false

            docRef.update(
                "status", nuevoEstado,
                "respondedAt", System.currentTimeMillis()
            ).await()

            if (nuevoEstado == "APPROVED") {
                // Add student to classroom members
                db.collection("classrooms")
                    .document(req.classroomId)
                    .collection("members")
                    .document(req.studentId)
                    .set(mapOf("studentId" to req.studentId, "studentName" to req.studentName, "joinedAt" to System.currentTimeMillis()))
                    .await()
            }
            true
        } catch (e: Exception) {
            println("Error al responder solicitud de unión: ${e.message}")
            false
        }
    }

    // ============ STUDENT OPERATIONS ============

    suspend fun actualizarDificultadTema(studentPin: String, tema: String, nuevoNivel: Int): Boolean {
        return try {
            val temaNorm = com.example.mathia.AdaptiveEngine.normalizarTema(tema)
            val docRef = db.collection("usuarios").document(studentPin)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (snapshot.exists()) {
                    val mapActual = (snapshot.get("dificultad_por_tema") as? Map<String, Any>)?.mapValues {
                        (it.value as? Long)?.toInt() ?: (it.value as? Int) ?: 1
                    }?.toMutableMap() ?: mutableMapOf()
                    
                    mapActual[temaNorm] = nuevoNivel.coerceIn(1, 5)
                    transaction.update(docRef, "dificultad_por_tema", mapActual)
                }
            }.await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun validarYGuardarApodo(studentPin: String, apodo: String): Pair<Boolean, String> {
        val cleanApodo = apodo.trim()
        if (cleanApodo.length < 3 || cleanApodo.length > 15) {
            return Pair(false, "El apodo debe tener entre 3 y 15 caracteres.")
        }
        val regex = Regex("^[a-zA-Z0-9_ -]+$")
        if (!regex.matches(cleanApodo)) {
            return Pair(false, "El apodo solo puede contener letras, números y guiones.")
        }

        return try {
            val query = db.collection("usuarios")
                .get()
                .await()

            val occupied = query.documents.any { doc ->
                val existingApodo = doc.getString("apodo") ?: ""
                val existingPin = doc.get("pin")?.toString() ?: doc.id
                existingApodo.equals(cleanApodo, ignoreCase = true) && existingPin != studentPin
            }

            if (occupied) {
                return Pair(false, "Este apodo ya está ocupado. Prueba con otro.")
            }

            val targetDoc = query.documents.firstOrNull { doc ->
                doc.get("pin")?.toString() == studentPin || doc.id == studentPin
            }

            if (targetDoc != null) {
                targetDoc.reference.update("apodo", cleanApodo).await()
            } else {
                db.collection("usuarios").document(studentPin).update("apodo", cleanApodo).await()
            }

            Pair(true, "¡Perfecto! Este apodo es tuyo.")
        } catch (e: Exception) {
            println("Error al guardar apodo: ${e.message}")
            Pair(false, "Error de conexión al guardar el apodo.")
        }
    }

    suspend fun obtenerAlumnoPorPin(pin: String): FirebaseStudent? {
        return try {
            val resultado = db.collection("usuarios")
                .whereEqualTo("pin", pin.toIntOrNull() ?: return null)
                .get()
                .await()

            if (resultado.documents.isEmpty()) return null
            resultado.documents.first().toObject(FirebaseStudent::class.java)
        } catch (e: Exception) {
            println("Error obteniendo alumno por PIN: ${e.message}")
            null
        }
    }

    suspend fun crearAlumno(alumno: Map<String, Any>, pin: String): Boolean {
        return try {
            db.collection("usuarios")
                .document(pin)
                .set(alumno)
                .await()
            println("Alumno creado con PIN: $pin")
            true
        } catch (e: Exception) {
            println("Error creando alumno: ${e.message}")
            false
        }
    }

    suspend fun obtenerTodosAlumnos(): List<FirebaseStudent> {
        return try {
            val resultado = db.collection("usuarios")
                .whereNotEqualTo("pin", null) // Fetch only students, who have pin fields
                .get()
                .await()
            resultado.documents.mapNotNull { it.toObject(FirebaseStudent::class.java) }
        } catch (e: Exception) {
            println("Error obteniendo todos los alumnos: ${e.message}")
            emptyList()
        }
    }

    suspend fun obtenerEstudiantesPorEmailPadre(email: String): List<FirebaseStudent> {
        return try {
            val resultado = db.collection("usuarios")
                .whereEqualTo("padre_email", email)
                .get()
                .await()
            resultado.documents.mapNotNull { it.toObject(FirebaseStudent::class.java) }
        } catch (e: Exception) {
            println("Error obteniendo estudiantes por email padre: ${e.message}")
            emptyList()
        }
    }

    suspend fun registrarAsistenciaYRacha(pin: String): Pair<Int, List<String>> {
        try {
            val resultado = db.collection("usuarios")
                .whereEqualTo("pin", pin.toIntOrNull() ?: return Pair(0, emptyList()))
                .get()
                .await()

            if (resultado.documents.isNotEmpty()) {
                val doc = resultado.documents.first()
                val currentStreak = doc.getLong("streak")?.toInt() ?: 0
                @Suppress("UNCHECKED_CAST")
                val currentAsistencia = doc.get("asistencia") as? List<String> ?: emptyList()

                val today = java.time.LocalDate.now(java.time.ZoneId.of("America/Lima"))
                val todayStr = today.toString()

                if (currentAsistencia.contains(todayStr)) {
                    // Idempotent: Already logged practice today in America/Lima, streak unchanged
                    return Pair(currentStreak, currentAsistencia)
                }

                val updatedAsistencia = currentAsistencia.toMutableList().apply { add(todayStr) }
                var newStreak = currentStreak

                if (currentAsistencia.isNotEmpty()) {
                    val lastDateStr = currentAsistencia.last()
                    try {
                        val lastDate = java.time.LocalDate.parse(lastDateStr)
                        val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(lastDate, today)
                        if (daysBetween == 1L) {
                            // Studied yesterday -> increment streak
                            newStreak++
                        } else if (daysBetween > 1L) {
                            // Missed days -> reset streak to 1
                            newStreak = 1
                        }
                    } catch (e: Exception) {
                        newStreak = 1
                    }
                } else {
                    newStreak = 1
                }

                doc.reference.update(
                    mapOf(
                        "asistencia" to updatedAsistencia,
                        "streak" to newStreak
                    )
                ).await()

                return Pair(newStreak, updatedAsistencia)
            }
        } catch (e: Exception) {
            println("Error registrando asistencia y racha: ${e.message}")
        }
        return Pair(0, emptyList())
    }

    suspend fun actualizarEstrellasYXP(pin: String, estrellasGained: Int, xpGained: Int) {
        try {
            val resultado = db.collection("usuarios")
                .whereEqualTo("pin", pin.toIntOrNull() ?: return)
                .get()
                .await()

            if (resultado.documents.isNotEmpty()) {
                val doc = resultado.documents.first()
                val currentEstrellas = doc.getLong("estrellas")?.toInt() ?: 0
                val currentXP = doc.getLong("xp")?.toInt() ?: 0
                val currentLevel = doc.getLong("nivel_actual")?.toInt() ?: 1

                val newEstrellas = calculateAddedStars(currentEstrellas, estrellasGained)
                val newXP = currentXP + xpGained
                val newLevel = (newXP / 100) + 1 // 100 XP per level

                val updateMap = mutableMapOf<String, Any>(
                    "estrellas" to newEstrellas,
                    "xp" to newXP,
                    "nivel_actual" to newLevel
                )

                doc.reference.update(updateMap).await()
                println("Estrellas y XP actualizadas: estrellas=$newEstrellas, xp=$newXP, nivel=$newLevel")
            }
        } catch (e: Exception) {
            println("Error actualizando estrellas y XP: ${e.message}")
        }
    }

    suspend fun verificarDisponibilidadApodo(apodo: String): Boolean {
        val cleanApodo = apodo.trim()
        if (cleanApodo.length < 3 || cleanApodo.length > 15) return false
        if (cleanApodo.contains("@") || cleanApodo.contains(".")) return false
        return try {
            val res = db.collection("usuarios")
                .whereEqualTo("apodo", cleanApodo)
                .get()
                .await()
            res.documents.isEmpty()
        } catch (e: Exception) {
            true
        }
    }

    suspend fun actualizarApodoEstudiante(pin: String, nuevoApodo: String): Boolean {
        val cleanApodo = nuevoApodo.trim()
        if (!verificarDisponibilidadApodo(cleanApodo)) return false
        return try {
            val query = db.collection("usuarios")
                .whereEqualTo("pin", pin.toIntOrNull() ?: return false)
                .get()
                .await()

            if (query.documents.isNotEmpty()) {
                query.documents.first().reference
                    .update("apodo", cleanApodo)
                    .await()
                true
            } else false
        } catch (e: Exception) {
            println("Error al actualizar apodo: ${e.message}")
            false
        }
    }

    suspend fun obtenerCompetenciaSemanalActual(): WeeklyCompetition {
        val limaZone = java.time.ZoneId.of("America/Lima")
        val today = java.time.LocalDate.now(limaZone)
        val monday = today.with(java.time.DayOfWeek.MONDAY)
        val sunday = today.with(java.time.DayOfWeek.SUNDAY)
        val weekNumber = today.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        val compId = "competencia_${today.year}_$weekNumber"

        val topics = listOf("divisiones", "fracciones", "multiplicacion", "series", "sumas", "restas", "problemas")
        val selectedTopic = topics[(weekNumber % topics.size)]
        val formattedTitle = "Desafío de ${selectedTopic.replaceFirstChar { it.uppercase() }}"

        return WeeklyCompetition(
            id = compId,
            title = formattedTitle,
            tema = selectedTopic,
            fechaInicio = monday.toString(),
            fechaFin = sunday.toString(),
            targetExercises = 20,
            rewardStars = 150,
            activa = true
        )
    }

    suspend fun obtenerRankingCompetencia(compId: String): List<CompetitionParticipant> {
        return try {
            val res = db.collection("competencias_semanales")
                .document(compId)
                .collection("participantes")
                .orderBy("puntos", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()
            val list = res.documents.mapNotNull { doc ->
                doc.toObject(CompetitionParticipant::class.java)
            }
            list.mapIndexed { idx, item -> item.copy(ranking = idx + 1) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun actualizarHabilidadesYErrores(
        pin: String,
        tema: String,
        correcta: Boolean,
        nuevaHabilidadScore: Int
    ) {
        try {
            val resultado = db.collection("usuarios")
                .whereEqualTo("pin", pin.toIntOrNull() ?: return)
                .get()
                .await()

            if (resultado.documents.isNotEmpty()) {
                val doc = resultado.documents.first()
                @Suppress("UNCHECKED_CAST")
                val currentSkills = doc.get("skills") as? Map<String, Long> ?: emptyMap()
                @Suppress("UNCHECKED_CAST")
                val currentErrors = doc.get("incorrectas_por_tema") as? Map<String, Long> ?: emptyMap()
                
                val currentCorrectas = doc.getLong("correctas")?.toInt() ?: 0
                val currentIncorrectas = doc.getLong("incorrectas")?.toInt() ?: 0
                val currentTotal = doc.getLong("total_preguntas")?.toInt() ?: 0

                val updatedSkills = currentSkills.toMutableMap()
                updatedSkills[tema] = nuevaHabilidadScore.toLong()

                val updatedErrors = currentErrors.toMutableMap()
                if (!correcta) {
                    val count = currentErrors[tema] ?: 0L
                    updatedErrors[tema] = count + 1L
                }

                val nextTotal = currentTotal + 1
                val nextCorrectas = if (correcta) currentCorrectas + 1 else currentCorrectas
                val nextIncorrectas = if (!correcta) currentIncorrectas + 1 else currentIncorrectas
                val nextPrecision = if (nextTotal > 0) (nextCorrectas.toDouble() / nextTotal) * 100.0 else 0.0

                doc.reference.update(
                    mapOf(
                        "skills" to updatedSkills,
                        "incorrectas_por_tema" to updatedErrors,
                        "total_preguntas" to nextTotal,
                        "correctas" to nextCorrectas,
                        "incorrectas" to nextIncorrectas,
                        "precision" to nextPrecision
                    )
                ).await()
            }
        } catch (e: Exception) {
            println("Error actualizando habilidades y errores: ${e.message}")
        }
    }

    suspend fun actualizarMisiones(pin: String, dailyInc: Int, weeklyInc: Int) {
        try {
            val resultado = db.collection("usuarios")
                .whereEqualTo("pin", pin.toIntOrNull() ?: return)
                .get()
                .await()

            if (resultado.documents.isNotEmpty()) {
                val doc = resultado.documents.first()
                val currentDaily = doc.getLong("daily_mission_progress")?.toInt() ?: 0
                val currentWeekly = doc.getLong("weekly_mission_progress")?.toInt() ?: 0

                doc.reference.update(
                    mapOf(
                        "daily_mission_progress" to currentDaily + dailyInc,
                        "weekly_mission_progress" to currentWeekly + weeklyInc
                    )
                ).await()
            }
        } catch (e: Exception) {
            println("Error actualizando misiones: ${e.message}")
        }
    }

    suspend fun guardarRecomendaciones(pin: String, recomendaciones: List<String>) {
        try {
            val resultado = db.collection("usuarios")
                .whereEqualTo("pin", pin.toIntOrNull() ?: return)
                .get()
                .await()

            if (resultado.documents.isNotEmpty()) {
                resultado.documents.first().reference
                    .update("recomendaciones", recomendaciones)
                    .await()
            }
        } catch (e: Exception) {
            println("Error guardando recomendaciones: ${e.message}")
        }
    }

    suspend fun guardarDiagnostico(
        pin: String,
        correctas: Int,
        incorrectas: Int,
        tiempoTotal: Long,
        tiempoPromedio: Double
    ) {
        try {
            val resultado = db.collection("usuarios")
                .whereEqualTo("pin", pin.toIntOrNull() ?: return)
                .get()
                .await()

            if (resultado.documents.isNotEmpty()) {
                val doc = resultado.documents.first()
                val curCorrectas = doc.getLong("correctas")?.toInt() ?: 0
                val curIncorrectas = doc.getLong("incorrectas")?.toInt() ?: 0
                val curTotal = doc.getLong("total_preguntas")?.toInt() ?: 0
                val curTime = doc.getLong("tiempo_total")?.toLong() ?: 0L

                val nextCorrectas = curCorrectas + correctas
                val nextIncorrectas = curIncorrectas + incorrectas
                val nextTotal = curTotal + correctas + incorrectas
                val nextTime = curTime + tiempoTotal
                val nextAvgTime = if (nextTotal > 0) nextTime.toDouble() / nextTotal else tiempoPromedio
                val nextPrecision = if (nextTotal > 0) (nextCorrectas.toDouble() / nextTotal) * 100.0 else 0.0

                doc.reference.update(
                    mapOf(
                        "diagnostico_realizado" to true,
                        "total_preguntas" to nextTotal,
                        "correctas" to nextCorrectas,
                        "incorrectas" to nextIncorrectas,
                        "tiempo_total" to nextTime,
                        "tiempo_promedio" to nextAvgTime,
                        "precision" to nextPrecision,
                        "fecha_diagnostico" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: Exception) {
            println("Error en guardarDiagnostico: ${e.message}")
        }
    }

    suspend fun comprarCosmetico(
        pin: String,
        nuevoCosmetico: String,
        esAvatar: Boolean,
        costo: Int
    ): Boolean {
        return try {
            val resultado = db.collection("usuarios")
                .whereEqualTo("pin", pin.toIntOrNull() ?: return false)
                .get()
                .await()

            if (resultado.documents.isNotEmpty()) {
                val doc = resultado.documents.first()
                val estrellasActuales = doc.getLong("estrellas") ?: 0
                if (estrellasActuales < costo) return false

                val campo = if (esAvatar) "unlocked_avatars" else "unlocked_themes"
                @Suppress("UNCHECKED_CAST")
                val listaActual = doc.get(campo) as? List<String> ?: emptyList()
                val listaActualizada = listaActual.toMutableList().apply {
                    if (!contains(nuevoCosmetico)) add(nuevoCosmetico)
                }

                doc.reference.update(
                    mapOf(
                        "estrellas" to (estrellasActuales - costo),
                        campo to listaActualizada
                    )
                ).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            println("Error comprando cosmético: ${e.message}")
            false
        }
    }

    suspend fun actualizarCosmeticos(pin: String, avatar: String, theme: String): Boolean {
        return try {
            val resultado = db.collection("usuarios")
                .whereEqualTo("pin", pin.toIntOrNull() ?: return false)
                .get()
                .await()

            if (resultado.documents.isNotEmpty()) {
                resultado.documents.first().reference.update(
                    mapOf(
                        "avatar" to avatar,
                        "equipped_theme" to theme
                    )
                ).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            println("Error actualizando cosméticos: ${e.message}")
            false
        }
    }

    // ============ ADULT OPERATIONS ============

    suspend fun loginConGoogle(idToken: String, email: String, rol: String): Pair<Boolean, Boolean> {
        return try {
            val auth = FirebaseAuth.getInstance()
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: return Pair(false, false)
            val uid = firebaseUser.uid

            val docRef = db.collection("usuarios").document(uid)
            val doc = docRef.get().await()
            if (doc.exists()) {
                val dbRole = doc.getString("rol") ?: ""
                val expectedRole = if (rol == "padres" || rol == "padre") "padre" else "docente"
                if (dbRole != expectedRole) {
                    println("Error: Conflicto de roles. Esperado: $expectedRole, Encontrado: $dbRole")
                    return Pair(false, false)
                }
                val isComplete = doc.getBoolean("perfil_completo") ?: false
                Pair(true, isComplete)
            } else {
                val rolNormalizado = if (rol == "padres" || rol == "padre") "padre" else "docente"
                val nuevoUsuario = hashMapOf(
                    "uid" to uid,
                    "email" to email,
                    "rol" to rolNormalizado,
                    "nombre" to (firebaseUser.displayName ?: email.substringBefore("@")),
                    "perfil_completo" to false
                )
                docRef.set(nuevoUsuario).await()
                Pair(true, false)
            }
        } catch (e: Exception) {
            println("Error en loginConGoogle: ${e.message}")
            e.printStackTrace()
            Pair(false, false)
        }
    }

    suspend fun actualizarPerfilDocente(
        uid: String,
        nombre: String,
        colegio: String,
        grado: String,
        seccion: String
    ): Boolean {
        return try {
            db.collection("usuarios")
                .document(uid)
                .update(
                    mapOf(
                        "nombre" to nombre,
                        "colegio" to colegio,
                        "grado" to grado,
                        "seccion" to seccion,
                        "perfil_completo" to true
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            println("Error actualizando perfil docente: ${e.message}")
            false
        }
    }

    // ============ PLATFORM IMPROVEMENT OPERATIONS ============

    suspend fun registrarRespuestaHistorial(res: RespuestaHistorial): Boolean {
        return try {
            val docRef = db.collection("HistorialRespuestas")
                .document(res.estudianteId)
                .collection("respuestas")
                .document(res.id.ifEmpty { db.collection("HistorialRespuestas").document().id })
            
            val finalRes = res.copy(id = docRef.id)
            docRef.set(finalRes).await()
            true
        } catch (e: Exception) {
            println("Error en registrarRespuestaHistorial: ${e.message}")
            false
        }
    }

    suspend fun obtenerHistorialRespuestas(pin: String): List<RespuestaHistorial> {
        return try {
            val resultado = db.collection("HistorialRespuestas")
                .document(pin)
                .collection("respuestas")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            resultado.documents.mapNotNull { it.toObject(RespuestaHistorial::class.java) }
        } catch (e: Exception) {
            println("Error en obtenerHistorialRespuestas: ${e.message}")
            emptyList()
        }
    }

    suspend fun guardarReporteSesion(reporte: ReporteSesion): Boolean {
        return try {
            val docRef = db.collection("ReportesSesiones")
                .document(reporte.id.ifEmpty { db.collection("ReportesSesiones").document().id })
            val finalReporte = reporte.copy(id = docRef.id)
            docRef.set(finalReporte).await()
            true
        } catch (e: Exception) {
            println("Error en guardarReporteSesion: ${e.message}")
            false
        }
    }

    suspend fun obtenerReportesSesionPorDocente(docenteId: String): List<ReporteSesion> {
        return try {
            val resultado = db.collection("ReportesSesiones")
                .whereEqualTo("docenteId", docenteId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            resultado.documents.mapNotNull { it.toObject(ReporteSesion::class.java) }
        } catch (e: Exception) {
            println("Error en obtenerReportesSesionPorDocente: ${e.message}")
            emptyList()
        }
    }

    suspend fun obtenerReportesSesionPorPadre(padreEmail: String): List<ReporteSesion> {
        return try {
            val resultado = db.collection("ReportesSesiones")
                .whereEqualTo("padreEmail", padreEmail)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            resultado.documents.mapNotNull { it.toObject(ReporteSesion::class.java) }
        } catch (e: Exception) {
            println("Error en obtenerReportesSesionPorPadre: ${e.message}")
            emptyList()
        }
    }

    suspend fun guardarObservacion(pin: String, obs: Observacion): Boolean {
        return try {
            val docRef = db.collection("usuarios")
                .document(pin)
                .collection("observaciones")
                .document(obs.id.ifEmpty { db.collection("usuarios").document().id })
            val finalObs = obs.copy(id = docRef.id)
            docRef.set(finalObs).await()
            true
        } catch (e: Exception) {
            println("Error en guardarObservacion: ${e.message}")
            false
        }
    }

    suspend fun obtenerObservaciones(pin: String): List<Observacion> {
        return try {
            val resultado = db.collection("usuarios")
                .document(pin)
                .collection("observaciones")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            resultado.documents.mapNotNull { it.toObject(Observacion::class.java) }
        } catch (e: Exception) {
            println("Error en obtenerObservaciones: ${e.message}")
            emptyList()
        }
    }

    suspend fun guardarNotificacion(uid: String, not: NotificacionFirebase): Boolean {
        return try {
            val docRef = db.collection("usuarios")
                .document(uid)
                .collection("notificaciones")
                .document(not.id.ifEmpty { db.collection("usuarios").document().id })
            val finalNot = not.copy(id = docRef.id)
            docRef.set(finalNot).await()
            true
        } catch (e: Exception) {
            println("Error en guardarNotificacion: ${e.message}")
            false
        }
    }

    suspend fun obtenerNotificaciones(uid: String): List<NotificacionFirebase> {
        return try {
            val resultado = db.collection("usuarios")
                .document(uid)
                .collection("notificaciones")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            resultado.documents.mapNotNull { it.toObject(NotificacionFirebase::class.java) }
        } catch (e: Exception) {
            println("Error en obtenerNotificaciones: ${e.message}")
            emptyList()
        }
    }

    suspend fun obtenerAdultoPorUid(uid: String): AdultoFirebase? {
        return try {
            val doc = db.collection("usuarios").document(uid).get().await()
            if (doc.exists()) {
                doc.toObject(AdultoFirebase::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error en obtenerAdultoPorUid: ${e.message}")
            null
        }
    }

    suspend fun actualizarPerfilPadre(uid: String, nombre: String, pinHijo: Int): Boolean {
        return try {
            db.collection("usuarios").document(uid).update(
                mapOf(
                    "nombre" to nombre,
                    "perfil_completo" to true,
                    "estudiante_pin" to pinHijo
                )
            ).await()
            true
        } catch (e: Exception) {
            println("Error en actualizarPerfilPadre: ${e.message}")
            false
        }
    }

    // ============ ASYNCHRONOUS & IDEMPOTENT DUEL OPERATIONS ============

    suspend fun buscarEstudiantePorApodo(apodoBuscado: String): FirebaseStudent? {
        return try {
            val query = db.collection("usuarios")
                .whereEqualTo("apodo", apodoBuscado.trim())
                .get().await()
            if (!query.isEmpty) {
                query.documents.first().toObject(FirebaseStudent::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error buscando estudiante por apodo: ${e.message}")
            null
        }
    }

    suspend fun guardarFcmToken(studentPin: String, fcmToken: String): Boolean {
        if (studentPin.isEmpty() || fcmToken.isEmpty()) return false
        return try {
            db.collection("usuarios").document(studentPin)
                .update("fcmToken", fcmToken)
                .await()
            true
        } catch (e: Exception) {
            println("Error guardando token FCM: ${e.message}")
            false
        }
    }

    suspend fun crearDesafioDuelo(
        challenger: FirebaseStudent,
        challenged: FirebaseStudent,
        topic: String,
        classroomId: String = ""
    ): Pair<Boolean, String> {
        return try {
            val cPin = challenger.pin.toString()
            val dPin = challenged.pin.toString()

            val existingPending = db.collection("duels")
                .whereEqualTo("challengerId", cPin)
                .whereEqualTo("challengedId", dPin)
                .whereEqualTo("status", DuelStatus.PENDING.name)
                .get().await()

            if (!existingPending.isEmpty) {
                return Pair(false, "Ya tienes un desafío pendiente con este compañero.")
            }

            val now = System.currentTimeMillis()
            val duelId = "DUEL_${now}_${cPin}_${dPin}"
            val normTopic = com.example.mathia.AdaptiveEngine.normalizarTema(topic)
            val calcDifficulty = ((challenger.nivel_actual + challenged.nivel_actual) / 2).coerceIn(1, 5)

            // Generate 10 fixed questions for the fixed difficulty
            val questionsList = mutableListOf<DuelQuestionItem>()
            for (i in 1..10) {
                val (enunciado, correctInt) = com.example.mathia.data.generator.DynamicExerciseGenerator.generateQuestion(normTopic, calcDifficulty)
                val correctStr = correctInt.toString()
                val optA = correctStr
                val optB = (correctInt + 2).toString()
                val optC = (correctInt - 1).coerceAtLeast(0).toString()
                val optD = (correctInt + 5).toString()
                val shuffledOpts = listOf(optA, optB, optC, optD).shuffled()
                questionsList.add(
                    DuelQuestionItem(
                        questionId = "Q_${normTopic}_${calcDifficulty}_$i",
                        enunciado = enunciado,
                        opcionA = shuffledOpts.getOrElse(0) { optA },
                        opcionB = shuffledOpts.getOrElse(1) { optB },
                        opcionC = shuffledOpts.getOrElse(2) { optC },
                        opcionD = shuffledOpts.getOrElse(3) { optD },
                        correcta = correctStr
                    )
                )
            }

            val expiresAt = now + (24 * 60 * 60 * 1000L) // 24 hours to accept
            val match = DuelMatch(
                duelId = duelId,
                challengerId = challenger.pin.toString(),
                challengerApodo = if (challenger.apodo.isNotBlank()) challenger.apodo else "MateNinja #${challenger.pin}",
                challengerAvatar = challenger.avatar,
                challengedId = challenged.pin.toString(),
                challengedApodo = if (challenged.apodo.isNotBlank()) challenged.apodo else "MateNinja #${challenged.pin}",
                challengedAvatar = challenged.avatar,
                classroomId = classroomId.ifEmpty { challenger.aulas.firstOrNull() ?: "" },
                topic = normTopic,
                difficulty = calcDifficulty,
                status = DuelStatus.PENDING,
                questions = questionsList,
                createdAt = now,
                expiresAt = expiresAt
            )

            db.collection("duels").document(duelId).set(match).await()
            Pair(true, duelId)
        } catch (e: Exception) {
            println("Error creando desafío de duelo: ${e.message}")
            Pair(false, e.message ?: "Error al crear desafío")
        }
    }

    fun escucharDuelosEstudiante(
        studentPin: String,
        onUpdate: (List<DuelMatch>) -> Unit
    ): com.google.firebase.firestore.ListenerRegistration {
        return db.collection("duels")
            .whereIn("challengerId", listOf(studentPin))
            .addSnapshotListener { snapshot1, _ ->
                db.collection("duels")
                    .whereIn("challengedId", listOf(studentPin))
                    .addSnapshotListener { snapshot2, _ ->
                        val list1 = snapshot1?.documents?.mapNotNull { it.toObject(DuelMatch::class.java) } ?: emptyList()
                        val list2 = snapshot2?.documents?.mapNotNull { it.toObject(DuelMatch::class.java) } ?: emptyList()
                        val combined = (list1 + list2).distinctBy { it.duelId }.sortedByDescending { it.createdAt }
                        onUpdate(combined)
                    }
            }
    }

    suspend fun aceptarDesafioDuelo(duelId: String): Boolean {
        return try {
            val now = System.currentTimeMillis()
            val expiresAt = now + (48 * 60 * 60 * 1000L) // 48 hours to complete
            db.collection("duels").document(duelId).update(
                mapOf(
                    "status" to DuelStatus.ACCEPTED.name,
                    "acceptedAt" to now,
                    "expiresAt" to expiresAt
                )
            ).await()
            true
        } catch (e: Exception) {
            println("Error aceptando duelo: ${e.message}")
            false
        }
    }

    suspend fun rechazarDesafioDuelo(duelId: String): Boolean {
        return try {
            db.collection("duels").document(duelId).update("status", DuelStatus.DECLINED.name).await()
            true
        } catch (e: Exception) {
            println("Error rechazando duelo: ${e.message}")
            false
        }
    }

    suspend fun cancelarDesafioDuelo(duelId: String): Boolean {
        return try {
            db.collection("duels").document(duelId).update("status", DuelStatus.CANCELLED.name).await()
            true
        } catch (e: Exception) {
            println("Error cancelando duelo: ${e.message}")
            false
        }
    }

    suspend fun registrarRespuestaDuelo(
        duelId: String,
        playerPin: String,
        questionId: String,
        selectedAnswer: String,
        correct: Boolean,
        timeSpent: Long
    ): Boolean {
        return try {
            val responseDocId = "${playerPin}_${questionId}"
            val responseDetail = DuelResponseDetail(
                playerPin = playerPin,
                questionId = questionId,
                selectedAnswer = selectedAnswer,
                correct = correct,
                timeSpent = timeSpent,
                answeredAt = System.currentTimeMillis()
            )
            db.collection("duels")
                .document(duelId)
                .collection("responses")
                .document(responseDocId)
                .set(responseDetail)
                .await()
            true
        } catch (e: Exception) {
            println("Error registrando respuesta de duelo: ${e.message}")
            false
        }
    }

    suspend fun guardarProgresoYFinalizarDuelo(
        duelId: String,
        playerPin: String,
        score: Int,
        totalTime: Long
    ): Boolean {
        return try {
            val docRef = db.collection("duels").document(duelId)
            val doc = docRef.get().await()
            if (!doc.exists()) return false

            val currentMatch = doc.toObject(DuelMatch::class.java) ?: return false
            val isChallenger = currentMatch.challengerId == playerPin

            val updates = mutableMapOf<String, Any>()
            if (isChallenger) {
                updates["challengerScore"] = score
                updates["challengerTime"] = totalTime
                updates["challengerFinished"] = true
            } else {
                updates["challengedScore"] = score
                updates["challengedTime"] = totalTime
                updates["challengedFinished"] = true
            }
            updates["status"] = DuelStatus.IN_PROGRESS.name
            docRef.update(updates).await()

            val cFin = if (isChallenger) true else currentMatch.challengerFinished
            val dFin = if (!isChallenger) true else currentMatch.challengedFinished

            // If both players have completed their attempts, execute atomic completion & reward processing
            if (cFin && dFin) {
                procesarResultadoAtomicoDuelo(duelId)
            }
            true
        } catch (e: Exception) {
            println("Error guardando progreso de duelo: ${e.message}")
            false
        }
    }

    private suspend fun procesarResultadoAtomicoDuelo(duelId: String) {
        val duelDocRef = db.collection("duels").document(duelId)
        try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(duelDocRef)
                if (!snapshot.exists()) return@runTransaction null
                val processed = snapshot.getBoolean("rewardProcessed") ?: false
                if (processed) return@runTransaction null // Idempotency check: already processed

                val cScore = (snapshot.getLong("challengerScore") ?: 0L).toInt()
                val dScore = (snapshot.getLong("challengedScore") ?: 0L).toInt()
                val cTime = snapshot.getLong("challengerTime") ?: 0L
                val dTime = snapshot.getLong("challengedTime") ?: 0L
                val cPin = snapshot.getString("challengerId") ?: ""
                val dPin = snapshot.getString("challengedId") ?: ""
                val cApodo = snapshot.getString("challengerApodo") ?: ""
                val dApodo = snapshot.getString("challengedApodo") ?: ""

                val winnerPair: Pair<String, String> = when {
                    cScore > dScore -> Pair(cPin, cApodo)
                    dScore > cScore -> Pair(dPin, dApodo)
                    cTime < dTime -> Pair(cPin, cApodo)
                    dTime < cTime -> Pair(dPin, dApodo)
                    else -> Pair("EMPATE", "¡Empate!")
                }

                // Update duel document to COMPLETED and rewardProcessed = true
                transaction.update(
                    duelDocRef,
                    mapOf(
                        "status" to DuelStatus.COMPLETED.name,
                        "winnerId" to winnerPair.first,
                        "winnerApodo" to winnerPair.second,
                        "rewardProcessed" to true,
                        "finishedAt" to System.currentTimeMillis()
                    )
                )

                // Atomic rewards allocation (does NOT modify academic accuracy/skills)
                fun aplicarRecompensa(pin: String, isWinner: Boolean, isDraw: Boolean) {
                    if (pin.isEmpty()) return
                    val userDocRef = db.collection("usuarios").document(pin)
                    val userSnapshot = transaction.get(userDocRef)
                    if (userSnapshot.exists()) {
                        val curXP = userSnapshot.getLong("xp") ?: 0L
                        val curStars = userSnapshot.getLong("estrellas") ?: 0L

                        val addXP = if (isDraw) 20L else if (isWinner) 30L else 15L
                        val addStars = if (isDraw) 10L else if (isWinner) 15L else 5L

                        transaction.update(
                            userDocRef,
                            mapOf(
                                "xp" to (curXP + addXP),
                                "estrellas" to (curStars + addStars)
                            )
                        )
                    }
                }

                val isDraw = winnerPair.first == "EMPATE"
                aplicarRecompensa(cPin, winnerPair.first == cPin, isDraw)
                aplicarRecompensa(dPin, winnerPair.first == dPin, isDraw)

                null
            }.await()
        } catch (e: Exception) {
            println("Error en procesarResultadoAtomicoDuelo: ${e.message}")
        }
    }
}