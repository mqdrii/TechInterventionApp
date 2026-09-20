package com.techapp.utils

import android.graphics.Color
import kotlin.math.abs

object AvatarHelper {

    private val PALETTE = listOf(
        "#1E40AF", // Royal Blue
        "#047857", // Deep Emerald
        "#4338CA", // Indigo
        "#B45309", // Warm Amber
        "#0F766E", // Teal
        "#6D28D9", // Purple
        "#BE123C", // Crimson Rose
        "#334155"  // Slate
    )

    fun getInitials(name: String?): String {
        if (name.isNullOrBlank()) return "CL"
        val words = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        return when {
            words.isEmpty() -> "CL"
            words.size == 1 -> words[0].take(2).uppercase()
            else -> ("" + words.first().first() + words.last().first()).uppercase()
        }
    }

    fun getColorForName(name: String?): Int {
        if (name.isNullOrBlank()) return Color.parseColor(PALETTE[0])
        val index = abs(name.hashCode()) % PALETTE.size
        return Color.parseColor(PALETTE[index])
    }
}
