package edu.pe.cibertec.dahvidmontes_dam1_t2.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import edu.pe.cibertec.dahvidmontes_dam1_t2.data.local.entity.GastoEntity

@Dao
interface GastoDao {

    @Query("SELECT * FROM gastos ORDER BY fechaMillis DESC")
    fun obtenerTodos(): Flow<List<GastoEntity>>

    @Query("SELECT * FROM gastos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): GastoEntity?

    @Insert
    suspend fun insertar(gasto: GastoEntity): Long

    @Delete
    suspend fun eliminar(gasto: GastoEntity)

    @Query("SELECT IFNULL(SUM(monto),0) FROM gastos WHERE categoriaId = :categoriaId AND fechaMillis >= :inicio AND fechaMillis < :fin")
    suspend fun sumaCategoriaEntre(categoriaId: Int, inicio: Long, fin: Long): Double

    @Query("SELECT IFNULL(SUM(monto),0) FROM gastos")
    suspend fun sumaTotal(): Double

}
