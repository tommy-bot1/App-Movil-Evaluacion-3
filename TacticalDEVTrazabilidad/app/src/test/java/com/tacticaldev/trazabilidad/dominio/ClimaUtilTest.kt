package com.tacticaldev.trazabilidad.dominio

import com.tacticaldev.trazabilidad.datos.remoto.aClima
import com.tacticaldev.trazabilidad.datos.remoto.dto.ClimaActual
import com.tacticaldev.trazabilidad.datos.remoto.dto.RespuestaClima
import com.tacticaldev.trazabilidad.dominio.clima.ClimaUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ClimaUtilTest {
    @Test fun `traduce codigos WMO a texto`() {
        assertEquals("Despejado", ClimaUtil.descripcion(0))
        assertEquals("Mayormente despejado", ClimaUtil.descripcion(1))
        assertEquals("Parcialmente nublado", ClimaUtil.descripcion(2))
        assertEquals("Nublado", ClimaUtil.descripcion(3))
        assertEquals("Niebla", ClimaUtil.descripcion(45))
        assertEquals("Llovizna", ClimaUtil.descripcion(53))
        assertEquals("Llovizna helada", ClimaUtil.descripcion(56))
        assertEquals("Lluvia", ClimaUtil.descripcion(63))
        assertEquals("Lluvia helada", ClimaUtil.descripcion(66))
        assertEquals("Nieve", ClimaUtil.descripcion(73))
        assertEquals("Chubascos", ClimaUtil.descripcion(81))
        assertEquals("Chubascos de nieve", ClimaUtil.descripcion(85))
        assertEquals("Tormenta", ClimaUtil.descripcion(95))
        assertEquals("Tormenta con granizo", ClimaUtil.descripcion(99))
    }

    @Test fun `codigo desconocido o nulo`() {
        assertEquals("Sin información", ClimaUtil.descripcion(1234))
        assertEquals("Sin información", ClimaUtil.descripcion(null))
    }

    @Test fun `alerta termica desde 30 grados`() {
        assertNull(ClimaUtil.alertaTermica(29.9))
        assertNotNull(ClimaUtil.alertaTermica(30.0))
        assertNotNull(ClimaUtil.alertaTermica(38.5))
    }

    @Test fun `mapea la respuesta de la API a Clima`() {
        val clima = RespuestaClima(ClimaActual("t", 31.4, 40, 0, 12.6)).aClima()!!
        assertEquals(31.4, clima.temperaturaC, 0.001)
        assertEquals(40, clima.humedadPct)
        assertEquals(12.6, clima.vientoKmh!!, 0.001)
        assertEquals("Despejado", clima.descripcion)
        assertNotNull(clima.alerta)
    }

    @Test fun `sin temperatura o sin bloque current no hay clima`() {
        assertNull(RespuestaClima(null).aClima())
        assertNull(RespuestaClima(ClimaActual(temperatura = null)).aClima())
    }

    @Test fun `datos opcionales ausentes no rompen el mapeo`() {
        val clima = RespuestaClima(ClimaActual(temperatura = 18.0)).aClima()!!
        assertNull(clima.humedadPct)
        assertNull(clima.vientoKmh)
        assertNull(clima.alerta)
        assertEquals("Sin información", clima.descripcion)
    }
}
