package com.pawlife.app.feature.weight.domain

import com.pawlife.app.core.data.db.entity.WeightRecordEntity
import com.pawlife.app.core.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWeightHistoryUseCase @Inject constructor(
    private val repository: WeightRepository
) {
    operator fun invoke(petId: Long): Flow<List<WeightRecordEntity>> {
        return repository.getWeightHistory(petId)
    }
}
