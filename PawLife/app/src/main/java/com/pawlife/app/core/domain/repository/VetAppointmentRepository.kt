package com.pawlife.app.core.domain.repository

import com.pawlife.app.core.data.db.entity.VetAppointmentEntity
import kotlinx.coroutines.flow.Flow

interface VetAppointmentRepository {
    fun getAppointmentsForPet(petId: Long): Flow<List<VetAppointmentEntity>>
    suspend fun insertAppointment(appointment: VetAppointmentEntity): Long
    suspend fun updateAppointment(appointment: VetAppointmentEntity)
    suspend fun deleteAppointment(appointment: VetAppointmentEntity)
}
