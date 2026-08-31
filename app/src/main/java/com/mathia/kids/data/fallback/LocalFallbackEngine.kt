package com.mathia.kids.data.fallback

import com.mathia.kids.data.model.HintRequest
import com.mathia.kids.data.model.HintResponse

object LocalFallbackEngine {
    fun generateHint(req: HintRequest): HintResponse {
        val attempts = req.intentosFallidos
        val tema = req.tema.lowercase()
        val nivel = if (attempts >= 3) "directa" else if (attempts == 2) "moderada" else "leve"

        val pista = when {
            "resta" in tema || "reagrupaci" in tema -> {
                if (attempts >= 3) "Descompón primero las decenas y luego resta las unidades paso a paso."
                else "Recuerda: si el número de arriba es menor, pide 10 prestado a la decena vecina."
            }
            "suma" in tema || "llevad" in tema -> {
                if (attempts >= 3) "Suma primero las unidades. Si pasa de 9, lleva 1 a las decenas."
                else "Revisa bien si al sumar las unidades debes llevar una decena."
            }
            "multiplicaci" in tema || "tabla" in tema -> {
                if (attempts >= 3) "Puedes sumar el número varias veces seguidas para encontrar la respuesta."
                else "Recuerda que multiplicar es sumar un mismo número varias veces."
            }
            "fraccion" in tema -> {
                if (attempts >= 3) "El número de arriba indica cuántas partes tomamos y el de abajo el total de partes."
                else "Observa la figura o las partes en las que dividiste el total."
            }
            "serie" in tema -> {
                if (attempts >= 3) "Calcula la diferencia entre los primeros dos números para saber de cuánto en cuánto avanza."
                else "Fíjate en cuánto aumenta o disminuye cada número consecutivo."
            }
            else -> "Observa detenidamente los números y resuelve paso a paso de derecha a izquierda."
        }

        return HintResponse(
            pista = pista,
            tonoAnimo = "¡Tú puedes, inténtalo con calma!",
            nivelPista = nivel,
            isFallback = true
        )
    }
}
