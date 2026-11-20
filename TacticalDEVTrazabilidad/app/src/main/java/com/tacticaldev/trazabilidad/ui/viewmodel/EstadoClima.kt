package com.tacticaldev.trazabilidad.ui.viewmodel

import com.tacticaldev.trazabilidad.dominio.clima.Clima

sealed interface EstadoClima {
    data object Inactivo : EstadoClima
    data object Cargando : EstadoClima
    data class Listo(val clima: Clima) : EstadoClima
    data class Error(val mensaje: String) : EstadoClima
}
