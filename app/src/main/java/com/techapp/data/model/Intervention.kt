package com.techapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interventions")
data class Intervention(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val clientId: Long = 0,
    val clientName: String,
    val date: String,           // formato: yyyy-MM-dd
    val description: String,
    val status: String = STATUS_OPEN,   // open | in_progress | closed
    val technicalNotes: String = "",
    val department: String = "Generale",
    val assignedUserId: Long = 0,
    val assignedUserName: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_OPEN = "open"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_CLOSED = "closed"
    }
}
