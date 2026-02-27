package com.planwise.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.planwise.domain.model.Event
import com.planwise.domain.usecase.ComputeNextOccurrenceUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val nextOccurrence: ComputeNextOccurrenceUseCase,
) {

    fun scheduleForEvent(event: Event) {
        if (event.deleted) return
        if (event.remindersMinutes.isEmpty()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()
        val baseStart = nextOccurrence.nextStartAtOrAfter(event, now)

        val offsets = event.remindersMinutes.distinct().sortedDescending()
        for (offsetMin in offsets) {
            val triggerAt = baseStart - TimeUnit.MINUTES.toMillis(offsetMin.toLong())
            if (triggerAt < now) continue
            val pi = reminderPendingIntent(eventId = event.id, baseStart = baseStart, offsetMin = offsetMin)
            setAlarm(alarmManager, triggerAt, pi)
        }
    }

    fun cancelForEvent(eventId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val common = listOf(0, 5, 10, 15, 30, 60, 120, 1440, 2880, 10080)
        for (offset in common) {
            val pi = reminderPendingIntent(eventId, baseStart = 0L, offsetMin = offset, create = false) ?: continue
            alarmManager.cancel(pi)
        }
    }

    private fun setAlarm(alarmManager: AlarmManager, triggerAt: Long, pi: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    private fun reminderPendingIntent(
        eventId: String,
        baseStart: Long,
        offsetMin: Int,
        create: Boolean = true
    ): PendingIntent? {
        val intent = Intent(context, EventReminderReceiver::class.java).apply {
            action = EventReminderReceiver.ACTION_REMINDER
            putExtra(EventReminderReceiver.EXTRA_EVENT_ID, eventId)
            putExtra(EventReminderReceiver.EXTRA_BASE_START, baseStart)
            putExtra(EventReminderReceiver.EXTRA_OFFSET_MINUTES, offsetMin)
        }
        val requestCode = (eventId.hashCode() * 31 + offsetMin).toInt()
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return if (create) {
            PendingIntent.getBroadcast(context, requestCode, intent, flags)
        } else {
            PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
