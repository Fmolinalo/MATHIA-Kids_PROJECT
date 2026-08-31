package com.example.mathia.util

sealed class NicknameValidationResult {
    object Valid : NicknameValidationResult()
    data class Invalid(val reason: String) : NicknameValidationResult()
}

object NicknameValidator {

    private val FORBIDDEN_WORDS = listOf(
        "admin", "root", "tonto", "bobos", "estupido", "mierda", "puto", "puta",
        "carajo", "idiota", "hacker", "sex", "porn", "fake"
    )

    private val EMAIL_REGEX = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    private val PHONE_REGEX = Regex("\\d{7,}")

    fun validateNickname(apodo: String): NicknameValidationResult {
        val trimmed = apodo.trim()

        if (trimmed.length < 3) {
            return NicknameValidationResult.Invalid("El apodo debe tener al menos 3 caracteres.")
        }

        if (trimmed.length > 15) {
            return NicknameValidationResult.Invalid("El apodo no puede exceder 15 caracteres.")
        }

        if (EMAIL_REGEX.containsMatchIn(trimmed)) {
            return NicknameValidationResult.Invalid("Por seguridad, el apodo no puede contener un correo electrónico.")
        }

        if (PHONE_REGEX.containsMatchIn(trimmed)) {
            return NicknameValidationResult.Invalid("Por seguridad, el apodo no puede contener un número de teléfono.")
        }

        val lower = trimmed.lowercase()
        for (word in FORBIDDEN_WORDS) {
            if (lower.contains(word)) {
                return NicknameValidationResult.Invalid("El apodo contiene una palabra no permitida.")
            }
        }

        return NicknameValidationResult.Valid
    }
}
