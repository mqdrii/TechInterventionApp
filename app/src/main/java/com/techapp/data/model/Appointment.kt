package com.techapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val clientId: Long = 0,
    val clientName: String,
    val date: String,        // formato: yyyy-MM-dd
    val time: String,        // formato: HH:mm
    val description: String,
    val status: String = STATUS_SCHEDULED,  // scheduled | completed | cancelled
    val department: String = "Generale",
    val assignedUserId: Long = 0,
    val assignedUserName: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_SCHEDULED = "scheduled"
        const val STATUS_COMPLETED = "completed"
        const val STATUS_CANCELLED = "cancelled"
    }
}
