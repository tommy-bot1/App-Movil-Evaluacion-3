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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun PantallaDetalle(viewModel: VisitaViewModel, id: Long, alVolver: () -> Unit, alEditar: (Long) -> Unit) {
    val flujo = remember(id) { viewModel.visita(id) }
    val visita by flujo.collectAsState(initial = null)
    var confirmar by remember { mutableStateOf(false) }
    val snack = remember { SnackbarHostState() }
    EfectoMensaje(viewModel, snack)

    Scaffold(
        snackbarHost = { SnackbarHost(snack) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle de visita") },
                navigationIcon = { IconButton(onClick = alVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
            )
        }
    ) { p ->
        val v = visita
        if (v == null) {
            Box(Modifier.fillMaxSize().padding(p), contentAlignment = Alignment.Center) { Text("Cargando…") }
        } else {
            Column(
                Modifier.fillMaxSize().padding(p).padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(v.codigoActivo, style = MaterialTheme.typography.headlineSmall)
                    EtiquetaSync(v.estadoSincronizacion)
                }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Tipo de activo: ${v.tipoActivo}")
                        Text("Punto de venta: ${v.nombreTienda}")
                        Text("Tipo de visita: ${v.tipoVisita}")
                        Text("Estado del equipo: ${v.estadoEquipo}")
                        Text("Operario: ${v.nombreOperario} (${v.rolOperario})")
                        Text("Fecha: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(v.marcaTiempo))}")
                        Text("Presencia: ${if (v.presenciaVerificada) "VERIFICADA" else "NO VERIFICADA"}")
                        Text("Distancia al punto: ${v.distanciaPuntoVentaMetros.toInt()} m • Precisión GPS: ${v.precisionGps.toInt()} m")
                        Text("Observaciones: ${v.observaciones}")
                        v.idRemoto?.let { Text("ID en servidor: $it", style = MaterialTheme.typography.bodySmall) }
                    }
                }
                if (v.rutaFoto.isNotEmpty()) {
                    AsyncImage(model = v.rutaFoto, contentDescription = "Evidencia", modifier = Modifier.fillMaxWidth().height(220.dp))
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { alEditar(v.id) }, modifier = Modifier.weight(1f).height(52.dp)) {
                        Icon(Icons.Default.Edit, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Editar")
                    }
                    OutlinedButton(
                        onClick = { confirmar = true },
                        modifier = Modifier.weight(1f).height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Eliminar")
                    }
                }
            }
        }
    }

    val v = visita
    if (confirmar && v != null) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("Eliminar visita") },
            text = { Text("¿Eliminar la visita de ${v.codigoActivo}? También se borrará del servidor. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmar = false
                    viewModel.eliminarVisita(v) { alVolver() }
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } }
        )
    }
}
