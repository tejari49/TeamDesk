package com.planwise.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.planwise.domain.repo.EventRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var eventRepository: EventRepository
    @Inject lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.IO).launch {
            val events = eventRepository.getAllNow()
            val now = System.currentTimeMillis()
            events.filter { !it.deleted && (it.startDateTime + 14L * 24 * 60 * 60 * 1000) >= now }
                .forEach { scheduler.scheduleForEvent(it) }
        }
    }
}
