package com.tacticaldev.trazabilidad.datos.local.entidad

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visitas")
data class Visita(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "rutOperario") val correo: String = "",
    val nombreOperario: String = "Operario",
    val rolOperario: String = "Técnico",
    val codigoActivo: String,
    val tipoActivo: String = "Vitrina freezer",
    val nombreTienda: String,
    val tipoVisita: String,
    val estadoEquipo: String,
    val observaciones: String,
    val rutaFoto: String,
    val latitud: Double,
    val longitud: Double,
    val precisionGps: Float = 0f,
    val distanciaPuntoVentaMetros: Float = 0f,
    val presenciaVerificada: Boolean = false,
    val estadoSincronizacion: String = "PENDIENTE",
    val idRemoto: Long? = null,
    val marcaTiempo: Long = System.currentTimeMillis()
)
