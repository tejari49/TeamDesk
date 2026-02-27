package com.planwise.data.export

import android.content.Context
import androidx.core.content.FileProvider
import com.planwise.domain.repo.EventRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class IcsExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val eventRepository: EventRepository,
) {

    private val dtFmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC)

    suspend fun exportIcsToCache(): android.net.Uri = withContext(Dispatchers.IO) {
        val events = eventRepository.getAllNow().filter { !it.deleted }
        val sb = StringBuilder()
        sb.appendLine("BEGIN:VCALENDAR")
        sb.appendLine("VERSION:2.0")
        sb.appendLine("PRODID:-//PlanWise//EN")
        events.forEach { e ->
            sb.appendLine("BEGIN:VEVENT")
            sb.appendLine("UID:${e.id}")
            sb.appendLine("DTSTAMP:${dtFmt.format(Instant.ofEpochMilli(e.updatedAt))}")
            sb.appendLine("SUMMARY:${escape(e.title)}")
            sb.appendLine("DTSTART:${dtFmt.format(Instant.ofEpochMilli(e.startDateTime))}")
            e.endDateTime?.let { sb.appendLine("DTEND:${dtFmt.format(Instant.ofEpochMilli(it))}") }
            e.locationText?.takeIf { it.isNotBlank() }?.let { sb.appendLine("LOCATION:${escape(it)}") }
            e.description?.takeIf { it.isNotBlank() }?.let { sb.appendLine("DESCRIPTION:${escape(it)}") }
            sb.appendLine("END:VEVENT")
        }
        sb.appendLine("END:VCALENDAR")

        val file = File(context.cacheDir, "planwise_events.ics")
        file.writeText(sb.toString(), Charsets.UTF_8)
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun escape(s: String): String =
        s.replace("\\", "\\\\").replace("\n", "\\n").replace(",", "\\,").replace(";", "\\;")
}
