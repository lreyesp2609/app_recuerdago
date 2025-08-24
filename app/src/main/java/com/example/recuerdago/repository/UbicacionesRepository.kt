package com.example.recuerdago.repository

import com.example.recuerdago.data.models.*
import com.example.recuerdago.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class UbicacionesRepository {

    private val api = RetrofitClient.ubicacionesApiService

    suspend fun crearUbicacion(ubicacion: UbicacionUsuarioCreate, token: String) = withContext(Dispatchers.IO) {
        try {
            val response = api.crearUbicacion(ubicacion, "Bearer $token")
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Respuesta vacía"))
            } else {
                Result.failure(Exception("Error al crear ubicación: ${response.code()}"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Error de red: ${e.message}"))
        } catch (e: HttpException) {
            Result.failure(Exception("Error HTTP: ${e.message}"))
        }
    }

    suspend fun listarUbicaciones(token: String) = withContext(Dispatchers.IO) {
        try {
            val response = api.listarUbicaciones("Bearer $token")
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Respuesta vacía"))
            } else {
                Result.failure(Exception("Error al listar ubicaciones: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarUbicacion(id: Int, ubicacion: UbicacionUsuarioUpdate, token: String) = withContext(Dispatchers.IO) {
        try {
            val response = api.actualizarUbicacion(id, ubicacion, "Bearer $token")
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Respuesta vacía"))
            } else {
                Result.failure(Exception("Error al actualizar ubicación: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarUbicacion(id: Int, token: String) = withContext(Dispatchers.IO) {
        try {
            val response = api.eliminarUbicacion(id, "Bearer $token")
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Respuesta vacía"))
            } else {
                Result.failure(Exception("Error al eliminar ubicación: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerUbicacion(id: Int, token: String) = withContext(Dispatchers.IO) {
        try {
            val response = api.obtenerUbicacion(id, "Bearer $token")
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("Ubicación no encontrada"))
            } else {
                Result.failure(Exception("Error al obtener ubicación: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
