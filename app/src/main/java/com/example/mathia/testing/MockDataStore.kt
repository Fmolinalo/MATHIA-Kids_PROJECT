package com.example.mathia.testing

import com.example.mathia.model.*

/**
 * Isolated Mock Data Store for Debug / Testing purposes.
 * Kept outside of production ViewModel logic to prevent leaking test mocks into release builds.
 */
object MockDataStore {

    var isDemoMode = false
    val mockUsuarios = mutableMapOf<String, Any>()
    val mockHistorial = mutableListOf<RespuestaHistorial>()
    val mockReportes = mutableListOf<ReporteSesion>()
    val mockObservaciones = mutableListOf<Observacion>()
    val mockNotificaciones = mutableListOf<NotificacionFirebase>()

    init {
        resetMockData()
    }

    fun resetMockData() {
        mockUsuarios.clear()
        mockHistorial.clear()
        mockReportes.clear()
        mockObservaciones.clear()
        mockNotificaciones.clear()

        // 1. Student Sofía Pacheco (pin "1234")
        mockUsuarios["1234"] = FirebaseStudent(
            nombre = "Sofía Pacheco",
            grado = "3ro de Primaria",
            seccion = "Sección A",
            edad = 6,
            colegio = "Maria Auxiliadora",
            docente_asignado = "Efraín Caceres",
            fecha_creacion = System.currentTimeMillis() - 10 * 24 * 3600000L,
            nivel_actual = 1,
            precision = 85.0,
            estrellas = 505,
            xp = 90,
            pin = 1234,
            padre_email = "parent@mathia.com",
            avatar = "default",
            equipped_theme = "Lila Clásico",
            unlocked_avatars = listOf("default"),
            unlocked_themes = listOf("Lila Clásico", "Verde Menta"),
            streak = 3,
            daily_mission_progress = 3,
            weekly_mission_progress = 12,
            skills = mapOf(
                "Sumas" to 90,
                "Restas" to 80,
                "Multiplicación" to 75,
                "Fracciones" to 60,
                "Series" to 85
            ),
            incorrectas_por_tema = mapOf("Fracciones" to 5, "Multiplicación" to 3),
            asistencia = listOf("2026-06-12", "2026-06-13", "2026-06-14", "2026-06-15"),
            recomendaciones = listOf(
                "¡Increíble precisión del 85%!",
                "Te sugiero practicar un poco más de Fracciones para dominarlo por completo.",
                "¡Llevas una súper racha de 3 días practicando!"
            )
        )

        // 2. Parent Melisa Gutierrez (uid "parent_uid")
        mockUsuarios["parent_uid"] = AdultoFirebase(
            uid = "parent_uid",
            email = "parent@mathia.com",
            password = "123456",
            rol = "padre",
            nombre = "Melisa Gutierrez",
            perfil_completo = true,
            estudiante_pin = 1234
        )

        // 3. Teacher Efraín Caceres (uid "teacher_uid")
        mockUsuarios["teacher_uid"] = AdultoFirebase(
            uid = "teacher_uid",
            email = "teacher@mathia.com",
            password = "123456",
            rol = "docente",
            nombre = "Efraín Caceres",
            colegio = "Maria Auxiliadora",
            grado = "3ro de Primaria",
            seccion = "Sección A",
            perfil_completo = true
        )

        // Pre-populate mock observations
        mockObservaciones.add(
            Observacion(
                id = "obs1",
                docenteId = "teacher_uid",
                docenteNombre = "Prof. Efraín Caceres",
                texto = "Sofía mostró excelente avance en sumas y multiplicaciones esta semana.",
                fecha = "2026-06-14",
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
