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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
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
import com.josuefp.servicenotebook.ui.viewmodel.CatalogSortOrder
import com.josuefp.servicenotebook.ui.viewmodel.CatalogTab
import com.josuefp.servicenotebook.ui.viewmodel.CatalogViewModel
import com.josuefp.servicenotebook.ui.viewmodel.CatalogViewModelFactory
import com.josuefp.servicenotebook.ui.viewmodel.LiveQuoteState
import com.josuefp.servicenotebook.ui.viewmodel.QuoteItem
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

    val selectedTab by viewModel.selectedTab.collectAsState()
    val services by viewModel.filteredServices.collectAsState()
    val allServices by viewModel.services.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()

    val allProducts by viewModel.allProducts.collectAsState()
    val selectedServiceMaterials by viewModel.selectedServiceMaterials.collectAsState()
    val quoteState by viewModel.quoteState.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<ServiceEntity?>(null) }

    var showCalculatorDialog by remember { mutableStateOf(false) }
    var serviceForCalculator by remember { mutableStateOf<ServiceEntity?>(null) }

    var serviceToDelete by remember { mutableStateOf<ServiceEntity?>(null) }

    CatalogScreenContent(
        appName = appName,
        selectedTab = selectedTab,
        services = services,
        allServices = allServices,
        searchQuery = searchQuery,
        selectedCategory = selectedCategory,
        sortOrder = sortOrder,
        quoteState = quoteState,
        onTabSelected = viewModel::setSelectedTab,
        onSearchQueryChange = viewModel::setSearchQuery,
        onCategorySelected = viewModel::setSelectedCategory,
        onSortOrderSelected = viewModel::setSortOrder,
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
        },
        onAddServiceToQuote = viewModel::addServiceToQuote,
        onUpdateQuoteItemQuantity = viewModel::updateQuoteItemQuantity,
        onRemoveQuoteItem = viewModel::removeServiceFromQuote,
        onSetQuoteDiscount = viewModel::setQuoteDiscount,
        onClearQuote = viewModel::clearQuote
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
    selectedTab: CatalogTab,
    services: List<ServiceEntity>,
    allServices: List<ServiceEntity>,
    searchQuery: String,
    selectedCategory: String,
    sortOrder: CatalogSortOrder,
    quoteState: LiveQuoteState,
    onTabSelected: (CatalogTab) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onSortOrderSelected: (CatalogSortOrder) -> Unit,
    onNavigateBack: () -> Unit,
    onAddServiceClick: () -> Unit,
    onEditServiceClick: (ServiceEntity) -> Unit,
    onDeleteServiceClick: (ServiceEntity) -> Unit,
    onCalculatorClick: (ServiceEntity) -> Unit,
    onAddServiceToQuote: (ServiceEntity) -> Unit,
    onUpdateQuoteItemQuantity: (serviceId: Long, newQty: Int) -> Unit,
    onRemoveQuoteItem: (serviceId: Long) -> Unit,
    onSetQuoteDiscount: (Double) -> Unit,
    onClearQuote: () -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }
    val categories = listOf("Todos", "Manos", "Pies", "Decoraciones", "Otros")

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
            if (selectedTab == CatalogTab.SERVICES_LIST) {
                FloatingActionButton(onClick = onAddServiceClick) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo Servicio")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab.ordinal
            ) {
                Tab(
                    selected = selectedTab == CatalogTab.SERVICES_LIST,
                    onClick = { onTabSelected(CatalogTab.SERVICES_LIST) },
                    text = { Text("Servicios (${services.size})") }
                )
                Tab(
                    selected = selectedTab == CatalogTab.LIVE_CALCULATOR,
                    onClick = { onTabSelected(CatalogTab.LIVE_CALCULATOR) },
                    text = {
                        if (quoteState.items.isNotEmpty()) {
                            BadgedBox(
                                badge = { Badge { Text("${quoteState.items.sumOf { it.quantity }}") } }
                            ) {
                                Text("Calculadora en Vivo")
                            }
                        } else {
                            Text("Calculadora en Vivo")
                        }
                    }
                )
            }

            when (selectedTab) {
                CatalogTab.SERVICES_LIST -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = onSearchQueryChange,
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Buscar servicio...") },
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

                            Spacer(modifier = Modifier.width(8.dp))

                            Box {
                                IconButton(onClick = { showSortMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Ordenar")
                                }
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    CatalogSortOrder.entries.forEach { order ->
                                        DropdownMenuItem(
                                            text = { Text(order.label) },
                                            onClick = {
                                                onSortOrderSelected(order)
                                                showSortMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Category Filter Chips Row
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { category ->
                                FilterChip(
                                    selected = selectedCategory.equals(category, ignoreCase = true),
                                    onClick = { onCategorySelected(category) },
                                    label = { Text(category) }
                                )
                            }
                        }

                        if (services.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No se encontraron servicios en esta sección.",
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
                                        onCalculatorClick = { onCalculatorClick(service) },
                                        onAddToQuoteClick = { onAddServiceToQuote(service) }
                                    )
                                }
                            }
                        }
                    }
                }
                CatalogTab.LIVE_CALCULATOR -> {
                    LiveCalculatorSection(
                        allServices = allServices,
                        quoteState = quoteState,
                        onAddService = onAddServiceToQuote,
                        onUpdateQuantity = onUpdateQuoteItemQuantity,
                        onRemoveItem = onRemoveQuoteItem,
                        onSetDiscount = onSetQuoteDiscount,
                        onClearQuote = onClearQuote
                    )
                }
            }
        }
    }
}

