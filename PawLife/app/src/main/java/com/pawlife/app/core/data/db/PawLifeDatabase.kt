package com.pawlife.app.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pawlife.app.core.data.db.dao.ActivityDao
import com.pawlife.app.core.data.db.dao.DocumentDao
import com.pawlife.app.core.data.db.dao.FeedingDao
import com.pawlife.app.core.data.db.dao.MedicationDao
import com.pawlife.app.core.data.db.dao.PetDao
import com.pawlife.app.core.data.db.dao.WeightDao
import com.pawlife.app.core.data.db.dao.VetAppointmentDao
import com.pawlife.app.core.data.db.dao.CommunityPostDao
import com.pawlife.app.core.data.db.dao.MarketplaceDao
import com.pawlife.app.core.data.db.entity.ActivityLogEntity
import com.pawlife.app.core.data.db.entity.DocumentEntity
import com.pawlife.app.core.data.db.entity.FeedingLogEntity
import com.pawlife.app.core.data.db.entity.FeedingScheduleEntity
import com.pawlife.app.core.data.db.entity.MedicationEntity
import com.pawlife.app.core.data.db.entity.PetEntity
import com.pawlife.app.core.data.db.entity.WeightRecordEntity
import com.pawlife.app.core.data.db.entity.VetAppointmentEntity
import com.pawlife.app.core.data.db.entity.CommunityPostEntity
import com.pawlife.app.core.data.db.entity.MarketplaceListingEntity

@Database(
    entities = [
        PetEntity::class,
        WeightRecordEntity::class,
        FeedingScheduleEntity::class,
        FeedingLogEntity::class,
        MedicationEntity::class,
        ActivityLogEntity::class,
        DocumentEntity::class,
        VetAppointmentEntity::class,
        CommunityPostEntity::class,
        MarketplaceListingEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class PawLifeDatabase : RoomDatabase() {
    abstract val petDao: PetDao
    abstract val weightDao: WeightDao
    abstract val feedingDao: FeedingDao
    abstract val medicationDao: MedicationDao
    abstract val activityDao: ActivityDao
    abstract val documentDao: DocumentDao
    abstract val vetAppointmentDao: VetAppointmentDao
    abstract val communityPostDao: CommunityPostDao
    abstract val marketplaceDao: MarketplaceDao
}
