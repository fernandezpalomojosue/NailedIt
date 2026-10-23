package com.josuefp.servicenotebook

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.local.entity.WorkPhotoEntity
import com.josuefp.servicenotebook.data.repository.GalleryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GalleryRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: GalleryRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = GalleryRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun saveAndFilterPhotosByCategory() = runBlocking {
        val photo1 = WorkPhotoEntity(
            imagePath = "/data/photo1.jpg",
            category = "Manos",
            description = "Esculpido Gel Rosa"
        )
        val photo2 = WorkPhotoEntity(
            imagePath = "/data/photo2.jpg",
            category = "Pies",
            description = "Pedicura Francesa"
        )

        db.workPhotoDao().insertPhoto(photo1)
        db.workPhotoDao().insertPhoto(photo2)

        val handsPhotos = repository.getPhotosByCategory("Manos").first()
        assertEquals(1, handsPhotos.size)
        assertEquals("Esculpido Gel Rosa", handsPhotos[0].description)

        val feetPhotos = repository.getPhotosByCategory("Pies").first()
        assertEquals(1, feetPhotos.size)
        assertEquals("Pedicura Francesa", feetPhotos[0].description)
    }
}
