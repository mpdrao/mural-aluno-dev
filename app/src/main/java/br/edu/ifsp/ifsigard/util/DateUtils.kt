package br.edu.ifsp.ifsigard.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val brFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun todayIso(): String = isoFormat.format(Date())

    fun tomorrowIso(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        return isoFormat.format(cal.time)
    }

    fun daysFromTodayIso(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return isoFormat.format(cal.time)
    }

    fun todayBr(): String = brFormat.format(Date())

    fun tomorrowBr(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        return brFormat.format(cal.time)
    }

    /**
     * Normaliza qualquer entrada de data (ex: "25/11/2026" ou "2026-11-25") para o padrao ISO "yyyy-MM-dd".
     * Retorna a propria string se nao for possivel fazer o parse.
     */
    fun normalizeToIso(dateStr: String): String {
        val trimmed = dateStr.trim()
        if (trimmed.isEmpty()) return ""

        return try {
            if (trimmed.contains("/")) {
                val parsed = brFormat.parse(trimmed)
                if (parsed != null) isoFormat.format(parsed) else trimmed
            } else if (trimmed.contains("-")) {
                val parsed = isoFormat.parse(trimmed)
                if (parsed != null) isoFormat.format(parsed) else trimmed
            } else {
                trimmed
            }
        } catch (e: Exception) {
            trimmed
        }
    }

    /**
     * Formata para exibicao amigavel: "Hoje", "Amanhã" ou "dd/MM/yyyy".
     */
    fun formatForDisplay(dateStr: String): String {
        val iso = normalizeToIso(dateStr)
        val hoje = todayIso()
        val amanha = tomorrowIso()

        return when (iso) {
            hoje -> "Hoje"
            amanha -> "Amanhã"
            else -> {
                try {
                    val parsed = isoFormat.parse(iso)
                    if (parsed != null) brFormat.format(parsed) else dateStr
                } catch (e: Exception) {
                    dateStr
                }
            }
        }
    }

    /**
     * RN04: Verifica se a data e anterior a data de hoje (no passado).
     */
    fun isPastDate(dateStr: String): Boolean {
        val iso = normalizeToIso(dateStr)
        val hoje = todayIso()
        return iso < hoje
    }

    /**
     * RN06: Calcula se o aviso foi publicado com menos de 24h de antecedencia da aula.
     */
    fun isAvisoTardio(dataIso: String, horaInicioStr: String, agoraTimestamp: Long = System.currentTimeMillis()): Boolean {
        return try {
            val iso = normalizeToIso(dataIso)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val aulaDate = sdf.parse("$iso $horaInicioStr") ?: return false
            val diferencaMillis = aulaDate.time - agoraTimestamp
            val vinteQuatroHorasMillis = 24 * 60 * 60 * 1000L
            diferencaMillis < vinteQuatroHorasMillis
        } catch (e: Exception) {
            false
        }
    }
}
