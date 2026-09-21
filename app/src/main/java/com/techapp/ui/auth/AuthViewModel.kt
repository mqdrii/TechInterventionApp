package com.techapp.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.techapp.data.api.ApiClient
import com.techapp.data.api.LoginRequest
import com.techapp.data.api.RegisterRequest
import com.techapp.data.api.ResendOtpRequest
import com.techapp.data.api.VerifyEmailRequest
import com.techapp.data.db.AppDatabase
import com.techapp.data.model.User
import com.techapp.data.repository.UserRepository
import com.techapp.utils.SessionManager
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository
    private val sessionManager: SessionManager

    val loginResult     = MutableLiveData<LoginResult>()
    val registerResult  = MutableLiveData<RegisterResult>()
    val verifyResult    = MutableLiveData<VerifyResult>()
    val resendResult    = MutableLiveData<String>()

    init {
        val db = AppDatabase.getInstance(application)
        repository = UserRepository(db.userDao())
        sessionManager = SessionManager(application)
    }

    // ─── LOGIN ───────────────────────────────────────────────────────────────
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            loginResult.value = LoginResult.Error("Compila tutti i campi")
            return
        }
        viewModelScope.launch {
            try {
                val api = ApiClient.getService(getApplication())
                val response = api.login(LoginRequest(email.trim().lowercase(), password))
                if (response.isSuccessful) {
                    val body = response.body()!!

                    // Account non verificato → manda alla schermata OTP
                    if (body.requiresVerification && body.userId != null) {
                        loginResult.value = LoginResult.RequiresVerification(body.userId)
                        return@launch
                    }

                    val u = body.user!!
                    sessionManager.saveSession(
                        userId     = u.id,
                        fullName   = "${u.firstName} ${u.lastName}",
                        email      = u.email,
                        role       = u.role,
                        department = u.department,
                        token      = body.token ?: ""
                    )
                    repository.upsertUserFromServer(u.id, u.email, u.firstName, u.lastName, u.role, u.department)
                    loginResult.value = LoginResult.Success(
                        User(id = u.id, email = u.email, firstName = u.firstName,
                            lastName = u.lastName, role = u.role, department = u.department,
                            passwordHash = "")
                    )
                } else {
                    // Controlla se 403 = account non verificato
                    if (response.code() == 403) {
                        try {
                            val errBody = response.errorBody()?.string() ?: ""
                            // Estrai userId dalla risposta JSON manualmente
                            val uidMatch = Regex("\"userId\":(\\d+)").find(errBody)
                            val uid = uidMatch?.groupValues?.get(1)?.toLongOrNull()
                            if (uid != null) {
                                loginResult.value = LoginResult.RequiresVerification(uid)
                                return@launch
                            }
                        } catch (_: Exception) {}
                    }
                    val errMsg = when (response.code()) {
                        401 -> "Email o password errati"
                        403 -> "Account non verificato. Controlla la tua email."
                        else -> "Errore server (${response.code()})"
                    }
                    loginResult.value = LoginResult.Error(errMsg)
                }
            } catch (e: Exception) {
                val user = repository.loginUser(email.trim(), password)
                if (user != null) {
                    sessionManager.saveSession(
                        userId     = user.id,
                        fullName   = "${user.firstName} ${user.lastName}",
                        email      = user.email,
                        role       = user.role,
                        department = user.department
                    )
                    loginResult.value = LoginResult.Success(user)
                } else {
                    loginResult.value = LoginResult.Error("Nessuna connessione al server. Riprova con internet.")
                }
            }
        }
    }

    // ─── REGISTER ─────────────────────────────────────────────────────────────
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
                    try {
                        val api = ApiClient.getService(getApplication())
                        val response = api.register(
                            RegisterRequest(
                                email      = email.trim().lowercase(),
                                password   = password,
                                firstName  = firstName.trim(),
                                lastName   = lastName.trim(),
                                role       = role,
                                department = department
                            )
                        )
                        if (response.isSuccessful) {
                            val body = response.body()!!
                            if (body.requiresVerification && body.userId != null) {
                                // Nuovo flusso: vai alla schermata OTP
                                registerResult.value = RegisterResult.RequiresVerification(
                                    userId = body.userId,
                                    message = body.message ?: "Controlla la tua email per il codice di verifica."
                                )
                            } else if (body.user != null && body.token != null) {
                                // Vecchio flusso (fallback senza email service)
                                val u = body.user
                                repository.upsertUserFromServer(u.id, u.email, u.firstName, u.lastName, u.role, u.department)
                                sessionManager.saveSession(
                                    userId     = u.id,
                                    fullName   = "${u.firstName} ${u.lastName}",
                                    email      = u.email,
                                    role       = u.role,
                                    department = u.department,
                                    token      = body.token
                                )
                                registerResult.value = RegisterResult.Success
                            }
                        } else {
                            val errMsg = when (response.code()) {
                                409  -> "Questa email è già registrata"
                                400  -> "Dati non validi — controlla email e password"
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

    // ─── VERIFY EMAIL (OTP) ───────────────────────────────────────────────────
    fun verifyEmail(userId: Long, otp: String) {
        if (otp.isBlank() || otp.length != 6) {
            verifyResult.value = VerifyResult.Error("Inserisci il codice a 6 cifre")
            return
        }
        viewModelScope.launch {
            try {
                val api = ApiClient.getService(getApplication())
                val response = api.verifyEmail(VerifyEmailRequest(userId, otp.trim()))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    val u = body.user!!
                    repository.upsertUserFromServer(u.id, u.email, u.firstName, u.lastName, u.role, u.department)
                    sessionManager.saveSession(
                        userId     = u.id,
                        fullName   = "${u.firstName} ${u.lastName}",
                        email      = u.email,
                        role       = u.role,
                        department = u.department,
                        token      = body.token ?: ""
                    )
                    verifyResult.value = VerifyResult.Success(
                        User(id = u.id, email = u.email, firstName = u.firstName,
                            lastName = u.lastName, role = u.role, department = u.department,
                            passwordHash = "")
                    )
                } else {
                    verifyResult.value = VerifyResult.Error(
                        when (response.code()) {
                            400  -> "Codice non corretto o scaduto. Riprova."
                            404  -> "Sessione scaduta. Registrati di nuovo."
                            else -> "Errore (${response.code()})"
                        }
                    )
                }
            } catch (e: Exception) {
                verifyResult.value = VerifyResult.Error("Nessuna connessione al server.")
            }
        }
    }

    // ─── RESEND OTP ───────────────────────────────────────────────────────────
    fun resendOtp(userId: Long) {
        viewModelScope.launch {
            try {
                val api = ApiClient.getService(getApplication())
                val response = api.resendOtp(ResendOtpRequest(userId))
                resendResult.value = if (response.isSuccessful) {
                    "Nuovo codice inviato. Controlla la tua email."
                } else {
                    "Errore nell'invio. Riprova."
                }
            } catch (e: Exception) {
                resendResult.value = "Nessuna connessione al server."
            }
        }
    }

    // ─── Result types ─────────────────────────────────────────────────────────
    sealed class LoginResult {
        data class Success(val user: User) : LoginResult()
        data class Error(val message: String) : LoginResult()
        data class RequiresVerification(val userId: Long) : LoginResult()
    }

    sealed class RegisterResult {
        object Success : RegisterResult()
        data class Error(val message: String) : RegisterResult()
        data class RequiresVerification(val userId: Long, val message: String) : RegisterResult()
    }

    sealed class VerifyResult {
        data class Success(val user: User) : VerifyResult()
        data class Error(val message: String) : VerifyResult()
    }
}
