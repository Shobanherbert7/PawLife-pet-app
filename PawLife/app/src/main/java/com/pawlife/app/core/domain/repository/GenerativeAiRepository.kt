package com.pawlife.app.core.domain.repository

import com.pawlife.app.core.data.db.entity.PetEntity
import kotlinx.coroutines.flow.Flow

interface GenerativeAiRepository {
    fun generateHealthTriageStream(
        pet: PetEntity,
        symptoms: String
    ): Flow<String>
    fun analyzePetImage(bitmap: android.graphics.Bitmap): Flow<String>
    
    fun simulateWhatIfScenario(scenario: String): Flow<String>
    
    fun parseVetRecord(bitmap: android.graphics.Bitmap): Flow<String>
}
