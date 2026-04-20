package com.example.biometricos.ui.dashboard

import com.example.biometricos.dominios.Entrenamiento
import com.example.biometricos.dominios.UserProfile

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val profile: UserProfile,
        val workouts: List<Entrenamiento>,
        val imc: Double,
        val imcCategory: String
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}