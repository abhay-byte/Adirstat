package com.ivarna.adirstat.di

import android.content.Context
import androidx.room.Room
import com.ivarna.adirstat.data.local.db.AdirstatDatabase
import com.ivarna.adirstat.data.local.db.ScanCacheDao
import com.ivarna.adirstat.data.local.db.ScanHistoryDao
import com.ivarna.adirstat.data.local.db.UserExclusionDao
import com.ivarna.adirstat.util.PermissionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database and dependency injection
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AdirstatDatabase {
        return Room.databaseBuilder(
            context,
            AdirstatDatabase::class.java,
            "adirstat_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideScanHistoryDao(database: AdirstatDatabase): ScanHistoryDao {
        return database.scanHistoryDao()
    }

    @Provides
    @Singleton
    fun provideScanCacheDao(database: AdirstatDatabase): ScanCacheDao {
        return database.scanCacheDao()
    }

    @Provides
    @Singleton
    fun provideUserExclusionDao(database: AdirstatDatabase): UserExclusionDao {
        return database.userExclusionDao()
    }

    @Provides
    @Singleton
    fun providePermissionManager(@ApplicationContext context: Context): PermissionManager {
        return PermissionManager(context)
    }
}
