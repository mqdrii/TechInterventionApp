package com.techapp.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestisce la sessione dell'utente loggato tramite SharedPreferences.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "techapp_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_DEPARTMENT = "user_department"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val NO_USER = -1L
    }

    fun saveSession(
        userId: Long,
        fullName: String,
        email: String,
        role: String = "technician",
        department: String = "Generale"
    ) {
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, fullName)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_ROLE, role)
            .putString(KEY_USER_DEPARTMENT, department)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, NO_USER)

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""

    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""

    fun getUserRole(): String = prefs.getString(KEY_USER_ROLE, "technician") ?: "technician"

    fun getUserDepartment(): String = prefs.getString(KEY_USER_DEPARTMENT, "Generale") ?: "Generale"

    fun isAdmin(): Boolean = getUserRole() == "admin"

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
