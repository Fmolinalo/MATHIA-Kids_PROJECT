package com.example.mathia.model

// ─── Star Economy Constants & Helpers ─────────────────────────────────────
const val MAX_STARS = 3000

fun calculateAddedStars(currentStars: Int, amount: Int): Int {
    return (currentStars + amount).coerceAtMost(MAX_STARS)
}

// ─── Enums & Helper Models ─────────────────────────────────────────────────
enum class AlertType {
    STREAK,      // Orange theme
    SUCCESS,     // Green theme
    MOTIVATIONAL, // Purple theme
    CHALLENGE    // Pink/Blue theme
}

data class MathiaAlert(
    val title: String,
    val message: String,
    val type: AlertType,
    val buttonText: String = "¡Entendido!"
)

data class Reward(
    val id: Int,
    val name: String,
    val emoji: String,
    val stars: Int,
    val desc: String
)

data class ShopItem(
    val id: String,
    val name: String,
    val visual: String, // Emoji or representation
    val cost: Int,
    val isAvatar: Boolean
)

data class ExamResponse(
    val pregunta: String,
    val correcta: Boolean,
    val tiempo: Long,
    val dificultad: Int
)

data class Mission(
    val id: String,
    val description: String,
    val target: Int,
    val current: Int,
    val rewardStars: Int,
    val isWeekly: Boolean
)

// ─── Domain Student Model ──────────────────────────────────────────────────
data class Student(
    val id: Int,
    val name: String,
    val lastName: String,
    val grade: String,
    val classroom: String,
    val pin: String,
    val level: Int,
    val stars: Int,
    val xp: Int = 0,
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val examsCompleted: Int = 0,
    val accuracy: Int = 0,
    val streak: Int = 0,
    val weekData: List<Int> = listOf(0, 0, 0, 0, 0, 0, 0),
    val monthData: List<Int> = listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    val skills: Map<String, Int> = emptyMap(),
    val incorrectasPorTema: Map<String, Int> = emptyMap(),
    val asistencia: List<String> = emptyList(),
    val recomendaciones: List<String> = emptyList(),
    val avatar: String = "ajolote_default",
    val diagnosticoRealizado: Boolean = false,
    val tiempoTotal: Long = 0,
    val tiempoPromedio: Double = 0.0,
    val equippedTheme: String = "Lila Clásico",
    val unlockedAvatars: List<String> = listOf("ajolote_default"),
    val unlockedThemes: List<String> = listOf("Lila Clásico"),
    val dailyMissionProgress: Int = 0,
    val weeklyMissionProgress: Int = 0,
    val edad: Int = 6,
    val colegio: String = "",
    val seccion: String = "",
    val docenteAsignado: String = "",
    val fechaCreacion: Long = 0L,
    val padreEmail: String = "",
    val apodo: String = "",
    val studentId: String = "",
    val aulas: List<String> = emptyList(),
    val dificultadPorTema: Map<String, Int> = emptyMap()
)

// ─── Student Credential & Authentication Models ─────────────────────────────
data class StudentCredential(
    val studentId: String = "",
    val credentialType: String = "PIN",
    val credentialHash: String = "",
    val salt: String = "",
    val failedAttempts: Int = 0,
    val lockedUntil: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)

sealed class StudentAuthResult {
    data class Success(val student: Student) : StudentAuthResult()
    data class InvalidPin(val remainingAttempts: Int) : StudentAuthResult()
    data class AccountLocked(val lockedUntilMs: Long) : StudentAuthResult()
    object UserNotFound : StudentAuthResult()
    data class Error(val message: String) : StudentAuthResult()
}

// ─── Firebase Models ───────────────────────────────────────────────────────
data class FirebaseStudent(
    val nombre: String = "",
    val apodo: String = "",
    val grado: String = "",
    val edad: Int = 6,
    val colegio: String = "",
    val seccion: String = "",
    val docente_asignado: String = "Sin asignar",
    val fecha_creacion: Long = 0L,
    val nivel_actual: Int = 1,
    val precision: Double = 0.0,
    val estrellas: Int = 0,
    val pin: Int = 0,
    val padre_email: String = "",
    val avatar: String = "ajolote_default",
    val equipped_theme: String = "Lila Clásico",
    val unlocked_avatars: List<String> = listOf("ajolote_default"),
    val unlocked_themes: List<String> = listOf("Lila Clásico"),
    
    // Gamification & AI tracking fields
    val xp: Int = 0,
    val streak: Int = 0,
    val total_preguntas: Int = 0,
    val correctas: Int = 0,
    val incorrectas: Int = 0,
    val tiempo_total: Long = 0,
    val tiempo_promedio: Double = 0.0,
    val diagnostico_realizado: Boolean = false,
    val weekData: List<Int> = listOf(0, 0, 0, 0, 0, 0, 0),
    val monthData: List<Int> = listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    val skills: Map<String, Int> = emptyMap(),
    val incorrectas_por_tema: Map<String, Int> = emptyMap(),
    val asistencia: List<String> = emptyList(),
    val recomendaciones: List<String> = emptyList(),
    val daily_mission_progress: Int = 0,
    val weekly_mission_progress: Int = 0,
    val aulas: List<String> = emptyList(),
    val dificultad_por_tema: Map<String, Int> = emptyMap()
)

