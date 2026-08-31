package com.example.mathia.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "t_estudiante")
data class StudentEntity(
    @PrimaryKey val studentId: String = "1",
    val nombre: String = "",
    val apodo: String = "",
    val edad: Int = 6,
    val grado: String = "1",
    val seccion: String = "",
    val colegio: String = "",
    val docenteAsignado: String = "",
    val nivelActual: Int = 1,
    val precision: Double = 0.0,
    val estrellas: Int = 0,
    val xp: Int = 0,
    val streak: Int = 0,
    val totalPreguntas: Int = 0,
    val correctas: Int = 0,
    val avatar: String = "👶",
    val equippedTheme: String = "Lila Clásico",
    val diagnosticoRealizado: Boolean = false,
    val tiempoTotal: Long = 0L,
    val tiempoPromedio: Double = 0.0,
    val padreEmail: String = "",
    val weekData: List<Int> = listOf(0, 0, 0, 0, 0, 0, 0),
    val monthData: List<Int> = listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    val skills: Map<String, Int> = emptyMap(),
    val incorrectasPorTema: Map<String, Int> = emptyMap(),
    val asistencia: List<String> = emptyList(),
    val recomendaciones: List<String> = emptyList(),
    val unlockedAvatars: List<String> = listOf("👶"),
    val unlockedThemes: List<String> = listOf("Lila Clásico"),
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)