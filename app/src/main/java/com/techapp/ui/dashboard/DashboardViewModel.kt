package com.techapp.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.techapp.data.db.AppDatabase
import com.techapp.data.model.Appointment
import com.techapp.data.model.Intervention
import com.techapp.data.repository.AppointmentRepository
import com.techapp.data.repository.InterventionRepository
import com.techapp.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val session = SessionManager(application)
    private val userId = session.getUserId()
    private val department = session.getUserDepartment()
    private val isAdmin = session.isAdmin()
    private val db = AppDatabase.getInstance(application)
    private val appointmentRepo = AppointmentRepository(db.appointmentDao())
    private val interventionRepo = InterventionRepository(db.interventionDao())
    private val clientRepo = ClientRepository(db.clientDao())

    val todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val totalClientsCount: LiveData<Int> = clientRepo.getAllClientCount()

    val todayAppointmentCount: LiveData<Int> = if (isAdmin) {
        appointmentRepo.getAllTodayAppointmentCount(todayDate)
    } else {
        appointmentRepo.getTodayAppointmentCountForTechnician(userId, department, todayDate)
    }

    val todayAppointments: LiveData<List<Appointment>> = if (isAdmin) {
        appointmentRepo.getAllTodayAppointments(todayDate)
    } else {
        appointmentRepo.getTodayAppointmentsForTechnician(userId, department, todayDate)
    }

    val openInterventionCount: LiveData<Int> = if (isAdmin) {
        interventionRepo.getAllOpenInterventionCount()
    } else {
        interventionRepo.getOpenInterventionCountForTechnician(userId, department)
    }

    val openInterventions: LiveData<List<Intervention>> = if (isAdmin) {
        interventionRepo.getAllOpenInterventions()
    } else {
        interventionRepo.getOpenInterventionsForTechnician(userId, department)
    }

    fun getUserName(): String = session.getUserName()
    fun getUserRole(): String = session.getUserRole()
    fun getUserDepartment(): String = session.getUserDepartment()
    fun isAdmin(): Boolean = session.isAdmin()
}