data class AdultoFirebase(
    val uid: String = "",
    val email: String = "",
    val password: String = "",
    val rol: String = "", // "padre" or "docente"
    val nombre: String = "",
    val colegio: String = "",
    val grado: String = "",
    val seccion: String = "",
    val perfil_completo: Boolean = false,
    val estudiante_pin: Int? = null,
    val fechaRegistro: Long = 0
)

// ─── Platform Improvement Models ───────────────────────────────────────────
data class RespuestaHistorial(
    val id: String = "",
    val estudianteId: String = "",
    val estudianteNombre: String = "",
    val grado: String = "",
    val seccion: String = "",
    val docenteAsignado: String = "",
    val padreAsociado: String = "",
    val fecha: String = "",
    val hora: String = "",
    val timestamp: Long = 0L,
    val operacion: String = "",
    val pregunta: String = "",
    val respuestaCorrecta: String = "",
    val respuestaElegida: String = "",
    val tiempoRespuesta: Long = 0L,
    val dificultad: String = "",
    val correcta: Boolean = false,
    val intentos: Int = 1,
    val estrellasObtenidas: Int = 0,
    val experienciaGanada: Int = 0,
    val dispositivo: String = ""
)

data class ReporteSesion(
    val id: String = "",
    val estudianteId: String = "",
    val estudianteNombre: String = "",
    val grado: String = "",
    val seccion: String = "",
    val docenteId: String = "",
    val padreEmail: String = "",
    val tipo: String = "", // "Práctica" o "Examen"
    val preguntasCount: Int = 0,
    val correctas: Int = 0,
    val incorrectas: Int = 0,
    val tiempoPromedio: Double = 0.0,
    val nivel: Int = 1,
    val competenciasDominadas: List<String> = emptyList(),
    val competenciasReforzar: List<String> = emptyList(),
    val recomendaciones: List<String> = emptyList(),
    val estrellasObtenidas: Int = 0,
    val fecha: String = "",
    val timestamp: Long = 0L
)

data class Observacion(
    val id: String = "",
    val docenteId: String = "",
    val docenteNombre: String = "",
    val texto: String = "",
    val fecha: String = "",
    val timestamp: Long = 0L
)

data class NotificacionFirebase(
    val id: String = "",
    val titulo: String = "",
    val cuerpo: String = "",
    val timestamp: Long = 0L,
    val read: Boolean = false,
    val tipo: String = "" // "alumno", "docente", "padre"
)

