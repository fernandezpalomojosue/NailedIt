package com.josuefp.servicenotebook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.local.entity.ProductEntity
import com.josuefp.servicenotebook.data.local.entity.ServiceEntity
import com.josuefp.servicenotebook.data.local.model.ServiceMaterialWithProduct
import com.josuefp.servicenotebook.data.repository.CatalogRepository
import com.josuefp.servicenotebook.ui.theme.ServiceNotebookTheme
import com.josuefp.servicenotebook.ui.viewmodel.CatalogViewModel
import com.josuefp.servicenotebook.ui.viewmodel.CatalogViewModelFactory
import java.util.Locale

@Composable
fun CatalogScreen(
    appName: String = "Virna Nails",
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { CatalogRepository(database) }
    val viewModel: CatalogViewModel = viewModel(factory = CatalogViewModelFactory(repository))

    val services by viewModel.filteredServices.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val selectedServiceMaterials by viewModel.selectedServiceMaterials.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<ServiceEntity?>(null) }

    var showCalculatorDialog by remember { mutableStateOf(false) }
    var serviceForCalculator by remember { mutableStateOf<ServiceEntity?>(null) }

    var serviceToDelete by remember { mutableStateOf<ServiceEntity?>(null) }

    CatalogScreenContent(
        appName = appName,
        services = services,
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::setSearchQuery,
        onNavigateBack = onNavigateBack,
        onAddServiceClick = {
            editingService = null
            viewModel.selectService(null)
            showAddEditDialog = true
        },
        onEditServiceClick = { service ->
            editingService = service
            viewModel.selectService(service.id)
            showAddEditDialog = true
        },
        onDeleteServiceClick = { service ->
            serviceToDelete = service
        },
        onCalculatorClick = { service ->
            serviceForCalculator = service
            viewModel.selectService(service.id)
            showCalculatorDialog = true
        }
    )

    if (showAddEditDialog) {
        AddEditServiceDialog(
            service = editingService,
            allProducts = allProducts,
            initialMaterials = selectedServiceMaterials,
            onDismiss = {
                showAddEditDialog = false
                viewModel.selectService(null)
            },
            onConfirm = { serviceEntity, materials ->
                viewModel.saveService(serviceEntity, materials)
                showAddEditDialog = false
                viewModel.selectService(null)
            }
        )
    }

    if (showCalculatorDialog && serviceForCalculator != null) {
        ServiceCalculatorDialog(
            service = serviceForCalculator!!,
            materials = selectedServiceMaterials,
            onDismiss = {
                showCalculatorDialog = false
                serviceForCalculator = null
                viewModel.selectService(null)
            },
            onApplyPrice = { newPrice ->
                viewModel.saveService(
                    serviceForCalculator!!.copy(basePrice = newPrice),
                    selectedServiceMaterials.map { it.product.id to it.serviceMaterial.estimatedQuantity }
                )
                showCalculatorDialog = false
                serviceForCalculator = null
                viewModel.selectService(null)
            }
        )
    }

    if (serviceToDelete != null) {
        AlertDialog(
            onDismissRequest = { serviceToDelete = null },
            title = { Text("Eliminar servicio") },
            text = { Text("¿Deseas eliminar '${serviceToDelete!!.name}' del catálogo?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteService(serviceToDelete!!)
                        serviceToDelete = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { serviceToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreenContent(
    appName: String,
    services: List<ServiceEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onAddServiceClick: () -> Unit,
    onEditServiceClick: (ServiceEntity) -> Unit,
    onDeleteServiceClick: (ServiceEntity) -> Unit,
    onCalculatorClick: (ServiceEntity) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$appName - Catálogo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddServiceClick) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Servicio")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar servicio por nombre...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true
            )

            if (services.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay servicios en el catálogo. Toca + para agregar el primero.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(services, key = { it.id }) { service ->
                        ServiceCard(
                            service = service,
                            onEditClick = { onEditServiceClick(service) },
                            onDeleteClick = { onDeleteServiceClick(service) },
                            onCalculatorClick = { onCalculatorClick(service) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceCard(
    service: ServiceEntity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCalculatorClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.height(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${service.estimatedDurationMinutes} min",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (service.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = service.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Precio público: $${String.format(Locale.getDefault(), "%.2f", service.basePrice)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Mano de obra: $${String.format(Locale.getDefault(), "%.2f", service.laborCost)} | Gastos: $${String.format(Locale.getDefault(), "%.2f", service.overheadCost)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onCalculatorClick) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Calculadora")
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditServiceDialog(
    service: ServiceEntity?,
    allProducts: List<ProductEntity>,
    initialMaterials: List<ServiceMaterialWithProduct>,
    onDismiss: () -> Unit,
    onConfirm: (service: ServiceEntity, materials: List<Pair<Long, Double>>) -> Unit
) {
    var name by remember { mutableStateOf(service?.name ?: "") }
    var description by remember { mutableStateOf(service?.description ?: "") }
    var durationStr by remember { mutableStateOf(service?.estimatedDurationMinutes?.toString() ?: "60") }
    var laborCostStr by remember { mutableStateOf(service?.laborCost?.toString() ?: "0.0") }
    var overheadCostStr by remember { mutableStateOf(service?.overheadCost?.toString() ?: "0.0") }
    var basePriceStr by remember { mutableStateOf(service?.basePrice?.toString() ?: "0.0") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val attachedMaterials = remember {
        mutableStateListOf<Pair<Long, Double>>().apply {
            addAll(initialMaterials.map { it.product.id to it.serviceMaterial.estimatedQuantity })
        }
    }

    LaunchedEffect(initialMaterials) {
        if (attachedMaterials.isEmpty() && initialMaterials.isNotEmpty()) {
            attachedMaterials.addAll(initialMaterials.map { it.product.id to it.serviceMaterial.estimatedQuantity })
        }
    }

    var selectedProductForAdd by remember { mutableStateOf<ProductEntity?>(null) }
    var materialQtyStr by remember { mutableStateOf("1.0") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (service == null) "Nuevo Servicio" else "Editar Servicio") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    if (errorMessage != null) {
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre del servicio *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción / Detalles") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = durationStr,
                            onValueChange = { durationStr = it },
                            label = { Text("Duración (min)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = basePriceStr,
                            onValueChange = { basePriceStr = it },
                            label = { Text("Precio público ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = laborCostStr,
                            onValueChange = { laborCostStr = it },
                            label = { Text("Mano de obra ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = overheadCostStr,
                            onValueChange = { overheadCostStr = it },
                            label = { Text("Gastos ind. ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Materiales e Insumos requeridos:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small)
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = isDropdownExpanded,
                            onExpandedChange = { isDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedProductForAdd?.name ?: "Seleccionar insumo...",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { isDropdownExpanded = false }
                            ) {
                                allProducts.forEach { product ->
                                    DropdownMenuItem(
                                        text = { Text("${product.name} ($${product.unitCost}/${product.unit})") },
                                        onClick = {
                                            selectedProductForAdd = product
                                            isDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = materialQtyStr,
                                onValueChange = { materialQtyStr = it },
                                label = { Text("Cantidad (${selectedProductForAdd?.unit ?: "unid"})") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    val prod = selectedProductForAdd
                                    val qty = materialQtyStr.toDoubleOrNull()
                                    if (prod != null && qty != null && qty > 0) {
                                        attachedMaterials.add(prod.id to qty)
                                        selectedProductForAdd = null
                                        materialQtyStr = "1.0"
                                    }
                                },
                                enabled = selectedProductForAdd != null
                            ) {
                                Text("Agregar")
                            }
                        }
                    }
                }

                items(attachedMaterials) { (prodId, qty) ->
                    val product = allProducts.find { it.id == prodId }
                    val nameStr = product?.name ?: "Producto #$prodId"
                    val unitStr = product?.unit ?: ""
                    val cost = (product?.unitCost ?: 0.0) * qty

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(nameStr, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("$qty $unitStr (~$${String.format(Locale.getDefault(), "%.2f", cost)})", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { attachedMaterials.remove(prodId to qty) }) {
                            Icon(Icons.Default.Clear, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duration = durationStr.toIntOrNull() ?: 60
                    val laborCost = laborCostStr.toDoubleOrNull() ?: 0.0
                    val overheadCost = overheadCostStr.toDoubleOrNull() ?: 0.0
                    val basePrice = basePriceStr.toDoubleOrNull() ?: 0.0

                    if (name.isBlank()) {
                        errorMessage = "El nombre no puede estar vacío."
                    } else {
                        val entity = (service ?: ServiceEntity(name = name)).copy(
                            name = name.trim(),
                            description = description.trim(),
                            estimatedDurationMinutes = duration,
                            laborCost = laborCost,
                            overheadCost = overheadCost,
                            basePrice = basePrice
                        )
                        onConfirm(entity, attachedMaterials.toList())
                    }
                }
            ) {
                Text(if (service == null) "Crear" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ServiceCalculatorDialog(
    service: ServiceEntity,
    materials: List<ServiceMaterialWithProduct>,
    onDismiss: () -> Unit,
    onApplyPrice: (Double) -> Unit
) {
    val materialsTotalCost = remember(materials) { materials.sumOf { it.totalCost } }
    val totalCost = materialsTotalCost + service.laborCost + service.overheadCost

    var profitMarginPercent by remember { mutableDoubleStateOf(50.0) }
    val suggestedPrice = totalCost * (1.0 + (profitMarginPercent / 100.0))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Calculadora de Precio: ${service.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Desglose de Costos Reales", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("• Insumos / Materiales: $${String.format(Locale.getDefault(), "%.2f", materialsTotalCost)}")
                        Text("• Mano de obra: $${String.format(Locale.getDefault(), "%.2f", service.laborCost)}")
                        Text("• Gastos indirectos: $${String.format(Locale.getDefault(), "%.2f", service.overheadCost)}")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Costo Total: $${String.format(Locale.getDefault(), "%.2f", totalCost)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text("Margen de Ganancia deseado: ${profitMarginPercent.toInt()}%", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = profitMarginPercent.toFloat(),
                    onValueChange = { profitMarginPercent = it.toDouble() },
                    valueRange = 0f..200f,
                    steps = 19
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(20.0, 30.0, 50.0, 75.0, 100.0).forEach { margin ->
                        FilterChip(
                            selected = profitMarginPercent.toInt() == margin.toInt(),
                            onClick = { profitMarginPercent = margin },
                            label = { Text("${margin.toInt()}%") }
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Precio Sugerido: $${String.format(Locale.getDefault(), "%.2f", suggestedPrice)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Precio Actual Cobrado: $${String.format(Locale.getDefault(), "%.2f", service.basePrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onApplyPrice(suggestedPrice) }
            ) {
                Text("Aplicar Precio Sugerido")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CatalogScreenPreview() {
    ServiceNotebookTheme {
        CatalogScreenContent(
            appName = "Virna Nails",
            services = listOf(
                ServiceEntity(id = 1, name = "Manicura Rusa + Semipermanente", description = "Limpieza profunda con torno y esmaltado", basePrice = 35.0, estimatedDurationMinutes = 90, laborCost = 10.0, overheadCost = 5.0),
                ServiceEntity(id = 2, name = "Uñas Esculpidas en Gel", description = "Esculpido completo con molde", basePrice = 50.0, estimatedDurationMinutes = 120, laborCost = 15.0, overheadCost = 8.0)
            ),
            searchQuery = "",
            onSearchQueryChange = {},
            onNavigateBack = {},
            onAddServiceClick = {},
            onEditServiceClick = {},
            onDeleteServiceClick = {},
            onCalculatorClick = {}
        )
    }
}
