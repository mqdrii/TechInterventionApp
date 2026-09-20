package com.techapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "interventions",
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
data class Intervention(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val clientId: Long,
    val clientName: String,
    val date: String,           // formato: yyyy-MM-dd
    val description: String,
    val status: String = STATUS_OPEN,   // open | in_progress | closed
    val technicalNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_OPEN = "open"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_CLOSED = "closed"
    }
}
