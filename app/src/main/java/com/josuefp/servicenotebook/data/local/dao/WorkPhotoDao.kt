package com.josuefp.servicenotebook.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.josuefp.servicenotebook.data.local.entity.WorkPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkPhotoDao {
    @Query("SELECT * FROM work_photos ORDER BY createdAt DESC")
    fun getAllPhotos(): Flow<List<WorkPhotoEntity>>

    @Query("SELECT * FROM work_photos WHERE category = :category ORDER BY createdAt DESC")
    fun getPhotosByCategory(category: String): Flow<List<WorkPhotoEntity>>

    @Query("SELECT * FROM work_photos")
    suspend fun getAllPhotosList(): List<WorkPhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: WorkPhotoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(photos: List<WorkPhotoEntity>)

    @Delete
    suspend fun deletePhoto(photo: WorkPhotoEntity)

    @Query("DELETE FROM work_photos")
    suspend fun deleteAll()
}
