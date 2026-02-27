package com.planwise.reminders

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.planwise.MainActivity
import com.planwise.domain.repo.EventRepository
import com.planwise.domain.usecase.ComputeNextOccurrenceUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class EventReminderReceiver : BroadcastReceiver() {

    @Inject lateinit var eventRepository: EventRepository
    @Inject lateinit var scheduler: ReminderScheduler
    @Inject lateinit var nextOccurrence: ComputeNextOccurrenceUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REMINDER) return

        val eventId = intent.getStringExtra(EXTRA_EVENT_ID) ?: return
        val baseStart = intent.getLongExtra(EXTRA_BASE_START, 0L)

        CoroutineScope(Dispatchers.IO).launch {
            val event = eventRepository.getById(eventId) ?: return@launch
            if (event.deleted) return@launch

            showNotification(context, event.title, baseStart.takeIf { it > 0L } ?: event.startDateTime)

            if (event.recurrenceType != com.planwise.domain.model.RecurrenceType.NONE) {
                val now = System.currentTimeMillis()
                val nextStart = nextOccurrence.addInterval(
                    baseStart.takeIf { it > 0L } ?: event.startDateTime,
                    event.recurrenceType,
                    event.recurrenceInterval.coerceAtLeast(1)
                )
                if (nextStart > now) {
                    val nextEvent = event.copy(startDateTime = nextStart)
                    scheduler.scheduleForEvent(nextEvent)
                }
            }
        }
    }

    private fun showNotification(context: Context, title: String, startMillis: Long) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val formatter = DateTimeFormatter.ofPattern("EEE, dd.MM.yyyy HH:mm")
            .withZone(ZoneId.systemDefault())
        val contentText = formatter.format(Instant.ofEpochMilli(startMillis))

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPi = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(contentText)
            .setContentIntent(openPi)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        nm.notify(title.hashCode(), notification)
    }

    companion object {
        const val ACTION_REMINDER = "com.planwise.ACTION_REMINDER"
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_BASE_START = "extra_base_start"
        const val EXTRA_OFFSET_MINUTES = "extra_offset_minutes"
    }
}
