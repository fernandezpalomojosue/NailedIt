package com.josuefp.servicenotebook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.local.entity.AppointmentEntity
import com.josuefp.servicenotebook.data.local.entity.ClientEntity
import com.josuefp.servicenotebook.data.local.entity.ServiceEntity
import com.josuefp.servicenotebook.data.local.model.AppointmentWithDetails
import com.josuefp.servicenotebook.data.repository.AgendaRepository
import com.josuefp.servicenotebook.notification.LocalNotificationScheduler
import com.josuefp.servicenotebook.ui.theme.ServiceNotebookTheme
import com.josuefp.servicenotebook.ui.viewmodel.AgendaTab
import com.josuefp.servicenotebook.ui.viewmodel.AgendaViewModel
import com.josuefp.servicenotebook.ui.viewmodel.AgendaViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AgendaScreen(
    appName: String = "Virna Nails",
    onNavigateToAppointmentForm: (Long, Long) -> Unit = { _, _ -> },
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { AgendaRepository(database) }
    val notificationScheduler = remember { LocalNotificationScheduler(context) }
    val viewModel: AgendaViewModel = viewModel(factory = AgendaViewModelFactory(repository, notificationScheduler))

    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedDateMillis by viewModel.selectedDateMillis.collectAsState()
    val (currentYear, currentMonth) = viewModel.currentYearMonth.collectAsState().value

    val allAppointments by viewModel.allAppointments.collectAsState()
    val appointmentsForSelectedDate by viewModel.appointmentsForSelectedDate.collectAsState()
    val filteredClients by viewModel.filteredClients.collectAsState()
    val clientSearchQuery by viewModel.clientSearchQuery.collectAsState()

    var showAddClientDialog by remember { mutableStateOf(false) }
    var editingClient by remember { mutableStateOf<ClientEntity?>(null) }
    var clientForHistory by remember { mutableStateOf<ClientEntity?>(null) }
    var completedAppForSuggestion by remember { mutableStateOf<AppointmentWithDetails?>(null) }

    var clientToDelete by remember { mutableStateOf<ClientEntity?>(null) }
    var appointmentToDelete by remember { mutableStateOf<AppointmentEntity?>(null) }

    AgendaScreenContent(
        appName = appName,
        selectedTab = selectedTab,
        selectedDateMillis = selectedDateMillis,
        currentYear = currentYear,
        currentMonth = currentMonth,
        allAppointments = allAppointments,
        appointmentsForSelectedDate = appointmentsForSelectedDate,
        clients = filteredClients,
        clientSearchQuery = clientSearchQuery,
        onTabSelected = viewModel::setSelectedTab,
        onDateSelected = viewModel::setSelectedDate,
        onYearMonthChanged = viewModel::setYearMonth,
        onClientSearchQueryChange = viewModel::setClientSearchQuery,
        onNavigateBack = onNavigateBack,
        onAddClientClick = {
            editingClient = null
            showAddClientDialog = true
        },
        onEditClientClick = { client ->
            editingClient = client
            showAddClientDialog = true
        },
        onDeleteClientClick = { client ->
            clientToDelete = client
        },
        onClientClick = { client ->
            clientForHistory = client
        },
        onAddAppointmentForClient = { _ ->
            onNavigateToAppointmentForm(0L, selectedDateMillis)
        },
        onAddAppointmentClick = {
            onNavigateToAppointmentForm(0L, selectedDateMillis)
        },
        onEditAppointmentClick = { appDetails ->
            onNavigateToAppointmentForm(appDetails.appointment.id, appDetails.appointment.appointmentDate)
        },
        onDeleteAppointmentClick = { appEntity ->
            appointmentToDelete = appEntity
        },
        onUpdateAppointmentStatus = { appId, newStatus ->
            if (newStatus == "COMPLETED") {
                val appDetails = allAppointments.find { it.appointment.id == appId }
                viewModel.updateAppointmentStatus(appId, newStatus)
                if (appDetails != null) {
                    completedAppForSuggestion = appDetails
                }
            } else {
                viewModel.updateAppointmentStatus(appId, newStatus)
            }
        }
    )

    if (showAddClientDialog) {
        AddEditClientDialog(
            client = editingClient,
            onDismiss = { showAddClientDialog = false },
            onConfirm = { name, phone, email, notes ->
                viewModel.saveClient(name, phone, email, notes, editingClient)
                showAddClientDialog = false
            }
        )
    }

    if (clientForHistory != null) {
        ClientHistoryDialog(
            client = clientForHistory!!,
            appointments = allAppointments,
            onDismiss = { clientForHistory = null }
        )
    }

    if (completedAppForSuggestion != null) {
        NextAppointmentSuggestionDialog(
            completedAppointment = completedAppForSuggestion!!,
            onDismiss = { completedAppForSuggestion = null },
            onScheduleNextAppointment = { _, suggestedDateMillis ->
                onNavigateToAppointmentForm(0L, suggestedDateMillis)
            }
        )
    }

    if (clientToDelete != null) {
        AlertDialog(
            onDismissRequest = { clientToDelete = null },
            title = { Text("Eliminar clienta") },
            text = { Text("¿Deseas eliminar a '${clientToDelete!!.name}' y sus citas registradas?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteClient(clientToDelete!!)
                    clientToDelete = null
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { clientToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (appointmentToDelete != null) {
        AlertDialog(
            onDismissRequest = { appointmentToDelete = null },
            title = { Text("Eliminar cita") },
            text = { Text("¿Deseas eliminar esta cita de la agenda?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteAppointment(appointmentToDelete!!)
                    appointmentToDelete = null
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { appointmentToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreenContent(
    appName: String,
    selectedTab: AgendaTab,
    selectedDateMillis: Long,
    currentYear: Int,
    currentMonth: Int,
    allAppointments: List<AppointmentWithDetails>,
    appointmentsForSelectedDate: List<AppointmentWithDetails>,
    clients: List<ClientEntity>,
    clientSearchQuery: String,
    onTabSelected: (AgendaTab) -> Unit,
    onDateSelected: (Long) -> Unit,
    onYearMonthChanged: (year: Int, month: Int) -> Unit,
    onClientSearchQueryChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onAddClientClick: () -> Unit,
    onEditClientClick: (ClientEntity) -> Unit,
    onDeleteClientClick: (ClientEntity) -> Unit,
    onClientClick: (ClientEntity) -> Unit,
    onAddAppointmentForClient: (ClientEntity) -> Unit,
    onAddAppointmentClick: () -> Unit,
    onEditAppointmentClick: (AppointmentWithDetails) -> Unit,
    onDeleteAppointmentClick: (AppointmentEntity) -> Unit,
    onUpdateAppointmentStatus: (appointmentId: Long, newStatus: String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$appName - Agenda") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == AgendaTab.CLIENTS) onAddClientClick()
                    else onAddAppointmentClick()
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
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
                    selected = selectedTab == AgendaTab.CALENDAR,
                    onClick = { onTabSelected(AgendaTab.CALENDAR) },
                    text = { Text("Calendario") }
                )
                Tab(
                    selected = selectedTab == AgendaTab.DAILY_LIST,
                    onClick = { onTabSelected(AgendaTab.DAILY_LIST) },
                    text = {
                        if (appointmentsForSelectedDate.isNotEmpty()) {
                            BadgedBox(
                                badge = { Badge { Text("${appointmentsForSelectedDate.size}") } }
                            ) {
                                Text("Citas del Día")
                            }
                        } else {
                            Text("Citas del Día")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == AgendaTab.CLIENTS,
                    onClick = { onTabSelected(AgendaTab.CLIENTS) },
                    text = { Text("Clientas (${clients.size})") }
                )
            }

            when (selectedTab) {
                AgendaTab.CALENDAR -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        CustomMonthlyCalendar(
                            year = currentYear,
                            month = currentMonth,
                            selectedDateMillis = selectedDateMillis,
                            allAppointments = allAppointments,
                            onDateSelected = { millis ->
                                onDateSelected(millis)
                                onTabSelected(AgendaTab.DAILY_LIST)
                            },
                            onPreviousMonth = {
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, currentYear)
                                    set(Calendar.MONTH, currentMonth)
                                    add(Calendar.MONTH, -1)
                                }
                                onYearMonthChanged(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
                            },
                            onNextMonth = {
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, currentYear)
                                    set(Calendar.MONTH, currentMonth)
                                    add(Calendar.MONTH, 1)
                                }
                                onYearMonthChanged(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
                            }
                        )
                    }
                }
                AgendaTab.DAILY_LIST -> {
                    DailyAppointmentsSection(
                        selectedDateMillis = selectedDateMillis,
                        appointments = appointmentsForSelectedDate,
                        onEditClick = onEditAppointmentClick,
                        onDeleteClick = onDeleteAppointmentClick,
                        onUpdateStatus = onUpdateAppointmentStatus
                    )
                }
                AgendaTab.CLIENTS -> {
                    ClientsListSection(
                        clients = clients,
                        searchQuery = clientSearchQuery,
                        onSearchQueryChange = onClientSearchQueryChange,
                        onEditClick = onEditClientClick,
                        onDeleteClick = onDeleteClientClick,
                        onAddAppointmentClick = onAddAppointmentForClient,
                        onClientClick = onClientClick
                    )
                }
            }
        }
    }
}

@Composable
fun NextAppointmentSuggestionDialog(
    completedAppointment: AppointmentWithDetails,
    onDismiss: () -> Unit,
    onScheduleNextAppointment: (clientId: Long, suggestedDateMillis: Long) -> Unit
) {
    val clientName = completedAppointment.client?.name ?: "la clienta"
    val baseDate = completedAppointment.appointment.appointmentDate

    val cal15 = remember(baseDate) {
        Calendar.getInstance().apply {
            timeInMillis = baseDate
            add(Calendar.DAY_OF_YEAR, 15)
        }
    }

    val cal21 = remember(baseDate) {
        Calendar.getInstance().apply {
            timeInMillis = baseDate
            add(Calendar.DAY_OF_YEAR, 21)
        }
    }

    val dateFormatDayMonth = remember { SimpleDateFormat("d 'de' MMMM", Locale("es", "ES")) }
    val dateFormatDayMonthYear = remember { SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "ES")) }

    val str15 = dateFormatDayMonth.format(cal15.time)
    val str21 = dateFormatDayMonthYear.format(cal21.time)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🎉 ¡Servicio Completado!") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Se ha registrado el servicio para $clientName y se han descontado los insumos del inventario.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "💡 Sugerencia de Próxima Cita:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Se recomienda agendar el retocado o mantenimiento en 15 a 21 días:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📅 Entre el $str15 y el $str21.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onScheduleNextAppointment(completedAppointment.client?.id ?: 0L, cal15.timeInMillis)
                    onDismiss()
                }
            ) {
                Text("Agendar Próxima Cita")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun ClientHistoryDialog(
    client: ClientEntity,
    appointments: List<AppointmentWithDetails>,
    onDismiss: () -> Unit
) {
    val clientAppointments = appointments.filter { it.client?.id == client.id }
    val totalSpent = clientAppointments.filter { it.appointment.status != "CANCELLED" }.sumOf { it.appointment.agreedPrice }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Historial: ${client.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text("Teléfono: ${client.phone.ifBlank { "No registrado" }}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Total invertido: $${String.format(Locale.getDefault(), "%.2f", totalSpent)} (${clientAppointments.size} citas)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (client.notes.isNotBlank()) {
                    Text("Notas: ${client.notes}", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Historial de Citas:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                if (clientAppointments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay citas registradas para esta clienta.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.height(240.dp)
                    ) {
                        items(clientAppointments, key = { it.appointment.id }) { appDetails ->
                            val app = appDetails.appointment
                            val servicesText = appDetails.services.joinToString(", ") { it.name }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(dateFormat.format(Date(app.appointmentDate)), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        Text(app.status, style = MaterialTheme.typography.labelSmall)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(servicesText.ifBlank { "Servicio personalizado" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text("$${String.format(Locale.getDefault(), "%.2f", app.agreedPrice)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun CustomMonthlyCalendar(
    year: Int,
    month: Int,
    selectedDateMillis: Long,
    allAppointments: List<AppointmentWithDetails>,
    onDateSelected: (Long) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale("es", "ES")) }
    val calendarHeaderDate = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }.time
    }

    val daysInMonth = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstDayOfWeek = remember(year, month) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }.get(Calendar.DAY_OF_WEEK)
    }

    val todayStartMillis = remember { AgendaViewModel.getStartOfDayMillis(System.currentTimeMillis()) }
    val selectedStartMillis = remember(selectedDateMillis) { AgendaViewModel.getStartOfDayMillis(selectedDateMillis) }

    val appointmentDaysMap = remember(allAppointments, year, month) {
        allAppointments
            .filter { it.appointment.status != "CANCELLED" }
            .groupBy { AgendaViewModel.getStartOfDayMillis(it.appointment.appointmentDate) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mes anterior")
            }
            Text(
                text = monthFormat.format(calendarHeaderDate).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Mes siguiente")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val daysOfWeek = listOf("DOM", "LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB")
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { dayName ->
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val totalGridCells = (firstDayOfWeek - 1) + daysInMonth
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(totalGridCells) { index ->
                if (index < firstDayOfWeek - 1) {
                    Box(modifier = Modifier.aspectRatio(1f))
                } else {
                    val dayNumber = index - (firstDayOfWeek - 1) + 1
                    val dayCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, dayNumber)
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val dayStartMillis = dayCal.timeInMillis
                    val isSelected = dayStartMillis == selectedStartMillis
                    val isToday = dayStartMillis == todayStartMillis
                    val appCount = appointmentDaysMap[dayStartMillis]?.size ?: 0

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else if (isToday) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent,
                                shape = CircleShape
                            )
                            .border(
                                width = if (isToday && !isSelected) 1.dp else 0.dp,
                                color = if (isToday && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onDateSelected(dayStartMillis) }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$dayNumber",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                            )
                            if (appCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .background(
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyAppointmentsSection(
    selectedDateMillis: Long,
    appointments: List<AppointmentWithDetails>,
    onEditClick: (AppointmentWithDetails) -> Unit,
    onDeleteClick: (AppointmentEntity) -> Unit,
    onUpdateStatus: (appointmentId: Long, newStatus: String) -> Unit
) {
    val fullDateFormat = remember { SimpleDateFormat("EEEE d 'de' MMMM, yyyy", Locale("es", "ES")) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = fullDateFormat.format(Date(selectedDateMillis)).replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (appointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay citas programadas para este día. Presiona + para agendar una.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(appointments, key = { it.appointment.id }) { appDetails ->
                    val app = appDetails.appointment
                    val clientName = appDetails.client?.name ?: "Clienta desasociada"
                    val serviceNames = appDetails.services.joinToString(", ") { it.name }
                    val servicesText = if (serviceNames.isNotBlank()) serviceNames else "Servicio personalizado"
                    val startTime = timeFormat.format(Date(app.appointmentDate))
                    val endTime = timeFormat.format(Date(app.appointmentDate + app.durationMinutes * 60 * 1000L))

                    val (statusLabel, statusColor) = when (app.status) {
                        "CONFIRMED" -> "CONFIRMADA" to Color(0xFF1976D2)
                        "COMPLETED" -> "COMPLETADA" to Color(0xFF2E7D32)
                        "CANCELLED" -> "CANCELADA" to MaterialTheme.colorScheme.error
                        else -> "PENDIENTE" to Color(0xFFF57C00)
                    }

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
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$startTime - $endTime",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = statusLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier
                                        .background(statusColor, shape = MaterialTheme.shapes.extraSmall)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = clientName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "$servicesText • $${String.format(Locale.getDefault(), "%.2f", app.agreedPrice)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (app.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Notas: ${app.notes}", style = MaterialTheme.typography.bodySmall)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    FilterChip(
                                        selected = app.status == "COMPLETED",
                                        onClick = { onUpdateStatus(app.id, "COMPLETED") },
                                        label = { Text("Completar") }
                                    )
                                    FilterChip(
                                        selected = app.status == "CANCELLED",
                                        onClick = { onUpdateStatus(app.id, "CANCELLED") },
                                        label = { Text("Cancelar") }
                                    )
                                }

                                Row {
                                    IconButton(onClick = { onEditClick(appDetails) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                                    }
                                    IconButton(onClick = { onDeleteClick(app) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
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

@Composable
fun ClientsListSection(
    clients: List<ClientEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onEditClick: (ClientEntity) -> Unit,
    onDeleteClick: (ClientEntity) -> Unit,
    onAddAppointmentClick: (ClientEntity) -> Unit,
    onClientClick: (ClientEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Buscar clienta por nombre o teléfono...") },
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

        if (clients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontraron clientas. Toca + para agregar una.",
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
                items(clients, key = { it.id }) { client ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClientClick(client) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(client.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                                Row {
                                    IconButton(onClick = { onEditClick(client) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                                    }
                                    IconButton(onClick = { onDeleteClick(client) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            if (client.phone.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(client.phone, style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            if (client.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Notas: ${client.notes}", style = MaterialTheme.typography.bodySmall)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Toca para ver historial",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                OutlinedButton(
                                    onClick = { onAddAppointmentClick(client) }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
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

@Composable
fun AddEditClientDialog(
    client: ClientEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, email: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(client?.name ?: "") }
    var phone by remember { mutableStateOf(client?.phone ?: "") }
    var email by remember { mutableStateOf(client?.email ?: "") }
    var notes by remember { mutableStateOf(client?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (client == null) "Nueva Clienta" else "Editar Clienta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre completo *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono de contacto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (preferencias, alergias, etc.)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "El nombre no puede estar vacío."
                    } else {
                        onConfirm(name.trim(), phone.trim(), email.trim(), notes.trim())
                    }
                }
            ) {
                Text(if (client == null) "Agregar" else "Guardar")
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
fun AgendaScreenPreview() {
    ServiceNotebookTheme {
        AgendaScreenContent(
            appName = "Virna Nails",
            selectedTab = AgendaTab.CALENDAR,
            selectedDateMillis = System.currentTimeMillis(),
            currentYear = 2025,
            currentMonth = 4,
            allAppointments = listOf(
                AppointmentWithDetails(
                    appointment = AppointmentEntity(id = 1, clientId = 1, serviceId = 1, appointmentDate = System.currentTimeMillis(), agreedPrice = 35.0, status = "CONFIRMED"),
                    client = ClientEntity(id = 1, name = "María López", phone = "555-1234"),
                    services = listOf(ServiceEntity(id = 1, name = "Manicura Rusa"))
                )
            ),
            appointmentsForSelectedDate = listOf(
                AppointmentWithDetails(
                    appointment = AppointmentEntity(id = 1, clientId = 1, serviceId = 1, appointmentDate = System.currentTimeMillis(), agreedPrice = 35.0, status = "CONFIRMED"),
                    client = ClientEntity(id = 1, name = "María López", phone = "555-1234"),
                    services = listOf(ServiceEntity(id = 1, name = "Manicura Rusa"))
                )
            ),
            clients = listOf(
                ClientEntity(id = 1, name = "María López", phone = "555-1234")
            ),
            clientSearchQuery = "",
            onTabSelected = {},
            onDateSelected = {},
            onYearMonthChanged = { _, _ -> },
            onClientSearchQueryChange = {},
            onNavigateBack = {},
            onAddClientClick = {},
            onEditClientClick = {},
            onDeleteClientClick = {},
            onClientClick = {},
            onAddAppointmentForClient = {},
            onAddAppointmentClick = {},
            onEditAppointmentClick = {},
            onDeleteAppointmentClick = {},
            onUpdateAppointmentStatus = { _, _ -> }
        )
    }
}
