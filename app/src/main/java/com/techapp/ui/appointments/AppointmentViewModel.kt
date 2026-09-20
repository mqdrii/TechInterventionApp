package com.techapp.ui.appointments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.techapp.data.db.AppDatabase
import com.techapp.data.model.Appointment
import com.techapp.data.repository.AppointmentRepository
import com.techapp.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AppointmentViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val userId = SessionManager(application).getUserId()
    private val repository = AppointmentRepository(db.appointmentDao())

    val todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val allAppointments: LiveData<List<Appointment>> = repository.getAppointmentsByUser(userId)
    val insertResult = MutableLiveData<Boolean>()

    fun getAppointmentsByClient(clientId: Long): LiveData<List<Appointment>> =
        repository.getAppointmentsByClient(userId, clientId)

    fun insertAppointment(clientId: Long, clientName: String, date: String, time: String, description: String) {
        if (clientName.isBlank() || date.isBlank() || description.isBlank()) {
            insertResult.value = false
            return
        }
        viewModelScope.launch {
            val appointment = Appointment(
                userId = userId,
                clientId = clientId,
                clientName = clientName,
                date = date,
                time = time,
                description = description
            )
            repository.insertAppointment(appointment)
            insertResult.value = true
        }
    }

    fun updateStatus(id: Long, status: String) {
        viewModelScope.launch { repository.updateStatus(id, status) }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch { repository.deleteAppointment(appointment) }
    }
}