@Composable
fun LiveCalculatorSection(
    allServices: List<ServiceEntity>,
    quoteState: LiveQuoteState,
    onAddService: (ServiceEntity) -> Unit,
    onUpdateQuantity: (serviceId: Long, newQty: Int) -> Unit,
    onRemoveItem: (serviceId: Long) -> Unit,
    onSetDiscount: (Double) -> Unit,
    onClearQuote: () -> Unit
) {
    var discountStr by remember { mutableStateOf(if (quoteState.discount > 0) quoteState.discount.toString() else "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Presupuesto Total",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$${String.format(Locale.getDefault(), "%.2f", quoteState.total)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Duración Est.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${quoteState.totalDurationMinutes} min",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (quoteState.items.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onClearQuote,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Limpiar Todo")
                        }

                        OutlinedTextField(
                            value = discountStr,
                            onValueChange = {
                                discountStr = it
                                onSetDiscount(it.toDoubleOrNull() ?: 0.0)
                            },
                            label = { Text("Descuento ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.width(130.dp)
                        )
                    }
                }
            }
        }

        Text(
            text = "Servicios Seleccionados (${quoteState.items.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (quoteState.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Selecciona servicios abajo para sumar precios en tiempo real.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quoteState.items, key = { it.service.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.service.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$${String.format(Locale.getDefault(), "%.2f", item.service.basePrice)} c/u • ${item.service.estimatedDurationMinutes} min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                IconButton(onClick = { onUpdateQuantity(item.service.id, item.quantity - 1) }) {
                                    Text("-", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = "${item.quantity}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(onClick = { onUpdateQuantity(item.service.id, item.quantity + 1) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Sumar")
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$${String.format(Locale.getDefault(), "%.2f", item.service.basePrice * item.quantity)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(onClick = { onRemoveItem(item.service.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "Catálogo de Servicios Disponibles",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(allServices, key = { it.id }) { service ->
                val inQuoteCount = quoteState.items.find { it.service.id == service.id }?.quantity ?: 0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(service.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Sección: ${service.category} • $${String.format(Locale.getDefault(), "%.2f", service.basePrice)} • ${service.estimatedDurationMinutes} min",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Button(
                            onClick = { onAddService(service) }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (inQuoteCount > 0) "Sumar ($inQuoteCount)" else "Agregar")
                        }
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
    onCalculatorClick: () -> Unit,
    onAddToQuoteClick: () -> Unit
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

            Text(
                text = "Sección: ${service.category}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onAddToQuoteClick) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Cotizar")
                }

                Row {
                    IconButton(onClick = onCalculatorClick) {
                        Icon(Icons.Default.Info, contentDescription = "Calculadora de Costos")
                    }
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
    var category by remember { mutableStateOf(service?.category ?: "Manos") }
    var durationStr by remember { mutableStateOf(service?.estimatedDurationMinutes?.toString() ?: "60") }
    var laborCostStr by remember { mutableStateOf(service?.laborCost?.toString() ?: "0.0") }
    var overheadCostStr by remember { mutableStateOf(service?.overheadCost?.toString() ?: "0.0") }
    var basePriceStr by remember { mutableStateOf(service?.basePrice?.toString() ?: "0.0") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val categoriesList = listOf("Manos", "Pies", "Decoraciones", "Otros")

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

                    Text("Sección / Categoría *", style = MaterialTheme.typography.labelMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categoriesList.forEach { cat ->
                            FilterChip(
                                selected = category.equals(cat, ignoreCase = true),
                                onClick = { category = cat },
                                label = { Text(cat) }
                            )
                        }
                    }

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
                            category = category.trim(),
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
            selectedTab = CatalogTab.SERVICES_LIST,
            services = listOf(
                ServiceEntity(id = 1, name = "Manicura Rusa + Semipermanente", category = "Manos", description = "Limpieza profunda con torno y esmaltado", basePrice = 35.0, estimatedDurationMinutes = 90, laborCost = 10.0, overheadCost = 5.0),
                ServiceEntity(id = 2, name = "Pedicura Completa con Gel", category = "Pies", description = "Exfoliación y esmaltado de pies", basePrice = 45.0, estimatedDurationMinutes = 90, laborCost = 12.0, overheadCost = 6.0)
            ),
            allServices = listOf(
                ServiceEntity(id = 1, name = "Manicura Rusa + Semipermanente", category = "Manos", description = "Limpieza profunda con torno y esmaltado", basePrice = 35.0, estimatedDurationMinutes = 90, laborCost = 10.0, overheadCost = 5.0),
                ServiceEntity(id = 2, name = "Pedicura Completa con Gel", category = "Pies", description = "Exfoliación y esmaltado de pies", basePrice = 45.0, estimatedDurationMinutes = 90, laborCost = 12.0, overheadCost = 6.0)
            ),
            searchQuery = "",
            selectedCategory = "Todos",
            sortOrder = CatalogSortOrder.ALPHABETICAL,
            quoteState = LiveQuoteState(),
            onTabSelected = {},
            onSearchQueryChange = {},
            onCategorySelected = {},
            onSortOrderSelected = {},
            onNavigateBack = {},
            onAddServiceClick = {},
            onEditServiceClick = {},
            onDeleteServiceClick = {},
            onCalculatorClick = {},
            onAddServiceToQuote = {},
            onUpdateQuoteItemQuantity = { _, _ -> },
            onRemoveQuoteItem = {},
            onSetQuoteDiscount = {},
            onClearQuote = {}
        )
    }
}
