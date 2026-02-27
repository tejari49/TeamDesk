package com.planwise.domain.usecase

import com.planwise.domain.model.Event
import com.planwise.domain.model.RecurrenceType
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

class ComputeNextOccurrenceUseCase @Inject constructor() {

    fun nextStartAtOrAfter(event: Event, nowMillis: Long): Long {
        val base = event.startDateTime
        if (event.recurrenceType == RecurrenceType.NONE) return base
        val interval = event.recurrenceInterval.coerceAtLeast(1)
        var candidate = base
        val maxOffset = (event.remindersMinutes.maxOrNull() ?: 0) * 60_000L
        val threshold = nowMillis - maxOffset
        var guard = 0
        while (candidate < threshold && guard < 10_000) {
            candidate = addInterval(candidate, event.recurrenceType, interval)
            guard++
        }
        return candidate
    }

    fun addInterval(startMillis: Long, type: RecurrenceType, interval: Int): Long {
        val zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(startMillis), ZoneId.systemDefault())
        val next = when (type) {
            RecurrenceType.DAILY -> zdt.plusDays(interval.toLong())
            RecurrenceType.WEEKLY -> zdt.plusWeeks(interval.toLong())
            RecurrenceType.MONTHLY -> zdt.plusMonths(interval.toLong())
            RecurrenceType.NONE -> zdt
        }
        return next.toInstant().toEpochMilli()
    }
}
