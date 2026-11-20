package com.tacticaldev.trazabilidad.datos.repositorio

import com.tacticaldev.trazabilidad.datos.EstadoSync
import com.tacticaldev.trazabilidad.datos.local.dao.PuntoVentaDao
import com.tacticaldev.trazabilidad.datos.local.dao.VisitaDao
import com.tacticaldev.trazabilidad.datos.local.entidad.PuntoVenta
import com.tacticaldev.trazabilidad.datos.local.entidad.Visita
import com.tacticaldev.trazabilidad.datos.remoto.ApiClima
import com.tacticaldev.trazabilidad.datos.remoto.ApiUsuarios
import com.tacticaldev.trazabilidad.datos.remoto.ApiVisitas
import com.tacticaldev.trazabilidad.datos.remoto.aDto
import com.tacticaldev.trazabilidad.datos.remoto.aClima
import com.tacticaldev.trazabilidad.datos.remoto.aEntidad
import com.tacticaldev.trazabilidad.datos.remoto.dto.LoginRequest
import com.tacticaldev.trazabilidad.datos.remoto.dto.UsuarioDto
import com.tacticaldev.trazabilidad.datos.remoto.dto.VisitaDto
import com.tacticaldev.trazabilidad.datos.remoto.mensajeAmigable
import com.tacticaldev.trazabilidad.dominio.clima.Clima
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

data class ResultadoSync(
    val sincronizada: Boolean,
    val mensaje: String,
    val sinConexion: Boolean = false
)

class Repositorio(
    private val daoVisita: VisitaDao,
    private val daoPuntoVenta: PuntoVentaDao,
    private val apiVisitas: ApiVisitas,
    private val apiUsuarios: ApiUsuarios,
    private val apiClima: ApiClima,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val todasLasVisitas: Flow<List<Visita>> = daoVisita.obtenerTodasLasVisitas()
    val cantidadTotal: Flow<Int> = daoVisita.obtenerTotalVisitas()
    val cantidadFallas: Flow<Int> = daoVisita.obtenerTotalFallasCriticas()
    val cantidadPendientes: Flow<Int> = daoVisita.obtenerTotalPendientes()
    val puntosVenta: Flow<List<PuntoVenta>> = daoPuntoVenta.observarTodos()

    fun observarVisita(id: Long): Flow<Visita?> = daoVisita.observarPorId(id)

    suspend fun iniciarSesion(correo: String, clave: String): Result<UsuarioDto> = withContext(dispatcher) {
        try {
            Result.success(apiUsuarios.login(LoginRequest(correo, clave)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun obtenerClima(latitud: Double, longitud: Double): Result<Clima> = withContext(dispatcher) {
        try {
            val respuesta = apiClima.climaActual(latitud, longitud, VARIABLES_CLIMA, "auto")
            val clima = respuesta.aClima()
                ?: return@withContext Result.failure(IllegalStateException("El servicio de clima no entregó datos"))
            Result.success(clima)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun guardarVisita(visita: Visita): ResultadoSync {
        val idLocal = withContext(dispatcher) {
            daoVisita.insertarVisita(visita.copy(estadoSincronizacion = EstadoSync.PENDIENTE, idRemoto = null))
        }
        return enviar(idLocal)
    }

    suspend fun actualizarVisita(visita: Visita): ResultadoSync {
        withContext(dispatcher) {
            daoVisita.actualizarVisita(visita.copy(estadoSincronizacion = EstadoSync.PENDIENTE))
        }
        return enviar(visita.id)
    }


    suspend fun eliminarVisita(visita: Visita): Result<Unit> = withContext(dispatcher) {
        try {
            val idRemoto = visita.idRemoto
            if (idRemoto != null) {
                val respuesta = apiVisitas.eliminar(idRemoto)
                if (!respuesta.isSuccessful && respuesta.code() != 404) throw HttpException(respuesta)
            }
            daoVisita.eliminarPorId(visita.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sincronizar(): ResultadoSync = withContext(dispatcher) {
        var enviadas = 0
        var fallidas = 0
        for (v in daoVisita.obtenerPendientes()) {
            val r = enviar(v.id)
            if (r.sincronizada) {
                enviadas++
            } else {
                fallidas++
                if (r.sinConexion) {
                    return@withContext ResultadoSync(
                        false,
                        "Sin conexión con el servidor. Los datos siguen guardados en el dispositivo.",
                        sinConexion = true
                    )
                }
            }
        }
        try {
            refrescarDesdeServidor()
            daoPuntoVenta.reemplazarTodos(apiUsuarios.puntosVenta().map { it.aEntidad() })
            val detalle = if (fallidas > 0) ", $fallidas con problemas" else ""
            ResultadoSync(fallidas == 0, "Sincronización completa: $enviadas enviada(s)$detalle")
        } catch (e: Exception) {
            ResultadoSync(false, e.mensajeAmigable(), sinConexion = e is IOException)
        }
    }

    private suspend fun enviar(idLocal: Long): ResultadoSync = withContext(dispatcher) {
        val v = daoVisita.obtenerPorId(idLocal)
            ?: return@withContext ResultadoSync(false, "La visita ya no existe en el dispositivo")
        try {
            val remota = enviarAlServidor(v)
            daoVisita.actualizarVisita(v.copy(idRemoto = remota.id, estadoSincronizacion = EstadoSync.SINCRONIZADO))
            ResultadoSync(true, "Visita ${v.codigoActivo} sincronizada con el servidor")
        } catch (e: HttpException) {
            if (e.code() == 400 || e.code() == 409 || e.code() == 422) {
                // El servidor entendió la petición pero NO la acepta: reintentar igual no sirve.
                daoVisita.actualizarVisita(v.copy(estadoSincronizacion = EstadoSync.RECHAZADO))
                ResultadoSync(false, "El servidor rechazó la visita: ${e.mensajeAmigable()}")
            } else {
                ResultadoSync(false, "Guardada en el dispositivo. ${e.mensajeAmigable()} (queda PENDIENTE)")
            }
        } catch (e: IOException) {
            ResultadoSync(false, "Guardada en el dispositivo. Sin conexión: queda PENDIENTE de sincronizar", sinConexion = true)
        } catch (e: Exception) {
            ResultadoSync(false, "Guardada en el dispositivo. ${e.mensajeAmigable()}")
        }
    }

    private suspend fun enviarAlServidor(v: Visita): VisitaDto {
        val dto = v.aDto()
        val idRemoto = v.idRemoto ?: return apiVisitas.crear(dto)
        return try {
            apiVisitas.actualizar(idRemoto, dto)
        } catch (e: HttpException) {
            if (e.code() == 404) apiVisitas.crear(dto.copy(id = null)) else throw e
        }
    }

    private suspend fun refrescarDesdeServidor() {
        val remotas = apiVisitas.listar()
        val idsRemotos = HashSet<Long>()
        for (dto in remotas) {
            val idRemoto = dto.id ?: continue
            idsRemotos.add(idRemoto)
            val local = daoVisita.obtenerPorIdRemoto(idRemoto)
            when {
                local == null -> daoVisita.insertarVisita(dto.aEntidad())
                local.estadoSincronizacion == EstadoSync.SINCRONIZADO -> daoVisita.actualizarVisita(dto.aEntidad(local))
            }
        }
        // Visitas ya sincronizadas que alguien eliminó en el servidor: se quitan también aquí.
        for (local in daoVisita.obtenerSincronizadas()) {
            if (local.idRemoto !in idsRemotos) daoVisita.eliminarPorId(local.id)
        }
    }

    private companion object {
        const val VARIABLES_CLIMA = "temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m"
    }
}
