package cl.esucar.tutorvirtual.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateUtils {
    private val display: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)
    private val long: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CL"))

    fun parseIso(s: String): LocalDate? = runCatching { LocalDate.parse(s) }.getOrNull()
    fun parseDisplay(s: String): LocalDate? = runCatching { LocalDate.parse(s.trim(), display) }.getOrNull()
    fun formatDisplay(d: LocalDate): String = d.format(display)
    fun formatLong(iso: String): String = parseIso(iso)?.format(long) ?: iso

    fun daysUntil(iso: String, today: LocalDate = LocalDate.now()): Long? =
        parseIso(iso)?.let { ChronoUnit.DAYS.between(today, it) }

    fun dueText(daysLeft: Long): String = when {
        daysLeft < -1 -> "Vencido hace ${-daysLeft} días"
        daysLeft == -1L -> "Venció ayer"
        daysLeft == 0L -> "Vence hoy"
        daysLeft == 1L -> "Vence mañana"
        else -> "Vence en $daysLeft días"
    }
}
