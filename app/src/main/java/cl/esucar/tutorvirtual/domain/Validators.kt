package cl.esucar.tutorvirtual.domain

import java.time.LocalDate

/**
 * Validaciones centralizadas y desacopladas de la UI (sin dependencias de Android).
 * Cada función devuelve el mensaje de error o null si el valor es válido.
 */
object Validators {
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun name(value: String): String? {
        val t = value.trim()
        return when {
            t.isEmpty() -> "Ingresa tu nombre."
            t.length < 3 -> "El nombre debe tener al menos 3 caracteres."
            t.length > 60 -> "El nombre no puede superar los 60 caracteres."
            !t.all { it.isLetter() || it == ' ' || it == '\'' || it == '-' || it == '.' } ->
                "El nombre solo puede contener letras y espacios."
            else -> null
        }
    }

    fun email(value: String): String? {
        val t = value.trim()
        return when {
            t.isEmpty() -> "Ingresa tu correo."
            !EMAIL.matches(t) -> "Correo inválido. Ejemplo: nombre@correo.cl"
            else -> null
        }
    }

    fun password(value: String): String? = when {
        value.isEmpty() -> "Ingresa una contraseña."
        value.length < 8 -> "Debe tener al menos 8 caracteres."
        value.none { it.isLetter() } -> "Debe incluir al menos una letra."
        value.none { it.isDigit() } -> "Debe incluir al menos un número."
        else -> null
    }

    fun loginPassword(value: String): String? =
        if (value.isEmpty()) "Ingresa tu contraseña." else null

    fun confirmPassword(password: String, confirm: String): String? = when {
        confirm.isEmpty() -> "Repite tu contraseña."
        confirm != password -> "Las contraseñas no coinciden."
        else -> null
    }

    fun notes(notes: String, status: DeliverableStatus): String? = when {
        notes.length > 500 -> "Máximo 500 caracteres."
        status.isDone && notes.trim().length < 10 ->
            "Describe brevemente lo entregado (mínimo 10 caracteres)."
        else -> null
    }

    fun sentDate(text: String, status: DeliverableStatus, today: LocalDate = LocalDate.now()): String? {
        val t = text.trim()
        if (t.isEmpty()) return if (status.isDone) "Indica la fecha de entrega." else null
        val date = DateUtils.parseDisplay(t) ?: return "Formato inválido. Usa dd/MM/aaaa (ej. 15/11/2026)."
        return if (date.isAfter(today)) "La fecha no puede ser futura." else null
    }

    /** Días de anticipación separados por coma, ej. "3,1". Máx. 3 valores entre 0 y 30, sin repetir. */
    fun reminderDays(text: String): String? {
        val parts = text.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty()) return "Ingresa al menos un día (ej. 3,1)."
        val nums = parts.map { it.toIntOrNull() ?: return "Usa solo números separados por coma (ej. 3,1)." }
        return when {
            nums.any { it !in 0..30 } -> "Cada valor debe estar entre 0 y 30."
            nums.size > 3 -> "Máximo 3 avisos por hito."
            nums.toSet().size != nums.size -> "No repitas días."
            else -> null
        }
    }

    fun parseReminderDays(text: String): List<Int> =
        text.split(",").mapNotNull { it.trim().toIntOrNull() }.distinct().sortedDescending()

    // ------------------------------------------------------------------ Perfil profesor guía

    fun teacherCode(value: String): String? = when {
        value.isBlank() -> "Ingresa el código docente."
        !value.trim().equals(UserRole.TEACHER_ACCESS_CODE, ignoreCase = true) -> "Código docente incorrecto."
        else -> null
    }

    private fun lengthRule(value: String, what: String, min: Int, max: Int): String? {
        val t = value.trim()
        return when {
            t.isEmpty() -> "Ingresa $what."
            t.length < min -> "Debe tener al menos $min caracteres."
            t.length > max -> "No puede superar los $max caracteres."
            else -> null
        }
    }

    fun stageTitle(v: String): String? = lengthRule(v, "el título de la etapa", 5, 80)
    fun stageDescription(v: String): String? = lengthRule(v, "la descripción", 20, 400)
    fun stageFormat(v: String): String? = lengthRule(v, "el formato requerido", 10, 300)

    /** Fecha límite en formato dd/MM/aaaa (la etapa la guarda en ISO). */
    fun deadline(text: String): String? {
        val t = text.trim()
        if (t.isEmpty()) return "Indica la fecha límite."
        val date = DateUtils.parseDisplay(t) ?: return "Formato inválido. Usa dd/MM/aaaa (ej. 15/11/2026)."
        return if (date.year < 2024 || date.year > 2100) "Ingresa una fecha entre 2024 y 2100." else null
    }

    /** Pauta: un criterio por línea (se ignoran viñetas y líneas vacías). */
    fun parseRubric(text: String): List<String> =
        text.lines().map { it.trim().trimStart('•', '-', '*').trim() }.filter { it.isNotEmpty() }

    fun rubric(text: String): String? {
        val items = parseRubric(text)
        return when {
            items.isEmpty() -> "Agrega al menos un criterio (uno por línea)."
            items.size > 10 -> "Máximo 10 criterios."
            items.any { it.length > 200 } -> "Cada criterio admite hasta 200 caracteres."
            else -> null
        }
    }

    fun stepTitle(v: String): String? = lengthRule(v, "el título del paso", 3, 80)
    fun stepContent(v: String): String? = lengthRule(v, "el contenido del paso", 10, 600)

    fun faqCategory(v: String): String? = lengthRule(v, "la categoría", 3, 30)
    fun faqQuestion(v: String): String? = lengthRule(v, "la pregunta", 8, 150)
    fun faqAnswer(v: String): String? = lengthRule(v, "la respuesta", 10, 800)

    /** Observaciones del profesor: opcionales al aprobar, obligatorias (mín. 10) al devolver el hito. */
    fun feedback(value: String, returning: Boolean): String? {
        val t = value.trim()
        return when {
            value.length > 500 -> "Máximo 500 caracteres."
            returning && t.length < 10 -> "Explica qué debe corregir el estudiante (mínimo 10 caracteres)."
            else -> null
        }
    }
}
