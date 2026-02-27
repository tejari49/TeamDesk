package com.planwise.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.planwise.data.db.entity.ShiftDayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shift_days")
    fun observeAll(): Flow<List<ShiftDayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ShiftDayEntity)

    @Query("SELECT * FROM shift_days")
    suspend fun getAllNow(): List<ShiftDayEntity>
}
