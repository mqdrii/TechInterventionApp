package com.techapp.data.repository

import androidx.lifecycle.LiveData
import com.techapp.data.db.InterventionDao
import com.techapp.data.model.Intervention

class InterventionRepository(private val interventionDao: InterventionDao) {

    fun getAllInterventions(): LiveData<List<Intervention>> =
        interventionDao.getAllInterventions()

    fun getAllOpenInterventions(): LiveData<List<Intervention>> =
        interventionDao.getAllOpenInterventions()

    fun getAllOpenInterventionCount(): LiveData<Int> =
        interventionDao.getAllOpenInterventionCount()

    fun getInterventionsForTechnician(userId: Long, department: String): LiveData<List<Intervention>> =
        interventionDao.getInterventionsForTechnician(userId, department)

    fun getOpenInterventionsForTechnician(userId: Long, department: String): LiveData<List<Intervention>> =
        interventionDao.getOpenInterventionsForTechnician(userId, department)

    fun getOpenInterventionCountForTechnician(userId: Long, department: String): LiveData<Int> =
        interventionDao.getOpenInterventionCountForTechnician(userId, department)

    fun getInterventionsByUser(userId: Long): LiveData<List<Intervention>> =
        interventionDao.getInterventionsByUser(userId)

    fun getInterventionsByClient(clientId: Long): LiveData<List<Intervention>> =
        interventionDao.getInterventionsByClient(clientId)

    fun getInterventionsByClient(userId: Long, clientId: Long): LiveData<List<Intervention>> =
        interventionDao.getInterventionsByClient(userId, clientId)

    fun getOpenInterventions(userId: Long): LiveData<List<Intervention>> =
        interventionDao.getOpenInterventions(userId)

    fun getOpenInterventionCount(userId: Long): LiveData<Int> =
        interventionDao.getOpenInterventionCount(userId)

    suspend fun getInterventionById(id: Long): Intervention? =
        interventionDao.getInterventionById(id)

    suspend fun insertIntervention(intervention: Intervention): Long =
        interventionDao.insert(intervention)

    suspend fun updateIntervention(intervention: Intervention) =
        interventionDao.update(intervention)

    suspend fun deleteIntervention(intervention: Intervention) =
        interventionDao.delete(intervention)

    suspend fun updateStatus(id: Long, status: String) =
        interventionDao.updateStatus(id, status)

    suspend fun updateNotes(id: Long, notes: String) =
        interventionDao.updateNotes(id, notes)
}
