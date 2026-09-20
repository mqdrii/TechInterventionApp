package com.techapp.data.repository

import com.techapp.data.db.UserDao
import com.techapp.data.model.User
import com.techapp.utils.HashUtils

class UserRepository(private val userDao: UserDao) {

    /**
     * Registra un nuovo utente. Restituisce l'ID inserito, o -1 se l'email esiste già.
     */
    suspend fun registerUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Long {
        if (userDao.emailExists(email)) return -1L
        val hash = HashUtils.hashPassword(password)
        val user = User(
            firstName = firstName,
            lastName = lastName,
            email = email,
            passwordHash = hash
        )
        return userDao.insert(user)
    }

    /**
     * Autentica un utente. Restituisce l'oggetto User se le credenziali sono valide, null altrimenti.
     */
    suspend fun loginUser(email: String, password: String): User? {
        val user = userDao.getUserByEmail(email) ?: return null
        return if (HashUtils.verifyPassword(password, user.passwordHash)) user else null
    }

    suspend fun getUserById(id: Long): User? = userDao.getUserById(id)

    suspend fun updateUser(user: User) = userDao.update(user)
}
