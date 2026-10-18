package com.pawlife.app.core.di

import android.app.Application
import androidx.room.Room
import com.pawlife.app.core.data.db.PawLifeDatabase
import com.pawlife.app.core.data.db.dao.ActivityDao
import com.pawlife.app.core.data.db.dao.DocumentDao
import com.pawlife.app.core.data.db.dao.FeedingDao
import com.pawlife.app.core.data.db.dao.MedicationDao
import com.pawlife.app.core.data.db.dao.PetDao
import com.pawlife.app.core.data.db.dao.VetAppointmentDao
import com.pawlife.app.core.data.db.dao.CommunityPostDao
import com.pawlife.app.core.data.db.dao.MarketplaceDao
import com.pawlife.app.core.data.db.dao.WeightDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePawLifeDatabase(app: Application): PawLifeDatabase {
        return Room.databaseBuilder(
            app,
            PawLifeDatabase::class.java,
            "pawlife.db"
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    @Singleton
    fun providePetDao(db: PawLifeDatabase): PetDao = db.petDao

    @Provides
    @Singleton
    fun provideWeightDao(db: PawLifeDatabase): WeightDao = db.weightDao

    @Provides
    @Singleton
    fun provideFeedingDao(db: PawLifeDatabase): FeedingDao = db.feedingDao

    @Provides
    @Singleton
    fun provideMedicationDao(db: PawLifeDatabase): MedicationDao = db.medicationDao

    @Provides
    @Singleton
    fun provideActivityDao(db: PawLifeDatabase): ActivityDao = db.activityDao

    @Provides
    @Singleton
    fun provideDocumentDao(db: PawLifeDatabase): DocumentDao = db.documentDao

    @Provides
    @Singleton
    fun provideVetAppointmentDao(db: PawLifeDatabase): VetAppointmentDao = db.vetAppointmentDao

    @Provides
    @Singleton
    fun provideCommunityPostDao(db: PawLifeDatabase): CommunityPostDao = db.communityPostDao

    @Provides
    @Singleton
    fun provideMarketplaceDao(db: PawLifeDatabase): MarketplaceDao = db.marketplaceDao
}
