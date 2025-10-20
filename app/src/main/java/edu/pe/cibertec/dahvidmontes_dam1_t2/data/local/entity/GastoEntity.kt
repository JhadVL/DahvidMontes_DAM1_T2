package edu.pe.cibertec.dahvidmontes_dam1_t2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gastos")
data class GastoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val categoriaId: Int,
    val descripcion: String?,
    val monto: Double,
    val fechaMillis: Long
)

