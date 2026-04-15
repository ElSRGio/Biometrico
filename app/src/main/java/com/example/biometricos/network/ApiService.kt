package com.example.biometricos.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class EntrenamientoRequest(
    val distanciaKm: Double,
    val tiempoMinutos: Int,
    val textoOriginal: String
)

interface ApiService {
    @POST("api/entrenamientos")
    suspend fun guardarEntrenamiento(
        @Header("x-api-key") apiKey: String,
        @Body request: EntrenamientoRequest
    ): Response<Unit>
}
