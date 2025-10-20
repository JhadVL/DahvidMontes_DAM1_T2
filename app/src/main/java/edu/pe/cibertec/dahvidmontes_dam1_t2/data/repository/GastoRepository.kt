package edu.pe.cibertec.dahvidmontes_dam1_t2.data.repository

import edu.pe.cibertec.dahvidmontes_dam1_t2.data.local.dao.GastoDao
import edu.pe.cibertec.dahvidmontes_dam1_t2.data.local.entity.GastoEntity
import kotlinx.coroutines.flow.Flow

class GastoRepository(private val gastoDao: GastoDao) {

    fun obtenerTodos(): Flow<List<GastoEntity>> {
        return gastoDao.obtenerTodos()
    }

    suspend fun buscarPorId(id: Int): GastoEntity? {
        return gastoDao.buscarPorId(id)
    }

    suspend fun insertar(gasto: GastoEntity): Long {
        return gastoDao.insertar(gasto)
    }

    suspend fun eliminar(gasto: GastoEntity) {
        gastoDao.eliminar(gasto)
    }

    suspend fun sumaCategoriaEntre(categoriaId: Int, inicio: Long, fin: Long): Double {
        return gastoDao.sumaCategoriaEntre(categoriaId, inicio, fin)
    }

    suspend fun sumaTotal(): Double {
        return gastoDao.sumaTotal()
    }

    suspend fun totalMensualPorCategoria(categoriaId: Int): Double {
        val calendar = java.util.Calendar.getInstance()

        // Inicio del mes
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val inicioMes = calendar.timeInMillis

        // Fin del mes
        calendar.add(java.util.Calendar.MONTH, 1)
        val finMes = calendar.timeInMillis

        return gastoDao.sumaCategoriaEntre(categoriaId, inicioMes, finMes)
    }


}
