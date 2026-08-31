package com.example.mathia.data.mapper

import com.example.mathia.data.StudentEntity
import com.example.mathia.model.FirebaseStudent
import com.example.mathia.model.Student

object ModelMappers {

    fun FirebaseStudent.toDomain(studentId: String = ""): Student {
        val parts = nombre.split(" ")
        val firstName = parts.firstOrNull() ?: nombre
        val lastName = if (parts.size > 1) parts.drop(1).joinToString(" ") else ""

        val effectiveId = studentId.ifEmpty { pin.toString() }
        val idAsInt = effectiveId.toIntOrNull() ?: pin

        return Student(
            id = idAsInt,
            name = firstName,
            lastName = lastName,
            grade = grado,
            classroom = seccion,
            pin = pin.toString(),
            level = nivel_actual,
            stars = estrellas,
            xp = xp,
            totalQuestions = total_preguntas,
            correctAnswers = correctas,
            examsCompleted = if (total_preguntas > 0) total_preguntas / 10 + 1 else 0,
            accuracy = precision.toInt(),
            streak = streak,
            weekData = weekData,
            monthData = monthData,
            skills = skills.ifEmpty {
                mapOf(
                    "Sumas" to precision.toInt(),
                    "Restas" to precision.toInt(),
                    "Multiplicación" to precision.toInt(),
                    "Fracciones" to precision.toInt(),
                    "Series" to precision.toInt()
                )
            },
            incorrectasPorTema = incorrectas_por_tema,
            asistencia = asistencia,
            recomendaciones = recomendaciones,
            avatar = avatar,
            diagnosticoRealizado = diagnostico_realizado,
            tiempoTotal = tiempo_total,
            tiempoPromedio = tiempo_promedio,
            equippedTheme = equipped_theme,
            unlockedAvatars = unlocked_avatars,
            unlockedThemes = unlocked_themes,
            dailyMissionProgress = daily_mission_progress,
            weeklyMissionProgress = weekly_mission_progress,
            edad = edad,
            colegio = colegio,
            seccion = seccion,
            docenteAsignado = docente_asignado,
            fechaCreacion = fecha_creacion,
            padreEmail = padre_email,
            apodo = apodo,
            studentId = effectiveId,
            aulas = aulas,
            dificultadPorTema = dificultad_por_tema
        )
    }

    fun Student.toFirebase(): FirebaseStudent {
        val fullName = if (lastName.isNotBlank()) "$name $lastName" else name
        return FirebaseStudent(
            nombre = fullName,
            apodo = apodo,
            grado = grade,
            edad = edad,
            colegio = colegio,
            seccion = classroom,
            docente_asignado = docenteAsignado,
            fecha_creacion = fechaCreacion,
            nivel_actual = level,
            precision = accuracy.toDouble(),
            estrellas = stars,
            pin = pin.toIntOrNull() ?: id,
            padre_email = padreEmail,
            avatar = avatar,
            equipped_theme = equippedTheme,
            unlocked_avatars = unlockedAvatars,
            unlocked_themes = unlockedThemes,
            xp = xp,
            streak = streak,
            total_preguntas = totalQuestions,
            correctas = correctAnswers,
            tiempo_total = tiempoTotal,
            tiempo_promedio = tiempoPromedio,
            diagnostico_realizado = diagnosticoRealizado,
            weekData = weekData,
            monthData = monthData,
            skills = skills,
            incorrectas_por_tema = incorrectasPorTema,
            asistencia = asistencia,
            recomendaciones = recomendaciones,
            daily_mission_progress = dailyMissionProgress,
            weekly_mission_progress = weeklyMissionProgress,
            aulas = aulas,
            dificultad_por_tema = dificultadPorTema
        )
    }

    fun StudentEntity.toDomain(): Student {
        val parts = nombre.split(" ")
        val firstName = parts.firstOrNull() ?: nombre
        val lastName = if (parts.size > 1) parts.drop(1).joinToString(" ") else ""

        return Student(
            id = studentId.toIntOrNull() ?: 1,
            name = firstName,
            lastName = lastName,
            grade = grado,
            classroom = seccion,
            pin = "",
            level = nivelActual,
            stars = estrellas,
            xp = xp,
            totalQuestions = totalPreguntas,
            correctAnswers = correctas,
            examsCompleted = if (totalPreguntas > 0) totalPreguntas / 10 + 1 else 0,
            accuracy = precision.toInt(),
            streak = streak,
            weekData = weekData,
            monthData = monthData,
            skills = skills,
            incorrectasPorTema = incorrectasPorTema,
            asistencia = asistencia,
            recomendaciones = recomendaciones,
            avatar = avatar,
            diagnosticoRealizado = diagnosticoRealizado,
            tiempoTotal = tiempoTotal,
            tiempoPromedio = tiempoPromedio,
            equippedTheme = equippedTheme,
            unlockedAvatars = unlockedAvatars,
            unlockedThemes = unlockedThemes,
            edad = edad,
            colegio = colegio,
            seccion = seccion,
            docenteAsignado = docenteAsignado,
            padreEmail = padreEmail,
            apodo = apodo,
            studentId = studentId
        )
    }

    fun Student.toEntity(): StudentEntity {
        val fullName = if (lastName.isNotBlank()) "$name $lastName" else name
        val effectiveStudentId = studentId.ifEmpty { id.toString() }
        return StudentEntity(
            studentId = effectiveStudentId,
            nombre = fullName,
            apodo = apodo,
            edad = edad,
            grado = grade,
            seccion = classroom,
            colegio = colegio,
            docenteAsignado = docenteAsignado,
            nivelActual = level,
            precision = accuracy.toDouble(),
            estrellas = stars,
            xp = xp,
            streak = streak,
            totalPreguntas = totalQuestions,
            correctas = correctAnswers,
            avatar = avatar,
            equippedTheme = equippedTheme,
            diagnosticoRealizado = diagnosticoRealizado,
            tiempoTotal = tiempoTotal,
            tiempoPromedio = tiempoPromedio,
            padreEmail = padreEmail,
            weekData = weekData,
            monthData = monthData,
            skills = skills,
            incorrectasPorTema = incorrectasPorTema,
            asistencia = asistencia,
            recomendaciones = recomendaciones,
            unlockedAvatars = unlockedAvatars,
            unlockedThemes = unlockedThemes,
            lastSyncTimestamp = System.currentTimeMillis()
        )
    }
}
