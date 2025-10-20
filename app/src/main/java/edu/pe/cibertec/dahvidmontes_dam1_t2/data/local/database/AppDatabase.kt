package edu.pe.cibertec.dahvidmontes_dam1_t2.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import edu.pe.cibertec.dahvidmontes_dam1_t2.data.local.dao.GastoDao
import edu.pe.cibertec.dahvidmontes_dam1_t2.data.local.entity.GastoEntity

@Database(entities = [GastoEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun gastoDao(): GastoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gastos_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
