package com.tacticaldev.trazabilidad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tacticaldev.trazabilidad.datos.EstadoSync
import com.tacticaldev.trazabilidad.datos.local.entidad.PuntoVenta
import com.tacticaldev.trazabilidad.datos.local.entidad.Visita
import com.tacticaldev.trazabilidad.datos.remoto.mensajeAmigable
import com.tacticaldev.trazabilidad.datos.repositorio.Repositorio
import com.tacticaldev.trazabilidad.dominio.validacion.Validador
import com.tacticaldev.trazabilidad.utilidades.Geocerca
import com.tacticaldev.trazabilidad.utilidades.ProveedorUbicacion
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class VisitaViewModel(
    private val repositorio: Repositorio,
    private val ubicacion: ProveedorUbicacion
) : ViewModel() {

    private val _estadoFormulario = MutableStateFlow(EstadoFormulario())
    val estadoFormulario: StateFlow<EstadoFormulario> = _estadoFormulario.asStateFlow()

    val todasLasVisitas = MutableStateFlow<List<Visita>>(emptyList())
    val totalVisitas = MutableStateFlow(0)
    val fallasCriticas = MutableStateFlow(0)
    val totalPendientes = MutableStateFlow(0)
    val puntosVenta = MutableStateFlow<List<PuntoVenta>>(emptyList())

    val clima = MutableStateFlow<EstadoClima>(EstadoClima.Inactivo)
    private var trabajoClima: Job? = null

    val sincronizando = MutableStateFlow(false)

    val mensaje = MutableStateFlow<String?>(null)

    var correoActivo: String = ""
        private set
    var nombreActivo: String = ""
        private set
    var rolActivo: String = "Técnico"
        private set

    init {
        viewModelScope.launch { repositorio.todasLasVisitas.collect { todasLasVisitas.value = it } }
        viewModelScope.launch { repositorio.cantidadTotal.collect { totalVisitas.value = it } }
        viewModelScope.launch { repositorio.cantidadFallas.collect { fallasCriticas.value = it } }
        viewModelScope.launch { repositorio.cantidadPendientes.collect { totalPendientes.value = it } }
        viewModelScope.launch { repositorio.puntosVenta.collect { puntosVenta.value = it } }
    }

    fun establecerSesion(correo: String, rol: String, nombre: String? = null) {
        correoActivo = correo
        nombreActivo = nombre?.takeIf { it.isNotBlank() }
            ?: correo.substringBefore('@').replaceFirstChar { it.uppercase() }
        rolActivo = rol
        sincronizar(silencioso = true)
    }

    fun cerrarSesion() {
        correoActivo = ""
        nombreActivo = ""
        rolActivo = "Técnico"
        _estadoFormulario.value = EstadoFormulario()
        clima.value = EstadoClima.Inactivo
    }

    fun sincronizar(silencioso: Boolean = false) {
        if (sincronizando.value) return
        viewModelScope.launch {
            sincronizando.value = true
            val r = repositorio.sincronizar()
            sincronizando.value = false
            if (!silencioso || !r.sincronizada) mensaje.value = r.mensaje
        }
    }

    fun mensajeMostrado() {
        mensaje.value = null
    }

    fun alCambiarCodigoActivo(codigo: String) = _estadoFormulario.update {
        val r = Validador.validarCodigoActivo(codigo)
        it.copy(codigoActivo = codigo.uppercase(), errorCodigoActivo = r.mensajeError)
    }

    fun alCambiarNombreTienda(nombre: String) {
        _estadoFormulario.update {
            it.copy(
                nombreTienda = nombre, latitud = null, longitud = null, presenciaVerificada = false,
                distanciaPuntoVentaMetros = null, errorUbicacion = null
            )
        }
        cargarClima(nombre)
    }

    private fun cargarClima(nombreTienda: String) {
        trabajoClima?.cancel()
        val punto = puntosVenta.value.firstOrNull { it.nombre == nombreTienda }
        if (punto == null) {
            clima.value = EstadoClima.Inactivo
            return
        }
        clima.value = EstadoClima.Cargando
        trabajoClima = viewModelScope.launch {
            repositorio.obtenerClima(punto.latitud, punto.longitud)
                .onSuccess { clima.value = EstadoClima.Listo(it) }
                .onFailure { clima.value = EstadoClima.Error(it.mensajeAmigable()) }
        }
    }

    fun alCambiarObservaciones(obs: String) = _estadoFormulario.update {
        it.copy(observaciones = obs, errorObservaciones = Validador.validarObservaciones(obs).mensajeError)
    }

    fun alSeleccionarTipoActivo(tipo: String) = _estadoFormulario.update { it.copy(tipoActivo = tipo) }
    fun alSeleccionarTipoVisita(tipo: String) = _estadoFormulario.update { it.copy(tipoVisita = tipo) }
    fun alSeleccionarEstadoEquipo(estado: String) = _estadoFormulario.update { it.copy(estadoEquipo = estado) }
    fun alCapturarFoto(uri: String) = _estadoFormulario.update { it.copy(uriFoto = uri, errorFoto = null) }

    fun capturarUbicacion() {
        val tienda = _estadoFormulario.value.nombreTienda
        if (tienda.isBlank()) {
            _estadoFormulario.update { it.copy(errorUbicacion = "Selecciona primero el punto de venta") }
            return
        }
        // Las coordenadas del punto de venta vienen del microservicio (copia local en Room).
        val punto = puntosVenta.value.firstOrNull { it.nombre == tienda }
        if (punto == null) {
            _estadoFormulario.update {
                it.copy(errorUbicacion = "No hay coordenadas de este punto de venta. Sincroniza con el servidor e intenta de nuevo.")
            }
            return
        }
        _estadoFormulario.update { it.copy(cargandoUbicacion = true, errorUbicacion = null, presenciaVerificada = false) }
        ubicacion.obtenerUbicacionActual(
            alExito = { gps ->
                val (dentro, distancia) = Geocerca.evaluar(punto.latitud, punto.longitud, gps)
                val precision = gps.precision
                val mensajeError = when {
                    !dentro -> "Presencia no verificada. Distancia al punto: ${distancia?.toInt() ?: 0} m. Radio permitido: ${Geocerca.RADIO_PERMITIDO_METROS.toInt()} m."
                    precision > Geocerca.PRECISION_MAXIMA_METROS -> "GPS obtenido, pero la precisión es baja (${precision.toInt()} m). Intenta nuevamente en un lugar despejado."
                    else -> null
                }
                val verificada = dentro && precision <= Geocerca.PRECISION_MAXIMA_METROS
                _estadoFormulario.update {
                    it.copy(
                        latitud = gps.latitud,
                        longitud = gps.longitud,
                        precisionGps = precision,
                        distanciaPuntoVentaMetros = distancia,
                        presenciaVerificada = verificada,
                        cargandoUbicacion = false,
                        errorUbicacion = mensajeError
                    )
                }
            },
            alError = { error ->
                _estadoFormulario.update {
                    it.copy(cargandoUbicacion = false, errorUbicacion = error.message ?: "Error al obtener GPS")
                }
            }
        )
    }

    fun enviarFormulario(alExito: () -> Unit) {
        val e = _estadoFormulario.value
        val rCodigo = Validador.validarCodigoActivo(e.codigoActivo)
        val rTienda = Validador.validarNombreTienda(e.nombreTienda)
        val rObs = Validador.validarObservaciones(e.observaciones)
        val rFoto = Validador.validarFoto(e.uriFoto)
        val rGeo = Validador.validarUbicacion(e.latitud, e.longitud, e.presenciaVerificada)
        if (listOf(rCodigo, rTienda, rObs, rFoto, rGeo).any { !it.esValido }) {
            _estadoFormulario.update {
                it.copy(
                    errorCodigoActivo = rCodigo.mensajeError, errorNombreTienda = rTienda.mensajeError,
                    errorObservaciones = rObs.mensajeError, errorFoto = rFoto.mensajeError,
                    errorUbicacion = rGeo.mensajeError
                )
            }
            return
        }
        viewModelScope.launch {
            val resultado = repositorio.guardarVisita(
                Visita(
                    correo = correoActivo,
                    nombreOperario = nombreActivo,
                    rolOperario = rolActivo,
                    codigoActivo = e.codigoActivo,
                    tipoActivo = e.tipoActivo,
                    nombreTienda = e.nombreTienda,
                    tipoVisita = e.tipoVisita,
                    estadoEquipo = e.estadoEquipo,
                    observaciones = e.observaciones,
                    rutaFoto = e.uriFoto!!,
                    latitud = e.latitud!!,
                    longitud = e.longitud!!,
                    precisionGps = e.precisionGps ?: 0f,
                    distanciaPuntoVentaMetros = e.distanciaPuntoVentaMetros ?: 0f,
                    presenciaVerificada = e.presenciaVerificada,
                    estadoSincronizacion = EstadoSync.PENDIENTE
                )
            )
            mensaje.value = resultado.mensaje
            _estadoFormulario.update { EstadoFormulario(esGuardadoExitoso = true) }
            alExito()
        }
    }

    fun visita(id: Long): Flow<Visita?> = repositorio.observarVisita(id)

    fun actualizarVisita(
        original: Visita,
        tipoActivo: String,
        tipoVisita: String,
        estadoEquipo: String,
        observaciones: String,
        alTerminar: () -> Unit
    ) {
        val r = Validador.validarObservaciones(observaciones)
        if (!r.esValido) {
            mensaje.value = r.mensajeError
            return
        }
        viewModelScope.launch {
            val res = repositorio.actualizarVisita(
                original.copy(
                    tipoActivo = tipoActivo,
                    tipoVisita = tipoVisita,
                    estadoEquipo = estadoEquipo,
                    observaciones = observaciones.trim()
                )
            )
            mensaje.value = res.mensaje
            alTerminar()
        }
    }

    fun eliminarVisita(visita: Visita, alTerminar: () -> Unit) {
        viewModelScope.launch {
            repositorio.eliminarVisita(visita)
                .onSuccess {
                    mensaje.value = "Visita ${visita.codigoActivo} eliminada"
                    alTerminar()
                }
                .onFailure { mensaje.value = it.mensajeAmigable() }
        }
    }
}
