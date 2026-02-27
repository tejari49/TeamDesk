package com.planwise.domain.usecase

import com.planwise.domain.model.Event
import com.planwise.domain.model.RecurrenceType
import com.planwise.domain.repo.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.Test

private class FakeEventRepo : EventRepository {
    val saved = mutableListOf<Event>()
    override fun observeAll(): Flow<List<Event>> = emptyFlow()
    override suspend fun getById(id: String): Event? = saved.firstOrNull { it.id == id }
    override suspend fun upsert(event: Event) { saved.add(event) }
    override suspend fun softDelete(id: String) {}
    override suspend fun getAllNow(): List<Event> = saved.toList()
}

class DuplicateEventUseCaseTest {
    @Test
    fun duplicate_createsNewId() = runBlocking {
        val repo = FakeEventRepo()
        val useCase = DuplicateEventUseCase(repo)
        val e = Event(
            id = "id1",
            title = "T",
            startDateTime = 1L,
            endDateTime = null,
            categoryId = "other",
            subcategoryId = null,
            color = 0,
            locationText = null,
            description = null,
            recurrenceType = RecurrenceType.NONE,
            recurrenceInterval = 1,
            remindersMinutes = listOf(60),
            createdAt = 1L,
            updatedAt = 1L,
            deleted = false
        )
        val dup = useCase(e)
        assertNotEquals(e.id, dup.id)
    }
}
