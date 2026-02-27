package com.planwise.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val CHANNEL_REMINDERS = "planwise_reminders"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_REMINDERS,
            "PlanWise Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Local reminders for PlanWise events"
            enableVibration(true)
        }
        nm.createNotificationChannel(channel)
    }
}
