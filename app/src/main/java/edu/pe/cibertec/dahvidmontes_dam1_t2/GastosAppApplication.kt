package edu.pe.cibertec.dahvidmontes_dam1_t2

import android.app.Application
import edu.pe.cibertec.dahvidmontes_dam1_t2.data.local.database.AppDatabase
import edu.pe.cibertec.dahvidmontes_dam1_t2.data.repository.GastoRepository

class GastosAppApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val gastoRepository by lazy { GastoRepository(database.gastoDao()) }
}
