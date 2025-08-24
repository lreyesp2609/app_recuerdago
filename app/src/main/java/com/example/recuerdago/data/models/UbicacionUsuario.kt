package com.example.recuerdago.data.models

data class UbicacionUsuario(
    val id: Int,
    val usuario_id: Int,
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val direccion_completa: String,
    val activo: Boolean = true
)

data class UbicacionUsuarioCreate(
    val nombre: String,
    val latitud: Double,
    val longitud: Double,
    val direccion_completa: String
)

data class UbicacionUsuarioUpdate(
    val nombre: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val direccion_completa: String? = null
)
