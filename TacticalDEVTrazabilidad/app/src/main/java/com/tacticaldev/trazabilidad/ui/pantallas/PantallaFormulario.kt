package com.tacticaldev.trazabilidad.ui.pantallas

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tacticaldev.trazabilidad.dominio.validacion.Validador
import com.tacticaldev.trazabilidad.ui.viewmodel.VisitaViewModel
import com.tacticaldev.trazabilidad.utilidades.Geocerca
import com.tacticaldev.trazabilidad.utilidades.Camara
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val titulosPasos = listOf("Activo", "Detalle", "Evidencia", "Resumen")
private val sugerenciasObservacion = listOf(
    "Equipo limpio",
    "Temperatura correcta",
    "Ruido anormal",
    "Puerta no sella bien",
    "Fuga de agua",
    "Sin energía"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PantallaFormulario(viewModel: VisitaViewModel, alVolver: () -> Unit) {
    val e by viewModel.estadoFormulario.collectAsState()
    val puntosVenta by viewModel.puntosVenta.collectAsState()
    val clima by viewModel.clima.collectAsState()
    val ctx = LocalContext.current
    var foto by rememberSaveable { mutableStateOf<Uri?>(null) }
    var menu by remember { mutableStateOf(false) }
    var paso by rememberSaveable { mutableIntStateOf(0) }
    var mostrarErrores by remember { mutableStateOf(false) }
    val fechaHora = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
    }
    val verde = MaterialTheme.colorScheme.primary

    // Validaciones locales para controlar el avance entre pasos
    val rCodigo = Validador.validarCodigoActivo(e.codigoActivo)
    val rTienda = Validador.validarNombreTienda(e.nombreTienda)
    val rObs = Validador.validarObservaciones(e.observaciones)
    val rFoto = Validador.validarFoto(e.uriFoto)
    val rGeo = Validador.validarUbicacion(e.latitud, e.longitud, e.presenciaVerificada)

    val errCodigo = e.errorCodigoActivo ?: if (mostrarErrores) rCodigo.mensajeError else null
    val errTienda = if (mostrarErrores) rTienda.mensajeError else null
    val errObs = e.errorObservaciones ?: if (mostrarErrores) rObs.mensajeError else null
    val errFoto = e.errorFoto ?: if (mostrarErrores) rFoto.mensajeError else null
    val errUbicacion = e.errorUbicacion ?: if (mostrarErrores) rGeo.mensajeError else null

    val pasoValido = when (paso) {
        0 -> rCodigo.esValido && rTienda.esValido
        1 -> rObs.esValido
        2 -> rFoto.esValido && rGeo.esValido
        else -> true
    }

    val cam = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok && foto != null) viewModel.alCapturarFoto(foto.toString())
    }
    val geo = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { p ->
        if (p.values.any { it }) viewModel.capturarUbicacion()
    }
    val camPerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) {
            foto = Camara.crearArchivoImagen(ctx)
            cam.launch(foto!!)
        }
    }

    val progreso by animateFloatAsState(targetValue = (paso + 1) / 4f, label = "progreso")

    BackHandler(enabled = paso > 0) {
        paso--
        mostrarErrores = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de visita") },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (paso > 0) {
                        OutlinedButton(
                            onClick = { paso--; mostrarErrores = false },
                            modifier = Modifier.weight(1f).height(52.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Atrás")
                        }
                    }
                    Button(
                        onClick = {
                            if (paso < 3) {
                                if (pasoValido) {
                                    paso++
                                    mostrarErrores = false
                                } else {
                                    mostrarErrores = true
                                }
                            } else {
                                viewModel.enviarFormulario { alVolver() }
                            }
                        },
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) {
                        if (paso < 3) {
                            Text("Siguiente")
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                        } else {
                            Icon(Icons.Default.CheckCircle, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Guardar visita")
                        }
                    }
                }
            }
        }
    ) { p ->
        Column(Modifier.fillMaxSize().padding(p)) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                IndicadorPasos(paso)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }

            AnimatedContent(
                targetState = paso,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it / 3 } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it / 3 } + fadeOut())
                    } else {
                        (slideInHorizontally { -it / 3 } + fadeIn()) togetherWith
                            (slideOutHorizontally { it / 3 } + fadeOut())
                    }
                },
                label = "pasos",
                modifier = Modifier.weight(1f)
            ) { pasoActual ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (pasoActual) {
                        // ---------- PASO 1: ACTIVO Y PUNTO DE VENTA ----------
                        0 -> {
                            SeccionTitulo("¿Qué activo estás visitando?", "Identifica el equipo y el punto de venta.")
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.Person, null)
                                    Column {
                                        Text(
                                            "${viewModel.nombreActivo} • ${viewModel.rolActivo}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text("Inicio de visita: $fechaHora", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = e.codigoActivo,
                                onValueChange = { viewModel.alCambiarCodigoActivo(it) },
                                label = { Text("Código del activo (ej. VTR-884)") },
                                isError = errCodigo != null,
                                supportingText = { errCodigo?.let { Text(it) } },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text("Tipo de activo", style = MaterialTheme.typography.titleSmall)
                            TarjetaOpcion(
                                titulo = "Vitrina freezer",
                                descripcion = "Vitrina refrigerada de exhibición",
                                icono = Icons.Default.Kitchen,
                                seleccionado = e.tipoActivo == "Vitrina freezer",
                                acento = verde,
                                onClick = { viewModel.alSeleccionarTipoActivo("Vitrina freezer") }
                            )
                            TarjetaOpcion(
                                titulo = "Conservadora",
                                descripcion = "Equipo de conservación de productos",
                                icono = Icons.Default.AcUnit,
                                seleccionado = e.tipoActivo == "Conservadora",
                                acento = verde,
                                onClick = { viewModel.alSeleccionarTipoActivo("Conservadora") }
                            )
                            ExposedDropdownMenuBox(expanded = menu, onExpandedChange = { menu = !menu }) {
                                OutlinedTextField(
                                    value = e.nombreTienda,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Punto de venta") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                                    isError = errTienda != null,
                                    supportingText = { errTienda?.let { Text(it) } },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(menu) }
                                )
                                ExposedDropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                    if (puntosVenta.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("Sin puntos de venta. Sincroniza con el servidor.") },
                                            onClick = { menu = false },
                                            enabled = false
                                        )
                                    }
                                    puntosVenta.forEach { punto ->
                                        DropdownMenuItem(
                                            text = { Text(punto.nombre) },
                                            onClick = {
                                                viewModel.alCambiarNombreTienda(punto.nombre)
                                                menu = false
                                            }
                                        )
                                    }
                                }
                            }
                            TarjetaClima(clima)
                        }

                        // ---------- PASO 2: DETALLE DE LA VISITA ----------
                        1 -> {
                            SeccionTitulo("Detalle de la visita", "Indica qué hiciste y en qué estado quedó el equipo.")
                            Text("Tipo de visita", style = MaterialTheme.typography.titleSmall)
                            TarjetaOpcion(
                                titulo = "Mantenimiento Preventivo",
                                descripcion = "Revisión programada del equipo",
                                icono = Icons.Default.Build,
                                seleccionado = e.tipoVisita == "Mantenimiento Preventivo",
                                acento = verde,
                                onClick = { viewModel.alSeleccionarTipoVisita("Mantenimiento Preventivo") }
                            )
                            TarjetaOpcion(
                                titulo = "Inspección",
                                descripcion = "Verificación de estado sin intervención",
                                icono = Icons.Default.Search,
                                seleccionado = e.tipoVisita == "Inspección",
                                acento = verde,
                                onClick = { viewModel.alSeleccionarTipoVisita("Inspección") }
                            )
                            Text("Estado del equipo", style = MaterialTheme.typography.titleSmall)
                            TarjetaOpcion(
                                titulo = "Bueno",
                                descripcion = "Funciona con normalidad",
                                icono = Icons.Default.CheckCircle,
                                seleccionado = e.estadoEquipo == "Bueno",
                                acento = colorEstado("Bueno"),
                                onClick = { viewModel.alSeleccionarEstadoEquipo("Bueno") }
                            )
                            TarjetaOpcion(
                                titulo = "Requiere Mantención",
                                descripcion = "Necesita atención programada",
                                icono = Icons.Default.Warning,
                                seleccionado = e.estadoEquipo == "Requiere Mantención",
                                acento = colorEstado("Requiere Mantención"),
                                onClick = { viewModel.alSeleccionarEstadoEquipo("Requiere Mantención") }
                            )
                            TarjetaOpcion(
                                titulo = "Falla Crítica",
                                descripcion = "Equipo detenido o con riesgo",
                                icono = Icons.Default.Error,
                                seleccionado = e.estadoEquipo == "Falla Crítica",
                                acento = colorEstado("Falla Crítica"),
                                onClick = { viewModel.alSeleccionarEstadoEquipo("Falla Crítica") }
                            )
                            Text("Observaciones técnicas", style = MaterialTheme.typography.titleSmall)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                sugerenciasObservacion.forEach { s ->
                                    AssistChip(
                                        onClick = {
                                            val base = e.observaciones.trimEnd().trimEnd('.')
                                            viewModel.alCambiarObservaciones(
                                                if (base.isBlank()) s else "$base. $s"
                                            )
                                        },
                                        label = { Text(s) }
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = e.observaciones,
                                onValueChange = { viewModel.alCambiarObservaciones(it) },
                                label = { Text("Describe lo observado") },
                                isError = errObs != null,
                                supportingText = {
                                    Text(errObs ?: "${e.observaciones.length} caracteres")
                                },
                                minLines = 4,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // ---------- PASO 3: EVIDENCIA Y PRESENCIA ----------
                        2 -> {
                            SeccionTitulo("Evidencia de la visita", "Foto del equipo y verificación de tu presencia en el punto de venta.")
                            Card(Modifier.fillMaxWidth()) {
                                Column(
                                    Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("Evidencia fotográfica", style = MaterialTheme.typography.titleMedium)
                                    e.uriFoto?.let {
                                        AsyncImage(
                                            model = it,
                                            contentDescription = "Evidencia",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(200.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                    }
                                    Button(onClick = { camPerm.launch(android.Manifest.permission.CAMERA) }) {
                                        Icon(Icons.Default.CameraAlt, null)
                                        Spacer(Modifier.width(8.dp))
                                        Text(if (e.uriFoto == null) "Tomar foto" else "Repetir foto")
                                    }
                                    errFoto?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                                }
                            }
                            val colorUbicacion = when {
                                e.presenciaVerificada -> MaterialTheme.colorScheme.primaryContainer
                                errUbicacion != null -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            Card(
                                Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = colorUbicacion)
                            ) {
                                Column(
                                    Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("Verificación de presencia física", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "Debes estar a menos de ${Geocerca.RADIO_PERMITIDO_METROS.toInt()} m de ${if (e.nombreTienda.isBlank()) "el punto de venta" else e.nombreTienda}.",
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center
                                    )
                                    Button(
                                        onClick = {
                                            geo.launch(
                                                arrayOf(
                                                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        },
                                        enabled = !e.cargandoUbicacion
                                    ) {
                                        if (e.cargandoUbicacion) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        } else {
                                            Icon(Icons.Default.LocationOn, null)
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(if (e.cargandoUbicacion) "Verificando..." else "Verificar ubicación")
                                    }
                                    e.latitud?.let { lat ->
                                        e.longitud?.let { lon ->
                                            Text("GPS: ${"%.6f".format(lat)}, ${"%.6f".format(lon)}")
                                        }
                                    }
                                    e.distanciaPuntoVentaMetros?.let {
                                        Text("Distancia al punto: ${it.toInt()} m • Precisión: ${(e.precisionGps ?: 0f).toInt()} m")
                                    }
                                    Text(
                                        if (e.presenciaVerificada) "✓ Presencia verificada" else "✕ Presencia pendiente",
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (e.presenciaVerificada) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                    errUbicacion?.let {
                                        Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }

                        // ---------- PASO 4: RESUMEN ----------
                        else -> {
                            SeccionTitulo("Revisa y confirma", "Verifica que todo esté correcto antes de guardar.")
                            Card(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    FilaResumen("Fecha y hora", fechaHora)
                                    FilaResumen("Operario", viewModel.correoActivo)
                                    FilaResumen("Perfil", viewModel.rolActivo)
                                    HorizontalDivider()
                                    FilaResumen("Activo", e.codigoActivo)
                                    FilaResumen("Tipo de activo", e.tipoActivo)
                                    FilaResumen("Punto de venta", e.nombreTienda)
                                    HorizontalDivider()
                                    FilaResumen("Tipo de visita", e.tipoVisita)
                                    FilaResumen("Estado", e.estadoEquipo, colorEstado(e.estadoEquipo))
                                    FilaResumen("Observaciones", e.observaciones)
                                    HorizontalDivider()
                                    FilaResumen(
                                        "Presencia",
                                        if (e.presenciaVerificada) "Verificada (${(e.distanciaPuntoVentaMetros ?: 0f).toInt()} m)" else "No verificada",
                                        if (e.presenciaVerificada) verde else MaterialTheme.colorScheme.error
                                    )
                                    FilaResumen("Registro", "Se guarda en el equipo y queda PENDIENTE de sincronización")
                                }
                            }
                            e.uriFoto?.let {
                                AsyncImage(
                                    model = it,
                                    contentDescription = "Evidencia",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun colorEstado(estado: String): Color = when (estado) {
    "Bueno" -> Color(0xFF2E7D32)
    "Requiere Mantención" -> Color(0xFFF9A825)
    "Falla Crítica" -> Color(0xFFC62828)
    else -> Color.Unspecified
}

@Composable
private fun SeccionTitulo(titulo: String, subtitulo: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            subtitulo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IndicadorPasos(paso: Int) {
    Row(Modifier.fillMaxWidth()) {
        titulosPasos.forEachIndexed { i, titulo ->
            val activo = i <= paso
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (i < paso) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                "${i + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (activo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    titulo,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (i == paso) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TarjetaOpcion(
    titulo: String,
    descripcion: String?,
    icono: ImageVector,
    seleccionado: Boolean,
    acento: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (seleccionado) acento.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            if (seleccionado) 2.dp else 1.dp,
            if (seleccionado) acento else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icono,
                contentDescription = null,
                tint = if (seleccionado) acento else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(Modifier.weight(1f)) {
                Text(
                    titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                )
                descripcion?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (seleccionado) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = acento)
            }
        }
    }
}

@Composable
private fun FilaResumen(etiqueta: String, valor: String, colorValor: Color = Color.Unspecified) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            etiqueta,
            modifier = Modifier.weight(0.4f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            valor,
            modifier = Modifier.weight(0.6f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = colorValor,
            textAlign = TextAlign.End
        )
    }
}
