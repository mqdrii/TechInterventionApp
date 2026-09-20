package com.techapp.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.techapp.data.model.Client

@Dao
interface ClientDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(client: Client): Long

    @Update
    suspend fun update(client: Client)

    @Delete
    suspend fun delete(client: Client)

    @Query("SELECT * FROM clients WHERE id = :clientId")
    suspend fun getClientById(clientId: Long): Client?

    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): LiveData<List<Client>>

    @Query("SELECT * FROM clients WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchAllClients(query: String): LiveData<List<Client>>

    @Query("SELECT COUNT(*) FROM clients")
    fun getAllClientCount(): LiveData<Int>

    @Query("SELECT * FROM clients WHERE userId = :userId ORDER BY name ASC")
    fun getClientsByUser(userId: Long): LiveData<List<Client>>

    @Query("SELECT * FROM clients WHERE userId = :userId AND name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchClients(userId: Long, query: String): LiveData<List<Client>>

    @Query("SELECT COUNT(*) FROM clients WHERE userId = :userId")
    fun getClientCount(userId: Long): LiveData<Int>
}
