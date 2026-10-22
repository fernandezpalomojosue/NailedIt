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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.local.entity.InventoryMovementEntity
import com.josuefp.servicenotebook.data.local.entity.ProductEntity
import com.josuefp.servicenotebook.data.repository.InventoryRepository
import com.josuefp.servicenotebook.ui.theme.ServiceNotebookTheme
import com.josuefp.servicenotebook.ui.viewmodel.InventoryTab
import com.josuefp.servicenotebook.ui.viewmodel.InventoryViewModel
import com.josuefp.servicenotebook.ui.viewmodel.InventoryViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InventoryScreen(
    appName: String = "Virna Nails",
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { InventoryRepository(database) }
    val viewModel: InventoryViewModel = viewModel(factory = InventoryViewModelFactory(repository))

    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val allProducts by viewModel.products.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val movements by viewModel.movements.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    var showMovementDialog by remember { mutableStateOf(false) }
    var selectedProductForMovement by remember { mutableStateOf<ProductEntity?>(null) }

    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    InventoryScreenContent(
        appName = appName,
        products = filteredProducts,
        lowStockProducts = lowStockProducts,
        movements = movements,
        allProductsMap = remember(allProducts) { allProducts.associateBy { it.id } },
        searchQuery = searchQuery,
        selectedTab = selectedTab,
        onSearchQueryChange = viewModel::setSearchQuery,
        onTabSelected = viewModel::setSelectedTab,
        onNavigateBack = onNavigateBack,
        onAddProductClick = {
            editingProduct = null
            showAddEditDialog = true
        },
        onEditProductClick = { product ->
            editingProduct = product
            showAddEditDialog = true
        },
        onDeleteProductClick = { product ->
            productToDelete = product
        },
        onRegisterMovementClick = { product ->
            selectedProductForMovement = product
            showMovementDialog = true
        }
    )

    if (showAddEditDialog) {
        AddEditProductDialog(
            product = editingProduct,
            onDismiss = { showAddEditDialog = false },
            onConfirm = { name, unit, cost, stock, minStock, notes ->
                if (editingProduct == null) {
                    viewModel.addProduct(name, unit, cost, stock, minStock, notes)
                } else {
                    viewModel.updateProduct(
                        editingProduct!!.copy(
                            name = name,
                            unit = unit,
                            unitCost = cost,
                            stockQuantity = stock,
                            minStock = minStock,
                            notes = notes
                        )
                    )
                }
                showAddEditDialog = false
            }
        )
    }

    if (showMovementDialog && selectedProductForMovement != null) {
        RegisterMovementDialog(
            product = selectedProductForMovement!!,
            onDismiss = {
                showMovementDialog = false
                selectedProductForMovement = null
            },
            onConfirm = { type, quantity, reason ->
                viewModel.registerMovement(selectedProductForMovement!!.id, type, quantity, reason)
                showMovementDialog = false
                selectedProductForMovement = null
            }
        )
    }

    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Eliminar producto") },
            text = { Text("¿Deseas eliminar '${productToDelete!!.name}'? Esta acción eliminará también su historial de movimientos.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(productToDelete!!)
                        productToDelete = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreenContent(
    appName: String,
    products: List<ProductEntity>,
    lowStockProducts: List<ProductEntity>,
    movements: List<InventoryMovementEntity>,
    allProductsMap: Map<Long, ProductEntity>,
    searchQuery: String,
    selectedTab: InventoryTab,
    onSearchQueryChange: (String) -> Unit,
    onTabSelected: (InventoryTab) -> Unit,
    onNavigateBack: () -> Unit,
    onAddProductClick: () -> Unit,
    onEditProductClick: (ProductEntity) -> Unit,
    onDeleteProductClick: (ProductEntity) -> Unit,
    onRegisterMovementClick: (ProductEntity) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$appName - Inventario") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab != InventoryTab.MOVEMENTS_HISTORY) {
                FloatingActionButton(onClick = onAddProductClick) {
                    Icon(Icons.Default.Add, contentDescription = "Nuevo Producto")
                }
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
                placeholder = { Text("Buscar producto o nota...") },
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

            PrimaryTabRow(
                selectedTabIndex = selectedTab.ordinal
            ) {
                Tab(
                    selected = selectedTab == InventoryTab.ALL_PRODUCTS,
                    onClick = { onTabSelected(InventoryTab.ALL_PRODUCTS) },
                    text = { Text("Productos (${products.size})") }
                )
                Tab(
                    selected = selectedTab == InventoryTab.LOW_STOCK_ALERTS,
                    onClick = { onTabSelected(InventoryTab.LOW_STOCK_ALERTS) },
                    text = {
                        if (lowStockProducts.isNotEmpty()) {
                            BadgedBox(
                                badge = { Badge { Text("${lowStockProducts.size}") } }
                            ) {
                                Text("Stock Bajo")
                            }
                        } else {
                            Text("Stock Bajo")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == InventoryTab.MOVEMENTS_HISTORY,
                    onClick = { onTabSelected(InventoryTab.MOVEMENTS_HISTORY) },
                    text = { Text("Movimientos") }
                )
            }

            when (selectedTab) {
                InventoryTab.ALL_PRODUCTS -> {
                    ProductListSection(
                        products = products,
                        onEditClick = onEditProductClick,
                        onDeleteClick = onDeleteProductClick,
                        onMovementClick = onRegisterMovementClick
                    )
                }
                InventoryTab.LOW_STOCK_ALERTS -> {
                    LowStockListSection(
                        lowStockProducts = lowStockProducts,
                        onMovementClick = onRegisterMovementClick
                    )
                }
                InventoryTab.MOVEMENTS_HISTORY -> {
                    MovementsListSection(
                        movements = movements,
                        productsMap = allProductsMap
                    )
                }
            }
        }
    }
}

@Composable
fun ProductListSection(
    products: List<ProductEntity>,
    onEditClick: (ProductEntity) -> Unit,
    onDeleteClick: (ProductEntity) -> Unit,
    onMovementClick: (ProductEntity) -> Unit
) {
    if (products.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No se encontraron productos. Presiona + para agregar el primero.",
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
            items(products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    onEditClick = { onEditClick(product) },
                    onDeleteClick = { onDeleteClick(product) },
                    onMovementClick = { onMovementClick(product) }
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMovementClick: () -> Unit
) {
    val isLowStock = product.stockQuantity <= product.minStock

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLowStock) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
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
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (isLowStock) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.error,
                                shape = MaterialTheme.shapes.extraSmall
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.height(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Stock Bajo",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Existencia: ${product.stockQuantity} ${product.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Costo: $${String.format(Locale.getDefault(), "%.2f", product.unitCost)} / ${product.unit}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (product.minStock > 0) {
                Text(
                    text = "Stock Mínimo: ${product.minStock} ${product.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (product.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notas: ${product.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onMovementClick) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Movimiento")
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

@Composable
fun LowStockListSection(
    lowStockProducts: List<ProductEntity>,
    onMovementClick: (ProductEntity) -> Unit
) {
    if (lowStockProducts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "¡Excelente! No hay ningún producto por debajo del stock mínimo.",
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
            items(lowStockProducts, key = { it.id }) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Existencia actual: ${product.stockQuantity} ${product.unit} (Mínimo requerido: ${product.minStock} ${product.unit})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onMovementClick(product) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Registrar Entrada")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MovementsListSection(
    movements: List<InventoryMovementEntity>,
    productsMap: Map<Long, ProductEntity>
) {
    if (movements.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No se ha registrado ningún movimiento de inventario.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(movements, key = { it.id }) { movement ->
                val productName = productsMap[movement.productId]?.name ?: "Producto #${movement.productId}"
                val unit = productsMap[movement.productId]?.unit ?: ""

                val (chipLabel, chipColor) = when (movement.movementType) {
                    "ENTRY" -> "ENTRADA" to Color(0xFF2E7D32)
                    "EXIT" -> "SALIDA" to MaterialTheme.colorScheme.error
                    else -> "AJUSTE" to Color(0xFFE65100)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = productName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = chipLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier
                                    .background(chipColor, shape = MaterialTheme.shapes.extraSmall)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Cantidad: ${movement.quantity} $unit",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = dateFormat.format(Date(movement.date)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (movement.reason.isNotBlank()) {
                            Text(
                                text = "Motivo: ${movement.reason}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditProductDialog(
    product: ProductEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, unit: String, unitCost: Double, stockQuantity: Double, minStock: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var unit by remember { mutableStateOf(product?.unit ?: "ml") }
    var unitCostStr by remember { mutableStateOf(product?.unitCost?.toString() ?: "0.0") }
    var stockQuantityStr by remember { mutableStateOf(product?.stockQuantity?.toString() ?: "0.0") }
    var minStockStr by remember { mutableStateOf(product?.minStock?.toString() ?: "0.0") }
    var notes by remember { mutableStateOf(product?.notes ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val popularUnits = listOf("ml", "g", "unidad", "frasco", "paquete")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Agregar Producto" else "Editar Producto") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del producto *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Unidad de medida:", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    popularUnits.forEach { u ->
                        FilterChip(
                            selected = unit.equals(u, ignoreCase = true),
                            onClick = { unit = u },
                            label = { Text(u) }
                        )
                    }
                }

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unidad personalizada") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = unitCostStr,
                    onValueChange = { unitCostStr = it },
                    label = { Text("Costo unitario ($) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = stockQuantityStr,
                    onValueChange = { stockQuantityStr = it },
                    label = { Text("Existencia inicial *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = minStockStr,
                    onValueChange = { minStockStr = it },
                    label = { Text("Existencia mínima (alerta)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas / Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = unitCostStr.toDoubleOrNull()
                    val stock = stockQuantityStr.toDoubleOrNull()
                    val minStock = minStockStr.toDoubleOrNull() ?: 0.0

                    if (name.isBlank()) {
                        errorMessage = "El nombre no puede estar vacío."
                    } else if (cost == null || cost < 0) {
                        errorMessage = "Ingresa un costo unitario válido."
                    } else if (stock == null || stock < 0) {
                        errorMessage = "Ingresa una existencia válida."
                    } else {
                        onConfirm(name.trim(), unit.trim(), cost, stock, minStock, notes.trim())
                    }
                }
            ) {
                Text(if (product == null) "Agregar" else "Guardar")
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
fun RegisterMovementDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirm: (type: String, quantity: Double, reason: String) -> Unit
) {
    var selectedType by remember { mutableStateOf("ENTRY") }
    var quantityStr by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Movimiento: ${product.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Stock actual: ${product.stockQuantity} ${product.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Text("Tipo de movimiento:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == "ENTRY",
                        onClick = { selectedType = "ENTRY" },
                        label = { Text("Entrada (+)") }
                    )
                    FilterChip(
                        selected = selectedType == "EXIT",
                        onClick = { selectedType = "EXIT" },
                        label = { Text("Salida (-)") }
                    )
                    FilterChip(
                        selected = selectedType == "ADJUSTMENT",
                        onClick = { selectedType = "ADJUSTMENT" },
                        label = { Text("Ajuste (=)") }
                    )
                }

                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = {
                        Text(
                            when (selectedType) {
                                "ENTRY" -> "Cantidad a ingresar (${product.unit}) *"
                                "EXIT" -> "Cantidad a retirar (${product.unit}) *"
                                else -> "Nuevo valor total de stock (${product.unit}) *"
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo / Referencia (ej. Compra, Consumo)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = quantityStr.toDoubleOrNull()
                    if (q == null || q <= 0) {
                        errorMessage = "Ingresa una cantidad válida mayor a 0."
                    } else {
                        onConfirm(selectedType, q, reason.trim())
                    }
                }
            ) {
                Text("Registrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun InventoryScreenPreview() {
    ServiceNotebookTheme {
        InventoryScreenContent(
            appName = "Virna Nails",
            products = listOf(
                ProductEntity(id = 1, name = "Esmalte Semipermanente Rojo", unit = "unidad", unitCost = 15.0, stockQuantity = 12.0, minStock = 5.0),
                ProductEntity(id = 2, name = "Gel UV Constructor", unit = "ml", unitCost = 25.0, stockQuantity = 3.0, minStock = 10.0)
            ),
            lowStockProducts = listOf(
                ProductEntity(id = 2, name = "Gel UV Constructor", unit = "ml", unitCost = 25.0, stockQuantity = 3.0, minStock = 10.0)
            ),
            movements = listOf(
                InventoryMovementEntity(id = 1, productId = 1, movementType = "ENTRY", quantity = 10.0, reason = "Compra inicial")
            ),
            allProductsMap = mapOf(
                1L to ProductEntity(id = 1, name = "Esmalte Semipermanente Rojo", unit = "unidad", unitCost = 15.0, stockQuantity = 12.0, minStock = 5.0)
            ),
            searchQuery = "",
            selectedTab = InventoryTab.ALL_PRODUCTS,
            onSearchQueryChange = {},
            onTabSelected = {},
            onNavigateBack = {},
            onAddProductClick = {},
            onEditProductClick = {},
            onDeleteProductClick = {},
            onRegisterMovementClick = {}
        )
    }
}
