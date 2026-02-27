package com.planwise.presentation.components

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object UiUtils {
    private val fmt = DateTimeFormatter.ofPattern("EEE, dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault())
    fun formatDateTime(millis: Long): String = fmt.format(Instant.ofEpochMilli(millis))
}
