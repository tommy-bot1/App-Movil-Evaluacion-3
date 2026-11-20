package com.tacticaldev.trazabilidad.datos.remoto

import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException


fun Throwable.mensajeAmigable(): String = when (this) {
    is HttpException -> mensajeDelServidor(this) ?: when (code()) {
        401 -> "Credenciales inválidas"
        404 -> "No encontrado en el servidor"
        in 500..599 -> "Error interno del servidor"
        else -> "Error HTTP ${code()}"
    }
    is IOException -> "Sin conexión con el servidor. Verifica que esté encendido y que estés en la misma red."
    else -> message ?: "Error inesperado"
}

private fun mensajeDelServidor(e: HttpException): String? = try {
    val cuerpo = e.response()?.errorBody()?.string()
    JsonParser.parseString(cuerpo).asJsonObject.get("mensaje")?.asString
} catch (_: Exception) {
    null
}
