package com.josuefp.servicenotebook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val category: String = "Manos",
    val basePrice: Double = 0.0,
    val estimatedDurationMinutes: Int = 60,
    val laborCost: Double = 0.0,
    val overheadCost: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
