package com.planwise.di

import android.content.Context
import androidx.room.Room
import com.planwise.data.db.PlanWiseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext context: Context): PlanWiseDatabase =
        Room.databaseBuilder(context, PlanWiseDatabase::class.java, "planwise.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideEventDao(db: PlanWiseDatabase) = db.eventDao()
    @Provides fun provideShiftDao(db: PlanWiseDatabase) = db.shiftDao()
    @Provides fun provideCategoryDao(db: PlanWiseDatabase) = db.categoryDao()
}
