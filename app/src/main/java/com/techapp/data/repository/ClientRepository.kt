package com.techapp.data.repository

import androidx.lifecycle.LiveData
import com.techapp.data.db.ClientDao
import com.techapp.data.model.Client

class ClientRepository(private val clientDao: ClientDao) {

    fun getAllClients(): LiveData<List<Client>> = clientDao.getAllClients()

    fun searchAllClients(query: String): LiveData<List<Client>> = clientDao.searchAllClients(query)

    fun getAllClientCount(): LiveData<Int> = clientDao.getAllClientCount()

    fun getClientsByUser(userId: Long): LiveData<List<Client>> =
        clientDao.getClientsByUser(userId)

    fun searchClients(userId: Long, query: String): LiveData<List<Client>> =
        clientDao.searchClients(userId, query)

    fun getClientCount(userId: Long): LiveData<Int> =
        clientDao.getClientCount(userId)

    suspend fun getClientById(id: Long): Client? = clientDao.getClientById(id)

    suspend fun insertClient(client: Client): Long = clientDao.insert(client)

    suspend fun updateClient(client: Client) = clientDao.update(client)

    suspend fun deleteClient(client: Client) = clientDao.delete(client)
}
