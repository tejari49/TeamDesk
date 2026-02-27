package com.planwise.di

import com.planwise.widget.WidgetUpdater
import com.planwise.widget.WidgetUpdaterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetModule {
    @Binds @Singleton abstract fun bindWidgetUpdater(impl: WidgetUpdaterImpl): WidgetUpdater
}
