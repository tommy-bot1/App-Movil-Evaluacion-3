package com.tacticaldev.trazabilidad.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tacticaldev.trazabilidad.datos.remoto.dto.UsuarioDto
import com.tacticaldev.trazabilidad.ui.viewmodel.FabricaViewModel
import com.tacticaldev.trazabilidad.ui.viewmodel.LoginViewModel

@Composable
fun PantallaLogin(
    onLoginExitoso: (UsuarioDto) -> Unit,
    viewModel: LoginViewModel = viewModel(factory = FabricaViewModel.Fabrica)
) {
    val rol = viewModel.rolActual()
    var verClave by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Tactical-Trazabilidad",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Trazabilidad y Verificación de Visitas",
            modifier = Modifier.padding(bottom = 24.dp)
        )
        OutlinedTextField(
            value = viewModel.correo,
            onValueChange = viewModel::onCorreoChanged,
            label = { Text("Correo de acceso") },
            placeholder = { Text("nombre@ventas.com") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            isError = viewModel.errorCorreo != null,
            supportingText = {
                Text(viewModel.errorCorreo ?: "Dominios: @ventas.com, @tecnico.com, @transporte.com")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        if (rol != null) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Perfil detectado: $rol",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.clave,
            onValueChange = viewModel::onClaveChanged,
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { verClave = !verClave }) {
                    Icon(
                        if (verClave) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (verClave) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            visualTransformation = if (verClave) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = viewModel.errorClave != null,
            supportingText = { viewModel.errorClave?.let { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        viewModel.errorGeneral?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { viewModel.iniciarSesion { usuario -> onLoginExitoso(usuario) } },
            enabled = !viewModel.cargando,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (viewModel.cargando) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text("Ingresar")
            }
        }
    }
}
