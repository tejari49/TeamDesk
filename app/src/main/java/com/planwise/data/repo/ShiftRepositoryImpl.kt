package com.planwise.data.repo

import com.planwise.data.db.dao.ShiftDao
import com.planwise.data.mappers.toDomain
import com.planwise.data.mappers.toEntity
import com.planwise.domain.model.ShiftDay
import com.planwise.domain.repo.ShiftRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShiftRepositoryImpl @Inject constructor(
    private val shiftDao: ShiftDao,
) : ShiftRepository {
    override fun observeAll(): Flow<List<ShiftDay>> =
        shiftDao.observeAll().map { it.map { e -> e.toDomain() } }

    override suspend fun upsert(day: ShiftDay) {
        shiftDao.upsert(day.toEntity())
    }

    override suspend fun getAllNow(): List<ShiftDay> =
        shiftDao.getAllNow().map { it.toDomain() }
}
