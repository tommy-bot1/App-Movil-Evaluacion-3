package com.tacticaldev.trazabilidad.ui

import com.tacticaldev.trazabilidad.ayuda.Mundo
import com.tacticaldev.trazabilidad.ayuda.ReglaDispatcherPrincipal
import com.tacticaldev.trazabilidad.ayuda.crearMundo
import com.tacticaldev.trazabilidad.ayuda.dtoRemoto
import com.tacticaldev.trazabilidad.ayuda.stubSincronizacionVacia
import com.tacticaldev.trazabilidad.ayuda.visita
import com.tacticaldev.trazabilidad.datos.local.entidad.PuntoVenta
import com.tacticaldev.trazabilidad.datos.remoto.dto.ClimaActual
import com.tacticaldev.trazabilidad.datos.remoto.dto.RespuestaClima
import com.tacticaldev.trazabilidad.ui.viewmodel.EstadoClima
import com.tacticaldev.trazabilidad.ui.viewmodel.VisitaViewModel
import com.tacticaldev.trazabilidad.utilidades.ProveedorUbicacion
import com.tacticaldev.trazabilidad.utilidades.UbicacionGps
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class VisitaViewModelTest {
    @get:Rule val reglaMain = ReglaDispatcherPrincipal()

    private val tienda = PuntoVenta(1, "Tienda Central", -33.4489, -70.6693)

    private fun gpsDevuelve(u: UbicacionGps): ProveedorUbicacion = mockk<ProveedorUbicacion>().also { p ->
        every { p.obtenerUbicacionActual(any(), any()) } answers { firstArg<(UbicacionGps) -> Unit>().invoke(u) }
    }

    private fun gpsFalla(e: Exception): ProveedorUbicacion = mockk<ProveedorUbicacion>().also { p ->
        every { p.obtenerUbicacionActual(any(), any()) } answers { secondArg<(Exception) -> Unit>().invoke(e) }
    }

    private fun gpsEnLaTienda() = gpsDevuelve(UbicacionGps(tienda.latitud, tienda.longitud, 10f))

    private fun crear(
        gps: ProveedorUbicacion = mockk(),
        puntos: List<PuntoVenta> = listOf(tienda),
        mundo: Mundo = crearMundo(puntos)
    ) = Pair(mundo, VisitaViewModel(mundo.repositorio, gps))

    private fun llenarFormulario(vm: VisitaViewModel) {
        vm.alCambiarCodigoActivo("vtr-1020")
        vm.alCambiarNombreTienda("Tienda Central")
        vm.alCambiarObservaciones("Equipo limpio")
        vm.alCapturarFoto("content://foto")
        vm.capturarUbicacion()
    }

    @Test fun `expone los datos del repositorio`() {
        val mundo = crearMundo(puntos = listOf(tienda), visitas = listOf(visita(1), visita(2)), total = 2, fallas = 1, pendientes = 2)
        val (_, vm) = crear(mundo = mundo)

        assertEquals(2, vm.todasLasVisitas.value.size)
        assertEquals(2, vm.totalVisitas.value)
        assertEquals(1, vm.fallasCriticas.value)
        assertEquals(2, vm.totalPendientes.value)
        assertEquals(listOf(tienda), vm.puntosVenta.value)
    }

    @Test fun `el codigo se pasa a mayusculas y se valida`() {
        val (_, vm) = crear()
        vm.alCambiarCodigoActivo("vtr-12")
        assertEquals("VTR-12", vm.estadoFormulario.value.codigoActivo)
        assertNotNull(vm.estadoFormulario.value.errorCodigoActivo)
        vm.alCambiarCodigoActivo("vtr-1020")
        assertNull(vm.estadoFormulario.value.errorCodigoActivo)
    }

    @Test fun `observaciones vacias muestran error`() {
        val (_, vm) = crear()
        vm.alCambiarObservaciones("")
        assertNotNull(vm.estadoFormulario.value.errorObservaciones)
        vm.alCambiarObservaciones("ok")
        assertNull(vm.estadoFormulario.value.errorObservaciones)
    }

    @Test fun `selecciones simples actualizan el estado`() {
        val (_, vm) = crear()
        vm.alSeleccionarTipoActivo("Conservadora")
        vm.alSeleccionarTipoVisita("Inspección")
        vm.alSeleccionarEstadoEquipo("Falla Crítica")
        vm.alCapturarFoto("content://f")
        val e = vm.estadoFormulario.value
        assertEquals("Conservadora", e.tipoActivo)
        assertEquals("Inspección", e.tipoVisita)
        assertEquals("Falla Crítica", e.estadoEquipo)
        assertEquals("content://f", e.uriFoto)
    }

    @Test fun `cambiar de tienda invalida la ubicacion ya capturada`() {
        val (_, vm) = crear(gps = gpsEnLaTienda())
        vm.alCambiarNombreTienda("Tienda Central")
        vm.capturarUbicacion()
        assertTrue(vm.estadoFormulario.value.presenciaVerificada)

        vm.alCambiarNombreTienda("Otra")

        assertFalse(vm.estadoFormulario.value.presenciaVerificada)
        assertNull(vm.estadoFormulario.value.latitud)
    }

    @Test fun `sin tienda no se captura ubicacion`() {
        val (_, vm) = crear()
        vm.capturarUbicacion()
        assertEquals("Selecciona primero el punto de venta", vm.estadoFormulario.value.errorUbicacion)
    }

    @Test fun `tienda sin coordenadas pide sincronizar`() {
        val (_, vm) = crear(puntos = emptyList())
        vm.alCambiarNombreTienda("Tienda Fantasma")
        vm.capturarUbicacion()
        assertTrue(vm.estadoFormulario.value.errorUbicacion!!.contains("No hay coordenadas"))
        assertFalse(vm.estadoFormulario.value.cargandoUbicacion)
    }

    @Test fun `dentro del radio y con buena precision verifica la presencia`() {
        val (_, vm) = crear(gps = gpsEnLaTienda())
        vm.alCambiarNombreTienda("Tienda Central")
        vm.capturarUbicacion()
        val e = vm.estadoFormulario.value
        assertTrue(e.presenciaVerificada)
        assertNull(e.errorUbicacion)
        assertFalse(e.cargandoUbicacion)
        assertEquals(tienda.latitud, e.latitud!!, 1e-9)
        assertEquals(10f, e.precisionGps!!, 0f)
    }

    @Test fun `fuera del radio no verifica la presencia`() {
        val (_, vm) = crear(gps = gpsDevuelve(UbicacionGps(tienda.latitud + 0.01, tienda.longitud, 10f)))
        vm.alCambiarNombreTienda("Tienda Central")
        vm.capturarUbicacion()
        val e = vm.estadoFormulario.value
        assertFalse(e.presenciaVerificada)
        assertTrue(e.errorUbicacion!!.startsWith("Presencia no verificada"))
        assertTrue(e.distanciaPuntoVentaMetros!! > 1000f)
    }

    @Test fun `precision baja no verifica la presencia`() {
        val (_, vm) = crear(gps = gpsDevuelve(UbicacionGps(tienda.latitud, tienda.longitud, 150f)))
        vm.alCambiarNombreTienda("Tienda Central")
        vm.capturarUbicacion()
        assertFalse(vm.estadoFormulario.value.presenciaVerificada)
        assertTrue(vm.estadoFormulario.value.errorUbicacion!!.contains("precisión es baja"))
    }

    @Test fun `error de GPS se informa`() {
        val (_, vm) = crear(gps = gpsFalla(Exception("sin señal")))
        vm.alCambiarNombreTienda("Tienda Central")
        vm.capturarUbicacion()
        assertEquals("sin señal", vm.estadoFormulario.value.errorUbicacion)
        assertFalse(vm.estadoFormulario.value.cargandoUbicacion)
    }

    @Test fun `error de GPS sin mensaje usa uno generico`() {
        val (_, vm) = crear(gps = gpsFalla(Exception()))
        vm.alCambiarNombreTienda("Tienda Central")
        vm.capturarUbicacion()
        assertEquals("Error al obtener GPS", vm.estadoFormulario.value.errorUbicacion)
    }

    @Test fun `enviar un formulario vacio muestra errores y no guarda`() {
        val (mundo, vm) = crear()
        var termino = false

        vm.enviarFormulario { termino = true }

        val e = vm.estadoFormulario.value
        assertFalse(termino)
        assertNotNull(e.errorCodigoActivo)
        assertNotNull(e.errorNombreTienda)
        assertNotNull(e.errorObservaciones)
        assertNotNull(e.errorFoto)
        assertNotNull(e.errorUbicacion)
        coVerify(exactly = 0) { mundo.daoVisita.insertarVisita(any()) }
    }

    @Test fun `enviar un formulario valido guarda y sincroniza`() {
        val (mundo, vm) = crear(gps = gpsEnLaTienda())
        mundo.stubSincronizacionVacia()
        vm.establecerSesion("ana@tecnico.com", "Técnico", "Ana")
        coEvery { mundo.daoVisita.obtenerPorId(1L) } returns visita(1)
        coEvery { mundo.apiVisitas.crear(any()) } returns dtoRemoto(99)
        llenarFormulario(vm)
        var termino = false

        vm.enviarFormulario { termino = true }

        assertTrue(termino)
        assertEquals("Visita VTR-1020 sincronizada con el servidor", vm.mensaje.value)
        assertTrue(vm.estadoFormulario.value.esGuardadoExitoso)
        assertEquals("", vm.estadoFormulario.value.codigoActivo)
        coVerify {
            mundo.daoVisita.insertarVisita(match {
                it.codigoActivo == "VTR-1020" && it.nombreTienda == "Tienda Central" && it.rutaFoto == "content://foto" &&
                    it.presenciaVerificada && it.correo == "ana@tecnico.com" && it.nombreOperario == "Ana" && it.rolOperario == "Técnico"
            })
        }
    }

    @Test fun `sin conexion igual se guarda y se avisa que queda pendiente`() {
        val (mundo, vm) = crear(gps = gpsEnLaTienda())
        coEvery { mundo.daoVisita.obtenerPorId(1L) } returns visita(1)
        coEvery { mundo.apiVisitas.crear(any()) } throws IOException("sin red")
        llenarFormulario(vm)
        var termino = false

        vm.enviarFormulario { termino = true }

        assertTrue(termino)
        assertTrue(vm.mensaje.value!!.contains("PENDIENTE"))
        coVerify { mundo.daoVisita.insertarVisita(any()) }
    }

    @Test fun `establecerSesion guarda los datos del usuario y sincroniza en silencio`() {
        val (mundo, vm) = crear()
        mundo.stubSincronizacionVacia()

        vm.establecerSesion("ana@tecnico.com", "Técnico", "Ana Pérez")

        assertEquals("ana@tecnico.com", vm.correoActivo)
        assertEquals("Ana Pérez", vm.nombreActivo)
        assertEquals("Técnico", vm.rolActivo)
        assertNull(vm.mensaje.value)
        assertFalse(vm.sincronizando.value)
        coVerify { mundo.apiVisitas.listar() }
    }

    @Test fun `sin nombre del servidor se deriva del correo`() {
        val (mundo, vm) = crear()
        mundo.stubSincronizacionVacia()
        vm.establecerSesion("pedro@transporte.com", "Transportista")
        assertEquals("Pedro", vm.nombreActivo)
    }

    @Test fun `una sincronizacion silenciosa fallida igual avisa`() {
        val (mundo, vm) = crear()
        mundo.stubSincronizacionVacia()
        coEvery { mundo.apiVisitas.listar() } throws IOException("sin red")

        vm.establecerSesion("ana@tecnico.com", "Técnico")

        assertTrue(vm.mensaje.value!!.contains("Sin conexión"))
    }

    @Test fun `sincronizar manual siempre muestra el resultado`() {
        val (mundo, vm) = crear()
        mundo.stubSincronizacionVacia()

        vm.sincronizar()

        assertEquals("Sincronización completa: 0 enviada(s)", vm.mensaje.value)
        vm.mensajeMostrado()
        assertNull(vm.mensaje.value)
    }

    @Test fun `cerrar sesion limpia todo`() {
        val (mundo, vm) = crear(gps = gpsEnLaTienda())
        mundo.stubSincronizacionVacia()
        vm.establecerSesion("ana@tecnico.com", "Vendedor", "Ana")
        vm.alCambiarCodigoActivo("VTR-1020")

        vm.cerrarSesion()

        assertEquals("", vm.correoActivo)
        assertEquals("", vm.nombreActivo)
        assertEquals("Técnico", vm.rolActivo)
        assertEquals("", vm.estadoFormulario.value.codigoActivo)
        assertEquals(EstadoClima.Inactivo, vm.clima.value)
    }

    // ------------------------------------------------------------------ READ / UPDATE / DELETE
    @Test fun `visita expone el flujo del repositorio`() = runTest {
        val (_, vm) = crear()
        assertNull(vm.visita(1).first())
    }

    @Test fun `actualizar una visita valida la envia y avisa`() {
        val (mundo, vm) = crear()
        coEvery { mundo.daoVisita.obtenerPorId(3L) } returns visita(3, idRemoto = 5)
        coEvery { mundo.apiVisitas.actualizar(5L, any()) } returns dtoRemoto(5)
        var termino = false

        vm.actualizarVisita(visita(3, idRemoto = 5), "Conservadora", "Mantenimiento Preventivo", "Falla Crítica", "  Compresor ruidoso ") { termino = true }

        assertTrue(termino)
        assertEquals("Visita VTR-1020 sincronizada con el servidor", vm.mensaje.value)
        coVerify {
            mundo.daoVisita.actualizarVisita(match {
                it.tipoActivo == "Conservadora" && it.estadoEquipo == "Falla Crítica" && it.observaciones == "Compresor ruidoso"
            })
        }
    }

    @Test fun `actualizar con observaciones vacias no hace nada`() {
        val (mundo, vm) = crear()
        var termino = false

        vm.actualizarVisita(visita(3), "Conservadora", "Inspección", "Bueno", "   ") { termino = true }

        assertFalse(termino)
        assertEquals("Debe agregar una observación técnica", vm.mensaje.value)
        coVerify(exactly = 0) { mundo.daoVisita.actualizarVisita(any()) }
    }

    @Test fun `eliminar una visita avisa y vuelve`() {
        val (mundo, vm) = crear()
        coEvery { mundo.apiVisitas.eliminar(5L) } returns Response.success(Unit)
        var termino = false

        vm.eliminarVisita(visita(3, idRemoto = 5)) { termino = true }

        assertTrue(termino)
        assertEquals("Visita VTR-1020 eliminada", vm.mensaje.value)
        coVerify { mundo.daoVisita.eliminarPorId(3L) }
    }

    @Test fun `si falla la eliminacion se informa y no se vuelve`() {
        val (mundo, vm) = crear()
        coEvery { mundo.apiVisitas.eliminar(5L) } throws IOException("sin red")
        var termino = false

        vm.eliminarVisita(visita(3, idRemoto = 5)) { termino = true }

        assertFalse(termino)
        assertTrue(vm.mensaje.value!!.startsWith("Sin conexión"))
        coVerify(exactly = 0) { mundo.daoVisita.eliminarPorId(any()) }
    }

    // ------------------------------------------------------------------ API externa (clima)
    @Test fun `al elegir tienda se consulta el clima`() {
        val (mundo, vm) = crear()
        coEvery { mundo.apiClima.climaActual(tienda.latitud, tienda.longitud, any(), any()) } returns
            RespuestaClima(ClimaActual("t", 33.0, 30, 0, 5.0))

        vm.alCambiarNombreTienda("Tienda Central")

        val estado = vm.clima.value
        assertTrue(estado is EstadoClima.Listo)
        assertEquals(33.0, (estado as EstadoClima.Listo).clima.temperaturaC, 0.001)
        assertNotNull(estado.clima.alerta)
    }

    @Test fun `si la API externa falla solo se muestra el error del clima`() {
        val (mundo, vm) = crear(gps = gpsEnLaTienda())
        coEvery { mundo.apiClima.climaActual(any(), any(), any(), any()) } throws IOException("sin red")

        vm.alCambiarNombreTienda("Tienda Central")

        assertTrue(vm.clima.value is EstadoClima.Error)
        vm.capturarUbicacion()                        // el resto del formulario sigue funcionando
        assertTrue(vm.estadoFormulario.value.presenciaVerificada)
    }

    @Test fun `tienda desconocida no consulta el clima`() {
        val (mundo, vm) = crear()
        vm.alCambiarNombreTienda("No existe")
        assertEquals(EstadoClima.Inactivo, vm.clima.value)
        coVerify(exactly = 0) { mundo.apiClima.climaActual(any(), any(), any(), any()) }
    }
}
