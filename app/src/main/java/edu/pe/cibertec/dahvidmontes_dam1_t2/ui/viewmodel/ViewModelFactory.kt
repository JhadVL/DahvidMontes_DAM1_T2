package edu.pe.cibertec.dahvidmontes_dam1_t2.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.pe.cibertec.dahvidmontes_dam1_t2.data.repository.GastoRepository

class ViewModelFactory(
    private val gastoRepository: GastoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(GastoViewModel::class.java) -> {
                GastoViewModel(gastoRepository) as T
            }
            else -> throw IllegalArgumentException("ViewModel no reconocido: ${modelClass.name}")
        }
    }
}
