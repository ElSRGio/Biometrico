package com.example.biometricos.dominios

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val weightKg: Double,
    val heightCm: Int,
    val avatarUrl: String
)

@Serializable
data class AiFeedback(
    val emoji: String,
    val shortMessage: String
)

@Serializable
data class Entrenamiento(
    val id: String? = null,
    val type: String = "RUNNING",
    val distanciaKm: Double,
    val tiempoMinutos: Int,
    val tags: List<String> = emptyList(),
    val originalText: String,
    val aiFeedback: AiFeedback? = null,
    val fecha: String? = null
)