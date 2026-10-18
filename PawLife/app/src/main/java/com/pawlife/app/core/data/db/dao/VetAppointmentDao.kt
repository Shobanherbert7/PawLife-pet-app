package com.pawlife.app.core.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pawlife.app.core.data.db.entity.VetAppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VetAppointmentDao {
    @Query("SELECT * FROM vet_appointments WHERE petId = :petId ORDER BY appointmentTime ASC")
    fun getAppointmentsForPet(petId: Long): Flow<List<VetAppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: VetAppointmentEntity): Long

    @Update
    suspend fun updateAppointment(appointment: VetAppointmentEntity)

    @Delete
    suspend fun deleteAppointment(appointment: VetAppointmentEntity)
}
