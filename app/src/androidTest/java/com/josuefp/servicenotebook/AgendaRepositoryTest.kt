package com.josuefp.servicenotebook

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.local.entity.AppointmentEntity
import com.josuefp.servicenotebook.data.local.entity.ClientEntity
import com.josuefp.servicenotebook.data.local.entity.ProductEntity
import com.josuefp.servicenotebook.data.local.entity.ServiceEntity
import com.josuefp.servicenotebook.data.repository.AgendaRepository
import com.josuefp.servicenotebook.data.repository.CatalogRepository
import com.josuefp.servicenotebook.data.repository.InventoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class AgendaRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: AgendaRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = AgendaRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun saveClientAndRetrieve() = runBlocking {
        val clientId = repository.saveClient(
            ClientEntity(name = "Laura Gómez", phone = "555-9876", email = "laura@example.com")
        )

        val client = repository.getClientById(clientId)
        assertEquals("Laura Gómez", client?.name)
        assertEquals("555-9876", client?.phone)
    }

    @Test
    fun detectAppointmentOverlap() = runBlocking {
        val clientId = repository.saveClient(ClientEntity(name = "Ana Torres", phone = "555-0000"))

        val baseTime = Calendar.getInstance().apply {
            set(2025, Calendar.MAY, 15, 10, 0, 0)
        }.timeInMillis

        repository.saveAppointmentWithServices(
            AppointmentEntity(
                clientId = clientId,
                serviceId = null,
                appointmentDate = baseTime,
                durationMinutes = 60,
                agreedPrice = 30.0,
                status = "CONFIRMED"
            ),
            emptyList()
        )

        val overlapTime1 = baseTime + (30 * 60 * 1000L)
        val conflict1 = repository.checkOverlap(
            startMillis = overlapTime1,
            durationMinutes = 60
        )
        assertNotNull(conflict1)
        assertEquals("Ana Torres", conflict1?.client?.name)

        val noOverlapTime = baseTime + (90 * 60 * 1000L)
        val conflict2 = repository.checkOverlap(
            startMillis = noOverlapTime,
            durationMinutes = 60
        )
        assertNull(conflict2)
    }

    @Test
    fun updateAppointmentStatus() = runBlocking {
        val clientId = repository.saveClient(ClientEntity(name = "Sofia Rivas", phone = "555-1111"))
        val appId = repository.saveAppointmentWithServices(
            AppointmentEntity(
                clientId = clientId,
                serviceId = null,
                appointmentDate = System.currentTimeMillis(),
                durationMinutes = 60,
                agreedPrice = 40.0,
                status = "PENDING"
            ),
            emptyList()
        )

        repository.updateAppointmentStatus(appId, "COMPLETED")

        val app = repository.getAppointmentById(appId)
        assertEquals("COMPLETED", app?.status)

        val allWithDetails = repository.getAllAppointmentsWithDetails().first()
        assertEquals(1, allWithDetails.size)
        assertEquals("Sofia Rivas", allWithDetails[0].client?.name)
    }

    @Test
    fun completingAppointmentConsumesMaterials() = runBlocking {
        val inventoryRepo = InventoryRepository(db)
        val catalogRepo = CatalogRepository(db)

        val prodId = inventoryRepo.addProduct(
            ProductEntity(name = "Gel Pink", unit = "ml", unitCost = 2.0, stockQuantity = 50.0)
        )

        val serviceId = catalogRepo.saveServiceWithMaterials(
            service = ServiceEntity(name = "Uñas Gel", basePrice = 40.0, laborCost = 10.0),
            materials = listOf(prodId to 5.0)
        )

        val clientId = repository.saveClient(ClientEntity(name = "Carla Ruiz", phone = "555-3333"))
        val appId = repository.saveAppointmentWithServices(
            AppointmentEntity(
                clientId = clientId,
                serviceId = serviceId,
                appointmentDate = System.currentTimeMillis(),
                durationMinutes = 60,
                agreedPrice = 40.0,
                status = "CONFIRMED"
            ),
            listOf(serviceId)
        )

        repository.updateAppointmentStatus(appId, "COMPLETED")

        val product = inventoryRepo.getProductById(prodId)
        assertEquals(45.0, product?.stockQuantity ?: 0.0, 0.01)

        val movements = inventoryRepo.getMovementsForProduct(prodId).first()
        assertEquals(2, movements.size)
        assertEquals("EXIT", movements[0].movementType)
        assertEquals(5.0, movements[0].quantity, 0.01)
    }
}
