package com.example.mathia

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mathia.model.RespuestaHistorial

// ─── Adaptive Difficulty Data Models & Engine ─────────────────────────────
data class AdaptiveDifficultyResult(
    val nivelCalculado: Int,               // 1..5
    val cambio: Int,                       // +1 (Subió), 0 (Mantiene), -1 (Bajó)
    val razon: String,                      // Explicación pedagógica para estudiante
    val explicacionPadre: String,           // Explicación amigable para padres
    val explicacionDocente: String,         // Análisis técnico para docentes
    val requierePistas: Boolean             // True si nivel <= 2 o precisión baja
)

private data class Tuple6<A, B, C, D, E, F>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F
)

object AdaptiveEngine {

    fun normalizarTema(tema: String): String {
        val t = tema.trim()
        return when {
            t.startsWith("Suma", ignoreCase = true) -> "Suma"
            t.startsWith("Resta", ignoreCase = true) -> "Resta"
            t.startsWith("Multiplic", ignoreCase = true) -> "Multiplicacion"
            t.startsWith("Divisi", ignoreCase = true) -> "Division"
            t.startsWith("Fraccion", ignoreCase = true) -> "Fracciones"
            t.startsWith("Serie", ignoreCase = true) -> "Series"
            else -> t
        }
    }

    fun obtenerNombreNivel(nivel: Int): String {
        return when (nivel.coerceIn(1, 5)) {
            1 -> "Nivel 1 - Básico"
            2 -> "Nivel 2 - Inicial"
            3 -> "Nivel 3 - Intermedio"
            4 -> "Nivel 4 - Avanzado"
            5 -> "Nivel 5 - Desafío"
            else -> "Nivel $nivel"
        }
    }

    /**
     * Motor de Cálculo de Dificultad Adaptativa Real.
     * Evalúa las respuestas recientes del estudiante específicamente para el tema solicitado.
     */
    fun calcularDificultadAdaptativa(
        tema: String,
        nivelActualTema: Int,
        historialTema: List<RespuestaHistorial>,
        precisionTema: Double = -1.0
    ): AdaptiveDifficultyResult {
        val temaNorm = normalizarTema(tema)
        val nivelActual = nivelActualTema.coerceIn(1, 5)

        // Filtrar respuestas recientes de este tema (máximo las últimas 5)
        val respuestasRecientes = historialTema
            .filter { normalizarTema(it.operacion).equals(temaNorm, ignoreCase = true) }
            .sortedByDescending { it.timestamp }
            .take(5)

        // FALLBACK: Si no hay suficiente historial (menos de 3 respuestas recientes)
        if (respuestasRecientes.size < 3) {
            return AdaptiveDifficultyResult(
                nivelCalculado = nivelActual,
                cambio = 0,
                razon = "MathIA está conociendo tu nivel. ¡Sigue practicando!",
                explicacionPadre = "MathIA está evaluando los primeros ejercicios de $temaNorm para determinar su nivel ideal.",
                explicacionDocente = "Nivel inicial $nivelActual para $temaNorm (Historial insuficiente: ${respuestasRecientes.size}/3 ejercicios mínimos).",
                requierePistas = nivelActual <= 1
            )
        }

        // Métricas Múltiples
        val total = respuestasRecientes.size
        val correctas = respuestasRecientes.count { it.correcta }
        val porcentajeAcierto = (correctas.toDouble() / total) * 100.0
        val promedioIntentos = respuestasRecientes.map { it.intentos }.average()
        val promedioTiempoSeg = respuestasRecientes.map { it.tiempoRespuesta }.average()
        val erroresConsecutivos = respuestasRecientes.takeWhile { !it.correcta }.size

        // Evaluación Multimétrica & Evitar Cambios Bruscos (máx +1 / -1)
        val (nuevoNivel, cambio, razon, expPadre, expDocente, pistas) = when {
            // REGLA 1: SUBIR DIFICULTAD (90%+ acierto, intentos <= 1.5, tiempo <= 20s)
            porcentajeAcierto >= 90.0 && promedioIntentos <= 1.5 && promedioTiempoSeg <= 20.0 -> {
                val targetNivel = (nivelActual + 1).coerceAtMost(5)
                val cambioEfectivo = if (targetNivel > nivelActual) 1 else 0
                val msg = if (cambioEfectivo > 0) "¡Muy bien! Estás listo para un nuevo desafío." else "¡Excelente dominio! Te mantienes en el nivel máximo de desafío."
                val padre = "MathIA detectó alto dominio en $temaNorm (${porcentajeAcierto.toInt()}%) y adaptó los ejercicios a un nivel mayor (Nivel $targetNivel)."
                val doc = "Dominio consolidado en $temaNorm (Precisión: ${porcentajeAcierto.toInt()}%, Intentos prom: ${String.format("%.1f", promedioIntentos)}, Tiempo: ${promedioTiempoSeg.toInt()}s). Nivel incrementado a $targetNivel."
                Tuple6(targetNivel, cambioEfectivo, msg, padre, doc, false)
            }

            // REGLA 2: MANTENER O AUMENTAR LIGERAMENTE (80–89%)
            porcentajeAcierto in 80.0..89.9 -> {
                if (promedioIntentos == 1.0 && promedioTiempoSeg <= 12.0 && nivelActual < 5) {
                    val targetNivel = nivelActual + 1
                    val msg = "¡Gran fluidez! Probemos con el Nivel $targetNivel."
                    val padre = "MathIA está aumentando gradualmente la exigencia en $temaNorm debido a su alta velocidad y precisión."
                    val doc = "Alta velocidad y precisión en $temaNorm (${porcentajeAcierto.toInt()}%). Nivel ajustado a Nivel $targetNivel."
                    Tuple6(targetNivel, 1, msg, padre, doc, false)
                } else {
                    val msg = "¡Excelente progreso! Sigamos consolidando este nivel."
                    val padre = "MathIA está reforzando $temaNorm en un nivel óptimo de consolidación."
                    val doc = "Rendimiento estable en $temaNorm (Precisión: ${porcentajeAcierto.toInt()}%). Nivel conservado en Nivel $nivelActual."
                    Tuple6(nivelActual, 0, msg, padre, doc, false)
                }
            }

            // REGLA 3: MANTENER DIFICULTAD (65–79%)
            porcentajeAcierto in 65.0..79.9 -> {
                val msg = "Sigamos practicando en este nivel para dominarlo por completo."
                val padre = "MathIA acompaña a su hijo con ejercicios adecuados a su ritmo de aprendizaje en $temaNorm."
                val doc = "Desempeño intermedio en $temaNorm (${porcentajeAcierto.toInt()}%). Nivel mantenido en Nivel $nivelActual."
                Tuple6(nivelActual, 0, msg, padre, doc, false)
            }

            // REGLA 4: DISMINUIR LIGERAMENTE (50–64%)
            porcentajeAcierto in 50.0..64.9 || erroresConsecutivos >= 2 -> {
                val targetNivel = (nivelActual - 1).coerceAtLeast(1)
                val cambioEfectivo = if (targetNivel < nivelActual) -1 else 0
                val msg = "Vamos paso a paso. Reforcemos primero estos conceptos."
                val padre = "MathIA adaptó suavemente los ejercicios de $temaNorm para afianzar la confianza de su hijo."
                val doc = "Dificultad detectada en $temaNorm (Precisión: ${porcentajeAcierto.toInt()}%, Errores consec: $erroresConsecutivos). Nivel ajustado a Nivel $targetNivel con refuerzo."
                Tuple6(targetNivel, cambioEfectivo, msg, padre, doc, true)
            }

            // REGLA 5: BAJAR Y PROPORCIONAR APOYO (<50%)
            else -> {
                val targetNivel = (nivelActual - 1).coerceAtLeast(1)
                val cambioEfectivo = if (targetNivel < nivelActual) -1 else 0
                val msg = "Volvamos a lo básico y aprendamos juntos. ¡Tú puedes!"
                val padre = "MathIA está acompañando a su hijo con apoyo guiado y pistas paso a paso en $temaNorm."
                val doc = "Atención requerida en $temaNorm (Precisión: ${porcentajeAcierto.toInt()}%). Reducción progresiva a Nivel $targetNivel con pistas pedagógicas activas."
                Tuple6(targetNivel, cambioEfectivo, msg, padre, doc, true)
            }
        }

        return AdaptiveDifficultyResult(
            nivelCalculado = nuevoNivel,
            cambio = cambio,
            razon = razon,
            explicacionPadre = expPadre,
            explicacionDocente = expDocente,
            requierePistas = pistas
        )
    }
}

