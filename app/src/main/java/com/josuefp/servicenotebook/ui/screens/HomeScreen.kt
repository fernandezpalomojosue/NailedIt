package com.josuefp.servicenotebook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.josuefp.servicenotebook.data.local.entity.ClientEntity
import com.josuefp.servicenotebook.data.local.entity.ProductEntity
import com.josuefp.servicenotebook.data.local.model.AppointmentWithDetails
import com.josuefp.servicenotebook.ui.theme.ServiceNotebookTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    appName: String = "NailedIt",
    themePalette: String = "ROSE",
    allAppointments: List<AppointmentWithDetails> = emptyList(),
    allProducts: List<ProductEntity> = emptyList(),
    allClients: List<ClientEntity> = emptyList(),
    onUpdateAppointmentStatus: (appointmentId: Long, newStatus: String) -> Unit = { _, _ -> },
    onUpdateAppName: (String) -> Unit = {},
    onUpdateThemePalette: (String) -> Unit = {},
    onNavigateToInventory: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToAgenda: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToBackup: () -> Unit,
    onNavigateToAppointmentForm: (appointmentId: Long, dateMillis: Long) -> Unit = { _, _ -> }
) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val topHeaderPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 64.dp

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale("es", "ES")) }

    val (todayStartMillis, todayEndMillis) = remember {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val end = cal.timeInMillis
        start to end
    }

    val todayAppointments = remember(allAppointments, todayStartMillis) {
        allAppointments.filter {
            it.appointment.appointmentDate in todayStartMillis..todayEndMillis
        }.sortedBy { it.appointment.appointmentDate }
    }

    val (thisMonthStartMillis, thisMonthEndMillis) = remember {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val end = cal.timeInMillis
        start to end
    }

    val thisMonthApps = remember(allAppointments, thisMonthStartMillis) {
        allAppointments.filter {
            it.appointment.appointmentDate in thisMonthStartMillis..thisMonthEndMillis && it.appointment.status != "CANCELLED"
        }
    }

    val monthIncome = remember(thisMonthApps) {
        thisMonthApps.sumOf { it.appointment.agreedPrice }
    }

    val monthCompletedCount = remember(thisMonthApps) {
        thisMonthApps.count { it.appointment.status == "COMPLETED" }
    }

    val lowStockProducts = remember(allProducts) {
        allProducts.filter { it.stockQuantity <= it.minStock }
    }

    val nowMillis = remember { System.currentTimeMillis() }
    val dayMillis = 24 * 60 * 60 * 1000L

    val maintenanceSuggestions = remember(allAppointments, allClients, nowMillis) {
        allClients.mapNotNull { client ->
            val clientCompletedApps = allAppointments
                .filter { it.client?.id == client.id && it.appointment.status == "COMPLETED" }
                .sortedByDescending { it.appointment.appointmentDate }

            val lastApp = clientCompletedApps.firstOrNull()
            if (lastApp != null) {
                val daysDiff = ((nowMillis - lastApp.appointment.appointmentDate) / dayMillis).toInt()
                if (daysDiff in 15..28) {
                    val hasFutureApp = allAppointments.any {
                        it.client?.id == client.id && it.appointment.appointmentDate > nowMillis && it.appointment.status != "CANCELLED"
                    }
                    if (!hasFutureApp) {
                        client to daysDiff
                    } else null
                } else null
            } else null
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = topHeaderPadding)
                    .fillMaxWidth(0.55f),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = appName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Estudio & Citas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { Text("Inventario", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToInventory()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Catálogo", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToCatalog()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Agenda", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToAgenda()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Galería", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToGallery()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Reportes", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToReports()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                Spacer(modifier = Modifier.height(4.dp))

                NavigationDrawerItem(
                    label = { Text("Respaldos", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToBackup()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Ajustes", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showSettingsDialog = true
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(appName) },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú de navegación")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Ajustes de la aplicación")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dashboard Header Greeting
                item {
                    Text(
                        text = "Bienvenida, $appName",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Resumen del Centro de Control",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // CARD 1: Citas de Hoy
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Citas de Hoy (${todayAppointments.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }

                                TextButton(onClick = onNavigateToAgenda) {
                                    Text("Ver Agenda")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (todayAppointments.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No tienes citas agendadas para el día de hoy.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    todayAppointments.forEach { appDetails ->
                                        val app = appDetails.appointment
                                        val clientName = appDetails.client?.name ?: "Clienta desasociada"
                                        val serviceNames = appDetails.services.joinToString(", ") { it.name }.ifBlank { "Servicio personalizado" }
                                        val startTime = timeFormat.format(Date(app.appointmentDate))

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
                                                    Text("$startTime • $clientName", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                    Text("$serviceNames • $${String.format(Locale.getDefault(), "%.2f", app.agreedPrice)}", style = MaterialTheme.typography.bodySmall)
                                                }

                                                if (app.status != "COMPLETED") {
                                                    FilterChip(
                                                        selected = false,
                                                        onClick = { onUpdateAppointmentStatus(app.id, "COMPLETED") },
                                                        label = { Text("Completar") }
                                                    )
                                                } else {
                                                    Text("✓ COMPLETADA", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // CARD 2: Resumen Financiero del Mes
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Rendimiento de ${monthFormat.format(Date()).replaceFirstChar { it.uppercase() }}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Ingresos Totales", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = "$${String.format(Locale.getDefault(), "%.2f", monthIncome)}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Citas Realizadas", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = "$monthCompletedCount completadas",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // CARD 3: Alertas de Inventario
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (lowStockProducts.isNotEmpty()) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        if (lowStockProducts.isNotEmpty()) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (lowStockProducts.isNotEmpty()) MaterialTheme.colorScheme.error else Color(0xFF2E7D32)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (lowStockProducts.isNotEmpty()) "Alertas de Stock Bajo (${lowStockProducts.size})" else "Estado del Inventario",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                TextButton(onClick = onNavigateToInventory) {
                                    Text("Ver Inventario")
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            if (lowStockProducts.isEmpty()) {
                                Text(
                                    text = "Inventario al día. Todos tus productos e insumos tienen stock suficiente.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    lowStockProducts.take(3).forEach { prod ->
                                        Text(
                                            text = "• ${prod.name}: Quedan ${prod.stockQuantity} ${prod.unit} (Mínimo: ${prod.minStock})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // CARD 4: Recordatorio Mantenimiento 15-21 días
                if (maintenanceSuggestions.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sugerencias de Retocado (15-21 días)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    maintenanceSuggestions.take(3).forEach { (client, daysDiff) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(client.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                Text("Cumplió $daysDiff días desde su última cita", style = MaterialTheme.typography.bodySmall)
                                            }

                                            OutlinedButton(
                                                onClick = { onNavigateToAppointmentForm(0L, System.currentTimeMillis()) }
                                            ) {
                                                Text("Agendar")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        AppSettingsDialog(
            currentName = appName,
            currentTheme = themePalette,
            onDismiss = { showSettingsDialog = false },
            onConfirm = { newName, newTheme ->
                onUpdateAppName(newName)
                onUpdateThemePalette(newTheme)
                showSettingsDialog = false
            },
            onNavigateToBackup = {
                showSettingsDialog = false
                onNavigateToBackup()
            }
        )
    }
}

@Composable
fun AppSettingsDialog(
    currentName: String,
    currentTheme: String,
    onDismiss: () -> Unit,
    onConfirm: (newName: String, newTheme: String) -> Unit,
    onNavigateToBackup: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var selectedTheme by remember { mutableStateOf(currentTheme) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val themeOptions = listOf(
        "ROSE" to ("Rosa / Magenta" to Color(0xFFD81B60)),
        "PURPLE" to ("Púrpura / Lavanda" to Color(0xFF8E24AA)),
        "TEAL" to ("Turquesa / Menta" to Color(0xFF00897B)),
        "CORAL" to ("Coral Cálido" to Color(0xFFE64A19)),
        "BLUE" to ("Azul Océano" to Color(0xFF1E88E5)),
        "ELEGANT_DARK" to ("Elegante Oscuro" to Color(0xFF37474F))
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustes de la Aplicación") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Text("Nombre de la aplicación", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(2.dp))
                Text("Tema de color principal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    themeOptions.forEach { (key, info) ->
                        val (label, color) = info
                        val isSelected = selectedTheme.equals(key, ignoreCase = true)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTheme = key },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) color.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(color, shape = CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = color)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(2.dp))

                Text("Sistema y Respaldo", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                OutlinedButton(
                    onClick = onNavigateToBackup,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copias de Seguridad (Backup)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "El nombre no puede estar vacío."
                    } else {
                        onConfirm(name.trim(), selectedTheme)
                    }
                }
            ) {
                Text("Guardar Cambios")
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
fun HomeScreenPreview() {
    ServiceNotebookTheme {
        HomeScreen(
            appName = "NailedIt",
            themePalette = "ROSE",
            onUpdateAppName = {},
            onUpdateThemePalette = {},
            onNavigateToInventory = {},
            onNavigateToCatalog = {},
            onNavigateToAgenda = {},
            onNavigateToGallery = {},
            onNavigateToReports = {},
            onNavigateToBackup = {}
        )
    }
}
