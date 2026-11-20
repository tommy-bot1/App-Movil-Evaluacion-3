package com.tacticaldev.trazabilidad.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.tacticaldev.trazabilidad.datos.local.entidad.Visita
import com.tacticaldev.trazabilidad.dominio.validacion.Validador
import com.tacticaldev.trazabilidad.ui.viewmodel.VisitaViewModel

private val TIPOS_ACTIVO = listOf("Vitrina freezer", "Conservadora")
private val TIPOS_VISITA = listOf("Mantenimiento Preventivo", "Inspección")
private val ESTADOS_EQUIPO = listOf("Bueno", "Requiere Mantención", "Falla Crítica")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditar(viewModel: VisitaViewModel, id: Long, alVolver: () -> Unit) {
    val flujo = remember(id) { viewModel.visita(id) }
    val visita by flujo.collectAsState(initial = null)
    val snack = remember { SnackbarHostState() }
    EfectoMensaje(viewModel, snack)

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title = { Text("Editar visita") },
                navigationIcon = { IconButton(onClick = alVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
            )
        }
    ) { p ->
        val v = visita
        if (v == null) {
            Box(Modifier.fillMaxSize().padding(p), contentAlignment = Alignment.Center) { Text("Cargando…") }
        } else {
            FormularioEdicion(v, viewModel, alVolver, Modifier.padding(p))
        }
    }
}

@Composable
private fun FormularioEdicion(v: Visita, viewModel: VisitaViewModel, alVolver: () -> Unit, modifier: Modifier) {
    var tipoActivo by rememberSaveable(v.id) { mutableStateOf(v.tipoActivo) }
    var tipoVisita by rememberSaveable(v.id) { mutableStateOf(v.tipoVisita) }
    var estadoEquipo by rememberSaveable(v.id) { mutableStateOf(v.estadoEquipo) }
    var observaciones by rememberSaveable(v.id) { mutableStateOf(v.observaciones) }
    var intento by remember { mutableStateOf(false) }
    val errorObs = if (intento) Validador.validarObservaciones(observaciones).mensajeError else null

    Column(
        modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("${v.codigoActivo} • ${v.nombreTienda}", style = MaterialTheme.typography.titleMedium)
        GrupoOpciones("Tipo de activo", TIPOS_ACTIVO, tipoActivo) { tipoActivo = it }
        GrupoOpciones("Tipo de visita", TIPOS_VISITA, tipoVisita) { tipoVisita = it }
        GrupoOpciones("Estado del equipo", ESTADOS_EQUIPO, estadoEquipo) { estadoEquipo = it }
        OutlinedTextField(
            value = observaciones,
            onValueChange = { observaciones = it },
            label = { Text("Observaciones técnicas") },
            isError = errorObs != null,
            supportingText = { errorObs?.let { Text(it) } },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = {
                intento = true
                viewModel.actualizarVisita(v, tipoActivo, tipoVisita, estadoEquipo, observaciones) { alVolver() }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text("Guardar cambios") }
    }
}

@Composable
private fun GrupoOpciones(titulo: String, opciones: List<String>, seleccion: String, alSeleccionar: (String) -> Unit) {
    Column {
        Text(titulo, style = MaterialTheme.typography.titleSmall)
        opciones.forEach { opcion ->
            Row(
                Modifier.fillMaxWidth().selectable(selected = opcion == seleccion, role = Role.RadioButton, onClick = { alSeleccionar(opcion) }),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = opcion == seleccion, onClick = null)
                Text(opcion, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
