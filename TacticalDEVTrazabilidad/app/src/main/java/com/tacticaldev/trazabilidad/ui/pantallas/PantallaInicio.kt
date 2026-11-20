package com.tacticaldev.trazabilidad.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tacticaldev.trazabilidad.ui.viewmodel.VisitaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaInicio(
    viewModel: VisitaViewModel,
    alNavegarAFormulario: () -> Unit,
    alNavegarAHistorial: () -> Unit,
    alCerrarSesion: () -> Unit
) {
    val total by viewModel.totalVisitas.collectAsState()
    val fallas by viewModel.fallasCriticas.collectAsState()
    val pendientes by viewModel.totalPendientes.collectAsState()
    val sincronizando by viewModel.sincronizando.collectAsState()
    val snack = remember { SnackbarHostState() }
    EfectoMensaje(viewModel, snack)

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title = { Text("Tactical-Trazabilidad") },
                actions = {
                    if (sincronizando) {
                        CircularProgressIndicator(Modifier.padding(horizontal = 12.dp).height(24.dp).width(24.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = { viewModel.sincronizar() }) { Icon(Icons.Default.Sync, "Sincronizar") }
                    }
                    IconButton(onClick = alCerrarSesion) { Icon(Icons.AutoMirrored.Filled.ExitToApp, "Cerrar sesión") }
                }
            )
        }
    ) { p ->
        Column(
            Modifier.fillMaxSize().padding(p).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Usuario: ${viewModel.correoActivo}")
                    Text("Perfil: ${viewModel.rolActivo}")
                    Spacer(Modifier.height(8.dp))
                    Text("Visitas registradas: $total")
                    Text("Alertas críticas: $fallas", color = MaterialTheme.colorScheme.error)
                    Text("Pendientes de sincronizar: $pendientes")
                }
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = alNavegarAFormulario, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Nueva Visita Técnica")
            }
            OutlinedButton(onClick = alNavegarAHistorial, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Icon(Icons.Default.History, null)
                Spacer(Modifier.width(8.dp))
                Text("Historial de Visitas")
            }
            Spacer(Modifier.weight(1f))
        }
    }
}
