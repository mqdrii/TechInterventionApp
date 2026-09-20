package com.techapp.utils

import org.mindrot.jbcrypt.BCrypt

object HashUtils {

    /**
     * Genera un hash BCrypt della password.
     */
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(12))
    }

    /**
     * Verifica se la password in chiaro corrisponde all'hash memorizzato.
     */
    fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
        return try {
            BCrypt.checkpw(plainPassword, hashedPassword)
        } catch (e: Exception) {
            false
        }
    }
}
