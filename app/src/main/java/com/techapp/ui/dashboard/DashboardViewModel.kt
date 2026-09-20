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
    private val db = AppDatabase.getInstance(application)
    private val appointmentRepo = AppointmentRepository(db.appointmentDao())
    private val interventionRepo = InterventionRepository(db.interventionDao())

    val todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayAppointmentCount: LiveData<Int> = appointmentRepo.getTodayAppointmentCount(userId, todayDate)
    val todayAppointments: LiveData<List<Appointment>> = appointmentRepo.getTodayAppointments(userId, todayDate)
    val openInterventionCount: LiveData<Int> = interventionRepo.getOpenInterventionCount(userId)
    val openInterventions: LiveData<List<Intervention>> = interventionRepo.getOpenInterventions(userId)

    fun getUserName(): String = session.getUserName()
}
