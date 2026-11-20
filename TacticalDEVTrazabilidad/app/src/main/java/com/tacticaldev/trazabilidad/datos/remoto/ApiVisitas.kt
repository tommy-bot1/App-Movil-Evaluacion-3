package com.tacticaldev.trazabilidad.datos.remoto

import com.tacticaldev.trazabilidad.datos.remoto.dto.VisitaDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiVisitas {
    @GET("api/visitas")
    suspend fun listar(): List<VisitaDto>

    @GET("api/visitas/{id}")
    suspend fun obtener(@Path("id") id: Long): VisitaDto

    @POST("api/visitas")
    suspend fun crear(@Body visita: VisitaDto): VisitaDto

    @PUT("api/visitas/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body visita: VisitaDto): VisitaDto

    @DELETE("api/visitas/{id}")
    suspend fun eliminar(@Path("id") id: Long): Response<Unit>
}
