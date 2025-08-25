package com.example.recuerdago.screens.rutas.views

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recuerdago.data.models.UbicacionUsuarioCreate
import com.example.recuerdago.data.models.UbicacionUsuarioResponse
import com.example.recuerdago.repository.UbicacionesRepository
import kotlinx.coroutines.launch

class UbicacionesViewModel(private val token: String) : ViewModel() {

    private val repository = UbicacionesRepository()

    var ubicaciones by mutableStateOf<List<UbicacionUsuarioResponse>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun crearUbicacion(ubicacion: UbicacionUsuarioCreate, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            repository.crearUbicacion(token, ubicacion).fold(onSuccess = {
                ubicaciones = ubicaciones + it
                isLoading = false
                onSuccess()
            }, onFailure = {
                errorMessage = it.message
                isLoading = false
            })
        }
    }

    fun cargarUbicaciones() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            repository.obtenerUbicaciones(token).fold(onSuccess = {
                ubicaciones = it
                isLoading = false
            }, onFailure = {
                errorMessage = it.message
                isLoading = false
            })
        }
    }
}
