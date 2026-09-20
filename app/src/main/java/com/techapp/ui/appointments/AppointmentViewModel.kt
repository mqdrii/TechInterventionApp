package com.techapp.ui.appointments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.techapp.data.api.ApiClient
import com.techapp.data.api.AppointmentDto
import com.techapp.data.api.StatusUpdateDto
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
    val errorMessage = MutableLiveData<String?>()

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
            try {
                // 1) Prima crea sul server
                val api = ApiClient.getService(getApplication())
                val response = api.createAppointment(
                    AppointmentDto(
                        clientId = clientId, clientName = clientName.trim(),
                        date = date, time = time, description = description.trim(),
                        department = department, assignedUserId = assignedUserId,
                        assignedUserName = assignedUserName
                    )
                )
                if (response.isSuccessful) {
                    val srv = response.body()!!
                    repository.insertAppointment(
                        Appointment(
                            id = srv.id, userId = userId, clientId = srv.clientId,
                            clientName = srv.clientName, date = srv.date, time = srv.time,
                            description = srv.description, department = srv.department,
                            assignedUserId = srv.assignedUserId,
                            assignedUserName = srv.assignedUserName, status = srv.status
                        )
                    )
                    insertResult.value = true
                } else {
                    errorMessage.value = "Errore server: ${response.code()}"
                    insertResult.value = false
                }
            } catch (e: Exception) {
                // Fallback offline
                repository.insertAppointment(
                    Appointment(
                        userId = userId, clientId = clientId, clientName = clientName.trim(),
                        date = date, time = time, description = description.trim(),
                        department = department, assignedUserId = assignedUserId,
                        assignedUserName = assignedUserName
                    )
                )
                insertResult.value = true
                errorMessage.value = "Salvato offline. Sincronizzazione al prossimo avvio."
            }
        }
    }

    fun updateStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateStatus(id, status)
            try {
                val api = ApiClient.getService(getApplication())
                api.updateAppointmentStatus(id, StatusUpdateDto(status))
            } catch (_: Exception) {}
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            var serverOk = false
            try {
                val api = ApiClient.getService(getApplication())
                val response = api.deleteAppointment(appointment.id)
                serverOk = response.isSuccessful
            } catch (_: Exception) {}

            if (serverOk) {
                repository.deleteAppointment(appointment)
            } else {
                // Server non raggiungibile o Render non aggiornato: elimina comunque localmente
                // ma l'elemento tornerà alla prossima sync dal server
                repository.deleteAppointment(appointment)
                errorMessage.postValue("Eliminato localmente. Potrebbe riapparire se il server non è aggiornato.")
            }
        }
    }
}
