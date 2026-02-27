package com.planwise.domain.repo

import com.planwise.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun observeAll(): Flow<List<Event>>
    suspend fun getById(id: String): Event?
    suspend fun upsert(event: Event)
    suspend fun softDelete(id: String)
    suspend fun getAllNow(): List<Event>
}
