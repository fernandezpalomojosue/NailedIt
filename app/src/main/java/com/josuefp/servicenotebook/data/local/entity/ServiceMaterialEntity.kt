package com.josuefp.servicenotebook.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "service_materials",
    foreignKeys = [
        ForeignKey(
            entity = ServiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["serviceId"]), Index(value = ["productId"])]
)
data class ServiceMaterialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serviceId: Long,
    val productId: Long,
    val estimatedQuantity: Double
)
