package com.techapp.ui.clients

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.techapp.data.api.ApiClient
import com.techapp.data.api.ClientDto
import com.techapp.data.db.AppDatabase
import com.techapp.data.model.Client
import com.techapp.data.repository.ClientRepository
import com.techapp.utils.SessionManager
import kotlinx.coroutines.launch

class ClientViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = ClientRepository(db.clientDao())
    private val session = SessionManager(application)
    private val userId = session.getUserId()

    private val searchQuery = MutableLiveData<String>("")

    val clients: LiveData<List<Client>> = searchQuery.switchMap { query ->
        if (query.isBlank()) repository.getAllClients()
        else repository.searchAllClients(query)
    }

    val insertResult = MutableLiveData<Boolean>()
    val updateResult = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()

    fun search(query: String) {
        searchQuery.value = query
    }

    fun insertClient(name: String, address: String, phone: String, email: String, notes: String) {
        if (name.isBlank()) { insertResult.value = false; return }
        viewModelScope.launch {
            try {
                // 1) Prima crea sul server
                val api = ApiClient.getService(getApplication())
                val response = api.createClient(
                    ClientDto(name = name.trim(), phone = phone.trim(),
                        address = address.trim(), email = email.trim(), notes = notes.trim())
                )
                if (response.isSuccessful) {
                    val serverClient = response.body()!!
                    // 2) Salva in locale con l'ID del server
                    repository.insertClient(
                        Client(id = serverClient.id, userId = userId,
                            name = serverClient.name, phone = serverClient.phone,
                            address = serverClient.address, email = serverClient.email,
                            notes = serverClient.notes)
                    )
                    insertResult.value = true
                } else {
                    errorMessage.value = "Errore server: ${response.code()}"
                    insertResult.value = false
                }
            } catch (e: Exception) {
                // Fallback: salva solo in locale se offline
                val client = Client(userId = userId, name = name.trim(),
                    address = address.trim(), phone = phone.trim(),
                    email = email.trim(), notes = notes.trim())
                repository.insertClient(client)
                insertResult.value = true
                errorMessage.value = "Salvato offline. Sincronizzazione al prossimo avvio."
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
            try {
                val api = ApiClient.getService(getApplication())
                api.deleteClient(client.id)
            } catch (_: Exception) {}
            repository.deleteClient(client)
        }
    }
}
