package com.josuefp.servicenotebook.data.repository

import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.local.entity.InventoryMovementEntity
import com.josuefp.servicenotebook.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

class InventoryRepository(private val database: AppDatabase) {

    private val productDao = database.productDao()
    private val movementDao = database.inventoryMovementDao()

    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()

    fun getLowStockProducts(): Flow<List<ProductEntity>> = productDao.getLowStockProducts()

    suspend fun getProductById(id: Long): ProductEntity? = productDao.getProductById(id)

    suspend fun addProduct(product: ProductEntity): Long {
        val id = productDao.insertProduct(product)
        if (product.stockQuantity > 0) {
            movementDao.insertMovement(
                InventoryMovementEntity(
                    productId = id,
                    movementType = "ENTRY",
                    quantity = product.stockQuantity,
                    reason = "Inventario inicial"
                )
            )
        }
        return id
    }

    suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    suspend fun registerMovement(
        productId: Long,
        type: String,
        quantity: Double,
        reason: String
    ): Boolean {
        val product = productDao.getProductById(productId) ?: return false

        val newStock = when (type) {
            "ENTRY" -> product.stockQuantity + quantity
            "EXIT" -> (product.stockQuantity - quantity).coerceAtLeast(0.0)
            "ADJUSTMENT" -> quantity
            else -> product.stockQuantity
        }

        productDao.updateProduct(
            product.copy(
                stockQuantity = newStock,
                updatedAt = System.currentTimeMillis()
            )
        )

        movementDao.insertMovement(
            InventoryMovementEntity(
                productId = productId,
                movementType = type,
                quantity = quantity,
                reason = reason
            )
        )

        return true
    }

    fun getMovementsForProduct(productId: Long): Flow<List<InventoryMovementEntity>> =
        movementDao.getMovementsForProduct(productId)

    fun getAllMovements(): Flow<List<InventoryMovementEntity>> = movementDao.getAllMovements()
}
