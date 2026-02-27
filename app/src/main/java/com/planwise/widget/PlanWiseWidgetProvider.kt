package com.planwise.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.planwise.MainActivity
import com.planwise.R
import com.planwise.domain.repo.EventRepository
import com.planwise.domain.repo.ShiftRepository
import com.planwise.domain.usecase.ComputeNextOccurrenceUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class PlanWiseWidgetProvider : AppWidgetProvider() {

    @Inject lateinit var eventRepository: EventRepository
    @Inject lateinit var shiftRepository: ShiftRepository
    @Inject lateinit var nextOccurrence: ComputeNextOccurrenceUseCase

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        CoroutineScope(Dispatchers.IO).launch {
            val events = eventRepository.getAllNow().filter { !it.deleted }
            val now = System.currentTimeMillis()
            val next = events.map { it.copy(startDateTime = nextOccurrence.nextStartAtOrAfter(it, now)) }
                .filter { it.startDateTime >= now }
                .minByOrNull { it.startDateTime }

            val todayInt = LocalDate.now().let { it.year * 10000 + it.monthValue * 100 + it.dayOfMonth }
            val shifts = shiftRepository.getAllNow().associateBy { it.dateYyyymmdd }
            val todayShift = shifts[todayInt]

            val formatter = DateTimeFormatter.ofPattern("dd.MM HH:mm").withZone(ZoneId.systemDefault())
            val nextText = if (next == null) context.getString(R.string.no_upcoming_events)
            else "${next.title} • ${formatter.format(Instant.ofEpochMilli(next.startDateTime))}"

            val shiftText = if (todayShift == null) "Shift: —"
            else "Shift: ${todayShift.shiftType.name} / ${todayShift.dayStatus.name}"

            appWidgetIds.forEach { widgetId ->
                val rv = RemoteViews(context.packageName, R.layout.planwise_widget)
                rv.setTextViewText(R.id.widget_next_event, nextText)
                rv.setTextViewText(R.id.widget_shift, shiftText)

                val openIntent = Intent(context, MainActivity::class.java)
                val pi = PendingIntent.getActivity(
                    context,
                    0,
                    openIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                rv.setOnClickPendingIntent(R.id.widget_title, pi)
                rv.setOnClickPendingIntent(R.id.widget_next_event, pi)
                rv.setOnClickPendingIntent(R.id.widget_shift, pi)

                appWidgetManager.updateAppWidget(widgetId, rv)
            }
        }
    }
}
