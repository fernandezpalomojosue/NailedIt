package com.josuefp.servicenotebook

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.local.entity.AppointmentEntity
import com.josuefp.servicenotebook.data.local.entity.ClientEntity
import com.josuefp.servicenotebook.data.repository.AgendaRepository
import com.josuefp.servicenotebook.data.repository.InventoryRepository
import com.josuefp.servicenotebook.ui.viewmodel.ReportTimeFilter
import com.josuefp.servicenotebook.ui.viewmodel.ReportsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class ReportsTest {

    private lateinit var db: AppDatabase
    private lateinit var agendaRepo: AgendaRepository
    private lateinit var inventoryRepo: InventoryRepository
    private lateinit var viewModel: ReportsViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        agendaRepo = AgendaRepository(db)
        inventoryRepo = InventoryRepository(db)
        viewModel = ReportsViewModel(agendaRepo, inventoryRepo)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun reportIncludesAppointmentsInCurrentMonthAndYear() = runBlocking {
        val clientId = agendaRepo.saveClient(ClientEntity(name = "Ana Fernández"))

        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 15)
            set(Calendar.HOUR_OF_DAY, 10)
        }

        agendaRepo.saveAppointmentWithServices(
            AppointmentEntity(
                clientId = clientId,
                serviceId = null,
                appointmentDate = cal.timeInMillis,
                durationMinutes = 60,
                agreedPrice = 50.0,
                status = "CONFIRMED"
            ),
            emptyList()
        )

        viewModel.setTimeFilter(ReportTimeFilter.THIS_MONTH)
        val stateThisMonth = viewModel.reportState.first()
        assertEquals(50.0, stateThisMonth.totalIncome, 0.01)
        assertEquals(1, stateThisMonth.completedAppointmentsCount)

        viewModel.setTimeFilter(ReportTimeFilter.THIS_YEAR)
        val stateThisYear = viewModel.reportState.first()
        assertEquals(50.0, stateThisYear.totalIncome, 0.01)
        assertEquals(1, stateThisYear.completedAppointmentsCount)
    }
}
