package com.planwise.data.repo

import com.planwise.data.db.dao.EventDao
import com.planwise.data.mappers.toDomain
import com.planwise.data.mappers.toEntity
import com.planwise.domain.model.Event
import com.planwise.domain.repo.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
) : EventRepository {

    override fun observeAll(): Flow<List<Event>> =
        eventDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): Event? =
        eventDao.getById(id)?.toDomain()

    override suspend fun upsert(event: Event) {
        eventDao.upsert(event.toEntity())
    }

    override suspend fun softDelete(id: String) {
        eventDao.softDelete(id, System.currentTimeMillis())
    }

    override suspend fun getAllNow(): List<Event> =
        eventDao.getAllNow().map { it.toDomain() }
}
