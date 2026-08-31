package com.example.mathia.util

import java.security.SecureRandom

object ClassroomCodeGenerator {

    // 6-character uppercase alphanumeric set excluding ambiguous characters (0, O, 1, I, L)
    private const val ALLOWED_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    private const val CODE_LENGTH = 6

    fun generateCode(): String {
        val random = SecureRandom()
        val sb = StringBuilder(CODE_LENGTH)
        for (i in 0 until CODE_LENGTH) {
            val randomIndex = random.nextInt(ALLOWED_CHARS.length)
            sb.append(ALLOWED_CHARS[randomIndex])
        }
        return sb.toString()
    }

    fun isValidCodeFormat(code: String): Boolean {
        val trimmed = code.trim().uppercase()
        if (trimmed.length != CODE_LENGTH) return false
        return trimmed.all { it in ALLOWED_CHARS }
    }
}
