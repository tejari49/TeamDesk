package com.planwise.domain.usecase

import com.planwise.domain.model.Event
import com.planwise.domain.repo.EventRepository
import java.util.UUID
import javax.inject.Inject

class DuplicateEventUseCase @Inject constructor(
    private val eventRepository: EventRepository,
) {
    suspend operator fun invoke(event: Event): Event {
        val now = System.currentTimeMillis()
        val dup = event.copy(
            id = UUID.randomUUID().toString(),
            createdAt = now,
            updatedAt = now,
        )
        eventRepository.upsert(dup)
        return dup
    }
}
