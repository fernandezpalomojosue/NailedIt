package com.josuefp.servicenotebook.data.repository

import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.local.entity.AppConfigEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val database: AppDatabase) {
    private val dao = database.appConfigDao()

    val appName: Flow<String> = dao.getAppConfig().map { it?.appName ?: "Virna Nails" }

    suspend fun updateAppName(newName: String) {
        dao.saveAppConfig(AppConfigEntity(id = 1, appName = newName.trim().ifBlank { "Virna Nails" }))
    }
}
