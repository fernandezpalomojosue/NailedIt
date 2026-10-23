package com.josuefp.servicenotebook.data.repository

import com.josuefp.servicenotebook.data.local.AppDatabase
import com.josuefp.servicenotebook.data.local.entity.AppointmentEntity
import com.josuefp.servicenotebook.data.local.entity.AppointmentServiceEntity
import com.josuefp.servicenotebook.data.local.entity.ClientEntity
import com.josuefp.servicenotebook.data.local.entity.InventoryMovementEntity
import com.josuefp.servicenotebook.data.local.entity.ServiceEntity
import com.josuefp.servicenotebook.data.local.model.AppointmentWithDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.runBlocking

class AgendaRepository(private val database: AppDatabase) {

    private val appointmentDao = database.appointmentDao()
    private val clientDao = database.clientDao()
    private val serviceDao = database.serviceDao()

    fun getAllClients(): Flow<List<ClientEntity>> = clientDao.getAllClients()

    suspend fun getClientById(id: Long): ClientEntity? = clientDao.getClientById(id)

    suspend fun saveClient(client: ClientEntity): Long {
        return if (client.id == 0L) {
            clientDao.insertClient(client)
        } else {
            clientDao.updateClient(client)
            client.id
        }
    }

    suspend fun deleteClient(client: ClientEntity) {
        clientDao.deleteClient(client)
    }

    fun getAllActiveServices(): Flow<List<ServiceEntity>> = serviceDao.getAllActiveServices()

    fun getAllAppointmentsWithDetails(): Flow<List<AppointmentWithDetails>> {
        val appointmentsFlow = appointmentDao.getAllAppointments()
        val clientsFlow = clientDao.getAllClients()
        val servicesFlow = serviceDao.getAllServicesFlow()

        return combine(appointmentsFlow, clientsFlow, servicesFlow) { appointments, clients, services ->
            val clientMap = clients.associateBy { it.id }
            val serviceMap = services.associateBy { it.id }
            val allAppServices = runBlocking { appointmentDao.getAllAppointmentServicesList() }
            val appServicesMap = allAppServices.groupBy({ it.appointmentId }, { it.serviceId })

            appointments.map { app ->
                val serviceIds = appServicesMap[app.id] ?: emptyList()
                val matchedServices = serviceIds.mapNotNull { serviceMap[it] }

                val finalServices = if (matchedServices.isEmpty() && app.serviceId != null) {
                    serviceMap[app.serviceId]?.let { listOf(it) } ?: emptyList()
                } else {
                    matchedServices
                }

                AppointmentWithDetails(
                    appointment = app,
                    client = clientMap[app.clientId],
                    services = finalServices
                )
            }
        }
    }

    suspend fun getAppointmentById(id: Long): AppointmentEntity? = appointmentDao.getAppointmentById(id)

    suspend fun saveAppointmentWithServices(
        appointment: AppointmentEntity,
        serviceIds: List<Long>
    ): Long {
        val appointmentId = if (appointment.id == 0L) {
            appointmentDao.insertAppointment(appointment)
        } else {
            appointmentDao.updateAppointment(appointment)
            appointment.id
        }

        appointmentDao.deleteServicesForAppointment(appointmentId)
        val serviceMap = serviceDao.getAllServicesList().associateBy { it.id }
        val appServiceEntities = serviceIds.map { sId ->
            AppointmentServiceEntity(
                appointmentId = appointmentId,
                serviceId = sId,
                priceAtBooking = serviceMap[sId]?.basePrice ?: 0.0
            )
        }
        if (appServiceEntities.isNotEmpty()) {
            appointmentDao.insertAppointmentServices(appServiceEntities)
        }

        return appointmentId
    }

    suspend fun updateAppointmentStatus(appointmentId: Long, newStatus: String) {
        val appointment = appointmentDao.getAppointmentById(appointmentId)
        if (appointment != null) {
            val oldStatus = appointment.status
            appointmentDao.updateAppointment(appointment.copy(status = newStatus))

            if (newStatus == "COMPLETED" && oldStatus != "COMPLETED") {
                consumeMaterialsForAppointment(appointmentId)
            }
        }
    }

    private suspend fun consumeMaterialsForAppointment(appointmentId: Long) {
        val appServices = appointmentDao.getServicesForAppointment(appointmentId)
        val productDao = database.productDao()
        val movementDao = database.inventoryMovementDao()

        val serviceIds = if (appServices.isEmpty()) {
            val app = appointmentDao.getAppointmentById(appointmentId)
            app?.serviceId?.let { listOf(it) } ?: emptyList()
        } else {
            appServices.map { it.serviceId }
        }

        for (sId in serviceIds) {
            val materials = serviceDao.getMaterialsForServiceList(sId)
            for (mat in materials) {
                val product = productDao.getProductById(mat.productId)
                if (product != null) {
                    val newStock = (product.stockQuantity - mat.estimatedQuantity).coerceAtLeast(0.0)
                    productDao.updateProduct(
                        product.copy(
                            stockQuantity = newStock,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    movementDao.insertMovement(
                        InventoryMovementEntity(
                            productId = mat.productId,
                            movementType = "EXIT",
                            quantity = mat.estimatedQuantity,
                            reason = "Consumo automático por servicio realizado (Cita)"
                        )
                    )
                }
            }
        }
    }

    suspend fun deleteAppointment(appointment: AppointmentEntity) {
        appointmentDao.deleteAppointment(appointment)
    }

    suspend fun checkOverlap(
        startMillis: Long,
        durationMinutes: Int,
        excludeAppointmentId: Long = 0L
    ): AppointmentWithDetails? {
        val endMillis = startMillis + (durationMinutes * 60 * 1000L)
        val allAppointments = appointmentDao.getAllAppointmentsList()
        val clientMap = clientDao.getAllClientsList().associateBy { it.id }
        val serviceMap = serviceDao.getAllServicesList().associateBy { it.id }
        val allAppServices = appointmentDao.getAllAppointmentServicesList().groupBy({ it.appointmentId }, { it.serviceId })

        for (app in allAppointments) {
            if (app.id == excludeAppointmentId || app.status == "CANCELLED") continue

            val appStart = app.appointmentDate
            val appEnd = appStart + (app.durationMinutes * 60 * 1000L)

            if (startMillis < appEnd && endMillis > appStart) {
                val sIds = allAppServices[app.id] ?: app.serviceId?.let { listOf(it) } ?: emptyList()
                return AppointmentWithDetails(
                    appointment = app,
                    client = clientMap[app.clientId],
                    services = sIds.mapNotNull { serviceMap[it] }
                )
            }
        }
        return null
    }
}
