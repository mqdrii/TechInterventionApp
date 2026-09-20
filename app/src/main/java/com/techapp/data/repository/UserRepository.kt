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
        password: String,
        role: String = User.ROLE_TECHNICIAN,
        department: String = User.DEPARTMENT_GENERAL
    ): Long {
        if (userDao.emailExists(email)) return -1L
        val hash = HashUtils.hashPassword(password)
        val user = User(
            firstName = firstName,
            lastName = lastName,
            email = email,
            passwordHash = hash,
            role = role,
            department = department
        )
        return userDao.insert(user)
    }

    fun getTechnicians() = userDao.getTechnicians()

    suspend fun getTechniciansList() = userDao.getTechniciansList()

    /**
     * Autentica un utente. Restituisce l'oggetto User se le credenziali sono valide, null altrimenti.
     */
    suspend fun loginUser(email: String, password: String): User? {
        val user = userDao.getUserByEmail(email) ?: return null
        return if (HashUtils.verifyPassword(password, user.passwordHash)) user else null
    }

    suspend fun getUserById(id: Long): User? = userDao.getUserById(id)

    suspend fun updateUser(user: User) = userDao.update(user)

    /** Salva (o aggiorna) un utente ricevuto dal server nel DB locale. */
    suspend fun upsertUserFromServer(
        id: Long, email: String, firstName: String,
        lastName: String, role: String, department: String
    ) {
        userDao.upsert(
            User(
                id = id,
                email = email,
                firstName = firstName,
                lastName = lastName,
                passwordHash = "",   // non gestiamo password lato client
                role = role,
                department = department
            )
        )
    }
}
