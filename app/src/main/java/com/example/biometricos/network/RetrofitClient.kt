package com.example.biometricos.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // REEMPLAZA ESTA URL por la de Render cuando subas tu backend
    // Ejemplo: "https://tu-app-deportiva.onrender.com/"
    private const val BASE_URL = "https://tu-app-en-la-nube.onrender.com/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}
