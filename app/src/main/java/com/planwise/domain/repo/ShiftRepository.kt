package com.planwise.domain.repo

import com.planwise.domain.model.ShiftDay
import kotlinx.coroutines.flow.Flow

interface ShiftRepository {
    fun observeAll(): Flow<List<ShiftDay>>
    suspend fun upsert(day: ShiftDay)
    suspend fun getAllNow(): List<ShiftDay>
}
