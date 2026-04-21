package com.example.biometricos.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biometricos.dominios.Entrenamiento
import com.example.biometricos.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.pow

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    private val API_KEY = "secreto_deportivo_123"

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                val profileRes = RetrofitClient.instance.getProfile(API_KEY)
                val workoutsRes = RetrofitClient.instance.obtenerEntrenamientos(API_KEY)

                if (profileRes.isSuccessful && workoutsRes.isSuccessful) {
                    val profile = profileRes.body()!!
                    val workouts = workoutsRes.body()!!
                    
                    val imc = profile.weightKg / (profile.heightCm / 100.0).pow(2.0)
                    val category = when {
                        imc < 18.5 -> "Bajo peso"
                        imc < 25.0 -> "Normal"
                        imc < 30.0 -> "Sobrepeso"
                        else -> "Obesidad"
                    }

                    _uiState.value = DashboardUiState.Success(profile, workouts, imc, category)
                } else {
                    _uiState.value = DashboardUiState.Error("Error al cargar datos del servidor")
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun eliminarEntrenamiento(id: String) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.instance.eliminarEntrenamiento(API_KEY, id)
                if (res.isSuccessful) {
                    loadData()
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error("No se pudo eliminar: ${e.message}")
            }
        }
    }

    fun actualizarEntrenamiento(id: String, distancia: Double, tiempo: Int) {
        viewModelScope.launch {
            try {
                // Buscamos el entrenamiento actual para mantener los otros campos
                val currentWorkouts = (uiState.value as? DashboardUiState.Success)?.workouts
                val current = currentWorkouts?.find { it.id == id }
                
                if (current != null) {
                    val updated = current.copy(distanciaKm = distancia, tiempoMinutos = tiempo)
                    val res = RetrofitClient.instance.actualizarEntrenamiento(API_KEY, id, updated)
                    if (res.isSuccessful) {
                        loadData()
                    } else {
                        _uiState.value = DashboardUiState.Error("Error al actualizar en el servidor")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error("Error de red al actualizar")
            }
        }
    }
}
