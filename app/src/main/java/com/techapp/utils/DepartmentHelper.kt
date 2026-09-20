package com.techapp.utils

object DepartmentHelper {

    val SUGGESTED_DEPARTMENTS = listOf(
        "Tecnico PC & Sistemi IT",
        "Telefonia & Centralini VoIP",
        "Montaggio Lavagne LIM & Monitor",
        "Elettricista & Impianti",
        "Reti Dati, Fibra & Wi-Fi",
        "Sistemi di Sicurezza & Allarmi",
        "Audio, Video & Conferenze",
        "Climatizzazione & Pompe di Calore",
        "Assistenza Hardware & Periferiche",
        "Manutenzione Generale"
    )

    fun getIcon(dept: String?): String {
        if (dept.isNullOrBlank()) return "💼"
        val lower = dept.lowercase()
        return when {
            lower.contains("comp") || lower.contains("pc") || lower.contains("it") || 
            lower.contains("hardw") || lower.contains("softw") || lower.contains("inform") -> "💻"

            lower.contains("tel") || lower.contains("voip") || lower.contains("smart") || 
            lower.contains("cell") || lower.contains("centralin") -> "📱"

            lower.contains("lavagn") || lower.contains("lim") || lower.contains("montag") || 
            lower.contains("fissag") || lower.contains("arred") || lower.contains("staff") -> "🛠️"

            lower.contains("elettr") || lower.contains("quadr") || lower.contains("cabl") || 
            lower.contains("luce") || lower.contains("corrent") -> "⚡"

            lower.contains("ret") || lower.contains("fibr") || lower.contains("wifi") || 
            lower.contains("router") || lower.contains("switch") || lower.contains("lan") -> "🌐"

            lower.contains("condiz") || lower.contains("clima") || lower.contains("aria") || 
            lower.contains("pompa") || lower.contains("fredd") -> "❄️"

            lower.contains("sicur") || lower.contains("allarm") || lower.contains("telecam") || 
            lower.contains("video") || lower.contains("antifurt") || lower.contains("cctv") -> "📹"

            lower.contains("audio") || lower.contains("cassa") || lower.contains("microf") || 
            lower.contains("proiett") || lower.contains("multimed") -> "🔊"

            lower.contains("idraul") || lower.contains("tub") || lower.contains("calda") || 
            lower.contains("perdit") -> "🔧"

            else -> "💼"
        }
    }
}
