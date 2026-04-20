package com.example.biometricos.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.biometricos.BuildConfig

object RetrofitClient {
    private val BASE_URL = BuildConfig.BASE_URL

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}
