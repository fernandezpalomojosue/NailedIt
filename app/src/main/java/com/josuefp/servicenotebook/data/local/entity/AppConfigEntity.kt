package com.josuefp.servicenotebook.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey
    val id: Int = 1,
    val appName: String = "Virna Nails"
)
