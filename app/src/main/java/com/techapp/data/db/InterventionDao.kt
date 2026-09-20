package com.techapp.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.techapp.data.model.Intervention

@Dao
interface InterventionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(intervention: Intervention): Long

    @Update
    suspend fun update(intervention: Intervention)

    @Delete
    suspend fun delete(intervention: Intervention)

    @Query("SELECT * FROM interventions WHERE id = :interventionId")
    suspend fun getInterventionById(interventionId: Long): Intervention?

    @Query("SELECT * FROM interventions ORDER BY createdAt DESC")
    fun getAllInterventions(): LiveData<List<Intervention>>

    @Query("SELECT * FROM interventions WHERE status != 'closed' ORDER BY createdAt DESC")
    fun getAllOpenInterventions(): LiveData<List<Intervention>>

    @Query("SELECT COUNT(*) FROM interventions WHERE status != 'closed'")
    fun getAllOpenInterventionCount(): LiveData<Int>

    @Query("SELECT * FROM interventions WHERE LOWER(TRIM(:department)) = 'tutti' OR assignedUserId = :userId OR (assignedUserId = 0 AND (LOWER(TRIM(department)) = LOWER(TRIM(:department)) OR LOWER(TRIM(department)) = 'generale' OR LOWER(TRIM(:department)) = 'generale')) ORDER BY date DESC, id DESC")
    fun getInterventionsForTechnician(userId: Long, department: String): LiveData<List<Intervention>>

    @Query("SELECT * FROM interventions WHERE (LOWER(TRIM(:department)) = 'tutti' OR assignedUserId = :userId OR (assignedUserId = 0 AND (LOWER(TRIM(department)) = LOWER(TRIM(:department)) OR LOWER(TRIM(department)) = 'generale' OR LOWER(TRIM(:department)) = 'generale'))) AND status != 'closed' ORDER BY date DESC, id DESC")
    fun getOpenInterventionsForTechnician(userId: Long, department: String): LiveData<List<Intervention>>

    @Query("SELECT COUNT(*) FROM interventions WHERE (LOWER(TRIM(:department)) = 'tutti' OR assignedUserId = :userId OR (assignedUserId = 0 AND (LOWER(TRIM(department)) = LOWER(TRIM(:department)) OR LOWER(TRIM(department)) = 'generale' OR LOWER(TRIM(:department)) = 'generale'))) AND status != 'closed'")
    fun getOpenInterventionCountForTechnician(userId: Long, department: String): LiveData<Int>

    @Query("SELECT * FROM interventions WHERE userId = :userId ORDER BY date DESC")
    fun getInterventionsByUser(userId: Long): LiveData<List<Intervention>>

    @Query("SELECT * FROM interventions WHERE clientId = :clientId ORDER BY date DESC")
    fun getInterventionsByClient(clientId: Long): LiveData<List<Intervention>>

    @Query("SELECT * FROM interventions WHERE userId = :userId AND clientId = :clientId ORDER BY date DESC")
    fun getInterventionsByClient(userId: Long, clientId: Long): LiveData<List<Intervention>>

    @Query("SELECT * FROM interventions WHERE userId = :userId AND status != 'closed' ORDER BY date DESC")
    fun getOpenInterventions(userId: Long): LiveData<List<Intervention>>

    @Query("SELECT COUNT(*) FROM interventions WHERE userId = :userId AND status != 'closed'")
    fun getOpenInterventionCount(userId: Long): LiveData<Int>

    @Query("UPDATE interventions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE interventions SET technicalNotes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String)
}
