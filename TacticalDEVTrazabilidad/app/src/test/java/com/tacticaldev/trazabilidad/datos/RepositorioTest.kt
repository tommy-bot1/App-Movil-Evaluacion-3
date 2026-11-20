package com.tacticaldev.trazabilidad.datos

import com.tacticaldev.trazabilidad.ayuda.Mundo
import com.tacticaldev.trazabilidad.ayuda.crearMundo
import com.tacticaldev.trazabilidad.ayuda.dtoRemoto
import com.tacticaldev.trazabilidad.ayuda.errorHttp
import com.tacticaldev.trazabilidad.ayuda.stubSincronizacionVacia
import com.tacticaldev.trazabilidad.ayuda.visita
import com.tacticaldev.trazabilidad.datos.remoto.dto.ClimaActual
import com.tacticaldev.trazabilidad.datos.remoto.dto.PuntoVentaDto
import com.tacticaldev.trazabilidad.datos.remoto.dto.RespuestaClima
import com.tacticaldev.trazabilidad.datos.remoto.dto.UsuarioDto
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class RepositorioTest {
    private lateinit var m: Mundo

    @Before fun preparar() {
        m = crearMundo()
    }

    @Test fun `guardarVisita envia al servidor y guarda el id remoto`() = runTest {
        coEvery { m.daoVisita.insertarVisita(any()) } returns 7L
        coEvery { m.daoVisita.obtenerPorId(7L) } returns visita(id = 7)
        coEvery { m.apiVisitas.crear(any()) } returns dtoRemoto(id = 99)

        val r = m.repositorio.guardarVisita(visita())

        assertTrue(r.sincronizada)
        assertFalse(r.sinConexion)
        coVerify { m.daoVisita.actualizarVisita(match { it.idRemoto == 99L && it.estadoSincronizacion == EstadoSync.SINCRONIZADO }) }
    }

    @Test fun `guardarVisita siempre inserta primero como PENDIENTE sin idRemoto`() = runTest {
        coEvery { m.daoVisita.obtenerPorId(1L) } returns visita()
        coEvery { m.apiVisitas.crear(any()) } returns dtoRemoto()

        m.repositorio.guardarVisita(visita(estado = EstadoSync.SINCRONIZADO, idRemoto = 5))

        coVerify { m.daoVisita.insertarVisita(match { it.estadoSincronizacion == EstadoSync.PENDIENTE && it.idRemoto == null }) }
    }

    @Test fun `sin conexion la visita queda guardada y PENDIENTE`() = runTest {
        coEvery { m.daoVisita.obtenerPorId(1L) } returns visita()
        coEvery { m.apiVisitas.crear(any()) } throws IOException("sin red")

        val r = m.repositorio.guardarVisita(visita())

        assertFalse(r.sincronizada)
        assertTrue(r.sinConexion)
        assertTrue(r.mensaje.contains("PENDIENTE"))
        coVerify(exactly = 0) { m.daoVisita.actualizarVisita(any()) }
    }

    @Test fun `el servidor rechaza datos invalidos y la visita pasa a RECHAZADO`() = runTest {
        coEvery { m.daoVisita.obtenerPorId(1L) } returns visita()
        coEvery { m.apiVisitas.crear(any()) } throws errorHttp(422, """{"mensaje":"Presencia no verificada"}""")

        val r = m.repositorio.guardarVisita(visita())

        assertFalse(r.sincronizada)
        assertFalse(r.sinConexion)
        assertTrue(r.mensaje.contains("Presencia no verificada"))
        coVerify { m.daoVisita.actualizarVisita(match { it.estadoSincronizacion == EstadoSync.RECHAZADO }) }
    }

    @Test fun `error 500 deja la visita PENDIENTE para reintentar`() = runTest {
        coEvery { m.daoVisita.obtenerPorId(1L) } returns visita()
        coEvery { m.apiVisitas.crear(any()) } throws errorHttp(500)

        val r = m.repositorio.guardarVisita(visita())

        assertFalse(r.sincronizada)
        assertFalse(r.sinConexion)
        assertTrue(r.mensaje.contains("PENDIENTE"))
        coVerify(exactly = 0) { m.daoVisita.actualizarVisita(any()) }
    }

    @Test fun `una excepcion inesperada no tumba la app`() = runTest {
        coEvery { m.daoVisita.obtenerPorId(1L) } returns visita()
        coEvery { m.apiVisitas.crear(any()) } throws IllegalStateException("boom")

        val r = m.repositorio.guardarVisita(visita())

        assertFalse(r.sincronizada)
        assertTrue(r.mensaje.contains("boom"))
    }

    @Test fun `si la visita desaparece localmente no se envia`() = runTest {
        coEvery { m.daoVisita.obtenerPorId(any()) } returns null
        val r = m.repositorio.guardarVisita(visita())
        assertFalse(r.sincronizada)
        coVerify(exactly = 0) { m.apiVisitas.crear(any()) }
    }

    @Test fun `actualizarVisita con idRemoto usa PUT`() = runTest {
        coEvery { m.daoVisita.obtenerPorId(3L) } returns visita(id = 3, idRemoto = 5)
        coEvery { m.apiVisitas.actualizar(5L, any()) } returns dtoRemoto(id = 5)

        val r = m.repositorio.actualizarVisita(visita(id = 3, idRemoto = 5))

        assertTrue(r.sincronizada)
        coVerify { m.apiVisitas.actualizar(5L, any()) }
        coVerify(exactly = 0) { m.apiVisitas.crear(any()) }
        coVerify(atLeast = 2) { m.daoVisita.actualizarVisita(any()) }   // primero PENDIENTE, luego SINCRONIZADO
    }

    @Test fun `actualizarVisita sin idRemoto crea la visita en el servidor`() = runTest {
        coEvery { m.daoVisita.obtenerPorId(3L) } returns visita(id = 3)
        coEvery { m.apiVisitas.crear(any()) } returns dtoRemoto(id = 61)

        m.repositorio.actualizarVisita(visita(id = 3))

        coVerify { m.daoVisita.actualizarVisita(match { it.idRemoto == 61L }) }
    }

    @Test fun `si el servidor ya no tiene la visita al editar se vuelve a crear`() = runTest {
        coEvery { m.daoVisita.obtenerPorId(3L) } returns visita(id = 3, idRemoto = 5)
        coEvery { m.apiVisitas.actualizar(5L, any()) } throws errorHttp(404)
        coEvery { m.apiVisitas.crear(match { it.id == null }) } returns dtoRemoto(id = 50)

        val r = m.repositorio.actualizarVisita(visita(id = 3, idRemoto = 5))

        assertTrue(r.sincronizada)
        coVerify { m.daoVisita.actualizarVisita(match { it.idRemoto == 50L && it.estadoSincronizacion == EstadoSync.SINCRONIZADO }) }
    }

    @Test fun `un error distinto de 404 al editar se propaga como problema`() = runTest {
        coEvery { m.daoVisita.obtenerPorId(3L) } returns visita(id = 3, idRemoto = 5)
        coEvery { m.apiVisitas.actualizar(5L, any()) } throws errorHttp(503)

        val r = m.repositorio.actualizarVisita(visita(id = 3, idRemoto = 5))

        assertFalse(r.sincronizada)
        coVerify(exactly = 0) { m.apiVisitas.crear(any()) }
    }

    @Test fun `eliminar una visita sincronizada la borra en el servidor y luego local`() = runTest {
        coEvery { m.apiVisitas.eliminar(5L) } returns Response.success(Unit)

        val r = m.repositorio.eliminarVisita(visita(id = 3, idRemoto = 5))

        assertTrue(r.isSuccess)
        coVerify { m.apiVisitas.eliminar(5L) }
        coVerify { m.daoVisita.eliminarPorId(3L) }
    }

    @Test fun `eliminar una visita solo local no llama al servidor`() = runTest {
        val r = m.repositorio.eliminarVisita(visita(id = 3))

        assertTrue(r.isSuccess)
        coVerify(exactly = 0) { m.apiVisitas.eliminar(any()) }
        coVerify { m.daoVisita.eliminarPorId(3L) }
    }

    @Test fun `si el servidor responde 404 al eliminar se considera ya eliminada`() = runTest {
        coEvery { m.apiVisitas.eliminar(5L) } returns Response.error(404, "".toResponseBody(null))

        val r = m.repositorio.eliminarVisita(visita(id = 3, idRemoto = 5))

        assertTrue(r.isSuccess)
        coVerify { m.daoVisita.eliminarPorId(3L) }
    }

    @Test fun `si el servidor falla al eliminar no se borra localmente`() = runTest {
        coEvery { m.apiVisitas.eliminar(5L) } returns Response.error(500, "".toResponseBody(null))

        val r = m.repositorio.eliminarVisita(visita(id = 3, idRemoto = 5))

        assertTrue(r.isFailure)
        coVerify(exactly = 0) { m.daoVisita.eliminarPorId(any()) }
    }

    @Test fun `sin conexion no se puede eliminar una visita sincronizada`() = runTest {
        coEvery { m.apiVisitas.eliminar(5L) } throws IOException("sin red")

        val r = m.repositorio.eliminarVisita(visita(id = 3, idRemoto = 5))

        assertTrue(r.isFailure)
        coVerify(exactly = 0) { m.daoVisita.eliminarPorId(any()) }
    }

    @Test fun `sincronizar envia pendientes descarga nuevas y elimina las borradas en el servidor`() = runTest {
        coEvery { m.daoVisita.obtenerPendientes() } returns listOf(visita(id = 1))
        coEvery { m.daoVisita.obtenerPorId(1L) } returns visita(id = 1)
        coEvery { m.apiVisitas.crear(any()) } returns dtoRemoto(id = 10)
        coEvery { m.apiVisitas.listar() } returns listOf(dtoRemoto(id = 10), dtoRemoto(id = 11, codigo = "VTR-2000"))
        coEvery { m.daoVisita.obtenerPorIdRemoto(10L) } returns visita(id = 1, idRemoto = 10, estado = EstadoSync.SINCRONIZADO)
        coEvery { m.daoVisita.obtenerPorIdRemoto(11L) } returns null
        coEvery { m.daoVisita.obtenerSincronizadas() } returns listOf(
            visita(id = 1, idRemoto = 10, estado = EstadoSync.SINCRONIZADO),
            visita(id = 2, idRemoto = 77, estado = EstadoSync.SINCRONIZADO)
        )
        coEvery { m.apiUsuarios.puntosVenta() } returns listOf(PuntoVentaDto(1, "Tienda A", -33.0, -70.0))

        val r = m.repositorio.sincronizar()

        assertTrue(r.sincronizada)
        assertTrue(r.mensaje.contains("1 enviada"))
        coVerify { m.daoVisita.insertarVisita(match { it.idRemoto == 11L && it.codigoActivo == "VTR-2000" }) }   // nueva
        coVerify { m.daoVisita.eliminarPorId(2L) }                                                              // borrada en servidor
        coVerify(exactly = 0) { m.daoVisita.eliminarPorId(1L) }
        coVerify { m.daoPunto.reemplazarTodos(match { it.size == 1 && it[0].nombre == "Tienda A" }) }
    }

    @Test fun `sincronizar se detiene al primer pendiente sin conexion`() = runTest {
        coEvery { m.daoVisita.obtenerPendientes() } returns listOf(visita(id = 1), visita(id = 2))
        coEvery { m.daoVisita.obtenerPorId(any()) } returns visita()
        coEvery { m.apiVisitas.crear(any()) } throws IOException("sin red")

        val r = m.repositorio.sincronizar()

        assertTrue(r.sinConexion)
        assertFalse(r.sincronizada)
        coVerify(exactly = 1) { m.apiVisitas.crear(any()) }
        coVerify(exactly = 0) { m.apiVisitas.listar() }
    }

    @Test fun `sincronizar sin pendientes y sin red informa falta de conexion`() = runTest {
        m.stubSincronizacionVacia()
        coEvery { m.apiVisitas.listar() } throws IOException("sin red")

        val r = m.repositorio.sincronizar()

        assertTrue(r.sinConexion)
        assertTrue(r.mensaje.contains("Sin conexión"))
    }

    @Test fun `sincronizar informa visitas con problemas`() = runTest {
        m.stubSincronizacionVacia()
        coEvery { m.daoVisita.obtenerPendientes() } returns listOf(visita(id = 1))
        coEvery { m.daoVisita.obtenerPorId(1L) } returns visita(id = 1)
        coEvery { m.apiVisitas.crear(any()) } throws errorHttp(422, """{"mensaje":"mal"}""")

        val r = m.repositorio.sincronizar()

        assertFalse(r.sincronizada)
        assertFalse(r.sinConexion)
        assertTrue(r.mensaje.contains("1 con problemas"))
    }

    @Test fun `sincronizar no pisa cambios locales pendientes`() = runTest {
        m.stubSincronizacionVacia()
        coEvery { m.apiVisitas.listar() } returns listOf(dtoRemoto(id = 10))
        coEvery { m.daoVisita.obtenerPorIdRemoto(10L) } returns visita(id = 1, idRemoto = 10, estado = EstadoSync.PENDIENTE)

        val r = m.repositorio.sincronizar()

        assertTrue(r.sincronizada)
        coVerify(exactly = 0) { m.daoVisita.actualizarVisita(any()) }
        coVerify(exactly = 0) { m.daoVisita.insertarVisita(any()) }
    }

    @Test fun `sincronizar actualiza las filas ya sincronizadas con lo del servidor`() = runTest {
        m.stubSincronizacionVacia()
        coEvery { m.apiVisitas.listar() } returns listOf(dtoRemoto(id = 10).copy(observaciones = "Cambió en el servidor"))
        coEvery { m.daoVisita.obtenerPorIdRemoto(10L) } returns visita(id = 4, idRemoto = 10, estado = EstadoSync.SINCRONIZADO)
        coEvery { m.daoVisita.obtenerSincronizadas() } returns listOf(visita(id = 4, idRemoto = 10, estado = EstadoSync.SINCRONIZADO))

        m.repositorio.sincronizar()

        coVerify { m.daoVisita.actualizarVisita(match { it.id == 4L && it.observaciones == "Cambió en el servidor" }) }
    }

    @Test fun `sincronizar ignora DTOs sin id`() = runTest {
        m.stubSincronizacionVacia()
        coEvery { m.apiVisitas.listar() } returns listOf(dtoRemoto(id = null))

        val r = m.repositorio.sincronizar()

        assertTrue(r.sincronizada)
        coVerify(exactly = 0) { m.daoVisita.obtenerPorIdRemoto(any()) }
    }

    @Test fun `iniciarSesion devuelve el usuario`() = runTest {
        coEvery { m.apiUsuarios.login(any()) } returns UsuarioDto(1, "ana@tecnico.com", "Ana", "Técnico")

        val r = m.repositorio.iniciarSesion("ana@tecnico.com", "123456")

        assertTrue(r.isSuccess)
        assertEquals("Técnico", r.getOrNull()!!.rol)
    }

    @Test fun `iniciarSesion propaga el error del servidor`() = runTest {
        coEvery { m.apiUsuarios.login(any()) } throws errorHttp(401, """{"mensaje":"Credenciales inválidas"}""")

        val r = m.repositorio.iniciarSesion("ana@tecnico.com", "mala")

        assertTrue(r.isFailure)
    }

    @Test fun `obtenerClima mapea la respuesta de la API externa`() = runTest {
        coEvery { m.apiClima.climaActual(-33.4, -70.6, any(), any()) } returns RespuestaClima(ClimaActual("t", 24.0, 50, 1, 8.0))

        val r = m.repositorio.obtenerClima(-33.4, -70.6)

        assertTrue(r.isSuccess)
        assertEquals(24.0, r.getOrNull()!!.temperaturaC, 0.001)
        assertEquals("Mayormente despejado", r.getOrNull()!!.descripcion)
    }

    @Test fun `obtenerClima falla si la API no entrega temperatura`() = runTest {
        coEvery { m.apiClima.climaActual(any(), any(), any(), any()) } returns RespuestaClima(null)
        assertTrue(m.repositorio.obtenerClima(0.0, 0.0).isFailure)
    }

    @Test fun `obtenerClima falla sin conexion`() = runTest {
        coEvery { m.apiClima.climaActual(any(), any(), any(), any()) } throws IOException("sin red")
        assertTrue(m.repositorio.obtenerClima(0.0, 0.0).isFailure)
    }

    @Test fun `el repositorio expone los flujos de los DAO`() {
        assertNotNull(m.repositorio.todasLasVisitas)
        assertNotNull(m.repositorio.puntosVenta)
        assertNotNull(m.repositorio.observarVisita(1))
    }
}
