package com.tacticaldev.trazabilidad.datos

object EstadoSync {
    const val PENDIENTE = "PENDIENTE"         // guardada en el celular, aún no enviada
    const val SINCRONIZADO = "SINCRONIZADO"   // existe igual en el servidor
    const val RECHAZADO = "RECHAZADO"         // el servidor la rechazó (datos inválidos); editarla reintenta
}
