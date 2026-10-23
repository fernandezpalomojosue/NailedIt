package com.josuefp.servicenotebook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.josuefp.servicenotebook.data.local.model.AppointmentWithDetails
import com.josuefp.servicenotebook.data.repository.AgendaRepository
import com.josuefp.servicenotebook.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

enum class ReportTimeFilter {
    THIS_MONTH,
    LAST_MONTH,
    THIS_YEAR,
    ALL_TIME
}

data class FinancialReportState(
    val totalIncome: Double = 0.0,
    val totalInventoryExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val completedAppointmentsCount: Int = 0,
    val timeFilter: ReportTimeFilter = ReportTimeFilter.THIS_MONTH
)

class ReportsViewModel(
    agendaRepository: AgendaRepository,
    inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _timeFilter = MutableStateFlow(ReportTimeFilter.THIS_MONTH)
    val timeFilter: StateFlow<ReportTimeFilter> = _timeFilter

    val appointments: StateFlow<List<AppointmentWithDetails>> = agendaRepository.getAllAppointmentsWithDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reportState: StateFlow<FinancialReportState> = combine(appointments, _timeFilter) { apps, filter ->
        val (startTime, endTime) = getTimeRangeMillis(filter)

        val filteredApps = apps.filter { app ->
            val date = app.appointment.appointmentDate
            app.appointment.status != "CANCELLED" && (filter == ReportTimeFilter.ALL_TIME || (date in startTime..endTime))
        }

        val income = filteredApps.sumOf { it.appointment.agreedPrice }

        FinancialReportState(
            totalIncome = income,
            totalInventoryExpenses = 0.0,
            netProfit = income,
            completedAppointmentsCount = filteredApps.size,
            timeFilter = filter
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialReportState())

    fun setTimeFilter(filter: ReportTimeFilter) {
        _timeFilter.value = filter
    }

    private fun getTimeRangeMillis(filter: ReportTimeFilter): Pair<Long, Long> {
        val cal = Calendar.getInstance()

        return when (filter) {
            ReportTimeFilter.THIS_MONTH -> {
                val startCal = (cal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = (cal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                startCal.timeInMillis to endCal.timeInMillis
            }
            ReportTimeFilter.LAST_MONTH -> {
                val startCal = (cal.clone() as Calendar).apply {
                    add(Calendar.MONTH, -1)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = (cal.clone() as Calendar).apply {
                    add(Calendar.MONTH, -1)
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                startCal.timeInMillis to endCal.timeInMillis
            }
            ReportTimeFilter.THIS_YEAR -> {
                val startCal = (cal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val endCal = (cal.clone() as Calendar).apply {
                    set(Calendar.MONTH, Calendar.DECEMBER)
                    set(Calendar.DAY_OF_MONTH, 31)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                startCal.timeInMillis to endCal.timeInMillis
            }
            ReportTimeFilter.ALL_TIME -> 0L to Long.MAX_VALUE
        }
    }
}

class ReportsViewModelFactory(
    private val agendaRepository: AgendaRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
            return ReportsViewModel(agendaRepository, inventoryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