fun calcularRiesgo(
    precision: Int
): String {

    return when {

        precision < 40 ->
            "ALTO"

        precision < 70 ->
            "MEDIO"

        else ->
            "BAJO"
    }
}

fun predecirNivel(
    precision: Int,
    tiempoPromedio: Double
): String {

    return when {

        precision >= 85 &&
                tiempoPromedio <= 15 ->

            "Probable Avanzado"

        precision >= 60 ->

            "Probable Intermedio"

        else ->

            "Necesita Refuerzo"
    }
}

fun generarAlertas(
    precision: Int,
    tiempoPromedio: Double
): List<String> {

    val alertas = mutableListOf<String>()

    if (precision < 50)
        alertas.add(
            "Precisión menor al 50%"
        )

    if (tiempoPromedio > 20)
        alertas.add(
            "Tiempo de respuesta elevado"
        )

    if (alertas.isEmpty())
        alertas.add(
            "Sin alertas"
        )

    return alertas
}

@Composable
fun AlertCard(
    alerts: List<String>
) {

    Card {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                "Alertas",
                fontWeight = FontWeight.Bold
            )

            Spacer(
                Modifier.height(8.dp)
            )

            alerts.forEach {

                Text("• $it")
            }
        }
    }
}

@Composable
fun PredictionCard(
    prediction: String
) {

    Card {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                "Predicción",
                fontWeight = FontWeight.Bold
            )

            Spacer(
                Modifier.height(8.dp)
            )

            Text(prediction)
        }
    }
}

@Composable
fun WeeklyProgressChart(
    weekData: List<Int>
) {

    Card {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                "Evolución Semanal",
                fontWeight = FontWeight.Bold
            )

            Spacer(
                Modifier.height(8.dp)
            )

            weekData.forEachIndexed { index, value ->

                Text("Día ${index + 1}")

                LinearProgressIndicator(
                    progress = { value / 100f },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("$value%")
            }
        }
    }
}