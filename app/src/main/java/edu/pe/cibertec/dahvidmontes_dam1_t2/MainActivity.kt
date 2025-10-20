package edu.pe.cibertec.dahvidmontes_dam1_t2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import edu.pe.cibertec.dahvidmontes_dam1_t2.ui.screens.ListaGastosScreen
import edu.pe.cibertec.dahvidmontes_dam1_t2.ui.theme.GastosAppTheme
import edu.pe.cibertec.dahvidmontes_dam1_t2.ui.viewmodel.GastoViewModel
import edu.pe.cibertec.dahvidmontes_dam1_t2.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    private val gastoViewModel: GastoViewModel by viewModels {
        val app = application as GastosAppApplication
        ViewModelFactory(app.gastoRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GastosAppTheme {
                ListaGastosScreen(viewModel = gastoViewModel)
            }
        }
    }
}
