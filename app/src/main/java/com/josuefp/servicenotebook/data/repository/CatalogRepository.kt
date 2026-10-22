package com.josuefp.servicenotebook.data.repository

import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.local.entity.ProductEntity
import com.josuefp.servicenotebook.data.local.entity.ServiceEntity
import com.josuefp.servicenotebook.data.local.entity.ServiceMaterialEntity
import com.josuefp.servicenotebook.data.local.model.ServiceMaterialWithProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class CatalogRepository(private val database: AppDatabase) {

    private val serviceDao = database.serviceDao()
    private val productDao = database.productDao()

    fun getAllActiveServices(): Flow<List<ServiceEntity>> = serviceDao.getAllActiveServices()

    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()

    suspend fun getServiceById(id: Long): ServiceEntity? = serviceDao.getServiceById(id)

    suspend fun saveServiceWithMaterials(
        service: ServiceEntity,
        materials: List<Pair<Long, Double>>
    ): Long {
        val serviceId = if (service.id == 0L) {
            serviceDao.insertService(service)
        } else {
            serviceDao.updateService(service)
            service.id
        }

        serviceDao.deleteMaterialsForService(serviceId)
        val materialEntities = materials.map { (productId, qty) ->
            ServiceMaterialEntity(
                serviceId = serviceId,
                productId = productId,
                estimatedQuantity = qty
            )
        }
        if (materialEntities.isNotEmpty()) {
            serviceDao.insertAllServiceMaterials(materialEntities)
        }

        return serviceId
    }

    suspend fun deleteService(service: ServiceEntity) {
        serviceDao.deleteService(service)
    }

    fun getServiceMaterialsWithProducts(serviceId: Long): Flow<List<ServiceMaterialWithProduct>> {
        val materialsFlow = serviceDao.getMaterialsForService(serviceId)
        val productsFlow = productDao.getAllProducts()

        return combine(materialsFlow, productsFlow) { materials, products ->
            val productMap = products.associateBy { it.id }
            materials.mapNotNull { mat ->
                productMap[mat.productId]?.let { prod ->
                    ServiceMaterialWithProduct(mat, prod)
                }
            }
        }
    }
}
