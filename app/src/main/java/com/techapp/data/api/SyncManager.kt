package com.techapp.data.api

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.techapp.data.db.AppDatabase
import com.techapp.data.model.Appointment
import com.techapp.data.model.Client
import com.techapp.data.model.Intervention
import com.techapp.data.model.User
import com.techapp.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SyncStatus {
    ONLINE,
    SYNCING,
    OFFLINE
}

object SyncManager {

    private val _syncStatus = MutableLiveData<SyncStatus>(SyncStatus.OFFLINE)
    val syncStatus: LiveData<SyncStatus> = _syncStatus

    private val _lastSyncTime = MutableLiveData<String>("")
    val lastSyncTime: LiveData<String> = _lastSyncTime

    suspend fun sync(context: Context): Boolean = withContext(Dispatchers.IO) {
        _syncStatus.postValue(SyncStatus.SYNCING)
        try {
            val api = ApiClient.getService(context)
            val response = api.syncAll()

            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!
                val db = AppDatabase.getInstance(context)

                val currentUserId = SessionManager(context).getUserId().takeIf { it > 0 } ?: 1L

                // Sync Clients
                val clientsDao = db.clientDao()
                data.clients.forEach { c ->
                    val clientEntity = Client(
                        id = c.id,
                        userId = currentUserId,
                        name = c.name,
                        phone = c.phone,
                        address = c.address,
                        email = c.email,
                        notes = c.notes
                    )
                    clientsDao.insert(clientEntity)
                }

                // Sync Appointments
                val appointmentDao = db.appointmentDao()
                data.appointments.forEach { a ->
                    val aptEntity = Appointment(
                        id = a.id,
                        userId = if (a.assignedUserId > 0) a.assignedUserId else currentUserId,
                        clientId = a.clientId,
                        clientName = a.clientName,
                        date = a.date,
                        time = a.time,
                        description = a.description,
                        department = a.department,
                        assignedUserId = a.assignedUserId,
                        assignedUserName = a.assignedUserName,
                        status = a.status
                    )
                    appointmentDao.insert(aptEntity)
                }

                // Sync Interventions
                val interventionDao = db.interventionDao()
                data.interventions.forEach { i ->
                    val intvEntity = Intervention(
                        id = i.id,
                        userId = i.technicianId,
                        clientId = i.clientId,
                        clientName = i.clientName,
                        date = i.date,
                        description = i.description,
                        status = i.status,
                        technicalNotes = i.notes,
                        department = i.department,
                        assignedUserId = i.technicianId,
                        assignedUserName = i.technicianName
                    )
                    interventionDao.insert(intvEntity)
                }

                // Sync Technicians
                val userDao = db.userDao()
                data.technicians.forEach { t ->
                    val existing = userDao.getUserByEmail(t.email)
                    if (existing == null) {
                        val userEntity = User(
                            id = t.id,
                            email = t.email,
                            passwordHash = "",
                            firstName = t.firstName,
                            lastName = t.lastName,
                            role = t.role.lowercase(),
                            department = t.department
                        )
                        userDao.insert(userEntity)
                    }
                }

                val nowStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                SessionManager(context).saveLastSync(nowStr)
                _lastSyncTime.postValue(nowStr)
                _syncStatus.postValue(SyncStatus.ONLINE)
                true
            } else {
                _syncStatus.postValue(SyncStatus.OFFLINE)
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _syncStatus.postValue(SyncStatus.OFFLINE)
            false
        }
    }
}
