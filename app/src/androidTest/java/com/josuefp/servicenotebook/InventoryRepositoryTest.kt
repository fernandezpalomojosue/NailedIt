package com.josuefp.servicenotebook

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.local.entity.ProductEntity
import com.josuefp.servicenotebook.data.repository.InventoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InventoryRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: InventoryRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = InventoryRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun addProductAndCheckInitialMovement() = runBlocking {
        val productId = repository.addProduct(
            ProductEntity(
                name = "Esmalte Rosa",
                unit = "unidad",
                unitCost = 12.0,
                stockQuantity = 10.0,
                minStock = 3.0
            )
        )

        val product = repository.getProductById(productId)
        assertEquals("Esmalte Rosa", product?.name)
        assertEquals(10.0, product?.stockQuantity ?: 0.0, 0.01)

        val movements = repository.getAllMovements().first()
        assertEquals(1, movements.size)
        assertEquals("ENTRY", movements[0].movementType)
        assertEquals(10.0, movements[0].quantity, 0.01)
    }

    @Test
    fun registerExitMovementUpdatesStock() = runBlocking {
        val productId = repository.addProduct(
            ProductEntity(
                name = "Acritip Líquido",
                unit = "ml",
                unitCost = 50.0,
                stockQuantity = 100.0,
                minStock = 20.0
            )
        )

        val success = repository.registerMovement(
            productId = productId,
            type = "EXIT",
            quantity = 25.0,
            reason = "Consumo en servicio de uñas"
        )

        assertTrue(success)

        val updatedProduct = repository.getProductById(productId)
        assertEquals(75.0, updatedProduct?.stockQuantity ?: 0.0, 0.01)

        val movements = repository.getMovementsForProduct(productId).first()
        assertEquals(2, movements.size)
    }

    @Test
    fun lowStockDetection() = runBlocking {
        repository.addProduct(
            ProductEntity(name = "Producto Normal", unit = "u", unitCost = 5.0, stockQuantity = 50.0, minStock = 10.0)
        )
        repository.addProduct(
            ProductEntity(name = "Producto Bajo", unit = "u", unitCost = 5.0, stockQuantity = 2.0, minStock = 5.0)
        )

        val lowStockList = repository.getLowStockProducts().first()
        assertEquals(1, lowStockList.size)
        assertEquals("Producto Bajo", lowStockList[0].name)
    }
}
