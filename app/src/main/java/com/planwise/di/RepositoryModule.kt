package com.planwise.di

import com.planwise.data.repo.CategoryRepositoryImpl
import com.planwise.data.repo.EventRepositoryImpl
import com.planwise.data.repo.ShiftRepositoryImpl
import com.planwise.data.settings.SettingsRepository
import com.planwise.data.settings.SettingsRepositoryImpl
import com.planwise.domain.repo.CategoryRepository
import com.planwise.domain.repo.EventRepository
import com.planwise.domain.repo.ShiftRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindEvents(impl: EventRepositoryImpl): EventRepository
    @Binds @Singleton abstract fun bindShifts(impl: ShiftRepositoryImpl): ShiftRepository
    @Binds @Singleton abstract fun bindCategories(impl: CategoryRepositoryImpl): CategoryRepository
    @Binds @Singleton abstract fun bindSettings(impl: SettingsRepositoryImpl): SettingsRepository
}
