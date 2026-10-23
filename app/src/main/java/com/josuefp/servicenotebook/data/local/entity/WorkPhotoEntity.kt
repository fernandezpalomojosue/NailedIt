package com.josuefp.servicenotebook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_photos")
data class WorkPhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imagePath: String,
    val category: String = "Manos",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
