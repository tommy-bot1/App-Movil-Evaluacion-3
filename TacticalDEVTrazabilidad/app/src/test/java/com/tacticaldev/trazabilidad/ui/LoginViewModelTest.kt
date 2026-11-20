package com.tacticaldev.trazabilidad.ui

import com.tacticaldev.trazabilidad.ayuda.ReglaDispatcherPrincipal
import com.tacticaldev.trazabilidad.ayuda.crearMundo
import com.tacticaldev.trazabilidad.ayuda.errorHttp
import com.tacticaldev.trazabilidad.datos.remoto.dto.UsuarioDto
import com.tacticaldev.trazabilidad.ui.viewmodel.LoginViewModel
import com.tacticaldev.trazabilidad.ui.viewmodel.rolDesdeCorreo
import io.mockk.coEvery
import io.mockk.coVerify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class LoginViewModelTest {
    @get:Rule val reglaMain = ReglaDispatcherPrincipal()

    private val mundo = crearMundo()
    private val vm = LoginViewModel(mundo.repositorio)

    private fun escribir(correo: String, clave: String) {
        vm.onCorreoChanged(correo)
        vm.onClaveChanged(clave)
    }

    @Test fun `reconoce el perfil por el dominio`() {
        assertEquals("Vendedor", rolDesdeCorreo("tomas@ventas.com"))
        assertEquals("Vendedor", rolDesdeCorreo("tomas@vendedor.com"))
        assertEquals("Técnico", rolDesdeCorreo("ana@tecnico.com"))
        assertEquals("Transportista", rolDesdeCorreo("pedro@transporte.com"))
        assertEquals("Transportista", rolDesdeCorreo("pedro@transportista.com"))
    }

    @Test fun `ignora mayusculas y espacios`() {
        assertEquals("Técnico", rolDesdeCorreo("  ANA@TECNICO.COM "))
    }

    @Test fun `dominio desconocido o sin com no tiene perfil`() {
        assertNull(rolDesdeCorreo("ana@gmail.com"))
        assertNull(rolDesdeCorreo("ana@tecnico.cl"))
        assertNull(rolDesdeCorreo("sin-arroba"))
        assertNull(rolDesdeCorreo(""))
    }

    //  validar
    @Test fun `validar rechaza campos vacios`() {
        assertFalse(vm.validar())
        assertEquals("Ingresa tu correo", vm.errorCorreo)
        assertEquals("Ingresa tu contraseña", vm.errorClave)
    }

    @Test fun `validar rechaza formato de correo invalido`() {
        escribir("ana@@tecnico", "123456")
        assertFalse(vm.validar())
        assertTrue(vm.errorCorreo!!.startsWith("Formato inválido"))
    }

    @Test fun `validar rechaza dominio no reconocido`() {
        escribir("ana@empresa.com", "123456")
        assertFalse(vm.validar())
        assertTrue(vm.errorCorreo!!.startsWith("Dominio no reconocido"))
    }

    @Test fun `validar rechaza clave corta`() {
        escribir("ana@tecnico.com", "123")
        assertFalse(vm.validar())
        assertNull(vm.errorCorreo)
        assertEquals("Mínimo 6 caracteres", vm.errorClave)
    }

    @Test fun `validar acepta datos correctos`() {
        escribir("ana@tecnico.com", "123456")
        assertTrue(vm.validar())
        assertNull(vm.errorCorreo)
        assertNull(vm.errorClave)
    }

    @Test fun `escribir limpia los errores y rolActual refleja el correo`() {
        vm.validar()
        vm.onCorreoChanged("ana@tecnico.com")
        vm.onClaveChanged("123456")
        assertNull(vm.errorCorreo)
        assertNull(vm.errorClave)
        assertEquals("Técnico", vm.rolActual())
    }

    @Test fun `login correcto entrega el usuario del servidor`() {
        coEvery { mundo.apiUsuarios.login(any()) } returns UsuarioDto(1, "ana@tecnico.com", "Ana", "Técnico")
        escribir("  Ana@Tecnico.com ", "123456")
        var recibido: UsuarioDto? = null

        vm.iniciarSesion { recibido = it }

        assertNotNull(recibido)
        assertEquals("Ana", recibido!!.nombre)
        assertFalse(vm.cargando)
        assertNull(vm.errorGeneral)
        coVerify { mundo.apiUsuarios.login(match { it.correo == "ana@tecnico.com" && it.clave == "123456" }) }
    }

    @Test fun `credenciales invalidas muestran el mensaje del servidor`() {
        coEvery { mundo.apiUsuarios.login(any()) } throws errorHttp(401, """{"mensaje":"Credenciales inválidas"}""")
        escribir("ana@tecnico.com", "equivocada")
        var llamado = false

        vm.iniciarSesion { llamado = true }

        assertFalse(llamado)
        assertEquals("Credenciales inválidas", vm.errorGeneral)
        assertFalse(vm.cargando)
    }

    @Test fun `sin conexion muestra un mensaje claro`() {
        coEvery { mundo.apiUsuarios.login(any()) } throws IOException("sin red")
        escribir("ana@tecnico.com", "123456")

        vm.iniciarSesion { }

        assertTrue(vm.errorGeneral!!.startsWith("Sin conexión"))
    }

    @Test fun `si el formato es invalido no se llama al servidor`() {
        escribir("ana@gmail.com", "123456")

        vm.iniciarSesion { }

        coVerify(exactly = 0) { mundo.apiUsuarios.login(any()) }
        assertNotNull(vm.errorCorreo)
    }
}
