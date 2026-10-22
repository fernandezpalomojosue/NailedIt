package com.josuefp.servicenotebook.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "appointments",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ServiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["clientId"]), Index(value = ["serviceId"])]
)
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long,
    val serviceId: Long?,
    val appointmentDate: Long,
    val durationMinutes: Int = 60,
    val status: String = "PENDING",
    val agreedPrice: Double = 0.0,
    val notes: String = "",
    val reminderScheduled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
