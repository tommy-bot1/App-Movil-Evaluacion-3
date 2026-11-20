package com.tacticaldev.trazabilidad.dominio.validacion

data class Resultado(
    val esValido: Boolean,
    val mensajeError: String? = null
)

object Validador {
    fun validarCodigoActivo(codigo: String): Resultado {
        if (codigo.isBlank()) return Resultado(false, "El código del activo es obligatorio")
        if (!codigo.trim().uppercase().matches(Regex("^[A-Z]{3}-\\d{3,6}$"))) {
            return Resultado(false, "Formato inválido. Ejemplo: VTR-1020")
        }
        return Resultado(true)
    }

    fun validarNombreTienda(tienda: String): Resultado {
        if (tienda.isBlank()) return Resultado(false, "El punto de venta es obligatorio")
        return Resultado(true)
    }

    fun validarObservaciones(observacion: String): Resultado {
        if (observacion.isBlank()) return Resultado(false, "Debe agregar una observación técnica")
        return Resultado(true)
    }

    fun validarFoto(uriFoto: String?): Resultado {
        if (uriFoto.isNullOrEmpty()) return Resultado(false, "Es obligatoria la evidencia fotográfica")
        return Resultado(true)
    }

    fun validarUbicacion(latitud: Double?, longitud: Double?, presenciaVerificada: Boolean): Resultado {
        if (latitud == null || longitud == null) return Resultado(false, "Se requiere ubicación GPS")
        if (!presenciaVerificada) return Resultado(false, "La presencia física no está verificada. Debes estar dentro del punto de venta.")
        return Resultado(true)
    }
}
