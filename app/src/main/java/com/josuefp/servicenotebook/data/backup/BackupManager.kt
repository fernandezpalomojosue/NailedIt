package com.josuefp.servicenotebook.data.backup

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.josuefp.servicenotebook.data.local.AppDatabase

class BackupManager(private val database: AppDatabase) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun createBackupJson(): String {
        val backupData = BackupData(
            version = 1,
            timestamp = System.currentTimeMillis(),
            products = database.productDao().getAllProductsList(),
            inventoryMovements = database.inventoryMovementDao().getAllMovementsList(),
            services = database.serviceDao().getAllServicesList(),
            serviceMaterials = database.serviceDao().getAllServiceMaterialsList(),
            clients = database.clientDao().getAllClientsList(),
            appointments = database.appointmentDao().getAllAppointmentsList()
        )
        return gson.toJson(backupData)
    }

    suspend fun restoreFromBackupJson(jsonString: String): Boolean {
        return try {
            val backupData = gson.fromJson(jsonString, BackupData::class.java) ?: return false

            database.appointmentDao().deleteAll()
            database.clientDao().deleteAll()
            database.serviceDao().deleteAllMaterials()
            database.serviceDao().deleteAll()
            database.inventoryMovementDao().deleteAll()
            database.productDao().deleteAll()

            if (backupData.products.isNotEmpty()) {
                database.productDao().insertAll(backupData.products)
            }
            if (backupData.inventoryMovements.isNotEmpty()) {
                database.inventoryMovementDao().insertAll(backupData.inventoryMovements)
            }
            if (backupData.services.isNotEmpty()) {
                database.serviceDao().insertAll(backupData.services)
            }
            if (backupData.serviceMaterials.isNotEmpty()) {
                database.serviceDao().insertAllServiceMaterials(backupData.serviceMaterials)
            }
            if (backupData.clients.isNotEmpty()) {
                database.clientDao().insertAll(backupData.clients)
            }
            if (backupData.appointments.isNotEmpty()) {
                database.appointmentDao().insertAll(backupData.appointments)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
