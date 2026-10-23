package com.josuefp.servicenotebook.data.local.model

import com.josuefp.servicenotebook.data.local.entity.AppointmentEntity
import com.josuefp.servicenotebook.data.local.entity.ClientEntity
import com.josuefp.servicenotebook.data.local.entity.ServiceEntity

data class AppointmentWithDetails(
    val appointment: AppointmentEntity,
    val client: ClientEntity?,
    val services: List<ServiceEntity> = emptyList()
) {
    val endTimeMillis: Long
        get() = appointment.appointmentDate + (appointment.durationMinutes * 60 * 1000L)
}
