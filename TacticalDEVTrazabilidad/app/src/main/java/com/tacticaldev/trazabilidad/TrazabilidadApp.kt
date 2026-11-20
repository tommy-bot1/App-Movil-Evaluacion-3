package com.tacticaldev.trazabilidad

import android.app.Application

class TrazabilidadApp : Application() {
    val contenedor: Contenedor by lazy { Contenedor(this) }
}
