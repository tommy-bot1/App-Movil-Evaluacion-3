package com.tacticaldev.trazabilidad.datos.local.entidad

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "puntos_venta")
data class PuntoVenta(
    @PrimaryKey val id: Long,
    val nombre: String,
    val latitud: Double,
    val longitud: Double
)
