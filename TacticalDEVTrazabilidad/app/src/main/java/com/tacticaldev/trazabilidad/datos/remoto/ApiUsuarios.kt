package com.tacticaldev.trazabilidad.datos.remoto

import com.tacticaldev.trazabilidad.datos.remoto.dto.LoginRequest
import com.tacticaldev.trazabilidad.datos.remoto.dto.PuntoVentaDto
import com.tacticaldev.trazabilidad.datos.remoto.dto.UsuarioDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiUsuarios {
    @POST("api/auth/login")
    suspend fun login(@Body datos: LoginRequest): UsuarioDto

    @GET("api/puntos-venta")
    suspend fun puntosVenta(): List<PuntoVentaDto>
}
