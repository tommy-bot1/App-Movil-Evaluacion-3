package com.tacticaldev.trazabilidad.dominio

import com.tacticaldev.trazabilidad.dominio.validacion.Validador
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorTest {
    @Test fun `codigo vacio es invalido`() {
        val r = Validador.validarCodigoActivo("   ")
        assertFalse(r.esValido)
        assertEquals("El código del activo es obligatorio", r.mensajeError)
    }

    @Test fun `codigo con formato incorrecto es invalido`() {
        listOf("1234", "VT-1020", "VTR1020", "VTR-12", "VTR-1234567", "VTR-ABCD").forEach {
            assertFalse("Debería ser inválido: $it", Validador.validarCodigoActivo(it).esValido)
        }
    }

    @Test fun `codigo valido acepta minusculas y espacios`() {
        assertTrue(Validador.validarCodigoActivo("VTR-1020").esValido)
        assertTrue(Validador.validarCodigoActivo(" vtr-1020 ").esValido)
        assertNull(Validador.validarCodigoActivo("CON-123").mensajeError)
    }

    @Test fun `tienda obligatoria`() {
        assertFalse(Validador.validarNombreTienda("").esValido)
        assertTrue(Validador.validarNombreTienda("Tienda Central").esValido)
    }

    @Test fun `observaciones obligatorias`() {
        assertEquals("Debe agregar una observación técnica", Validador.validarObservaciones("  ").mensajeError)
        assertTrue(Validador.validarObservaciones("Todo bien").esValido)
    }

    @Test fun `foto obligatoria`() {
        assertFalse(Validador.validarFoto(null).esValido)
        assertFalse(Validador.validarFoto("").esValido)
        assertTrue(Validador.validarFoto("content://foto").esValido)
    }

    @Test fun `ubicacion requiere gps y presencia verificada`() {
        assertEquals("Se requiere ubicación GPS", Validador.validarUbicacion(null, -70.0, true).mensajeError)
        assertEquals("Se requiere ubicación GPS", Validador.validarUbicacion(-33.0, null, true).mensajeError)
        assertFalse(Validador.validarUbicacion(-33.0, -70.0, false).esValido)
        assertTrue(Validador.validarUbicacion(-33.0, -70.0, true).esValido)
    }
}
