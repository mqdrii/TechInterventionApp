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

    @Query("SELECT * FROM appointments WHERE userId = :userId ORDER BY date DESC, time DESC")
    fun getAppointmentsByUser(userId: Long): LiveData<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE userId = :userId AND date = :date ORDER BY time ASC")
    fun getAppointmentsByDate(userId: Long, date: String): LiveData<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE userId = :userId AND clientId = :clientId ORDER BY date DESC")
    fun getAppointmentsByClient(userId: Long, clientId: Long): LiveData<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE userId = :userId AND date = :today AND status = 'scheduled' ORDER BY time ASC")
    fun getTodayAppointments(userId: Long, today: String): LiveData<List<Appointment>>

    @Query("SELECT COUNT(*) FROM appointments WHERE userId = :userId AND date = :today AND status = 'scheduled'")
    fun getTodayAppointmentCount(userId: Long, today: String): LiveData<Int>

    @Query("UPDATE appointments SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}
