package com.planwise.sync

import com.planwise.data.settings.SettingsRepository
import com.planwise.data.settings.SupabaseSession
import com.planwise.domain.model.DayStatus
import com.planwise.domain.model.Event
import com.planwise.domain.model.RecurrenceType
import com.planwise.domain.model.ShiftDay
import com.planwise.domain.model.ShiftType
import com.planwise.domain.repo.EventRepository
import com.planwise.domain.repo.ShiftRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CloudSyncManager @Inject constructor(
    private val settings: SettingsRepository,
    private val eventRepository: EventRepository,
    private val shiftRepository: ShiftRepository,
) {

    suspend fun syncNow(): Result<Unit> = runCatching {
        val cfg = settings.supabaseConfig.first()
        if (!cfg.enabled) return@runCatching

        val url = cfg.url.ifBlank { com.planwise.BuildConfig.PLANWISE_SUPABASE_URL }
        val key = cfg.key.ifBlank { com.planwise.BuildConfig.PLANWISE_SUPABASE_PUBLISHABLE_KEY }
        require(url.isNotBlank() && key.isNotBlank()) { "Supabase URL/Key missing" }

        val client = SupabaseClientFactory.create(url, key)

        // Restore session if available
        settings.supabaseSession.first()?.let { s ->
            client.auth.importSession(SupabaseClientFactory.toUserSession(s))
        }

        // Ensure signed in
        if (client.auth.currentUserOrNull() == null) {
            client.auth.signInAnonymously()
        }

        val session = client.auth.currentSessionOrNull()
        if (session != null) {
            settings.setSupabaseSession(
                SupabaseSession(
                    accessToken = session.accessToken,
                    refreshToken = session.refreshToken,
                    expiresIn = session.expiresIn,
                    tokenType = session.tokenType,
                )
            )
        }

        val userId = client.auth.currentUserOrNull()?.id ?: error("No user id")

        pushEvents(client, userId)
        pushShifts(client, userId)

        pullEvents(client, userId)
        pullShifts(client, userId)
    }

    private suspend fun pushEvents(client: SupabaseClient, userId: String) {
        val locals = eventRepository.getAllNow()
        val remotes = locals.map { e ->
            RemoteEvent(
                id = e.id,
                userId = userId,
                title = e.title,
                startDateTime = e.startDateTime,
                endDateTime = e.endDateTime,
                categoryId = e.categoryId,
                subcategoryId = e.subcategoryId,
                color = e.color,
                locationText = e.locationText,
                description = e.description,
                recurrenceType = e.recurrenceType.name,
                recurrenceInterval = e.recurrenceInterval,
                reminders = e.remindersMinutes,
                createdAt = e.createdAt,
                updatedAt = e.updatedAt,
                deleted = e.deleted,
            )
        }
        if (remotes.isEmpty()) return
        client.from("planwise_events").upsert(remotes) {
            onConflict = "id"
        }
    }

    private suspend fun pullEvents(client: SupabaseClient, userId: String) {
        val remote = client.from("planwise_events")
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeList<RemoteEvent>()

        remote.forEach { r ->
            val local = eventRepository.getById(r.id)
            if (local == null || r.updatedAt > local.updatedAt) {
                eventRepository.upsert(
                    Event(
                        id = r.id,
                        title = r.title,
                        startDateTime = r.startDateTime,
                        endDateTime = r.endDateTime,
                        categoryId = r.categoryId,
                        subcategoryId = r.subcategoryId,
                        color = r.color,
                        locationText = r.locationText,
                        description = r.description,
                        recurrenceType = runCatching { RecurrenceType.valueOf(r.recurrenceType) }
                            .getOrDefault(RecurrenceType.NONE),
                        recurrenceInterval = r.recurrenceInterval,
                        remindersMinutes = r.reminders,
                        createdAt = r.createdAt,
                        updatedAt = r.updatedAt,
                        deleted = r.deleted,
                    )
                )
            }
        }
    }

    private suspend fun pushShifts(client: SupabaseClient, userId: String) {
        val locals = shiftRepository.getAllNow()
        val remotes = locals.map { s ->
            RemoteShiftDay(
                date = s.dateYyyymmdd,
                userId = userId,
                shiftType = s.shiftType.name,
                dayStatus = s.dayStatus.name,
                note = s.note,
                updatedAt = s.updatedAt,
            )
        }
        if (remotes.isEmpty()) return
        client.from("planwise_shift_days").upsert(remotes) {
            onConflict = "date,user_id"
        }
    }

    private suspend fun pullShifts(client: SupabaseClient, userId: String) {
        val remote = client.from("planwise_shift_days")
            .select { filter { eq("user_id", userId) } }
            .decodeList<RemoteShiftDay>()

        val localMap = shiftRepository.getAllNow().associateBy { it.dateYyyymmdd }
        remote.forEach { r ->
            val local = localMap[r.date]
            if (local == null || r.updatedAt > local.updatedAt) {
                shiftRepository.upsert(
                    ShiftDay(
                        dateYyyymmdd = r.date,
                        shiftType = runCatching { ShiftType.valueOf(r.shiftType) }.getOrDefault(ShiftType.NONE),
                        dayStatus = runCatching { DayStatus.valueOf(r.dayStatus) }.getOrDefault(DayStatus.NORMAL),
                        note = r.note,
                        updatedAt = r.updatedAt,
                    )
                )
            }
        }
    }
}
