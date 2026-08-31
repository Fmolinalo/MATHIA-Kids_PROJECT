package com.mathia.kids.data.model

import com.google.gson.annotations.SerializedName

data class HintRequest(
    @SerializedName("edad") val edad: Int = 7,
    @SerializedName("idioma") val idioma: String = "es",
    @SerializedName("tema") val tema: String,
    @SerializedName("enunciado") val enunciado: String,
    @SerializedName("respuestaCorrecta") val respuestaCorrecta: String,
    @SerializedName("listaDeIntentos") val listaDeIntentos: List<String>,
    @SerializedName("intentosFallidos") val intentosFallidos: Int,
    @SerializedName("dificultad") val dificultad: String = "media",
    @SerializedName("studentId") val studentId: String? = null
)

data class HintResponse(
    @SerializedName("pista") val pista: String,
    @SerializedName("tono_animo") val tonoAnimo: String,
    @SerializedName("nivel_pista") val nivelPista: String, // "leve" | "moderada" | "directa"
    @SerializedName("isFallback") val isFallback: Boolean = false
)

data class Competencia(
    @SerializedName("tema") val tema: String,
    @SerializedName("precision") val precision: Int
)

data class ProgressSummaryRequest(
    @SerializedName("idioma") val idioma: String = "es",
    @SerializedName("tipoDestinatario") val tipoDestinatario: String, // "padre" o "docente"
    @SerializedName("nombreEstudiante") val nombreEstudiante: String,
    @SerializedName("edad") val edad: Int = 7,
    @SerializedName("fechaInicio") val fechaInicio: String,
    @SerializedName("fechaFin") val fechaFin: String,
    @SerializedName("ejerciciosCompletados") val ejerciciosCompletados: Int,
    @SerializedName("correctas") val correctas: Int,
    @SerializedName("incorrectas") val incorrectas: Int,
    @SerializedName("precisionPorcentaje") val precisionPorcentaje: Int,
    @SerializedName("rachaDias") val rachaDias: Int,
    @SerializedName("nivelActual") val nivelActual: Int,
    @SerializedName("xpActual") val xpActual: Int,
    @SerializedName("xpSiguienteNivel") val xpSiguienteNivel: Int,
    @SerializedName("listaCompetenciasConPrecision") val listaCompetenciasConPrecision: List<Competencia>,
    @SerializedName("tiempoTotal") val tiempoTotal: String,
    @SerializedName("tiempoPromedio") val tiempoPromedio: String,
    @SerializedName("studentId") val studentId: String? = null,
    @SerializedName("parentId") val parentId: String? = null,
    @SerializedName("teacherId") val teacherId: String? = null
)

data class ProgressSummaryResponse(
    @SerializedName("resumen") val resumen: String,
    @SerializedName("area_fortaleza") val areaFortaleza: String,
    @SerializedName("area_oportunidad") val areaOportunidad: String,
    @SerializedName("recomendacion_accion") val recomendacionAccion: String,
    @SerializedName("confianza_datos") val confianzaDatos: String // "alta" | "media" | "baja"
)
