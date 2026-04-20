package com.example.biometricos.network

import retrofit2.Response
import retrofit2.http.*

// Modelo de Feedback de IA
data class AiFeedback(
    val emoji: String,
    val shortMessage: String
)

// Modelo de Usuario (Perfil)
data class Usuario(
    val id: String,
    val name: String,
    val email: String,
    val weightKg: Double,
    val heightCm: Int,
    val avatarUrl: String
)

// Modelo de Entrenamiento Premium
data class Entrenamiento(
    val id: String? = null,
    val type: String = "RUNNING",
    val distanceKm: Double,
    val timeMinutes: Int,
    val tags: List<String> = listOf("Manos Libres"),
    val originalText: String,
    val date: String? = null,
    val aiFeedback: AiFeedback? = null
)

interface ApiService {
    @Headers("x-api-key: secreto_deportivo_123")
    @GET("api/users/profile")
    suspend fun getProfile(): Response<Usuario>

    @Headers("x-api-key: secreto_deportivo_123")
    @GET("api/workouts")
    suspend fun getEntrenamientos(): Response<List<Entrenamiento>>

    @Headers("x-api-key: secreto_deportivo_123")
    @POST("api/workouts")
    suspend fun guardarEntrenamiento(@Body entrenamiento: Entrenamiento): Response<Entrenamiento>

    @Headers("x-api-key: secreto_deportivo_123")
    @DELETE("api/workouts/{id}")
    suspend fun borrarEntrenamiento(@Path("id") id: String): Response<Unit>
}
