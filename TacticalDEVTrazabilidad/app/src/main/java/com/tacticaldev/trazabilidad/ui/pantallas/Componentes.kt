package com.tacticaldev.trazabilidad.ui.pantallas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tacticaldev.trazabilidad.datos.EstadoSync
import com.tacticaldev.trazabilidad.ui.viewmodel.EstadoClima
import com.tacticaldev.trazabilidad.ui.viewmodel.VisitaViewModel

@Composable
fun EfectoMensaje(viewModel: VisitaViewModel, snack: SnackbarHostState) {
    val mensaje by viewModel.mensaje.collectAsState()
    LaunchedEffect(mensaje) {
        mensaje?.let {
            snack.showSnackbar(it)
            viewModel.mensajeMostrado()
        }
    }
}

@Composable
fun EtiquetaSync(estado: String) {
    val color = when (estado) {
        EstadoSync.SINCRONIZADO -> Color(0xFF2E7D32)
        EstadoSync.RECHAZADO -> Color(0xFFC62828)
        else -> Color(0xFFEF6C00)
    }
    Surface(color = color, shape = RoundedCornerShape(50)) {
        Text(
            estado,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun TarjetaClima(estado: EstadoClima) {
    when (estado) {
        EstadoClima.Inactivo -> Unit
        EstadoClima.Cargando -> Card(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(12.dp))
                Text("Consultando el clima del punto de venta…")
            }
        }
        is EstadoClima.Listo -> Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Cloud, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Clima en el punto de venta", style = MaterialTheme.typography.titleSmall)
                }
                Text("${estado.clima.temperaturaC.toInt()} °C • ${estado.clima.descripcion}", style = MaterialTheme.typography.titleMedium)
                val detalle = listOfNotNull(
                    estado.clima.humedadPct?.let { "Humedad $it %" },
                    estado.clima.vientoKmh?.let { "Viento ${it.toInt()} km/h" }
                ).joinToString(" • ")
                if (detalle.isNotEmpty()) Text(detalle, style = MaterialTheme.typography.bodySmall)
                estado.clima.alerta?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        }
        is EstadoClima.Error -> Card(Modifier.fillMaxWidth()) {
            Text(
                "Clima no disponible: ${estado.mensaje}",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
