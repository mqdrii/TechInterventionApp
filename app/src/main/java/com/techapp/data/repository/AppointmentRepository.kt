package com.techapp.data.repository

import androidx.lifecycle.LiveData
import com.techapp.data.db.AppointmentDao
import com.techapp.data.model.Appointment

class AppointmentRepository(private val appointmentDao: AppointmentDao) {

    fun getAppointmentsByUser(userId: Long): LiveData<List<Appointment>> =
        appointmentDao.getAppointmentsByUser(userId)

    fun getAppointmentsByDate(userId: Long, date: String): LiveData<List<Appointment>> =
        appointmentDao.getAppointmentsByDate(userId, date)

    fun getAppointmentsByClient(userId: Long, clientId: Long): LiveData<List<Appointment>> =
        appointmentDao.getAppointmentsByClient(userId, clientId)

    fun getTodayAppointments(userId: Long, today: String): LiveData<List<Appointment>> =
        appointmentDao.getTodayAppointments(userId, today)

    fun getTodayAppointmentCount(userId: Long, today: String): LiveData<Int> =
        appointmentDao.getTodayAppointmentCount(userId, today)

    suspend fun getAppointmentById(id: Long): Appointment? =
        appointmentDao.getAppointmentById(id)

    suspend fun insertAppointment(appointment: Appointment): Long =
        appointmentDao.insert(appointment)

    suspend fun updateAppointment(appointment: Appointment) =
        appointmentDao.update(appointment)

    suspend fun deleteAppointment(appointment: Appointment) =
        appointmentDao.delete(appointment)

    suspend fun updateStatus(id: Long, status: String) =
        appointmentDao.updateStatus(id, status)
}
