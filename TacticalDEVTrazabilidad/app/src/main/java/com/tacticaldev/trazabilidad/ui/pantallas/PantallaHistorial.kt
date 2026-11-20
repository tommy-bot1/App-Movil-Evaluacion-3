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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tacticaldev.trazabilidad.ui.viewmodel.VisitaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaHistorial(viewModel: VisitaViewModel, alVolver: () -> Unit, alAbrirDetalle: (Long) -> Unit) {
    val visitas by viewModel.todasLasVisitas.collectAsState()
    val sincronizando by viewModel.sincronizando.collectAsState()
    var filtro by remember { mutableStateOf("") }
    val snack = remember { SnackbarHostState() }
    EfectoMensaje(viewModel, snack)
    val filtradas = visitas.filter {
        filtro.isBlank() || it.codigoActivo.contains(filtro, true) || it.nombreTienda.contains(filtro, true)
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title = { Text("Historial de Activos") },
                navigationIcon = { IconButton(onClick = alVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
                actions = {
                    IconButton(onClick = { viewModel.sincronizar() }, enabled = !sincronizando) {
                        Icon(Icons.Default.Sync, "Sincronizar")
                    }
                }
            )
        }
    ) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(16.dp)) {
            OutlinedTextField(
                value = filtro, onValueChange = { filtro = it },
                label = { Text("Buscar por activo o punto de venta") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            if (filtradas.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay registros que coincidan.") }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filtradas, key = { it.id }) { v ->
                        Card(onClick = { alAbrirDetalle(v.id) }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("${v.codigoActivo} • ${v.tipoActivo}", style = MaterialTheme.typography.titleMedium)
                                    EtiquetaSync(v.estadoSincronizacion)
                                }
                                Text(v.nombreTienda)
                                Text("Perfil: ${v.rolOperario}")
                                Text("Visita: ${v.tipoVisita} • Estado: ${v.estadoEquipo}")
                                Text("Fecha: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(v.marcaTiempo))}")
                                Text("Presencia: ${if (v.presenciaVerificada) "VERIFICADA" else "NO VERIFICADA"}")
                                Text("Observaciones: ${v.observaciones}", maxLines = 2)
                                if (v.rutaFoto.isNotEmpty()) {
                                    Spacer(Modifier.height(8.dp))
                                    AsyncImage(model = v.rutaFoto, contentDescription = "Evidencia", modifier = Modifier.fillMaxWidth().height(150.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