// ─── Classroom & Member Models ─────────────────────────────────────────────
data class Classroom(
    val classroomId: String = "",
    val joinCode: String = "",
    val name: String = "",
    val grade: String = "",
    val section: String = "",
    val teacherId: String = "",
    val teacherName: String = "",
    val school: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

data class JoinRequest(
    val requestId: String = "",
    val classroomId: String = "",
    val classroomName: String = "",
    val joinCode: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val parentId: String = "",
    val parentEmail: String = "",
    val teacherId: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED, CANCELLED
    val requestedAt: Long = System.currentTimeMillis(),
    val respondedAt: Long? = null
)

sealed class JoinRequestResult {
    data class Success(val request: JoinRequest) : JoinRequestResult()
    object InvalidCode : JoinRequestResult()
    object AlreadyMemberOrPending : JoinRequestResult()
    data class Error(val message: String) : JoinRequestResult()
}

sealed class ClassroomValidationResult {
    data class Success(val classroom: Classroom) : ClassroomValidationResult()
    object InvalidCode : ClassroomValidationResult()
    object ClassroomClosed : ClassroomValidationResult()
    data class Error(val message: String) : ClassroomValidationResult()
}

sealed class ClassroomLinkResult {
    data class Success(val classroomName: String, val studentName: String) : ClassroomLinkResult()
    object AlreadyLinked : ClassroomLinkResult()
    object Unauthorized : ClassroomLinkResult()
    data class Error(val message: String) : ClassroomLinkResult()
}

// ─── Calendar Models ───────────────────────────────────────────────────────
enum class CalendarEventSource {
    MATHIA,
    GOOGLE,
    TEACHER
}

data class CalendarEvent(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "", // ISO format "yyyy-MM-dd"
    val startTime: String = "", // Format "HH:mm" or empty if allDay
    val endTime: String = "",   // Format "HH:mm" or empty if allDay
    val isAllDay: Boolean = true,
    val source: CalendarEventSource = CalendarEventSource.MATHIA,
    val externalId: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

// ─── Single Source of Truth Streak Calculator ──────────────────────────────
object StreakCalculator {
    val LIMA_ZONE: java.time.ZoneId = java.time.ZoneId.of("America/Lima")

    fun getActiveStreak(asistencia: List<String>): Int {
        if (asistencia.isEmpty()) return 0

        val today = java.time.LocalDate.now(LIMA_ZONE)
        val yesterday = today.minusDays(1)

        // 1. Normalizar, eliminar duplicados (varias prácticas el mismo día -> 1 día) y ordenar
        val dateSet = asistencia.mapNotNull { dateStr ->
            try { java.time.LocalDate.parse(dateStr) } catch (e: Exception) { null }
        }.toSet()

        if (dateSet.isEmpty()) return 0

        // 2. Determinar si hay práctica válida hoy o ayer
        val startDate = when {
            dateSet.contains(today) -> today
            dateSet.contains(yesterday) -> yesterday
            else -> return 0 // Si no se practicó ni hoy ni ayer -> racha activa = 0
        }

        // 3. Contar días consecutivos hacia atrás desde la última práctica activa
        var streakCount = 0
        var checkDate = startDate
        while (dateSet.contains(checkDate)) {
            streakCount++
            checkDate = checkDate.minusDays(1)
        }

        return streakCount
    }
}

// ─── Weekly Competition & Duels Models ──────────────────────────────────────
data class WeeklyCompetition(
    val id: String = "",
    val title: String = "Desafío de Divisiones",
    val tema: String = "divisiones", // "sumas", "restas", "multiplicacion", "divisiones", "fracciones", "series", "problemas"
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val targetExercises: Int = 20,
    val rewardStars: Int = 150,
    val activa: Boolean = true
)

data class CompetitionParticipant(
    val studentId: String = "",
    val apodo: String = "",
    val avatar: String = "ajolote_default",
    val puntos: Int = 0,
    val correctas: Int = 0,
    val incorrectas: Int = 0,
    val precision: Double = 0.0,
    val ranking: Int = 0
)

enum class DuelStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    IN_PROGRESS,
    COMPLETED,
    EXPIRED,
    CANCELLED
}

data class DuelQuestionItem(
    val questionId: String = "",
    val enunciado: String = "",
    val opcionA: String = "",
    val opcionB: String = "",
    val opcionC: String = "",
    val opcionD: String = "",
    val correcta: String = ""
)

data class DuelResponseDetail(
    val playerPin: String = "",
    val questionId: String = "",
    val selectedAnswer: String = "",
    val correct: Boolean = false,
    val timeSpent: Long = 0L,
    val answeredAt: Long = System.currentTimeMillis()
)

data class DuelMatch(
    val duelId: String = "",
    val challengerId: String = "",
    val challengerApodo: String = "",
    val challengerAvatar: String = "ajolote_default",
    val challengedId: String = "",
    val challengedApodo: String = "",
    val challengedAvatar: String = "ajolote_default",
    val classroomId: String = "",
    val topic: String = "Divisiones",
    val difficulty: Int = 3,
    val status: DuelStatus = DuelStatus.PENDING,
    val questions: List<DuelQuestionItem> = emptyList(),
    val challengerScore: Int = 0,
    val challengedScore: Int = 0,
    val challengerTime: Long = 0L,
    val challengedTime: Long = 0L,
    val challengerFinished: Boolean = false,
    val challengedFinished: Boolean = false,
    val winnerId: String = "",
    val winnerApodo: String = "",
    val rewardProcessed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val acceptedAt: Long = 0L,
    val finishedAt: Long = 0L,
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
)



