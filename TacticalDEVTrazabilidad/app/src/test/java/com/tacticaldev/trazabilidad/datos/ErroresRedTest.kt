package com.tacticaldev.trazabilidad.datos

import com.tacticaldev.trazabilidad.ayuda.errorHttp
import com.tacticaldev.trazabilidad.datos.remoto.mensajeAmigable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class ErroresRedTest {
    @Test fun `usa el mensaje que envia el servidor`() {
        assertEquals("Código de activo inválido", errorHttp(400, """{"mensaje":"Código de activo inválido"}""").mensajeAmigable())
    }

    @Test fun `sin cuerpo usa un mensaje segun el codigo`() {
        assertEquals("Credenciales inválidas", errorHttp(401).mensajeAmigable())
        assertEquals("No encontrado en el servidor", errorHttp(404).mensajeAmigable())
        assertEquals("Error interno del servidor", errorHttp(503).mensajeAmigable())
        assertEquals("Error HTTP 418", errorHttp(418).mensajeAmigable())
    }

    @Test fun `cuerpo que no es JSON tambien cae al mensaje por codigo`() {
        assertEquals("Error interno del servidor", errorHttp(500, "<html>error</html>").mensajeAmigable())
        assertEquals("Error HTTP 409", errorHttp(409, """{"otro":"campo"}""").mensajeAmigable())
    }

    @Test fun `problemas de conexion`() {
        assertTrue(IOException("fallo").mensajeAmigable().startsWith("Sin conexión con el servidor"))
        assertTrue(SocketTimeoutException().mensajeAmigable().startsWith("Sin conexión con el servidor"))
    }

    @Test fun `otras excepciones`() {
        assertEquals("algo salió mal", RuntimeException("algo salió mal").mensajeAmigable())
        assertEquals("Error inesperado", Exception().mensajeAmigable())
    }
}
