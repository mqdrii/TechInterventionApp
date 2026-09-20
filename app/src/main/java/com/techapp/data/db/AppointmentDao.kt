package com.techapp.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.techapp.data.model.Appointment

@Dao
interface AppointmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: Appointment): Long

    @Update
    suspend fun update(appointment: Appointment)

    @Delete
    suspend fun delete(appointment: Appointment)

    @Query("SELECT * FROM appointments WHERE id = :appointmentId")
    suspend fun getAppointmentById(appointmentId: Long): Appointment?

    @Query("SELECT * FROM appointments ORDER BY date DESC, time DESC")
    fun getAllAppointments(): LiveData<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE LOWER(TRIM(:department)) = 'tutti' OR assignedUserId = :userId OR (assignedUserId = 0 AND (LOWER(TRIM(department)) = LOWER(TRIM(:department)) OR LOWER(TRIM(department)) = 'generale' OR LOWER(TRIM(:department)) = 'generale')) ORDER BY date DESC, time DESC")
    fun getAppointmentsForTechnician(userId: Long, department: String): LiveData<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE userId = :userId ORDER BY date DESC, time DESC")
    fun getAppointmentsByUser(userId: Long): LiveData<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE clientId = :clientId ORDER BY date DESC")
    fun getAppointmentsByClient(clientId: Long): LiveData<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE userId = :userId AND clientId = :clientId ORDER BY date DESC")
    fun getAppointmentsByClient(userId: Long, clientId: Long): LiveData<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE date = :today AND status = 'scheduled' ORDER BY time ASC")
    fun getAllTodayAppointments(today: String): LiveData<List<Appointment>>

    @Query("SELECT COUNT(*) FROM appointments WHERE date = :today AND status = 'scheduled'")
    fun getAllTodayAppointmentCount(today: String): LiveData<Int>

    @Query("SELECT * FROM appointments WHERE (LOWER(TRIM(:department)) = 'tutti' OR assignedUserId = :userId OR (assignedUserId = 0 AND (LOWER(TRIM(department)) = LOWER(TRIM(:department)) OR LOWER(TRIM(department)) = 'generale' OR LOWER(TRIM(:department)) = 'generale'))) AND date = :today AND status = 'scheduled' ORDER BY time ASC")
    fun getTodayAppointmentsForTechnician(userId: Long, department: String, today: String): LiveData<List<Appointment>>

    @Query("SELECT COUNT(*) FROM appointments WHERE (LOWER(TRIM(:department)) = 'tutti' OR assignedUserId = :userId OR (assignedUserId = 0 AND (LOWER(TRIM(department)) = LOWER(TRIM(:department)) OR LOWER(TRIM(department)) = 'generale' OR LOWER(TRIM(:department)) = 'generale'))) AND date = :today AND status = 'scheduled'")
    fun getTodayAppointmentCountForTechnician(userId: Long, department: String, today: String): LiveData<Int>

    @Query("SELECT * FROM appointments WHERE userId = :userId AND date = :today AND status = 'scheduled' ORDER BY time ASC")
    fun getTodayAppointments(userId: Long, today: String): LiveData<List<Appointment>>

    @Query("SELECT COUNT(*) FROM appointments WHERE userId = :userId AND date = :today AND status = 'scheduled'")
    fun getTodayAppointmentCount(userId: Long, today: String): LiveData<Int>

    @Query("UPDATE appointments SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}
