package com.tacticaldev.trazabilidad.datos.remoto

import com.tacticaldev.trazabilidad.datos.EstadoSync
import com.tacticaldev.trazabilidad.datos.local.entidad.PuntoVenta
import com.tacticaldev.trazabilidad.datos.local.entidad.Visita
import com.tacticaldev.trazabilidad.datos.remoto.dto.PuntoVentaDto
import com.tacticaldev.trazabilidad.datos.remoto.dto.RespuestaClima
import com.tacticaldev.trazabilidad.dominio.clima.Clima
import com.tacticaldev.trazabilidad.dominio.clima.ClimaUtil
import com.tacticaldev.trazabilidad.datos.remoto.dto.VisitaDto

fun Visita.aDto(): VisitaDto = VisitaDto(
    id = idRemoto,
    correo = correo,
    nombreOperario = nombreOperario,
    rolOperario = rolOperario,
    codigoActivo = codigoActivo,
    tipoActivo = tipoActivo,
    nombreTienda = nombreTienda,
    tipoVisita = tipoVisita,
    estadoEquipo = estadoEquipo,
    observaciones = observaciones,
    rutaFoto = rutaFoto.ifBlank { null },
    latitud = latitud,
    longitud = longitud,
    precisionGps = precisionGps,
    distanciaPuntoVentaMetros = distanciaPuntoVentaMetros,
    presenciaVerificada = presenciaVerificada,
    marcaTiempo = marcaTiempo
)


fun VisitaDto.aEntidad(local: Visita? = null): Visita = Visita(
    id = local?.id ?: 0,
    correo = correo,
    nombreOperario = nombreOperario,
    rolOperario = rolOperario,
    codigoActivo = codigoActivo,
    tipoActivo = tipoActivo,
    nombreTienda = nombreTienda,
    tipoVisita = tipoVisita,
    estadoEquipo = estadoEquipo,
    observaciones = observaciones,
    rutaFoto = local?.rutaFoto?.ifBlank { null } ?: (rutaFoto ?: ""),
    latitud = latitud,
    longitud = longitud,
    precisionGps = precisionGps ?: 0f,
    distanciaPuntoVentaMetros = distanciaPuntoVentaMetros ?: 0f,
    presenciaVerificada = presenciaVerificada,
    estadoSincronizacion = EstadoSync.SINCRONIZADO,
    idRemoto = id,
    marcaTiempo = marcaTiempo ?: System.currentTimeMillis()
)

fun PuntoVentaDto.aEntidad(): PuntoVenta = PuntoVenta(id = id, nombre = nombre, latitud = latitud, longitud = longitud)

fun RespuestaClima.aClima(): Clima? {
    val actual = current ?: return null
    val temperatura = actual.temperatura ?: return null
    return Clima(
        temperaturaC = temperatura,
        humedadPct = actual.humedad,
        vientoKmh = actual.viento,
        descripcion = ClimaUtil.descripcion(actual.codigoClima),
        alerta = ClimaUtil.alertaTermica(temperatura)
    )
}
