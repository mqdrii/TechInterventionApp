package com.techapp.ui.clients

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.techapp.data.db.AppDatabase
import com.techapp.data.model.Client
import com.techapp.data.repository.ClientRepository
import com.techapp.utils.SessionManager
import kotlinx.coroutines.launch

class ClientViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = ClientRepository(db.clientDao())
    private val userId = SessionManager(application).getUserId()

    private val searchQuery = MutableLiveData<String>("")

    val clients: LiveData<List<Client>> = searchQuery.switchMap { query ->
        if (query.isBlank()) {
            repository.getAllClients()
        } else {
            repository.searchAllClients(query)
        }
    }

    val insertResult = MutableLiveData<Boolean>()
    val updateResult = MutableLiveData<Boolean>()

    fun search(query: String) {
        searchQuery.value = query
    }

    fun insertClient(name: String, address: String, phone: String, email: String, notes: String) {
        if (name.isBlank()) {
            insertResult.value = false
            return
        }
        viewModelScope.launch {
            val client = Client(
                userId = userId,
                name = name.trim(),
                address = address.trim(),
                phone = phone.trim(),
                email = email.trim(),
                notes = notes.trim()
            )
            val localId = repository.insertClient(client)
            insertResult.value = true
            try {
                val api = com.techapp.data.api.ApiClient.getService(getApplication())
                val dto = com.techapp.data.api.ClientDto(
                    id = localId,
                    name = client.name,
                    phone = client.phone,
                    address = client.address,
                    email = client.email,
                    notes = client.notes
                )
                api.createClient(dto)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateClient(client: Client) {
        viewModelScope.launch {
            repository.updateClient(client)
            updateResult.value = true
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            repository.deleteClient(client)
            try {
                val api = com.techapp.data.api.ApiClient.getService(getApplication())
                api.deleteClient(client.id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
