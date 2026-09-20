package com.techapp.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.techapp.data.api.ApiClient
import com.techapp.data.api.LoginRequest
import com.techapp.data.api.RegisterRequest
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
            // 1) Prova sempre prima il server cloud
            try {
                val api = ApiClient.getService(getApplication())
                val response = api.login(LoginRequest(email.trim().lowercase(), password))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    val u = body.user
                    sessionManager.saveSession(
                        userId    = u.id,
                        fullName  = "${u.firstName} ${u.lastName}",
                        email     = u.email,
                        role      = u.role,
                        department= u.department,
                        token     = body.token
                    )
                    // Aggiorna anche il DB locale per coerenza
                    repository.upsertUserFromServer(u.id, u.email, u.firstName, u.lastName, u.role, u.department)
                    loginResult.value = LoginResult.Success(
                        User(id = u.id, email = u.email, firstName = u.firstName,
                            lastName = u.lastName, role = u.role, department = u.department,
                            passwordHash = "")
                    )
                    return@launch
                } else {
                    val errMsg = when (response.code()) {
                        401 -> "Email o password errati"
                        else -> "Errore server (${response.code()})"
                    }
                    loginResult.value = LoginResult.Error(errMsg)
                    return@launch
                }
            } catch (e: Exception) {
                // 2) Nessuna connessione → fallback locale
                val user = repository.loginUser(email.trim(), password)
                if (user != null) {
                    sessionManager.saveSession(
                        userId    = user.id,
                        fullName  = "${user.firstName} ${user.lastName}",
                        email     = user.email,
                        role      = user.role,
                        department= user.department
                    )
                    loginResult.value = LoginResult.Success(user)
                } else {
                    loginResult.value = LoginResult.Error("Nessuna connessione al server. Riprova con internet.")
                }
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
                    // 1) Registra sul server
                    try {
                        val api = ApiClient.getService(getApplication())
                        val response = api.register(
                            RegisterRequest(
                                email = email.trim().lowercase(),
                                password = password,
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                role = role,
                                department = department
                            )
                        )
                        if (response.isSuccessful) {
                            val body = response.body()!!
                            val u = body.user
                            // Salva nel DB locale
                            repository.upsertUserFromServer(u.id, u.email, u.firstName, u.lastName, u.role, u.department)
                            sessionManager.saveSession(
                                userId    = u.id,
                                fullName  = "${u.firstName} ${u.lastName}",
                                email     = u.email,
                                role      = u.role,
                                department= u.department,
                                token     = body.token
                            )
                            registerResult.value = RegisterResult.Success
                        } else {
                            val errMsg = when (response.code()) {
                                409 -> "Questa email è già registrata"
                                else -> "Errore server (${response.code()})"
                            }
                            registerResult.value = RegisterResult.Error(errMsg)
                        }
                    } catch (e: Exception) {
                        registerResult.value = RegisterResult.Error("Nessuna connessione al server. Assicurati di avere internet.")
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
