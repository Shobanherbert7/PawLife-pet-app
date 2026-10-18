package com.pawlife.app.core.di

import com.pawlife.app.core.data.repository.ActivityRepositoryImpl
import com.pawlife.app.core.data.repository.CommunityRepositoryImpl
import com.pawlife.app.core.data.repository.MarketplaceRepositoryImpl
import com.pawlife.app.core.data.repository.DocumentRepositoryImpl
import com.pawlife.app.core.data.repository.FeedingRepositoryImpl
import com.pawlife.app.core.data.repository.GenerativeAiRepositoryImpl
import com.pawlife.app.core.data.repository.FirestorePetRepository
import com.pawlife.app.core.data.repository.PetRepositoryImpl
import com.pawlife.app.core.data.repository.VetAppointmentRepositoryImpl
import com.pawlife.app.core.data.repository.WeightRepositoryImpl
import com.pawlife.app.core.domain.repository.ActivityRepository
import com.pawlife.app.core.domain.repository.CommunityRepository
import com.pawlife.app.core.domain.repository.MarketplaceRepository
import com.pawlife.app.core.domain.repository.DocumentRepository
import com.pawlife.app.core.domain.repository.FeedingRepository
import com.pawlife.app.core.domain.repository.GenerativeAiRepository
import com.pawlife.app.core.domain.repository.PetRepository
import com.pawlife.app.core.domain.repository.VetAppointmentRepository
import com.pawlife.app.core.domain.repository.WeightRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWeightRepository(
        weightRepositoryImpl: WeightRepositoryImpl
    ): WeightRepository

    @Binds
    @Singleton
    abstract fun bindFeedingRepository(
        feedingRepositoryImpl: FeedingRepositoryImpl
    ): FeedingRepository

    @Binds
    @Singleton
    abstract fun bindActivityRepository(
        activityRepositoryImpl: ActivityRepositoryImpl
    ): ActivityRepository

    @Binds
    @Singleton
    abstract fun bindDocumentRepository(
        documentRepositoryImpl: DocumentRepositoryImpl
    ): DocumentRepository

    @Binds
    @Singleton
    abstract fun bindVetAppointmentRepository(
        vetAppointmentRepositoryImpl: VetAppointmentRepositoryImpl
    ): VetAppointmentRepository

    @Binds
    @Singleton
    abstract fun bindCommunityRepository(
        communityRepositoryImpl: CommunityRepositoryImpl
    ): CommunityRepository

    @Binds
    @Singleton
    abstract fun bindPetRepository(
        firestorePetRepository: FirestorePetRepository
    ): PetRepository

    @Binds
    @Singleton
    abstract fun bindMarketplaceRepository(
        marketplaceRepositoryImpl: MarketplaceRepositoryImpl
    ): MarketplaceRepository

    @Binds
    @Singleton
    abstract fun bindGenerativeAiRepository(
        generativeAiRepositoryImpl: GenerativeAiRepositoryImpl
    ): GenerativeAiRepository
}
