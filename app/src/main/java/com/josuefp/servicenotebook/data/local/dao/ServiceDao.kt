package com.josuefp.servicenotebook.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.josuefp.servicenotebook.data.local.entity.ServiceEntity
import com.josuefp.servicenotebook.data.local.entity.ServiceMaterialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveServices(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services")
    fun getAllServicesFlow(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE id = :id")
    suspend fun getServiceById(id: Long): ServiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(services: List<ServiceEntity>)

    @Update
    suspend fun updateService(service: ServiceEntity)

    @Delete
    suspend fun deleteService(service: ServiceEntity)

    @Query("DELETE FROM services")
    suspend fun deleteAll()

    @Query("SELECT * FROM services")
    suspend fun getAllServicesList(): List<ServiceEntity>

    // Service materials
    @Query("SELECT * FROM service_materials WHERE serviceId = :serviceId")
    fun getMaterialsForService(serviceId: Long): Flow<List<ServiceMaterialEntity>>

    @Query("SELECT * FROM service_materials WHERE serviceId = :serviceId")
    suspend fun getMaterialsForServiceList(serviceId: Long): List<ServiceMaterialEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceMaterial(serviceMaterial: ServiceMaterialEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllServiceMaterials(materials: List<ServiceMaterialEntity>)

    @Query("DELETE FROM service_materials WHERE serviceId = :serviceId")
    suspend fun deleteMaterialsForService(serviceId: Long)

    @Query("DELETE FROM service_materials")
    suspend fun deleteAllMaterials()

    @Query("SELECT * FROM service_materials")
    suspend fun getAllServiceMaterialsList(): List<ServiceMaterialEntity>
}
