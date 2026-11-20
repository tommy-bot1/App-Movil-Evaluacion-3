package com.tacticaldev.trazabilidad.utilidades

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object Geocerca {
    const val RADIO_PERMITIDO_METROS = 250f
    const val PRECISION_MAXIMA_METROS = 100f
    private const val RADIO_TIERRA_METROS = 6_371_000.0

    fun distanciaMetros(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return (2 * RADIO_TIERRA_METROS * asin(sqrt(a))).toFloat()
    }

    fun evaluar(latPunto: Double?, lonPunto: Double?, ubicacion: UbicacionGps): Pair<Boolean, Float?> {
        if (latPunto == null || lonPunto == null) return Pair(false, null)
        val distancia = distanciaMetros(ubicacion.latitud, ubicacion.longitud, latPunto, lonPunto)
        return Pair(distancia <= RADIO_PERMITIDO_METROS, distancia)
    }
}
