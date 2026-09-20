package com.techapp.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.techapp.data.db.AppDatabase
import com.techapp.data.model.User
import com.techapp.data.repository.UserRepository
import com.techapp.utils.SessionManager
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository
    private val sessionManager: SessionManager

    val loginResult = MutableLiveData<LoginResult>()
    val registerResult = MutableLiveData<RegisterResult>()

    init {
        val db = AppDatabase.getInstance(application)
        repository = UserRepository(db.userDao())
        sessionManager = SessionManager(application)
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            loginResult.value = LoginResult.Error("Compila tutti i campi")
            return
        }
        viewModelScope.launch {
            val user = repository.loginUser(email.trim(), password)
            if (user != null) {
                sessionManager.saveSession(
                    userId = user.id,
                    fullName = "${user.firstName} ${user.lastName}",
                    email = user.email,
                    role = user.role,
                    department = user.department
                )
                loginResult.value = LoginResult.Success(user)
            } else {
                loginResult.value = LoginResult.Error("Email o password errati")
            }
        }
    }

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String,
        role: String = User.ROLE_TECHNICIAN,
        department: String = User.DEPARTMENT_GENERAL
    ) {
        when {
            firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank() ->
                registerResult.value = RegisterResult.Error("Compila tutti i campi")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                registerResult.value = RegisterResult.Error("Email non valida")
            password.length < 6 ->
                registerResult.value = RegisterResult.Error("La password deve essere di almeno 6 caratteri")
            password != confirmPassword ->
                registerResult.value = RegisterResult.Error("Le password non coincidono")
            else -> {
                viewModelScope.launch {
                    val userId = repository.registerUser(
                        firstName = firstName.trim(),
                        lastName = lastName.trim(),
                        email = email.trim(),
                        password = password,
                        role = role,
                        department = department
                    )
                    if (userId > 0) {
                        registerResult.value = RegisterResult.Success
                    } else {
                        registerResult.value = RegisterResult.Error("Email già registrata")
                    }
                }
            }
        }
    }

    sealed class LoginResult {
        data class Success(val user: User) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }

    sealed class RegisterResult {
        object Success : RegisterResult()
        data class Error(val message: String) : RegisterResult()
    }
}
