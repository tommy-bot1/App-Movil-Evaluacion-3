package com.tacticaldev.trazabilidad.utilidades

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeocercaTest {
    private val centro = Pair(-33.4489, -70.6693)

    @Test fun `distancia entre el mismo punto es cero`() {
        assertEquals(0f, Geocerca.distanciaMetros(centro.first, centro.second, centro.first, centro.second), 0.01f)
    }

    @Test fun `un grado de latitud son unos 111 km`() {
        assertEquals(111_195f, Geocerca.distanciaMetros(0.0, 0.0, 1.0, 0.0), 100f)
    }

    @Test fun `la distancia es simetrica`() {
        val ida = Geocerca.distanciaMetros(-33.4489, -70.6693, -33.4170, -70.6062)
        val vuelta = Geocerca.distanciaMetros(-33.4170, -70.6062, -33.4489, -70.6693)
        assertEquals(ida, vuelta, 0.01f)
    }

    @Test fun `dentro del radio permitido`() {
        // 0.0009 grados de latitud ≈ 100 m
        val gps = UbicacionGps(centro.first + 0.0009, centro.second, 10f)
        val (dentro, distancia) = Geocerca.evaluar(centro.first, centro.second, gps)
        assertTrue(dentro)
        assertEquals(100f, distancia!!, 3f)
    }

    @Test fun `fuera del radio permitido`() {
        // 0.003 grados de latitud ≈ 333 m
        val gps = UbicacionGps(centro.first + 0.003, centro.second, 10f)
        val (dentro, distancia) = Geocerca.evaluar(centro.first, centro.second, gps)
        assertFalse(dentro)
        assertTrue(distancia!! > Geocerca.RADIO_PERMITIDO_METROS)
    }

    @Test fun `sin coordenadas del punto no se puede verificar`() {
        val gps = UbicacionGps(centro.first, centro.second, 10f)
        assertEquals(Pair(false, null), Geocerca.evaluar(null, centro.second, gps))
        assertEquals(Pair(false, null), Geocerca.evaluar(centro.first, null, gps))
        assertNull(Geocerca.evaluar(null, null, gps).second)
    }

    @Test fun `constantes de negocio`() {
        assertEquals(250f, Geocerca.RADIO_PERMITIDO_METROS, 0f)
        assertEquals(100f, Geocerca.PRECISION_MAXIMA_METROS, 0f)
        assertNotNull(UbicacionGps(1.0, 2.0, 3f))
    }
}
