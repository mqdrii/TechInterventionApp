package com.techapp.ui.interventions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.techapp.data.api.ApiClient
import com.techapp.data.api.InterventionDto
import com.techapp.data.api.StatusUpdateDto
import com.techapp.data.db.AppDatabase
import com.techapp.data.model.Intervention
import com.techapp.data.repository.InterventionRepository
import com.techapp.utils.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class InterventionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val session = SessionManager(application)
    private val userId = session.getUserId()
    private val department = session.getUserDepartment()
    private val isAdmin = session.isAdmin()
    private val repository = InterventionRepository(db.interventionDao())

    val allInterventions: LiveData<List<Intervention>> = if (isAdmin) {
        repository.getAllInterventions()
    } else {
        repository.getInterventionsForTechnician(userId, department)
    }

    val openInterventions: LiveData<List<Intervention>> = if (isAdmin) {
        repository.getAllOpenInterventions()
    } else {
        repository.getOpenInterventionsForTechnician(userId, department)
    }

    val technicians: LiveData<List<com.techapp.data.model.User>> = db.userDao().getTechnicians()

    val insertResult = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()

    fun getInterventionsByClient(clientId: Long): LiveData<List<Intervention>> =
        repository.getInterventionsByClient(clientId)

    fun insertIntervention(
        clientId: Long,
        clientName: String,
        description: String,
        technicalNotes: String,
        department: String = "Generale",
        assignedUserId: Long = 0,
        assignedUserName: String = ""
    ) {
        if (clientName.isBlank() || description.isBlank()) {
            insertResult.value = false
            return
        }
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            try {
                // 1) Prima crea sul server
                val api = ApiClient.getService(getApplication())
                val response = api.createIntervention(
                    InterventionDto(
                        clientId = clientId, clientName = clientName.trim(),
                        date = today, description = description.trim(),
                        department = department, technicianId = assignedUserId,
                        technicianName = assignedUserName, notes = technicalNotes.trim()
                    )
                )
                if (response.isSuccessful) {
                    val srv = response.body()!!
                    repository.insertIntervention(
                        Intervention(
                            id = srv.id, userId = userId, clientId = srv.clientId,
                            clientName = srv.clientName, date = srv.date,
                            description = srv.description, department = srv.department,
                            assignedUserId = srv.technicianId,
                            assignedUserName = srv.technicianName,
                            technicalNotes = srv.notes, status = srv.status
                        )
                    )
                    insertResult.value = true
                } else {
                    errorMessage.value = "Errore server: ${response.code()}"
                    insertResult.value = false
                }
            } catch (e: Exception) {
                // Fallback offline
                repository.insertIntervention(
                    Intervention(
                        userId = userId, clientId = clientId, clientName = clientName.trim(),
                        date = today, description = description.trim(), department = department,
                        assignedUserId = assignedUserId, assignedUserName = assignedUserName,
                        technicalNotes = technicalNotes.trim()
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
                api.updateInterventionStatus(id, StatusUpdateDto(status))
            } catch (_: Exception) {}
        }
    }

    fun updateNotes(id: Long, notes: String) {
        viewModelScope.launch { repository.updateNotes(id, notes) }
    }

    fun deleteIntervention(intervention: Intervention) {
        viewModelScope.launch {
            try {
                val api = ApiClient.getService(getApplication())
                api.deleteIntervention(intervention.id)
            } catch (_: Exception) {}
            repository.deleteIntervention(intervention)
        }
    }
}
