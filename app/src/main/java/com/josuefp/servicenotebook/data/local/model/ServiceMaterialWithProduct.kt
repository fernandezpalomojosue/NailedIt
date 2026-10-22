package com.josuefp.servicenotebook.data.local.model

import com.josuefp.servicenotebook.data.local.entity.ProductEntity
import com.josuefp.servicenotebook.data.local.entity.ServiceMaterialEntity

data class ServiceMaterialWithProduct(
    val serviceMaterial: ServiceMaterialEntity,
    val product: ProductEntity
) {
    val totalCost: Double
        get() = serviceMaterial.estimatedQuantity * product.unitCost
}
