package com.tacticaldev.trazabilidad.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tacticaldev.trazabilidad.datos.local.entidad.Visita
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVisita(visita: Visita): Long

    @Update
    suspend fun actualizarVisita(visita: Visita)

    @Query("DELETE FROM visitas WHERE id = :id")
    suspend fun eliminarPorId(id: Long)

    @Query("SELECT * FROM visitas ORDER BY marcaTiempo DESC")
    fun obtenerTodasLasVisitas(): Flow<List<Visita>>

    @Query("SELECT * FROM visitas WHERE id = :id")
    fun observarPorId(id: Long): Flow<Visita?>

    @Query("SELECT * FROM visitas WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Visita?

    @Query("SELECT * FROM visitas WHERE idRemoto = :idRemoto LIMIT 1")
    suspend fun obtenerPorIdRemoto(idRemoto: Long): Visita?

    @Query("SELECT * FROM visitas WHERE estadoSincronizacion = 'PENDIENTE' ORDER BY marcaTiempo ASC")
    suspend fun obtenerPendientes(): List<Visita>

    @Query("SELECT * FROM visitas WHERE estadoSincronizacion = 'SINCRONIZADO' AND idRemoto IS NOT NULL")
    suspend fun obtenerSincronizadas(): List<Visita>

    @Query("SELECT COUNT(*) FROM visitas")
    fun obtenerTotalVisitas(): Flow<Int>

    @Query("SELECT COUNT(*) FROM visitas WHERE estadoEquipo = 'Falla Crítica'")
    fun obtenerTotalFallasCriticas(): Flow<Int>

    @Query("SELECT COUNT(*) FROM visitas WHERE estadoSincronizacion = 'PENDIENTE'")
    fun obtenerTotalPendientes(): Flow<Int>
}
