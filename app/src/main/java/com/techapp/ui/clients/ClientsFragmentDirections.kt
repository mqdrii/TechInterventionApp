package com.techapp.ui.clients

import androidx.navigation.NavDirections
import com.techapp.R

/**
 * Directions generate automaticamente dal Navigation Component (Safe Args).
 * In un progetto Android Studio reale, questa classe viene auto-generata dal plugin
 * androidx.navigation.safeargs.kotlin. Questo file è incluso manualmente
 * come riferimento finché il progetto non viene costruito con Gradle.
 */
object ClientsFragmentDirections {

    fun actionClientsToClientDetail(clientId: Long): NavDirections {
        return object : NavDirections {
            override val actionId: Int = R.id.action_clients_to_client_detail
            override val arguments: android.os.Bundle = android.os.Bundle().apply {
                putLong("clientId", clientId)
            }
        }
    }
}
