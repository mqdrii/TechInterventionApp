package com.techapp.utils

import com.techapp.R

object DepartmentHelper {

    val SUGGESTED_DEPARTMENTS = listOf(
        "Specialista Reti, Fibra & Wi-Fi",
        "Sistemi IT, Computer & Server",
        "Telefonia, Smartphone & VoIP",
        "Installazione Monitor & LIM",
        "Impianti Elettrici & Cablaggio",
        "Assistenza Hardware & Periferiche",
        "Sistemi di Sicurezza & Videosorveglianza",
        "Manutenzione Impianti Generale"
    )

    fun getIconRes(dept: String?): Int {
        if (dept.isNullOrBlank()) return R.drawable.ic_dept_tools
        val lower = dept.lowercase()
        return when {
            lower.contains("ret") || lower.contains("fibr") || lower.contains("wifi") || 
            lower.contains("router") || lower.contains("switch") || lower.contains("lan") -> 
                R.drawable.ic_dept_network

            lower.contains("comp") || lower.contains("pc") || lower.contains("it") || 
            lower.contains("hardw") || lower.contains("softw") || lower.contains("inform") ||
            lower.contains("server") -> 
                R.drawable.ic_dept_computer

            lower.contains("tel") || lower.contains("voip") || lower.contains("smart") || 
            lower.contains("cell") || lower.contains("centralin") -> 
                R.drawable.ic_dept_phone

            lower.contains("lavagn") || lower.contains("lim") || lower.contains("monitor") || 
            lower.contains("display") || lower.contains("proiet") || lower.contains("scherm") -> 
                R.drawable.ic_dept_display

            lower.contains("elettr") || lower.contains("quadr") || lower.contains("cabl") || 
            lower.contains("luce") || lower.contains("corrent") || lower.contains("energi") -> 
                R.drawable.ic_dept_electrical

            else -> R.drawable.ic_dept_tools
        }
    }

    // Deprecated string helper maintained for backward-compatibility without emojis
    fun getIcon(dept: String?): String = ""
}
