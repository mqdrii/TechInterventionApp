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
    val role: String = "technician",
    val createdAt: Long = System.currentTimeMillis()
)
