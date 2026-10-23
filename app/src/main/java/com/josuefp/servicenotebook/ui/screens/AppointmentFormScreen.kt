package com.josuefp.servicenotebook.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.local.entity.AppointmentEntity
import com.josuefp.servicenotebook.data.local.entity.ClientEntity
import com.josuefp.servicenotebook.data.local.entity.ServiceEntity
import com.josuefp.servicenotebook.data.local.model.AppointmentWithDetails
import com.josuefp.servicenotebook.data.repository.AgendaRepository
import com.josuefp.servicenotebook.notification.LocalNotificationScheduler
import com.josuefp.servicenotebook.ui.viewmodel.AgendaViewModel
import com.josuefp.servicenotebook.ui.viewmodel.AgendaViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentFormScreen(
    appName: String = "Virna Nails",
    appointmentId: Long = 0L,
    initialDateMillis: Long = System.currentTimeMillis(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { AgendaRepository(database) }
    val notificationScheduler = remember { LocalNotificationScheduler(context) }
    val viewModel: AgendaViewModel = viewModel(factory = AgendaViewModelFactory(repository, notificationScheduler))

    val allClients by viewModel.allClients.collectAsState()
    val allServices by viewModel.allServices.collectAsState()
    val allAppointments by viewModel.allAppointments.collectAsState()

    val editingAppointment = remember(appointmentId, allAppointments) {
        if (appointmentId > 0L) allAppointments.find { it.appointment.id == appointmentId } else null
    }

    var selectedClient by remember(editingAppointment, allClients) {
        mutableStateOf(editingAppointment?.client ?: allClients.firstOrNull())
    }

    val selectedServiceIds = remember(editingAppointment, allServices) {
        val initialIds = editingAppointment?.services?.map { it.id } ?: emptyList()
        mutableStateListOf<Long>().apply {
            if (initialIds.isNotEmpty()) {
                addAll(initialIds)
            } else if (allServices.isNotEmpty()) {
                add(allServices[0].id)
            }
        }
    }

    var showCatalogPicker by remember { mutableStateOf(false) }

    val selectedServicesList = remember(selectedServiceIds.toList(), allServices) {
        allServices.filter { selectedServiceIds.contains(it.id) }
    }

    val calculatedDuration = remember(selectedServicesList) {
        selectedServicesList.sumOf { it.estimatedDurationMinutes }.coerceAtLeast(30)
    }

    val calculatedPrice = remember(selectedServicesList) {
        selectedServicesList.sumOf { it.basePrice }
    }

    var agreedPriceStr by remember(editingAppointment, calculatedPrice) {
        mutableStateOf(editingAppointment?.appointment?.agreedPrice?.toString() ?: calculatedPrice.toString())
    }

    var durationStr by remember(editingAppointment, calculatedDuration) {
        mutableStateOf(editingAppointment?.appointment?.durationMinutes?.toString() ?: calculatedDuration.toString())
    }

    LaunchedEffect(calculatedPrice, calculatedDuration) {
        if (editingAppointment == null) {
            agreedPriceStr = calculatedPrice.toString()
            durationStr = calculatedDuration.toString()
        }
    }

    var notes by remember(editingAppointment) { mutableStateOf(editingAppointment?.appointment?.notes ?: "") }
    var scheduleReminder by remember(editingAppointment) { mutableStateOf(editingAppointment?.appointment?.reminderScheduled ?: true) }

    val cal = remember(editingAppointment, initialDateMillis) {
        Calendar.getInstance().apply {
            timeInMillis = editingAppointment?.appointment?.appointmentDate ?: initialDateMillis
            if (editingAppointment == null) {
                set(Calendar.HOUR_OF_DAY, 10)
                set(Calendar.MINUTE, 0)
            }
        }
    }

    var hour by remember { mutableIntStateOf(cal.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableIntStateOf(cal.get(Calendar.MINUTE)) }

    var isClientDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showOverlapDialog by remember { mutableStateOf<AppointmentWithDetails?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (appointmentId == 0L) "$appName - Nueva Cita" else "$appName - Editar Cita") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
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
            item {
                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text("Clienta *", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = isClientDropdownExpanded,
                    onExpandedChange = { isClientDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedClient?.name ?: "Seleccionar clienta...",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isClientDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isClientDropdownExpanded,
                        onDismissRequest = { isClientDropdownExpanded = false }
                    ) {
                        allClients.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = {
                                    selectedClient = c
                                    isClientDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Servicios Agregados (${selectedServicesList.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Button(onClick = { showCatalogPicker = true }) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Catálogo")
                    }
                }
            }

            if (selectedServicesList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aún no has agregado ningún servicio. Toca 'Catálogo' para seleccionar.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(selectedServicesList, key = { it.id }) { service ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
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
                                Text("${service.estimatedDurationMinutes} min • $${service.basePrice} • Sección: ${service.category}", style = MaterialTheme.typography.bodySmall)
                            }

                            IconButton(onClick = { selectedServiceIds.remove(service.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Quitar servicio", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Horario de inicio:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = String.format(Locale.getDefault(), "%02d", hour),
                        onValueChange = { it.toIntOrNull()?.let { h -> if (h in 0..23) hour = h } },
                        label = { Text("Hora (0-23)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Text(":", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = String.format(Locale.getDefault(), "%02d", minute),
                        onValueChange = { it.toIntOrNull()?.let { m -> if (m in 0..59) minute = m } },
                        label = { Text("Minuto (0-59)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = durationStr,
                        onValueChange = { durationStr = it },
                        label = { Text("Duración Total (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = agreedPriceStr,
                        onValueChange = { agreedPriceStr = it },
                        label = { Text("Precio Acordado ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas de la cita") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { scheduleReminder = !scheduleReminder }
                ) {
                    Checkbox(
                        checked = scheduleReminder,
                        onCheckedChange = { scheduleReminder = it }
                    )
                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Recordatorio local (30 min antes)", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val client = selectedClient
                        val price = agreedPriceStr.toDoubleOrNull()
                        val duration = durationStr.toIntOrNull()

                        if (client == null) {
                            errorMessage = "Debes seleccionar una clienta."
                        } else if (selectedServiceIds.isEmpty()) {
                            errorMessage = "Selecciona al menos un servicio del catálogo."
                        } else if (price == null || price < 0) {
                            errorMessage = "Ingresa un precio válido."
                        } else if (duration == null || duration <= 0) {
                            errorMessage = "Ingresa una duración válida."
                        } else {
                            val appointmentCal = Calendar.getInstance().apply {
                                timeInMillis = initialDateMillis
                                set(Calendar.HOUR_OF_DAY, hour)
                                set(Calendar.MINUTE, minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }

                            val appointmentEntity = (editingAppointment?.appointment ?: AppointmentEntity(
                                clientId = client.id,
                                serviceId = selectedServiceIds.firstOrNull(),
                                appointmentDate = appointmentCal.timeInMillis
                            )).copy(
                                clientId = client.id,
                                serviceId = selectedServiceIds.firstOrNull(),
                                appointmentDate = appointmentCal.timeInMillis,
                                durationMinutes = duration,
                                agreedPrice = price,
                                notes = notes.trim(),
                                reminderScheduled = scheduleReminder
                            )

                            viewModel.saveAppointmentWithServices(
                                appointment = appointmentEntity,
                                serviceIds = selectedServiceIds.toList(),
                                scheduleReminder = scheduleReminder,
                                onOverlapError = { overlap ->
                                    showOverlapDialog = overlap
                                },
                                onSuccess = {
                                    onNavigateBack()
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(if (appointmentId == 0L) "Guardar y Agendar Cita" else "Guardar Cambios", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    if (showCatalogPicker) {
        CatalogSelectionDialog(
            allServices = allServices,
            currentlySelectedIds = selectedServiceIds.toList(),
            onDismiss = { showCatalogPicker = false },
            onConfirmSelection = { newIds ->
                selectedServiceIds.clear()
                selectedServiceIds.addAll(newIds)
                showCatalogPicker = false
            }
        )
    }

    if (showOverlapDialog != null) {
        AlertDialog(
            onDismissRequest = { showOverlapDialog = null },
            title = { Text("Solapamiento de Horario") },
            text = {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val startTime = timeFormat.format(Date(showOverlapDialog!!.appointment.appointmentDate))
                val clientName = showOverlapDialog!!.client?.name ?: "Clienta"
                Text("Existe un conflicto de horario con la cita de $clientName programada a las $startTime (${showOverlapDialog!!.appointment.durationMinutes} min). Por favor selecciona otro horario.")
            },
            confirmButton = {
                Button(onClick = { showOverlapDialog = null }) {
                    Text("Entendido")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CatalogSelectionDialog(
    allServices: List<ServiceEntity>,
    currentlySelectedIds: List<Long>,
    onDismiss: () -> Unit,
    onConfirmSelection: (List<Long>) -> Unit
) {
    val tempSelectedIds = remember { mutableStateListOf<Long>().apply { addAll(currentlySelectedIds) } }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    val categories = listOf("Todos", "Manos", "Pies", "Decoraciones", "Otros")

    val filteredServices = remember(allServices, searchQuery, selectedCategory) {
        allServices.filter { service ->
            val matchCategory = selectedCategory == "Todos" || service.category.equals(selectedCategory, ignoreCase = true)
            val matchQuery = searchQuery.isBlank() || service.name.contains(searchQuery, ignoreCase = true)
            matchCategory && matchQuery
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar del Catálogo (${tempSelectedIds.size})") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar servicio...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory.equals(cat, ignoreCase = true),
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Text(
                    text = "Toca o mantén presionado un servicio para selección múltiple:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredServices, key = { it.id }) { service ->
                        val isSelected = tempSelectedIds.contains(service.id)

                        val toggleSelection = {
                            if (isSelected) tempSelectedIds.remove(service.id)
                            else tempSelectedIds.add(service.id)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { toggleSelection() },
                                    onLongClick = { toggleSelection() }
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
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
                                    Text("${service.estimatedDurationMinutes} min • $${service.basePrice} • [${service.category}]", style = MaterialTheme.typography.bodySmall)
                                }

                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmSelection(tempSelectedIds.toList()) }
            ) {
                Text("Aceptar (${tempSelectedIds.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
