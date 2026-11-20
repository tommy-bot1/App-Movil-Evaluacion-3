package com.tacticaldev.trazabilidad.datos

import com.tacticaldev.trazabilidad.ayuda.dtoRemoto
import com.tacticaldev.trazabilidad.ayuda.visita
import com.tacticaldev.trazabilidad.datos.remoto.aDto
import com.tacticaldev.trazabilidad.datos.remoto.aEntidad
import com.tacticaldev.trazabilidad.datos.remoto.dto.PuntoVentaDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MapeadoresTest {
    @Test fun `entidad a DTO conserva los campos y usa idRemoto como id`() {
        val dto = visita(id = 5, idRemoto = 42).aDto()
        assertEquals(42L, dto.id)
        assertEquals("VTR-1020", dto.codigoActivo)
        assertEquals("Tienda Central", dto.nombreTienda)
        assertEquals("content://foto/1", dto.rutaFoto)
        assertEquals(true, dto.presenciaVerificada)
    }

    @Test fun `visita nueva se envia sin id y foto vacia como null`() {
        val dto = visita(foto = "").aDto()
        assertNull(dto.id)
        assertNull(dto.rutaFoto)
    }

    @Test fun `DTO a entidad crea fila SINCRONIZADA con idRemoto`() {
        val e = dtoRemoto(id = 77).aEntidad()
        assertEquals(0L, e.id)
        assertEquals(77L, e.idRemoto)
        assertEquals(EstadoSync.SINCRONIZADO, e.estadoSincronizacion)
        assertEquals("", e.rutaFoto)
    }

    @Test fun `DTO a entidad conserva id local y foto local`() {
        val local = visita(id = 9, idRemoto = 77, foto = "content://mi/foto")
        val e = dtoRemoto(id = 77).copy(rutaFoto = "content://otra/foto").aEntidad(local)
        assertEquals(9L, e.id)
        assertEquals("content://mi/foto", e.rutaFoto)
    }

    @Test fun `DTO a entidad usa la foto del servidor si no hay local y rellena nulos`() {
        val e = dtoRemoto().copy(rutaFoto = "content://srv", precisionGps = null, distanciaPuntoVentaMetros = null, marcaTiempo = null).aEntidad()
        assertEquals("content://srv", e.rutaFoto)
        assertEquals(0f, e.precisionGps, 0f)
        assertEquals(0f, e.distanciaPuntoVentaMetros, 0f)
        assertEquals(true, e.marcaTiempo > 0)
    }

    @Test fun `punto de venta DTO a entidad`() {
        val p = PuntoVentaDto(3, "Tienda X", -33.0, -70.0).aEntidad()
        assertEquals(3L, p.id)
        assertEquals("Tienda X", p.nombre)
        assertEquals(-33.0, p.latitud, 0.0)
    }
}
