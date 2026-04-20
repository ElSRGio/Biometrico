package com.example.biometricos.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.biometricos.BuildConfig
import android.util.Log

object RetrofitClient {
    // Obtenemos la URL y nos aseguramos de que termine en / para evitar errores de Retrofit
    private val BASE_URL: String = if (BuildConfig.BASE_URL.endsWith("/")) {
        BuildConfig.BASE_URL
    } else {
        "${BuildConfig.BASE_URL}/"
    }

    val instance: ApiService by lazy {
        Log.d("RETROFIT", "Conectando a: $BASE_URL")
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}
