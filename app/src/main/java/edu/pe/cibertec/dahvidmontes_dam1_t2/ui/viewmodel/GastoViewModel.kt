package edu.pe.cibertec.dahvidmontes_dam1_t2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.pe.cibertec.dahvidmontes_dam1_t2.data.local.entity.GastoEntity
import edu.pe.cibertec.dahvidmontes_dam1_t2.data.repository.GastoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GastoViewModel(
    private val repository: GastoRepository
) : ViewModel() {

    private val _gastos = MutableStateFlow<List<GastoEntity>>(emptyList())
    val gastos: StateFlow<List<GastoEntity>> = _gastos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        cargarGastos()
    }

    private fun cargarGastos() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.obtenerTodos().collect { lista ->
                    _gastos.value = lista
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar gastos: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun insertarGasto(categoriaId: Int, descripcion: String?, monto: Double, fechaMillis: Long) {
        viewModelScope.launch {
            try {
                val gasto = GastoEntity(
                    categoriaId = categoriaId,
                    descripcion = descripcion,
                    monto = monto,
                    fechaMillis = fechaMillis
                )
                repository.insertar(gasto)
            } catch (e: Exception) {
                _error.value = "Error al guardar: ${e.message}"
            }
        }
    }

    fun actualizarGasto(gasto: GastoEntity) {
        viewModelScope.launch {
            try {
                repository.insertar(gasto) // en Room, insertar con mismo ID reemplaza
            } catch (e: Exception) {
                _error.value = "Error al actualizar: ${e.message}"
            }
        }
    }

    fun eliminarGasto(gasto: GastoEntity) {
        viewModelScope.launch {
            try {
                repository.eliminar(gasto)
            } catch (e: Exception) {
                _error.value = "Error al eliminar: ${e.message}"
            }
        }
    }

    fun limpiarError() {
        _error.value = null
    }

    fun validarLimiteYGardarGasto(
        categoriaId: Int,
        descripcion: String?,
        monto: Double,
        fechaMillis: Long,
        onLimiteExcedido: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val limites = mapOf(
                    1 to 800.0,  // Alimentación
                    2 to 300.0,  // Transporte
                    3 to 200.0,  // Entretenimiento
                    4 to 1500.0, // Vivienda
                    5 to 450.0,  // Salud
                    6 to 150.0,  // Café/Bebidas
                    7 to 500.0,  // Compras
                    8 to 300.0   // Otros
                )

                val nombresCategorias = mapOf(
                    1 to "Alimentación",
                    2 to "Transporte",
                    3 to "Entretenimiento",
                    4 to "Vivienda",
                    5 to "Salud",
                    6 to "Café/Bebidas",
                    7 to "Compras",
                    8 to "Otros"
                )

                val limite = limites[categoriaId] ?: Double.MAX_VALUE
                val totalActual = repository.totalMensualPorCategoria(categoriaId)
                val nuevoTotal = totalActual + monto

                if (nuevoTotal > limite) {
                    val nombreCategoria = nombresCategorias[categoriaId] ?: "Categoría"
                    val mensaje = "⚠ Has excedido el límite de $nombreCategoria: S/. %.2f de S/. %.2f".format(nuevoTotal, limite)
                    onLimiteExcedido(mensaje)
                }

                val gasto = GastoEntity(
                    categoriaId = categoriaId,
                    descripcion = descripcion,
                    monto = monto,
                    fechaMillis = fechaMillis
                )
                repository.insertar(gasto)
                cargarGastos()

            } catch (e: Exception) {
                _error.value = "Error al guardar gasto: ${e.message}"
            }
        }
    }



}
