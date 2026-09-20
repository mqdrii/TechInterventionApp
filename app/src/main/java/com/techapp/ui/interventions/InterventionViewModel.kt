package com.techapp.ui.interventions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
            val intervention = Intervention(
                userId = userId,
                clientId = clientId,
                clientName = clientName,
                date = today,
                description = description,
                technicalNotes = technicalNotes,
                department = department,
                assignedUserId = assignedUserId,
                assignedUserName = assignedUserName
            )
            repository.insertIntervention(intervention)
            insertResult.value = true
        }
    }

    fun updateStatus(id: Long, status: String) {
        viewModelScope.launch { repository.updateStatus(id, status) }
    }

    fun updateNotes(id: Long, notes: String) {
        viewModelScope.launch { repository.updateNotes(id, notes) }
    }

    fun deleteIntervention(intervention: Intervention) {
        viewModelScope.launch { repository.deleteIntervention(intervention) }
    }
}
