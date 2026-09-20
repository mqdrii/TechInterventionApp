package com.techapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.techapp.data.model.Appointment
import com.techapp.data.model.Client
import com.techapp.data.model.Intervention
import com.techapp.data.model.User

@Database(
    entities = [User::class, Client::class, Appointment::class, Intervention::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun clientDao(): ClientDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun interventionDao(): InterventionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "techapp_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
