package com.tacticaldev.trazabilidad.ui.viewmodel

data class EstadoFormulario(
    val codigoActivo: String = "",
    val errorCodigoActivo: String? = null,
    val tipoActivo: String = "Vitrina freezer",
    val nombreTienda: String = "",
    val errorNombreTienda: String? = null,
    val tipoVisita: String = "Mantenimiento Preventivo",
    val estadoEquipo: String = "Bueno",
    val observaciones: String = "",
    val errorObservaciones: String? = null,
    val uriFoto: String? = null,
    val errorFoto: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val precisionGps: Float? = null,
    val distanciaPuntoVentaMetros: Float? = null,
    val presenciaVerificada: Boolean = false,
    val errorUbicacion: String? = null,
    val cargandoUbicacion: Boolean = false,
    val esFormularioValido: Boolean = false,
    val esGuardadoExitoso: Boolean = false
)
