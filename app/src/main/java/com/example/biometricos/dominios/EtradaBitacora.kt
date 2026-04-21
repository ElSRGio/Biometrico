package com.example.biometricos.dominios

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val weightKg: Double,
    val heightCm: Int,
    val avatarUrl: String
)

data class AiFeedback(
    val emoji: String,
    val shortMessage: String
)

data class Entrenamiento(
    @SerializedName("_id")
    val id: String? = null,
    val type: String = "RUNNING",
    val distanciaKm: Double,
    val tiempoMinutos: Int,
    val tags: List<String> = emptyList(),
    val originalText: String,
    val aiFeedback: AiFeedback? = null,
    val fecha: String? = null
)
