package com.josuefp.servicenotebook

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.local.entity.ProductEntity
import com.josuefp.servicenotebook.data.local.entity.ServiceEntity
import com.josuefp.servicenotebook.data.repository.CatalogRepository
import com.josuefp.servicenotebook.data.repository.InventoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var catalogRepository: CatalogRepository
    private lateinit var inventoryRepository: InventoryRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        catalogRepository = CatalogRepository(db)
        inventoryRepository = InventoryRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun createServiceWithMaterialsAndCalculateCost() = runBlocking {
        val p1 = inventoryRepository.addProduct(
            ProductEntity(name = "Esmalte Gel", unit = "ml", unitCost = 2.0, stockQuantity = 50.0)
        )
        val p2 = inventoryRepository.addProduct(
            ProductEntity(name = "Lima Desechable", unit = "unidad", unitCost = 1.5, stockQuantity = 20.0)
        )

        val serviceId = catalogRepository.saveServiceWithMaterials(
            service = ServiceEntity(
                name = "Manicura Pro",
                description = "Manicura completa",
                basePrice = 30.0,
                estimatedDurationMinutes = 60,
                laborCost = 10.0,
                overheadCost = 3.0
            ),
            materials = listOf(p1 to 5.0, p2 to 1.0)
        )

        val service = catalogRepository.getServiceById(serviceId)
        assertEquals("Manicura Pro", service?.name)

        val materialsWithProducts = catalogRepository.getServiceMaterialsWithProducts(serviceId).first()
        assertEquals(2, materialsWithProducts.size)

        val materialsTotalCost = materialsWithProducts.sumOf { it.totalCost }
        assertEquals(11.5, materialsTotalCost, 0.01)

        val totalCost = materialsTotalCost + (service?.laborCost ?: 0.0) + (service?.overheadCost ?: 0.0)
        assertEquals(24.5, totalCost, 0.01)
    }
}
