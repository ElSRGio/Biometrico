package com.example.biometricos.network

import com.example.biometricos.dominios.Entrenamiento
import com.example.biometricos.dominios.UserProfile
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("api/users/profile")
    suspend fun getProfile(
        @Header("x-api-key") apiKey: String
    ): Response<UserProfile>

    @POST("api/users/profile")
    suspend fun updateProfile(
        @Header("x-api-key") apiKey: String,
        @Body profile: UserProfile
    ): Response<UserProfile>

    @POST("api/entrenamientos")
    suspend fun guardarEntrenamiento(
        @Header("x-api-key") apiKey: String,
        @Body entrenamiento: Entrenamiento
    ): Response<Entrenamiento>

    @PUT("api/entrenamientos/{id}")
    suspend fun actualizarEntrenamiento(
        @Header("x-api-key") apiKey: String,
        @Path("id") id: String,
        @Body entrenamiento: Entrenamiento
    ): Response<Entrenamiento>

    @GET("api/entrenamientos")
    suspend fun obtenerEntrenamientos(
        @Header("x-api-key") apiKey: String
    ): Response<List<Entrenamiento>>
    
    @DELETE("api/entrenamientos/{id}")
    suspend fun eliminarEntrenamiento(
        @Header("x-api-key") apiKey: String,
        @Path("id") id: String
    ): Response<Unit>
}
