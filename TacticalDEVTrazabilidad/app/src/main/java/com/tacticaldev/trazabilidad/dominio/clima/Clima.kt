package com.tacticaldev.trazabilidad.dominio.clima

data class Clima(
    val temperaturaC: Double,
    val humedadPct: Int?,
    val vientoKmh: Double?,
    val descripcion: String,
    val alerta: String?
)

object ClimaUtil {
    const val TEMPERATURA_ALTA_C = 30.0

    fun descripcion(codigo: Int?): String = when (codigo) {
        0 -> "Despejado"
        1 -> "Mayormente despejado"
        2 -> "Parcialmente nublado"
        3 -> "Nublado"
        45, 48 -> "Niebla"
        51, 53, 55 -> "Llovizna"
        56, 57 -> "Llovizna helada"
        61, 63, 65 -> "Lluvia"
        66, 67 -> "Lluvia helada"
        71, 73, 75, 77 -> "Nieve"
        80, 81, 82 -> "Chubascos"
        85, 86 -> "Chubascos de nieve"
        95 -> "Tormenta"
        96, 99 -> "Tormenta con granizo"
        else -> "Sin información"
    }

    fun alertaTermica(temperaturaC: Double): String? =
        if (temperaturaC >= TEMPERATURA_ALTA_C) {
            "Temperatura ambiente alta: los equipos de frío trabajan con más carga. Revisa condensador y sellos."
        } else {
            null
        }
}
