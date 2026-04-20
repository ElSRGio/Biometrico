package com.example.biometricos.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biometricos.dominios.AiFeedback
import com.example.biometricos.dominios.Entrenamiento
import com.example.biometricos.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class FocusUiState {
    object Idle : FocusUiState()
    object Recording : FocusUiState()
    object Processing : FocusUiState()
    data class Success(val feedback: AiFeedback) : FocusUiState()
    data class Error(val message: String) : FocusUiState()
}

class FocusViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<FocusUiState>(FocusUiState.Idle)
    val uiState: StateFlow<FocusUiState> = _uiState

    private val API_KEY = "secreto_deportivo_123"

    fun setRecording(isRecording: Boolean) {
        _uiState.value = if (isRecording) FocusUiState.Recording else FocusUiState.Idle
    }

    fun enviarMetricas(distancia: Double, tiempo: Int, texto: String) {
        viewModelScope.launch {
            _uiState.value = FocusUiState.Processing
            try {
                val request = Entrenamiento(
                    distanciaKm = distancia,
                    tiempoMinutos = tiempo,
                    originalText = texto,
                    type = "RUNNING",
                    tags = listOf("Manos Libres", "Voz")
                )
                val response = RetrofitClient.instance.guardarEntrenamiento(API_KEY, request)
                if (response.isSuccessful && response.body()?.aiFeedback != null) {
                    _uiState.value = FocusUiState.Success(response.body()!!.aiFeedback!!)
                } else {
                    _uiState.value = FocusUiState.Error("Error al procesar entrenamiento")
                }
            } catch (e: Exception) {
                _uiState.value = FocusUiState.Error(e.message ?: "Error de red")
            }
        }
    }
    
    fun reset() {
        _uiState.value = FocusUiState.Idle
    }
}