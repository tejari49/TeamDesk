package com.planwise.widget

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WidgetUpdaterImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WidgetUpdater {
    override fun requestUpdate() {
        val intent = Intent(context, PlanWiseWidgetProvider::class.java).apply {
            action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        context.sendBroadcast(intent)
    }
}
