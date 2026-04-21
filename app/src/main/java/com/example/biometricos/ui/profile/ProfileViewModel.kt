package com.example.biometricos.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biometricos.dominios.UserProfile
import com.example.biometricos.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val profile: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
    object Saved : ProfileUiState()
}

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val API_KEY = "secreto_deportivo_123"

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val res = RetrofitClient.instance.getProfile(API_KEY)
                if (res.isSuccessful && res.body() != null) {
                    _uiState.value = ProfileUiState.Success(res.body()!!)
                } else {
                    _uiState.value = ProfileUiState.Error("Error al cargar perfil")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Error de red")
            }
        }
    }

    fun saveProfile(name: String, weight: Double, height: Int) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val current = (uiState.value as? ProfileUiState.Success)?.profile
                val updated = UserProfile(
                    id = current?.id ?: "",
                    name = name,
                    weightKg = weight,
                    heightCm = height,
                    avatarUrl = current?.avatarUrl ?: ""
                )
                val res = RetrofitClient.instance.updateProfile(API_KEY, updated)
                if (res.isSuccessful) {
                    _uiState.value = ProfileUiState.Saved
                } else {
                    _uiState.value = ProfileUiState.Error("Error al guardar perfil")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Error de red")
            }
        }
    }
}
