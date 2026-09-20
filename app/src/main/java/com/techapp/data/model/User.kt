package com.techapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val email: String,
    val passwordHash: String,
    val role: String = ROLE_TECHNICIAN,
    val department: String = DEPARTMENT_GENERAL,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val ROLE_ADMIN = "admin"
        const val ROLE_TECHNICIAN = "technician"

        const val DEPARTMENT_COMPUTERS = "Computer"
        const val DEPARTMENT_PHONES = "Telefoni"
        const val DEPARTMENT_BOARDS = "Montaggio Lavagne"
        const val DEPARTMENT_GENERAL = "Generale"
        const val DEPARTMENT_ALL = "Tutti"

        val DEPARTMENTS = listOf(
            DEPARTMENT_COMPUTERS,
            DEPARTMENT_PHONES,
            DEPARTMENT_BOARDS,
            DEPARTMENT_GENERAL
        )
    }

    val fullName: String get() = "$firstName $lastName".trim()
    val isAdmin: Boolean get() = role == ROLE_ADMIN
}
