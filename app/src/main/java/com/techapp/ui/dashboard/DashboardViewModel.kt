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

    private val appointmentRepo: AppointmentRepository
    private val interventionRepo: InterventionRepository
    private val session: SessionManager
    private val userId: Long

    val todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayAppointmentCount: LiveData<Int>
    val todayAppointments: LiveData<List<Appointment>>
    val openInterventionCount: LiveData<Int>
    val openInterventions: LiveData<List<Intervention>>

    init {
        val db = AppDatabase.getInstance(application)
        session = SessionManager(application)
        userId = session.getUserId()
        appointmentRepo = AppointmentRepository(db.appointmentDao())
        interventionRepo = InterventionRepository(db.interventionDao())

        todayAppointmentCount = appointmentRepo.getTodayAppointmentCount(userId, todayDate)
        todayAppointments = appointmentRepo.getTodayAppointments(userId, todayDate)
        openInterventionCount = interventionRepo.getOpenInterventionCount(userId)
        openInterventions = interventionRepo.getOpenInterventions(userId)
    }

    fun getUserName(): String = session.getUserName()
}
