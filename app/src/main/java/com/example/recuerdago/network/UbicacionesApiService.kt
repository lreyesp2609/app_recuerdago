package com.example.recuerdago.network

import com.example.recuerdago.data.models.UbicacionUsuario
import com.example.recuerdago.data.models.UbicacionUsuarioCreate
import com.example.recuerdago.data.models.UbicacionUsuarioUpdate
import retrofit2.Response
import retrofit2.http.*

interface UbicacionesApiService {

    @POST("ubicaciones/")
    suspend fun crearUbicacion(
        @Body ubicacion: UbicacionUsuarioCreate,
        @Header("Authorization") token: String
    ): Response<UbicacionUsuario>

    @GET("ubicaciones/")
    suspend fun listarUbicaciones(
        @Header("Authorization") token: String
    ): Response<List<UbicacionUsuario>>

    @GET("ubicaciones/{id}")
    suspend fun obtenerUbicacion(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<UbicacionUsuario>

    @PUT("ubicaciones/{id}")
    suspend fun actualizarUbicacion(
        @Path("id") id: Int,
        @Body ubicacion: UbicacionUsuarioUpdate,
        @Header("Authorization") token: String
    ): Response<UbicacionUsuario>

    @DELETE("ubicaciones/{id}")
    suspend fun eliminarUbicacion(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<UbicacionUsuario>
}
