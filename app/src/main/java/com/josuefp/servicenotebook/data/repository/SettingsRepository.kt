package com.josuefp.servicenotebook.data.repository

import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.local.entity.AppConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsRepository(private val database: AppDatabase) {
    private val dao = database.appConfigDao()

    suspend fun getAppConfigOnce(): AppConfigEntity = withContext(Dispatchers.IO) {
        dao.getAppConfigOnce() ?: AppConfigEntity().also { default ->
            dao.saveAppConfig(default)
        }
    }

    suspend fun updateAppName(newName: String) = withContext(Dispatchers.IO) {
        val current = dao.getAppConfigOnce() ?: AppConfigEntity()
        val updated = current.copy(appName = newName.trim().ifBlank { "Virna Nails" })
        dao.saveAppConfig(updated)
    }

    suspend fun updateThemePalette(newPalette: String) = withContext(Dispatchers.IO) {
        val current = dao.getAppConfigOnce() ?: AppConfigEntity()
        val updated = current.copy(themePalette = newPalette)
        dao.saveAppConfig(updated)
    }
}
