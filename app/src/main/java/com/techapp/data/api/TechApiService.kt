package com.techapp.data.api

import retrofit2.Response
import retrofit2.http.*

interface TechApiService {

    @GET("api/health")
    suspend fun checkHealth(): Response<HealthDto>

    @POST("api/auth/register")
    suspend fun register(@Body req: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): Response<AuthResponse>

    @GET("api/auth/technicians")
    suspend fun getTechnicians(): Response<List<UserDto>>

    @GET("api/sync")
    suspend fun syncAll(): Response<SyncResponse>

    @POST("api/clients")
    suspend fun createClient(@Body client: ClientDto): Response<ClientDto>

    @DELETE("api/clients/{id}")
    suspend fun deleteClient(@Path("id") id: Long): Response<GenericResponse>

    @POST("api/appointments")
    suspend fun createAppointment(@Body apt: AppointmentDto): Response<AppointmentDto>

    @PATCH("api/appointments/{id}/status")
    suspend fun updateAppointmentStatus(
        @Path("id") id: Long,
        @Body body: StatusUpdateDto
    ): Response<GenericResponse>

    @POST("api/interventions")
    suspend fun createIntervention(@Body intv: InterventionDto): Response<InterventionDto>

    @PATCH("api/interventions/{id}/status")
    suspend fun updateInterventionStatus(
        @Path("id") id: Long,
        @Body body: StatusUpdateDto
    ): Response<GenericResponse>
}
