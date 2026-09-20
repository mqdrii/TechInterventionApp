package com.techapp.data.repository

import androidx.lifecycle.LiveData
import com.techapp.data.db.AppointmentDao
import com.techapp.data.model.Appointment

class AppointmentRepository(private val appointmentDao: AppointmentDao) {

    fun getAllAppointments(): LiveData<List<Appointment>> =
        appointmentDao.getAllAppointments()

    fun getAppointmentsForTechnician(userId: Long, department: String): LiveData<List<Appointment>> =
        appointmentDao.getAppointmentsForTechnician(userId, department)

    fun getAppointmentsByUser(userId: Long): LiveData<List<Appointment>> =
        appointmentDao.getAppointmentsByUser(userId)

    fun getAppointmentsByClient(clientId: Long): LiveData<List<Appointment>> =
        appointmentDao.getAppointmentsByClient(clientId)

    fun getAppointmentsByClient(userId: Long, clientId: Long): LiveData<List<Appointment>> =
        appointmentDao.getAppointmentsByClient(userId, clientId)

    fun getAllTodayAppointments(today: String): LiveData<List<Appointment>> =
        appointmentDao.getAllTodayAppointments(today)

    fun getAllTodayAppointmentCount(today: String): LiveData<Int> =
        appointmentDao.getAllTodayAppointmentCount(today)

    fun getTodayAppointmentsForTechnician(userId: Long, department: String, today: String): LiveData<List<Appointment>> =
        appointmentDao.getTodayAppointmentsForTechnician(userId, department, today)

    fun getTodayAppointmentCountForTechnician(userId: Long, department: String, today: String): LiveData<Int> =
        appointmentDao.getTodayAppointmentCountForTechnician(userId, department, today)

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
