package com.planwise.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.planwise.domain.repo.EventRepository
import com.planwise.domain.repo.ShiftRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

class ExportImportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val eventRepository: EventRepository,
    private val shiftRepository: ShiftRepository,
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun exportToCacheFile(): Uri = withContext(Dispatchers.IO) {
        val bundle = ExportBundle(
            exportedAt = System.currentTimeMillis(),
            events = eventRepository.getAllNow().map { e ->
                ExportEvent(
                    id = e.id,
                    title = e.title,
                    startDateTime = e.startDateTime,
                    endDateTime = e.endDateTime,
                    categoryId = e.categoryId,
                    subcategoryId = e.subcategoryId,
                    color = e.color,
                    locationText = e.locationText,
                    description = e.description,
                    recurrenceType = e.recurrenceType,
                    recurrenceInterval = e.recurrenceInterval,
                    remindersMinutes = e.remindersMinutes,
                    createdAt = e.createdAt,
                    updatedAt = e.updatedAt,
                    deleted = e.deleted,
                )
            },
            shifts = shiftRepository.getAllNow().map { s ->
                ExportShiftDay(
                    dateYyyymmdd = s.dateYyyymmdd,
                    shiftType = s.shiftType,
                    dayStatus = s.dayStatus,
                    note = s.note,
                    updatedAt = s.updatedAt,
                )
            }
        )

        val content = json.encodeToString(ExportBundle.serializer(), bundle)
        val file = File(context.cacheDir, "planwise_export_${bundle.exportedAt}.json")
        file.writeText(content, Charsets.UTF_8)
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    suspend fun importFromUri(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                ?: error("Cannot read file")
            val bundle = json.decodeFromString(ExportBundle.serializer(), text)

            bundle.events.forEach { e ->
                eventRepository.upsert(
                    com.planwise.domain.model.Event(
                        id = e.id,
                        title = e.title,
                        startDateTime = e.startDateTime,
                        endDateTime = e.endDateTime,
                        categoryId = e.categoryId,
                        subcategoryId = e.subcategoryId,
                        color = e.color,
                        locationText = e.locationText,
                        description = e.description,
                        recurrenceType = e.recurrenceType,
                        recurrenceInterval = e.recurrenceInterval,
                        remindersMinutes = e.remindersMinutes,
                        createdAt = e.createdAt,
                        updatedAt = e.updatedAt,
                        deleted = e.deleted,
                    )
                )
            }
            bundle.shifts.forEach { s ->
                shiftRepository.upsert(
                    com.planwise.domain.model.ShiftDay(
                        dateYyyymmdd = s.dateYyyymmdd,
                        shiftType = s.shiftType,
                        dayStatus = s.dayStatus,
                        note = s.note,
                        updatedAt = s.updatedAt,
                    )
                )
            }
        }
    }
}
