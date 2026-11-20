package com.tacticaldev.trazabilidad.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tacticaldev.trazabilidad.datos.remoto.dto.UsuarioDto
import com.tacticaldev.trazabilidad.datos.remoto.mensajeAmigable
import com.tacticaldev.trazabilidad.datos.repositorio.Repositorio
import kotlinx.coroutines.launch

private const val LARGO_MINIMO_CLAVE = 6
private val FORMATO_CORREO = Regex("""^[A-Za-z0-9._%+-]+@[A-Za-z]+\.com$""", RegexOption.IGNORE_CASE)


fun rolDesdeCorreo(correo: String): String? {
    val dominioCompleto = correo.trim().substringAfter('@', "").lowercase()
    if (!dominioCompleto.endsWith(".com")) return null
    return when (dominioCompleto.removeSuffix(".com")) {
        "ventas", "vendedor" -> "Vendedor"
        "tecnico" -> "Técnico"
        "transporte", "transportista" -> "Transportista"
        else -> null
    }
}

class LoginViewModel(private val repositorio: Repositorio) : ViewModel() {
    var correo by mutableStateOf("")
        private set
    var clave by mutableStateOf("")
        private set
    var errorCorreo by mutableStateOf<String?>(null)
        private set
    var errorClave by mutableStateOf<String?>(null)
        private set

    var cargando by mutableStateOf(false)
        private set

    var errorGeneral by mutableStateOf<String?>(null)
        private set

    fun onCorreoChanged(nuevo: String) {
        correo = nuevo
        errorCorreo = null
    }

    fun onClaveChanged(nueva: String) {
        clave = nueva
        errorClave = null
    }

    fun rolActual(): String? = rolDesdeCorreo(correo)

    fun validar(): Boolean {
        val c = correo.trim()
        errorCorreo = when {
            c.isBlank() -> "Ingresa tu correo"
            !FORMATO_CORREO.matches(c) -> "Formato inválido. Ejemplo: tomas@ventas.com"
            rolDesdeCorreo(c) == null -> "Dominio no reconocido. Usa @ventas.com, @tecnico.com o @transporte.com"
            else -> null
        }
        errorClave = when {
            clave.isBlank() -> "Ingresa tu contraseña"
            clave.length < LARGO_MINIMO_CLAVE -> "Mínimo $LARGO_MINIMO_CLAVE caracteres"
            else -> null
        }
        return errorCorreo == null && errorClave == null
    }

    fun iniciarSesion(alExito: (UsuarioDto) -> Unit) {
        if (cargando || !validar()) return
        cargando = true
        errorGeneral = null
        viewModelScope.launch {
            repositorio.iniciarSesion(correo.trim().lowercase(), clave)
                .onSuccess {
                    cargando = false
                    alExito(it)
                }
                .onFailure {
                    cargando = false
                    errorGeneral = it.mensajeAmigable()
                }
        }
    }
}
