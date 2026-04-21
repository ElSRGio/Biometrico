package com.example.biometricos.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log

object RetrofitClient {
    // URL de producción en Render
    private const val BASE_URL = "https://backend-deportivo-sergio.onrender.com/"

    val instance: ApiService by lazy {
        Log.d("RETROFIT", "Conectando a: $BASE_URL")
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}
