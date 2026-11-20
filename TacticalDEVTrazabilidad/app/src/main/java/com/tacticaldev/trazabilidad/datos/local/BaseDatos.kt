package com.tacticaldev.trazabilidad.datos.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tacticaldev.trazabilidad.datos.local.dao.PuntoVentaDao
import com.tacticaldev.trazabilidad.datos.local.dao.VisitaDao
import com.tacticaldev.trazabilidad.datos.local.entidad.PuntoVenta
import com.tacticaldev.trazabilidad.datos.local.entidad.Visita

@Database(entities = [Visita::class, PuntoVenta::class], version = 3, exportSchema = false)
abstract class BaseDatos : RoomDatabase() {
    abstract fun daoVisita(): VisitaDao
    abstract fun daoPuntoVenta(): PuntoVentaDao

    companion object {
        @Volatile
        private var INSTANCIA: BaseDatos? = null

        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE visitas ADD COLUMN rolOperario TEXT NOT NULL DEFAULT 'Técnico'")
                db.execSQL("ALTER TABLE visitas ADD COLUMN tipoActivo TEXT NOT NULL DEFAULT 'Vitrina freezer'")
                db.execSQL("ALTER TABLE visitas ADD COLUMN precisionGps REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE visitas ADD COLUMN distanciaPuntoVentaMetros REAL NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE visitas ADD COLUMN presenciaVerificada INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE visitas ADD COLUMN estadoSincronizacion TEXT NOT NULL DEFAULT 'PENDIENTE'")
            }
        }

        private val MIGRACION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE visitas ADD COLUMN idRemoto INTEGER")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS puntos_venta (" +
                        "id INTEGER NOT NULL, nombre TEXT NOT NULL, latitud REAL NOT NULL, " +
                        "longitud REAL NOT NULL, PRIMARY KEY(id))"
                )
            }
        }

        fun obtenerBaseDatos(contexto: Context): BaseDatos {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    contexto.applicationContext,
                    BaseDatos::class.java,
                    "base_datos_tacticaldev"
                )
                    .addMigrations(MIGRACION_1_2, MIGRACION_2_3)
                    .build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}
