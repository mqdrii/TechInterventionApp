package com.techapp.data.api

import com.google.gson.annotations.SerializedName

data class HealthDto(
    val status: String,
    val server: String,
    val uptime: Double?,
    val timestamp: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val department: String
)

data class AuthResponse(
    val user: UserDto?,
    val token: String?,
    // OTP flow
    val requiresVerification: Boolean = false,
    val userId: Long? = null,
    val message: String? = null
)

data class VerifyEmailRequest(
    val userId: Long,
    val otp: String
)

data class ResendOtpRequest(
    val userId: Long
)

data class UserDto(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val fullName: String?,
    val role: String,
    val department: String
)

data class ClientDto(
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val email: String = "",
    val notes: String = ""
)

data class AppointmentDto(
    val id: Long = 0,
    val clientId: Long = 0,
    val clientName: String,
    val date: String,
    val time: String,
    val description: String,
    val department: String = "Generale",
    val assignedUserId: Long = 0,
    val assignedUserName: String = "",
    val status: String = "Programmato"
)

data class InterventionDto(
    val id: Long = 0,
    val clientId: Long = 0,
    val clientName: String,
    val date: String,
    val description: String,
    val department: String = "Generale",
    val technicianId: Long = 0,
    val technicianName: String = "",
    val notes: String = "",
    val status: String = "In corso"
)

data class StatusUpdateDto(
    val status: String
)

data class GenericResponse(
    val success: Boolean,
    val message: String? = null
)

data class SyncResponse(
    val success: Boolean,
    val serverTime: String,
    val clients: List<ClientDto>,
    val appointments: List<AppointmentDto>,
    val interventions: List<InterventionDto>,
    val technicians: List<UserDto>
)

data class UpdateUserRequest(
    val firstName: String,
    val lastName: String,
    val department: String,
    val role: String,
    val password: String? = null
)

data class UpdateUserResponse(
    val success: Boolean,
    val user: UserDto? = null,
    val error: String? = null
)

data class FcmTokenRequest(
    val token: String
)

data class SimpleResponse(
    val success: Boolean,
    val message: String? = null
)
