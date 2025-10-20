package edu.pe.cibertec.dahvidmontes_dam1_t2.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import edu.pe.cibertec.dahvidmontes_dam1_t2.data.local.entity.GastoEntity
import edu.pe.cibertec.dahvidmontes_dam1_t2.ui.viewmodel.GastoViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaGastosScreen(
    viewModel: GastoViewModel
) {
    val gastos by viewModel.gastos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val scope = rememberCoroutineScope()


    var mostrarDialogo by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var mostrarSnackbar by remember { mutableStateOf(false) }
    var mensajeAdvertencia by remember { mutableStateOf<String?>(null) }

    // 🧮 Total general
    val totalGeneral = gastos.sumOf { it.monto }

    // 🔻 Estados para eliminar
    var gastoSeleccionado by remember { mutableStateOf<GastoEntity?>(null) }
    var mostrarSheet by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    // ✅ Snackbar éxito guardar
    if (mostrarSnackbar) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar("✓ Gasto guardado correctamente")
            mostrarSnackbar = false
        }
    }

    // ✅ Snackbar advertencia
    mensajeAdvertencia?.let { mensaje ->
        LaunchedEffect(mensaje) {
            snackbarHostState.showSnackbar(
                message = mensaje,
                withDismissAction = true
            )
            mensajeAdvertencia = null
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    val mensaje = data.visuals.message

                    Snackbar(
                        snackbarData = data,
                        containerColor = if (mensaje.startsWith("⚠"))
                            Color(0xFFFFA726)
                        else MaterialTheme.colorScheme.primaryContainer
                    )
                }
            )
        },
        topBar = {
            TopAppBar(
                title = { Text("Mis Gastos") },
                actions = {
                    FloatingActionButton(
                        onClick = { mostrarDialogo = true },
                        containerColor = Color(0xFF4CAF50), // Verde Material
                        contentColor = Color.White,
                        modifier = Modifier
                            .size(40.dp) // Tamaño más pequeño que el FAB normal
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar Gasto"
                        )
                    }
                },


                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Error: $error")
                        Button(onClick = { viewModel.limpiarError() }) {
                            Text("Reintentar")
                        }
                    }
                }
                gastos.isEmpty() -> Text(
                    "NO HAY GASTOS. PRESIONA + PARA AGREGAR.",
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(gastos) { gasto ->
                            GastoItem(
                                gasto = gasto,
                                onDelete = { viewModel.eliminarGasto(gasto) },
                                onClick = {
                                    gastoSeleccionado = gasto
                                    mostrarSheet = true
                                }
                            )
                        }

                        // 🧮 Item final: total general
                        item {
                            Text(
                                text = "Total: -S/. %.2f".format(totalGeneral),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // 🪟 Dialogo de agregar gasto
    if (mostrarDialogo) {
        AgregarGastoDialog(
            onDismiss = { mostrarDialogo = false },
            onConfirm = { descripcion, monto, categoriaId, fechaMillis ->
                viewModel.validarLimiteYGardarGasto(
                    categoriaId = categoriaId,
                    descripcion = descripcion,
                    monto = monto,
                    fechaMillis = fechaMillis
                ) { mensaje ->
                    mensajeAdvertencia = mensaje
                }
                mostrarDialogo = false
                mostrarSnackbar = true
            }
        )
    }

    // ⬇️ BottomSheet con opciones
    if (mostrarSheet && gastoSeleccionado != null) {
        ModalBottomSheet(onDismissRequest = { mostrarSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = {
                        mostrarSheet = false
                        mostrarConfirmacion = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🗑 Eliminar", color = MaterialTheme.colorScheme.error)
                }
                Divider()
                TextButton(
                    onClick = { mostrarSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("✕ Cancelar")
                }
            }
        }
    }

    // ⚠️ Confirmar eliminación
    if (mostrarConfirmacion && gastoSeleccionado != null) {
        val gasto = gastoSeleccionado!!
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Confirmar eliminación") },
            text = {
                Text("¿Eliminar este gasto de S/. %.2f?".format(gasto.monto))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarGasto(gasto)
                        mostrarConfirmacion = false
                        gastoSeleccionado = null
                        scope.launch {
                            snackbarHostState.showSnackbar("🗑 Gasto eliminado")
                        }
                    }
                ) { Text("Eliminar") }
            }
            ,
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}


fun colorCategoria(categoriaId: Int): Color {
    return when (categoriaId) {
        1 -> Color(0xFFDFF6E0) // Verde claro (Alimentación)
        2 -> Color(0xFFDDEBFF) // Azul claro (Transporte)
        3 -> Color(0xFFE8D9FF) // Púrpura claro (Entretenimiento)
        4 -> Color(0xFFFFD9D9) // Rojo claro (Vivienda)
        5 -> Color(0xFFFFE5E5) // Rojo más claro (Salud)
        6 -> Color(0xFFF2E0D0) // Café claro (Café/Bebidas)
        7 -> Color(0xFFFFE8CC) // Naranja claro (Compras)
        8 -> Color(0xFFE6E6E6) // Gris claro (Otros)
        else -> Color(0xFFF5F5F5) // Por defecto
    }
}

@Composable
fun GastoItem(
    gasto: GastoEntity,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
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

    val textoDescripcion = gasto.descripcion?.takeIf { it.isNotBlank() }
        ?: nombresCategorias[gasto.categoriaId]
        ?: "(Sin descripción)"

    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val fechaTexto = formato.format(Date(gasto.fechaMillis))
    val montoTexto = "-S/. %.2f".format(gasto.monto)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // 🟢 Detectar clic para abrir el BottomSheet
        colors = CardDefaults.cardColors(
            containerColor = colorCategoria(gasto.categoriaId)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = textoDescripcion,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Fecha: $fechaTexto",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = montoTexto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error, // 🔴 monto en rojo
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarGastoDialog(
    onDismiss: () -> Unit,
    onConfirm: (descripcion: String, monto: Double, categoriaId: Int, fechaMillis: Long) -> Unit
) {
    var descripcion by remember { mutableStateOf("") }
    var monto by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Alimentación") }

    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var fechaTexto by remember { mutableStateOf(formato.format(Date())) }
    var fechaMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    val context = LocalContext.current

    val categorias = listOf(
        "Alimentación", "Transporte", "Entretenimiento", "Vivienda",
        "Salud", "Café/Bebidas", "Compras", "Otros"
    )

    // Estados para AlertDialog de validación
    var mostrarErrorMonto by remember { mutableStateOf(false) }
    var mostrarErrorCategoria by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Nuevo Gasto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                OutlinedTextField(
                    value = monto,
                    onValueChange = { monto = it },
                    label = { Text("Monto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fechaTexto,
                    onValueChange = {},
                    label = { Text("Fecha") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            val calendario = Calendar.getInstance()
                            val datePickerDialog = DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    calendario.set(year, month, dayOfMonth)
                                    fechaTexto = formato.format(calendario.time)
                                    fechaMillis = calendario.timeInMillis
                                },
                                calendario.get(Calendar.YEAR),
                                calendario.get(Calendar.MONTH),
                                calendario.get(Calendar.DAY_OF_MONTH)
                            )
                            datePickerDialog.show()
                        }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Seleccionar fecha"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = categoriaSeleccionada,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categorias.forEachIndexed { index, nombre ->
                            val limite = when (nombre) {
                                "Alimentación" -> 800.0
                                "Transporte" -> 300.0
                                "Entretenimiento" -> 200.0
                                "Vivienda" -> 1500.0
                                "Salud" -> 400.0
                                "Café/Bebidas" -> 150.0
                                "Compras" -> 500.0
                                "Otros" -> 300.0
                                else -> 0.0
                            }

                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(colorCategoria(index + 1))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = nombre,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Límite: $${"%.2f".format(limite)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.DarkGray
                                        )
                                    }
                                },
                                onClick = {
                                    categoriaSeleccionada = nombre
                                    expanded = false
                                }
                            )
                        }

                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val montoDouble = monto.toDoubleOrNull() ?: 0.0
                    val categoriaInt = categorias.indexOf(categoriaSeleccionada) + 1

                    // ✅ Validaciones
                    when {
                        montoDouble <= 0 -> mostrarErrorMonto = true
                        categoriaSeleccionada.isBlank() -> mostrarErrorCategoria = true
                        else -> {
                            onConfirm(descripcion, montoDouble, categoriaInt, fechaMillis)
                            // Limpiar campos después de guardar
                            descripcion = ""
                            monto = ""
                            categoriaSeleccionada = "Alimentación"
                            fechaTexto = formato.format(Date())
                        }
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    // AlertDialog para error de monto
    if (mostrarErrorMonto) {
        AlertDialog(
            onDismissRequest = { mostrarErrorMonto = false },
            title = { Text("Error") },
            text = { Text("El monto debe ser mayor a 0.") },
            confirmButton = {
                TextButton(onClick = { mostrarErrorMonto = false }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // AlertDialog para error de categoría
    if (mostrarErrorCategoria) {
        AlertDialog(
            onDismissRequest = { mostrarErrorCategoria = false },
            title = { Text("Error") },
            text = { Text("Debe seleccionar una categoría válida.") },
            confirmButton = {
                TextButton(onClick = { mostrarErrorCategoria = false }) {
                    Text("Aceptar")
                }
            }
        )
    }
}
