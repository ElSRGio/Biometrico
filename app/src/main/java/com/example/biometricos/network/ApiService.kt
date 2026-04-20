package com.example.biometricos.network

import com.google.gson.annotations.SerializedName
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
    val weightKg: Double,
    val heightCm: Int,
    val avatarUrl: String
)

// Modelo de Entrenamiento Premium - Alineado con el nuevo Backend
data class Entrenamiento(
    val id: String? = null,
    val type: String = "RUNNING",
    
    @SerializedName("distanciaKm")
    val distanceKm: Double,
    
    @SerializedName("tiempoMinutos")
    val timeMinutes: Int,
    
    @SerializedName("textoOriginal")
    val originalText: String,
    
    val tags: List<String> = listOf("Cardio"),
    
    @SerializedName("fecha")
    val date: String? = null,
    
    val aiFeedback: AiFeedback? = null
)

interface ApiService {
    @Headers("x-api-key: secreto_deportivo_123")
    @GET("api/users/profile")
    suspend fun getProfile(): Response<Usuario>

    @Headers("x-api-key: secreto_deportivo_123")
    @GET("api/entrenamientos")
    suspend fun getEntrenamientos(): Response<List<Entrenamiento>>

    @Headers("x-api-key: secreto_deportivo_123")
    @POST("api/entrenamientos")
    suspend fun guardarEntrenamiento(@Body entrenamiento: Entrenamiento): Response<Entrenamiento>

    // Nota: El backend actual no incluyó endpoint de DELETE, 
    // se mantiene aquí por si se agrega después.
    @Headers("x-api-key: secreto_deportivo_123")
    @DELETE("api/entrenamientos/{id}")
    suspend fun borrarEntrenamiento(@Path("id") id: String): Response<Unit>
}
