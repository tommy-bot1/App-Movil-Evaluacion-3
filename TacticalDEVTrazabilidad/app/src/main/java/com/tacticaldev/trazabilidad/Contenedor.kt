package com.tacticaldev.trazabilidad

import android.app.Application
import com.tacticaldev.trazabilidad.datos.local.BaseDatos
import com.tacticaldev.trazabilidad.datos.remoto.ClienteRetrofit
import com.tacticaldev.trazabilidad.datos.repositorio.Repositorio
import com.tacticaldev.trazabilidad.utilidades.Gps
import com.tacticaldev.trazabilidad.utilidades.ProveedorUbicacion

class Contenedor(app: Application) {
    private val baseDatos = BaseDatos.obtenerBaseDatos(app)

    val repositorio: Repositorio by lazy {
        Repositorio(
            baseDatos.daoVisita(),
            baseDatos.daoPuntoVenta(),
            ClienteRetrofit.apiVisitas,
            ClienteRetrofit.apiUsuarios,
            ClienteRetrofit.apiClima
        )
    }
    val ubicacion: ProveedorUbicacion by lazy { Gps(app) }
}
