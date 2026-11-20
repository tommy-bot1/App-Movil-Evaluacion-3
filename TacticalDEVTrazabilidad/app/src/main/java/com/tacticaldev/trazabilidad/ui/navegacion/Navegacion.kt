package com.tacticaldev.trazabilidad.ui.navegacion

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tacticaldev.trazabilidad.ui.pantallas.PantallaDetalle
import com.tacticaldev.trazabilidad.ui.pantallas.PantallaEditar
import com.tacticaldev.trazabilidad.ui.pantallas.PantallaFormulario
import com.tacticaldev.trazabilidad.ui.pantallas.PantallaHistorial
import com.tacticaldev.trazabilidad.ui.pantallas.PantallaInicio
import com.tacticaldev.trazabilidad.ui.pantallas.PantallaLogin
import com.tacticaldev.trazabilidad.ui.viewmodel.FabricaViewModel
import com.tacticaldev.trazabilidad.ui.viewmodel.VisitaViewModel

object Destinos {
    const val LOGIN = "login"
    const val INICIO = "inicio"
    const val FORMULARIO = "formulario"
    const val HISTORIAL = "historial"
    const val DETALLE = "detalle/{id}"
    const val EDITAR = "editar/{id}"
    fun detalle(id: Long) = "detalle/$id"
    fun editar(id: Long) = "editar/$id"
}

@Composable
fun Navegacion(viewModel: VisitaViewModel = viewModel(factory = FabricaViewModel.Fabrica)) {
    val nav = rememberNavController()
    val argId = listOf(navArgument("id") { type = NavType.LongType })
    NavHost(navController = nav, startDestination = Destinos.LOGIN) {
        composable(Destinos.LOGIN) {
            PantallaLogin(onLoginExitoso = { usuario ->
                viewModel.establecerSesion(usuario.correo, usuario.rol, usuario.nombre)
                nav.navigate(Destinos.INICIO) { popUpTo(Destinos.LOGIN) { inclusive = true } }
            })
        }
        composable(Destinos.INICIO) {
            PantallaInicio(viewModel, { nav.navigate(Destinos.FORMULARIO) }, { nav.navigate(Destinos.HISTORIAL) }) {
                viewModel.cerrarSesion()
                nav.navigate(Destinos.LOGIN) { popUpTo(0) { inclusive = true } }
            }
        }
        composable(Destinos.FORMULARIO) { PantallaFormulario(viewModel) { nav.popBackStack() } }
        composable(Destinos.HISTORIAL) {
            PantallaHistorial(viewModel, { nav.popBackStack() }) { id -> nav.navigate(Destinos.detalle(id)) }
        }
        composable(Destinos.DETALLE, arguments = argId) { entrada ->
            val id = entrada.arguments?.getLong("id") ?: 0L
            PantallaDetalle(viewModel, id, { nav.popBackStack() }) { idEditar -> nav.navigate(Destinos.editar(idEditar)) }
        }
        composable(Destinos.EDITAR, arguments = argId) { entrada ->
            val id = entrada.arguments?.getLong("id") ?: 0L
            PantallaEditar(viewModel, id) { nav.popBackStack() }
        }
    }
}
