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
    private val session = SessionManager(application)
    private val userId = session.getUserId()
    private val department = session.getUserDepartment()
    private val isAdmin = session.isAdmin()
    private val repository = AppointmentRepository(db.appointmentDao())

    val todayDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val allAppointments: LiveData<List<Appointment>> = if (isAdmin) {
        repository.getAllAppointments()
    } else {
        repository.getAppointmentsForTechnician(userId, department)
    }

    val technicians: LiveData<List<com.techapp.data.model.User>> = db.userDao().getTechnicians()

    val insertResult = MutableLiveData<Boolean>()

    fun getAppointmentsByClient(clientId: Long): LiveData<List<Appointment>> =
        repository.getAppointmentsByClient(clientId)

    fun insertAppointment(
        clientId: Long,
        clientName: String,
        date: String,
        time: String,
        description: String,
        department: String = "Generale",
        assignedUserId: Long = 0,
        assignedUserName: String = ""
    ) {
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
                description = description,
                department = department,
                assignedUserId = assignedUserId,
                assignedUserName = assignedUserName
            )
            val localId = repository.insertAppointment(appointment)
            insertResult.value = true
            try {
                val api = com.techapp.data.api.ApiClient.getService(getApplication())
                val dto = com.techapp.data.api.AppointmentDto(
                    id = localId,
                    clientId = appointment.clientId,
                    clientName = appointment.clientName,
                    date = appointment.date,
                    time = appointment.time,
                    description = appointment.description,
                    department = appointment.department,
                    assignedUserId = appointment.assignedUserId,
                    assignedUserName = appointment.assignedUserName,
                    status = appointment.status
                )
                api.createAppointment(dto)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateStatus(id, status)
            try {
                val api = com.techapp.data.api.ApiClient.getService(getApplication())
                api.updateAppointmentStatus(id, com.techapp.data.api.StatusUpdateDto(status))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch { repository.deleteAppointment(appointment) }
    }
}
