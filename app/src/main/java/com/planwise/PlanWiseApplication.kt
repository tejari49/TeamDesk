package com.planwise

import android.app.Application
import com.planwise.reminders.NotificationChannels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PlanWiseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureChannels(this)
    }
}
