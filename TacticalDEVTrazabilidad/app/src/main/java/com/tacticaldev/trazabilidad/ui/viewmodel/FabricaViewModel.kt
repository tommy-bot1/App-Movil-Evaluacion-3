package com.tacticaldev.trazabilidad.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tacticaldev.trazabilidad.Contenedor
import com.tacticaldev.trazabilidad.TrazabilidadApp

object FabricaViewModel {
    val Fabrica = viewModelFactory {
        initializer { VisitaViewModel(contenedor().repositorio, contenedor().ubicacion) }
        initializer { LoginViewModel(contenedor().repositorio) }
    }
}

private fun CreationExtras.contenedor(): Contenedor =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TrazabilidadApp).contenedor
