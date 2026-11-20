package com.tacticaldev.trazabilidad.datos.remoto

import com.tacticaldev.trazabilidad.datos.remoto.dto.RespuestaClima
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiClima {
    @GET("v1/forecast")
    suspend fun climaActual(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current") variables: String,
        @Query("timezone") zonaHoraria: String
    ): RespuestaClima
}
