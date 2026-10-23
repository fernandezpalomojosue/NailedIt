package com.josuefp.servicenotebook.data.repository

import android.content.Context
import android.net.Uri
import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.local.entity.WorkPhotoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class GalleryRepository(private val database: AppDatabase) {

    private val photoDao = database.workPhotoDao()

    fun getAllPhotos(): Flow<List<WorkPhotoEntity>> = photoDao.getAllPhotos()

    fun getPhotosByCategory(category: String): Flow<List<WorkPhotoEntity>> = photoDao.getPhotosByCategory(category)

    suspend fun savePhotoFromUri(
        context: Context,
        uri: Uri,
        category: String,
        description: String
    ): Long = withContext(Dispatchers.IO) {
        val galleryDir = File(context.filesDir, "gallery").apply { if (!exists()) mkdirs() }
        val photoFile = File(galleryDir, "photo_${System.currentTimeMillis()}.jpg")

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(photoFile).use { output ->
                input.copyTo(output)
            }
        }

        val photoEntity = WorkPhotoEntity(
            imagePath = photoFile.absolutePath,
            category = category.trim(),
            description = description.trim()
        )

        photoDao.insertPhoto(photoEntity)
    }

    suspend fun deletePhoto(context: Context, photo: WorkPhotoEntity) = withContext(Dispatchers.IO) {
        try {
            val file = File(photo.imagePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        photoDao.deletePhoto(photo)
    }
}
