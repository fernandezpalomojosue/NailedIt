package com.josuefp.servicenotebook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.josuefp.servicenotebook.data.local.entity.AppointmentEntity
import com.josuefp.servicenotebook.data.local.entity.ClientEntity
import com.josuefp.servicenotebook.data.local.entity.ServiceEntity
import com.josuefp.servicenotebook.data.local.model.AppointmentWithDetails
import com.josuefp.servicenotebook.data.repository.AgendaRepository
import com.josuefp.servicenotebook.notification.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AgendaTab {
    CALENDAR,
    DAILY_LIST,
    CLIENTS
}

class AgendaViewModel(
    private val repository: AgendaRepository,
    private val notificationScheduler: NotificationScheduler? = null
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(AgendaTab.CALENDAR)
    val selectedTab: StateFlow<AgendaTab> = _selectedTab

    private val _selectedDateMillis = MutableStateFlow(getStartOfDayMillis(System.currentTimeMillis()))
    val selectedDateMillis: StateFlow<Long> = _selectedDateMillis

    private val _currentYearMonth = MutableStateFlow(
        Calendar.getInstance().run {
            get(Calendar.YEAR) to get(Calendar.MONTH)
        }
    )
    val currentYearMonth: StateFlow<Pair<Int, Int>> = _currentYearMonth

    private val _clientSearchQuery = MutableStateFlow("")
    val clientSearchQuery: StateFlow<String> = _clientSearchQuery

    val allAppointments: StateFlow<List<AppointmentWithDetails>> = repository.getAllAppointmentsWithDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClients: StateFlow<List<ClientEntity>> = repository.getAllClients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredClients: StateFlow<List<ClientEntity>> = combine(allClients, _clientSearchQuery) { clients, query ->
        if (query.isBlank()) clients
        else clients.filter { it.name.contains(query, ignoreCase = true) || it.phone.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allServices: StateFlow<List<ServiceEntity>> = repository.getAllActiveServices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointmentsForSelectedDate: StateFlow<List<AppointmentWithDetails>> = combine(
        allAppointments,
        _selectedDateMillis
    ) { appointments, dateMillis ->
        val dayStart = getStartOfDayMillis(dateMillis)
        val dayEnd = dayStart + (24 * 60 * 60 * 1000L)
        appointments.filter {
            it.appointment.appointmentDate >= dayStart && it.appointment.appointmentDate < dayEnd
        }.sortedBy { it.appointment.appointmentDate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedTab(tab: AgendaTab) {
        _selectedTab.value = tab
    }

    fun setSelectedDate(millis: Long) {
        _selectedDateMillis.value = getStartOfDayMillis(millis)
    }

    fun setYearMonth(year: Int, month: Int) {
        _currentYearMonth.value = year to month
    }

    fun setClientSearchQuery(query: String) {
        _clientSearchQuery.value = query
    }

    fun saveClient(name: String, phone: String, email: String, notes: String, existingClient: ClientEntity? = null) {
        viewModelScope.launch {
            repository.saveClient(
                (existingClient ?: ClientEntity(name = name)).copy(
                    name = name.trim(),
                    phone = phone.trim(),
                    email = email.trim(),
                    notes = notes.trim()
                )
            )
        }
    }

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch {
            repository.deleteClient(client)
        }
    }

    fun saveAppointmentWithServices(
        appointment: AppointmentEntity,
        serviceIds: List<Long>,
        scheduleReminder: Boolean,
        onOverlapError: (AppointmentWithDetails) -> Unit,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val overlap = repository.checkOverlap(
                startMillis = appointment.appointmentDate,
                durationMinutes = appointment.durationMinutes,
                excludeAppointmentId = appointment.id
            )

            if (overlap != null) {
                onOverlapError(overlap)
                return@launch
            }

            val savedId = repository.saveAppointmentWithServices(appointment, serviceIds)

            if (scheduleReminder && notificationScheduler != null) {
                val reminderTime = appointment.appointmentDate - (30 * 60 * 1000L)
                if (reminderTime > System.currentTimeMillis()) {
                    notificationScheduler.scheduleAppointmentReminder(
                        appointmentId = savedId,
                        timeInMillis = reminderTime,
                        title = "Virna Nails - Recordatorio de Cita",
                        message = "Tienes una cita en 30 minutos."
                    )
                }
            } else {
                notificationScheduler?.cancelAppointmentReminder(savedId)
            }

            onSuccess()
        }
    }

    fun updateAppointmentStatus(appointmentId: Long, status: String) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(appointmentId, status)
        }
    }

    fun deleteAppointment(appointment: AppointmentEntity) {
        viewModelScope.launch {
            repository.deleteAppointment(appointment)
            notificationScheduler?.cancelAppointmentReminder(appointment.id)
        }
    }

    companion object {
        fun getStartOfDayMillis(timeMillis: Long): Long {
            return Calendar.getInstance().apply {
                timeInMillis = timeMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
    }
}

class AgendaViewModelFactory(
    private val repository: AgendaRepository,
    private val notificationScheduler: NotificationScheduler? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AgendaViewModel::class.java)) {
            return AgendaViewModel(repository, notificationScheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
