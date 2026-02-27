package com.planwise.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.planwise.data.db.dao.CategoryDao
import com.planwise.data.db.dao.EventDao
import com.planwise.data.db.dao.ShiftDao
import com.planwise.data.db.entity.CategoryEntity
import com.planwise.data.db.entity.EventEntity
import com.planwise.data.db.entity.ShiftDayEntity
import com.planwise.data.db.entity.SubcategoryEntity

@Database(
    entities = [EventEntity::class, ShiftDayEntity::class, CategoryEntity::class, SubcategoryEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class PlanWiseDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun shiftDao(): ShiftDao
    abstract fun categoryDao(): CategoryDao
}
