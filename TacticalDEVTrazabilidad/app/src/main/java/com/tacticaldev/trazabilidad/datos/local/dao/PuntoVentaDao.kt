package com.tacticaldev.trazabilidad.datos.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.tacticaldev.trazabilidad.datos.local.entidad.PuntoVenta
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PuntoVentaDao {
    @Query("SELECT * FROM puntos_venta ORDER BY nombre")
    abstract fun observarTodos(): Flow<List<PuntoVenta>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarTodos(puntos: List<PuntoVenta>)

    @Query("DELETE FROM puntos_venta")
    abstract suspend fun borrarTodos()

    @Transaction
    open suspend fun reemplazarTodos(puntos: List<PuntoVenta>) {
        borrarTodos()
        insertarTodos(puntos)
    }
}
