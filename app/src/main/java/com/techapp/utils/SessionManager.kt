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
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_LAST_SYNC = "last_sync"
        const val DEFAULT_SERVER_URL = "https://techinterventionapp.onrender.com"
        const val NO_USER = -1L
    }

    fun saveSession(
        userId: Long,
        fullName: String,
        email: String,
        role: String = "technician",
        department: String = "Generale",
        token: String = ""
    ) {
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, fullName)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_ROLE, role)
            .putString(KEY_USER_DEPARTMENT, department)
            .putString(KEY_AUTH_TOKEN, token)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getServerUrl(): String = prefs.getString(KEY_SERVER_URL, DEFAULT_SERVER_URL) ?: DEFAULT_SERVER_URL

    fun saveServerUrl(url: String) {
        prefs.edit().putString(KEY_SERVER_URL, url.trim()).apply()
        com.techapp.data.api.ApiClient.reset()
    }

    fun getAuthToken(): String? = prefs.getString(KEY_AUTH_TOKEN, null)

    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getLastSync(): String = prefs.getString(KEY_LAST_SYNC, "") ?: ""

    fun saveLastSync(timestamp: String) {
        prefs.edit().putString(KEY_LAST_SYNC, timestamp).apply()
    }

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, NO_USER)

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""

    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""

    fun getUserRole(): String = prefs.getString(KEY_USER_ROLE, "technician") ?: "technician"

    fun getUserDepartment(): String = prefs.getString(KEY_USER_DEPARTMENT, "Generale") ?: "Generale"

    fun isAdmin(): Boolean = getUserRole().equals("admin", ignoreCase = true)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun clearSession() {
        val serverUrl = getServerUrl()
        prefs.edit().clear().putString(KEY_SERVER_URL, serverUrl).apply()
    }
}
