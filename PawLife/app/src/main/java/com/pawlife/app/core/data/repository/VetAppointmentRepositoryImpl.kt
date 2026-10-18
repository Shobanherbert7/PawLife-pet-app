package com.pawlife.app.core.data.repository

import com.pawlife.app.core.data.db.dao.VetAppointmentDao
import com.pawlife.app.core.data.db.entity.VetAppointmentEntity
import com.pawlife.app.core.domain.repository.VetAppointmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VetAppointmentRepositoryImpl @Inject constructor(
    private val dao: VetAppointmentDao
) : VetAppointmentRepository {
    override fun getAppointmentsForPet(petId: Long): Flow<List<VetAppointmentEntity>> {
        return dao.getAppointmentsForPet(petId)
    }

    override suspend fun insertAppointment(appointment: VetAppointmentEntity): Long {
        return dao.insertAppointment(appointment)
    }

    override suspend fun updateAppointment(appointment: VetAppointmentEntity) {
        dao.updateAppointment(appointment)
    }

    override suspend fun deleteAppointment(appointment: VetAppointmentEntity) {
        dao.deleteAppointment(appointment)
    }
}
