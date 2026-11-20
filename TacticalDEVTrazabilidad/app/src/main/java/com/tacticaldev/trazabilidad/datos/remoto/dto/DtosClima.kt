package com.tacticaldev.trazabilidad.datos.remoto.dto

import com.google.gson.annotations.SerializedName

/**
 * Respuesta de la API externa Open-Meteo (GET /v1/forecast?...&current=...).
 * Ejemplo: {"current": {"time": "...", "temperature_2m": 21.4, "relative_humidity_2m": 48, "weather_code": 1, "wind_speed_10m": 9.7}}
 * Los nombres del JSON usan guion bajo; @SerializedName los une con nombres Kotlin. Todo es nullable
 * porque no controlamos ese servidor: si falta un dato, la app no debe caerse.
 */
data class RespuestaClima(val current: ClimaActual? = null)

data class ClimaActual(
    val time: String? = null,
    @SerializedName("temperature_2m") val temperatura: Double? = null,
    @SerializedName("relative_humidity_2m") val humedad: Int? = null,
    @SerializedName("weather_code") val codigoClima: Int? = null,
    @SerializedName("wind_speed_10m") val viento: Double? = null
)
