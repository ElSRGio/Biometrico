package com.example.biometricos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

// Modelo de datos estándar para el Backend de Node.js
data class Entrenamiento(
    val _id: String? = null,
    val distanciaKm: Double,
    val tiempoMinutos: Int,
    val textoOriginal: String
)

interface ApiService {
    // RNF01: Seguridad mediante API Key definida en el backend
    @Headers("x-api-key: secreto_deportivo_123")
    @GET("api/entrenamientos")
    suspend fun getEntrenamientos(): Response<List<Entrenamiento>>

    @Headers("x-api-key: secreto_deportivo_123")
    @POST("api/entrenamientos")
    suspend fun guardarEntrenamiento(@Body entrenamiento: Entrenamiento): Response<Entrenamiento>

    @Headers("x-api-key: secreto_deportivo_123")
    @DELETE("api/entrenamientos/{id}")
    suspend fun borrarEntrenamiento(@Path("id") id: String): Response<Unit>
}
