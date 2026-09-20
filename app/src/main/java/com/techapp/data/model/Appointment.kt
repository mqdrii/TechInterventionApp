package com.techapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("clientId"), Index("userId")]
)
data class Appointment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val clientId: Long,
    val clientName: String,
    val date: String,        // formato: yyyy-MM-dd
    val time: String,        // formato: HH:mm
    val description: String,
    val status: String = STATUS_SCHEDULED,  // scheduled | completed | cancelled
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_SCHEDULED = "scheduled"
        const val STATUS_COMPLETED = "completed"
        const val STATUS_CANCELLED = "cancelled"
    }
}
