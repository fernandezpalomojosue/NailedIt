package com.josuefp.servicenotebook

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.josuefp.servicenotebook.data.backup.BackupManager
import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.local.entity.ClientEntity
import com.josuefp.servicenotebook.data.local.entity.ProductEntity
import com.josuefp.servicenotebook.data.local.entity.ServiceEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var backupManager: BackupManager

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        backupManager = BackupManager(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertProductAndRetrieve() = runBlocking {
        val product = ProductEntity(
            name = "Esmalte Semipermanente Rojo",
            unit = "unidad",
            unitCost = 15.0,
            stockQuantity = 5.0,
            minStock = 2.0
        )
        val id = db.productDao().insertProduct(product)
        val fetched = db.productDao().getProductById(id)

        assertEquals("Esmalte Semipermanente Rojo", fetched?.name)
        assertEquals(15.0, fetched?.unitCost ?: 0.0, 0.01)
    }

    @Test
    fun backupAndRestoreData() = runBlocking {
        db.productDao().insertProduct(
            ProductEntity(name = "Gel UV", unit = "ml", unitCost = 25.0, stockQuantity = 100.0)
        )
        db.clientDao().insertClient(
            ClientEntity(name = "María López", phone = "555-1234")
        )
        db.serviceDao().insertService(
            ServiceEntity(name = "Manicura Rusa", basePrice = 30.0)
        )

        val json = backupManager.createBackupJson()
        assertTrue(json.contains("Gel UV"))
        assertTrue(json.contains("María López"))

        val restored = backupManager.restoreFromBackupJson(json)
        assertTrue(restored)

        val products = db.productDao().getAllProductsList()
        val clients = db.clientDao().getAllClientsList()
        val services = db.serviceDao().getAllServicesList()

        assertEquals(1, products.size)
        assertEquals("Gel UV", products[0].name)
        assertEquals(1, clients.size)
        assertEquals("María López", clients[0].name)
        assertEquals(1, services.size)
        assertEquals("Manicura Rusa", services[0].name)
    }
}
