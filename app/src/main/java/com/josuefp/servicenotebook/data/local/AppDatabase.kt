package com.josuefp.servicenotebook.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.josuefp.servicenotebook.data.local.dao.AppConfigDao
import com.josuefp.servicenotebook.data.local.dao.AppointmentDao
import com.josuefp.servicenotebook.data.local.dao.ClientDao
import com.josuefp.servicenotebook.data.local.dao.InventoryMovementDao
import com.josuefp.servicenotebook.data.local.dao.ProductDao
import com.josuefp.servicenotebook.data.local.dao.ServiceDao
import com.josuefp.servicenotebook.data.local.entity.AppConfigEntity
import com.josuefp.servicenotebook.data.local.entity.AppointmentEntity
import com.josuefp.servicenotebook.data.local.entity.ClientEntity
import com.josuefp.servicenotebook.data.local.entity.InventoryMovementEntity
import com.josuefp.servicenotebook.data.local.entity.ProductEntity
import com.josuefp.servicenotebook.data.local.entity.ServiceEntity
import com.josuefp.servicenotebook.data.local.entity.ServiceMaterialEntity

@Database(
    entities = [
        ProductEntity::class,
        InventoryMovementEntity::class,
        ServiceEntity::class,
        ServiceMaterialEntity::class,
        ClientEntity::class,
        AppointmentEntity::class,
        AppConfigEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun inventoryMovementDao(): InventoryMovementDao
    abstract fun serviceDao(): ServiceDao
    abstract fun clientDao(): ClientDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun appConfigDao(): AppConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "virna_nails_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
