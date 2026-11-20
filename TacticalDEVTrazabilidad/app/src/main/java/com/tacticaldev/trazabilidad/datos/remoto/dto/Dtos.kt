package com.tacticaldev.trazabilidad.datos.remoto.dto

/**
 * DTOs = "Data Transfer Objects": clases que representan el JSON que viaja por la red.
 * Gson convierte JSON <-> estas clases usando el NOMBRE de cada propiedad, por eso deben
 * coincidir exactamente con los campos que devuelve/espera el microservicio.
 */
data class LoginRequest(val correo: String, val clave: String)

data class UsuarioDto(val id: Long, val correo: String, val nombre: String, val rol: String)

data class PuntoVentaDto(val id: Long, val nombre: String, val latitud: Double, val longitud: Double)

data class VisitaDto(
    val id: Long? = null,
    val correo: String,
    val nombreOperario: String,
    val rolOperario: String,
    val codigoActivo: String,
    val tipoActivo: String,
    val nombreTienda: String,
    val tipoVisita: String,
    val estadoEquipo: String,
    val observaciones: String,
    val rutaFoto: String? = null,
    val latitud: Double,
    val longitud: Double,
    val precisionGps: Float? = null,
    val distanciaPuntoVentaMetros: Float? = null,
    val presenciaVerificada: Boolean,
    val marcaTiempo: Long? = null
)
