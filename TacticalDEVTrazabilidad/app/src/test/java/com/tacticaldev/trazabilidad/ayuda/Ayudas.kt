package com.tacticaldev.trazabilidad.ayuda

import com.tacticaldev.trazabilidad.datos.EstadoSync
import com.tacticaldev.trazabilidad.datos.local.dao.PuntoVentaDao
import com.tacticaldev.trazabilidad.datos.local.dao.VisitaDao
import com.tacticaldev.trazabilidad.datos.local.entidad.PuntoVenta
import com.tacticaldev.trazabilidad.datos.local.entidad.Visita
import com.tacticaldev.trazabilidad.datos.remoto.ApiClima
import com.tacticaldev.trazabilidad.datos.remoto.ApiUsuarios
import com.tacticaldev.trazabilidad.datos.remoto.ApiVisitas
import com.tacticaldev.trazabilidad.datos.remoto.dto.VisitaDto
import com.tacticaldev.trazabilidad.datos.repositorio.Repositorio
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ReglaDispatcherPrincipal(private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}

class Mundo(
    val daoVisita: VisitaDao,
    val daoPunto: PuntoVentaDao,
    val apiVisitas: ApiVisitas,
    val apiUsuarios: ApiUsuarios,
    val apiClima: ApiClima,
    val repositorio: Repositorio
)

fun crearMundo(
    puntos: List<PuntoVenta> = emptyList(),
    visitas: List<Visita> = emptyList(),
    total: Int = 0,
    fallas: Int = 0,
    pendientes: Int = 0
): Mundo {
    val dao = mockk<VisitaDao>()
    every { dao.obtenerTodasLasVisitas() } returns flowOf(visitas)
    every { dao.obtenerTotalVisitas() } returns flowOf(total)
    every { dao.obtenerTotalFallasCriticas() } returns flowOf(fallas)
    every { dao.obtenerTotalPendientes() } returns flowOf(pendientes)
    every { dao.observarPorId(any()) } returns flowOf(null)
    coEvery { dao.insertarVisita(any()) } returns 1L
    coEvery { dao.actualizarVisita(any()) } just Runs
    coEvery { dao.eliminarPorId(any()) } just Runs

    val daoPunto = mockk<PuntoVentaDao>()
    every { daoPunto.observarTodos() } returns flowOf(puntos)
    coEvery { daoPunto.reemplazarTodos(any()) } just Runs

    val apiV = mockk<ApiVisitas>()
    val apiU = mockk<ApiUsuarios>()
    val apiC = mockk<ApiClima>()
    // Dispatchers.Unconfined: todo corre de forma síncrona y determinista dentro de la prueba.
    val repo = Repositorio(dao, daoPunto, apiV, apiU, apiC, Dispatchers.Unconfined)
    return Mundo(dao, daoPunto, apiV, apiU, apiC, repo)
}

fun Mundo.stubSincronizacionVacia() {
    coEvery { daoVisita.obtenerPendientes() } returns emptyList()
    coEvery { daoVisita.obtenerSincronizadas() } returns emptyList()
    coEvery { apiVisitas.listar() } returns emptyList()
    coEvery { apiUsuarios.puntosVenta() } returns emptyList()
}

fun visita(
    id: Long = 1,
    codigo: String = "VTR-1020",
    estado: String = EstadoSync.PENDIENTE,
    idRemoto: Long? = null,
    foto: String = "content://foto/1"
) = Visita(
    id = id, correo = "ana@tecnico.com", nombreOperario = "Ana", rolOperario = "Técnico",
    codigoActivo = codigo, tipoActivo = "Vitrina freezer", nombreTienda = "Tienda Central",
    tipoVisita = "Inspección", estadoEquipo = "Bueno", observaciones = "Equipo limpio", rutaFoto = foto,
    latitud = -33.4489, longitud = -70.6693, precisionGps = 12f, distanciaPuntoVentaMetros = 30f,
    presenciaVerificada = true, estadoSincronizacion = estado, idRemoto = idRemoto, marcaTiempo = 1_700_000_000_000L
)

fun dtoRemoto(id: Long? = 99, codigo: String = "VTR-1020") = VisitaDto(
    id = id, correo = "ana@tecnico.com", nombreOperario = "Ana", rolOperario = "Técnico",
    codigoActivo = codigo, tipoActivo = "Vitrina freezer", nombreTienda = "Tienda Central",
    tipoVisita = "Inspección", estadoEquipo = "Bueno", observaciones = "Equipo limpio", rutaFoto = null,
    latitud = -33.4489, longitud = -70.6693, precisionGps = 12f, distanciaPuntoVentaMetros = 30f,
    presenciaVerificada = true, marcaTiempo = 1_700_000_000_000L
)

/** Error HTTP como lo lanzaría Retrofit (4xx/5xx) con el cuerpo JSON de Spring. */
fun errorHttp(codigo: Int, cuerpo: String = ""): HttpException =
    HttpException(Response.error<Any>(codigo, cuerpo.toResponseBody("application/json".toMediaType())))
