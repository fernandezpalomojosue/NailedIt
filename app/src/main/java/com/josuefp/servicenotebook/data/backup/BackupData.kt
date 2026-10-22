package com.josuefp.servicenotebook.data.backup

import com.josuefp.servicenotebook.data.local.entity.AppointmentEntity
import com.josuefp.servicenotebook.data.local.entity.ClientEntity
import com.josuefp.servicenotebook.data.local.entity.InventoryMovementEntity
import com.josuefp.servicenotebook.data.local.entity.ProductEntity
import com.josuefp.servicenotebook.data.local.entity.ServiceEntity
import com.josuefp.servicenotebook.data.local.entity.ServiceMaterialEntity

data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val products: List<ProductEntity> = emptyList(),
    val inventoryMovements: List<InventoryMovementEntity> = emptyList(),
    val services: List<ServiceEntity> = emptyList(),
    val serviceMaterials: List<ServiceMaterialEntity> = emptyList(),
    val clients: List<ClientEntity> = emptyList(),
    val appointments: List<AppointmentEntity> = emptyList()
)
